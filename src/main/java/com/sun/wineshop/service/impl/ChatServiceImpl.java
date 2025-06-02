package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.request.MessageRequest;
import com.sun.wineshop.dto.response.MessageResponse;
import com.sun.wineshop.mapper.ToDtoMappers;
import com.sun.wineshop.model.entity.Conversation;
import com.sun.wineshop.model.entity.Message;
import com.sun.wineshop.repository.ConversationRepository;
import com.sun.wineshop.repository.MessageRepository;
import com.sun.wineshop.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Override
    public MessageResponse saveMessage(MessageRequest messageRequest) {
        Conversation conversation = conversationRepository.findByUserId(messageRequest.userId())
                .orElseGet(() -> conversationRepository.save(
                        Conversation.builder().userId(messageRequest.userId()).build()
                ));

        Message message = Message.builder()
                .senderRole(messageRequest.sender())
                .content(messageRequest.content())
                .timestamp(LocalDateTime.now())
                .conversation(conversation)
                .build();

        messageRepository.save(message);

        return ToDtoMappers.toMessageResponse(message);
    }

    @Override
    public List<MessageResponse> getMessageHistory(Long userId) {

        return messageRepository.findAllByConversationUserIdOrderByTimestampAsc(userId).stream()
                .map(m -> new MessageResponse(m.getSenderRole(), m.getContent(), m.getTimestamp()))
                .toList();
    }
}
