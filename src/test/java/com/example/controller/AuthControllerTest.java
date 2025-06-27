package com.example.controller;

import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;

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
        String email = "a".repeat(64) + // ローカル部 64 文字（上限ぎりぎり）
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
                Arguments.of(email, "12345678", true),
                Arguments.of(email + "a", "12345678", false),
                // @email
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
                // @email
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
                  "phoneNumber": "0312345678",
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

        @Test
        void register_fail() throws Exception {
            String body = """
                    {
                      "token": "%s",
                      "email": "test@example.com",
                      "password": "short",
                      "lastName": "山田",
                      "firstName": "太郎",
                      "lastNameKana": "ヤマダ",
                      "firstNameKana": "タロウ",
                      "postCode": "1500001",
                      "addressPrefCity": "東京都渋谷区",
                      "addressArea": "神南",
                      "addressBlock": "1-19-11",
                      "addressBuilding": "パークビル201",
                      "phoneNumber": "0312345678",
                      "birthday": "1990-04-01",
                      "gender": "E"
                    }
                    """.formatted("a".repeat(22));

            String expected = """
                      [
                        { "field": "password", "errorCode": "Size" },
                        { "field": "gender",  "errorCode": "Pattern" }
                      ]
                    """;

            mockMvc.perform(post("/register/complete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().json(expected, JsonCompareMode.LENIENT));

            verify(authService, never()).register(any());
        }

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
        void register_parameterFail(Map<String, ?> diff, String errorCode)
                throws JsonProcessingException, Exception {
            mockMvc.perform(post("/register/complete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(breakJson(diff)))
                    .andExpect(status().isBadRequest())
                    .andExpect((jsonPath("$..errorCode", hasItem(errorCode))));
        }

        static Stream<Arguments> provideValidRegistrationArguments() {
            String email255 = "a".repeat(64) + // ローカル部 64 文字（上限ぎりぎり）
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

                    // @email
                    // @Length
                    Arguments.of(Map.of("email", email255)),

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
                    Arguments.of(Map.of("addressBuilding", "い".repeat(100))),

                    // phoneNumber
                    // @Pattern
                    Arguments.of(Map.of("phoneNumber", "0123456789")),
                    Arguments.of(Map.of("phoneNumber", "01234567890")),

                    // gender
                    // @Pattern
                    Arguments.of(Map.of("gender", "M")),
                    Arguments.of(Map.of("gender", "F")));
        }

        static Stream<Arguments> provideInvalidRegistrationArguments() {
            // メール全体 256 文字（= 64 + 1 + 63 + 1 + 63 + 1 + 63）
            String email256 = "a".repeat(64) + "@" +
                    "b".repeat(63) + "." +
                    "c".repeat(63) + "." +
                    "d".repeat(63);

            return Stream.of(
                    /* token -------------------------------------------------------------- */
                    Arguments.of(Map.of("token", ""), "NotBlank"),
                    Arguments.of(Map.of("token", "a".repeat(21)), "Size"), // <22
                    Arguments.of(Map.of("token", "a".repeat(23)), "Size"), // >22

                    /* email -------------------------------------------------------------- */
                    Arguments.of(Map.of("email", ""), "NotBlank"),
                    Arguments.of(Map.of("email", email256), "Size"), // >255

                    /* password ----------------------------------------------------------- */
                    Arguments.of(Map.of("password", ""), "NotBlank"),
                    Arguments.of(Map.of("password", "1234567"), "Size"), // <8
                    Arguments.of(Map.of("password", "123456789012345678901"), "Size"), // >20
                    Arguments.of(Map.of("password", "testあい"), "Pattern"),

                    /* lastName ----------------------------------------------------------- */
                    Arguments.of(Map.of("lastName", ""), "NotBlank"),
                    Arguments.of(Map.of("lastName", "あ".repeat(51)), "Size"),

                    /* firstName ---------------------------------------------------------- */
                    Arguments.of(Map.of("firstName", ""), "NotBlank"),
                    Arguments.of(Map.of("firstName", "あ".repeat(51)), "Size"),

                    /* lastNameKana ------------------------------------------------------- */
                    Arguments.of(Map.of("lastNameKana", ""), "NotBlank"),
                    Arguments.of(Map.of("lastNameKana", "ア".repeat(51)), "Size"),
                    Arguments.of(Map.of("lastNameKana", "テスト3"), "Pattern"),

                    /* firstNameKana ------------------------------------------------------ */
                    Arguments.of(Map.of("firstNameKana", ""), "NotBlank"),
                    Arguments.of(Map.of("firstNameKana", "ア".repeat(51)), "Size"),
                    Arguments.of(Map.of("firstNameKana", "テストな"), "Pattern"),

                    /* postCode ----------------------------------------------------------- */
                    Arguments.of(Map.of("postCode", ""), "NotBlank"),
                    Arguments.of(Map.of("postCode", "12345678"), "Pattern"), // 8 桁
                    Arguments.of(Map.of("postCode", "123456a"), "Pattern"), // 英字混入

                    /* addressPrefCity ---------------------------------------------------- */
                    Arguments.of(Map.of("addressPrefCity", ""), "NotBlank"),
                    Arguments.of(Map.of("addressPrefCity", "い".repeat(101)), "Size"),

                    /* addressArea -------------------------------------------------------- */
                    Arguments.of(Map.of("addressArea", ""), "NotBlank"),
                    Arguments.of(Map.of("addressArea", "い".repeat(101)), "Size"),

                    /* addressBuilding (任意) -------------------------------------------- */
                    Arguments.of(Map.of("addressBuilding", "い".repeat(101)), "Size"),

                    /* phoneNumber -------------------------------------------------------- */
                    Arguments.of(Map.of("phoneNumber", ""), "NotBlank"),
                    Arguments.of(Map.of("phoneNumber", "09012A4567"), "Pattern"), // 非数字
                    Arguments.of(Map.of("phoneNumber", "012345678"), "Pattern"), // 9 桁
                    Arguments.of(Map.of("phoneNumber", "012345678901"), "Pattern"), // 12 桁
                    Arguments.of(Map.of("phoneNumber", "11234567890"), "Pattern"), // 先頭 0 でない

                    /* birthday ----------------------------------------------------------- */
                    Arguments.of(new HashMap<>() {
                        {
                            put("birthday", null);
                        }
                    }, "NotNull"),
                    Arguments.of(Map.of("birthday", LocalDate.now().plusYears(3)), "Past"),

                    /* gender ------------------------------------------------------------- */
                    Arguments.of(Map.of("gender", ""), "NotBlank"),
                    Arguments.of(Map.of("gender", "U"), "Pattern"),
                    Arguments.of(Map.of("gender", "MM"), "Pattern"));
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
                    "", // 建物名（任意）
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
}
