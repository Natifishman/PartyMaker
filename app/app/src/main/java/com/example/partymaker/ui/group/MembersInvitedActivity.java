package com.example.partymaker.ui.group;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.User;
import com.example.partymaker.ui.adapters.InvitedAdapter;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MembersInvitedActivity extends AppCompatActivity {

  private ListView lv2;
  private HashMap<String, Object> FriendKeys;
  private String adminKey;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_party_invited);

    // this 2 lines changes title's name
    ActionBar actionBar = getSupportActionBar();
    Objects.requireNonNull(actionBar).setTitle("Invited to party");
    actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0081d1")));

    ExtrasMetadata extras = Common.getExtrasMetadataFromIntent(getIntent());
    if (extras == null) {
      Toast.makeText(this, "Missing intent data", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    // connection between intent from GroupScreen and InvitedList
    FriendKeys = extras.getFriendKeys();
    adminKey = extras.getAdminKey();
    lv2 = findViewById(R.id.lv2);

    ShowData();
    EventHandler();
  }

  private void EventHandler() {
    lv2.setOnItemClickListener((parent, view, position, id) -> {});
    lv2.setOnItemLongClickListener((parent, view, position, id) -> false);
  }

  private void ShowData() {
    DBRef.refUsers.addValueEventListener(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            ArrayList<User> ArrUsers = new ArrayList<>();
            HashMap<String, Object> GroupFriends;
            for (DataSnapshot data : dataSnapshot.getChildren()) {
              User p = data.getValue(User.class);
              String UserMail = Objects.requireNonNull(p).getEmail().replace('.', ' ');
              GroupFriends = FriendKeys;
              for (String GroupFriend : GroupFriends.keySet()) {
                if (GroupFriend.equals(UserMail)) {
                  ArrUsers.add(p);
                }
              }
            }
            InvitedAdapter adapt =
                new InvitedAdapter(MembersInvitedActivity.this, 0, 0, ArrUsers, adminKey);
            lv2.setAdapter(adapt);
          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
  }
}
