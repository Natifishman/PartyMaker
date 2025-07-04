package com.example.partymaker.server.service;

import com.example.partymaker.server.model.ServerResponse;
import com.example.partymaker.server.model.User;
import com.example.partymaker.server.util.FirebaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Authentication service
 */
@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
    
    private final FirebaseAuth firebaseAuth;
    private final FirebaseUtil firebaseUtil;
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    public AuthService(FirebaseAuth firebaseAuth, FirebaseUtil firebaseUtil, UserService userService) {
        this.firebaseAuth = firebaseAuth;
        this.firebaseUtil = firebaseUtil;
        this.userService = userService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }
    
    public ServerResponse<User> signIn(String email, String password) {
        logger.info("Signing in user: {}", email);
        try {
            // First verify with Firebase
            UserRecord userRecord = firebaseAuth.getUserByEmail(email);
            
            // Then check our password
            ServerResponse<User> userResponse = userService.getUserByEmail(email);
            if (!"success".equals(userResponse.getMessage()) || userResponse.getData() == null) {
                return new ServerResponse<>("User not found", null);
            }
            
            User user = userResponse.getData();
            if (!passwordEncoder.matches(password, user.getPassword())) {
                return new ServerResponse<>("Invalid password", null);
            }
            
            // Don't send password back to client
            user.setPassword(null);
            return new ServerResponse<>("success", user);
        } catch (Exception e) {
            logger.error("Error signing in user: {}", e.getMessage());
            return new ServerResponse<>("Error signing in user", null);
        }
    }

    public ServerResponse<User> signUp(String email, String password) {
        logger.info("Creating new user: {}", email);
        if (!isValidPassword(password)) {
            return new ServerResponse<>("Invalid password format", null);
        }
        
        try {
            // Create user in Firebase
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(password);
            UserRecord userRecord = firebaseAuth.createUser(request);
            
            // Create user in our database with hashed password
            User user = new User();
            user.setUid(userRecord.getUid());
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setDisplayName(userRecord.getDisplayName());
            user.setPhotoUrl(userRecord.getPhotoUrl());
            
            ServerResponse<User> response = userService.createUser(user);
            if ("success".equals(response.getMessage()) && response.getData() != null) {
                response.getData().setPassword(null); // Don't send password back to client
            }
            return response;
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage());
            return new ServerResponse<>("Error creating user", null);
        }
    }
    
    public ServerResponse<String> signOut(String token) {
        logger.info("Signing out user");
        try {
            ServerResponse<User> authResponse = firebaseUtil.verifyIdToken(token);
            if (!"success".equals(authResponse.getMessage()) || authResponse.getData() == null) {
                return new ServerResponse<>("Invalid token", null);
            }
            
            String uid = authResponse.getData().getUid();
            firebaseAuth.revokeRefreshTokens(uid);
            return new ServerResponse<>("success", "User signed out successfully");
        } catch (Exception e) {
            logger.error("Error signing out user: {}", e.getMessage());
            return new ServerResponse<>("Error signing out user", null);
        }
    }
    
    public ServerResponse<User> verifyToken(String token) {
        logger.info("Verifying token");
        try {
            return firebaseUtil.verifyIdToken(token);
        } catch (Exception e) {
            logger.error("Error verifying token: {}", e.getMessage());
            return new ServerResponse<>("Invalid token", null);
        }
    }
    
    public ServerResponse<String> changePassword(String token, String oldPassword, String newPassword) {
        logger.info("Changing password");
        if (!isValidPassword(newPassword)) {
            return new ServerResponse<>("Invalid password format", null);
        }
        
        try {
            ServerResponse<User> authResponse = firebaseUtil.verifyIdToken(token);
            if (!"success".equals(authResponse.getMessage()) || authResponse.getData() == null) {
                return new ServerResponse<>("Invalid token", null);
            }
            
            User user = authResponse.getData();
            ServerResponse<User> userResponse = userService.getUser(user.getUid());
            if (!"success".equals(userResponse.getMessage()) || userResponse.getData() == null) {
                return new ServerResponse<>("User not found", null);
            }
            
            User storedUser = userResponse.getData();
            if (!passwordEncoder.matches(oldPassword, storedUser.getPassword())) {
                return new ServerResponse<>("Invalid old password", null);
            }
            
            // Update password in Firebase
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(user.getUid())
                .setPassword(newPassword);
            firebaseAuth.updateUser(request);
            
            // Update password in our database
            storedUser.setPassword(passwordEncoder.encode(newPassword));
            userService.updateUser(storedUser);
            
            return new ServerResponse<>("success", "Password changed successfully");
        } catch (Exception e) {
            logger.error("Error changing password: {}", e.getMessage());
            return new ServerResponse<>("Error changing password", null);
        }
    }
    
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }
} 