package com.example.partymaker.ui.group;

import static com.example.partymaker.utilities.Common.hideViews;
import static com.example.partymaker.utilities.Common.showViews;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import java.util.HashMap;

public class ChangeDateActivity extends AppCompatActivity {
  private Button btnHide, btnHelp, btnChangeDate;
  private TextView tvHide, tvHelp, tvInstructions1;
  private HashMap<String, Object> FriendKeys, ComingKeys, MessageKeys;
  private String GroupKey,
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
  private boolean CanAdd;
  private Spinner spnDays, spnMonths, spnYears, spnHours;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_party_change_date);

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

    // connection
    btnChangeDate = findViewById(R.id.btnChangeDate);
    btnHide = findViewById(R.id.btnHide2);
    btnHelp = findViewById(R.id.btnHelp2);
    tvHide = findViewById(R.id.tvHide2);
    tvHelp = findViewById(R.id.tvHelp2);
    tvInstructions1 = findViewById(R.id.tvInstructions2);
    spnDays = findViewById(R.id.spnDayDate);
    spnMonths = findViewById(R.id.spnMonthDate);
    spnYears = findViewById(R.id.spnYearDate);
    spnHours = findViewById(R.id.spnHourDate);
    // spinner adapter for days
    ArrayAdapter<CharSequence> daysAdapter =
        ArrayAdapter.createFromResource(
            this, R.array.array_days, android.R.layout.simple_spinner_item);
    daysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // spinner adapter for months
    ArrayAdapter<CharSequence> monthsAdapter =
        ArrayAdapter.createFromResource(
            this, R.array.array_months, android.R.layout.simple_spinner_item);
    monthsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // spinner adapter for years
    ArrayAdapter<CharSequence> yearsAdapter =
        ArrayAdapter.createFromResource(
            this, R.array.array_years, android.R.layout.simple_spinner_item);
    yearsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // spinner adapter for hours
    ArrayAdapter<CharSequence> hoursAdapter =
        ArrayAdapter.createFromResource(
            this, R.array.array_hours, android.R.layout.simple_spinner_item);
    hoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // set adapter for each spinner
    spnDays.setAdapter(daysAdapter);
    spnMonths.setAdapter(monthsAdapter);
    spnYears.setAdapter(yearsAdapter);
    spnHours.setAdapter(hoursAdapter);

    eventHandler();
  }

  private void eventHandler() {
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

    btnChangeDate.setOnClickListener(
        v -> {
          // set new value in firebase
          DBRef.refGroups.child(GroupKey).child("groupDays").setValue(GroupDay);
          DBRef.refGroups.child(GroupKey).child("groupMonths").setValue(GroupMonth);
          DBRef.refGroups.child(GroupKey).child("groupYears").setValue(GroupYear);
          DBRef.refGroups.child(GroupKey).child("groupHours").setValue(GroupHour);

          // Type Successfully Changes
          Toast.makeText(ChangeDateActivity.this, "Successfully Changed", Toast.LENGTH_SHORT)
              .show();

          // intent from ChangeDate to AdminOptions
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
    spnDays.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            GroupDay = parent.getItemAtPosition(position).toString();
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {}
        });
    spnMonths.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            GroupMonth = parent.getItemAtPosition(position).toString();
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {}
        });
    spnYears.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            GroupYear = parent.getItemAtPosition(position).toString();
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {}
        });
    spnHours.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            GroupHour = parent.getItemAtPosition(position).toString();
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {}
        });
  }
}
