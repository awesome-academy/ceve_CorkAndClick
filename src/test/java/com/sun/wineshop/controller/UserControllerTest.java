package com.sun.wineshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.wineshop.configuration.CustomJwtDecoder;
import com.sun.wineshop.configuration.SecurityConfig;
import com.sun.wineshop.dto.request.CreateUserRequest;
import com.sun.wineshop.dto.response.UserResponse;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.exception.GlobalExceptionHandler;
import com.sun.wineshop.service.UserService;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.UserApiPaths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@WebMvcTest(UserController.class)
@Import({
        UserControllerTest.TestConfig.class,
        SecurityConfig.class,
        GlobalExceptionHandler.class,
})
class UserControllerTest {

    @TestConfiguration
    public static class TestConfig {

        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }

        @Bean
        public MessageSource messageSource() {
            ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
            messageSource.setBasename("classpath:messages");
            messageSource.setDefaultEncoding("UTF-8");
            return messageSource;
        }

        @Bean
        public MessageUtil messageUtil(MessageSource messageSource) {
            return new MessageUtil(messageSource);
        }

        @Bean
        public CustomJwtDecoder customJwtDecoder() {
            return Mockito.mock(CustomJwtDecoder.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private MessageUtil messageUtil;

    private CreateUserRequest createUserRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {

        LocalDateTime dob = LocalDateTime.parse("1996-04-22T21:05:20.968331");
        createUserRequest = CreateUserRequest.builder()
                .username("riophan")
                .password("12345678")
                .fullName("Rio Phan")
                .email("rio@gmail.com")
                .phone("0963389695")
                .address("Hai Châu, Đà Nẵng")
                .birthday(dob)
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .username("riophan")
                .fullName("Rio Phan")
                .email("rio@gmail.com")
                .phone("0963389695")
                .address("Hai Châu, Đà Nẵng")
                .birthday(dob)
                .role("USER")
                .build();
    }

    @AfterEach
    void resetSecurityContext() {
        Mockito.reset(userService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void createUser_validRequest_success() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String requestBody = objectMapper.writeValueAsString(createUserRequest);

        when(userService.createUser(ArgumentMatchers.any())).thenReturn(userResponse);

        mockMvc.perform(MockMvcRequestBuilders.post(UserApiPaths.Endpoint.FULL_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.username").value("riophan"));
    }

    @Test
    void createUser_InvalidRequest_fail() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        CreateUserRequest invalidRequest = CreateUserRequest.builder()
                .username("ri")
                .password("12345678")
                .fullName("Rio Phan")
                .email("rio@gmail.com")
                .phone("0963389695")
                .address("Hai Châu, Đà Nẵng")
                .birthday(LocalDateTime.parse("1996-04-22T21:05:20.968331"))
                .build();

        String requestBody = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(MockMvcRequestBuilders.post(UserApiPaths.Endpoint.FULL_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(40101))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Username must be at least 3 characters!"));

    }

    @Test
    void createUser_invalidPassword_fail() throws Exception {

        CreateUserRequest invalidRequest = CreateUserRequest.builder()
                .username("rio")
                .password("1234567")
                .fullName("Rio Phan")
                .email("rio@gmail.com")
                .phone("0963389695")
                .address("Hai Châu, Đà Nẵng")
                .birthday(LocalDateTime.parse("1996-04-22T21:05:20.968331"))
                .build();

        String requestBody = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .writeValueAsString(invalidRequest);

        mockMvc.perform(MockMvcRequestBuilders.post(UserApiPaths.Endpoint.FULL_REGISTER)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(40103))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Password must be at least 8 characters!"));
    }

    @WithMockUser(username = "riophan")
    @Test
    void getInfoCurrentUser_withAuth_success() throws Exception {
        when(userService.getCurrentUser()).thenReturn(userResponse);

        mockMvc.perform(MockMvcRequestBuilders.get(UserApiPaths.Endpoint.FULL_INFO))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.username").value("riophan"));
    }

    @WithMockUser(username = "riophan")
    @Test
    void getInfoCurrentUser_userNotFound_throwException() throws Exception {
        when(userService.getCurrentUser())
                .thenThrow(new AppException(ErrorCode.USER_NOT_FOUND_FROM_TOKEN));

        mockMvc.perform(MockMvcRequestBuilders.get(UserApiPaths.Endpoint.FULL_INFO))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND_FROM_TOKEN.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(messageUtil.getMessage(ErrorCode.USER_NOT_FOUND_FROM_TOKEN.getMessageKey())));
    }

    @Test
    void getInfoCurrentUser_withoutAuth_fail() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(UserApiPaths.Endpoint.FULL_INFO))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
}