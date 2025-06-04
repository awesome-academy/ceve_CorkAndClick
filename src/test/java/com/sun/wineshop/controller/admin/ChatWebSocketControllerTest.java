package com.sun.wineshop.controller.admin;

import com.sun.wineshop.configuration.CustomJwtDecoder;
import com.sun.wineshop.dto.request.MessageRequest;
import com.sun.wineshop.dto.response.MessageResponse;
import com.sun.wineshop.service.ChatService;
import com.sun.wineshop.utils.api.AdminApiPaths;
import com.sun.wineshop.utils.api.UserApiPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private CustomJwtDecoder customJwtDecoder;

    private ChatWebSocketController controller;

    private SimpMessageHeaderAccessor headerAccessor;

    private MessageRequest messageRequest;
    private MessageResponse messageResponse;

    @BeforeEach
    void setUp() {
        controller = new ChatWebSocketController(chatService, messagingTemplate, customJwtDecoder);

        headerAccessor = SimpMessageHeaderAccessor.create();

        messageRequest = new MessageRequest("Hello Admin", "USER", 123L);

        messageResponse = new MessageResponse("ADMIN", "Hello Admin", LocalDateTime.now());

        when(chatService.saveMessage(any())).thenReturn(messageResponse);
    }

    @Test
    void handleMessage_shouldSaveAndSendMessage() {
        controller.handleMessage(headerAccessor, messageRequest);

        verify(chatService, times(1)).saveMessage(eq(messageRequest));
        verify(messagingTemplate, times(1))
                .convertAndSend(eq(UserApiPaths.Chat.WEBSOCKET_BROKER + messageRequest.userId()), eq(messageResponse));
        verify(messagingTemplate, times(1))
                .convertAndSend(eq(AdminApiPaths.Chat.WEBSOCKET_BROKER), eq(messageResponse));
    }

    @Test
    void handleMessage_whenServiceReturnsNull_shouldNotSendMessage() {
        when(chatService.saveMessage(any())).thenReturn(null);

        controller.handleMessage(headerAccessor, messageRequest);

        verify(messagingTemplate, never()).convertAndSend(any(String.class), any(MessageResponse.class));
    }

    @Test
    void handleMessage_whenRequestIsNull_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> controller.handleMessage(headerAccessor, null));
    }

    @Test
    void handleMessage_shouldReturnExpectedResponseContent() {
        when(chatService.saveMessage(any())).thenReturn(messageResponse);

        controller.handleMessage(headerAccessor, messageRequest);

        ArgumentCaptor<MessageResponse> captor = ArgumentCaptor.forClass(MessageResponse.class);
        verify(messagingTemplate, atLeastOnce()).convertAndSend(anyString(), captor.capture());

        MessageResponse actual = captor.getValue();
        assertEquals(messageResponse.content(), actual.content());
        assertEquals(messageResponse.senderRole(), actual.senderRole());
    }
}