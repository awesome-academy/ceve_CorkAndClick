package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.request.CreateUserRequest;
import com.sun.wineshop.dto.response.UserResponse;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.model.entity.User;
import com.sun.wineshop.repository.UserRepository;
import com.sun.wineshop.service.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private UserServiceImpl userService;

    private CreateUserRequest createUserRequest;
    private User user;

    @BeforeEach
    void setUp() {
        createUserRequest = CreateUserRequest.builder()
                .username("riophan")
                .password("12345678")
                .fullName("Rio Phan")
                .email("riophan1996@gmail.com")
                .phone("0963389695")
                .address("Hai Châu, Đà Nẵng")
                .birthday(LocalDateTime.of(1996, 4, 22, 21, 5, 20))
                .build();

        user = User.builder()
                .id(1L)
                .username(createUserRequest.username())
                .fullName(createUserRequest.fullName())
                .email(createUserRequest.email())
                .phone(createUserRequest.phone())
                .address(createUserRequest.address())
                .birthday(createUserRequest.birthday())
                .password("hashedPwd")
                .role("USER")
                .build();
    }

    @Test
    void createUser_whenUsernameExists_shouldThrowAppException() {
        when(userRepository.existsUserByUsername("riophan")).thenReturn(true);

        AppException ex = assertThrows(AppException.class,
                () -> userService.createUser(createUserRequest));

        assertEquals(ErrorCode.USER_EXISTED, ex.getErrorCode());
    }

    @Test
    void createUser_whenValidRequest_shouldReturnUserResponse() {
        when(userRepository.existsUserByUsername(createUserRequest.username())).thenReturn(false);
        when(passwordService.encodePassword(createUserRequest.password())).thenReturn("hashedPwd");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.createUser(createUserRequest);

        assertEquals("riophan", response.username());
        assertEquals("Rio Phan", response.fullName());
    }

    @Test
    void getAllUsers_shouldReturnPagedUserResponses() {
        Page<User> users = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(users);

        Page<UserResponse> result = userService.getAllUsers(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("riophan", result.getContent().getFirst().username());
    }

    @Test
    void getUserByUserId_whenNotFound_shouldThrowAppException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> userService.getUserByUserId(99L));

        assertEquals(ErrorCode.USER_NOT_EXIST, ex.getErrorCode());
    }

    @Test
    void getUserByUserId_whenFound_shouldReturnUserResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserByUserId(1L);

        assertEquals("riophan", response.username());
    }

    @Test
    void getCurrentUser_whenUserNotFound_shouldThrowAppException() {
        SecurityContext context = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);

        when(auth.getName()).thenReturn("riophan");
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        when(userRepository.findUserByUsername("riophan")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> userService.getCurrentUser());

        assertEquals(ErrorCode.USER_NOT_FOUND_FROM_TOKEN, ex.getErrorCode());
    }

    @Test
    void getCurrentUser_whenFound_shouldReturnUserResponse() {
        SecurityContext context = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);

        when(auth.getName()).thenReturn("riophan");
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        when(userRepository.findUserByUsername("riophan")).thenReturn(Optional.of(user));

        UserResponse response = userService.getCurrentUser();

        assertEquals("riophan", response.username());
    }
}
