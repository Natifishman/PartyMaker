package com.example.partymaker.utilities;

import java.io.Serializable;
import java.util.HashMap;

/** Holds all the data that needs to be passed between activities. */
public class ExtrasMetadata implements Serializable {
  private final String groupName;
  private final String groupKey;
  private final String groupDays;
  private final String groupMonths;
  private final String groupYears;
  private final String groupHours;
  private final String groupLocation;
  private final String adminKey;
  private final String createdAt;
  private final String groupPrice;
  private final int groupType;
  private final boolean canAdd;
  private final HashMap<String, Object> friendKeys;
  private final HashMap<String, Object> comingKeys;
  private final HashMap<String, Object> messageKeys;

  public ExtrasMetadata(
      String groupName,
      String groupKey,
      String groupDays,
      String groupMonths,
      String groupYears,
      String groupHours,
      String groupLocation,
      String adminKey,
      String createdAt,
      String groupPrice,
      int groupType,
      boolean canAdd,
      HashMap<String, Object> friendKeys,
      HashMap<String, Object> comingKeys,
      HashMap<String, Object> messageKeys) {
    this.groupName = groupName;
    this.groupKey = groupKey;
    this.groupDays = groupDays;
    this.groupMonths = groupMonths;
    this.groupYears = groupYears;
    this.groupHours = groupHours;
    this.groupLocation = groupLocation;
    this.adminKey = adminKey;
    this.createdAt = createdAt;
    this.groupPrice = groupPrice;
    this.groupType = groupType;
    this.canAdd = canAdd;
    this.friendKeys = friendKeys;
    this.comingKeys = comingKeys;
    this.messageKeys = messageKeys;
  }

  public String getGroupName() {
    return groupName;
  }

  public String getGroupKey() {
    return groupKey;
  }

  public String getGroupDays() {
    return groupDays;
  }

  public String getGroupMonths() {
    return groupMonths;
  }

  public String getGroupYears() {
    return groupYears;
  }

  public String getGroupHours() {
    return groupHours;
  }

  public String getGroupLocation() {
    return groupLocation;
  }

  public String getAdminKey() {
    return adminKey;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public String getGroupPrice() {
    return groupPrice;
  }

  public int getGroupType() {
    return groupType;
  }

  public boolean isCanAdd() {
    return canAdd;
  }

  public HashMap<String, Object> getFriendKeys() {
    return friendKeys;
  }

  public HashMap<String, Object> getComingKeys() {
    return comingKeys;
  }

  public HashMap<String, Object> getMessageKeys() {
    return messageKeys;
  }
}
