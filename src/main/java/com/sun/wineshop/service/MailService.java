package com.sun.wineshop.service;

import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.utils.MessageUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final MessageUtil messageUtil;

    public void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject(messageUtil.getMessage("welcome.mail.subject"));
            helper.setText(messageUtil.getMessage("welcome.mail.body", fullName), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_MAIL_FAIL);
        }
    }
}
