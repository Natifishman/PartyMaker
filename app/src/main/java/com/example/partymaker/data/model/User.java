package com.example.partymaker.data.model;

/** Simple user profile model used across the app. */
public class User {
  /** User email used as a unique identifier. */
  private String Email;
  /** Display name chosen by the user. */
  private String UserName;

  public String getEmail() {
    return Email;
  }

  public void setEmail(String email) {
    Email = email;
  }

  public String getUserName() {
    return UserName;
  }

  @SuppressWarnings("unused")
  public void setUserName(String userName) {
    UserName = userName;
  }

  public User() {}

  public User(String Email, String UserName) {
    this.Email = Email;
    this.UserName = UserName;
  }
}
