package com.fana.realtimechat.service;

import com.fana.realtimechat.model.Conversation;
import com.fana.realtimechat.model.Message;
import com.fana.realtimechat.model.User;
import com.fana.realtimechat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SimpMessagingTemplate messagingTamplate;

    public List<Message> getConversationMessages(Conversation conversation) {
        return messageRepository.findByConversationOrderByTimestampsAsc(conversation);
    }

    @Transactional
    public Message saveAndSendMessage(Message message) {
        message.setTimestamp(LocalDateTime.now());
        Message savedMessage = messageRepository.save(message);

        // Send to conversation topic
        messagingTamplate.convertAndSend(
                "/topic/conversation" + message.getConversation().getId(), savedMessage
        );

        // Send notification to each user in the conversation
        message.getConversation().getParticipants().forEach(participant -> {
            if (!participant.equals(message.getSender())) {
                messagingTamplate.convertAndSendToUser(participant.getUsername(), "/queue/messages", savedMessage);
            }
        });

        return savedMessage;
    }

    @Transactional
    public void markMessagesAsRead(Conversation conversation, User user) {
        List<Message> unreadMessages = messageRepository.findByConversationOrderByTimestampsAsc(conversation);
        unreadMessages.forEach(message -> {
            if (!message.getSender().equals(user) && !message.isRead()) {
                message.setRead(true);
                messageRepository.save(message);
            }
        });
    }

    public long getUnreadMessageCount(Conversation conversation) {
        return messageRepository.countByConversationAndReadFalse(conversation);
    }
}
