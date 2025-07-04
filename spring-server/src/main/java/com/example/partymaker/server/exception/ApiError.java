package com.example.partymaker.server.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * מחלקה לייצוג שגיאות API
 */
public class ApiError {
    
    private HttpStatus status;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String message;
    private String debugMessage;
    private List<String> errors;
    
    public ApiError() {
        this.timestamp = LocalDateTime.now();
        this.errors = new ArrayList<>();
    }
    
    public ApiError(HttpStatus status) {
        this();
        this.status = status;
    }
    
    public ApiError(HttpStatus status, String message) {
        this();
        this.status = status;
        this.message = message;
    }
    
    public ApiError(HttpStatus status, String message, Throwable ex) {
        this();
        this.status = status;
        this.message = message;
        this.debugMessage = ex.getLocalizedMessage();
    }
    
    public void addError(String error) {
        this.errors.add(error);
    }
    
    // Getters and Setters
    
    public HttpStatus getStatus() {
        return status;
    }
    
    public void setStatus(HttpStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getDebugMessage() {
        return debugMessage;
    }
    
    public void setDebugMessage(String debugMessage) {
        this.debugMessage = debugMessage;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
} 