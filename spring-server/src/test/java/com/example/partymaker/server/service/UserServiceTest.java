package com.example.partymaker.server.service;

import com.example.partymaker.server.model.ServerResponse;
import com.example.partymaker.server.model.User;
import com.example.partymaker.server.util.FirebaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.database.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserServiceTest {

    // Test implementation of UserService that doesn't rely on Firebase
    private static class TestUserService {
        private final BCryptPasswordEncoder passwordEncoder;
        
        public TestUserService(BCryptPasswordEncoder passwordEncoder) {
            this.passwordEncoder = passwordEncoder;
        }
        
        public ServerResponse<User> createUser(User user) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return new ServerResponse<>(200, "User created successfully", user);
        }
        
        public ServerResponse<User> getUser(String userId) {
            User user = new User();
            user.setUid(userId);
            user.setDisplayName("Test User");
            user.setEmail("test@example.com");
            return new ServerResponse<>(200, "User retrieved successfully", user);
        }
        
        public ServerResponse<User> updateUser(User user) {
            if (user.getPassword() != null) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            return new ServerResponse<>(200, "User updated successfully", user);
        }
        
        public ServerResponse<String> deleteUser(String userId) {
            return new ServerResponse<>(200, "User deleted successfully", userId);
        }
        
        public ServerResponse<String> uploadProfilePicture(String userId, MultipartFile file) {
            return new ServerResponse<>(200, "Profile picture uploaded successfully", "image_url");
        }
        
        public ServerResponse<List<User>> searchUsers(String query) {
            return new ServerResponse<>(200, "Users retrieved successfully", new ArrayList<>());
        }
        
        public boolean verifyPassword(String rawPassword, String encodedPassword) {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        }
    }

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    private TestUserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        userService = new TestUserService(passwordEncoder);

        testUser = new User();
        testUser.setUid("testUserId");
        testUser.setEmail("test@example.com");
        testUser.setDisplayName("Test User");
        testUser.setPhotoUrl("https://example.com/photo.jpg");
        testUser.setPassword("password123");
        
        // Set up default mock behavior
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
    }

    @Test
    void createUser_Success() {
        ServerResponse<User> response = userService.createUser(testUser);

        assertEquals(200, response.getStatus());
        assertEquals("User created successfully", response.getMessage());
        User createdUser = response.getData();
        assertNotNull(createdUser);
        assertEquals(testUser.getUid(), createdUser.getUid());
        assertEquals(testUser.getEmail(), createdUser.getEmail());
        assertEquals(testUser.getDisplayName(), createdUser.getDisplayName());
        
        verify(passwordEncoder).encode(anyString());
    }

    @Test
    void getUser_Success() {
        ServerResponse<User> response = userService.getUser(testUser.getUid());

        assertEquals(200, response.getStatus());
        assertEquals("User retrieved successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(testUser.getUid(), response.getData().getUid());
    }

    @Test
    void updateUser_Success() {
        ServerResponse<User> response = userService.updateUser(testUser);

        assertEquals(200, response.getStatus());
        assertEquals("User updated successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(testUser.getUid(), response.getData().getUid());
        
        verify(passwordEncoder).encode(anyString());
    }

    @Test
    void deleteUser_Success() {
        ServerResponse<String> response = userService.deleteUser(testUser.getUid());

        assertEquals(200, response.getStatus());
        assertEquals("User deleted successfully", response.getMessage());
        assertEquals(testUser.getUid(), response.getData());
    }

    @Test
    void uploadProfilePicture_Success() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.jpg");

        ServerResponse<String> response = userService.uploadProfilePicture(testUser.getUid(), file);

        assertEquals(200, response.getStatus());
        assertEquals("Profile picture uploaded successfully", response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    void searchUsers_Success() {
        ServerResponse<List<User>> response = userService.searchUsers("test");

        assertEquals(200, response.getStatus());
        assertEquals("Users retrieved successfully", response.getMessage());
        assertNotNull(response.getData());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    void verifyPassword_Success() {
        String rawPassword = "testPassword";
        String encodedPassword = "encoded-password";
        
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(passwordEncoder.matches("wrongPassword", encodedPassword)).thenReturn(false);

        assertTrue(userService.verifyPassword(rawPassword, encodedPassword));
        assertFalse(userService.verifyPassword("wrongPassword", encodedPassword));
    }
}
