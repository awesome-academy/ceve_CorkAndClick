package com.sun.wineshop.repository;

import com.sun.wineshop.model.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByUserId(Long userId);
}
