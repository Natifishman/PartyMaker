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

public class FriendsRemoveActivity extends AppCompatActivity {
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
  private DatabaseReference database;

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

    // database set
    database = FirebaseDatabase.getInstance().getReference("Users");

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
                        if (flag1) {
                          FriendKeys.remove(CurrentFriend);
                          DBRef.refGroups.child(GroupKey).child("FriendKeys").removeValue();
                          DBRef.refGroups
                              .child(GroupKey)
                              .child("FriendKeys")
                              .updateChildren(FriendKeys);
                          ComingKeys.remove(CurrentFriend);
                          DBRef.refGroups.child(GroupKey).child("ComingKeys").removeValue();
                          DBRef.refGroups
                              .child(GroupKey)
                              .child("ComingKeys")
                              .updateChildren(ComingKeys);
                          Toast.makeText(
                                  FriendsRemoveActivity.this,
                                  "Friend successfully deleted",
                                  Toast.LENGTH_SHORT)
                              .show();
                        } else {
                          Toast.makeText(
                                  FriendsRemoveActivity.this,
                                  "User not in group",
                                  Toast.LENGTH_SHORT)
                              .show();
                        }
                        flag = true;
                      }
                    }
                    if (!flag) {
                      Toast.makeText(
                              FriendsRemoveActivity.this, "Email not Exist", Toast.LENGTH_SHORT)
                          .show();
                    }
                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
          } else {
            Toast.makeText(FriendsRemoveActivity.this, "Input email please", Toast.LENGTH_SHORT)
                .show();
          }
        });
    btnBack.setOnClickListener(
        v -> {
          Intent intent = new Intent(getBaseContext(), AdminOptionsActivity.class);
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
        });
  }
}
