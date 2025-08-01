package com.example.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import jakarta.mail.MessagingException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.entity.PasswordResetToken;
import com.example.entity.PreRegistration;
import com.example.entity.User;
import com.example.enums.MailTemplate;
import com.example.error.BusinessException;
import com.example.mapper.UserMapper;
import com.example.request.EmailChangeRequest;
import com.example.request.PasswordResetMailRequest;
import com.example.request.PasswordResetUpdateRequest;
import com.example.request.RegisterUserRequest;
import com.example.support.MailGateway;
import com.example.util.JwtUtil;
import com.example.util.RandomTokenUtil;

@SpringBootTest
@Transactional
class AuthServiceTest {
    /*    // TODO:
    ・トランザクションありだから@SpringBootTestを使用してるが、テストフェーズの定義どう考えるか？
    ・@SpringBootTestのパフォーマンス調べる
    */

    @MockitoBean
    AuthenticationManager manager;

    @MockitoBean
    JwtUtil jwtUtil;

    @Autowired
    AuthService authService;

    @MockitoSpyBean
    UserMapper userMapper;

    @MockitoBean
    MailGateway gateway;

    @MockitoSpyBean
    PasswordEncoder encoder;

    String email = "foo@example.com";

    String fixedToken = "a".repeat(22);

    String hashed = RandomTokenUtil.hash(fixedToken);

    LocalDateTime ld = LocalDateTime.of(2025, 7, 2, 10, 40, 5);

    @Nested
    class SendRegistrationUrl {

        @Test
        void sendRegistrationUrl_success() throws MessagingException {
            // TODO:
            // モックと実DBが混ざってる、どう書くのがよいか？
            try (MockedConstruction<MimeMessageHelper> mocked = Mockito.mockConstruction(MimeMessageHelper.class,
                    (mock, ctx) -> {
                        // setter 系は特に何もしない
                    });
                    MockedStatic<RandomTokenUtil> rnd = Mockito.mockStatic(RandomTokenUtil.class)) {
                rnd.when(RandomTokenUtil::generate).thenReturn(fixedToken);
                rnd.when(() -> RandomTokenUtil.hash(fixedToken)).thenReturn(hashed);
                authService.sendRegistrationUrl(email);

                PreRegistration pr = userMapper.selectPreRegistrationByPrimaryKey(hashed);
                assertThat(pr.getToken()).hasSize(64);
                assertThat(pr.getEmail()).isEqualTo(email);
                assertThat(pr.getExpiresAt()).isAfter(LocalDateTime.now());

                verify(gateway).send(argThat(msg -> msg.subject().equals(MailTemplate.REGISTRATION.getSubject())));
                ;
            }
        }

        @Test
        void sendRegistrationUrl_alreadyRegistered() {
            User user = new User(); // Lombok の @Data がデフォルトコンストラクタを生成
            user.setUserId(UUID.randomUUID().toString());
            user.setEmail(email);
            user.setPasswordHash("$2b$10$seiTk5/jHFtb3fG2smcP.ukTJa8Yw9gaO9Izl8QY0ZObcURI.1Jma");
            user.setLastName("山田");
            user.setFirstName("太郎");
            user.setLastNameKana("ヤマダ");
            user.setFirstNameKana("タロウ");
            user.setPostalCode("1500001");
            user.setAddressPrefCity("東京都渋谷区");
            user.setAddressArea("神南一丁目");
            user.setAddressBlock("1-19-11");
            user.setAddressBuilding("マンション101");
            user.setPhoneNumber("0312345678");
            user.setBirthday(LocalDate.of(1990, 1, 1));
            user.setGender("M");
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.insertUser(user);

            assertThatThrownBy(() -> authService.sendRegistrationUrl(email))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.CONFLICT);
        }

