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
    // 返品、交換
    // メール送信の整合性
    // 管理者アドレスは仮値

    private final MailGateway mailGateway;

    public void handle(InquiryRequest req) {
        InquiryContext adminCtx = new InquiryContext(
                "admin@example.com",
                req.getLastName(),
                req.getFirstName(),
                req.getEmail(),
                req.getPhoneNumber(),
                req.getOrderNumber() == null ? "" : req.getOrderNumber(),
                req.getMessage());
        mailGateway.send(MailTemplate.INQUIRY_ADMIN_NOTIFICATION.build(adminCtx));

        InquiryContext userCtx = new InquiryContext(
                req.getEmail(),
                req.getLastName(),
                req.getFirstName(),
                req.getEmail(),
                req.getPhoneNumber(),
                req.getOrderNumber() == null ? "" : req.getOrderNumber(),
                null);
        mailGateway.send(MailTemplate.INQUIRY_AUTO_REPLY.build(userCtx));
    }
}
