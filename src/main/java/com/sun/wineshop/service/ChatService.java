package com.sun.wineshop.service;

import com.sun.wineshop.dto.request.MessageRequest;
import com.sun.wineshop.dto.response.MessageResponse;

import java.util.List;

public interface ChatService {
    MessageResponse saveMessage(MessageRequest messageRequest);
    List<MessageResponse> getMessageHistory(Long userId);
}
