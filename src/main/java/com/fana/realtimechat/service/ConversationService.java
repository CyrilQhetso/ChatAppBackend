package com.fana.realtimechat.service;

import com.fana.realtimechat.model.Conversation;
import com.fana.realtimechat.model.User;
import com.fana.realtimechat.repository.ConversationRepository;
import com.fana.realtimechat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ConversationService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Conversation> getUserConversations(User user) {
        return conversationRepository.findByParticipantsContaining(user);
    }

    @Transactional
    public Conversation createGroupConversation(String name, Set<User> participants) {
        Conversation conversation = new Conversation();
        conversation.setName(name);
        conversation.setGroupChat(true);
        conversation.setParticipants(participants);

        Conversation savedConversation = conversationRepository.save(conversation);

        // Update users conversations
        for (User user: participants) {
            user.getConversations().add(savedConversation);
            userRepository.save(user);
        }

        return savedConversation;
    }

    @Transactional
    public Conversation getOrCreatePrivateConversation(User userA, User userB) {
        return conversationRepository.findPrivateConversation(userA, userB)
                .orElseGet(() -> {
                    Conversation conversation = new Conversation();
                    conversation.setGroupChat(false);
                    conversation.getParticipants().add(userA);
                    conversation.getParticipants().add(userB);

                    Conversation savedConversation = conversationRepository.save(conversation);

                    userA.getConversations().add(savedConversation);
                    userB.getConversations().add(savedConversation);
                    userRepository.save(userA);
                    userRepository.save(userB);

                    return savedConversation;
                });
    }

    public Optional<Conversation> findById(Long id) {
        return conversationRepository.findById(id);
    }
}
