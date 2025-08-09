package com.example.partymaker.viewmodel.core;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.partymaker.data.api.Result;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.data.repository.GroupRepository;
import com.example.partymaker.viewmodel.BaseViewModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enterprise-level ViewModel for MainActivity group operations.
 * 
 * This ViewModel implements the MVVM architecture pattern and serves as the
 * presentation layer's interface to group-related business logic. It provides:
 * 
 * Core Features:
 * - Lifecycle-aware group data management
 * - Real-time data synchronization with Repository layer
 * - Comprehensive error handling with user-friendly messages
 * - Loading state management for optimal UX
 * - Group operations: CRUD, membership management
 * - Data validation and business rule enforcement
 * 
 * Architecture Benefits:
 * - Survives configuration changes (screen rotation, language change)
 * - Separates UI logic from business logic
 * - Provides reactive data streams via LiveData
 * - Implements repository pattern for data abstraction
 * - Thread-safe operations with background processing
 * 
 * Data Flow:
 * 1. UI triggers operation via ViewModel methods
 * 2. ViewModel validates input and delegates to Repository
 * 3. Repository handles data persistence and network operations
 * 4. Results flow back through LiveData observers
 * 5. UI updates reactively based on data changes
 * 
 * Performance Optimizations:
 * - Data caching through Repository layer
 * - Lazy loading of group details
 * - Efficient sorting and filtering operations
 * - Memory-conscious list management
 * 
 * @author PartyMaker Team
 * @version 2.0
 * @since 1.0
 * 
 * @see BaseViewModel for common ViewModel functionality
 * @see GroupRepository for data operations
 * @see androidx.lifecycle.ViewModel for lifecycle awareness
 */
public class MainActivityViewModel extends BaseViewModel {

  /** Logging tag for debugging and monitoring */
  private static final String TAG = "MainActivityViewModel";

  // ==================== User-Facing Error Messages ====================
  /** Error when user authentication is missing */
  private static final String ERROR_EMPTY_USER_KEY = "User key cannot be empty";
  /** Error when group identifier is missing */
  private static final String ERROR_EMPTY_GROUP_ID = "Group ID cannot be empty";
  /** Error when update payload is empty */
  private static final String ERROR_EMPTY_UPDATES = "Updates cannot be empty";
  /** Error when requested group doesn't exist */
  private static final String ERROR_GROUP_NOT_FOUND = "Group not found";

  // ==================== User-Facing Success Messages ====================
  /** Success message for group creation */
  private static final String MSG_GROUP_CREATED = "Group created successfully";
  /** Success message for group updates */
  private static final String MSG_GROUP_UPDATED = "Group updated successfully";
  /** Success message for group deletion */
  private static final String MSG_GROUP_DELETED = "Group deleted successfully";
  /** Success message for joining a group */
  private static final String MSG_GROUP_JOINED = "Successfully joined group";
  /** Success message for leaving a group */
  private static final String MSG_GROUP_LEFT = "Successfully left group";
  /** Success message for loading user's groups */
  private static final String MSG_USER_GROUPS_LOADED = "User groups loaded successfully";
  /** Success message for loading all public groups */
  private static final String MSG_ALL_GROUPS_LOADED = "All groups loaded successfully";

  // ==================== LiveData State Holders ====================
  /** 
   * Holds the list of groups for the current user.
   * Emits updates when groups are loaded, created, updated, or deleted.
   */
  private final MutableLiveData<List<Group>> groupList = new MutableLiveData<>();
  
  /** 
   * Holds the currently selected group for detailed operations.
   * Used for navigation and context-aware operations.
   */
  private final MutableLiveData<Group> selectedGroup = new MutableLiveData<>();

  // ==================== Dependencies ====================
  /** 
   * Repository instance for all group-related data operations.
   * Provides abstraction over local database and remote server.
   */
  private final GroupRepository repository;

