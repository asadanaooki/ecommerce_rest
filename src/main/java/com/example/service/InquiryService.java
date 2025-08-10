package com.example.service;

import org.springframework.stereotype.Service;

import com.example.enums.MailTemplate;
import com.example.enums.MailTemplate.InquiryContext;
import com.example.request.InquiryRequest;
import com.example.support.MailGateway;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class InquiryService {
    // TODO:
    // ダッシュボードでリアルタイムで問い合わせ検知する
    // 注文キャンセル、返品、交換→マイページ経由を検討。現状、お問い合わせフォームで対応
    // メール送信の整合性
    // 管理者アドレスは仮値

    private final MailGateway mailGateway;

    public void handle(InquiryRequest req) {
        String orderNo = req.getOrderNo() == null ? ""
                : String.format("%04d", Integer.parseInt(req.getOrderNo()));

        InquiryContext adminCtx = new InquiryContext(
                "admin@example.com",
                req.getLastName(),
                req.getFirstName(),
                req.getEmail(),
                req.getPhoneNumber(),
                orderNo,
                req.getMessage());
        mailGateway.send(MailTemplate.INQUIRY_ADMIN_NOTIFICATION.build(adminCtx));

        InquiryContext userCtx = new InquiryContext(
                req.getEmail(),
                req.getLastName(),
                req.getFirstName(),
                req.getEmail(),
                req.getPhoneNumber(),
                orderNo,
                null);
        mailGateway.send(MailTemplate.INQUIRY_AUTO_REPLY.build(userCtx));
    }
}
