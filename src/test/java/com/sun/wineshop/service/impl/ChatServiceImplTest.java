package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.request.MessageRequest;
import com.sun.wineshop.dto.response.MessageResponse;
import com.sun.wineshop.model.entity.Conversation;
import com.sun.wineshop.model.entity.Message;
import com.sun.wineshop.repository.ConversationRepository;
import com.sun.wineshop.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private ChatServiceImpl chatService;

    private MessageRequest messageRequest;
    private Conversation conversation;
    private Message message;

    @BeforeEach
    void setUp() {
        messageRequest = new MessageRequest("Hello world!", "USER", 1L);

        conversation = Conversation.builder()
                .id(10L)
                .userId(messageRequest.userId())
                .build();

        message = Message.builder()
                .id(100L)
                .senderRole(messageRequest.sender())
                .content(messageRequest.content())
                .timestamp(LocalDateTime.now())
                .conversation(conversation)
                .build();
    }

    @Test
    void saveMessage_whenConversationExists_shouldSaveMessage() {
        when(conversationRepository.findByUserId(messageRequest.userId()))
                .thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        MessageResponse response = chatService.saveMessage(messageRequest);

        verify(conversationRepository, never()).save(any(Conversation.class));
        verify(messageRepository).save(any(Message.class));

        assertEquals(messageRequest.sender(), response.senderRole());
        assertEquals(messageRequest.content(), response.content());
        assertNotNull(response.timestamp());
    }

    @Test
    void saveMessage_whenConversationDoesNotExist_shouldCreateConversationAndSaveMessage() {
        when(conversationRepository.findByUserId(messageRequest.userId()))
                .thenReturn(Optional.empty());
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        MessageResponse response = chatService.saveMessage(messageRequest);

        verify(conversationRepository).save(any(Conversation.class));
        verify(messageRepository).save(any(Message.class));

        assertEquals(messageRequest.sender(), response.senderRole());
        assertEquals(messageRequest.content(), response.content());
        assertNotNull(response.timestamp());
    }

    @Test
    void getMessageHistory_shouldReturnListOfMessageResponses() {
        List<Message> messages = List.of(
                Message.builder()
                        .senderRole("USER")
                        .content("Hi")
                        .timestamp(LocalDateTime.of(2025, 6, 3, 10, 0))
                        .conversation(conversation)
                        .build(),
                Message.builder()
                        .senderRole("ADMIN")
                        .content("Hello")
                        .timestamp(LocalDateTime.of(2025, 6, 3, 10, 1))
                        .conversation(conversation)
                        .build()
        );

        when(messageRepository.findAllByConversationUserIdOrderByTimestampAsc(messageRequest.userId()))
                .thenReturn(messages);

        List<MessageResponse> responses = chatService.getMessageHistory(messageRequest.userId());

        assertEquals(2, responses.size());
        assertEquals("USER", responses.get(0).senderRole());
        assertEquals("Hi", responses.get(0).content());
        assertEquals("ADMIN", responses.get(1).senderRole());
        assertEquals("Hello", responses.get(1).content());
    }
}