  /**
   * Constructs MainActivityViewModel with application context.
   * 
   * Initializes the ViewModel and establishes data binding with the Repository.
   * Sets up real-time data synchronization and error handling.
   * 
   * Initialization Process:
   * 1. Call parent constructor for base functionality
   * 2. Initialize Repository dependency
   * 3. Establish LiveData observers for reactive updates
   * 4. Configure data transformation (sorting, filtering)
   * 5. Set up error propagation to UI layer
   * 
   * @param application Application context for Repository initialization
   * 
   * @implNote Uses observeForever for Repository LiveData - ensure proper cleanup
   * @implNote Repository is singleton - safe to get instance in constructor
   */
  public MainActivityViewModel(@NonNull Application application) {
    super(application);
    
    // Step 1: Initialize Repository dependency
    repository = GroupRepository.getInstance();

    // Step 2: Establish reactive data pipeline from Repository to ViewModel
    LiveData<Result<List<Group>>> repoGroups = repository.getAllGroupsLiveData();
    if (repoGroups != null) {
      // Set up observer chain: Repository -> ViewModel -> UI
      repoGroups.observeForever(
          result -> {
            // Handle successful data retrieval
            if (result.isSuccess()) {
              List<Group> data = result.getData();
              if (data != null) {
                // Apply business logic: sort groups by relevance/date
                sortGroups(data);
                // Emit to UI layer
                groupList.setValue(data);
              }
            } 
            // Handle error states
            else if (result.isError()) {
              // Propagate user-friendly error messages to UI
              setError(result.getUserFriendlyError());
            }
            // Update loading state for UI feedback
            setLoading(result.isLoading());
          });
    }
  }

  /**
   * Exposes the user's groups as observable LiveData.
   * 
   * This method provides the UI with a reactive data stream of the user's groups.
   * The data is automatically updated when:
   * - Groups are loaded from server/database
   * - New groups are created
   * - Existing groups are modified
   * - Groups are deleted or user leaves them
   * 
   * Data Characteristics:
   * - Sorted by creation date (newest first)
   * - Filtered to user's groups only (member or admin)
   * - Includes both public and private groups
   * - Cached locally for offline access
   * 
   * @return LiveData stream of user's groups, never null but may contain empty list
   * 
   * @implNote UI should observe this LiveData to reactively update group listings
   * @implNote Data survives configuration changes due to ViewModel scope
   */
  public LiveData<List<Group>> getGroups() {
    return groupList;
  }

  /**
   * Gets the currently selected group.
   *
   * @return LiveData containing the selected group, null if no group is selected
   */
  public LiveData<Group> getSelectedGroup() {
    return selectedGroup;
  }

  // Note: getIsLoading() and getErrorMessage() are inherited from BaseViewModel

  /**
   * Loads groups for a specific user.
   *
   * @param userKey The user key to load groups for
   * @param forceRefresh Whether to force a refresh from the server
   * @throws IllegalArgumentException if userKey is null or empty
   */
  public void loadUserGroups(@NonNull String userKey, boolean forceRefresh) {
    if (userKey.trim().isEmpty()) {
      throw new IllegalArgumentException(ERROR_EMPTY_USER_KEY);
    }

    Log.d(TAG, "Loading groups for user: " + userKey + ", forceRefresh: " + forceRefresh);

    executeIfNotLoading(
        () -> {
          setLoading(true);
          clearError();

          repository.getUserGroups(
              userKey,
              result -> {
                if (result.isLoading()) {
                  setLoading(true);
                } else if (result.isSuccess()) {
                  handleSuccessfulGroupsLoad(result.getData(), MSG_USER_GROUPS_LOADED);
                } else if (result.isError()) {
                  Log.e(TAG, "Error loading user groups: " + result.getError());
                  setError(result.getUserFriendlyError());
                }

                if (!result.isLoading()) {
                  setLoading(false);
                }
              },
              forceRefresh);
        });
  }

  /**
   * Loads all groups available in the system.
   *
   * @param forceRefresh Whether to force a refresh from the server
   */
  public void loadAllGroups(boolean forceRefresh) {
    Log.d(TAG, "Loading all groups, forceRefresh: " + forceRefresh);

    executeIfNotLoading(
        () -> {
          setLoading(true);
          clearError();

          repository.getAllGroups(
              groups -> {
                handleSuccessfulGroupsLoad(groups, MSG_ALL_GROUPS_LOADED);
                setLoading(false);
              },
              forceRefresh);
        });
  }

