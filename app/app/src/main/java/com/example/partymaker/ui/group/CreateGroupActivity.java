package com.example.partymaker.ui.group;

import static com.example.partymaker.utilities.Common.hideViews;
import static com.example.partymaker.utilities.Common.showViews;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.ui.auth.LoginActivity;
import com.example.partymaker.ui.chatbot.GptChatActivity;
import com.example.partymaker.ui.common.MainActivity;
import com.example.partymaker.ui.profile.EditProfileActivity;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.MapUtilities;
import com.example.partymaker.utilities.groupBuilder.GroupBuilder;
import com.example.partymaker.utilities.groupBuilder.GroupDateTime;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class CreateGroupActivity extends AppCompatActivity implements OnMapReadyCallback {
  // Constants
  private static final int IMAGE_PICKER_REQUEST_CODE = 100;
  private static final int INSTRUCTION_DELAY_MS = 3000;
  private static final String TAG = "CreateGroupActivity";
  private final int FINE_PERMISSION_CODE = 1;
  private Button btnAddGroup, btnNext1, btnNext2, btnBack1, btnBack2, btnDone;
  private TextView tvPartyName, tvPartyDate, tvGroupPicture, tvHours, tvSelectedDate;
  private EditText etPartyName;
  private ImageView imgLogin, imgGroupPicture;
  private String GroupKey1, DaysSelected, MonthsSelected, YearsSelected;
  private CheckBox cbGroupType;
  private Calendar selectedDate;
  private TimePicker timePicker;
  private FloatingActionButton fabChat;
  private GoogleMap map;
  private LatLng chosenLatLng;
  private FusedLocationProviderClient locationClient;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_party_create);
    setupUI();
    setupLogic();
  }

  // UI call
  private void setupUI() {
    setupActionBar();
    initializeViews();
    setupMapAndLocation();
  }

  // Logical call
  private void setupLogic() {
    initializePlacesAPI();
    eventHandler();
    setupValidation();
  }

  // Initialize Google Places API if not already initialized
  private void initializePlacesAPI() {
    if (!Places.isInitialized()) {
      String apiKey = Common.getApiKey(this, "MAPS_KEY");
      Places.initialize(getApplicationContext(), apiKey);
    }
  }

  // Configure ActionBar appearance and styling
  private void setupActionBar() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar == null) return;

    // Set styled title with color
    int titleColor = ContextCompat.getColor(this, R.color.white);
    SpannableString spannableTitle = new SpannableString(getString(R.string.new_party_title));
    spannableTitle.setSpan(
        new ForegroundColorSpan(titleColor),
        0,
        spannableTitle.length(),
        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    actionBar.setTitle(spannableTitle);

    // Set background drawable
    Drawable backgroundDrawable = ContextCompat.getDrawable(this, R.color.primaryBlue);
    if (backgroundDrawable != null) {
      actionBar.setBackgroundDrawable(backgroundDrawable);
    }
  }

  // Initialize all view references and components
  private void initializeViews() {
    // Image views
    imgLogin = findViewById(R.id.imgLogin);
    imgGroupPicture = findViewById(R.id.imgGroupPicture);

    // Buttons
    btnNext1 = findViewById(R.id.btnNext1);
    btnNext2 = findViewById(R.id.btnNext2);
    btnBack1 = findViewById(R.id.btnBack1);
    btnBack2 = findViewById(R.id.btnBack2);
    btnDone = findViewById(R.id.btnDone);
    btnAddGroup = findViewById(R.id.btnAddGroup);

    // Text views
    tvPartyName = findViewById(R.id.tvPartyName);
    tvPartyDate = findViewById(R.id.tvPartyDate);
    tvGroupPicture = findViewById(R.id.tvGroupPicture);
    tvHours = findViewById(R.id.tvHours);
    tvSelectedDate = findViewById(R.id.tvSelectedDate);

    // Input views
    etPartyName = findViewById(R.id.etPartyName);
    cbGroupType = findViewById(R.id.cbGroupType);
    timePicker = findViewById(R.id.timePicker);

    // Floating action button
    fabChat = findViewById(R.id.fabChat);

    // Initialize calendar
    selectedDate = Calendar.getInstance();

    // Setup hours spinner
    setupHoursSpinner();
  }

  // Configure hours spinner with adapter
  private void setupHoursSpinner() {
    ArrayAdapter<CharSequence> hoursAdapter =
        ArrayAdapter.createFromResource(
            this, R.array.array_hours, android.R.layout.simple_spinner_item);
    hoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
  }

  // Initialize map fragment and location services
  private void setupMapAndLocation() {
    // Initialize map fragment
    SupportMapFragment mapFragment =
        (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
    if (mapFragment != null) {
      mapFragment.getMapAsync(this);
    }

    // Initialize autocomplete fragment
    setupAutocompleteFragment();

    // Initialize location client
    locationClient = LocationServices.getFusedLocationProviderClient(this);
  }

  // Configure Google Places autocomplete fragment
  private void setupAutocompleteFragment() {
    AutocompleteSupportFragment autocompleteFragment =
        (AutocompleteSupportFragment)
            getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

    if (autocompleteFragment == null) return;

    // Set required place fields
    autocompleteFragment.setPlaceFields(
        Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

    // Set place selection listener
    autocompleteFragment.setOnPlaceSelectedListener(
        new PlaceSelectionListener() {
          @Override
          public void onPlaceSelected(@NonNull Place place) {
            handlePlaceSelection(place);
          }

          @Override
          public void onError(@NonNull Status status) {
            handlePlaceSelectionError(status);
          }
        });
  }

  // Handle successful place selection from autocomplete
  private void handlePlaceSelection(@NonNull Place place) {
    try {
      chosenLatLng = MapUtilities.centerMapOnChosenPlace(map, place);
      Log.d(TAG, "Place selected: " + place.getName());
    } catch (Exception e) {
      Log.e(TAG, "Error handling place selection", e);
      showErrorMessage(getString(R.string.error_place_selection));
    }
  }

  // Handle place selection errors
  private void handlePlaceSelectionError(@NonNull Status status) {
    String errorMessage = "Search error: " + status.getStatusMessage();
    Log.w(TAG, "Place selection error: " + status.getStatusMessage());
    showErrorMessage(errorMessage);
  }

  // Display error message to user
  private void showErrorMessage(String message) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }

  // Validate next-button if name is empty
  private void setupValidation() {
    // Check in real-time
    etPartyName.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Check if empty
            String text = s.toString().trim();
            btnNext1.setEnabled(!text.isEmpty());

            // Change the color according to the status
            if (text.isEmpty()) {
              btnNext1.setAlpha(0.5f); // Bright button
            } else {
              btnNext1.setAlpha(1.0f); // Normal button
            }
          }

          @Override
          public void afterTextChanged(Editable s) {}
        });

    // Set start point for button
    btnNext1.setEnabled(false);
    btnNext1.setAlpha(0.5f);
  }

  // Validate the name of the party
  private boolean validatePartyName() {
    String text = etPartyName.getText().toString().trim();
    if (text.isEmpty()) {
      etPartyName.setError("Enter party name");
      etPartyName.requestFocus();
      return false;
    }
    return true;
  }

  // Main event handler
  @SuppressLint({"SetTextI18n", "ClickableViewAccessibility", "DefaultLocale"})
  private void eventHandler() {
    setupNavigationButtons();
    setupFormButtons();
    setupImageSelection();
    setupFloatingActionButton();
  }

  private void setupNavigationButtons() {
    btnNext1.setOnClickListener(this::handleNextButtonClick);
    btnNext2.setOnClickListener(this::handleSecondStepNavigation);
    btnBack1.setOnClickListener(this::handleBackToFirstStep);
    btnBack2.setOnClickListener(this::handleBackToSecondStep);
  }

  private void setupFormButtons() {
    btnAddGroup.setOnClickListener(this::handleCreateGroup);
    btnDone.setOnClickListener(this::handleDoneClick);
    tvSelectedDate.setOnClickListener(v -> showDatePicker());
  }

  private void setupImageSelection() {
    imgGroupPicture.setOnClickListener(this::handleImageSelection);
  }

  @SuppressLint("ClickableViewAccessibility")
  private void setupFloatingActionButton() {
    fabChat.setOnClickListener(this::handleChatFabClick);
    fabChat.setOnTouchListener(this::handleChatFabTouch);
  }

  // Navigation handlers
  private void handleNextButtonClick(View v) {
    if (!validatePartyName()) {
      return;
    }

    transitionToLocationStep();
  }

  private void handleSecondStepNavigation(View v) {
    transitionToDateTimeStep();
  }

  private void handleBackToFirstStep(View v) {
    transitionToNameStep();
  }

  private void handleBackToSecondStep(View v) {
    transitionBackToLocationStep();
  }

  // Step transition methods
  private void transitionToLocationStep() {
    hideViews(tvPartyName, etPartyName, imgLogin, btnNext1);
    showViews(btnNext2, btnBack1);

    showMapAndLocationSearch();
  }

  private void transitionBackToLocationStep() {
    showViews(cbGroupType, btnNext2, btnBack1);
    hideViews(tvPartyDate, tvHours, tvSelectedDate, timePicker, btnAddGroup, btnBack2);

    showMapAndLocationSearch();
  }

  private void transitionToDateTimeStep() {
    hideViews(cbGroupType, btnNext2, btnBack1);
    showViews(tvPartyDate, tvHours, tvSelectedDate, timePicker, btnAddGroup, btnBack2);

    hideMapAndLocationSearch();
    showDatePicker();
  }

  private void transitionToNameStep() {
    hideViews(btnNext2, btnBack1);
    showViews(tvPartyName, etPartyName, imgLogin, btnNext1);

    hideMapAndLocationSearch();
  }

  private void transitionToImageStep() {
    hideViews(
        imgLogin,
        tvPartyDate,
        tvHours,
        tvSelectedDate,
        timePicker,
        btnBack2,
        btnAddGroup,
        cbGroupType);
    showViews(imgGroupPicture, tvGroupPicture, btnDone);

    updateActionBarForImageStep();
    scheduleInstructionTextUpdate();
  }

  // Form handlers
  private void handleCreateGroup(View v) {
    try {
      Group group = createGroupFromForm();
      saveGroupToDatabase(group);
      showSuccessMessage();
      transitionToImageStep();
    } catch (Exception e) {
      handleGroupCreationError(e);
    }
  }

  private void handleDoneClick(View v) {
    navigateToMainActivity();
  }

  private void handleImageSelection(View v) {
    openImagePicker();
    tvGroupPicture.setVisibility(View.INVISIBLE);
  }

  private void handleChatFabClick(View view) {
    Intent intent = new Intent(CreateGroupActivity.this, GptChatActivity.class);
    startActivity(intent);
  }

  @SuppressLint("ClickableViewAccessibility")
  private boolean handleChatFabTouch(View view, MotionEvent event) {
    return Common.dragChatButtonOnTouch(view, event);
  }

  // Group creation logic
  private Group createGroupFromForm() {
    GroupBuilder builder =
        new GroupBuilder()
            .setName(etPartyName.getText().toString())
            .setAdmin(getCurrentUserEmail())
            .setType(determineGroupType())
            .setCreationTime(getCurrentTimestamp())
            .setPrice("0")
            .setLocation(getSelectedLocation())
            .setDateTime(getSelectedDateTime());

    return builder.build();
  }

  private void saveGroupToDatabase(Group group) {
    String groupKey = generateGroupKey();
    GroupKey1 = groupKey;
    group.setGroupKey(groupKey);

    // Save group to database
    DBRef.refGroups.child(groupKey).setValue(group);

    // Add admin to group members
    addAdminToGroup(group);

    // Initialize group chat
    initializeGroupChat(group);
  }

  // Helper methods
  private void showMapAndLocationSearch() {
    findViewById(R.id.mapFragment).setVisibility(View.VISIBLE);
    findViewById(R.id.autocomplete_fragment).setVisibility(View.VISIBLE);
    MapUtilities.requestLocationPermission(this, map, locationClient, FINE_PERMISSION_CODE);
  }

  private void hideMapAndLocationSearch() {
    findViewById(R.id.mapFragment).setVisibility(View.INVISIBLE);
    findViewById(R.id.autocomplete_fragment).setVisibility(View.INVISIBLE);
  }

  private void updateActionBarForImageStep() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle(Html.fromHtml("<font color='#FFFFFF'>Set picture</font>"));
    }
  }

  @SuppressLint("SetTextI18n")
  private void scheduleInstructionTextUpdate() {
    new Handler()
        .postDelayed(
            () -> tvGroupPicture.setText("Tap on the picture above to set a profile picture"),
            INSTRUCTION_DELAY_MS);
  }

  private void openImagePicker() {
    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("image/*");
    startActivityForResult(
        Intent.createChooser(intent, "Select Picture"), IMAGE_PICKER_REQUEST_CODE);
  }

  private void navigateToMainActivity() {
    Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);
  }

  // Inner classes for better organization
  public static class GroupType {
    public static final int PUBLIC = 0;
    public static final int PRIVATE = 1;
  }

  // Group configuration methods
  private int determineGroupType() {
    return cbGroupType.isChecked() ? GroupType.PRIVATE : GroupType.PUBLIC;
  }

  private String getCurrentUserEmail() {
    FirebaseUser currentUser = DBRef.Auth.getCurrentUser();
    return currentUser != null
        ? Objects.requireNonNull(currentUser.getEmail()).replace('.', ' ')
        : "";
  }

  private String getCurrentTimestamp() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    return sdf.format(new Date());
  }

  private String getSelectedLocation() {
    if (chosenLatLng != null) {
      return MapUtilities.encodeCoordinatesToStringLocation(chosenLatLng);
    } else {
      showLocationWarning();
      return null;
    }
  }

  private GroupDateTime getSelectedDateTime() {
    int hour = timePicker.getHour();
    int minute = timePicker.getMinute();
    String timeString = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

    return new GroupDateTime(DaysSelected, MonthsSelected, YearsSelected, timeString);
  }

  private String generateGroupKey() {
    return DBRef.refGroups.push().getKey();
  }

  private void addAdminToGroup(Group group) {
    String adminKey = getCurrentUserEmail();
    Map<String, Object> memberData = new HashMap<>();
    memberData.put(adminKey, "true");

    // Add to both friend keys and coming keys
    DBRef.refGroups.child(group.getGroupKey()).child("FriendKeys").updateChildren(memberData);
    DBRef.refGroups.child(group.getGroupKey()).child("ComingKeys").updateChildren(memberData);
  }

  private void initializeGroupChat(Group group) {
    Map<String, Object> emptyChat = new HashMap<>();
    DBRef.refGroups.child(group.getGroupKey()).child("MessageKeys").updateChildren(emptyChat);
  }

  private void showSuccessMessage() {
    Toast.makeText(this, "Group successfully created", Toast.LENGTH_SHORT).show();
  }

  private void showLocationWarning() {
    Toast.makeText(this, "Warning: You have not set an address for your party.", Toast.LENGTH_LONG)
        .show();
  }

  private void handleGroupCreationError(Exception e) {
    Toast.makeText(this, "Error creating group: " + e.getMessage(), Toast.LENGTH_LONG).show();
    Log.e(TAG, "Group creation failed", e);
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == 100) {
        Uri uri = data.getData();
        if (null != uri) {

          ((ImageView) findViewById(R.id.imgGroupPicture)).setImageURI(uri);

          DBRef.refStorage
              .child("Groups/" + GroupKey1)
              .putFile(uri)
              .addOnSuccessListener(
                  taskSnapshot ->
                      Toast.makeText(CreateGroupActivity.this, "saved", Toast.LENGTH_SHORT).show())
              .addOnFailureListener(
                  exception ->
                      Toast.makeText(
                              CreateGroupActivity.this, "error while saving ", Toast.LENGTH_SHORT)
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
      goToNextActivity = new Intent(getApplicationContext(), CreateGroupActivity.class);
      startActivity(goToNextActivity);
    } else if (item.getItemId() == R.id.idEditProfile) {
      goToNextActivity = new Intent(getApplicationContext(), EditProfileActivity.class);
      startActivity(goToNextActivity);
    } else if (item.getItemId() == R.id.idPublicParties) {
      goToNextActivity = new Intent(getApplicationContext(), PublicGroupsActivity.class);
      startActivity(goToNextActivity);
    } else if (item.getItemId() == R.id.idLogout) {
      DBRef.Auth.signOut();
      DBRef.CurrentUser = null;
      goToNextActivity = new Intent(getApplicationContext(), LoginActivity.class);
      startActivity(goToNextActivity);
    }

    return true;
  }

  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }

  private void showDatePicker() {
    DatePickerDialog datePickerDialog =
        new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
              selectedDate.set(year, month, dayOfMonth);
              updateSelectedDate();
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH));
    datePickerDialog.show();
  }

  @SuppressLint("DefaultLocale")
  private void updateSelectedDate() {
    // Get month name in English
    String monthName = new SimpleDateFormat("MMMM", Locale.ENGLISH).format(selectedDate.getTime());

    // Update the selected date text
    tvSelectedDate.setText(
        String.format(
            "%d %s %d",
            selectedDate.get(Calendar.DAY_OF_MONTH), monthName, selectedDate.get(Calendar.YEAR)));

    // Store the values
    DaysSelected = String.valueOf(selectedDate.get(Calendar.DAY_OF_MONTH));
    MonthsSelected = monthName;
    YearsSelected = String.valueOf(selectedDate.get(Calendar.YEAR));
  }

  @Override
  public void onRequestPermissionsResult(
      int code, @NonNull String[] perms, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(code, perms, grantResults);
    MapUtilities.handlePermissionsResult(
        this, code, grantResults, FINE_PERMISSION_CODE, map, locationClient);
  }

  @Override
  public void onMapReady(@NonNull GoogleMap googleMap) {
    this.map = googleMap;
    // Wherever the user clicks gets stored in chosenLatLng
    map.setOnMapClickListener(
        latlng -> {
          chosenLatLng = latlng;
          map.clear();
          map.addMarker(new MarkerOptions().position(latlng).title("Party here"));
        });
  }
}
