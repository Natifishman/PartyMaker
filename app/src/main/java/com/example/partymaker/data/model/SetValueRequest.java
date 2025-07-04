package com.example.partymaker.data.model;

/**
 * Request class for setValue API
 */
public class SetValueRequest {
    private final String path;
    private final Object value;
    
    public SetValueRequest(String path, Object value) {
        this.path = path;
        this.value = value;
    }
    
    public String getPath() {
        return path;
    }
    
    public Object getValue() {
        return value;
    }
} 