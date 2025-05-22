package com.fana.realtimechat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fana.realtimechat.model.Conversation;
import com.fana.realtimechat.model.Message;
import com.fana.realtimechat.model.User;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationOrderByTimestampsAsc(Conversation conversation);
    List<Message> findBySenderAndReadFalse(User recipient);
    Long countByConversationAndReadFalse(Conversation conversation);
}
