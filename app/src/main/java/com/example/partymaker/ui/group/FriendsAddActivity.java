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

public class FriendsAddActivity extends AppCompatActivity {
  private static final String TAG = "FriendsAddActivity";
  private static final String USERS_DB_PATH = "Users";
  private static final String GROUPS_DB_PATH = "Groups";
  
  private Button btnHide, btnHelp, btnAddFriend, btnYes, btnNo, btnFriendsList;
  private TextView tvHide, tvHelp, tvInstructions1, tvAddMore;
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
    setContentView(R.layout.activity_party_friends_add);

    // this 2 lines disables the action bar only in this activity
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
    btnAddFriend = findViewById(R.id.btnAddFriend);
    btnFriendsList = findViewById(R.id.btnFriendsList);
    btnHide = findViewById(R.id.btnHide1);
    btnHelp = findViewById(R.id.btnHelp1);
    btnYes = findViewById(R.id.btnYes);
    btnNo = findViewById(R.id.btnNo);
    tvHide = findViewById(R.id.tvHide1);
    tvHelp = findViewById(R.id.tvHelp1);
    tvAddMore = findViewById(R.id.tvAddMore);
    tvInstructions1 = findViewById(R.id.tvInstructions1);
    etFriendEmail = findViewById(R.id.etFriendEmail);
    btnBack = findViewById(R.id.btnBack3);

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

    btnAddFriend.setOnClickListener(
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
        
    btnFriendsList.setOnClickListener(
        v -> {
          Intent intent = new Intent(FriendsAddActivity.this, UsersListActivity.class);
          startActivity(intent);
        });
        
    btnYes.setOnClickListener(
        v -> {
          // Check if ComingKeys is null and initialize if needed
          if (ComingKeys == null) {
            ComingKeys = new HashMap<>();
            Log.w(TAG, "ComingKeys was null, initialized with empty HashMap");
          }
          
          // add Friend to coming list
          ComingKeys.put(CurrentFriend, "true");
          
          // Update coming keys on server
          String comingKeysPath = GROUPS_DB_PATH + "/" + GroupKey + "/ComingKeys";
          databaseService.updateChildren(comingKeysPath, ComingKeys)
            .thenAccept(response -> {
              if (response != null && response.isSuccess()) {
                showToast("Friend added to coming list");
                
                // Reset UI
                showViews(etFriendEmail, btnAddFriend, btnFriendsList, btnHelp, tvHelp);
                hideViews(tvAddMore, btnYes, btnNo);
                etFriendEmail.setText("");
              } else {
                Log.e(TAG, "Error updating coming keys: " + (response != null ? response.getMessage() : "null response"));
                showToast("Failed to add friend to coming list");
              }
            }).exceptionally(e -> {
              Log.e(TAG, "Error updating coming keys", e);
              showToast("Failed to add friend to coming list");
              return null;
            });
        });
        
    btnNo.setOnClickListener(
        v -> {
          // Reset UI
          showViews(etFriendEmail, btnAddFriend, btnFriendsList, btnHelp, tvHelp);
          hideViews(tvAddMore, btnYes, btnNo);
          etFriendEmail.setText("");
        });
        
    btnBack.setOnClickListener(v -> finish());
  }
  
  private void processUserData(ServerDataSnapshot dataSnapshot) {
    boolean userFound = false;
    boolean userAlreadyInGroup = false;
    
    // Check if FriendKeys is null and initialize if needed
    if (FriendKeys == null) {
      FriendKeys = new HashMap<>();
      Log.w(TAG, "FriendKeys was null in processUserData(), initialized with empty HashMap");
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
            
            // Check if user is already in group
            if (FriendKeys.containsKey(currentUserEmail)) {
              userAlreadyInGroup = true;
              break;
            }
            
            // Add user to group
            FriendKeys.put(CurrentFriend, "true");
            String friendKeysPath = GROUPS_DB_PATH + "/" + GroupKey + "/FriendKeys";
            
            databaseService.updateChildren(friendKeysPath, FriendKeys)
              .thenAccept(response -> {
                if (response != null && response.isSuccess()) {
                  showToast("Friend successfully added");
                  
                  // Update UI to ask about adding to coming list
                  runOnUiThread(() -> {
                    hideViews(
                        etFriendEmail,
                        btnAddFriend,
                        btnFriendsList,
                        tvInstructions1,
                        btnHide,
                        tvHide,
                        btnHelp,
                        tvHelp);
                    showViews(tvAddMore, btnYes, btnNo);
                  });
                } else {
                  Log.e(TAG, "Error updating friend keys: " + (response != null ? response.getMessage() : "null response"));
                  showToast("Failed to add friend");
                }
              }).exceptionally(e -> {
                Log.e(TAG, "Error updating friend keys", e);
                showToast("Failed to add friend");
                return null;
              });
            
            break;
          }
        }
      } catch (Exception e) {
        Log.e(TAG, "Error processing user data", e);
      }
    }
    
    if (!userFound) {
      showToast("User not found");
    } else if (userAlreadyInGroup) {
      showToast("User already in group");
    }
  }
  
  private void showToast(String message) {
    runOnUiThread(() -> Toast.makeText(FriendsAddActivity.this, message, Toast.LENGTH_SHORT).show());
  }
}
