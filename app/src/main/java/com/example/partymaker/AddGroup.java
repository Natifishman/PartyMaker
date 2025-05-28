package com.example.partymaker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.partymaker.data.DBref;
import com.example.partymaker.data.Group;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import android.app.DatePickerDialog;
import android.widget.TimePicker;

public class AddGroup extends AppCompatActivity {
  private Button btnAddGroup, btnNext1, btnNext2, btnBack1, btnBack2, btnDone;
  private TextView tvPartyName, tvPartyLocation, tvPartyDate, tvGroupPicture, tvHours, tvSelectedDate;
  private EditText etPartyName, etPartyLocation;
  private ImageView imgLogin, imgGroupPicture;
  private String GroupKey1, DaysSelected, MonthsSelected, YearsSelected, HoursSelected;
  private CheckBox cbGroupType;
  private Calendar selectedDate;
  private TimePicker timePicker;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_group);

    // Better approach for setting a colored ActionBar title
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      // Define color using resources for better maintainability
      int titleColor = ContextCompat.getColor(this, R.color.teal);

      // Create a SpannableString for styling the title
      SpannableString spannableTitle = new SpannableString("New Party");
      spannableTitle.setSpan(
          new ForegroundColorSpan(titleColor),
          0,
          spannableTitle.length(),
          Spannable.SPAN_INCLUSIVE_INCLUSIVE);

      actionBar.setTitle(spannableTitle);
    }

    // set actionbar background
    Drawable d = ContextCompat.getDrawable(this, R.drawable.background3);
    assert actionBar != null;
    actionBar.setBackgroundDrawable(d);

    imgLogin = findViewById(R.id.imgLogin);
    imgGroupPicture = findViewById(R.id.imgGroupPicture);
    btnNext1 = findViewById(R.id.btnNext1);
    btnNext2 = findViewById(R.id.btnNext2);
    btnBack1 = findViewById(R.id.btnBack1);
    btnBack2 = findViewById(R.id.btnBack2);
    btnDone = findViewById(R.id.btnDone);
    btnAddGroup = findViewById(R.id.btnAddGroup);
    tvPartyName = findViewById(R.id.tvPartyName);
    tvPartyLocation = findViewById(R.id.tvPartyLocation);
    tvPartyDate = findViewById(R.id.tvPartyDate);
    tvGroupPicture = findViewById(R.id.tvGroupPicture);
    tvHours = findViewById(R.id.tvHours);
    tvSelectedDate = findViewById(R.id.tvSelectedDate);
    etPartyName = findViewById(R.id.etPartyName);
    etPartyLocation = findViewById(R.id.etPartyLocation);
    cbGroupType = findViewById(R.id.cbGroupType);
    selectedDate = Calendar.getInstance();
    timePicker = findViewById(R.id.timePicker);

    // spinner adapter for hours
    ArrayAdapter<CharSequence> hoursAdapter =
        ArrayAdapter.createFromResource(
            this, R.array.array_hours, android.R.layout.simple_spinner_item);
    hoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    evantHandler();
  }

  @SuppressLint("SetTextI18n")
  private void evantHandler() {
    btnNext1.setOnClickListener(
        v -> {
          tvPartyName.setVisibility(View.INVISIBLE);
          etPartyName.setVisibility(View.INVISIBLE);
          tvPartyLocation.setVisibility(View.VISIBLE);
          etPartyLocation.setVisibility(View.VISIBLE);
          btnNext1.setVisibility(View.INVISIBLE);
          btnNext2.setVisibility(View.VISIBLE);
          btnBack1.setVisibility(View.VISIBLE);
        });
    btnNext2.setOnClickListener(
        v -> {
          tvPartyLocation.setVisibility(View.INVISIBLE);
          etPartyLocation.setVisibility(View.INVISIBLE);
          cbGroupType.setVisibility(View.INVISIBLE);
          tvPartyDate.setVisibility(View.VISIBLE);
          tvHours.setVisibility(View.VISIBLE);
          tvSelectedDate.setVisibility(View.VISIBLE);
          timePicker.setVisibility(View.VISIBLE);
          btnNext2.setVisibility(View.INVISIBLE);
          btnAddGroup.setVisibility(View.VISIBLE);
          btnBack1.setVisibility(View.INVISIBLE);
          btnBack2.setVisibility(View.VISIBLE);
          showDatePicker();
        });
    btnBack1.setOnClickListener(
        v -> {
          tvPartyName.setVisibility(View.VISIBLE);
          etPartyName.setVisibility(View.VISIBLE);
          tvPartyLocation.setVisibility(View.INVISIBLE);
          etPartyLocation.setVisibility(View.INVISIBLE);
          btnNext1.setVisibility(View.VISIBLE);
          btnNext2.setVisibility(View.INVISIBLE);
          btnBack1.setVisibility(View.INVISIBLE);
        });
    btnBack2.setOnClickListener(
        v -> {
          tvPartyLocation.setVisibility(View.VISIBLE);
          etPartyLocation.setVisibility(View.VISIBLE);
          cbGroupType.setVisibility(View.VISIBLE);
          tvPartyDate.setVisibility(View.INVISIBLE);
          tvHours.setVisibility(View.INVISIBLE);
          tvSelectedDate.setVisibility(View.INVISIBLE);
          timePicker.setVisibility(View.INVISIBLE);
          btnNext2.setVisibility(View.VISIBLE);
          btnAddGroup.setVisibility(View.INVISIBLE);
          btnBack1.setVisibility(View.VISIBLE);
          btnBack2.setVisibility(View.INVISIBLE);
        });
    btnAddGroup.setOnClickListener(
        v -> {
          Group p = new Group();
          // Group's name
          p.setGroupName(etPartyName.getText().toString());
          // admin name
          p.setAdminKey(
              Objects.requireNonNull(Objects.requireNonNull(DBref.Auth.getCurrentUser()).getEmail())
                  .replace('.', ' '));

          // set group's type if 0 so Public group if 1 so Private group
          if (cbGroupType.isChecked()) {
            p.setGroupType(1);
          } else {
            p.setGroupType(0);
          }

          // set if people can add their friends
          if (p.getGroupType() == 0) // if group is public
          p.setCanAdd(true); // so people can add
          else if (p.getGroupType() == 1) // if group is private
          p.setCanAdd(false); // so people cant add

          // Time when opened Group
          Calendar c = Calendar.getInstance();
          @SuppressLint("SimpleDateFormat")
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          String strDate = sdf.format(c.getTime());
          p.setCreatedAt(strDate);

          // set Group's entry price to 0 (free)
          p.setGroupPrice("0");

          // Group's Location
          p.setGroupLocation(etPartyLocation.getText().toString());

          // Group's date
          p.setGroupDays(DaysSelected);
          p.setGroupMonths(MonthsSelected);
          p.setGroupYears(YearsSelected);
          // Get hour and minute from TimePicker
          int hour, minute;
          if (android.os.Build.VERSION.SDK_INT >= 23) {
            hour = timePicker.getHour();
            minute = timePicker.getMinute();
          } else {
            hour = timePicker.getCurrentHour();
            minute = timePicker.getCurrentMinute();
          }
          HoursSelected = String.format("%02d:%02d", hour, minute);
          p.setGroupHours(HoursSelected);

          // create unique key for Group
          String GroupKey = DBref.refGroups.push().getKey();
          GroupKey1 = GroupKey;
          p.setGroupKey(GroupKey);

          // create unique key for FriendKey
          String FriendKey = DBref.Auth.getCurrentUser().getEmail().replace('.', ' ');

          // set key in database
          assert GroupKey != null;
          DBref.refGroups.child(GroupKey).setValue(p);

          // adding reference to FriendKeys with the admin email
          HashMap<String, Object> result1 = new HashMap<>();
          result1.put(FriendKey, "true");
          DBref.refGroups.child(p.getGroupKey()).child("FriendKeys").updateChildren(result1);
          // adding reference to ComingKeys with the admin email
          DBref.refGroups.child(p.getGroupKey()).child("ComingKeys").updateChildren(result1);

          // create empty HashMap for Chat
          HashMap<String, Object> result3 = new HashMap<>();
          DBref.refGroups.child(p.getGroupKey()).child("MessageKeys").updateChildren(result3);

          // write Group created successfully
          Toast.makeText(AddGroup.this, "Group successfully created", Toast.LENGTH_SHORT).show();

          // Design
          imgLogin.setVisibility(View.INVISIBLE);
          tvPartyDate.setVisibility(View.INVISIBLE);
          tvHours.setVisibility(View.INVISIBLE);
          tvSelectedDate.setVisibility(View.INVISIBLE);
          timePicker.setVisibility(View.INVISIBLE);
          btnBack2.setVisibility(View.INVISIBLE);
          btnAddGroup.setVisibility(View.INVISIBLE);
          cbGroupType.setVisibility(View.INVISIBLE);
          imgGroupPicture.setVisibility(View.VISIBLE);
          tvGroupPicture.setVisibility(View.VISIBLE);
          btnDone.setVisibility(View.VISIBLE);

          // Set title in action bar - i chose no title
          ActionBar actionBar = getSupportActionBar();
          assert actionBar != null;
          actionBar.setTitle(Html.fromHtml("<font color='#039694'>Set party's picture</font>"));

          // wait 2 seconds and after it makes text Disappeared
          Handler handler = new Handler();
          handler.postDelayed(
              () -> tvGroupPicture.setText("Tap on the picture above to set a profile picture"),
              3000);
        });
    imgGroupPicture.setOnClickListener(
        v -> {
          Intent i = new Intent();
          i.setType("image/*");
          i.setAction(Intent.ACTION_GET_CONTENT);
          startActivityForResult(Intent.createChooser(i, "Select Picture"), 100);
          tvGroupPicture.setVisibility(View.INVISIBLE);
        });
    tvSelectedDate.setOnClickListener(v -> showDatePicker());
    btnDone.setOnClickListener(
        v -> {
          Intent intent = new Intent(getBaseContext(), MainActivity.class);
          startActivity(intent);
        });
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == 100) {
        Uri uri = data.getData();
        if (null != uri) {

          ((ImageView) findViewById(R.id.imgGroupPicture)).setImageURI(uri);

          DBref.refStorage
              .child("Groups/" + GroupKey1)
              .putFile(uri)
              .addOnSuccessListener(
                  taskSnapshot -> Toast.makeText(AddGroup.this, "saved", Toast.LENGTH_SHORT).show())
              .addOnFailureListener(
                  exception ->
                      Toast.makeText(AddGroup.this, "error while saving ", Toast.LENGTH_SHORT)
                          .show());
        }
      }
    }
  }

  @Override
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

  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }

  private void showDatePicker() {
    DatePickerDialog datePickerDialog = new DatePickerDialog(
        this,
        (view, year, month, dayOfMonth) -> {
          selectedDate.set(year, month, dayOfMonth);
          updateSelectedDate();
        },
        selectedDate.get(Calendar.YEAR),
        selectedDate.get(Calendar.MONTH),
        selectedDate.get(Calendar.DAY_OF_MONTH)
    );
    datePickerDialog.show();
  }
  
  private void updateSelectedDate() {
    // Get month name in English
    String monthName = new SimpleDateFormat("MMMM", Locale.ENGLISH).format(selectedDate.getTime());
    
    // Update the selected date text
    tvSelectedDate.setText(String.format("%d %s %d", 
        selectedDate.get(Calendar.DAY_OF_MONTH),
        monthName,
        selectedDate.get(Calendar.YEAR)));
    
    // Store the values
    DaysSelected = String.valueOf(selectedDate.get(Calendar.DAY_OF_MONTH));
    MonthsSelected = monthName;
    YearsSelected = String.valueOf(selectedDate.get(Calendar.YEAR));
  }
}