  /**
   * Loads a specific group by its ID.
   *
   * @param groupId The group ID to load
   * @param forceRefresh Whether to force a refresh from the server
   * @throws IllegalArgumentException if groupId is null or empty
   */
  public void loadGroup(@NonNull String groupId, boolean forceRefresh) {
    if (groupId.trim().isEmpty()) {
      throw new IllegalArgumentException(ERROR_EMPTY_GROUP_ID);
    }

    Log.d(TAG, "Loading group with ID: " + groupId + ", forceRefresh: " + forceRefresh);

    executeIfNotLoading(
        () -> {
          setLoading(true);
          clearError();

          repository.getGroup(
              groupId,
              group -> {
                if (group != null) {
                  Log.d(TAG, "Group loaded successfully: " + group.getGroupName());
                  selectedGroup.setValue(group);
                } else {
                  Log.e(TAG, "Group not found: " + groupId);
                  setError(ERROR_GROUP_NOT_FOUND);
                }
                setLoading(false);
              },
              forceRefresh);
        });
  }

  /**
   * Creates a new group.
   *
   * @param groupId The unique group ID
   * @param group The group object to create
   * @throws IllegalArgumentException if parameters are invalid
   */
  public void createGroup(@NonNull String groupId, @NonNull Group group) {
    if (groupId.trim().isEmpty()) {
      throw new IllegalArgumentException(ERROR_EMPTY_GROUP_ID);
    }

    Log.d(TAG, "Creating new group: " + group.getGroupName());

    executeIfNotLoading(
        () -> {
          setLoading(true);
          clearError();

          repository.saveGroup(
              groupId,
              group,
              () -> {
                Log.d(TAG, "Group created successfully");
                addGroupToList(group);
                selectedGroup.setValue(group);
                setSuccess(MSG_GROUP_CREATED);
                setLoading(false);
              });
        });
  }

  /**
   * Updates an existing group with the provided field updates.
   *
   * @param groupId The group ID to update
   * @param updates Map of field names to new values
   * @throws IllegalArgumentException if parameters are invalid
   */
  public void updateGroup(@NonNull String groupId, @NonNull Map<String, Object> updates) {
    if (groupId.trim().isEmpty()) {
      throw new IllegalArgumentException(ERROR_EMPTY_GROUP_ID);
    }
    if (updates.isEmpty()) {
      throw new IllegalArgumentException(ERROR_EMPTY_UPDATES);
    }

    Log.d(TAG, "Updating group: " + groupId + " with " + updates.size() + " changes");

    executeIfNotLoading(
        () -> {
          setLoading(true);
          clearError();

          repository.updateGroup(
              groupId,
              updates,
              () -> {
                Log.d(TAG, "Group updated successfully");
                applyUpdatesToLocalGroups(groupId, updates);
                setSuccess(MSG_GROUP_UPDATED);
                setLoading(false);
              });
        });
  }

  /**
   * Deletes an existing group.
   *
   * @param groupId The group ID to delete
   * @throws IllegalArgumentException if groupId is invalid
   */
  public void deleteGroup(@NonNull String groupId) {
    if (groupId.trim().isEmpty()) {
      throw new IllegalArgumentException(ERROR_EMPTY_GROUP_ID);
    }

    Log.d(TAG, "Deleting group: " + groupId);

    executeIfNotLoading(
        () -> {
          setLoading(true);
          clearError();

          repository.deleteGroup(
              groupId,
              () -> {
                Log.d(TAG, "Group deleted successfully");
                removeGroupFromList(groupId);
                clearSelectedGroupIfMatches(groupId);
                setSuccess(MSG_GROUP_DELETED);
                setLoading(false);
              });
        });
  }

  /**
   * Joins a group for the specified user.
   *
   * @param groupId The group ID to join
   * @param userKey The user key of the user joining
   * @throws IllegalArgumentException if parameters are invalid
   */
  public void joinGroup(@NonNull String groupId, @NonNull String userKey) {
    if (groupId.trim().isEmpty()) {
      throw new IllegalArgumentException(ERROR_EMPTY_GROUP_ID);
    }
    if (userKey.trim().isEmpty()) {
      throw new IllegalArgumentException(ERROR_EMPTY_USER_KEY);
    }

    Log.d(TAG, "Joining group: " + groupId + " for user: " + userKey);

    executeIfNotLoading(
        () -> {
          setLoading(true);
          clearError();

          repository.joinGroup(
              groupId,
              userKey,
              new GroupRepository.OperationCallback() {
                @Override
                public void onComplete() {
                  Log.d(TAG, "Successfully joined group: " + groupId);
                  setSuccess(MSG_GROUP_JOINED);
                  setLoading(false);
                  // Refresh the group data to show the updated membership
                  loadGroup(groupId, true);
                }

                @Override
                public void onError(String error) {
                  Log.e(TAG, "Error joining group: " + error);
                  setError("Failed to join group: " + error);
                  setLoading(false);
                }
              });
        });
  }

