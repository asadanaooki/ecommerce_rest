package com.example.controller;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.request.PasswordResetUpdateRequest;
import com.example.request.RegisterUserRequest;
import com.example.service.AuthService;
import com.example.service.CartService;
import com.example.util.CookieUtil;
import com.example.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Import(CookieUtil.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    /* TODO:
    ・Emailのバリデーションテスト増やす(全角NGなど)
    ・@Patternで同値分析すると、NGは１ケースで良いとなるが、実際いくつか試すべきか？
    ・電話番号の正規表現テストケースの考え方
    */

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AuthService authService;

    @MockitoBean
    CartService cartService;

    @MockitoBean
    JwtUtil jwtUtil;

    @Autowired
    CookieUtil cookieUtil;

    @Autowired
    ObjectMapper objectMapper;

    @ParameterizedTest
    @MethodSource("provideArguments")
    void login_parameterized(String username, String password, boolean expected) throws Exception {
        doReturn(new AuthService.AuthResult("token", "userId"))
                .when(authService).authenticate(username, password);

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """.formatted(username, password)))
                .andExpect(expected ? status().isOk() : status().isBadRequest());
    }

    static Stream<Arguments> provideArguments() {
        String email254 = "a".repeat(63) + // ローカル部 63文字
                "@" +
                "b".repeat(63) + // ドメイン・ラベル 1   (63 文字)
                "." +
                "c".repeat(63) + // ドメイン・ラベル 2   (63 文字)
                "." +
                "d".repeat(62);
        return Stream.of(
                // username
                // @notBlank
                Arguments.of("", "12345678", false),
                // @Length
                Arguments.of(email254, "12345678", true),
                Arguments.of(email254 + "a", "12345678", false),
                // @EmailFormat
                Arguments.of("test4@gmai.com", "12345678", true),

                // password
                // @NotBlank
                Arguments.of("test4@gmai.com", "12345678", true),
                Arguments.of("test4@gmai.com", "", false),
                // @Length
                Arguments.of("test4@gmai.com", "1234567", false),
                Arguments.of("test4@gmai.com", "12345678", true),
                Arguments.of("test4@gmai.com", "12345678901234567890", true),
                Arguments.of("test4@gmai.com", "123456789012345678901", false),
                // @Pattern
                Arguments.of("test4@gmai.com", "12345678sofTENI", true),
                Arguments.of("test4@gmai.com", "12345678あい", false));
    }

    @Test
    void send() throws Exception {
        mockMvc.perform(post("/register/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "%s"
                        }
                        """.formatted("test@example.com")))
                .andExpect(status().isOk());
    }

    @Nested
    class Register {
        String body = """
                {
                  "token": "%s",
                  "email": "test@example.com",
                  "password": "Password1",
                  "lastName": "山田",
                  "firstName": "太郎",
                  "lastNameKana": "ヤマダ",
                  "firstNameKana": "タロウ",
                  "postCode": "1500001",
                  "addressPrefCity": "東京都渋谷区",
                  "addressArea": "神南",
                  "addressBlock": "1-19-11",
                  "addressBuilding": "パークビル201",
                  "phoneNumber": "08012345678",
                  "birthday": "1990-04-01",
                  "gender": "M"
                }
                """.formatted("a".repeat(22));

        @Test
        void register_success() throws Exception {
            mockMvc.perform(post("/register/complete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isOk());
            verify(authService).register(any());

        }

//        @Test
//        void register_fail() throws Exception {
//            String body = """
//                    {
//                      "token": "%s",
//                      "email": "test@example.com",
//                      "password": "short",
//                      "lastName": "山田",
//                      "firstName": "太郎",
//                      "lastNameKana": "ヤマダ",
//                      "firstNameKana": "タロウ",
//                      "postCode": "1500001",
//                      "addressPrefCity": "東京都渋谷区",
//                      "addressArea": "神南",
//                      "addressBlock": "1-19-11",
//                      "addressBuilding": "パークビル201",
//                      "phoneNumber": "0312345678",
//                      "birthday": "1990-04-01",
//                      "gender": "E"
//                    }
//                    """.formatted("a".repeat(22));
//
//            String expected = """
//                      [
//                        { "field": "password", "errorCode": "Size" },
//                        { "field": "gender",  "errorCode": "Pattern" }
//                      ]
//                    """;
//
//            mockMvc.perform(post("/register/complete")
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .content(body))
//                    .andExpect(status().isBadRequest())
//                    .andExpect(content().json(expected, JsonCompareMode.LENIENT));
//
//            verify(authService, never()).register(any());
//        }

        @ParameterizedTest
        @MethodSource("provideValidRegistrationArguments")
        void register_parameterSuccess(Map<String, ?> diff)
                throws JsonProcessingException, Exception {
            mockMvc.perform(post("/register/complete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(breakJson(diff)))
                    .andExpect(status().isOk());
        }

        @ParameterizedTest
        @MethodSource("provideInvalidRegistrationArguments")
        void register_parameterFail(Map<String, ?> diff, String expField, String expCode)
                throws JsonProcessingException, Exception {
            mockMvc.perform(post("/register/complete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(breakJson(diff)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].field").value(expField))
                    .andExpect(jsonPath("$.data[0].errorCode").value(expCode));
        }

        static Stream<Arguments> provideValidRegistrationArguments() {
            String email254 = "a".repeat(63) + // ローカル部 63 文字
                    "@" +
                    "b".repeat(63) + // ドメイン・ラベル 1   (63 文字)
                    "." +
                    "c".repeat(63) + // ドメイン・ラベル 2   (63 文字)
                    "." +
                    "d".repeat(62);
            return Stream.of(
                    // token
                    // @Length
                    Arguments.of(Map.of("token", "a".repeat(22))),

                    // @EmailFormat
                    // @Length
                    Arguments.of(Map.of("email", email254)),

                    // password
                    // @Length
                    Arguments.of(Map.of("password", "12345678")),
                    Arguments.of(Map.of("password", "12345678901234567890")),

                    // lastName
                    // @Length
                    Arguments.of(Map.of("lastName", "あ".repeat(50))),

                    // firstName
                    // @Length
                    Arguments.of(Map.of("firstName", "あ".repeat(50))),

                    // lastNameKana
                    // @Length
                    Arguments.of(Map.of("lastNameKana", "ア".repeat(50))),

                    // firstNameKana
                    // @Length
                    Arguments.of(Map.of("firstNameKana", "ア".repeat(50))),

                    // addressPrefCity
                    // @Length
                    Arguments.of(Map.of("addressPrefCity", "い".repeat(100))),

                    // addressArea
                    // @Length
                    Arguments.of(Map.of("addressArea", "い".repeat(100))),

                    // addressBuilding
                    // @Length
                    Arguments.of(Collections.singletonMap("addressBuilding", null)),
                    Arguments.of(Map.of("addressBuilding", "い")),
                    Arguments.of(Map.of("addressBuilding", "い".repeat(100))),

                    // phoneNumber
                    Arguments.of(Map.of("phoneNumber", "01234567890")),

                    // gender
                    // @Pattern
                    Arguments.of(Map.of("gender", "M")),
                    Arguments.of(Map.of("gender", "F")));
        }

        static Stream<Arguments> provideInvalidRegistrationArguments() {
            // メール全体 255 文字（= 63 + 1 + 63 + 1 + 63 + 1 + 63）
            String email255 = "a".repeat(63) + "@" +
                    "b".repeat(63) + "." +
                    "c".repeat(63) + "." +
                    "d".repeat(63);

            return Stream.of(
                    /* token -------------------------------------------------------------- */
                    Arguments.of(Map.of("token", ""), "token", "NotBlank"),
                    Arguments.of(Map.of("token", "a".repeat(21)), "token", "Size"), // <22
                    Arguments.of(Map.of("token", "a".repeat(23)), "token", "Size"), // >22

                    /* email -------------------------------------------------------------- */
                    Arguments.of(Map.of("email", ""), "email", "NotBlank"),
                    Arguments.of(Map.of("email", email255), "email", "Size"), // >255

                    /* password ----------------------------------------------------------- */
                    Arguments.of(Map.of("password", ""), "password", "NotBlank"),
                    Arguments.of(Map.of("password", "1234567"), "password", "Size"), // <8
                    Arguments.of(Map.of("password", "123456789012345678901"), "password", "Size"), // >20
                    Arguments.of(Map.of("password", "testあい"), "password", "Size"),

                    /* lastName ----------------------------------------------------------- */
                    Arguments.of(Map.of("lastName", ""), "lastName", "NotBlank"),
                    Arguments.of(Map.of("lastName", "あ".repeat(51)), "lastName", "Size"),

                    /* firstName ---------------------------------------------------------- */
                    Arguments.of(Map.of("firstName", ""), "firstName", "NotBlank"),
                    Arguments.of(Map.of("firstName", "あ".repeat(51)), "firstName", "Size"),

                    /* lastNameKana ------------------------------------------------------- */
                    Arguments.of(Map.of("lastNameKana", ""), "lastNameKana", "NotBlank"),
                    Arguments.of(Map.of("lastNameKana", "ア".repeat(51)), "lastNameKana", "Size"),
                    Arguments.of(Map.of("lastNameKana", "テスト3"), "lastNameKana", "Pattern"),

                    /* firstNameKana ------------------------------------------------------ */
                    Arguments.of(Map.of("firstNameKana", ""), "firstNameKana", "NotBlank"),
                    Arguments.of(Map.of("firstNameKana", "ア".repeat(51)), "firstNameKana", "Size"),
                    Arguments.of(Map.of("firstNameKana", "テストな"), "firstNameKana", "Pattern"),

                    /* postCode ----------------------------------------------------------- */
                    Arguments.of(Map.of("postCode", ""), "postCode", "NotBlank"),
                    Arguments.of(Map.of("postCode", "12345678"), "postCode", "Pattern"), // 8 桁
                    Arguments.of(Map.of("postCode", "123456a"), "postCode", "Pattern"), // 英字混入

                    /* addressPrefCity ---------------------------------------------------- */
                    Arguments.of(Map.of("addressPrefCity", ""), "addressPrefCity", "NotBlank"),
                    Arguments.of(Map.of("addressPrefCity", "い".repeat(101)), "addressPrefCity", "Size"),

                    /* addressArea -------------------------------------------------------- */
                    Arguments.of(Map.of("addressArea", ""), "addressArea", "NotBlank"),
                    Arguments.of(Map.of("addressArea", "い".repeat(101)), "addressArea", "Size"),

                    /* addressBuilding (任意) -------------------------------------------- */
                    Arguments.of(Map.of("addressBuilding", ""), "addressBuilding", "Size"),
                    Arguments.of(Map.of("addressBuilding", "い".repeat(101)), "addressBuilding", "Size"),

                    /* phoneNumber -------------------------------------------------------- */
                    Arguments.of(Map.of("phoneNumber", ""), "phoneNumber", "NotBlank"),
                    Arguments.of(Map.of("phoneNumber", "09012A4567"), "phoneNumber", "Size"), // 非数字
                    Arguments.of(Map.of("phoneNumber", "0123456789"), "phoneNumber", "Size"), // 10 桁
                    Arguments.of(Map.of("phoneNumber", "012345678901"), "phoneNumber", "Size"), // 12 桁

                    /* birthday ----------------------------------------------------------- */
                    Arguments.of(new HashMap<>() {
                        {
                            put("birthday", null);
                        }
                    }, "birthday", "NotNull"),
                    Arguments.of(Map.of("birthday", LocalDate.now().plusYears(3)), "birthday", "Past"),

                    /* gender ------------------------------------------------------------- */
                    Arguments.of(Map.of("gender", ""), "gender", "NotBlank"),
                    Arguments.of(Map.of("gender", "U"), "gender", "Pattern"),
                    Arguments.of(Map.of("gender", "MM"), "gender", "Pattern"));
        }

        private RegisterUserRequest createValid() {
            return new RegisterUserRequest(
                    "1".repeat(22), // token
                    "user@example.com", // email
                    "Password1", // password
                    "山田", "太郎", // 氏名
                    "ヤマダ", "タロウ", // フリガナ
                    "1500001", // 郵便
                    "東京都渋谷区", "神南一丁目", "1-19-11", // 住所
                    "aa", // 建物名（任意）
                    "09012345678", // 電話
                    LocalDate.of(1990, 1, 1), // 誕生日
                    "M" // 性別
            );
        }

        private String breakJson(Map<String, ?> diff) throws JsonProcessingException {
            Map<String, Object> m = objectMapper.convertValue(createValid(), new TypeReference<>() {});
            m.putAll(diff);
            return objectMapper.writeValueAsString(m);
        }

    }

    @ParameterizedTest
    @MethodSource("provideInvalidPasswordResetArguments")
    void request_parameter(String token, String email, String newPw, String confirmPw,
            String expField, String expCode) throws JsonProcessingException, Exception {
        PasswordResetUpdateRequest req = new PasswordResetUpdateRequest() {
            {
                setToken(token);
                setEmail(email);
                setNewPassword(newPw);
                setConfirmPassword(confirmPw);
            }
        };

        mockMvc.perform(post("/password-reset/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].field").value(expField))
                .andExpect(jsonPath("$.data[0].errorCode").value(expCode));
    }

    static Stream<Arguments> provideInvalidPasswordResetArguments() {
        String email254 = "a".repeat(63) + // ローカル部 63 文字
                "@" +
                "b".repeat(63) + // ドメイン・ラベル 1   (63 文字)
                "." +
                "c".repeat(63) + // ドメイン・ラベル 2   (63 文字)
                "." +
                "d".repeat(62);
        String token22 = "a".repeat(22);
        return Stream.of(
                // token
                // @notBlank
                Arguments.of("", email254, "testpass1", "testpass1", "token", "NotBlank"),
                // @size
                Arguments.of("a".repeat(21), email254, "testpass1", "testpass1", "token", "Size"),
                Arguments.of(token22 + "b", email254, "testpass1", "testpass1", "token", "Size"),

                // email
                // @notBlank
                Arguments.of(token22, "", "testpass1", "testpass1", "email", "NotBlank"),
                // @size
                Arguments.of(token22, email254 + "s", "testpass1", "testpass1", "email", "Size"),
                // @EmailFormat
                Arguments.of(token22, "user@", "testpass1", "testpass1", "email", "EmailFormat"),

                // newPassword
                // @notBlank
                Arguments.of(token22, email254, "", "validpass123", "newPassword", "NotBlank"),
                // @size
                Arguments.of(token22, email254, "1234567", "1234567", "newPassword", "Size"),
                Arguments.of(token22, email254, "12345678901234567890a", "12345678901234567890a", "newPassword",
                        "Size"),
                // @pattern
                Arguments.of(token22, email254, "123あい45678", "123あい45678", "newPassword", "Pattern"),

                // confirmPassword
                // @notBlank
                Arguments.of(token22, email254, "testpass1", "", "confirmPassword", "NotBlank"),

                // isMatch
                Arguments.of(token22, email254, "testpass1", "testpass2", "confirmPassword", "PASSWORDS_MATCH"));
    }
    
    @ParameterizedTest
    @CsvSource({
        "'', false",
        "'123456789012345678901', false",
        "'1234567890123456789012', true", // 22文字
        "'12345678901234567890123', false",
    })
    void completeEmailChange_parameter(String token, boolean expected) throws Exception {
        mockMvc.perform(get("/profile/email-change/complete").param("token", token))
        .andExpect(expected ? status().isOk() : status().isBadRequest());
    }
}
