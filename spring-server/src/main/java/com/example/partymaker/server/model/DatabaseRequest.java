package com.example.partymaker.server.model;

/**
 * מודל עבור בקשות מסד נתונים
 */
public class DatabaseRequest {
    private String path;
    private Object value;
    
    public DatabaseRequest() {
        // נדרש עבור Jackson
    }
    
    public DatabaseRequest(String path, Object value) {
        this.path = path;
        this.value = value;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public Object getValue() {
        return value;
    }
    
    public void setValue(Object value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return "DatabaseRequest{" +
                "path='" + path + '\'' +
                ", value=" + value +
                '}';
    }
} 