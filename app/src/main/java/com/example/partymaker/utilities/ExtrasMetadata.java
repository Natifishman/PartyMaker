package com.example.partymaker.utilities;

import android.content.Intent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/** Holds all the data that needs to be passed between activities. */
public class ExtrasMetadata implements Serializable {
  private final HashMap<String, Object> data;
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

  public ExtrasMetadata() {
    this.groupName = null;
    this.groupKey = null;
    this.groupDays = null;
    this.groupMonths = null;
    this.groupYears = null;
    this.groupHours = null;
    this.groupLocation = null;
    this.adminKey = null;
    this.createdAt = null;
    this.groupPrice = null;
    this.groupType = 0;
    this.canAdd = false;
    this.friendKeys = null;
    this.comingKeys = null;
    this.messageKeys = null;
    this.data = new HashMap<>();
  }
  
  /**
   * Constructor with the minimal required parameters
   */
  public ExtrasMetadata(String groupName, String groupKey, String adminKey) {
    this.groupName = groupName;
    this.groupKey = groupKey;
    this.adminKey = adminKey;
    this.groupDays = null;
    this.groupMonths = null;
    this.groupYears = null;
    this.groupHours = null;
    this.groupLocation = null;
    this.createdAt = null;
    this.groupPrice = null;
    this.groupType = 0;
    this.canAdd = false;
    this.friendKeys = null;
    this.comingKeys = null;
    this.messageKeys = null;
    this.data = new HashMap<>();
  }

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
    this.data = new HashMap<>();
  }

  /**
   * Add a key-value pair to the extras
   * @param key The key
   * @param value The value
   */
  public void put(String key, Object value) {
    data.put(key, value);
  }

  /**
   * Add all extras to an intent
   * @param intent The intent to add extras to
   */
  public void addToIntent(Intent intent) {
    for (Map.Entry<String, Object> entry : data.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      
      if (value instanceof String) {
        intent.putExtra(key, (String) value);
      } else if (value instanceof Integer) {
        intent.putExtra(key, (Integer) value);
      } else if (value instanceof Boolean) {
        intent.putExtra(key, (Boolean) value);
      } else if (value instanceof Serializable) {
        intent.putExtra(key, (Serializable) value);
      }
    }
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
    if (friendKeys == null) {
      return new HashMap<>();
    }
    return new HashMap<>(friendKeys);
  }

  public HashMap<String, Object> getComingKeys() {
    if (comingKeys == null) {
      return new HashMap<>();
    }
    return new HashMap<>(comingKeys);
  }

  public HashMap<String, Object> getMessageKeys() {
    if (messageKeys == null) {
      return new HashMap<>();
    }
    return new HashMap<>(messageKeys);
  }
}
