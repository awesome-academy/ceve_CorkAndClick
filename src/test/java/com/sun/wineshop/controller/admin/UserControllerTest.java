package com.sun.wineshop.controller.admin;

import com.sun.wineshop.configuration.CustomJwtDecoder;
import com.sun.wineshop.configuration.SecurityConfig;
import com.sun.wineshop.dto.response.UserResponse;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.exception.GlobalExceptionHandler;
import com.sun.wineshop.service.UserService;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.AdminApiPaths;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({
        UserControllerTest.TestConfig.class,
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @TestConfiguration
    public static class TestConfig {
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
    private UserService userService;

    @MockitoBean
    private CustomJwtDecoder customJwtDecoder;

    @Autowired
    private MessageUtil messageUtil;

    @Test
    void getAllUsers_shouldReturnPageOfUsers() throws Exception {
        UserResponse user1 = sampleUserResponse(1L, "user1");
        UserResponse user2 = sampleUserResponse(2L, "user2");

        Page<UserResponse> page = new PageImpl<>(List.of(user1, user2));

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get(AdminApiPaths.User.BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].username").value("user1"))
                .andExpect(jsonPath("$.content[1].username").value("user2"));
    }

    @Test
    void getUserById_shouldReturnUserResponse() throws Exception {
        UserResponse user = sampleUserResponse(1L, "user1");

        when(userService.getUserByUserId(1L)).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.get(AdminApiPaths.User.BASE + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.username").value("user1"))
                .andExpect(jsonPath("$.data.email").value("user1@example.com"));
    }

    @Test
    void getUserById_whenNotFound_shouldReturnNotFound() throws Exception {
        when(userService.getUserByUserId(999L))
                .thenThrow(new AppException(ErrorCode.USER_NOT_EXIST));

        mockMvc.perform(MockMvcRequestBuilders.get(AdminApiPaths.User.BASE + "/999"))
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_EXIST.getCode()))
                .andExpect(jsonPath("$.message").value(messageUtil.getMessage(ErrorCode.USER_NOT_EXIST.getMessageKey())));
    }

    private UserResponse sampleUserResponse(Long id, String username) {
        return UserResponse.builder()
                .id(id)
                .username(username)
                .fullName("Full " + username)
                .email(username + "@example.com")
                .phone("0123456789")
                .address("123 Test Street")
                .birthday(LocalDateTime.of(1995, 5, 15, 0, 0))
                .role("USER")
                .build();
    }
}
