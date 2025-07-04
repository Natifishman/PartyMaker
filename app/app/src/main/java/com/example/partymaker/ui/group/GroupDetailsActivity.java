package com.example.partymaker.ui.group;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.ChatMessage;
import com.example.partymaker.ui.common.MainActivity;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import com.example.partymaker.utilities.MapUtilities;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GroupDetailsActivity extends AppCompatActivity {
  private ImageView imgCalender;
  private ImageView imgThumbUp;
  private ImageView imgThumbDown;
  private ImageView imgSeeHours;
  private ImageView imgOptions;
  private ImageButton btnEditName;
  private Button back5;
  private TextView tvDateText;
  private TextView tvDateDays;
  private TextView tvDateMonths;
  private TextView tvDateYears;
  private TextView tvDateHours;
  private TextView tvComing;
  private TextView tvNotComing;
  private TextView tvGroupName;
  private TextView tvSeeHours;
  private TextView tvSeeDate;
  private TextView tvAt;
  private TextView tvOptions;
  private CardView card5;
  private String GroupName,
      GroupKey,
      GroupLocation,
      AdminKey,
      CreatedAt,
      GroupDay,
      GroupMonth,
      GroupYear,
      GroupHour,
      CurrentUser,
      GroupPrice;
  private int GroupType;
  private int IsClicked = 1;
  private HashMap<String, Object> FriendKeys, ComingKeys, MessageKeys;
  private boolean isComing = true;
  private boolean CanAdd;

  @SuppressLint({"SuspiciousIndentation", "SetTextI18n"})
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_party_main);

    // this 2 lines disables the action bar only in this activity
    ActionBar actionBar = getSupportActionBar();
    Objects.requireNonNull(actionBar).hide();

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

    // connection
    GridLayout mainGrid = findViewById(R.id.MainGrid);
    back5 = findViewById(R.id.back5);
    imgCalender = findViewById(R.id.imgCalender);
    imgThumbUp = findViewById(R.id.imgThumbUp);
    imgThumbDown = findViewById(R.id.imgThumbDown);
    imgSeeHours = findViewById(R.id.imgSeeHours);
    ImageView imgAddFriend = findViewById(R.id.imgAddFriend);
    imgOptions = findViewById(R.id.imgOptions);
    btnEditName = findViewById(R.id.btnEditName);
    tvDateText = findViewById(R.id.tvDateText);
    tvDateDays = findViewById(R.id.tvDateDays);
    tvDateMonths = findViewById(R.id.tvDateMonths);
    tvDateYears = findViewById(R.id.tvDateYears);
    tvDateHours = findViewById(R.id.tvDateHours);
    tvComing = findViewById(R.id.tvComing);
    tvNotComing = findViewById(R.id.tvNotComing);
    tvGroupName = findViewById(R.id.tvGroupName);
    TextView tvCreatedBy = findViewById(R.id.tvCreatedBy);
    tvSeeHours = findViewById(R.id.tvSeeHours);
    tvSeeDate = findViewById(R.id.tvSeeDate);
    TextView tvAddFriend = findViewById(R.id.tvAddFriend);
    tvOptions = findViewById(R.id.tvOptions);
    tvAt = findViewById(R.id.tvAt);
    TextView tvEntryPrice = findViewById(R.id.tvEntryPrice);
    TextView tvYourEntry = findViewById(R.id.tvYourEntry);
    card5 = findViewById(R.id.Card5);

    // get current account's email
    CurrentUser =
        Objects.requireNonNull(Objects.requireNonNull(DBRef.Auth.getCurrentUser()).getEmail())
            .replace('.', ' ');

    // setting of GroupScreen for all users(not in buttons)
    tvGroupName.setText(GroupName);
    tvCreatedBy.setText(AdminKey.replace(' ', '.') + " ,  " + CreatedAt);

    // if GroupPrice=0 so it write that the party is free to enter if its not so it
    // writes the entry price
    if (GroupPrice.equals("0")) {
      tvEntryPrice.setVisibility(View.INVISIBLE);
      tvYourEntry.setText("Free Party");
    } else {
      tvEntryPrice.setText(GroupPrice);
    } // convert int to string

    // if current account is admin
    if (CurrentUser.equals(AdminKey)) {
      AdminEvent(mainGrid);
    }

    // if current account is not admin
    if (!CurrentUser.equals(AdminKey)) {
      // if group is public
      if (GroupType != 0) {
        // settings if group is private
        imgAddFriend.setVisibility(View.INVISIBLE);
        tvAddFriend.setVisibility(View.INVISIBLE);
      }
      isComing();
      FriendEvent(mainGrid);
    }
    EventHandler();
  }

  private void EventHandler() {
    btnEditName.setOnClickListener(
        v -> {
          final EditText edittext = new EditText(GroupDetailsActivity.this);
          AlertDialog.Builder alert = new AlertDialog.Builder(GroupDetailsActivity.this);
          alert.setMessage("Input new name below");
          alert.setTitle("Change party's name");

          alert.setView(edittext);

          alert.setPositiveButton(
              "Change name",
              (dialog, whichButton) -> {
                // if pressed changed name
                GroupName = edittext.getText().toString();
                DBRef.refGroups.child(GroupKey).child("groupName").setValue(GroupName);
                tvGroupName.setText(GroupName);
                Toast.makeText(GroupDetailsActivity.this, "Name Changed", Toast.LENGTH_SHORT)
                    .show();
              });

          alert.setNegativeButton(
              "Back",
              (dialog, whichButton) -> {
                // what ever you want to do with Back.
              });

          alert.show();
        });
    back5.setOnClickListener(
        v -> {
          Intent i = new Intent(getBaseContext(), MainActivity.class);
          startActivity(i);
        });
  }

  private void AdminEvent(GridLayout mainGrid) {
    // Loop all child item of Main Grid
    for (int i = 0; i < mainGrid.getChildCount(); i++) {
      // You can see , all child item is CardView , so we just cast object to CardView
      final CardView cardView = (CardView) mainGrid.getChildAt(i);
      final int finalI = i;
      cardView.setOnClickListener(
          view -> {
            if (finalI == 0) // open 1,1 (1) Location
            {
              MapUtilities.showGroupLocationOnGoogleMaps(GroupLocation, GroupDetailsActivity.this);
            } else if (finalI == 1) // open 1,2 (2) Date
            {
              IsClicked = isClicked(IsClicked);
            } else if (finalI == 2) // open 2,1 (3) People Invited
            {
              Intent i3 = new Intent(getBaseContext(), MembersInvitedActivity.class);
              i3.putExtra("FriendKeys", FriendKeys);
              i3.putExtra("adminKey", AdminKey);
              startActivity(i3);
            } else if (finalI == 3) // open 2,2 (4) People Coming
            {
              Intent i3 = new Intent(getBaseContext(), MembersComingActivity.class);
              i3.putExtra("ComingKeys", ComingKeys);
              i3.putExtra("adminKey", AdminKey);
              startActivity(i3);
            } else if (finalI == 4) // open 3,1 (5) Admin Options
            {
              // intent from GroupScreen to AdminOptions
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

            } else if (finalI == 5) // open 3,2 (6) Chat
            {

              Intent i3 = new Intent(getBaseContext(), ChatActivity.class);
              i3.putExtra("MessageKeys", MessageKeys);
              i3.putExtra("groupKey", GroupKey);
              startActivity(i3);

            } else if (finalI == 6) // open 4,1 (7) Add Friends
            {
              Intent intent = new Intent(getBaseContext(), FriendsAddActivity.class);
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
            } else if (finalI == 7) // open 4,2 (8) Leave
            {

              AlertDialog.Builder builder = new AlertDialog.Builder(GroupDetailsActivity.this);
              builder.setTitle("Leave Party");
              builder.setMessage("Are you sure you want to leave this party?");
              builder.setPositiveButton(
                  "Yes",
                  (dialogInterface, i2) -> {

                    // if pressed Yes
                    // if group have no friends so delete friends
                    if (FriendKeys.size() == 1) {
                      // delete all messages written by current group
                      deleteMessages();

                      // delete group from database
                      DBRef.refGroups.child(GroupKey).removeValue();

                      // delete group's picture
                      DBRef.refStorage.child("Groups/" + GroupKey).delete();

                      // if it went successfully so toast write it
                      Toast.makeText(
                              GroupDetailsActivity.this, "successfully left", Toast.LENGTH_SHORT)
                          .show();

                      // intent from GroupScreen to MainMenu
                      Intent intent = new Intent(getBaseContext(), MainActivity.class);
                      startActivity(intent);

                    }
                    // only admin leave group and set admin key to another user
                    else {
                      // remove CurrentUser from FriendList and ComingList
                      FriendKeys.remove(CurrentUser);
                      ComingKeys.remove(CurrentUser);

                      // set new admin from users in FriendList
                      Map.Entry<String, Object> entry = FriendKeys.entrySet().iterator().next();
                      AdminKey = entry.getKey();
                      DBRef.refGroups.child(GroupKey).child("adminKey").setValue(AdminKey);

                      // removes friend list from firebase and set new one without Current user
                      // (Because cant update Hashmap with value deleting)
                      DBRef.refGroups.child(GroupKey).child("FriendKeys").removeValue();
                      DBRef.refGroups
                          .child(GroupKey)
                          .child("FriendKeys")
                          .updateChildren(FriendKeys);

                      // removes coming list from firebase and set new one without Current user
                      // (Because cant update Hashmap with value deleting)
                      DBRef.refGroups.child(GroupKey).child("ComingKeys").removeValue();
                      DBRef.refGroups
                          .child(GroupKey)
                          .child("ComingKeys")
                          .updateChildren(ComingKeys);

                      // if it went successfully so toast write it
                      Toast.makeText(
                              GroupDetailsActivity.this, "successfully left", Toast.LENGTH_SHORT)
                          .show();

                      // intent from GroupScreen to MainMenu
                      Intent intent = new Intent(getBaseContext(), MainActivity.class);
                      startActivity(intent);
                    }
                  });
              builder.setNegativeButton("No", (dialogInterface, i1) -> {});
              builder.show();
            }
          });
    }
  }

  private void FriendEvent(GridLayout mainGrid) {
    // Loop all child item of Main Grid
    for (int i = 0; i < mainGrid.getChildCount(); i++) {
      // You can see , all child item is CardView , so we just cast object to CardView
      final CardView cardView = (CardView) mainGrid.getChildAt(i);
      final int finalI = i;
      cardView.setOnClickListener(
          view -> {
            if (finalI == 0) // open 1,1 (1) Location
            {
              MapUtilities.showGroupLocationOnGoogleMaps(GroupLocation, GroupDetailsActivity.this);
            } else if (finalI == 1) // open 1,2 (2) Date
            {
              IsClicked = isClicked(IsClicked);
            } else if (finalI == 2) // open 2,1 (3) People Invited
            {
              Intent i3 = new Intent(getBaseContext(), MembersInvitedActivity.class);
              i3.putExtra("FriendKeys", FriendKeys);
              i3.putExtra("adminKey", AdminKey);
              startActivity(i3);
            } else if (finalI == 3) // open 2,2 (4) People Coming
            {
              Intent i3 = new Intent(getBaseContext(), MembersComingActivity.class);
              i3.putExtra("ComingKeys", ComingKeys);
              i3.putExtra("adminKey", AdminKey);
              startActivity(i3);
            } else if (finalI == 4) // open 3,1 (5) Coming/Not Coming
            {
              if (!isComing) {
                String CurrentUser =
                    Objects.requireNonNull(
                            Objects.requireNonNull(DBRef.Auth.getCurrentUser()).getEmail())
                        .replace('.', ' ');
                ComingKeys.put(CurrentUser, "true");
                DBRef.refGroups.child(GroupKey).child("ComingKeys").updateChildren(ComingKeys);
                Toast.makeText(GroupDetailsActivity.this, "You're Coming", Toast.LENGTH_SHORT)
                    .show();
                tvNotComing.setVisibility(View.INVISIBLE);
                imgThumbDown.setVisibility(View.INVISIBLE);
                tvComing.setVisibility(View.VISIBLE);
                imgThumbUp.setVisibility(View.VISIBLE);
                card5.setCardBackgroundColor(Color.parseColor("#1986ed"));
              }
            } else if (finalI == 5) // open 3,2 (6) Chat
            {

              Intent i3 = new Intent(getBaseContext(), ChatActivity.class);
              i3.putExtra("MessageKeys", MessageKeys);
              i3.putExtra("groupKey", GroupKey);
              startActivity(i3);

            } else if (finalI == 6) // open 4,1 (7) Add Friends
            {
              // if its a public group/got admin set to add
              if (CanAdd) {
                Intent intent = new Intent(getBaseContext(), FriendsAddActivity.class);
                intent.putExtra("GroupName", GroupName);
                intent.putExtra("groupKey", GroupKey);
                intent.putExtra("groupDays", GroupDay);
                intent.putExtra("groupMonths", GroupMonth);
                intent.putExtra("groupYears", GroupYear);
                intent.putExtra("groupHours", GroupHour);
                intent.putExtra("groupLocation", GroupLocation);
                intent.putExtra("adminKey", AdminKey);
                intent.putExtra("createdAt", CreatedAt);
                intent.putExtra("GroupType", GroupType);
                intent.putExtra("GroupPrice", GroupPrice);
                intent.putExtra("CanAdd", CanAdd);
                intent.putExtra("FriendKeys", FriendKeys);
                intent.putExtra("ComingKeys", ComingKeys);
                intent.putExtra("MessageKeys", MessageKeys);
                startActivity(intent);
              }
            } else if (finalI == 7) // open 4,2 (8) Leave
            {

              AlertDialog.Builder builder = new AlertDialog.Builder(GroupDetailsActivity.this);
              builder.setTitle("Leave Party");
              builder.setMessage("Are you sure you want to leave this party?");
              builder.setPositiveButton(
                  "Yes",
                  (dialogInterface, i2) -> {
                    // if pressed Yes
                    // if group have no friends so delete friends
                    if (FriendKeys.size() == 1) {
                      // delete all messages written by current group
                      deleteMessages();

                      // delete group screen from database
                      DBRef.refGroups.child(GroupKey).removeValue();

                      // delete group's picture
                      DBRef.refStorage.child("Groups/" + GroupKey).delete();

                      // if it went successfully so toast write it
                      Toast.makeText(
                              GroupDetailsActivity.this, "successfully left", Toast.LENGTH_SHORT)
                          .show();

                      // intent from GroupScreen to MainMenu
                      Intent intent = new Intent(getBaseContext(), MainActivity.class);
                      startActivity(intent);
                    }
                    // only current user leave group
                    else {
                      // remove CurrentUser from FriendList and ComingList
                      FriendKeys.remove(CurrentUser);
                      ComingKeys.remove(CurrentUser);

                      // removes friend list from firebase and set new one without Current user
                      // (Because cant update Hashmap with value deleting)
                      DBRef.refGroups.child(GroupKey).child("FriendKeys").removeValue();
                      DBRef.refGroups
                          .child(GroupKey)
                          .child("FriendKeys")
                          .updateChildren(FriendKeys);

                      // removes coming list from firebase and set new one without Current user
                      // (Because cant update Hashmap with value deleting)
                      DBRef.refGroups.child(GroupKey).child("ComingKeys").removeValue();
                      DBRef.refGroups
                          .child(GroupKey)
                          .child("ComingKeys")
                          .updateChildren(ComingKeys);

                      // if it went successfully so toast write it
                      Toast.makeText(
                              GroupDetailsActivity.this, "successfully left", Toast.LENGTH_SHORT)
                          .show();

                      // intent from GroupScreen to MainMenu
                      Intent intent = new Intent(getBaseContext(), MainActivity.class);
                      startActivity(intent);
                    }
                  });
              builder.setNegativeButton("No", (dialogInterface, i1) -> {});
              builder.show();
            }
          });
    }
  }

  private void isComing() {
    String CurrentUser =
        Objects.requireNonNull(Objects.requireNonNull(DBRef.Auth.getCurrentUser()).getEmail())
            .replace('.', ' ');
    boolean flag = false;
    for (String ComingFriend : ComingKeys.keySet()) {
      if (ComingFriend.equals(CurrentUser)) {
        tvNotComing.setVisibility(View.INVISIBLE);
        imgThumbDown.setVisibility(View.INVISIBLE);
        imgOptions.setVisibility(View.INVISIBLE);
        tvOptions.setVisibility(View.INVISIBLE);
        tvComing.setVisibility(View.VISIBLE);
        imgThumbUp.setVisibility(View.VISIBLE);
        card5.setCardBackgroundColor(Color.parseColor("#1986ed"));
        flag = true;
      }
    }
    if (!flag) {
      tvComing.setVisibility(View.INVISIBLE);
      imgThumbUp.setVisibility(View.INVISIBLE);
      imgOptions.setVisibility(View.INVISIBLE);
      tvOptions.setVisibility(View.INVISIBLE);
      tvNotComing.setVisibility(View.VISIBLE);
      imgThumbDown.setVisibility(View.VISIBLE);
      card5.setCardBackgroundColor(Color.parseColor("#af0707"));
      isComing = false;
    }
  }

  private int isClicked(int isClicked) {
    if (isClicked == 1) {
      // changes Month name to 3 letters
      String Month = GroupMonth.substring(0, 3);
      // set text
      tvDateDays.setText(GroupDay);
      tvDateMonths.setText(Month);
      tvDateYears.setText(GroupYear);
      tvDateHours.setText(GroupHour);
      // Design
      imgCalender.setVisibility(View.INVISIBLE);
      tvDateText.setVisibility(View.INVISIBLE);
      tvDateDays.setVisibility(View.VISIBLE);
      tvDateMonths.setVisibility(View.VISIBLE);
      tvDateYears.setVisibility(View.VISIBLE);
      tvSeeHours.setVisibility(View.VISIBLE);
      imgSeeHours.setVisibility(View.VISIBLE);
      return 2;
    }
    // if clicked again
    if (isClicked == 2) {
      tvDateDays.setVisibility(View.INVISIBLE);
      tvDateMonths.setVisibility(View.INVISIBLE);
      tvDateYears.setVisibility(View.INVISIBLE);
      tvSeeHours.setVisibility(View.INVISIBLE);
      tvSeeDate.setVisibility(View.VISIBLE);
      tvAt.setVisibility(View.VISIBLE);
      tvDateHours.setVisibility(View.VISIBLE);
      return 3;
    }
    // if click third time
    if (isClicked == 3) {
      tvDateDays.setVisibility(View.VISIBLE);
      tvDateMonths.setVisibility(View.VISIBLE);
      tvDateYears.setVisibility(View.VISIBLE);
      tvSeeHours.setVisibility(View.VISIBLE);
      tvSeeDate.setVisibility(View.INVISIBLE);
      tvAt.setVisibility(View.INVISIBLE);
      tvDateHours.setVisibility(View.INVISIBLE);
    }
    return 2;
  }

  private void deleteMessages() {
    DBRef.refMessages.addValueEventListener(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            for (DataSnapshot data : dataSnapshot.getChildren()) {
              String msgKey =
                  Objects.requireNonNull(data.getValue(ChatMessage.class)).getMessageKey();
              for (String chatMsgKey : MessageKeys.keySet()) {
                if (chatMsgKey.equals(msgKey)) {
                  DBRef.refMessages.child(chatMsgKey).removeValue();
                }
              }
            }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
  }
}
