package com.university.knowledge.repository;

import com.university.knowledge.entity.ConversationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationMessageRepository
        extends JpaRepository<ConversationMessage, Long> {

    List<ConversationMessage> findAllByOrderByCreatedAtAsc();
}
