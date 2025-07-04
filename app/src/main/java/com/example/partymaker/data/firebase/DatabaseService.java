package com.example.partymaker.data.firebase;

import com.example.partymaker.data.model.ServerResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Interface defining database operations that should be routed through the Spring Boot server.
 * This ensures that the Android application does not directly access Firebase Database.
 */
public interface DatabaseService {
    
    /**
     * Get value from the database at the specified path
     * 
     * @param path Database path
     * @return CompletableFuture with server response containing the value
     */
    CompletableFuture<ServerResponse<Object>> getValue(String path);
    
    /**
     * Get children of the database at the specified path
     * 
     * @param path Database path
     * @return CompletableFuture with server response containing the children
     */
    CompletableFuture<ServerResponse<Object>> getChildren(String path);
    
    /**
     * Set value in the database at the specified path
     * 
     * @param path Database path
     * @param value Value to set
     * @return CompletableFuture with server response
     */
    CompletableFuture<ServerResponse<Object>> setValue(String path, Object value);
    
    /**
     * Update children in the database at the specified path
     * 
     * @param path Database path
     * @param updates Map of updates to apply
     * @return CompletableFuture with server response
     */
    CompletableFuture<ServerResponse<Object>> updateChildren(String path, Map<String, Object> updates);
    
    /**
     * Remove value from the database at the specified path
     * 
     * @param path Database path
     * @return CompletableFuture with server response
     */
    CompletableFuture<ServerResponse<Object>> removeValue(String path);
} 