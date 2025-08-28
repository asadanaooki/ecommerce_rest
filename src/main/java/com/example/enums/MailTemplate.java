package com.example.enums;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import com.example.dto.CheckoutItemDto;
import com.example.dto.CheckoutProcessDto;
import com.example.entity.OrderItem;
import com.example.entity.User;
import com.example.util.OrderUtil;

import lombok.Getter;

@Getter
public enum MailTemplate {

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
            String to    = (String) args[0];
            String link  = (String) args[1];
            Long ttlMin  = (Long)   args[2];

            String body = getBody().formatted(
                    link,
                    ttlMin
            );
            return new EmailMessage(to, getSubject(), body);
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
            User user               = (User) args[0];
            CheckoutProcessDto ck   = (CheckoutProcessDto) args[1];
            int orderNumber         = (int) args[2];

            String body = getBody().formatted(
                    ck.getFullName(),
                    ck.getFullName(),
                    ck.getFullAddress(),
                    OrderUtil.formatOrderNumber(orderNumber),
                    createItemsBlock(
                            ck.getItems(),
                            CheckoutItemDto::getProductName,
                            CheckoutItemDto::getQty,
                            CheckoutItemDto::getUnitPriceIncl,
                            CheckoutItemDto::getSubtotalIncl
                    ),
                    ck.getTotalPriceIncl()
            );
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
            String to    = (String) args[0];
            String link  = (String) args[1];
            long ttlMin  = (Long)   args[2];

            String body = getBody().formatted(
                    link,
                    ttlMin
            );
            return new EmailMessage(to, getSubject(), body);
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
            String to   = (String) args[0];
            String link = (String) args[1];
            long ttl    = (Long)   args[2];

            String body = getBody().formatted(
                    link,
                    ttl
            );
            return new EmailMessage(to, getSubject(), body);
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
            String to           = (String) args[0];
            LocalDateTime when  = (LocalDateTime) args[1];

            String body = getBody().formatted(
                    when.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH時mm分"))
            );
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
            String to           = (String) args[0];
            LocalDateTime when  = (LocalDateTime) args[1];

            String body = getBody().formatted(
                    when.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH時mm分"))
            );
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
            OrderEditCompletedContext ctx = (OrderEditCompletedContext) args[0];

