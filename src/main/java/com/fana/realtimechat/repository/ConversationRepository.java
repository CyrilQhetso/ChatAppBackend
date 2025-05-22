package com.fana.realtimechat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fana.realtimechat.model.Conversation;
import com.fana.realtimechat.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByParticipantsContaining(User user);

    @Query("SELECT c FROM Conversation c WHERE c.isGroupChat = false AND :userA MEMBER OF c.participants AND :userB MEMBER OF c.participants")
    Optional<Conversation> findPrivateConversation(User userA, User userB);
}
