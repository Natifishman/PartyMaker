package com.example.partymaker.ui.group;

import static com.example.partymaker.utilities.Common.hideViews;
import static com.example.partymaker.utilities.Common.showViews;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import com.example.partymaker.utilities.MapUtilities;
import java.util.HashMap;
import java.util.Objects;

public class JoinGroupActivity extends AppCompatActivity {
  private ImageView imgCalender;
  private ImageView imgSeeHours;
  private TextView tvDateText;
  private TextView tvDateDays;
  private TextView tvDateMonths;
  private TextView tvDateYears;
  private TextView tvDateHours;
  private TextView tvSeeHours;
  private TextView tvSeeDate;
  private TextView tvAt;
  private String GroupKey;
  private String GroupLocation;
  private String GroupDay;
  private String GroupMonth;
  private String GroupYear;
  private String GroupHour;
  private String CurrentUser;
  private int IsClicked = 1;
  private HashMap<String, Object> FriendKeys;

  @SuppressLint("SetTextI18n")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_party_join);

    // this 2 lines disables the action bar only in this activity
    ActionBar actionBar = getSupportActionBar();
    Objects.requireNonNull(actionBar).hide();

    // Get Values from PublicGroups By intent + connection between intent and
    // current activity objects
    ExtrasMetadata extras = Common.getExtrasMetadataFromIntent(getIntent());
    if (extras == null) {
      Toast.makeText(this, "Missing intent data", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }
    String groupName = extras.getGroupName();
    GroupKey = extras.getGroupKey();
    GroupDay = extras.getGroupDays();
    GroupMonth = extras.getGroupMonths();
    GroupYear = extras.getGroupYears();
    GroupHour = extras.getGroupHours();
    GroupLocation = extras.getGroupLocation();
    String adminKey = extras.getAdminKey();
    String createdAt = extras.getCreatedAt();
    String groupPrice = extras.getGroupPrice();
    FriendKeys = extras.getFriendKeys();

    // connection
    GridLayout joinGrid = findViewById(R.id.joinGrid);
    imgCalender = findViewById(R.id.imgGroupDate);
    imgSeeHours = findViewById(R.id.imgSeeHours1);
    tvDateText = findViewById(R.id.tvGroupDateText);
    tvDateDays = findViewById(R.id.tvGroupDateDays);
    tvDateMonths = findViewById(R.id.tvGroupDateMonths);
    tvDateYears = findViewById(R.id.tvGroupDateYears);
    tvDateHours = findViewById(R.id.tvGroupDateHours);
    TextView tvGroupName = findViewById(R.id.tvGroupName1);
    TextView tvCreatedBy = findViewById(R.id.tvCreatedBy1);
    tvSeeHours = findViewById(R.id.tvSeeHours1);
    tvSeeDate = findViewById(R.id.tvSeeDate1);
    tvAt = findViewById(R.id.tvGroupAt);
    TextView tvEntryPrice = findViewById(R.id.tvEntryPrice1);
    TextView tvYourEntry = findViewById(R.id.tvYourEntry1);

    // get current account's email
    CurrentUser =
        Objects.requireNonNull(Objects.requireNonNull(DBRef.Auth.getCurrentUser()).getEmail())
            .replace('.', ' ');

    // setting of GroupScreen for all users(not in buttons)
    tvGroupName.setText(groupName);
    tvCreatedBy.setText(adminKey.replace(' ', '.') + " ,  " + createdAt);

    // if GroupPrice=0 so it write that the party is free to enter if its not so it
    // writes the entry price
    if (Objects.requireNonNull(groupPrice).equals("0")) {
      tvEntryPrice.setVisibility(View.INVISIBLE);
      tvYourEntry.setText("Free Party");
    } else {
      tvEntryPrice.setText(groupPrice);
    } // convert int to string

    JoinEvent(joinGrid);
  }

  private void JoinEvent(GridLayout mainGrid) {
    // Loop all child item of Main Grid
    for (int i = 0; i < mainGrid.getChildCount(); i++) {
      // You can see , all child item is CardView , so we just cast object to CardView
      final CardView cardView = (CardView) mainGrid.getChildAt(i);
      final int finalI = i;
      cardView.setOnClickListener(
          view -> {
            if (finalI == 0) // open 1,1 (1) Location
            {
              MapUtilities.showGroupLocationOnGoogleMaps(GroupLocation, JoinGroupActivity.this);
            } else if (finalI == 1) // open 1,2 (2) Date
            {
              IsClicked = isClicked(IsClicked);
            } else if (finalI == 2) // open 2,1 (3) Join
            {
              FriendKeys.put(CurrentUser, "true");
              DBRef.refGroups.child(GroupKey).child("FriendKeys").removeValue();
              DBRef.refGroups.child(GroupKey).child("FriendKeys").updateChildren(FriendKeys);
              Toast.makeText(JoinGroupActivity.this, "Successfully joined ", Toast.LENGTH_SHORT)
                  .show();
              Intent i1 = new Intent(getBaseContext(), PublicGroupsActivity.class);
              startActivity(i1);
            } else if (finalI == 3) // open 2,2 (4) Back
            {
              Intent i1 = new Intent(getBaseContext(), PublicGroupsActivity.class);
              startActivity(i1);
            }
          });
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
      showViews(tvDateDays, tvDateMonths, tvDateYears, tvSeeHours, imgSeeHours);
      hideViews(imgCalender, tvDateText);
      return 2;
    }
    // if clicked again
    if (isClicked == 2) {
      showViews(tvSeeDate, tvAt, tvDateHours);
      hideViews(tvDateDays, tvDateMonths, tvDateYears, tvSeeHours);
      return 3;
    }
    // if click third time
    if (isClicked == 3) {
      showViews(tvDateDays, tvDateMonths, tvDateYears, tvSeeHours);
      hideViews(tvSeeDate, tvAt, tvDateHours);
    }
    return 2;
  }
}
