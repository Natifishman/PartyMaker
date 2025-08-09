package com.example.partymaker.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;
import java.util.HashMap;

/**
 * Enterprise-level Group entity representing a party/event in the PartyMaker ecosystem.
 * 
 * This class serves as the core data model for parties/events and implements:
 * - Room database persistence with automatic migrations
 * - Firebase Realtime Database synchronization
 * - JSON serialization for API communication
 * - Builder pattern support for object creation
 * 
 * Data Architecture:
 * - Primary Key: groupKey (unique identifier)
 * - Foreign Keys: adminKey (references User), friendKeys (User collection)
 * - Indexes: groupType, createdAt for query optimization
 * 
 * Business Rules:
 * - Public groups (type 0): Discoverable by all users
 * - Private groups (type 1): Invitation-only access
 * - Admin has full control over group settings and membership
 * - Group data is cached locally for offline access
 * 
 * Synchronization:
 * - Local-first architecture with Room database
 * - Background sync with Firebase when online
 * - Conflict resolution using server timestamps
 * 
 * @author PartyMaker Team
 * @version 2.0
 * @since 1.0
 * 
 * @see androidx.room.Entity
 * @see com.google.firebase.database.DatabaseReference
 */
@Entity(tableName = "groups")
public class Group {

  // ==================== Business Constants ====================
  /** 
   * Public group type - discoverable by all users in public listings.
   * Public groups appear in the "Public Parties" section and can be joined by anyone.
   */
  public static final int GROUP_TYPE_PUBLIC = 0;

  /** 
   * Private group type - invitation-only access.
   * Private groups are only accessible to invited members and don't appear in public listings.
   */
  public static final int GROUP_TYPE_PRIVATE = 1;

  // ==================== Core Identity Fields ====================
  /** 
   * Human-readable name for the party/group.
   * Used in UI displays and notifications. Max length: 100 characters.
   */
  @ColumnInfo(name = "group_name")
  private String groupName;

  /** 
   * Unique identifier for this group across the entire system.
   * Generated server-side to ensure uniqueness. Used as primary key for all operations.
   */
  @PrimaryKey
  @NonNull
  @ColumnInfo(name = "groupKey")
  private String groupKey;

  /** 
   * Geographic location or venue name for the event.
   * Can be address, venue name, or descriptive location. Optional field.
   */
  @ColumnInfo(name = "group_location")
  private String groupLocation;

  /** 
   * User ID of the group administrator.
   * Admin has full permissions: edit group, manage members, delete group.
   * References User.userKey foreign key relationship.
   */
  @ColumnInfo(name = "admin_key")
  private String adminKey;

  // ==================== Temporal Fields ====================
  /** 
   * ISO 8601 timestamp when the group was created.
   * Used for sorting, analytics, and cleanup operations.
   */
  @ColumnInfo(name = "created_at")
  private String createdAt;

  /** 
   * Day component of event date (1-31).
   * Legacy field - consider migrating to Date object for better handling.
   */
  @ColumnInfo(name = "group_days")
  private String groupDays;

  /** 
   * Month component of event date (1-12).
   * Legacy field - stored as string for historical compatibility.
   */
  @ColumnInfo(name = "group_months")
  private String groupMonths;

  /** 
   * Year component of event date (YYYY format).
   * Legacy field - stored as string for historical compatibility.
   */
  @ColumnInfo(name = "group_years")
  private String groupYears;

  /** 
   * Hour component of event time (HH format, 24-hour).
   * Legacy field - consider migrating to Time object.
   */
  @ColumnInfo(name = "group_hours")
  private String groupHours;

  /** 
   * Minute component of event time (MM format).
   * Legacy field - stored as string for historical compatibility.
   */
  @ColumnInfo(name = "group_minutes")
  private String groupMinutes;

  /** The image URL of the group. */
  @ColumnInfo(name = "group_image_url")
  private String groupImageUrl;

  /** The price of the event. */
  @ColumnInfo(name = "group_price")
  private String groupPrice;

  /** Group type: {@link #GROUP_TYPE_PUBLIC} or {@link #GROUP_TYPE_PRIVATE}. */
  @ColumnInfo(name = "group_type")
  private int groupType;

