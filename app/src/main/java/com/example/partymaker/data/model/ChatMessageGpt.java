package com.example.partymaker.data.model;

public class ChatMessageGpt {
  public String role;
  public String content;

  public ChatMessageGpt(String role, String content) {
    this.role = role;
    this.content = content;
  }
}
