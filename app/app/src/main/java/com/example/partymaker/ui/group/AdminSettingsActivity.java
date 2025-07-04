package com.example.partymaker.ui.group;

import static com.example.partymaker.utilities.Constants.GROUP_NAME;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import java.util.Objects;

public class AdminSettingsActivity extends AppCompatActivity {
  private ImageView imgCanAdd, imgType, imgEditGroup;
  private ImageButton btnEditName;
  private String AdminKey,
      GroupKey,
      GroupName,
      GroupDay,
      GroupMonth,
      GroupYear,
      GroupHour,
      GroupLocation,
      CreatedAt,
      GroupPrice;
  private TextView tvNameGroup, tvCanAdd, tvType;
  private int GroupType;
  private HashMap<String, Object> FriendKeys, ComingKeys, MessageKeys;
  boolean CanAdd;

  @SuppressLint("SetTextI18n")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_party_settings);

    // this 2 lines disables the action bar only in this activity
    ActionBar actionBar = getSupportActionBar();
    assert actionBar != null;
    actionBar.hide();

    // Get Values from GroupScreen By intent + connection between intent and current
    // activity objects
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
    GridLayout groupGrid = findViewById(R.id.GroupGrid);
    tvCanAdd = findViewById(R.id.tvCanAdd);
    tvType = findViewById(R.id.tvType);
    tvNameGroup = findViewById(R.id.tvNameGroup);
    imgCanAdd = findViewById(R.id.imgCanAdd);
    imgType = findViewById(R.id.imgType);
    imgEditGroup = findViewById(R.id.imgEditGroup);
    btnEditName = findViewById(R.id.btnEditName1);

    // group's Picture options
    DBRef.refStorage
        .child("Groups/" + GroupKey)
        .getDownloadUrl()
        .addOnSuccessListener(uri -> Picasso.get().load(uri).into(imgEditGroup))
        .addOnFailureListener(exception -> {});

    // set Group Name
    tvNameGroup.setText(GroupName);

    // set 1st button if group is private or if group is public
    if (GroupType == 0) { // if group is public
      imgType.setImageResource(R.drawable.ic_party_everyone);
      tvType.setText("Public Group");
    } else { // if group is private
      imgType.setImageResource(R.drawable.ic_party_lock);
      tvType.setText("Private Group");
    }

    // set 3rd button if all can add it shows that all can if if only admin can add
    // it shows that only admin can add
    if (CanAdd) {
      imgCanAdd.setImageResource(R.drawable.ic_party_everyone);
      tvCanAdd.setText("Everyone Add");
    } else {
      imgCanAdd.setImageResource(R.drawable.ic_party_private);
      tvCanAdd.setText("Admin Add");
    }

    // call GroupOptions
    Options(groupGrid);

    eventHandler();
  }

  private void eventHandler() {
    // Profile picture on click to change it
    imgEditGroup.setOnClickListener(
        v -> {
          Intent i = new Intent();
          i.setType("image/*");
          i.setAction(Intent.ACTION_GET_CONTENT);
          startActivityForResult(Intent.createChooser(i, "Select Picture"), 100);
        });
    btnEditName.setOnClickListener(
        v -> {
          final EditText edittext = new EditText(AdminSettingsActivity.this);
          AlertDialog.Builder alert = new AlertDialog.Builder(AdminSettingsActivity.this);
          alert.setMessage("Input new name below");
          alert.setTitle("Change party's name");

          alert.setView(edittext);

          alert.setPositiveButton(
              "Change name",
              (dialog, whichButton) -> {
                // if pressed changed name
                GroupName = edittext.getText().toString();
                DBRef.refGroups.child(GroupKey).child(GROUP_NAME).setValue(GroupName);
                tvNameGroup.setText(GroupName);
                Toast.makeText(AdminSettingsActivity.this, "Name Changed", Toast.LENGTH_SHORT)
                    .show();
              });

          alert.setNegativeButton(
              "Back",
              (dialog, whichButton) -> {
                // what ever you want to do with Back.
              });

          alert.show();
        });
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == 100) {
        Uri uri = data.getData();
        if (null != uri) {
          ((ImageView) findViewById(R.id.imgEditGroup)).setImageURI(uri);

          DBRef.refStorage
              .child("Groups/" + GroupKey)
              .putFile(uri)
              .addOnSuccessListener(
                  taskSnapshot ->
                      Toast.makeText(AdminSettingsActivity.this, "saved", Toast.LENGTH_SHORT)
                          .show())
              .addOnFailureListener(
                  exception ->
                      Toast.makeText(
                              AdminSettingsActivity.this, "error while saving ", Toast.LENGTH_SHORT)
                          .show());
        }
      }
    }
  }

  @SuppressLint("SetTextI18n")
  private void Options(GridLayout GroupGrid) {
    // Loop all child item of Main Grid
    for (int i = 0; i < GroupGrid.getChildCount(); i++) {
      // You can see , all child item is CardView , so we just cast object to CardView
      final CardView cardView = (CardView) GroupGrid.getChildAt(i);
      final int finalI = i;
      cardView.setOnClickListener(
          view -> {
            if (finalI == 0) // open 1,1 (1) Change GroupType to Public/Private
            {
              if (GroupType == 0) {
                imgType.setImageResource(R.drawable.ic_party_lock);
                tvType.setText("Private Group");
                GroupType = 1;
                DBRef.refGroups.child(GroupKey).child("groupType").setValue(1);
              } else {
                imgType.setImageResource(R.drawable.ic_party_everyone);
                tvType.setText("Public Group");
                GroupType = 0;
                DBRef.refGroups.child(GroupKey).child("groupType").setValue(0);
              }
            } else if (finalI == 1) // open 1,2 (2) set if anyone can add/only admin can add
            {

              if (CanAdd) {
                imgCanAdd.setImageResource(R.drawable.ic_party_private);
                tvCanAdd.setText("Only Admin");
                CanAdd = false;
                DBRef.refGroups.child(GroupKey).child("canAdd").setValue(false);
              } else {
                imgCanAdd.setImageResource(R.drawable.ic_party_everyone);
                tvCanAdd.setText("Everyone Add");
                CanAdd = true;
                DBRef.refGroups.child(GroupKey).child("canAdd").setValue(true);
              }

            } else if (finalI == 2) // open 2,1 (3) Delete Group
            {
              // delete all messages written by current group
              deleteMessages();

              // delete group from database
              DBRef.refGroups.child(GroupKey).removeValue();

              // delete group's picture
              DBRef.refStorage.child("Groups/" + GroupKey).delete();

              // if it went successfully so toast write it
              Toast.makeText(AdminSettingsActivity.this, "successfully deleted", Toast.LENGTH_SHORT)
                  .show();

              // intent from GroupScreen to MainMenu
              Intent intent = new Intent(getBaseContext(), MainActivity.class);
              startActivity(intent);
            } else if (finalI == 3) // open 2,2 (4) Back
            {
              // intent to AdminOptions Activity with Values
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
            }
          });
    }
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
