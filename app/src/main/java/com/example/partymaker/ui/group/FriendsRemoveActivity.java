package com.example.partymaker.ui.group;

import static com.example.partymaker.utilities.Common.hideViews;
import static com.example.partymaker.utilities.Common.showViews;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.firebase.DatabaseFactory;
import com.example.partymaker.data.firebase.DatabaseService;
import com.example.partymaker.data.firebase.ServerDataSnapshot;
import com.example.partymaker.data.model.ServerResponse;
import com.example.partymaker.data.model.User;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class FriendsRemoveActivity extends AppCompatActivity {
  private static final String TAG = "FriendsRemoveActivity";
  private static final String USERS_DB_PATH = "Users";
  private static final String GROUPS_DB_PATH = "Groups";
  
  private Button btnHide, btnHelp, btnDeleteFriend;
  private TextView tvHide, tvHelp, tvInstructions1;
  private EditText etFriendEmail;
  private HashMap<String, Object> FriendKeys, ComingKeys, MessageKeys;
  private String GroupKey,
      CurrentFriend,
      GroupName,
      GroupDay,
      GroupMonth,
      GroupYear,
      GroupHour,
      GroupLocation,
      AdminKey,
      CreatedAt,
      GroupPrice;
  private int GroupType;
  private ImageButton btnBack;
  private boolean CanAdd;
  private DatabaseService databaseService;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_party_friends_remove);

    // this 3 lines disables the action bar only in this activity
    ActionBar actionBar = getSupportActionBar();
    assert actionBar != null;
    actionBar.hide();

    // Get Values from MainActivity By intent + connection between intent and
    // current activity objects
    ExtrasMetadata extras = Common.getExtrasMetadataFromIntent(getIntent());
    if (extras == null) {
      Toast.makeText(this, "Missing intent data", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }
    GroupName = extras.getGroupName();
    GroupKey = extras.getGroupKey();
    GroupDay = extras.getGroupDays();
    GroupMonth = extras.getGroupMonths();
    GroupYear = extras.getGroupYears();
    GroupHour = extras.getGroupHours();
    GroupLocation = extras.getGroupLocation();
    AdminKey = extras.getAdminKey();
    CreatedAt = extras.getCreatedAt();
    GroupPrice = extras.getGroupPrice();
    GroupType = extras.getGroupType();
    CanAdd = extras.isCanAdd();
    FriendKeys = extras.getFriendKeys();
    ComingKeys = extras.getComingKeys();
    MessageKeys = extras.getMessageKeys();

    // Initialize database service
    databaseService = DatabaseFactory.getDatabaseService(this);

    // connection
    btnDeleteFriend = findViewById(R.id.btnDeleteFriend);
    btnHide = findViewById(R.id.btnHide3);
    btnHelp = findViewById(R.id.btnHelp3);
    tvHide = findViewById(R.id.tvHide3);
    tvHelp = findViewById(R.id.tvHelp3);
    tvInstructions1 = findViewById(R.id.tvInstructions3);
    etFriendEmail = findViewById(R.id.etDeleteEmail);
    btnBack = findViewById(R.id.btnBack4);

    EventHandler();
  }

  private void EventHandler() {
    btnHelp.setOnClickListener(
        v -> {
          showViews(tvInstructions1, btnHide, tvHide);
          hideViews(btnHelp, tvHelp);
        });

    btnHide.setOnClickListener(
        v -> {
          showViews(btnHelp, tvHelp);
          hideViews(tvInstructions1, btnHide, tvHide);
        });
        
    btnDeleteFriend.setOnClickListener(
        v -> {
          // This if - checks if EditText is not Empty
          if (!etFriendEmail.getText().toString().trim().isEmpty()) {
            CurrentFriend = etFriendEmail.getText().toString().replace('.', ' ');
            
            // Get users from server
            CompletableFuture<ServerResponse<Object>> future = databaseService.getChildren(USERS_DB_PATH);
            future.thenAccept(response -> {
              if (response != null && response.isSuccess() && response.getData() != null) {
                ServerDataSnapshot dataSnapshot = new ServerDataSnapshot(response.getData());
                processUserData(dataSnapshot);
              } else {
                Log.e(TAG, "Error retrieving user data: " + (response != null ? response.getMessage() : "null response"));
                showToast("Could not retrieve user data");
              }
            }).exceptionally(e -> {
              Log.e(TAG, "Error retrieving user data", e);
              showToast("Could not retrieve user data");
              return null;
            });
          } else {
            showToast("Input email please");
          }
        });
        
    btnBack.setOnClickListener(v -> finish());
  }
  
  private void processUserData(ServerDataSnapshot dataSnapshot) {
    boolean userFound = false;
    boolean userInGroup = false;
    
    // Check if FriendKeys is null and initialize if needed
    if (FriendKeys == null) {
      FriendKeys = new HashMap<>();
      Log.w(TAG, "FriendKeys was null in processUserData(), initialized with empty HashMap");
      showToast("No friends in group");
      return;
    }
    
    // Check if ComingKeys is null and initialize if needed
    if (ComingKeys == null) {
      ComingKeys = new HashMap<>();
      Log.w(TAG, "ComingKeys was null in processUserData(), initialized with empty HashMap");
    }
    
    Map<String, ServerDataSnapshot> children = dataSnapshot.getChildren();
    for (Map.Entry<String, ServerDataSnapshot> entry : children.entrySet()) {
      try {
        ServerDataSnapshot userSnapshot = entry.getValue();
        User user = userSnapshot.getValue(User.class);
        
        if (user != null) {
          String userEmail = user.getEmail().replace('.', ' ');
          String currentUserEmail = etFriendEmail.getText().toString().replace('.', ' ');
          
          if (currentUserEmail.equals(userEmail)) {
            userFound = true;
            
            // Check if user is in group
            if (FriendKeys.containsKey(currentUserEmail)) {
              userInGroup = true;
              
              // Remove user from group
              FriendKeys.remove(CurrentFriend);
              String friendKeysPath = GROUPS_DB_PATH + "/" + GroupKey + "/FriendKeys";
              
              // First remove all friend keys
              databaseService.removeValue(friendKeysPath)
                .thenAccept(removeResponse -> {
                  if (removeResponse != null && removeResponse.isSuccess()) {
                    // Then update with new friend keys (without the removed user)
                    databaseService.updateChildren(friendKeysPath, FriendKeys)
                      .thenAccept(updateResponse -> {
                        if (updateResponse != null && updateResponse.isSuccess()) {
                          // Now remove from coming keys if present
                          if (ComingKeys.containsKey(CurrentFriend)) {
                            ComingKeys.remove(CurrentFriend);
                            String comingKeysPath = GROUPS_DB_PATH + "/" + GroupKey + "/ComingKeys";
                            
                            databaseService.removeValue(comingKeysPath)
                              .thenAccept(removeComingResponse -> {
                                if (removeComingResponse != null && removeComingResponse.isSuccess()) {
                                  databaseService.updateChildren(comingKeysPath, ComingKeys)
                                    .thenAccept(updateComingResponse -> {
                                      if (updateComingResponse != null && updateComingResponse.isSuccess()) {
                                        showToast("Friend successfully deleted");
                                      } else {
                                        Log.e(TAG, "Error updating coming keys: " + 
                                            (updateComingResponse != null ? updateComingResponse.getMessage() : "null response"));
                                      }
                                    });
                                } else {
                                  Log.e(TAG, "Error removing coming keys: " + 
                                      (removeComingResponse != null ? removeComingResponse.getMessage() : "null response"));
                                }
                              });
                          } else {
                            showToast("Friend successfully deleted");
                          }
                        } else {
                          Log.e(TAG, "Error updating friend keys: " + 
                              (updateResponse != null ? updateResponse.getMessage() : "null response"));
                          showToast("Failed to delete friend");
                        }
                      });
                  } else {
                    Log.e(TAG, "Error removing friend keys: " + 
                        (removeResponse != null ? removeResponse.getMessage() : "null response"));
                    showToast("Failed to delete friend");
                  }
                }).exceptionally(e -> {
                  Log.e(TAG, "Error removing friend", e);
                  showToast("Failed to delete friend");
                  return null;
                });
            } else {
              showToast("User not in group");
            }
            break;
          }
        }
      } catch (Exception e) {
        Log.e(TAG, "Error processing user data", e);
      }
    }
    
    if (!userFound) {
      showToast("User not found");
    }
  }
  
  private void showToast(String message) {
    runOnUiThread(() -> Toast.makeText(FriendsRemoveActivity.this, message, Toast.LENGTH_SHORT).show());
  }
}
