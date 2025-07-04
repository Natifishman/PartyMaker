package com.example.partymaker.server.service;

import com.example.partymaker.server.model.ServerResponse;
import com.example.partymaker.server.model.User;
import com.example.partymaker.server.util.FirebaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class StorageService {
    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);
    private final FirebaseUtil firebaseUtil;

    public StorageService(FirebaseUtil firebaseUtil) {
        this.firebaseUtil = firebaseUtil;
    }

    public ServerResponse<String> uploadFile(MultipartFile file, String path, String token) {
        logger.info("Mock uploading file to path: {}", path);
        ServerResponse<User> userResponse = firebaseUtil.verifyIdToken(token);
        if (!"success".equals(userResponse.getMessage())) {
            return new ServerResponse<>(userResponse.getMessage(), null);
        }
        
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String mockUrl = "https://example.com/files/" + fileName;
        return new ServerResponse<>("success", mockUrl);
    }

    public ServerResponse<Void> deleteFile(String path, String token) {
        logger.info("Mock deleting file at path: {}", path);
        ServerResponse<User> userResponse = firebaseUtil.verifyIdToken(token);
        if (!"success".equals(userResponse.getMessage())) {
            return new ServerResponse<>(userResponse.getMessage(), null);
        }
        return new ServerResponse<>("success", null);
    }

    public ServerResponse<byte[]> downloadFile(String path, String token) {
        logger.info("Mock downloading file from path: {}", path);
        ServerResponse<User> userResponse = firebaseUtil.verifyIdToken(token);
        if (!"success".equals(userResponse.getMessage())) {
            return new ServerResponse<>(userResponse.getMessage(), null);
        }
        return new ServerResponse<>("success", "Mock file content".getBytes());
    }

    public ServerResponse<String> getDownloadUrl(String path, String token) {
        logger.info("Mock getting download URL for path: {}", path);
        ServerResponse<User> userResponse = firebaseUtil.verifyIdToken(token);
        if (!"success".equals(userResponse.getMessage())) {
            return new ServerResponse<>(userResponse.getMessage(), null);
        }
        return new ServerResponse<>("success", "https://example.com/files/" + path);
    }

    public ServerResponse<Boolean> checkFileExists(String path, String token) {
        logger.info("Mock checking if file exists at path: {}", path);
        ServerResponse<User> userResponse = firebaseUtil.verifyIdToken(token);
        if (!"success".equals(userResponse.getMessage())) {
            return new ServerResponse<>(userResponse.getMessage(), null);
        }
        return new ServerResponse<>("success", true);
    }

    public ServerResponse<List<String>> listFiles(String path, String token) {
        logger.info("Mock listing files in path: {}", path);
        ServerResponse<User> userResponse = firebaseUtil.verifyIdToken(token);
        if (!"success".equals(userResponse.getMessage())) {
            return new ServerResponse<>(userResponse.getMessage(), null);
        }
        return new ServerResponse<>("success", new ArrayList<>());
    }
}