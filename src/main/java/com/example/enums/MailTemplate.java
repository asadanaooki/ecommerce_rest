package com.example.enums;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.example.dto.CartDto;
import com.example.entity.OrderItem;
import com.example.entity.User;
import com.example.service.CheckoutService;
import com.example.service.CheckoutService.NameAddress;

import lombok.Getter;

@Getter
public enum MailTemplate {
    // TODO:
    // 明細部分の処理共通化する？共通のクラスを受け取って共通の処理する？

    REGISTRATION(
            "会員登録のご案内",
            """
                    以下のURLにアクセス頂き、お客様情報をご入力の上、会員登録をお願いいたします。

                    %s

                    %d分以内に会員登録が完了しない場合、このURLが無効となりますのでご注意ください。
                    無効となった際は再度会員登録のページよりお進みください
                    """) {
        @Override
        public EmailMessage build(Object... args) {
            String to = (String) args[0];
            String link = (String) args[1];
            Long ttlMin = (Long) args[2];
            return new EmailMessage(to, getSubject(), getBody().formatted(link, ttlMin));
        }

    },
    ORDER_CONFIRMATION(
            "ご注文ありがとうございます",
            """
                    %s様、ご注文ありがとうございます。

                    【お届け先】
                    %s
                    %s

                    【注文番号】
                    %s

                    【ご注文内容】
                    %s

                    --- 合計欄 ---
                    合計金額 : ¥%,d

                    ※本メールは自動送信です。ご不明点がございましたらお問い合わせフォームよりご連絡ください。
                    """) {

        @Override
        public EmailMessage build(Object... args) {
            User user = (User) args[0];
            CartDto cart = (CartDto) args[1];
            int orderNumber = (int) args[2];

            NameAddress na = CheckoutService.buildNameAddress(user);

            /* ---------- 明細ブロック ---------- */
            String itemsBlock = cart.getItems().stream().map(i -> """
                    %s
                    数量 : %d
                    価格 : ¥%,d
                    小計 : ¥%,d
                    """.formatted(i.getProductName(), i.getQty(), i.getPriceInc(), i.getSubtotal()))
                    .collect(Collectors.joining("\n"));

            String body = getBody().formatted(
                    na.fullName(),
                    na.fullName(),
                    na.fullAddress(),
                    String.format("%04d", orderNumber),
                    itemsBlock,
                    cart.getTotalPrice());

            return new EmailMessage(user.getEmail(), getSubject(), body);

        }

    },
    PASSWORD_RESET(
            "パスワード再設定のご案内",
            """
                    以下のURLよりパスワードを再設定してください。

                    %s

                    %d分以内にパスワードを再設定しない場合、このURLは無効となります。
                    無効となった際は再度パスワード再設定のページよりお進みください
                    """) {

        @Override
        public EmailMessage build(Object... args) {
            String to = (String) args[0];
            String link = (String) args[1];
            long ttlMin = (Long) args[2];

            return new EmailMessage(to, getSubject(), getBody().formatted(link, ttlMin));
        }
    },

    EMAIL_CHANGE_COMPLETE_NEW(
            "【重要】新しいメールアドレスの確認をお願いします",
            """
                     ご登録メールアドレスの変更申請を受け付けました。下記URLから変更を確定してください。

                     %s

                     %d 分で無効になります。心当たりがない場合はこのメールを破棄してください。
                    """) {

        @Override
        public EmailMessage build(Object... args) {
            String to = (String) args[0];
            String link = (String) args[1];
            long ttl = (Long) args[2];

            return new EmailMessage(to, getSubject(), getBody().formatted(link, ttl));
        }

    },

    EMAIL_CHANGE_ALERT_OLD(
            "メールアドレス変更が申請されました",
            """
                     ご登録メールアドレスの変更申請が行われました。

                     変更日時 : %s

                     心当たりがない場合は至急サポートまでご連絡ください。
                    """) {
        @Override
        public EmailMessage build(Object... args) {
            String to = (String) args[0];
            LocalDateTime when = (LocalDateTime) args[1];

            String body = getBody().formatted(
                    when.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH時mm分")));

            return new EmailMessage(to, getSubject(), body);
        }

    },

    PROFILE_CHANGED(
            "プロフィール情報が更新されました",
            """
                     プロフィール情報が更新されました。内容をご確認ください。　

                     変更日時 : %s

                     心当たりがない場合は至急サポートまでご連絡ください。
                    """) {
        @Override
        public EmailMessage build(Object... args) {
            String to = (String) args[0];
            LocalDateTime when = (LocalDateTime) args[1];

            String body = getBody().formatted(
                    when.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH時mm分")));

            return new EmailMessage(to, getSubject(), body);
        }
    },

    ORDER_EDIT_COMPLETED(
            "ご注文内容を更新しました",
            """
                    %s様、注文内容の変更を承りました。

                    【お届け先】
                     %s
                     %s

                    【注文番号】
                     %s

                    【変更後のご注文内容】
                     %s

                    --- 合計欄 ---
                    変更後合計金額 : ¥%,d
                        """) {
        @Override
        public EmailMessage build(Object... args) {
            User u = (User) args[0];
            int orderNumber = (int) args[1];
            List<OrderItem> items = (List<OrderItem>) args[2];

            NameAddress na = CheckoutService.buildNameAddress(u);
            String itemsBlock = items.stream()
                    .map(i -> """
                            %s
                            数量 : %d
                            価格 : ¥%,d
                            小計 : ¥%,d
                            """.formatted(
                            i.getProductName(),
                            i.getQty(),
                            i.getPrice(),
                            i.getSubtotal()))
                    .collect(Collectors.joining("\n"));

            String body = getBody().formatted(
                    na.fullName(),
                    na.fullName(),
                    na.fullAddress(),
                    String.format("%04d", orderNumber),
                    itemsBlock,
                    items.stream().mapToInt(OrderItem::getSubtotal).sum());
            
            return new EmailMessage(u.getEmail(), getSubject(), body);
        }

    };

    private final String subject;

    private final String body;

    private MailTemplate(String subject, String body) {
        this.subject = subject;
        this.body = body;
    }

    public abstract EmailMessage build(Object... args);

    public record EmailMessage(String to, String subject, String body) {
    }

}
