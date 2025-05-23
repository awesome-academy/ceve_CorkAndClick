package com.sun.wineshop.service;

public interface EmailService {
    void sendActivationEmail(String to, String name, String activationLink);
}
