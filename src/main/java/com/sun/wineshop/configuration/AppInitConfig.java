package com.sun.wineshop.configuration;

import com.sun.wineshop.model.entity.User;
import com.sun.wineshop.model.enums.UserRole;
import com.sun.wineshop.repository.UserRepository;
import com.sun.wineshop.service.PasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AppInitConfig {

    private final SecurityProperties securityProperties;

    @Autowired
    private PasswordService passwordService;

    @Bean
    ApplicationRunner init(UserRepository userRepository) {
        return args -> {
            if (userRepository.findUserByUsername(securityProperties.getAdmin().getUsername()).isEmpty()) {
                User user = User.builder()
                        .username(securityProperties.getAdmin().getUsername())
                        .password(passwordService.encodePassword(securityProperties.getAdmin().getPassword()))
                        .role(UserRole.ADMIN.name())
                        .build();
                userRepository.save(user);
                log.warn("Admin created");
            }
        };
    }
}
