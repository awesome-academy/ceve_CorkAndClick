package com.sun.wineshop.controller;

import com.sun.wineshop.dto.response.MessageResponse;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.exception.GlobalExceptionHandler;
import com.sun.wineshop.service.ChatService;
import com.sun.wineshop.utils.AppConstants;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.UserApiPaths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@Import({
        ChatControllerTest.TestConfig.class,
        GlobalExceptionHandler.class
})
@AutoConfigureMockMvc(addFilters = false)
class ChatControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public MessageSource messageSource() {
            ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
            source.setBasename("classpath:messages");
            source.setDefaultEncoding("UTF-8");
            return source;
        }

        @Bean
        public MessageUtil messageUtil(MessageSource source) {
            return new MessageUtil(source);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @Autowired
    private MessageUtil messageUtil;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getChatHistory_validUserId_shouldReturnMessageList() throws Exception {
        Long userId = 123L;
        Jwt jwt = buildJwtWithUserId(userId);

        List<MessageResponse> mockMessages = List.of(
                new MessageResponse("USER", "Hello", LocalDateTime.now())
        );

        Mockito.when(chatService.getMessageHistory(userId)).thenReturn(mockMessages);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new JwtAuthenticationToken(jwt));
        SecurityContextHolder.setContext(context);

        mockMvc.perform(MockMvcRequestBuilders.get(UserApiPaths.Chat.BASE + UserApiPaths.Chat.HISTORY)
                        .principal(jwt::getTokenValue)
                        .header("Authorization", "Bearer " + jwt.getTokenValue())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getChatHistory_missingUserIdClaim_shouldReturnUnauthorized() throws Exception {
        Jwt jwt = buildJwtWithMissingUserId();

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new JwtAuthenticationToken(jwt));
        SecurityContextHolder.setContext(context);

        mockMvc.perform(MockMvcRequestBuilders.get(UserApiPaths.Chat.BASE + UserApiPaths.Chat.HISTORY)
                        .principal(jwt::getTokenValue)
                        .header("Authorization", "Bearer " + jwt.getTokenValue())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getCode()))
                .andExpect(jsonPath("$.message").value(messageUtil.getMessage(ErrorCode.UNAUTHORIZED.getMessageKey())));
    }

    private Jwt buildJwtWithUserId(Long userId) {
        Map<String, Object> claims = Map.of(
                AppConstants.JWT_USER_ID, userId
        );
        return new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                claims
        );
    }

    private Jwt buildJwtWithMissingUserId() {
        Map<String, Object> claims = Map.of("sub", "someUser");
        return new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                claims
        );
    }
}
