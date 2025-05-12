package com.sun.wineshop.controller.admin;

import com.sun.wineshop.dto.response.BaseApiResponse;
import com.sun.wineshop.dto.response.UserResponse;
import com.sun.wineshop.service.UserService;
import com.sun.wineshop.utils.api.AdminApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(AdminApiPaths.User.BASE)
public class UserController {

    private final UserService userService;

    @GetMapping()
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping(AdminApiPaths.User.BY_ID)
    public ResponseEntity<BaseApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(new BaseApiResponse<>(
                HttpStatus.OK.value(),
                userService.getUserByUserId(id)
        ));
    }

    // Add other actions for admin here...
}