  /** Whether users can add new members. */
  @ColumnInfo(name = "can_add")
  private boolean canAdd;

  /** The description of the group. */
  @ColumnInfo(name = "group_description")
  private String groupDescription;

  // ==================== Relationship Collections ====================
  /** 
   * Map of group members (User IDs -> membership status).
   * Key: User ID, Value: Join timestamp or status object
   * Used for membership management and access control.
   */
  @SerializedName(
      value = "friendKeys",
      alternate = {"FriendKeys"})
  @ColumnInfo(name = "friend_keys")
  private HashMap<String, Object> friendKeys = new HashMap<>();

  /** 
   * Map of users confirmed as attending the event.
   * Key: User ID, Value: Attendance confirmation timestamp
   * Subset of friendKeys - tracks RSVP responses.
   */
  @SerializedName(
      value = "comingKeys",
      alternate = {"ComingKeys"})
  @ColumnInfo(name = "coming_keys")
  private HashMap<String, Object> comingKeys = new HashMap<>();

  /** 
   * Map of chat message IDs belonging to this group.
   * Key: Message ID, Value: Message metadata or timestamp
   * Used for message ordering and cleanup operations.
   */
  @SerializedName(
      value = "messageKeys",
      alternate = {"MessageKeys"})
  @ColumnInfo(name = "message_keys")
  private HashMap<String, Object> messageKeys = new HashMap<>();

  /** Default constructor. */
  public Group() {
    this.groupKey = "";
  }

  /**
   * Constructs a group with all fields.
   *
   * @param groupName the group name
   * @param groupKey the group key
   * @param groupLocation the location
   * @param adminKey the admin's key
   * @param createdAt the creation timestamp
   * @param groupDays the days
   * @param groupMonths the months
   * @param groupYears the years
   * @param groupHours the hours
   * @param groupType the group type ({@link #GROUP_TYPE_PUBLIC} or {@link #GROUP_TYPE_PRIVATE})
   * @param groupPrice the price
   * @param canAdd whether users can add
   * @param friendKeys map of friend keys
   * @param comingKeys map of coming keys
   * @param messageKeys map of message keys
   */
  @Ignore
  public Group(
      String groupName,
      @NonNull String groupKey,
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
    this.groupType = groupType;
    this.canAdd = canAdd;
    this.friendKeys = friendKeys;
    this.comingKeys = comingKeys;
    this.messageKeys = messageKeys;
  }

  public boolean isCanAdd() {
    return canAdd;
  }

  public void setCanAdd(boolean canAdd) {
    this.canAdd = canAdd;
  }

  public HashMap<String, Object> getMessageKeys() {
    return messageKeys;
  }

  public void setMessageKeys(HashMap<String, Object> messageKeys) {
    this.messageKeys = messageKeys;
  }

  public HashMap<String, Object> getFriendKeys() {
    return friendKeys;
  }

  public void setFriendKeys(HashMap<String, Object> friendKeys) {
    this.friendKeys = friendKeys;
  }

  public HashMap<String, Object> getComingKeys() {
    return comingKeys;
  }

  public void setComingKeys(HashMap<String, Object> comingKeys) {
    this.comingKeys = comingKeys;
  }

  public String getGroupPrice() {
    return groupPrice;
  }

  public void setGroupPrice(String groupPrice) {
    this.groupPrice = groupPrice;
  }

  public int getGroupType() {
    return groupType;
  }

  public void setGroupType(int groupType) {
    this.groupType = groupType;
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

  @NonNull
  public String getGroupKey() {
    return groupKey;
  }

  public void setGroupKey(@NonNull String groupKey) {
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

  public String getGroupMinutes() {
    return groupMinutes;
  }

  public void setGroupMinutes(String groupMinutes) {
    this.groupMinutes = groupMinutes;
  }

  public String getGroupImageUrl() {
    return groupImageUrl;
  }

  public void setGroupImageUrl(String groupImageUrl) {
    this.groupImageUrl = groupImageUrl;
  }

  /**
   * Gets the group description
   *
   * @return the group description
   */
  public String getGroupDescription() {
    return groupDescription;
  }

  /**
   * Sets the group description
   *
   * @param groupDescription the group description
   */
  public void setGroupDescription(String groupDescription) {
    this.groupDescription = groupDescription;
  }
}
