package com.sun.wineshop.service;

import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.utils.AppConstants;
import com.sun.wineshop.utils.MessageUtil;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final MessageUtil messageUtil;

    public void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            ClassPathResource resource = new ClassPathResource(AppConstants.MAIL_TEMPLATE_FILE);
            String template = Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);

            String htmlContent = template.replace("{{fullName}}", fullName);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(messageUtil.getMessage("welcome.mail.subject"));
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            throw new AppException(ErrorCode.SEND_MAIL_FAIL);
        }
    }

}
