package com.sun.wineshop.job;

import com.sun.wineshop.model.entity.User;
import com.sun.wineshop.model.entity.VerificationToken;
import com.sun.wineshop.repository.UserRepository;
import com.sun.wineshop.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountCleanupJob {

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    /**
     * Run at 1 am every day
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void cleanupExpiredAccounts() {
        log.info("[CLEANUP] Start check token expired...");

        List<VerificationToken> expiredTokens = tokenRepository.findAllByExpiryDateBefore(LocalDateTime.now());

        for (VerificationToken token : expiredTokens) {
            User user = token.getUser();

            if (!user.isActive()) {
                log.info("[CLEANUP] Remove account inactive: {}", user.getEmail());
                userRepository.delete(user);
            }

            tokenRepository.delete(token);
        }

        log.info("[CLEANUP] Done clean {} token.", expiredTokens.size());
    }
}
