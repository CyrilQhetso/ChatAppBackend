package com.fana.realtimechat.controller;

import com.fana.realtimechat.dto.CreateGroupRequest;
import com.fana.realtimechat.dto.PrivateConversationRequest;
import com.fana.realtimechat.model.Conversation;
import com.fana.realtimechat.model.User;
import com.fana.realtimechat.service.ConversationService;
import com.fana.realtimechat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<Conversation>> getUseronversation() {
        Authentication auth =  SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        User currentUser = userService.findByUsername(currentUsername).orElseThrow(() -> new RuntimeException("User not found"));

        List<Conversation> conversations = conversationService.getUserConversations(currentUser);
        return ResponseEntity.ok(conversations);
    }

    @PostMapping("/private")
    public ResponseEntity<Conversation> createOrGetPrivateConversation(@RequestBody PrivateConversationRequest request) {
        Authentication auth =  SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        User currentUser = userService.findByUsername(currentUsername).orElseThrow(() -> new RuntimeException("User not found"));

        User otherUser = userService.findByUsername(request.getOtherUsername()).orElseThrow(() -> new RuntimeException("Other user not found"));

        Conversation conversation = conversationService.getOrCreatePrivateConversation(currentUser, otherUser);
        return ResponseEntity.ok(conversation);
    }

    @PostMapping("/group")
    public ResponseEntity<Conversation> createGroupConversation(@RequestBody CreateGroupRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        User currentUser = userService.findByUsername(currentUsername).orElseThrow(() -> new RuntimeException("User not found"));

        Set<User> participants = new HashSet<>();
        participants.add(currentUser);

        for (String username: request.getParticipantUsernames()) {
            userService.findByUsername(username).ifPresent(participants::add);
        }

        Conversation groupConversation = conversationService.createGroupConversation(request.getName(), participants);
        return ResponseEntity.ok(groupConversation);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Conversation> getConversationById(@RequestBody Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        User currentUser = userService.findByUsername(currentUsername).orElseThrow(() -> new RuntimeException("User not found"));

        Conversation conversation = conversationService.findById(id).orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Check if the current user is a participant
        if (!conversation.getParticipants().contains(currentUser)) {
            return ResponseEntity.status(403).build(); // forbidden
        }

        return ResponseEntity.ok(conversation);
    }
}
