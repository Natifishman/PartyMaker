package com.example.partymaker.server.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * תצורת Firebase עבור שרת PartyMaker
 */
@Configuration
public class FirebaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);
    
    @Value("${firebase.database.url}")
    private String databaseUrl;
    
    @Value("${firebase.storage.bucket}")
    private String storageBucket;
    
    @Bean
    public FirebaseApp firebaseApp() {
        if (FirebaseApp.getApps().isEmpty()) {
            logger.info("Initializing Firebase application");
            
            try (InputStream serviceAccount = new ClassPathResource("serviceAccountKey.json").getInputStream()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setDatabaseUrl(databaseUrl)
                        .setStorageBucket(storageBucket)
                        .build();
                
                return FirebaseApp.initializeApp(options);
            } catch (Exception e) {
                logger.error("Error initializing Firebase", e);
                // במקרה של כישלון, נחזיר null ונטפל בזה בשירותים
                return null;
            }
        } else {
            logger.info("Firebase application already initialized");
            return FirebaseApp.getInstance();
        }
    }
    
    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        if (firebaseApp != null) {
            return FirebaseAuth.getInstance(firebaseApp);
        }
        logger.warn("Firebase app is null, returning null for FirebaseAuth");
        return null;
    }
    
    @Bean
    public FirebaseDatabase firebaseDatabase(FirebaseApp firebaseApp) {
        if (firebaseApp != null) {
            return FirebaseDatabase.getInstance(firebaseApp);
        }
        logger.warn("Firebase app is null, returning null for FirebaseDatabase");
        return null;
    }
} 