            String body = getBody().formatted(
                    ctx.fullName(),
                    ctx.fullName(),
                    ctx.fullAddress(),
                    ctx.orderNumber(),
                    createItemsBlock(
                            ctx.items(),
                            OrderItem::getProductName,
                            OrderItem::getQty,
                            OrderItem::getUnitPriceIncl,
                            OrderItem::getSubtotalIncl
                    ),
                    ctx.totalIncl()
            );
            return new EmailMessage(ctx.to(), getSubject(), body);
        }
    },

    INQUIRY_ADMIN_NOTIFICATION(
            "新しいお問い合わせが届きました",
            """
                        【送信者】
                          氏名 : %s %s
                          メール : %s
                          電話 : %s
                          注文番号 : %s

                        【メッセージ】
                          %s
                    """) {
        @Override
        public EmailMessage build(Object... args) {
            InquiryContext ctx = (InquiryContext) args[0];

            String body = getBody().formatted(
                    ctx.lastName(),
                    ctx.firstName(),
                    ctx.email(),
                    ctx.phoneNumber(),
                    ctx.orderNumber(),
                    ctx.message()
            );
            return new EmailMessage(ctx.to(), getSubject(), body);
        }
    },

    INQUIRY_AUTO_REPLY(
            "お問い合わせ受付のお知らせ",
            """
                        %s %s 様

                        お問い合わせを受け付けました。
                        担当者より折り返しご連絡いたします。

                        【ご入力内容】
                        メール : %s
                        電話 : %s
                        注文番号 : %s

                        ※本メールは自動送信です。心当たりがない場合は破棄してください。
                    """) {
        @Override
        public EmailMessage build(Object... args) {
            InquiryContext ctx = (InquiryContext) args[0];

            String body = getBody().formatted(
                    ctx.lastName(),
                    ctx.firstName(),
                    ctx.email(),
                    ctx.phoneNumber(),
                    ctx.orderNumber()
            );
            return new EmailMessage(ctx.to(), getSubject(), body);
        }
    },

    SHIPPING_NOTIFICATION(
            "出荷のお知らせ",
            """
                    %s 様
                    
                    ご注文いただいた商品を出荷いたしました。
                    お届けまで今しばらくお待ちください。

                    【注文番号】
                    %s

                    【お届け先】
                    %s

                    【ご注文内容】
                    %s

                    --- 合計欄 ---
                    合計金額 : ¥%,d
                    """) {
        @Override
        public EmailMessage build(Object... args) {
            ShipmentContext ctx = (ShipmentContext) args[0];

            String body = getBody().formatted(
                    ctx.fullName(),
                    ctx.orderNumber(),
                    ctx.fullAddress(),
                    createItemsBlock(
                            ctx.items(),
                            OrderItem::getProductName,
                            OrderItem::getQty,
                            OrderItem::getUnitPriceIncl,
                            OrderItem::getSubtotalIncl
                    ),
                    ctx.totalIncl()
            );
            return new EmailMessage(ctx.to(), getSubject(), body);
        }
    },

    CANCEL_APPROVED(
            "キャンセル手続き完了のお知らせ",
            """
                    %s 様

                    以下のご注文のキャンセルを承りました。

                    【注文番号】
                    %s

                    【ご注文内容】
                    %s

                    --- 合計欄 ---
                    合計金額 : ¥%,d

                    またのご利用をお待ちしております。
                    """) {
        @Override
        public EmailMessage build(Object... args) {
            CancelApprovedContext ctx = (CancelApprovedContext) args[0];

            String body = getBody().formatted(
                    ctx.fullName(),
                    ctx.orderNumber(),
                    createItemsBlock(
                            ctx.items(),
                            OrderItem::getProductName,
                            OrderItem::getQty,
                            OrderItem::getUnitPriceIncl,
                            OrderItem::getSubtotalIncl
                    ),
                    ctx.totalIncl()
            );
            return new EmailMessage(ctx.to(), getSubject(), body);
        }
    },

    SHIPPED_AND_CANCEL_REJECTED(
            "出荷のお知らせとキャンセル不可のご連絡",
            """
                    %s 様

                    キャンセルのご依頼を確認しましたが、出荷手続きが先行したためキャンセルを承れませんでした。
                    併せて出荷情報をご案内します。

                    【注文番号】
                    %s

                    【ご注文内容】
                    %s

                    --- 合計欄 ---
                    合計金額 : ¥%,d

                    商品到着後の返品手続きについては別途ご案内いたします。
                    """) {
        @Override
        public EmailMessage build(Object... args) {
            CancelRejectedContext ctx = (CancelRejectedContext) args[0];

            String body = getBody().formatted(
                    ctx.fullName(),
                    ctx.orderNumber(),
                    createItemsBlock(
                            ctx.items(),
                            OrderItem::getProductName,
                            OrderItem::getQty,
                            OrderItem::getUnitPriceIncl,
                            OrderItem::getSubtotalIncl
                    ),
                    ctx.totalIncl()
            );
            return new EmailMessage(ctx.to(), getSubject(), body);
        }
    };

    private final String subject;
    private final String body;

    private MailTemplate(String subject, String body) {
        this.subject = subject;
        this.body = body;
    }

    public abstract EmailMessage build(Object... args);

    public record EmailMessage(String to, String subject, String body) {}

    public record OrderEditCompletedContext(
            String to,
            String fullName,
            String fullAddress,
            String orderNumber,
            List<OrderItem> items,
            int totalIncl
    ) {
        public OrderEditCompletedContext(
                String to,
                String fullName,
                String fullAddress,
                int orderNumber,
                List<OrderItem> items,
                int totalIncl
        ) {
            this(
                    to,
                    fullName,
                    fullAddress,
                    OrderUtil.formatOrderNumber(orderNumber),
                    items,
                    totalIncl
            );
        }
    }

    public record InquiryContext(
            String to,
            String lastName,
            String firstName,
            String email,
            String phoneNumber,
            String orderNumber,
            String message
    ) {
        public InquiryContext {
            orderNumber = orderNumber == null ? ""
                    : OrderUtil.formatOrderNumber(Integer.parseInt(orderNumber));
        }
    }

    public record ShipmentContext(
            String to,
            String fullName,
            String fullAddress,
            String orderNumber,
            List<OrderItem> items,
            int totalIncl
    ) {
        public ShipmentContext(
                String to,
                String fullName,
                String fullAddress,
                int orderNumber,
                List<OrderItem> items,
                int totalIncl
        ) {
            this(
                    to,
                    fullName,
                    fullAddress,
                    OrderUtil.formatOrderNumber(orderNumber),
                    items,
                    totalIncl
            );
        }
    }

    public record CancelApprovedContext(
            String to,
            String fullName,
            String orderNumber,
            List<OrderItem> items,
            int totalIncl
    ) {
        public CancelApprovedContext(
                String to,
                String fullName,
                int orderNumber,
                List<OrderItem> items,
                int totalIncl
        ) {
            this(
                    to,
                    fullName,
                    OrderUtil.formatOrderNumber(orderNumber),
                    items,
                    totalIncl
            );
        }
    }

    public record CancelRejectedContext(
            String to,
            String fullName,
            String orderNumber,
            List<OrderItem> items,
            int totalIncl
    ) {
        public CancelRejectedContext(
                String to,
                String fullName,
                int orderNumber,
                List<OrderItem> items,
                int totalIncl
        ) {
            this(
                    to,
                    fullName,
                    OrderUtil.formatOrderNumber(orderNumber),
                    items,
                    totalIncl
            );
        }
    }

    private static <T> String createItemsBlock(
            List<T> items,
            Function<T, String> productNameFn,
            ToIntFunction<T> qtyFn,
            ToIntFunction<T> priceFn,
            ToIntFunction<T> subtotalFn
    ) {
        return items.stream()
                .map(i -> """
                        %s
                        数量 : %d
                        価格 : ¥%,d
                        小計 : ¥%,d
                        """.formatted(
                        productNameFn.apply(i),
                        qtyFn.applyAsInt(i),
                        priceFn.applyAsInt(i),
                        subtotalFn.applyAsInt(i)
                ))
                .collect(Collectors.joining("\n"));
    }
}
