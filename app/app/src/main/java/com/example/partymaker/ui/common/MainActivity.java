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
import com.example.partymaker.data.model.Group;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

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
  private DatabaseReference database;
  private ArrayList<Group> groupList;
  private String UserKey;

  @SuppressLint("ClickableViewAccessibility")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

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
      Log.d(TAG, "User initialized successfully");
      return true;

    } catch (Exception e) {
      Log.e(TAG, "Error initializing user", e);
      showError("User initialization failed");
      navigateToLogin();
      return false;
    }
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

  // Initializes the Firebase database reference and retrieves data.
  private void initializeDatabase() {
    try {
      database = FirebaseDatabase.getInstance().getReference(GROUPS_DB_PATH);
      retrieveGroupData();
      Log.d(TAG, "Database initialized successfully");

    } catch (Exception e) {
      Log.e(TAG, "Error initializing database", e);
      showError("Database connection failed");
    }
  }

  // Sets up all event handlers for UI components.
  private void setupEventHandlers() {
    if (lv1 != null) {
      lv1.setOnItemClickListener(
          (parent, view, position, id) -> {
            if (isValidPosition(position)) {
              navigateToGroupScreen(groupList.get(position));
            }
          });
    }
  }

  // Sets up the floating chat button with click and touch handlers.
  @SuppressLint("ClickableViewAccessibility")
  private void setupFloatingChatButton() {
    if (fabChat == null) {
      Log.w(TAG, "FloatingActionButton not found");
      return;
    }

    fabChat.setOnClickListener(view -> navigateToChat());
    fabChat.setOnTouchListener(Common::dragChatButtonOnTouch);
  }

  // Navigates to the chat activity.
  private void navigateToChat() {
    try {
      Intent intent = new Intent(MainActivity.this, GptChatActivity.class);
      startActivity(intent);
    } catch (Exception e) {
      Log.e(TAG, "Error navigating to chat", e);
      showError("Failed to open chat");
    }
  }

  // Navigates to the group screen with all necessary data.
  private void navigateToGroupScreen(Group group) {
    if (group == null) {
      Log.e(TAG, "Cannot navigate to null group");
      return;
    }

    try {
      Intent intent = new Intent(getBaseContext(), GroupDetailsActivity.class);
      ExtrasMetadata extras = createExtrasFromGroup(group);
      Common.addExtrasToIntent(intent, extras);
      startActivity(intent);

    } catch (Exception e) {
      Log.e(TAG, "Error navigating to group screen", e);
      showError("Failed to open group");
    }
  }

  // Creates ExtrasMetadata object from Group data.
  private ExtrasMetadata createExtrasFromGroup(Group group) {
    return new ExtrasMetadata(
        group.getGroupName(),
        group.getGroupKey(),
        group.getGroupDays(),
        group.getGroupMonths(),
        group.getGroupYears(),
        group.getGroupHours(),
        group.getGroupLocation(),
        group.getAdminKey(),
        group.getCreatedAt(),
        group.getGroupPrice(),
        group.getGroupType(),
        group.isCanAdd(),
        group.getFriendKeys(),
        group.getComingKeys(),
        group.getMessageKeys());
  }

  // Retrieves group data from Firebase database.
  public void retrieveGroupData() {
    database.addValueEventListener(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            try {
              processGroupData(dataSnapshot);
            } catch (Exception e) {
              Log.e(TAG, "Error processing group data", e);
              showError("Failed to load groups");
            }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(TAG, "Database error: " + databaseError.getMessage());
          }
        });
  }

  // Processes the retrieved group data and updates the UI.
  private void processGroupData(DataSnapshot dataSnapshot) {
    groupList = new ArrayList<>();

    for (DataSnapshot data : dataSnapshot.getChildren()) {
      Group group = data.getValue(Group.class);
      if (group != null && isUserInGroup(group)) {
        groupList.add(group);
      }
    }

    sortAndDisplayGroups();
  }

  // Checks if the current user is a member of the given group.
  private boolean isUserInGroup(Group group) {
    HashMap<String, Object> userKeys = group.getFriendKeys();
    if (userKeys == null || UserKey == null) {
      return false;
    }

    return userKeys.containsKey(UserKey);
  }

  // Sorts groups by creation date and updates the adapter.
  private void sortAndDisplayGroups() {
    if (groupList != null) {
      groupList.sort(Comparator.comparing(Group::getCreatedAt));
      GroupAdapter allGroupsAdapter = new GroupAdapter(MainActivity.this, 0, 0, groupList);
      lv1.setAdapter(allGroupsAdapter);
    }
  }

  // Validates if the given position is within bounds.
  private boolean isValidPosition(int position) {
    return groupList != null && position >= 0 && position < groupList.size();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();

    try {
      if (itemId == R.id.idMenu) {
        navigateToActivity(MainActivity.class);
      } else if (itemId == R.id.idAddProfile) {
        navigateToActivity(CreateGroupActivity.class);
      } else if (itemId == R.id.idEditProfile) {
        navigateToActivity(EditProfileActivity.class);
      } else if (itemId == R.id.idPublicParties) {
        navigateToActivity(PublicGroupsActivity.class);
      } else if (itemId == R.id.idLogout) {
        handleLogout();
      }
    } catch (Exception e) {
      Log.e(TAG, "Error handling menu selection", e);
      showError("Menu action failed");
    }

    return true;
  }

  // Navigates to the specified activity class.
  private void navigateToActivity(Class<?> activityClass) {
    Intent intent = new Intent(getApplicationContext(), activityClass);
    startActivity(intent);
  }

  // Handles user logout process
  private void handleLogout() {
    try {
      DBRef.Auth.signOut();
      DBRef.CurrentUser = null;
      navigateToLogin();
      Log.d(TAG, "User logged out successfully");
    } catch (Exception e) {
      Log.e(TAG, "Error during logout", e);
      showError("Logout failed");
    }
  }

  // Navigates to the login activity and finishes current activity.
  private void navigateToLogin() {
    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
    startActivity(intent);
    finish();
  }

  // Shows an error message to the user.
  private void showError(String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }
}
