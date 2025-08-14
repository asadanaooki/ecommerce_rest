package com.example.validation.validator;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.example.validation.constraint.EmailFormat;

public class EmailFormatValidator implements ConstraintValidator<EmailFormat, String> {

    // ローカル部: 英数字・._+- のみ、先頭末尾は英数字、連続ドット禁止
    private static final Pattern LOCAL = Pattern.compile("^[A-Za-z0-9](?:[A-Za-z0-9._+-]*[A-Za-z0-9])?$");

    // ドメインラベル: 英数字・- のみ、先頭末尾は英数字
    private static final Pattern LABEL = Pattern.compile("^[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        
        if (value.length() > 254) {
            return false;
        }

        // @の数と位置チェック
        int at = value.indexOf('@');
        if (at <= 0
                || at != value.lastIndexOf('@')
                || at == value.length() - 1) {
            return false;
        }

        String local = value.substring(0, at);
        String domain = value.substring(at + 1);

        // ローカル部検証
        if (local.length() > 64
                || !LOCAL.matcher(local).matches()
                || local.contains("..")) {
            return false;
        }

        // ドメイン部検証
        String[] labels = domain.split("\\.", -1);
        if (domain.length() > 253
                // TLD含め最低2ラベル
                || labels.length < 2
                // TLDは2文字以上
                || labels[labels.length - 1].length() < 2) {
            return false;
        }

        for (String label : labels) {
            if ((label.length() < 1 || label.length() > 63)
                    || !LABEL.matcher(label).matches()) {
                return false;
            }
        }
        return true;
    }
}
