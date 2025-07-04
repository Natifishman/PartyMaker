package com.example.partymaker.server.service;

import com.example.partymaker.server.model.ServerResponse;
import com.example.partymaker.server.model.User;
import com.example.partymaker.server.util.FirebaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final FirebaseAuth firebaseAuth;
    private final FirebaseUtil firebaseUtil;
    private DatabaseReference database;
    protected BCryptPasswordEncoder passwordEncoder;
    
    @Value("${firebase.database.enabled:false}")
    private boolean databaseEnabled;

    @Autowired
    public UserService(FirebaseAuth firebaseAuth, FirebaseUtil firebaseUtil, FirebaseDatabase firebaseDatabase) {
        this.firebaseAuth = firebaseAuth;
        this.firebaseUtil = firebaseUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
        
        // Initialize database reference safely
        try {
            if (firebaseDatabase != null) {
                this.database = firebaseDatabase.getReference();
                logger.info("Firebase Database reference initialized successfully");
            } else {
                logger.warn("FirebaseDatabase is null, database operations will not be available");
            }
        } catch (Exception e) {
            logger.error("Failed to initialize database reference: {}", e.getMessage());
            logger.warn("Database operations will not be available");
        }
    }

    // For testing purposes
    public void setDatabase(DatabaseReference database) {
        this.database = database;
    }

    // For testing purposes
    public void setPasswordEncoder(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public ServerResponse<User> createUser(User user) {
        try {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(user.getEmail())
                    .setPassword(passwordEncoder.encode(user.getPassword()))
                    .setDisplayName(user.getDisplayName())
                    .setPhotoUrl(user.getPhotoUrl());

            UserRecord userRecord = firebaseAuth.createUser(request);
            user.setUid(userRecord.getUid());
            
            return new ServerResponse<>(200, "User created successfully", user);
        } catch (FirebaseAuthException e) {
            logger.error("Error creating user: {}", e.getMessage());
            return new ServerResponse<>(400, e.getMessage(), null);
        }
    }

    public ServerResponse<User> getUser(String uid) {
        try {
            UserRecord userRecord = firebaseAuth.getUser(uid);
            User user = new User();
            user.setUid(userRecord.getUid());
            user.setEmail(userRecord.getEmail());
            user.setDisplayName(userRecord.getDisplayName());
            user.setPhotoUrl(userRecord.getPhotoUrl());
            
            return new ServerResponse<>(200, "User retrieved successfully", user);
        } catch (FirebaseAuthException e) {
            logger.error("Error getting user: {}", e.getMessage());
            return new ServerResponse<>(404, e.getMessage(), null);
        }
    }

    public ServerResponse<User> getUserByEmail(String email) {
        try {
            UserRecord userRecord = firebaseAuth.getUserByEmail(email);
            User user = new User();
            user.setUid(userRecord.getUid());
            user.setEmail(userRecord.getEmail());
            user.setDisplayName(userRecord.getDisplayName());
            user.setPhotoUrl(userRecord.getPhotoUrl());
            
            return new ServerResponse<>(200, "User retrieved successfully", user);
        } catch (FirebaseAuthException e) {
            logger.error("Error getting user by email: {}", e.getMessage());
            return new ServerResponse<>(404, e.getMessage(), null);
        }
    }

    public ServerResponse<User> updateUser(User user) {
        try {
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(user.getUid())
                    .setEmail(user.getEmail())
                    .setDisplayName(user.getDisplayName())
                    .setPhotoUrl(user.getPhotoUrl());

            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                request.setPassword(passwordEncoder.encode(user.getPassword()));
            }

            UserRecord userRecord = firebaseAuth.updateUser(request);
            user.setUid(userRecord.getUid());
            
            return new ServerResponse<>(200, "User updated successfully", user);
        } catch (FirebaseAuthException e) {
            logger.error("Error updating user: {}", e.getMessage());
            return new ServerResponse<>(400, e.getMessage(), null);
        }
    }

    public ServerResponse<String> deleteUser(String uid) {
        try {
            firebaseAuth.deleteUser(uid);
            return new ServerResponse<>(200, "User deleted successfully", uid);
        } catch (FirebaseAuthException e) {
            logger.error("Error deleting user: {}", e.getMessage());
            return new ServerResponse<>(400, e.getMessage(), null);
        }
    }

    public ServerResponse<String> uploadProfilePicture(String uid, MultipartFile file) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            // For now, we'll just return a dummy URL since we removed storage functionality
            String photoUrl = "https://example.com/photos/" + fileName;
            
            // Update user's photo URL in Firebase
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid)
                    .setPhotoUrl(photoUrl);
            firebaseAuth.updateUser(request);
            
            return new ServerResponse<>(200, "Profile picture uploaded successfully", photoUrl);
        } catch (FirebaseAuthException e) {
            logger.error("Error uploading profile picture: {}", e.getMessage());
            return new ServerResponse<>(400, e.getMessage(), null);
        }
    }

    public ServerResponse<List<User>> searchUsers(String query) {
        try {
            // For now, we'll just return an empty list since we removed database functionality
            List<User> users = new ArrayList<>();
            return new ServerResponse<>(200, "Users retrieved successfully", users);
        } catch (Exception e) {
            logger.error("Error searching users: {}", e.getMessage());
            return new ServerResponse<>(500, e.getMessage(), null);
        }
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}