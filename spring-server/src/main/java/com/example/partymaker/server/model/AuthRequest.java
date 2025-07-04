package com.example.partymaker.server.model;

/**
 * מודל עבור בקשות אימות
 */
public class AuthRequest {
    private String email;
    private String password;
    
    public AuthRequest() {
        // נדרש עבור Jackson
    }
    
    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    @Override
    public String toString() {
        return "AuthRequest{" +
                "email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
} 