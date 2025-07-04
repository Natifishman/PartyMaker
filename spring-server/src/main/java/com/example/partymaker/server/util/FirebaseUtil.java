package com.example.partymaker.server.util;

import com.example.partymaker.server.model.ServerResponse;
import com.example.partymaker.server.model.User;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.StorageClient;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class FirebaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseUtil.class);
    private FirebaseAuth firebaseAuth;
    private Storage storage;
    
    @Value("${firebase.storage.bucket:partymaker-app.appspot.com}")
    private String bucketName;
    
    @Value("${firebase.storage.enabled:false}")
    private boolean storageEnabled;
    
    @Value("${firebase.database.enabled:true}")
    private boolean databaseEnabled;
    
    @Value("${firebase.database.url:https://partymaker-9c966-default-rtdb.firebaseio.com}")
    private String databaseUrl;

    @PostConstruct
    public void initialize() {
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ClassPathResource("serviceAccountKey.json").getInputStream());

            FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                    .setCredentials(credentials);
            
            // Only set storage bucket if storage is enabled
            if (storageEnabled) {
                optionsBuilder.setStorageBucket(bucketName);
                logger.info("Firebase Storage bucket set to: {}", bucketName);
            }
            
            // Set database URL if database is enabled
            if (databaseEnabled) {
                optionsBuilder.setDatabaseUrl(databaseUrl);
                logger.info("Firebase Database URL set to: {}", databaseUrl);
            }
            
            FirebaseOptions options = optionsBuilder.build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                logger.info("Firebase App initialized with options: storage={}, database={}", 
                        storageEnabled, databaseEnabled);
            } else {
                logger.info("Using existing Firebase App instance");
            }

            firebaseAuth = FirebaseAuth.getInstance();
            logger.info("Firebase Auth initialized successfully");
            
            // Only initialize storage if enabled
            if (storageEnabled) {
                try {
                    storage = StorageClient.getInstance().bucket().getStorage();
                    logger.info("Firebase Storage initialized successfully");
                } catch (Exception e) {
                    logger.warn("Could not initialize Firebase Storage: {}", e.getMessage());
                    logger.warn("Storage operations will not be available");
                }
            } else {
                logger.info("Firebase Storage is disabled");
            }
        } catch (IOException e) {
            logger.error("Error initializing Firebase: {}", e.getMessage());
            throw new RuntimeException("Could not initialize Firebase", e);
        }
    }

    public ServerResponse<User> verifyIdToken(String token) {
        if (token == null || token.isEmpty()) {
            return new ServerResponse<>("No token provided", null);
        }

        try {
            String idToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            String uid = decodedToken.getUid();

            UserRecord userRecord = firebaseAuth.getUser(uid);
            User user = new User();
            user.setUid(userRecord.getUid());
            user.setEmail(userRecord.getEmail());
            user.setDisplayName(userRecord.getDisplayName());
            user.setPhotoUrl(userRecord.getPhotoUrl());

            return new ServerResponse<>("success", user);
        } catch (FirebaseAuthException e) {
            logger.error("Error verifying token: {}", e.getMessage());
            return new ServerResponse<>("Invalid token", null);
        }
    }

    public ServerResponse<String> uploadFile(MultipartFile file, String path) {
        if (!storageEnabled || storage == null) {
            logger.warn("Storage operations are disabled or not initialized");
            return new ServerResponse<>("Storage operations are disabled", null);
        }
        
        try {
            BlobId blobId = BlobId.of(bucketName, path);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            Blob blob = storage.create(blobInfo, file.getBytes());
            String downloadUrl = blob.signUrl(365, TimeUnit.DAYS).toString();

            logger.info("File uploaded successfully: {}", path);
            return new ServerResponse<>("success", downloadUrl);
        } catch (IOException e) {
            logger.error("Error uploading file: {}", e.getMessage());
            return new ServerResponse<>("Error uploading file: " + e.getMessage(), null);
        }
    }

    public ServerResponse<Void> deleteFile(String path) {
        if (!storageEnabled || storage == null) {
            logger.warn("Storage operations are disabled or not initialized");
            return new ServerResponse<>("Storage operations are disabled", null);
        }
        
        try {
            BlobId blobId = BlobId.of(bucketName, path);
            boolean deleted = storage.delete(blobId);

            if (deleted) {
                logger.info("File deleted successfully: {}", path);
                return new ServerResponse<>("success", null);
            } else {
                logger.warn("File not found for deletion: {}", path);
                return new ServerResponse<>("File not found", null);
            }
        } catch (Exception e) {
            logger.error("Error deleting file: {}", e.getMessage());
            return new ServerResponse<>("Error deleting file: " + e.getMessage(), null);
        }
    }

    public ServerResponse<byte[]> downloadFile(String path) {
        if (!storageEnabled || storage == null) {
            logger.warn("Storage operations are disabled or not initialized");
            return new ServerResponse<>("Storage operations are disabled", null);
        }
        
        try {
            Blob blob = storage.get(BlobId.of(bucketName, path));
            if (blob == null) {
                logger.warn("File not found: {}", path);
                return new ServerResponse<>("File not found", null);
            }

            byte[] content = blob.getContent();
            logger.info("File downloaded successfully: {}", path);
            return new ServerResponse<>("success", content);
        } catch (Exception e) {
            logger.error("Error downloading file: {}", e.getMessage());
            return new ServerResponse<>("Error downloading file: " + e.getMessage(), null);
        }
    }

    public ServerResponse<String> getDownloadUrl(String path) {
        if (!storageEnabled || storage == null) {
            logger.warn("Storage operations are disabled or not initialized");
            return new ServerResponse<>("Storage operations are disabled", null);
        }
        
        try {
            Blob blob = storage.get(BlobId.of(bucketName, path));
            if (blob == null) {
                logger.warn("File not found: {}", path);
                return new ServerResponse<>("File not found", null);
            }

            String url = blob.signUrl(1, TimeUnit.HOURS).toString();
            logger.info("Download URL generated for path: {}", path);
            return new ServerResponse<>("success", url);
        } catch (Exception e) {
            logger.error("Error getting download URL: {}", e.getMessage());
            return new ServerResponse<>("Error getting download URL: " + e.getMessage(), null);
        }
    }

    public ServerResponse<Boolean> checkFileExists(String path) {
        if (!storageEnabled || storage == null) {
            logger.warn("Storage operations are disabled or not initialized");
            return new ServerResponse<>("Storage operations are disabled", null);
        }
        
        try {
            Blob blob = storage.get(BlobId.of(bucketName, path));
            boolean exists = blob != null;
            logger.info("File exists check for path {}: {}", path, exists);
            return new ServerResponse<>("success", exists);
        } catch (Exception e) {
            logger.error("Error checking file existence: {}", e.getMessage());
            return new ServerResponse<>("Error checking file existence: " + e.getMessage(), null);
        }
    }

    public ServerResponse<List<String>> listFiles(String path) {
        if (!storageEnabled || storage == null) {
            logger.warn("Storage operations are disabled or not initialized");
            return new ServerResponse<>("Storage operations are disabled", null);
        }
        
        try {
            List<String> fileList = new ArrayList<>();
            storage.list(bucketName, Storage.BlobListOption.prefix(path))
                    .iterateAll()
                    .forEach(blob -> fileList.add(blob.getName()));

            logger.info("Listed {} files in path: {}", fileList.size(), path);
            return new ServerResponse<>("success", fileList);
        } catch (Exception e) {
            logger.error("Error listing files: {}", e.getMessage());
            return new ServerResponse<>("Error listing files: " + e.getMessage(), null);
        }
    }

    public ServerResponse<User> createUser(String email, String password) {
        try {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password);

            UserRecord userRecord = firebaseAuth.createUser(request);
            User user = new User();
            user.setUid(userRecord.getUid());
            user.setEmail(userRecord.getEmail());
            user.setDisplayName(userRecord.getDisplayName());
            user.setPhotoUrl(userRecord.getPhotoUrl());

            return new ServerResponse<>("success", user);
        } catch (FirebaseAuthException e) {
            logger.error("Error creating user: {}", e.getMessage());
            return new ServerResponse<>("Error creating user: " + e.getMessage(), null);
        }
    }

    public ServerResponse<User> updateUser(String uid, User updatedUser) {
        try {
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid)
                    .setEmail(updatedUser.getEmail())
                    .setDisplayName(updatedUser.getDisplayName())
                    .setPhotoUrl(updatedUser.getPhotoUrl());

            UserRecord userRecord = firebaseAuth.updateUser(request);
            User user = new User();
            user.setUid(userRecord.getUid());
            user.setEmail(userRecord.getEmail());
            user.setDisplayName(userRecord.getDisplayName());
            user.setPhotoUrl(userRecord.getPhotoUrl());

            return new ServerResponse<>("success", user);
        } catch (FirebaseAuthException e) {
            logger.error("Error updating user: {}", e.getMessage());
            return new ServerResponse<>("Error updating user: " + e.getMessage(), null);
        }
    }

    public ServerResponse<String> deleteUser(String uid) {
        try {
            firebaseAuth.deleteUser(uid);
            logger.info("User deleted successfully: {}", uid);
            return new ServerResponse<>("success", "User deleted successfully");
        } catch (FirebaseAuthException e) {
            logger.error("Error deleting user: {}", e.getMessage());
            return new ServerResponse<>("Error deleting user: " + e.getMessage(), null);
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

            return new ServerResponse<>("success", user);
        } catch (FirebaseAuthException e) {
            logger.error("Error getting user: {}", e.getMessage());
            return new ServerResponse<>("Error getting user: " + e.getMessage(), null);
        }
    }

    public ServerResponse<String> sendPasswordResetEmail(String email) {
        try {
            firebaseAuth.generatePasswordResetLink(email);
            logger.info("Password reset email sent to: {}", email);
            return new ServerResponse<>("success", "Password reset email sent");
        } catch (FirebaseAuthException e) {
            logger.error("Error sending password reset email: {}", e.getMessage());
            return new ServerResponse<>("Error sending password reset email: " + e.getMessage(), null);
        }
    }

    public ServerResponse<String> changePassword(String uid, String newPassword) {
        try {
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid)
                    .setPassword(newPassword);
            firebaseAuth.updateUser(request);
            logger.info("Password changed successfully for user: {}", uid);
            return new ServerResponse<>("success", "Password changed successfully");
        } catch (FirebaseAuthException e) {
            logger.error("Error changing password: {}", e.getMessage());
            return new ServerResponse<>("Error changing password: " + e.getMessage(), null);
        }
    }
}