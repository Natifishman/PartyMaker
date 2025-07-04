package com.example.partymaker.server.config;

import com.google.cloud.storage.Storage;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * תצורת Firebase Storage עבור שרת PartyMaker
 */
@Configuration
public class StorageConfig {
    private static final Logger logger = LoggerFactory.getLogger(StorageConfig.class);
    
    @Value("${firebase.storage.bucket}")
    private String storageBucket;
    
    @Bean
    public StorageClient storageClient(FirebaseApp firebaseApp) {
        if (firebaseApp != null) {
            try {
                return StorageClient.getInstance(firebaseApp);
            } catch (Exception e) {
                logger.error("Error creating StorageClient", e);
                return null;
            }
        }
        logger.warn("Firebase app is null, returning null for StorageClient");
        return null;
    }
    
    @Bean
    public Storage storage(StorageClient storageClient) {
        if (storageClient != null) {
            try {
                // מנקה רווחים מיותרים משם הבאקט
                String bucketName = storageBucket.trim();
                logger.info("Attempting to get storage bucket: '{}'", bucketName);
                
                try {
                    // נסה לקבל את הבאקט ברירת המחדל
                    logger.info("Trying default bucket");
                    return storageClient.bucket().getStorage();
                } catch (Exception e) {
                    logger.warn("Failed to get default bucket, trying with specified bucket name");
                    return storageClient.bucket(bucketName).getStorage();
                }
            } catch (Exception e) {
                logger.error("Error getting storage from bucket", e);
            }
        }
        logger.warn("StorageClient is null or error occurred, returning null for Storage");
        return null;
    }
} 