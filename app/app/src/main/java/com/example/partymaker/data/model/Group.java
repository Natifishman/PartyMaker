package com.example.partymaker.data.model;

import java.util.HashMap;

public class Group {
  private String groupName;
  private String groupKey;
  private String groupLocation;
  private String adminKey;
  private String createdAt;
  private String groupDays;
  private String groupMonths;
  private String groupYears;
  private String groupHours;
  private String groupPrice;
  private int GroupType; // if 0 so Public group if 1 so Private group
  private boolean CanAdd;
  private HashMap<String, Object> FriendKeys = new HashMap<>();
  private HashMap<String, Object> ComingKeys = new HashMap<>();
  private HashMap<String, Object> MessageKeys = new HashMap<>();

  public boolean isCanAdd() {
    return CanAdd;
  }

  public void setCanAdd(boolean canAdd) {
    CanAdd = canAdd;
  }

  public HashMap<String, Object> getMessageKeys() {
    return MessageKeys;
  }

  public HashMap<String, Object> getFriendKeys() {
    return FriendKeys;
  }

  public HashMap<String, Object> getComingKeys() {
    return ComingKeys;
  }

  public Group() {}

  public Group(
      String groupName,
      String groupKey,
      String groupLocation,
      String adminKey,
      String createdAt,
      String groupDays,
      String groupMonths,
      String groupYears,
      String groupHours,
      int groupType,
      String groupPrice,
      boolean canAdd,
      HashMap<String, Object> friendKeys,
      HashMap<String, Object> comingKeys,
      HashMap<String, Object> messageKeys) {
    this.groupName = groupName;
    this.groupKey = groupKey;
    this.groupLocation = groupLocation;
    this.adminKey = adminKey;
    this.createdAt = createdAt;
    this.groupDays = groupDays;
    this.groupMonths = groupMonths;
    this.groupYears = groupYears;
    this.groupHours = groupHours;
    this.groupPrice = groupPrice;
    GroupType = groupType;
    CanAdd = canAdd;
    FriendKeys = friendKeys;
    ComingKeys = comingKeys;
    MessageKeys = messageKeys;
  }

  public String getGroupPrice() {
    return groupPrice;
  }

  public void setGroupPrice(String groupPrice) {
    this.groupPrice = groupPrice;
  }

  public int getGroupType() {
    return GroupType;
  }

  public void setGroupType(int groupType) {
    GroupType = groupType;
  }

  public String getGroupLocation() {
    return groupLocation;
  }

  public void setGroupLocation(String groupLocation) {
    this.groupLocation = groupLocation;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public String getGroupKey() {
    return groupKey;
  }

  public void setGroupKey(String groupKey) {
    this.groupKey = groupKey;
  }

  public String getAdminKey() {
    return adminKey;
  }

  public void setAdminKey(String adminKey) {
    this.adminKey = adminKey;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  public String getGroupDays() {
    return groupDays;
  }

  public void setGroupDays(String groupDays) {
    this.groupDays = groupDays;
  }

  public String getGroupMonths() {
    return groupMonths;
  }

  public void setGroupMonths(String groupMonths) {
    this.groupMonths = groupMonths;
  }

  public String getGroupYears() {
    return groupYears;
  }

  public void setGroupYears(String groupYears) {
    this.groupYears = groupYears;
  }

  public String getGroupHours() {
    return groupHours;
  }

  public void setGroupHours(String groupHours) {
    this.groupHours = groupHours;
  }
}
