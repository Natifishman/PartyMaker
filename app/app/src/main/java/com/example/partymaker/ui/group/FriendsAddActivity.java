package com.example.partymaker.ui.group;

import static com.example.partymaker.utilities.Common.hideViews;
import static com.example.partymaker.utilities.Common.showViews;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.partymaker.data.model.User;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Objects;

public class FriendsAddActivity extends AppCompatActivity {
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
  private DatabaseReference database;

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

    // database set
    database = FirebaseDatabase.getInstance().getReference("Users");

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
            database.addValueEventListener(
                new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean flag = false;
                    boolean flag1 = false;
                    HashMap<String, Object> GroupFriends;
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                      String UserEmail =
                          Objects.requireNonNull(data.getValue(User.class))
                              .getEmail()
                              .replace('.', ' ');
                      String CurrentUserEmail =
                          etFriendEmail.getText().toString().replace('.', ' ');
                      GroupFriends = FriendKeys;
                      if (CurrentUserEmail.equals(UserEmail)) {
                        for (String GroupFriend : GroupFriends.keySet()) {
                          if (CurrentUserEmail.equals(GroupFriend)) {
                            flag1 = true;
                            break;
                          }
                        }
                        if (!flag1) {
                          FriendKeys.put(CurrentFriend, "true");
                          DBRef.refGroups
                              .child(GroupKey)
                              .child("FriendKeys")
                              .updateChildren(FriendKeys);
                          Toast.makeText(
                                  FriendsAddActivity.this,
                                  "Friend successfully added",
                                  Toast.LENGTH_SHORT)
                              .show();
                        } else {
                          Toast.makeText(
                                  FriendsAddActivity.this,
                                  "User already in group",
                                  Toast.LENGTH_SHORT)
                              .show();
                        }
                        flag = true;
                      }
                    }
                    if (!flag) {
                      Toast.makeText(FriendsAddActivity.this, "Email not Exist", Toast.LENGTH_SHORT)
                          .show();
                    }
                    if (!flag1 && flag) {
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
                    }
                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
          } else {
            Toast.makeText(FriendsAddActivity.this, "Input email please", Toast.LENGTH_SHORT)
                .show();
          }
        });
    btnFriendsList.setOnClickListener(
        v -> {
          Intent intent = new Intent(FriendsAddActivity.this, UsersListActivity.class);
          startActivity(intent);
        });
    btnYes.setOnClickListener(
        v -> {

          // add Friend to coming list
          ComingKeys.put(CurrentFriend, "true");
          DBRef.refGroups.child(GroupKey).child("ComingKeys").updateChildren(FriendKeys);
          Toast.makeText(FriendsAddActivity.this, "Added to Coming List", Toast.LENGTH_SHORT)
              .show();

          showViews(etFriendEmail, btnAddFriend, btnFriendsList, btnHelp, tvHelp);
          hideViews(tvInstructions1, btnHide, tvHide, tvAddMore, btnYes, btnNo);
        });

    btnNo.setOnClickListener(
        v -> {
          showViews(etFriendEmail, btnAddFriend, btnFriendsList, btnHelp, tvHelp);
          hideViews(tvInstructions1, btnHide, tvHide, tvAddMore, btnYes, btnNo);
        });
    btnBack.setOnClickListener(
        v -> {
          Intent intent = new Intent(getBaseContext(), GroupDetailsActivity.class);
          ExtrasMetadata extras =
              new ExtrasMetadata(
                  GroupName,
                  GroupKey,
                  GroupDay,
                  GroupMonth,
                  GroupYear,
                  GroupHour,
                  GroupLocation,
                  AdminKey,
                  CreatedAt,
                  GroupPrice,
                  GroupType,
                  CanAdd,
                  FriendKeys,
                  ComingKeys,
                  MessageKeys);
          Common.addExtrasToIntent(intent, extras);
          startActivity(intent);
          startActivity(intent);
        });
  }
}
