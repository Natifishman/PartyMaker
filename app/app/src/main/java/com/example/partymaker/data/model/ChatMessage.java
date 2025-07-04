package com.example.partymaker.data.model;

public class ChatMessage {
  private String messageText;
  private String messageUser;
  private String messageTime;
  private String MessageKey;

  public ChatMessage(
      String messageText, String messageUser, String messageTime, String messageKey) {
    this.messageText = messageText;
    this.messageUser = messageUser;
    this.messageTime = messageTime;
    MessageKey = messageKey;
  }

  public ChatMessage() {}

  public String getMessageText() {
    return messageText;
  }

  public void setMessageText(String messageText) {
    this.messageText = messageText;
  }

  public String getMessageUser() {
    return messageUser;
  }

  public void setMessageUser(String messageUser) {
    this.messageUser = messageUser;
  }

  public String getMessageTime() {
    return messageTime;
  }

  public void setMessageTime(String messageTime) {
    this.messageTime = messageTime;
  }

  public String getMessageKey() {
    return MessageKey;
  }

  public void setMessageKey(String messageKey) {
    MessageKey = messageKey;
  }
}
