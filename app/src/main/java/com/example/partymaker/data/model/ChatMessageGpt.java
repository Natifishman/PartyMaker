package com.example.partymaker.data.model;

/**
 * Simple model used for sending chat history to OpenAI.
 * The {@code role} is either "user", "assistant" or "system" and
 * {@code content} holds the actual message text.
 */
public class ChatMessageGpt {
  /** Message role such as "user", "assistant" or "system". */
  public String role;
  /** Message text content. */
  public String content;

  public ChatMessageGpt(String role, String content) {
    this.role = role;
    this.content = content;
  }
}
