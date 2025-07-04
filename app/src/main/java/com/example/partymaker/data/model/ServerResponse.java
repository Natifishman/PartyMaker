package com.example.partymaker.data.model;

/**
 * Model class for server responses
 */
public class ServerResponse<T> {
    private int status;
    private String message;
    private T data;
    
    public ServerResponse() {
        // Constructor needed for Gson
    }
    
    public ServerResponse(String status, T data) {
        this.status = "success".equals(status) ? 200 : 400;
        this.message = status;
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
    
    public boolean isSuccess() {
        return status >= 200 && status < 300;
    }
    
    @Override
    public String toString() {
        return "ServerResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
} 