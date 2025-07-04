package com.example.partymaker.server.config;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
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
    private String bucketName;
    
    private Storage storage;
    
    @Bean
    public Storage storage() {
        if (storage == null) {
            try {
                storage = StorageOptions.getDefaultInstance().getService();
                logger.info("Firebase Storage initialized successfully");
            } catch (Exception e) {
                logger.error("Failed to initialize Firebase Storage", e);
                return null;
            }
        }
        return storage;
    }
    
    public String getBucketName() {
        return bucketName;
    }
    
    public Storage getStorage() {
        if (storage == null) {
            storage = storage();
        }
        return storage;
    }
} 