package com.fana.realtimechat.controller;

import com.fana.realtimechat.model.User;
import com.fana.realtimechat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        List<User> users = userService.getAllUsers().stream()
                            .filter(user -> !user.getUsername().equals(currentUsername))
                            .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        User user = userService.findByUsername(currentUsername).orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(user);
    }
}
