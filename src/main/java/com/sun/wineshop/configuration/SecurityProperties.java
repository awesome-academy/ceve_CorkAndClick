package com.sun.wineshop.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:app-security-config.properties")
@ConfigurationProperties(prefix = "security")
@Data
public class SecurityProperties {
    private Jwt jwt;
    private Admin admin;

    @Data
    public static class Jwt {
        private String signerKey;
        private String domain;
    }

    @Data
    public static class Admin {
        private String username;
        private String password;
    }
}
