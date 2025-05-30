package com.sun.wineshop.controller;

import com.sun.wineshop.dto.response.MessageResponse;
import com.sun.wineshop.service.ChatService;
import com.sun.wineshop.utils.JwtUtil;
import com.sun.wineshop.utils.api.UserApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping(UserApiPaths.Chat.BASE)
public class ChatController {

    private final ChatService chatService;

    @GetMapping(UserApiPaths.Chat.HISTORY)
    @ResponseBody
    public ResponseEntity<List<MessageResponse>> getChatHistory(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(chatService.getMessageHistory(JwtUtil.extractUserIdFromJwt(jwt)));
    }
}
