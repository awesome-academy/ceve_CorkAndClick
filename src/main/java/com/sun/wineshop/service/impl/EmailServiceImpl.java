package com.sun.wineshop.service.impl;

import com.sun.wineshop.configuration.SecurityProperties;
import com.sun.wineshop.service.EmailService;
import com.sun.wineshop.utils.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private MessageUtil messageUtil;
    @Autowired
    private SecurityProperties securityProperties;

    @Async
    @Override
    public void sendActivationEmail(String to, String name, String activationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(messageUtil.getMessage("mail.activation.subject"));
        message.setText(messageUtil.getMessage("mail.activation.content") + activationLink);
        message.setFrom(securityProperties.getMail().getOwner());

        mailSender.send(message);
    }
}