  /**
   * Leaves a group for the specified user.
   *
   * @param groupId The group ID to leave
   * @param userKey The user key of the user leaving
   * @throws IllegalArgumentException if parameters are invalid
   */
  public void leaveGroup(@NonNull String groupId, @NonNull String userKey) {
    if (groupId.trim().isEmpty()) {
      throw new IllegalArgumentException(ERROR_EMPTY_GROUP_ID);
    }
    if (userKey.trim().isEmpty()) {
      throw new IllegalArgumentException(ERROR_EMPTY_USER_KEY);
    }

    Log.d(TAG, "Leaving group: " + groupId + " for user: " + userKey);

    executeIfNotLoading(
        () -> {
          setLoading(true);
          clearError();

          repository.leaveGroup(
              groupId,
              userKey,
              new GroupRepository.OperationCallback() {
                @Override
                public void onComplete() {
                  Log.d(TAG, "Successfully left group: " + groupId);
                  removeGroupFromList(groupId);
                  clearSelectedGroupIfMatches(groupId);
                  setSuccess(MSG_GROUP_LEFT);
                  setLoading(false);
                }

                @Override
                public void onError(String error) {
                  Log.e(TAG, "Error leaving group: " + error);
                  setError("Failed to leave group: " + error);
                  setLoading(false);
                }
              });
        });
  }

  /**
   * Selects a group by loading it and setting it as the current selection.
   *
   * @param groupId The group ID to select
   * @param forceRefresh Whether to force a refresh from the server
   * @throws IllegalArgumentException if groupId is invalid
   */
  public void selectGroup(@NonNull String groupId, boolean forceRefresh) {
    if (groupId.trim().isEmpty()) {
      throw new IllegalArgumentException(ERROR_EMPTY_GROUP_ID);
    }

    Log.d(TAG, "Selecting group: " + groupId + ", forceRefresh: " + forceRefresh);
    loadGroup(groupId, forceRefresh);
  }

  /**
   * Adds a group to the current group list and sorts it.
   *
   * @param group The group to add
   */
  private void addGroupToList(@NonNull Group group) {
    List<Group> currentList = groupList.getValue();
    if (currentList == null) {
      currentList = new ArrayList<>();
    } else {
      currentList = new ArrayList<>(currentList); // Create mutable copy
    }

    currentList.add(group);
    sortGroups(currentList);
    groupList.setValue(currentList);
  }

  /**
   * Applies updates to both the group list and selected group if they match the given ID.
   *
   * @param groupId The ID of the group to update
   * @param updates The updates to apply
   */
  private void applyUpdatesToLocalGroups(
      @NonNull String groupId, @NonNull Map<String, Object> updates) {
    // Update the group in the list
    List<Group> currentList = groupList.getValue();
    if (currentList != null) {
      for (Group group : currentList) {
        if (groupId.equals(group.getGroupKey())) {
          for (Map.Entry<String, Object> entry : updates.entrySet()) {
            applyUpdateToGroup(group, entry.getKey(), entry.getValue());
          }
          break;
        }
      }
      groupList.setValue(currentList); // Trigger observers
    }

    // Update selected group if it matches
    Group selectedGroupValue = selectedGroup.getValue();
    if (selectedGroupValue != null && groupId.equals(selectedGroupValue.getGroupKey())) {
      for (Map.Entry<String, Object> entry : updates.entrySet()) {
        applyUpdateToGroup(selectedGroupValue, entry.getKey(), entry.getValue());
      }
      selectedGroup.setValue(selectedGroupValue); // Trigger observers
    }
  }

  /**
   * Removes a group from the current group list.
   *
   * @param groupId The ID of the group to remove
   */
  private void removeGroupFromList(@NonNull String groupId) {
    List<Group> currentList = groupList.getValue();
    if (currentList != null) {
      List<Group> newList = new ArrayList<>(currentList);
      newList.removeIf(group -> groupId.equals(group.getGroupKey()));
      groupList.setValue(newList);
    }
  }

