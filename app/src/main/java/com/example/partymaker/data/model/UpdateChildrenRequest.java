package com.example.partymaker.data.model;

import java.util.Map;

/**
 * Request class for updateChildren API
 */
public class UpdateChildrenRequest {
    private final String path;
    private final Map<String, Object> updates;
    
    public UpdateChildrenRequest(String path, Map<String, Object> updates) {
        this.path = path;
        this.updates = updates;
    }
    
    public String getPath() {
        return path;
    }
    
    public Map<String, Object> getUpdates() {
        return updates;
    }
} 