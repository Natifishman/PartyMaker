package com.example.partymaker.data.model;

public class User {
  private String Email, UserName;

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
