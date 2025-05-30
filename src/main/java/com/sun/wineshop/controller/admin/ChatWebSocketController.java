package com.sun.wineshop.controller.admin;

import com.sun.wineshop.configuration.CustomJwtDecoder;
import com.sun.wineshop.dto.request.MessageRequest;
import com.sun.wineshop.dto.response.MessageResponse;
import com.sun.wineshop.service.ChatService;
import com.sun.wineshop.utils.api.AdminApiPaths;
import com.sun.wineshop.utils.api.UserApiPaths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController(AdminApiPaths.Chat.ADMIN_CHAT_CONTROLLER)
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final CustomJwtDecoder customJwtDecoder;

    @MessageMapping(AdminApiPaths.Chat.SEND)
    public void handleMessage(SimpMessageHeaderAccessor accessor, @Payload MessageRequest request) {
        MessageResponse response = chatService.saveMessage(request);
        messagingTemplate.convertAndSend(UserApiPaths.Chat.WEBSOCKET_BROKER + request.userId(), response);
        messagingTemplate.convertAndSend(AdminApiPaths.Chat.WEBSOCKET_BROKER, response);
    }
}
