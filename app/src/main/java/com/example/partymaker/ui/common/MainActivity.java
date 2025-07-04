package com.example.partymaker.ui.common;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.firebase.DatabaseFactory;
import com.example.partymaker.data.firebase.DatabaseService;
import com.example.partymaker.data.firebase.FirebaseConfig;
import com.example.partymaker.data.firebase.ServerDataSnapshot;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.data.model.ServerResponse;
import com.example.partymaker.ui.adapters.GroupAdapter;
import com.example.partymaker.ui.auth.LoginActivity;
import com.example.partymaker.ui.chatbot.GptChatActivity;
import com.example.partymaker.ui.group.CreateGroupActivity;
import com.example.partymaker.ui.group.GroupDetailsActivity;
import com.example.partymaker.ui.group.PublicGroupsActivity;
import com.example.partymaker.ui.profile.EditProfileActivity;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {

  // Constants
  private static final String TAG = "MainActivity";
  private static final String ACTION_BAR_START_COLOR = "#0E81D1";
  private static final String ACTION_BAR_END_COLOR = "#0E81D1";
  private static final String ACTION_BAR_TITLE_COLOR = "#FFFFFF";
  private static final float ACTION_BAR_ELEVATION = 15f;
  private static final String GROUPS_DB_PATH = "Groups";

  // UI Components
  private ListView lv1;
  private FloatingActionButton fabChat;

  // Data Components
  private DatabaseService databaseService;
  private ArrayList<Group> groupList;
  private String UserKey;

  @SuppressLint("ClickableViewAccessibility")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Ensure database connection (will try emulator if needed)
    boolean dbConnected = FirebaseConfig.ensureDatabaseConnection(this);
    Log.d(TAG, "Database connection status: " + (dbConnected ? "Connected" : "Offline mode"));
    
    // If we're in offline mode, show a notification to the user
    if (!dbConnected) {
      Toast.makeText(this, "Working in offline mode. Some features may be limited.", Toast.LENGTH_LONG).show();
    }

    if (!initializeUser()) {
      return; // Exit if user initialization failed
    }

    initializeViews();
    setupActionBar();
    initializeDatabase();
    setupEventHandlers();
    setupFloatingChatButton();
  }

  /**
   * Initializes and validates the current user.
   *
   * @return true if user is valid and initialized, false otherwise
   */
  private boolean initializeUser() {
    try {
      FirebaseUser currentUser = DBRef.Auth.getCurrentUser();
      if (currentUser == null || currentUser.getEmail() == null) {
        Log.e(TAG, "User not logged in or email is null");
        showError("Authentication error. Please login again.");
        navigateToLogin();
        return false;
      }

      UserKey = currentUser.getEmail().replace('.', ' ');
      DBRef.CurrentUser = UserKey;
      Log.d(TAG, "User initialized successfully");
      return true;

    } catch (Exception e) {
      Log.e(TAG, "Error initializing user", e);
      showError("User initialization failed");
      navigateToLogin();
      return false;
    }
  }

  /**
   * Check if running on emulator
   *
   * @return true if running on emulator
   */
  private boolean isEmulator() {
    return android.os.Build.MODEL.contains("sdk_gphone")
        || android.os.Build.MODEL.contains("Emulator")
        || android.os.Build.PRODUCT.contains("sdk")
        || android.os.Build.HARDWARE.contains("ranchu");
  }

  // Initializes all view components.
  private void initializeViews() {
    lv1 = findViewById(R.id.lv1);
    fabChat = findViewById(R.id.fabChat);

    if (lv1 == null) {
      Log.e(TAG, "Critical view lv1 not found");
      showError("UI initialization failed");
      finish();
    }
  }

  // Sets up the action bar with custom gradient background and styling.
  private void setupActionBar() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar == null) {
      Log.w(TAG, "ActionBar not available");
      return;
    }

    try {
      GradientDrawable gradient = createActionBarGradient();
      actionBar.setBackgroundDrawable(gradient);

      String styledTitle = createStyledTitle();
      actionBar.setTitle(Html.fromHtml(styledTitle, Html.FROM_HTML_MODE_LEGACY));

      configureActionBarProperties(actionBar);

      Log.d(TAG, "ActionBar setup completed");

    } catch (Exception e) {
      Log.e(TAG, "Error setting up ActionBar", e);
    }
  }

  // Creates a gradient drawable for the action bar background.
  private GradientDrawable createActionBarGradient() {
    GradientDrawable gradient = new GradientDrawable();
    gradient.setShape(GradientDrawable.RECTANGLE);
    gradient.setColors(
        new int[] {
          Color.parseColor(ACTION_BAR_START_COLOR), Color.parseColor(ACTION_BAR_END_COLOR)
        });
    gradient.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
    return gradient;
  }

  // Creates a styled HTML title string.
  private String createStyledTitle() {
    return String.format(
        "<font color='%s'><b>%s</b></font>", MainActivity.ACTION_BAR_TITLE_COLOR, "My Parties");
  }

  // Configures action bar properties.
  private void configureActionBarProperties(ActionBar actionBar) {
    actionBar.setElevation(ACTION_BAR_ELEVATION);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(false);
  }

  // Initializes the database service and retrieves data.
  private void initializeDatabase() {
    try {
      // Get database service from factory
      databaseService = DatabaseFactory.getDatabaseService(this);
      
      retrieveGroupData();
      Log.d(TAG, "Database initialized successfully");

    } catch (Exception e) {
      Log.e(TAG, "Error initializing database", e);
      showError("Database connection failed");
    }
  }

  // Sets up event handlers for UI components.
  private void setupEventHandlers() {
    lv1.setOnItemClickListener(
        (parent, view, position, id) -> {
          if (isValidPosition(position)) {
            navigateToGroupScreen(groupList.get(position));
          }
        });
  }

  @SuppressLint("ClickableViewAccessibility")
  private void setupFloatingChatButton() {
    fabChat.setOnClickListener(v -> navigateToChat());
  }

  private void navigateToChat() {
    try {
      Intent intent = new Intent(MainActivity.this, GptChatActivity.class);
      startActivity(intent);
    } catch (Exception e) {
      Log.e(TAG, "Error navigating to chat", e);
      showError("Could not open chat");
    }
  }

  private void navigateToGroupScreen(Group group) {
    try {
      Intent intent = new Intent(MainActivity.this, GroupDetailsActivity.class);
      ExtrasMetadata extras = createExtrasFromGroup(group);
      extras.addToIntent(intent);
      startActivity(intent);
    } catch (Exception e) {
      Log.e(TAG, "Error navigating to group screen", e);
      showError("Could not open group details");
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

  public void retrieveGroupData() {
    groupList = new ArrayList<>();
    
    CompletableFuture<ServerResponse<Object>> future = databaseService.getChildren(GROUPS_DB_PATH);
    future.thenAccept(response -> {
      if (response != null && response.isSuccess() && response.getData() != null) {
        ServerDataSnapshot dataSnapshot = new ServerDataSnapshot(response.getData());
        processGroupData(dataSnapshot);
      } else {
        Log.e(TAG, "Error retrieving group data: " + (response != null ? response.getMessage() : "null response"));
        showError("Could not retrieve group data");
      }
    }).exceptionally(e -> {
      Log.e(TAG, "Error retrieving group data", e);
      showError("Could not retrieve group data");
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
          
          if (isUserInGroup(group)) {
            groupList.add(group);
          }
        }
      } catch (Exception e) {
        Log.e(TAG, "Error processing group data", e);
      }
    }
    
    sortAndDisplayGroups();
  }
  
  private Group createGroupFromData(HashMap<String, Object> data) {
    Group group = new Group();
    
    // Set basic properties
    group.setGroupName((String) data.get("groupName"));
    group.setAdminKey((String) data.get("adminKey"));
    group.setGroupLocation((String) data.get("groupLocation"));
    group.setGroupDays((String) data.get("groupDays"));
    group.setGroupMonths((String) data.get("groupMonths"));
    group.setGroupYears((String) data.get("groupYears"));
    group.setGroupHours((String) data.get("groupHours"));
    group.setGroupPrice((String) data.get("groupPrice"));
    group.setCreatedAt((String) data.get("createdAt"));
    
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
    
    return group;
  }

  private boolean isUserInGroup(Group group) {
    if (group == null || group.getFriendKeys() == null || UserKey == null) {
      return false;
    }
    
    return group.getFriendKeys().containsKey(UserKey) || 
           (group.getAdminKey() != null && group.getAdminKey().equals(UserKey));
  }

  private void sortAndDisplayGroups() {
    // Sort groups by date (year, month, day)
    groupList.sort((g1, g2) -> {
      // Compare years
      int yearComparison = g2.getGroupYears().compareTo(g1.getGroupYears());
      if (yearComparison != 0) return yearComparison;
      
      // If years are the same, compare months
      int monthComparison = g2.getGroupMonths().compareTo(g1.getGroupMonths());
      if (monthComparison != 0) return monthComparison;
      
      // If months are the same, compare days
      return g2.getGroupDays().compareTo(g1.getGroupDays());
    });
    
    // Display groups in ListView
    GroupAdapter adapter = new GroupAdapter(this, groupList);
    lv1.setAdapter(adapter);
  }

  private boolean isValidPosition(int position) {
    return position >= 0 && position < groupList.size();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_create_group) {
      navigateToActivity(CreateGroupActivity.class);
      return true;
    } else if (id == R.id.action_public_groups) {
      navigateToActivity(PublicGroupsActivity.class);
      return true;
    } else if (id == R.id.action_edit_profile) {
      navigateToActivity(EditProfileActivity.class);
      return true;
    } else if (id == R.id.action_refresh) {
      retrieveGroupData();
      return true;
    } else if (id == R.id.action_logout) {
      handleLogout();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private void navigateToActivity(Class<?> activityClass) {
    Intent intent = new Intent(MainActivity.this, activityClass);
    startActivity(intent);
  }

  private void handleLogout() {
    try {
      // Sign out from Firebase
      DBRef.Auth.signOut();
      
      // Clear any stored user data
      DBRef.CurrentUser = null;
      
      // Navigate to login screen
      navigateToLogin();
      
      // Finish this activity
      finish();
    } catch (Exception e) {
      Log.e(TAG, "Error during logout", e);
      showError("Logout failed");
    }
  }

  private void navigateToLogin() {
    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
  }

  private void showError(String message) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }
}