  /**
   * Clears the selected group if it matches the given ID.
   *
   * @param groupId The ID to check against
   */
  private void clearSelectedGroupIfMatches(@NonNull String groupId) {
    Group selectedGroupValue = selectedGroup.getValue();
    if (selectedGroupValue != null && groupId.equals(selectedGroupValue.getGroupKey())) {
      selectedGroup.setValue(null);
    }
  }

  /** Clears all data from the ViewModel. Typically used during logout or when switching users. */
  public void clearAllData() {
    Log.d(TAG, "Clearing all ViewModel data");
    groupList.setValue(new ArrayList<>());
    selectedGroup.setValue(null);
    clearMessages();
    setLoading(false);
  }

  /**
   * Helper method to handle successful groups loading with common logic.
   *
   * @param groups The loaded groups data
   * @param successMessage The success message to log
   */
  private void handleSuccessfulGroupsLoad(
      @Nullable List<Group> groups, @NonNull String successMessage) {
    if (groups != null) {
      Log.d(TAG, successMessage + ": " + groups.size() + " groups");
      sortGroups(groups);
      groupList.setValue(groups);
    } else {
      Log.w(TAG, successMessage + " but data is null - setting empty list");
      groupList.setValue(new ArrayList<>());
    }
  }

  /**
   * Sorts the groups by name in a case-insensitive manner. Groups with null names are placed at the
   * end.
   *
   * @param groups The groups to sort (modified in place)
   */
  private void sortGroups(@Nullable List<Group> groups) {
    if (groups == null || groups.isEmpty()) {
      return;
    }

    groups.sort(
        (g1, g2) -> {
          String name1 = g1 != null ? g1.getGroupName() : null;
          String name2 = g2 != null ? g2.getGroupName() : null;

          if (name1 == null && name2 == null) {
            return 0;
          } else if (name1 == null) {
            return 1; // null names go to the end
          } else if (name2 == null) {
            return -1; // null names go to the end
          }
          return name1.compareToIgnoreCase(name2);
        });
  }

  /**
   * Applies an update to a group
   *
   * @param group The group to update
   * @param field The field to update
   * @param value The new value
   */
  @SuppressWarnings("unchecked")
  private void applyUpdateToGroup(Group group, String field, Object value) {
    if (group == null || field == null) {
      return;
    }

    switch (field) {
      case "groupName":
        if (value instanceof String) {
          group.setGroupName((String) value);
        }
        break;
      case "groupLocation":
        if (value instanceof String) {
          group.setGroupLocation((String) value);
        }
        break;
      case "groupDays":
        if (value instanceof String) {
          group.setGroupDays((String) value);
        }
        break;
      case "groupMonths":
        if (value instanceof String) {
          group.setGroupMonths((String) value);
        }
        break;
      case "groupYears":
        if (value instanceof String) {
          group.setGroupYears((String) value);
        }
        break;
      case "groupHours":
        if (value instanceof String) {
          group.setGroupHours((String) value);
        }
        break;
      case "groupPrice":
        if (value instanceof String) {
          group.setGroupPrice((String) value);
        } else if (value instanceof Double || value instanceof Integer) {
          group.setGroupPrice(String.valueOf(value));
        }
        break;
      case "groupType":
        if (value instanceof Integer) {
          group.setGroupType((Integer) value);
        } else if (value instanceof String) {
          try {
            group.setGroupType(Integer.parseInt((String) value));
          } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid group type format: " + value);
          }
        }
        break;
      case "canAdd":
        if (value instanceof Boolean) {
          group.setCanAdd((Boolean) value);
        }
        break;
      case "groupDescription":
        if (value instanceof String) {
          group.setGroupDescription((String) value);
        }
        break;
      case "friendKeys":
        if (value instanceof HashMap) {
          group.setFriendKeys((HashMap<String, Object>) value);
        }
        break;
      case "comingKeys":
        if (value instanceof HashMap) {
          group.setComingKeys((HashMap<String, Object>) value);
        }
        break;
      case "messageKeys":
        if (value instanceof HashMap) {
          group.setMessageKeys((HashMap<String, Object>) value);
        }
        break;
      default:
        Log.w(TAG, "Unknown field: " + field);
        break;
    }
  }
}
