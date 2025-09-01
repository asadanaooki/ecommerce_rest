package com.example.enums;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import com.example.dto.CheckoutItemDto;
import com.example.entity.OrderItem;
import com.example.enums.order.RejectReason;
import com.example.util.OrderUtil;

import lombok.AllArgsConstructor;
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
        public EmailMessage build(MailContext ctx) {
            RegistrationContext c = (RegistrationContext) ctx; // 対応 Context にキャスト
            String body = getBody().formatted(
                    c.getLink(),
                    c.getTtlMinutes()
            );
            return new EmailMessage(c.getTo(), getSubject(), body);
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
                    商品小計 : ¥%,d
                    送料     : ¥%,d
                    お支払合計 : ¥%,d

                    ※本メールは自動送信です。ご不明点がございましたらお問い合わせフォームよりご連絡ください。
                    """) {
        @Override
        public EmailMessage build(MailContext ctx) {
            OrderConfirmationContext c = (OrderConfirmationContext) ctx;
            String body = getBody().formatted(
                    c.getFullName(),
                    c.getFullName(),
                    c.getFullAddress(),
                    c.getOrderNumber(),
                    createItemsBlock(
                            c.getItems(),
                            CheckoutItemDto::getProductName,
                            CheckoutItemDto::getQty,
                            CheckoutItemDto::getUnitPriceIncl,
                            CheckoutItemDto::getSubtotalIncl
                    ),
                    c.getItemsSubtotalIncl(),
                    c.getShippingFeeIncl(),
                    c.getGrandTotalIncl()
            );
            return new EmailMessage(c.getTo(), getSubject(), body);
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
        public EmailMessage build(MailContext ctx) {
            PasswordResetContext c = (PasswordResetContext) ctx;
            String body = getBody().formatted(
                    c.getLink(),
                    c.getTtlMinutes()
            );
            return new EmailMessage(c.getTo(), getSubject(), body);
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
        public EmailMessage build(MailContext ctx) {
            EmailChangeCompleteNewContext c = (EmailChangeCompleteNewContext) ctx;
            String body = getBody().formatted(
                    c.getLink(),
                    c.getTtlMinutes()
            );
            return new EmailMessage(c.getTo(), getSubject(), body);
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
        public EmailMessage build(MailContext ctx) {
            EmailChangeAlertOldContext c = (EmailChangeAlertOldContext) ctx;
            String body = getBody().formatted(
                    c.getWhen().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH時mm分"))
            );
            return new EmailMessage(c.getTo(), getSubject(), body);
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
        public EmailMessage build(MailContext ctx) {
            ProfileChangedContext c = (ProfileChangedContext) ctx;
            String body = getBody().formatted(
                    c.getWhen().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH時mm分"))
            );
            return new EmailMessage(c.getTo(), getSubject(), body);
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
                    商品小計 : ¥%,d
                    送料     : ¥%,d
                    お支払合計 : ¥%,d
                        """) {
        @Override
        public EmailMessage build(MailContext ctx) {
            OrderEditCompletedContext c = (OrderEditCompletedContext) ctx;
            String body = getBody().formatted(
                    c.getFullName(),
                    c.getFullName(),
                    c.getFullAddress(),
                    c.getOrderNumber(),
                    createItemsBlock(
                            c.getItems(),
                            OrderItem::getProductName,
                            OrderItem::getQty,
                            OrderItem::getUnitPriceIncl,
                            OrderItem::getSubtotalIncl
                    ),
                    c.getItemsSubtotalIncl(),
                    c.getShippingFeeIncl(),
                    c.getGrandTotalIncl()
            );
            return new EmailMessage(c.getTo(), getSubject(), body);
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
        public EmailMessage build(MailContext ctx) {
            InquiryContext c = (InquiryContext) ctx;
            String body = getBody().formatted(
                    c.getLastName(),
                    c.getFirstName(),
                    c.getEmail(),
                    c.getPhoneNumber(),
                    c.getOrderNumber(),
                    c.getMessage()
            );
            return new EmailMessage(c.getTo(), getSubject(), body);
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
        public EmailMessage build(MailContext ctx) {
            InquiryContext c = (InquiryContext) ctx;
            String body = getBody().formatted(
                    c.getLastName(),
                    c.getFirstName(),
                    c.getEmail(),
                    c.getPhoneNumber(),
                    c.getOrderNumber()
            );
            return new EmailMessage(c.getTo(), getSubject(), body);
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
                    商品小計 : ¥%,d
                    送料     : ¥%,d
                    お支払合計 : ¥%,d
                    """) {
        @Override
        public EmailMessage build(MailContext ctx) {
            ShipmentContext c = (ShipmentContext) ctx;
            String body = getBody().formatted(
                    c.getFullName(),
                    c.getOrderNumber(),
                    c.getFullAddress(),
                    createItemsBlock(
                            c.getItems(),
                            OrderItem::getProductName,
                            OrderItem::getQty,
                            OrderItem::getUnitPriceIncl,
                            OrderItem::getSubtotalIncl
                    ),
                    c.getItemsSubtotalIncl(),
                    c.getShippingFeeIncl(),
                    c.getGrandTotalIncl()
            );
            return new EmailMessage(c.getTo(), getSubject(), body);
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
                    商品小計 : ¥%,d
                    送料     : ¥%,d
                    お支払合計 : ¥%,d

                    またのご利用をお待ちしております。
                    """) {
        @Override
        public EmailMessage build(MailContext ctx) {
            CancelApprovedContext c = (CancelApprovedContext) ctx;
            String body = getBody().formatted(
                    c.getFullName(),
                    c.getOrderNumber(),
                    createItemsBlock(
                            c.getItems(),
                            OrderItem::getProductName,
                            OrderItem::getQty,
                            OrderItem::getUnitPriceIncl,
                            OrderItem::getSubtotalIncl
                    ),
                    c.getItemsSubtotalIncl(),
                    c.getShippingFeeIncl(),
                    c.getGrandTotalIncl()
            );
            return new EmailMessage(c.getTo(), getSubject(), body);
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
                    商品小計 : ¥%,d
                    送料     : ¥%,d
                    お支払合計 : ¥%,d

                    商品到着後の返品手続きについては別途ご案内いたします。
                    """) {
        @Override
        public EmailMessage build(MailContext ctx) {
            CancelRejectedContext c = (CancelRejectedContext) ctx;
            String body = getBody().formatted(
                    c.getFullName(),
                    c.getOrderNumber(),
                    createItemsBlock(
                            c.getItems(),
                            OrderItem::getProductName,
                            OrderItem::getQty,
                            OrderItem::getUnitPriceIncl,
                            OrderItem::getSubtotalIncl
                    ),
                    c.getItemsSubtotalIncl(),
                    c.getShippingFeeIncl(),
                    c.getGrandTotalIncl()
            );
            return new EmailMessage(c.getTo(), getSubject(), body);
        }
    },
    
    REVIEW_REJECTED(
            "レビュー掲載見送りのお知らせ",
            """
                    %s 様

                    ご投稿いただいたレビューは以下の理由により掲載できませんでした。

                    【否認理由】
                    %s

                    %s

                    再投稿される場合は、お手数ですが内容を修正のうえ送信してください。
                    """) {
        @Override
        public EmailMessage build(MailContext ctx) {
            ReviewRejectedContext c = (ReviewRejectedContext) ctx;
            String noteBlock = (c.getNote() == null || c.getNote().isBlank()) ? ""
                    : """
                        【備考】
                        %s
                        
                      """.formatted(c.getNote());

            String body = getBody().formatted(
                    c.getFullName(),
                    c.getReason().getMessage(),
                    noteBlock
            );
            return new EmailMessage(c.getTo(), getSubject(), body);
        }
    };

    private final String subject;
    private final String body;

    MailTemplate(String subject, String body) {
        this.subject = subject;
        this.body = body;
    }

    public abstract EmailMessage build(MailContext ctx);

    // ===================== 共通 DTO =====================
    @Getter
    @AllArgsConstructor
    public static class EmailMessage {
        private final String to;
        private final String subject;
        private final String body;
    }

    // ===================== Context 型 =====================

    /** マーカー */
    public interface MailContext {}

    // --- 1) 合計計算なし（@AllArgsConstructor） ---

    @Getter @AllArgsConstructor
    public static class RegistrationContext implements MailContext {
        private final String to;
        private final String link;
        private final long ttlMinutes;
    }

    @Getter @AllArgsConstructor
    public static class PasswordResetContext implements MailContext {
        private final String to;
        private final String link;
        private final long ttlMinutes;
    }

    @Getter @AllArgsConstructor
    public static class EmailChangeCompleteNewContext implements MailContext {
        private final String to;
        private final String link;
        private final long ttlMinutes;
    }

    @Getter @AllArgsConstructor
    public static class EmailChangeAlertOldContext implements MailContext {
        private final String to;
        private final LocalDateTime when;
    }

    @Getter @AllArgsConstructor
    public static class ProfileChangedContext implements MailContext {
        private final String to;
        private final LocalDateTime when;
    }

    @Getter @AllArgsConstructor
    public static class InquiryContext implements MailContext {
        private final String to;
        private final String lastName;
        private final String firstName;
        private final String email;
        private final String phoneNumber;
        private final String orderNumber; // 文字列（フォーマット済みを想定）
        private final String message;

        /** 便宜：数値注文番号を渡された時のフォーマット付きコンストラクタ */
        public InquiryContext(String to, String lastName, String firstName, String email,
                              String phoneNumber, Integer orderNumber, String message) {
            this(to, lastName, firstName, email, phoneNumber,
                    orderNumber == null ? "" : OrderUtil.formatOrderNumber(orderNumber), message);
        }
    }

    @Getter @AllArgsConstructor
    public static class ReviewRejectedContext implements MailContext {
        private final String to;
        private final String fullName;
        private final RejectReason reason;
        private final String note;
    }

    // --- 2) 合計計算あり（items から合計を内製） ---

    @Getter
    public static class OrderConfirmationContext implements MailContext {
        private final String to;
        private final String fullName;
        private final String fullAddress;
        private final String orderNumber; // フォーマット済
        private final List<CheckoutItemDto> items;

        private final int itemsSubtotalIncl;
        private final int shippingFeeIncl;
        private final int grandTotalIncl;

        public OrderConfirmationContext(String to, String fullName, String fullAddress,
                                        int orderNumber, List<CheckoutItemDto> items) {
            this.to = to;
            this.fullName = fullName;
            this.fullAddress = fullAddress;
            this.orderNumber = OrderUtil.formatOrderNumber(orderNumber);
            this.items = items;

            int subtotalIncl = OrderUtil.sumBy(items, CheckoutItemDto::getSubtotalIncl);
            int shippingFeeIncl = OrderUtil.calculateShippingFeeIncl(subtotalIncl);
            int grandTotalIncl = OrderUtil.calculateGrandTotalIncl(subtotalIncl, shippingFeeIncl);

            this.itemsSubtotalIncl = subtotalIncl;
            this.shippingFeeIncl = shippingFeeIncl;
            this.grandTotalIncl = grandTotalIncl;
        }
    }

    @Getter
    public static class OrderEditCompletedContext implements MailContext {
        private final String to;
        private final String fullName;
        private final String fullAddress;
        private final String orderNumber; // フォーマット済
        private final List<OrderItem> items;

        private final int itemsSubtotalIncl;
        private final int shippingFeeIncl;
        private final int grandTotalIncl;

        public OrderEditCompletedContext(String to, String fullName, String fullAddress,
                                         int orderNumber, List<OrderItem> items) {
            this.to = to;
            this.fullName = fullName;
            this.fullAddress = fullAddress;
            this.orderNumber = OrderUtil.formatOrderNumber(orderNumber);
            this.items = items;

            int subtotalIncl = OrderUtil.sumBy(items, OrderItem::getSubtotalIncl);
            int shippingFeeIncl = OrderUtil.calculateShippingFeeIncl(subtotalIncl);
            int grandTotalIncl = OrderUtil.calculateGrandTotalIncl(subtotalIncl, shippingFeeIncl);

            this.itemsSubtotalIncl = subtotalIncl;
            this.shippingFeeIncl = shippingFeeIncl;
            this.grandTotalIncl = grandTotalIncl;
        }
    }

    @Getter
    public static class ShipmentContext implements MailContext {
        private final String to;
        private final String fullName;
        private final String fullAddress;
        private final String orderNumber; // フォーマット済
        private final List<OrderItem> items;

        private final int itemsSubtotalIncl;
        private final int shippingFeeIncl;
        private final int grandTotalIncl;

        public ShipmentContext(String to, String fullName, String fullAddress,
                               int orderNumber, List<OrderItem> items) {
            this.to = to;
            this.fullName = fullName;
            this.fullAddress = fullAddress;
            this.orderNumber = OrderUtil.formatOrderNumber(orderNumber);
            this.items = items;

            int subtotalIncl = OrderUtil.sumBy(items, OrderItem::getSubtotalIncl);
            int shippingFeeIncl = OrderUtil.calculateShippingFeeIncl(subtotalIncl);
            int grandTotalIncl = OrderUtil.calculateGrandTotalIncl(subtotalIncl, shippingFeeIncl);

            this.itemsSubtotalIncl = subtotalIncl;
            this.shippingFeeIncl = shippingFeeIncl;
            this.grandTotalIncl = grandTotalIncl;
        }
    }

    @Getter
    public static class CancelApprovedContext implements MailContext {
        private final String to;
        private final String fullName;
        private final String orderNumber; // フォーマット済
        private final List<OrderItem> items;

        private final int itemsSubtotalIncl;
        private final int shippingFeeIncl;
        private final int grandTotalIncl;

        public CancelApprovedContext(String to, String fullName,
                                     int orderNumber, List<OrderItem> items) {
            this.to = to;
            this.fullName = fullName;
            this.orderNumber = OrderUtil.formatOrderNumber(orderNumber);
            this.items = items;

            int subtotalIncl = OrderUtil.sumBy(items, OrderItem::getSubtotalIncl);
            int shippingFeeIncl = OrderUtil.calculateShippingFeeIncl(subtotalIncl);
            int grandTotalIncl = OrderUtil.calculateGrandTotalIncl(subtotalIncl, shippingFeeIncl);

            this.itemsSubtotalIncl = subtotalIncl;
            this.shippingFeeIncl = shippingFeeIncl;
            this.grandTotalIncl = grandTotalIncl;
        }
    }

    @Getter
    public static class CancelRejectedContext implements MailContext {
        private final String to;
        private final String fullName;
        private final String orderNumber; // フォーマット済
        private final List<OrderItem> items;

        private final int itemsSubtotalIncl;
        private final int shippingFeeIncl;
        private final int grandTotalIncl;

        public CancelRejectedContext(String to, String fullName,
                                     int orderNumber, List<OrderItem> items) {
            this.to = to;
            this.fullName = fullName;
            this.orderNumber = OrderUtil.formatOrderNumber(orderNumber);
            this.items = items;

            int subtotalIncl = OrderUtil.sumBy(items, OrderItem::getSubtotalIncl);
            int shippingFeeIncl = OrderUtil.calculateShippingFeeIncl(subtotalIncl);
            int grandTotalIncl = OrderUtil.calculateGrandTotalIncl(subtotalIncl, shippingFeeIncl);

            this.itemsSubtotalIncl = subtotalIncl;
            this.shippingFeeIncl = shippingFeeIncl;
            this.grandTotalIncl = grandTotalIncl;
        }
    }

    // ===================== 共通ヘルパ =====================

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
