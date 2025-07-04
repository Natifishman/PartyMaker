package com.example.partymaker.server.controller;

import com.example.partymaker.server.model.ServerResponse;
import com.example.partymaker.server.model.User;
import com.example.partymaker.server.service.DatabaseService;
import com.example.partymaker.server.util.FirebaseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class DatabaseControllerTest {

    @Mock
    private DatabaseService databaseService;

    @Mock
    private FirebaseUtil firebaseUtil;

    @InjectMocks
    private DatabaseController databaseController;

    private static final String TEST_TOKEN = "testToken";
    private static final String TEST_USER_ID = "testUserId";
    private static final String TEST_PATH = "test/path";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Mock Firebase token verification
        User user = createTestUser();
        ServerResponse<User> verifyResponse = new ServerResponse<>(200, "Success", user);
        when(firebaseUtil.verifyIdToken(TEST_TOKEN)).thenReturn(verifyResponse);
    }

    @Test
    void getData_Success() {
        // Arrange
        ServerResponse<Object> expectedResponse = new ServerResponse<>(200, "Data retrieved successfully", "{\"key\":\"value\"}");
        when(databaseService.getData(TEST_PATH)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ServerResponse<Object>> response = databaseController.getData(TEST_PATH);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Data retrieved successfully", response.getBody().getMessage());
    }

    @Test
    void setData_Success() {
        // Arrange
        Object data = new Object();
        ServerResponse<Object> expectedResponse = new ServerResponse<>(200, "Data set successfully", null);
        when(databaseService.setData(eq(TEST_PATH), any())).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ServerResponse<Object>> response = databaseController.setData(TEST_PATH, data);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Data set successfully", response.getBody().getMessage());
    }

    @Test
    void updateData_Success() {
        // Arrange
        Object data = new Object();
        ServerResponse<Object> expectedResponse = new ServerResponse<>(200, "Data updated successfully", null);
        when(databaseService.updateData(eq(TEST_PATH), any())).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ServerResponse<Object>> response = databaseController.updateData(TEST_PATH, data);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Data updated successfully", response.getBody().getMessage());
    }

    @Test
    void deleteData_Success() {
        // Arrange
        ServerResponse<Object> expectedResponse = new ServerResponse<>(200, "Data deleted successfully", null);
        when(databaseService.deleteData(TEST_PATH)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ServerResponse<Object>> response = databaseController.deleteData(TEST_PATH);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("Data deleted successfully", response.getBody().getMessage());
    }

    private User createTestUser() {
        User user = new User();
        user.setUid(TEST_USER_ID);
        user.setDisplayName("Test User");
        user.setEmail("test@example.com");
        return user;
    }
} 