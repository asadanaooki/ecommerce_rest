package com.example.support;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.example.enums.MailTemplate.EmailMessage;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class MailGateway {
    // TODO:
    // メール送信はローカルで仮実装
    // sendされるmsgの中身まで単体テストで見るべきか？setTextやsetSubjectなど
    // linkは仮で固定値localhost

    private final JavaMailSender sender;
    
    public void send(EmailMessage msg) {
        MimeMessage mime = sender.createMimeMessage();
        MimeMessageHelper h = new MimeMessageHelper(mime, "UTF-8");

        try {
            h.setTo(msg.to());
            h.setSubject(msg.subject());
            h.setText(msg.body());
            sender.send(mime);
        } catch (MessagingException e) {
            throw new MailPreparationException(e);
        }
    }
}
