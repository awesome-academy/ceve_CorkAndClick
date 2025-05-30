package com.sun.wineshop.repository;

import com.sun.wineshop.model.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByConversationUserIdOrderByTimestampAsc(Long conversationId);
}
