package com.fana.realtimechat.controller;

import com.fana.realtimechat.dto.AuthRequest;
import com.fana.realtimechat.dto.AuthResponse;
import com.fana.realtimechat.dto.RegisterRequest;
import com.fana.realtimechat.model.User;
import com.fana.realtimechat.service.JwtService;
import com.fana.realtimechat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        UserDetails userDetails = userService.loadUserByUsername(request.getUsername());
        String token = jwtService.generateToken(userDetails);

        // Update user status to online
        userService.updateUserOnlineStatus(request.getUsername(), true);

        User user = userService.findByUsername(request.getUsername()).orElseThrow();

        return ResponseEntity.ok(new AuthResponse(token, user));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setDisplayName(request.getDisplayName());
        user.setProfilePicture(request.getProfilePicture());
        user.setOnline(true);

        User registeredUser = userService.registerUser(user);

        UserDetails userDetails = userService.loadUserByUsername(registeredUser.getUsername());
        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(token, registeredUser));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody String username) {
        userService.updateUserOnlineStatus(username, false);
        return ResponseEntity.ok().build();
    }
}
