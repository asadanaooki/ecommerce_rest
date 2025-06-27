package com.example.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.entity.PreRegistration;
import com.example.entity.User;
import com.example.exception.BusinessException;
import com.example.mapper.UserMapper;
import com.example.request.RegisterUserRequest;
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

    @MockitoBean
    JavaMailSender sender;

    @Autowired
    AuthService authService;

    @MockitoSpyBean
    UserMapper userMapper;

    String email = "foo@example.com";

    @Nested
    class SendVerification {

        String fixedToken = "a".repeat(22);

        @BeforeEach
        void setup() {
            doReturn(mock(MimeMessage.class)).when(sender).createMimeMessage();
        }

        @Test
        void sendVerification_success() throws MessagingException {
            // TODO:
            // モックと実DBが混ざってる、どう書くのがよいか？
            try (MockedConstruction<MimeMessageHelper> mocked = Mockito.mockConstruction(MimeMessageHelper.class,
                    (mock, ctx) -> {
                        // setter 系は特に何もしない
                    });
                    MockedStatic<RandomTokenUtil> rnd = Mockito.mockStatic(RandomTokenUtil.class)) {
                rnd.when(RandomTokenUtil::generate).thenReturn(fixedToken);
                authService.sendRegistrationUrl(email);

                PreRegistration pr = userMapper.selectPreRegistrationByPrimaryKey(fixedToken);
                assertThat(pr.getToken()).isNotNull();
                assertThat(pr.getEmail()).isEqualTo(email);
                assertThat(pr.getExpiresAt()).isAfter(LocalDateTime.now());

                verify(sender).send(any(MimeMessage.class));
            }
        }

        @Test
        void sendVerification_alreadyRegistered() {
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

        @Test
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void sendVerification_mailFail() {
            doThrow(new MailSendException("smtp down")).when(sender).send(any(MimeMessage.class));

            try (MockedConstruction<MimeMessageHelper> mocked = Mockito.mockConstruction(MimeMessageHelper.class,
                    (mock, ctx) -> {
                        // setter 系は特に何もしない
                    });
                    MockedStatic<RandomTokenUtil> rnd = Mockito.mockStatic(RandomTokenUtil.class)) {
                rnd.when(RandomTokenUtil::generate).thenReturn(fixedToken);

                assertThatThrownBy(() -> authService.sendRegistrationUrl(email))
                        .isInstanceOf(MailSendException.class);

                PreRegistration p = userMapper.selectPreRegistrationByPrimaryKey(fixedToken);
                assertThat(p).isNull();
            }
        }
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

}
