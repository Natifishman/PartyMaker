package com.example.partymaker.server.controller;

import com.example.partymaker.server.controller.UserController;
import com.example.partymaker.server.model.ServerResponse;
import com.example.partymaker.server.model.User;
import com.example.partymaker.server.service.UserService;
import com.example.partymaker.server.util.FirebaseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private FirebaseUtil firebaseUtil;

    @InjectMocks
    private UserController userController;

    private static final String TEST_TOKEN = "testToken";
    private static final String TEST_USER_ID = "testUserId";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void getUser_Success() throws Exception {
        User user = createTestUser();
        ServerResponse<User> verifyResponse = new ServerResponse<>(200, "Success", user);
        when(firebaseUtil.verifyIdToken(TEST_TOKEN)).thenReturn(verifyResponse);

        ServerResponse<User> response = new ServerResponse<>(200, "User retrieved successfully", user);
        when(userService.getUser(TEST_USER_ID)).thenReturn(response);

        mockMvc.perform(get("/api/users/{id}", TEST_USER_ID)
                .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("User retrieved successfully"));
    }

    @Test
    void updateUser_Success() throws Exception {
        User user = createTestUser();
        ServerResponse<User> verifyResponse = new ServerResponse<>(200, "Success", user);
        when(firebaseUtil.verifyIdToken(TEST_TOKEN)).thenReturn(verifyResponse);

        ServerResponse<User> response = new ServerResponse<>(200, "User updated successfully", user);
        when(userService.updateUser(any(User.class))).thenReturn(response);

        mockMvc.perform(put("/api/users/{id}", TEST_USER_ID)
                .header("Authorization", TEST_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"uid\":\"" + TEST_USER_ID + "\",\"displayName\":\"Updated Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("User updated successfully"));
    }

    @Test
    void deleteUser_Success() throws Exception {
        User user = createTestUser();
        ServerResponse<User> verifyResponse = new ServerResponse<>(200, "Success", user);
        when(firebaseUtil.verifyIdToken(TEST_TOKEN)).thenReturn(verifyResponse);

        ServerResponse<String> response = new ServerResponse<>(200, "User deleted successfully", null);
        when(userService.deleteUser(TEST_USER_ID)).thenReturn(response);

        mockMvc.perform(delete("/api/users/{id}", TEST_USER_ID)
                .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }

    @Test
    void uploadProfilePicture_Success() throws Exception {
        User user = createTestUser();
        ServerResponse<User> verifyResponse = new ServerResponse<>(200, "Success", user);
        when(firebaseUtil.verifyIdToken(TEST_TOKEN)).thenReturn(verifyResponse);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        ServerResponse<String> response = new ServerResponse<>(200, "Profile picture uploaded successfully", "image_url");
        when(userService.uploadProfilePicture(eq(TEST_USER_ID), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/users/{id}/profile-picture", TEST_USER_ID)
                .file(file)
                .header("Authorization", TEST_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Profile picture uploaded successfully"));
    }

    private User createTestUser() {
        User user = new User();
        user.setUid(TEST_USER_ID);
        user.setDisplayName("Test User");
        user.setEmail("test@example.com");
        return user;
    }
}