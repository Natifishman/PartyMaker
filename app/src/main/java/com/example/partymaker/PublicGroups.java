package com.example.partymaker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.data.DBref;
import com.example.partymaker.data.Group;
import com.example.partymaker.data.GroupAdpter;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;

public class PublicGroups extends AppCompatActivity {
  private ListView lv1;
  private DatabaseReference database;
  ArrayList<Group> group;
  GroupAdpter allGroupsAdapter;
  String UserKey;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_public_groups);

    // Change title Name and Color
    ActionBar actionBar = getSupportActionBar();
    actionBar.setTitle(Html.fromHtml("<font color='#1986ed'>Public Parties</font>"));

    // set actionbar background
    @SuppressLint("UseCompatLoadingForDrawables")
    Drawable d = getResources().getDrawable(R.drawable.background5);
    actionBar.setBackgroundDrawable(d);

    // connection
    lv1 = findViewById(R.id.lv5);
    UserKey = DBref.Auth.getCurrentUser().getEmail().replace('.', ' ');
    database = FirebaseDatabase.getInstance().getReference("Groups");
    retriveData();
    EventHandler();
  }

  private void EventHandler() {
    lv1.setOnItemClickListener(
        new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // intent Value
            String groupName = group.get(position).getGroupName();
            String groupKey = group.get(position).getGroupKey();
            String groupDays = group.get(position).getGroupDays();
            String groupMonths = group.get(position).getGroupMonths();
            String groupYears = group.get(position).getGroupYears();
            String groupHours = group.get(position).getGroupHours();
            String groupLocation = group.get(position).getGroupLocation();
            String adminKey = group.get(position).getAdminKey();
            String createdAt = group.get(position).getCreatedAt();
            String GroupPrice = group.get(position).getGroupPrice();
            int GroupType = group.get(position).getGroupType();
            boolean CanAdd = group.get(position).isCanAdd();
            HashMap<String, Object> FriendKeys = group.get(position).getFriendKeys();
            HashMap<String, Object> ComingKeys = group.get(position).getComingKeys();
            HashMap<String, Object> MessageKeys = group.get(position).getMessageKeys();
            Intent intent = new Intent(getBaseContext(), GroupJoin.class);
            ExtrasMetadata extras =
                new ExtrasMetadata(
                    groupName,
                    groupKey,
                    groupDays,
                    groupMonths,
                    groupYears,
                    groupHours,
                    groupLocation,
                    adminKey,
                    createdAt,
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
    lv1.setOnItemLongClickListener(
        new AdapterView.OnItemLongClickListener() {
          @Override
          public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            return false;
          }
        });
  }

  public void retriveData() {
    database.addValueEventListener(
        new ValueEventListener() {
          @SuppressLint("SuspiciousIndentation")
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
            HashMap<String, Object> UserKeys = new HashMap<>();
            group = new ArrayList<Group>();

            for (DataSnapshot data : dataSnapshot.getChildren()) { // scan all group in data
              Group p = data.getValue(Group.class);
              UserKeys = data.getValue(Group.class).getFriendKeys();

              if (p.getGroupType() == 0) { // if group is public
                boolean flag = false;
                for (String userKey : UserKeys.keySet()) { // scan all group friends
                  if (UserKey.equals(
                      userKey)) // if current user not friend in current group so it show
                  // current group
                  {
                    flag = true;
                    break;
                  }
                }
                if (!flag) {
                  group.add(p);
                }
              }
            }
            allGroupsAdapter = new GroupAdpter(PublicGroups.this, 0, 0, group);
            lv1.setAdapter(allGroupsAdapter);
          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    Intent goToNextActivity;

    if (item.getItemId() == R.id.idMenu) {
      goToNextActivity = new Intent(getApplicationContext(), MainActivity.class);
      startActivity(goToNextActivity);
    } else if (item.getItemId() == R.id.idAddProfile) {
      goToNextActivity = new Intent(getApplicationContext(), AddGroup.class);
      startActivity(goToNextActivity);
    } else if (item.getItemId() == R.id.idEditProfile) {
      goToNextActivity = new Intent(getApplicationContext(), EditProfile.class);
      startActivity(goToNextActivity);
    } else if (item.getItemId() == R.id.idPublicParties) {
      goToNextActivity = new Intent(getApplicationContext(), PublicGroups.class);
      startActivity(goToNextActivity);
    } else if (item.getItemId() == R.id.idLogout) {
      DBref.Auth.signOut();
      DBref.CurrentUser = null;
      goToNextActivity = new Intent(getApplicationContext(), Login.class);
      startActivity(goToNextActivity);
    }

    return true;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }
}
