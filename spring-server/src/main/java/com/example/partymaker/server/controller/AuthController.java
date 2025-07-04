package com.example.partymaker.server.controller;

import com.example.partymaker.server.model.ServerResponse;
import com.example.partymaker.server.model.User;
import com.example.partymaker.server.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication controller
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthService authService;
    
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * Sign in with email and password
     */
    @PostMapping("/signin")
    public ResponseEntity<ServerResponse<User>> signIn(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        logger.info("Received sign in request for email: {}", email);
        return ResponseEntity.ok(authService.signIn(email, password));
    }
    
    /**
     * Sign up with email and password
     */
    @PostMapping("/signup")
    public ResponseEntity<ServerResponse<User>> signUp(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        logger.info("Received sign up request for email: {}", email);
        return ResponseEntity.ok(authService.signUp(email, password));
    }
    
    /**
     * Sign out the current user
     */
    @PostMapping("/signout")
    public ResponseEntity<ServerResponse<String>> signOut(@RequestHeader("Authorization") String token) {
        logger.info("Received sign out request");
        return ResponseEntity.ok(authService.signOut(token));
    }
    
    /**
     * Verify the authentication token
     */
    @PostMapping("/verify")
    public ResponseEntity<ServerResponse<User>> verifyToken(@RequestHeader("Authorization") String token) {
        logger.info("Received token verification request");
        return ResponseEntity.ok(authService.verifyToken(token));
    }
    
    /**
     * Change user password
     */
    @PostMapping("/password/change")
    public ResponseEntity<ServerResponse<String>> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> passwords) {
        String oldPassword = passwords.get("oldPassword");
        String newPassword = passwords.get("newPassword");
        logger.info("Received password change request");
        return ResponseEntity.ok(authService.changePassword(token, oldPassword, newPassword));
    }
} 