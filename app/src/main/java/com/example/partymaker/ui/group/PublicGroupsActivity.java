package com.example.partymaker.ui.group;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DatabaseFactory;
import com.example.partymaker.data.firebase.DatabaseService;
import com.example.partymaker.data.firebase.ServerDataSnapshot;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.data.model.ServerResponse;
import com.example.partymaker.ui.adapters.GroupAdapter;
import com.example.partymaker.ui.auth.LoginActivity;
import com.example.partymaker.ui.common.MainActivity;
import com.example.partymaker.ui.profile.EditProfileActivity;
import com.example.partymaker.utilities.ExtrasMetadata;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class PublicGroupsActivity extends AppCompatActivity {
  private static final String TAG = "PublicGroupsActivity";
  private static final String GROUPS_DB_PATH = "Groups";
  private static final String USERS_DB_PATH = "Users";
  private static final String CURRENT_USER_PATH = "CurrentUser";
  private static final String LOGOUT_PATH = "logout";
  
  private ListView lv1;
  private DatabaseService databaseService;
  private ArrayList<Group> groupList;
  private String userKey;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_public_parties);

    // Change title Name and Color
    ActionBar actionBar = getSupportActionBar();
    Objects.requireNonNull(actionBar)
        .setTitle(Html.fromHtml("<font color='#E4E9EF'>Public Parties</font>"));

    // set actionbar background
    @SuppressLint("UseCompatLoadingForDrawables")
    Drawable d = getResources().getDrawable(R.color.primaryBlue);
    actionBar.setBackgroundDrawable(d);

    // Initialize UI components
    lv1 = findViewById(R.id.lv5);
    
    // Initialize database service
    databaseService = DatabaseFactory.getDatabaseService(this);
    
    // Get current user key
    getCurrentUserKey();
    
    // Initialize group list
    groupList = new ArrayList<>();
  }

  @Override
  protected void onResume() {
    super.onResume();
    // Retrieve data and set up event handlers when activity resumes
    retrieveData();
    setupEventHandlers();
  }

  private void getCurrentUserKey() {
    CompletableFuture<ServerResponse<Object>> userFuture = databaseService.getValue(CURRENT_USER_PATH);
    userFuture.thenAccept(response -> {
      if (response != null && response.isSuccess() && response.getData() != null) {
        ServerDataSnapshot snapshot = new ServerDataSnapshot(response.getData());
        String email = snapshot.child("email").getValue(String.class);
        if (email != null) {
          userKey = email.replace('.', ' ');
          // Once we have the user key, retrieve data
          retrieveData();
        } else {
          Log.e(TAG, "User email is null");
          showToast("Error retrieving user information");
        }
      } else {
        Log.e(TAG, "User data response is null or failed");
        showToast("Error retrieving user information");
      }
    }).exceptionally(e -> {
      Log.e(TAG, "Error getting current user", e);
      showToast("Error retrieving user information");
      return null;
    });
  }

  private void setupEventHandlers() {
    lv1.setOnItemClickListener(
        (parent, view, position, id) -> {
          if (position >= 0 && position < groupList.size()) {
            Group selectedGroup = groupList.get(position);
            navigateToJoinGroupActivity(selectedGroup);
          }
        });
  }
  
  private void navigateToJoinGroupActivity(Group group) {
    try {
      Intent intent = new Intent(this, JoinGroupActivity.class);
      ExtrasMetadata extras = createExtrasFromGroup(group);
      extras.addToIntent(intent);
      startActivity(intent);
    } catch (Exception e) {
      Log.e(TAG, "Error navigating to join group activity", e);
      showToast("Could not open group details");
    }
  }
  
  private ExtrasMetadata createExtrasFromGroup(Group group) {
    ExtrasMetadata extras = new ExtrasMetadata();
    extras.put("group_id", group.getGroupKey());
    extras.put("group_name", group.getGroupName());
    extras.put("group_admin", group.getAdminKey());
    extras.put("group_location", group.getGroupLocation());
    extras.put("group_date", group.getGroupDays() + "/" + group.getGroupMonths() + "/" + group.getGroupYears());
    extras.put("group_time", group.getGroupHours());
    extras.put("group_public", group.getGroupType() == 0);
    extras.put("group_members", group.getFriendKeys());
    
    return extras;
  }

  public void retrieveData() {
    // Only proceed if we have a valid user key
    if (userKey == null) {
      Log.d(TAG, "User key not available yet, skipping data retrieval");
      return;
    }
    
    groupList.clear();
    
    CompletableFuture<ServerResponse<Object>> future = databaseService.getChildren(GROUPS_DB_PATH);
    future.thenAccept(response -> {
      if (response != null && response.isSuccess() && response.getData() != null) {
        ServerDataSnapshot dataSnapshot = new ServerDataSnapshot(response.getData());
        processGroupData(dataSnapshot);
      } else {
        Log.e(TAG, "Error retrieving group data: response is null or failed");
        showToast("Could not retrieve public groups");
      }
    }).exceptionally(e -> {
      Log.e(TAG, "Error retrieving group data", e);
      showToast("Could not retrieve public groups");
      return null;
    });
  }
  
  private void processGroupData(ServerDataSnapshot dataSnapshot) {
    groupList.clear();
    
    Map<String, ServerDataSnapshot> children = dataSnapshot.getChildren();
    for (Map.Entry<String, ServerDataSnapshot> entry : children.entrySet()) {
      try {
        ServerDataSnapshot groupSnapshot = entry.getValue();
        String groupId = entry.getKey();
        
        // Try to convert the data to a HashMap
        HashMap<String, Object> groupData = groupSnapshot.getValue(HashMap.class);
        if (groupData != null) {
          Group group = createGroupFromData(groupData);
          group.setGroupKey(groupId);
          
          // Only add public groups where the user is not a member
          if (group.getGroupType() == 0 && !isUserInGroup(group)) {
            groupList.add(group);
          }
        }
      } catch (Exception e) {
        Log.e(TAG, "Error processing group data", e);
      }
    }
    
    // Display groups in ListView
    runOnUiThread(() -> {
      if (groupList.isEmpty()) {
        showToast("No public groups available");
      }
      GroupAdapter adapter = new GroupAdapter(this, groupList);
      lv1.setAdapter(adapter);
    });
  }
  
  private Group createGroupFromData(HashMap<String, Object> data) {
    Group group = new Group();
    
    try {
      // Set basic properties
      if (data.get("groupName") instanceof String) {
        group.setGroupName((String) data.get("groupName"));
      }
      
      if (data.get("adminKey") instanceof String) {
        group.setAdminKey((String) data.get("adminKey"));
      }
      
      if (data.get("groupLocation") instanceof String) {
        group.setGroupLocation((String) data.get("groupLocation"));
      }
      
      if (data.get("groupDays") instanceof String) {
        group.setGroupDays((String) data.get("groupDays"));
      }
      
      if (data.get("groupMonths") instanceof String) {
        group.setGroupMonths((String) data.get("groupMonths"));
      }
      
      if (data.get("groupYears") instanceof String) {
        group.setGroupYears((String) data.get("groupYears"));
      }
      
      if (data.get("groupHours") instanceof String) {
        group.setGroupHours((String) data.get("groupHours"));
      }
      
      if (data.get("groupPrice") instanceof String) {
        group.setGroupPrice((String) data.get("groupPrice"));
      }
      
      if (data.get("createdAt") instanceof String) {
        group.setCreatedAt((String) data.get("createdAt"));
      }
      
      // Set group type (public/private)
      if (data.get("GroupType") instanceof Long) {
        group.setGroupType(((Long) data.get("GroupType")).intValue());
      } else if (data.get("GroupType") instanceof Integer) {
        group.setGroupType((Integer) data.get("GroupType"));
      }
      
      // Set CanAdd property
      if (data.get("CanAdd") instanceof Boolean) {
        group.setCanAdd((Boolean) data.get("CanAdd"));
      }
      
      // Set collections
      if (data.get("FriendKeys") instanceof HashMap) {
        @SuppressWarnings("unchecked")
        HashMap<String, Object> friendKeys = (HashMap<String, Object>) data.get("FriendKeys");
        group.setFriendKeys(friendKeys);
      }
      
      if (data.get("ComingKeys") instanceof HashMap) {
        @SuppressWarnings("unchecked")
        HashMap<String, Object> comingKeys = (HashMap<String, Object>) data.get("ComingKeys");
        group.setComingKeys(comingKeys);
      }
      
      if (data.get("MessageKeys") instanceof HashMap) {
        @SuppressWarnings("unchecked")
        HashMap<String, Object> messageKeys = (HashMap<String, Object>) data.get("MessageKeys");
        group.setMessageKeys(messageKeys);
      }
    } catch (Exception e) {
      Log.e(TAG, "Error creating group from data", e);
    }
    
    return group;
  }
  
  private boolean isUserInGroup(Group group) {
    if (group == null || group.getFriendKeys() == null || userKey == null) {
      return false;
    }
    
    return group.getFriendKeys().containsKey(userKey) || 
           (group.getAdminKey() != null && group.getAdminKey().equals(userKey));
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_home) {
      navigateToActivity(MainActivity.class);
      return true;
    } else if (id == R.id.action_create_group) {
      navigateToActivity(CreateGroupActivity.class);
      return true;
    } else if (id == R.id.action_edit_profile) {
      navigateToActivity(EditProfileActivity.class);
      return true;
    } else if (id == R.id.action_refresh) {
      retrieveData();
      return true;
    } else if (id == R.id.action_logout) {
      handleLogout();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
  
  private void navigateToActivity(Class<?> activityClass) {
    Intent intent = new Intent(this, activityClass);
    startActivity(intent);
  }
  
  private void handleLogout() {
    try {
      // Sign out using the database service
      CompletableFuture<ServerResponse<Object>> future = databaseService.getValue(LOGOUT_PATH);
      future.thenAccept(response -> {
        // Navigate to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        
        // Finish this activity
        finish();
      }).exceptionally(e -> {
        Log.e(TAG, "Error during logout", e);
        showToast("Logout failed");
        return null;
      });
    } catch (Exception e) {
      Log.e(TAG, "Error during logout", e);
      showToast("Logout failed");
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }
  
  private void showToast(String message) {
    runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
  }
}
