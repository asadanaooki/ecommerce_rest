package com.example.validation.validator;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EmailFormatValidatorTest {

    EmailFormatValidator validator = new EmailFormatValidator();

    static String baseDomain = "@example.com";

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideValidArguments")
    void checkEmail_success(String name, String email) {
        assertThat(validator.isValid(email, null)).isTrue();
    }

    static Stream<Arguments> provideValidArguments() {
        String a63 = "a".repeat(63);
        String b63 = "b".repeat(63);
        String c63 = "c".repeat(63);
        String d60 = "d".repeat(60);
        String local64 = "a".repeat(64);
        String domain253 = a63 + "." + b63 + "." + c63 + "." + d60;
        String label63 = "a".repeat(63);

        return Stream.of(
                // --- ローカル部: 形式
                Arguments.of("先頭が英数字", "a" + baseDomain),
                Arguments.of("末尾が英数字", "ab1" + baseDomain),
                Arguments.of("中間で .", "a.b" + baseDomain),
                Arguments.of("中間で _", "a_b" + baseDomain),
                Arguments.of("中間で +", "a+b" + baseDomain),
                Arguments.of("中間で -", "a-b" + baseDomain),
                Arguments.of("連続で _", "a__b" + baseDomain),

                // --- ローカル部: 長さ
                Arguments.of("ローカル64文字", local64 + baseDomain),

                // --- ドメイン部: 形式
                Arguments.of("ハイフン含む", "a@my-site.com"),

                // --- ドメイン部: 長さ
                Arguments.of("ドメイン252文字", "a@" + domain253),
                Arguments.of("TLD=2文字", "a@ex.co"),
                Arguments.of("ラベル1文字", "a@x.com"),
                Arguments.of("ラベル63文字", "a@" + label63 + ".com"),
                Arguments.of("2ラベル", "a" + baseDomain),

                // 全体
                Arguments.of("全体254文字", local64 + "@" + a63 + "." + d60 + "." + d60 + ".com"),
                Arguments.of("NULL", null));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidArguments")
    void checkEmail_failure(String name, String email) {
        assertThat(validator.isValid(email, null)).isFalse();
    }

    static Stream<Arguments> provideInvalidArguments() {
        String a63 = "a".repeat(63);
        String c61 = "c".repeat(61);
        String d60 = "d".repeat(60);
        String local65 = "a".repeat(65);
        String label64 = "a".repeat(64);

        return Stream.of(
                // --- ローカル部: 形式
                Arguments.of("先頭が記号", "-a" + baseDomain),
                Arguments.of("末尾が記号", "a-" + baseDomain),
                Arguments.of("中間で許容外記号", "a=b" + baseDomain),
                Arguments.of("連続ドット", "a..b" + baseDomain),

                // --- ローカル部: 長さ
                Arguments.of("ローカル65文字", local65 + baseDomain),

                // --- ドメイン部: 形式
                Arguments.of("1ラベルのみ(TLD)", "a@com"),
                Arguments.of("ラベル先頭がハイフン", "a@-example.com"),
                Arguments.of("ラベル末尾がハイフン", "a@example-.com"),
                Arguments.of("ラベル中間でハイフン以外の記号", "a@exa_mple.com"),
                Arguments.of("先頭ドット", "a@.example.com"),
                Arguments.of("終端ドット", "a@example.com."),
                Arguments.of("中間で連続ドット", "a@example..com"),

                // --- ドメイン部: 長さ
                Arguments.of("TLD=1文字", "a@example.c"),
                Arguments.of("ラベル64文字", "a@" + label64 + ".com"),

                // 全体
                Arguments.of("@なし", "userexample.com"),
                Arguments.of("@先頭", "@example.com"),
                Arguments.of("@が末尾", "user@"),
                Arguments.of("@複数", "a@b@c.com"),
                Arguments.of("全体255文字", "a".repeat(64) + "@" + a63 + "." + c61 + "." + d60 + ".com"));

    }

}
