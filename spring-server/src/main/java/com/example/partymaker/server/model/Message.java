package com.example.partymaker.server.model;

/**
 * מודל המייצג הודעה במסיבה
 */
public class Message {
    private String id;
    private String senderId;
    private String receiverId;
    private String partyId;
    private String content;
    private Long timestamp;
    private boolean read;
    
    public Message() {
        this.timestamp = System.currentTimeMillis() / 1000;
        this.read = false;
    }
    
    public Message(String senderId, String receiverId, String partyId, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.partyId = partyId;
        this.content = content;
        this.timestamp = System.currentTimeMillis() / 1000;
        this.read = false;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getSenderId() {
        return senderId;
    }
    
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
    
    public String getReceiverId() {
        return receiverId;
    }
    
    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }
    
    public String getPartyId() {
        return partyId;
    }
    
    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isRead() {
        return read;
    }
    
    public void setRead(boolean read) {
        this.read = read;
    }
} 