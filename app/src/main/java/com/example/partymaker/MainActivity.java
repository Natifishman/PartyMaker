package com.example.partymaker;

import static com.example.partymaker.utilities.Constants.ADMIN_KEY;
import static com.example.partymaker.utilities.Constants.CAN_ADD;
import static com.example.partymaker.utilities.Constants.COMING_KEYS;
import static com.example.partymaker.utilities.Constants.CREATED_AT;
import static com.example.partymaker.utilities.Constants.FRIEND_KEYS;
import static com.example.partymaker.utilities.Constants.GROUP_DAYS;
import static com.example.partymaker.utilities.Constants.GROUP_HOURS;
import static com.example.partymaker.utilities.Constants.GROUP_KEY;
import static com.example.partymaker.utilities.Constants.GROUP_LOCATION;
import static com.example.partymaker.utilities.Constants.GROUP_MONTHS;
import static com.example.partymaker.utilities.Constants.GROUP_NAME;
import static com.example.partymaker.utilities.Constants.GROUP_PRICE;
import static com.example.partymaker.utilities.Constants.GROUP_TYPE;
import static com.example.partymaker.utilities.Constants.GROUP_YEARS;
import static com.example.partymaker.utilities.Constants.MESSAGE_KEYS;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.partymaker.data.DBref;
import com.example.partymaker.data.Group;
import com.example.partymaker.data.GroupAdpter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private ListView lv1;
    private DatabaseReference database;
    ArrayList<Group> group;
    GroupAdpter allGroupsAdapter;
    String UserKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Change title Name and Color
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(Html.fromHtml("<font color='#FFFFFF'>Your Parties</font>"));
            //set actionbar background
            Drawable d = getResources().getDrawable(R.drawable.fade);
            actionBar.setBackgroundDrawable(d);
            //Set bar above all
            actionBar.setElevation(8f);
        }

        lv1 = (ListView) findViewById(R.id.lv1);
        UserKey = DBref.Auth.getCurrentUser().getEmail().replace('.', ' ');
        database = FirebaseDatabase.getInstance().getReference("Groups");
        retriveData();
        EventHandler();

        // Setup chat FAB
        FloatingActionButton fabChat = findViewById(R.id.fabChat);
        fabChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GptChatActivity.class);
                startActivity(intent);
            }
        });
    }

    private void EventHandler() {
        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //intent Value
                var chosenGroup = group.get(position);
                String GroupName = chosenGroup.getGroupName();
                String GroupKey = chosenGroup.getGroupKey();
                String GroupDays = chosenGroup.getGroupDays();
                String GroupMonths = chosenGroup.getGroupMonths();
                String GroupYears = chosenGroup.getGroupYears();
                String GroupHours = chosenGroup.getGroupHours();
                String GroupLocation = chosenGroup.getGroupLocation();
                String AdminKey = chosenGroup.getAdminKey();
                String CreatedAt = chosenGroup.getCreatedAt();
                String GroupPrice = chosenGroup.getGroupPrice();
                int GroupType = chosenGroup.getGroupType();
                boolean CanAdd = chosenGroup.isCanAdd();
                HashMap<String, Object> FriendKeys = chosenGroup.getFriendKeys();
                HashMap<String, Object> ComingKeys = chosenGroup.getComingKeys();
                HashMap<String, Object> MessageKeys = chosenGroup.getMessageKeys();
                Intent intent = new Intent(getBaseContext(), GroupScreen.class);
                ExtrasMetadata extras = new ExtrasMetadata(GroupName, GroupKey,
                        GroupDays, GroupMonths, GroupYears, GroupHours, GroupLocation,
                        AdminKey, CreatedAt, GroupPrice, GroupType, CanAdd,
                        FriendKeys, ComingKeys, MessageKeys);
                Common.addExtrasToIntent(intent, extras);
                startActivity(intent);
                startActivity(intent);
            }
        });
        lv1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });
    }


    public void retriveData() {
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Object> UserKeys = new HashMap<>();
                group = new ArrayList<Group>();

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Group p = data.getValue(Group.class);
                    UserKeys = data.getValue(Group.class).getFriendKeys();

                    for (String userKey : UserKeys.keySet()) {

                        if (UserKey.equals(userKey))
                            group.add(p);
                    }


                }
                allGroupsAdapter = new GroupAdpter(MainActivity.this, 0, 0, group);
                lv1.setAdapter(allGroupsAdapter);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //menu
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
