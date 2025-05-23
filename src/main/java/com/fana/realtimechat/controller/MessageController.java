package com.fana.realtimechat.controller;

import com.fana.realtimechat.dto.MessageRequest;
import com.fana.realtimechat.model.Conversation;
import com.fana.realtimechat.model.Message;
import com.fana.realtimechat.model.User;
import com.fana.realtimechat.service.ConversationService;
import com.fana.realtimechat.service.MessageService;
import com.fana.realtimechat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private ConversationService conversationService;

    @GetMapping("/api/conversations/{conversationId}/messages")
    public ResponseEntity<List<Message>> getConversationMessages(@PathVariable Long conversationId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        User currentUser = userService.findByUsername(currentUsername).orElseThrow(() -> new RuntimeException("User not found"));

        Conversation conversation = conversationService.findById(conversationId).orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Check id the current user is a participant
        if (!conversation.getParticipants().contains(currentUser)) {
            return ResponseEntity.status(403).build();
        }

        // Mark message as read
        messageService.markMessagesAsRead(conversation, currentUser);

        List<Message> messages = messageService.getConversationMessages(conversation);
        return ResponseEntity.ok(messages);
    }

    @MessageMapping("/chat/{conversationId}")
    public void processMessage(@DestinationVariable Long conversationId, MessageRequest messageRequest) {
        User sender = userService.findByUsername(messageRequest.getSenderUsername()).orElseThrow(() -> new RuntimeException("Sender not found"));

        Conversation conversation = conversationService.findById(conversationId).orElseThrow(() -> new RuntimeException("Conversation not found"));

        Message message = new Message();
        message.setSender(sender);
        message.setConversation(conversation);
        message.setContent(messageRequest.getContent());
        message.setRead(false);

        messageService.saveAndSendMessage(message);
    }

    @PostMapping("/api/conversations/{conversationId}/read")
    public ResponseEntity<?> markConversationAsRead(@PathVariable Long conversationIs) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        User currentUser = userService.findByUsername(currentUsername).orElseThrow(() -> new RuntimeException("User not found"));

        Conversation conversation = conversationService.findById(conversationIs).orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Check if the current user is a participant
        if (!conversation.getParticipants().contains(currentUser)) {
            return ResponseEntity.status(403).build();
        }

        messageService.markMessagesAsRead(conversation, currentUser);
        return ResponseEntity.ok().build();
    }
}