        //        @Test
        //        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        //        void sendRegistrationUrl_mailFail() {
        //            doThrow(new MailSendException("smtp down")).when(sender).send(any(MimeMessage.class));
        //
        //            try (MockedConstruction<MimeMessageHelper> mocked = Mockito.mockConstruction(MimeMessageHelper.class,
        //                    (mock, ctx) -> {
        //                        // setter 系は特に何もしない
        //                    });
        //                    MockedStatic<RandomTokenUtil> rnd = Mockito.mockStatic(RandomTokenUtil.class)) {
        //                rnd.when(RandomTokenUtil::generate).thenReturn(fixedToken);
        //                rnd.when(() -> RandomTokenUtil.hash(fixedToken)).thenReturn(hashed);
        //                
        //                assertThatThrownBy(() -> authService.sendRegistrationUrl(email))
        //                        .isInstanceOf(MailSendException.class);
        //
        //                PreRegistration p = userMapper.selectPreRegistrationByPrimaryKey(fixedToken);
        //                assertThat(p).isNull();
        //            }
        //        }
    }

    @Nested
    class register {
        PreRegistration pr;
        String token;
        RegisterUserRequest req;

        @BeforeEach
        void setup() {
            token = RandomTokenUtil.generate();
            LocalDateTime exp = LocalDateTime.now().plusMinutes(20);
            pr = new PreRegistration();
            pr.setToken(token);
            pr.setEmail(email);
            pr.setExpiresAt(exp);

            req = new RegisterUserRequest(
                    /* token */ token,
                    /* email */ "foo@example.com",
                    /* password */ "Password1", // 8–20 英数字
                    /* lastName */ "山田",
                    /* firstName */ "太郎",
                    /* lastNameKana */ "ヤマダ",
                    /* firstNameKana */ "タロウ",
                    /* postalCode */ "1500041",
                    /* addressPrefCity */ "東京都渋谷区",
                    /* addressArea */ "神南一丁目",
                    /* addressBlock */ "1-19-11",
                    /* addressBuilding */ null, // 任意項目
                    /* phoneNumber */ "0312345678",
                    /* birthday */ LocalDate.of(1990, 4, 15),
                    /* gender */ "M");
        }

        @Test
        void register_success() {

            doReturn(pr).when(userMapper).selectPreRegistrationByPrimaryKey(token);
            doReturn(1).when(userMapper).insertUser(any(User.class));
            doReturn(1).when(userMapper).deletePreRegistrationByPrimaryKey(token);

            UUID uuid = UUID.randomUUID();

            try (MockedStatic<UUID> rnd = Mockito.mockStatic(UUID.class)) {
                rnd.when(UUID::randomUUID).thenReturn(uuid);

                authService.register(req);

                ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
                verify(userMapper).insertUser(captor.capture());

                User saved = captor.getValue();

                assertThat(saved)
                        .extracting(
                                User::getUserId,
                                User::getEmail,
                                User::getPasswordHash,
                                User::getLastName,
                                User::getFirstName,
                                User::getLastNameKana,
                                User::getFirstNameKana,
                                User::getPostalCode,
                                User::getAddressPrefCity,
                                User::getAddressArea,
                                User::getAddressBlock,
                                User::getAddressBuilding,
                                User::getPhoneNumber,
                                User::getBirthday,
                                User::getGender)
                        .containsExactly(
                                /* userId           */ saved.getUserId(), // UUID はランダムなのでそのまま
                                /* email            */ "foo@example.com",
                                /* passwordHash     */ saved.getPasswordHash(), // ↓後で個別検証
                                /* lastName         */ "山田",
                                /* firstName        */ "太郎",
                                /* lastNameKana     */ "ヤマダ",
                                /* firstNameKana    */ "タロウ",
                                /* postalCode       */ "1500041",
                                /* addressPrefCity  */ "東京都渋谷区",
                                /* addressArea      */ "神南一丁目",
                                /* addressBlock     */ "1-19-11",
                                /* addressBuilding  */ null,
                                /* phoneNumber      */ "0312345678",
                                /* birthday         */ LocalDate.of(1990, 4, 15),
                                /* gender           */ "M");
            }

            verify(userMapper).deletePreRegistrationByPrimaryKey(token);
        }

        @Test
        void register_tokenNotFound() {
            String token = RandomTokenUtil.generate();
            doReturn(null).when(userMapper).selectPreRegistrationByPrimaryKey(token);

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.NOT_FOUND);
        }

        @Test
        void register_emailNotMatch() {
            String token = RandomTokenUtil.generate();
            pr.setEmail("test@gmail.com");
            doReturn(pr).when(userMapper).selectPreRegistrationByPrimaryKey(token);

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.NOT_FOUND);
        }

        @Test
        void register_alreadyRegistered() {
            String token = RandomTokenUtil.generate();
            UUID uuid = UUID.randomUUID();
            User user = new User(); // Lombok の @Data がデフォルトコンストラクタを生成
            user.setUserId(uuid.toString());
            user.setEmail("taro.yamada@example.com");
            user.setPasswordHash("$2a$10$abcxyz...hash...");
            user.setLastName("山田");
            user.setFirstName("太郎");
            user.setLastNameKana("ヤマダ");
            user.setFirstNameKana("タロウ");
            user.setPostalCode("1500001");
            user.setAddressPrefCity("東京都渋谷区");
            user.setAddressArea("神南一丁目");
            user.setAddressBlock("1-19-11");
            user.setAddressBuilding("マンション101");
            user.setPhoneNumber("0312345678");
            user.setBirthday(LocalDate.of(1990, 1, 1));
            user.setGender("M");
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            userMapper.insertUser(user);
            doReturn(pr).when(userMapper).selectPreRegistrationByPrimaryKey(token);

            try (MockedStatic<UUID> rnd = Mockito.mockStatic(UUID.class)) {
                rnd.when(UUID::randomUUID).thenReturn(uuid);

                assertThatThrownBy(() -> authService.register(req))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.NOT_FOUND);
            }
        }
    }

    @Nested
    class SendPasswordRestMail {
        PasswordResetMailRequest req;

        @BeforeEach
        void setup() {
            req = new PasswordResetMailRequest() {
                {
                    setEmail("sample@example.com");
                    setBirthday(LocalDate.of(1990, 4, 15));
                }
            };
        }

        @Test
        void sendPasswordRestMail_success() {
            LocalDateTime fixed = LocalDateTime.of(2025, 7, 2, 10, 43, 2);
            long expireMin = (long) ReflectionTestUtils.getField(authService, "expireMin");
            try (MockedStatic<RandomTokenUtil> rnd = Mockito.mockStatic(RandomTokenUtil.class);
                    MockedStatic<LocalDateTime> time = Mockito.mockStatic(LocalDateTime.class,
                            Mockito.CALLS_REAL_METHODS);) {
                rnd.when(RandomTokenUtil::generate).thenReturn(fixedToken);
                rnd.when(() -> RandomTokenUtil.hash(fixedToken)).thenReturn(hashed);
                time.when(LocalDateTime::now).thenReturn(fixed);

                authService.sendPasswordRestMail(req);

                PasswordResetToken token = userMapper.selectPasswordResetTokenByPrimaryKey(hashed);

                assertThat(token).extracting(
                        PasswordResetToken::getTokenHash,
                        PasswordResetToken::getEmail,
                        PasswordResetToken::getExpiresAt)
                        .containsExactly(
                                hashed,
                                "sample@example.com",
                                fixed.plusMinutes(expireMin));

                verify(gateway).send(assertArg(msg -> msg.subject().equals(MailTemplate.PASSWORD_RESET.getSubject())));
            }
        }

        @Test
        void sendPasswordRestMail_userNotFound() {
            req.setEmail("invalid");

            assertThatThrownBy(() -> authService.sendPasswordRestMail(req))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
        }

        @Test
        void sendPasswordRestMail_birthdayMismatch() {
            req.setBirthday(LocalDate.of(1995, 4, 16));

            assertThatThrownBy(() -> authService.sendPasswordRestMail(req))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    class ResetPassword {
        PasswordResetUpdateRequest req;
        PasswordResetToken tk;
        String raw = "a".repeat(22);
        String hashed = RandomTokenUtil.hash(raw);

        MockedStatic<RandomTokenUtil> rnd;
        MockedStatic<LocalDateTime> time;

        @BeforeEach
        void setup() {

            req = new PasswordResetUpdateRequest() {
                {
                    setToken(raw);
                    setEmail("test@sample.com");
                    setNewPassword("testpass");
                    setConfirmPassword("testpass");
                    setToken(raw);
                }
            };
            tk = new PasswordResetToken() {
                {
                    setTokenHash(hashed);
                    setEmail("test@sample.com");
                    setExpiresAt(LocalDateTime.of(2025, 7, 2, 10, 42, 5));
                }
            };
            rnd = Mockito.mockStatic(RandomTokenUtil.class);
            time = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS);
            rnd.when(() -> RandomTokenUtil.hash(raw)).thenReturn(hashed);
            time.when(LocalDateTime::now).thenReturn(ld);

            doReturn(tk).when(userMapper).selectPasswordResetTokenByPrimaryKey(hashed);
        }

        @AfterEach
        void tearDown() {
            rnd.close();
            time.close();
        }

        @Test
        void resetPassword_tokenNotFound() {
            doReturn(null).when(userMapper).selectPasswordResetTokenByPrimaryKey(hashed);
            assertThatThrownBy(() -> authService.resetPassword(req))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
        }

        @Test
        void resetPassword_emailMismatch() {
            req.setEmail("test2@sample.com");
            assertThatThrownBy(() -> authService.resetPassword(req))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
        }

        static Stream<Arguments> provideTokenExpiryArguments() {
            return Stream.of(
                    Arguments.of(LocalDateTime.of(2025, 7, 2, 10, 40, 0), false),
                    Arguments.of(LocalDateTime.of(2025, 7, 2, 10, 42, 5), false),
                    Arguments.of(LocalDateTime.of(2025, 7, 2, 10, 44, 2), true));
        }

        @ParameterizedTest
        @MethodSource("provideTokenExpiryArguments")
        void resetPassword_isExpired(LocalDateTime now, boolean expected) {
            time.when(LocalDateTime::now).thenReturn(now);

            if (expected) {
                assertThatThrownBy(() -> authService.resetPassword(req))
                        .isInstanceOf(ResponseStatusException.class)
                        .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
            } else {
                assertThatCode(() -> authService.resetPassword(req))
                        .doesNotThrowAnyException();
            }
        }

        @Test
        void resetPassword_success() {

            doReturn("encode").when(encoder).encode(anyString());
            doReturn(1).when(userMapper).updatePasswordByEmail(anyString(), anyString());
            doReturn(1).when(userMapper).deletePasswordResetToken(anyString());

            authService.resetPassword(req);

            verify(encoder).encode("testpass");
            verify(userMapper).updatePasswordByEmail("test@sample.com", "encode");
            verify(userMapper).deletePasswordResetToken(hashed);
        }
    }

    @Nested
    class RequestEmailChange {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        EmailChangeRequest req;

        @BeforeEach
        void setup() {
            req = new EmailChangeRequest() {
                {
                    setNewEmail("test@example.com");
                    setConfirmEmail("test@example.com");
                }
            };
        }

        @Test
        void requestEmailChange_sameEmail() {
            req.setNewEmail("sample@example.com");
            req.setConfirmEmail("sample@example.com");

            assertThatThrownBy(() -> authService.requestEmailChange(userId, req))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.BAD_REQUEST)
                    .hasFieldOrPropertyWithValue("errorCode", "EMAIL_SAME");
        }

        @Test
        void requestEmailChange_emailAlreadyUsed() {
            req.setNewEmail("bob2@example.com");
            req.setConfirmEmail("bob2@example.com");

            assertThatThrownBy(() -> authService.requestEmailChange(userId, req))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
        }

        @Test
        void requestEmailChange_success() {
            try (
                    MockedStatic<RandomTokenUtil> rnd = Mockito.mockStatic(RandomTokenUtil.class,
                            Mockito.CALLS_REAL_METHODS);
                    MockedStatic<LocalDateTime> time = Mockito.mockStatic(LocalDateTime.class,
                            Mockito.CALLS_REAL_METHODS);) {
                rnd.when(RandomTokenUtil::generate).thenReturn(fixedToken);
                time.when(LocalDateTime::now).thenReturn(ld);

                authService.requestEmailChange(userId, req);

                User user = userMapper.selectUserByPrimaryKey(userId);
                assertThat(user)
                        .extracting(
                                User::getPendingEmail,
                                User::getEmailToken,
                                User::getPendingExpiresAt)
                        .containsExactly(
                                "test@example.com",
                                hashed,
                                ld.plusMinutes(30));

                verify(gateway).send(
                        argThat(mail -> mail.subject().equals(MailTemplate.EMAIL_CHANGE_COMPLETE_NEW.getSubject())
                                && mail.to().equals("test@example.com")
                                && mail.body().contains(fixedToken)));

                verify(gateway).send(
                        argThat(mail -> mail.subject().equals(MailTemplate.EMAIL_CHANGE_ALERT_OLD.getSubject())
                                && mail.to().equals("sample@example.com")
                                && mail.body().contains("2025年07月02日 10時40分")));
            }
        }
    }
}
