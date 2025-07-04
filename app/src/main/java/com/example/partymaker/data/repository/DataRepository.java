package com.example.partymaker.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.partymaker.data.firebase.DatabaseFactory;
import com.example.partymaker.data.firebase.DatabaseService;
import com.example.partymaker.data.model.ServerResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Repository class for handling data operations.
 * This class ensures all database operations go through the Spring Boot server.
 */
public class DataRepository {
    private static final String TAG = "DataRepository";
    private final DatabaseService databaseService;
    private static DataRepository instance;
    
    /**
     * Private constructor to prevent direct instantiation.
     * Use getInstance() method to get the singleton instance.
     * 
     * @param context Android context
     */
    private DataRepository(Context context) {
        databaseService = DatabaseFactory.getDatabaseService(context);
        Log.d(TAG, "DataRepository initialized with DatabaseService");
    }
    
    /**
     * Get the singleton instance of DataRepository.
     * 
     * @param context Android context
     * @return DataRepository instance
     */
    public static synchronized DataRepository getInstance(Context context) {
        if (instance == null) {
            instance = new DataRepository(context);
        }
        return instance;
    }
    
    /**
     * Get a value from the database at the specified path.
     * 
     * @param path Database path
     * @return CompletableFuture with server response containing the value
     */
    public CompletableFuture<ServerResponse<Object>> getValue(String path) {
        Log.d(TAG, "Getting value at path: " + path);
        return databaseService.getValue(path);
    }
    
    /**
     * Get children of the database at the specified path.
     * 
     * @param path Database path
     * @return CompletableFuture with server response containing the children
     */
    public CompletableFuture<ServerResponse<Object>> getChildren(String path) {
        Log.d(TAG, "Getting children at path: " + path);
        return databaseService.getChildren(path);
    }
    
    /**
     * Set a value in the database at the specified path.
     * 
     * @param path Database path
     * @param value Value to set
     * @return CompletableFuture with server response
     */
    public CompletableFuture<ServerResponse<Object>> setValue(String path, Object value) {
        Log.d(TAG, "Setting value at path: " + path);
        return databaseService.setValue(path, value);
    }
    
    /**
     * Update children in the database at the specified path.
     * 
     * @param path Database path
     * @param updates Map of updates to apply
     * @return CompletableFuture with server response
     */
    public CompletableFuture<ServerResponse<Object>> updateChildren(String path, Map<String, Object> updates) {
        Log.d(TAG, "Updating children at path: " + path);
        return databaseService.updateChildren(path, updates);
    }
    
    /**
     * Remove a value from the database at the specified path.
     * 
     * @param path Database path
     * @return CompletableFuture with server response
     */
    public CompletableFuture<ServerResponse<Object>> removeValue(String path) {
        Log.d(TAG, "Removing value at path: " + path);
        return databaseService.removeValue(path);
    }
    
    /**
     * Reset the singleton instance (primarily for testing purposes).
     */
    public static synchronized void resetInstance() {
        instance = null;
        DatabaseFactory.resetInstance();
    }
} 