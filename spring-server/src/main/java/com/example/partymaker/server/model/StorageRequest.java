package com.example.partymaker.server.model;

/**
 * מודל עבור בקשות אחסון
 */
public class StorageRequest {
    private String path;
    
    public StorageRequest() {
        // נדרש עבור Jackson
    }
    
    public StorageRequest(String path) {
        this.path = path;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    @Override
    public String toString() {
        return "StorageRequest{" +
                "path='" + path + '\'' +
                '}';
    }
} 