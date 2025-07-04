package com.example.partymaker.server.model;

import java.util.concurrent.CompletableFuture;

/**
 * Generic model for server responses
 */
public class ServerResponse<T> {
    private int status;
    private String message;
    private T data;
    
    public ServerResponse() {
        // Required for Jackson
    }
    
    public ServerResponse(String message, T data) {
        this.status = "success".equals(message) ? 200 : 400;
        this.message = message;
        this.data = data;
    }
    
    public ServerResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "ServerResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
    
    /**
     * Waits for completion and returns the result
     */
    @SuppressWarnings("unchecked")
    public T join() {
        if (data instanceof CompletableFuture<?>) {
            CompletableFuture<T> future = (CompletableFuture<T>) data;
            return future.join();
        }
        return data;
    }
} 