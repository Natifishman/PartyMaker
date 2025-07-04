package com.example.partymaker.ui.group;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import com.example.partymaker.utilities.MapUtilities;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class AdminOptionsActivity extends AppCompatActivity implements OnMapReadyCallback {
  private LinearLayout mainContent;
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
  private int GroupType;
  private HashMap<String, Object> FriendKeys, ComingKeys, MessageKeys;
  private boolean CanAdd;
  private CardView CardPrice, CardLocation;
  private GoogleMap map;
  private LatLng chosenLatLng;
  private Button saveLocationButton;
  private FrameLayout mapContainer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_party_options);

    if (!Places.isInitialized()) {
      Places.initialize(getApplicationContext(), Common.getApiKey(this, "MAPS_KEY"));
    }

    // this 2 lines disables the action bar only in this activity
    ActionBar actionBar = getSupportActionBar();
    Objects.requireNonNull(actionBar).hide();

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

    // connection between XML and AdminOptions
    mainContent = findViewById(R.id.mainContent);
    GridLayout myGrid = findViewById(R.id.MyGrid);
    TextView tvAdminEmail = findViewById(R.id.tvAdminEmail);
    CardPrice = findViewById(R.id.CardPrice);
    CardLocation = findViewById(R.id.CardLocation);
    saveLocationButton = findViewById(R.id.saveLocation);
    mapContainer = findViewById(R.id.mapContainer);

    wireMapComponents();

    // settings + things to see when activity starts
    tvAdminEmail.setText(AdminKey.replace(' ', '.'));

    // start AdminOptions
    wireAdminOptions(myGrid);

    eventHandler();
  }

  private void eventHandler() {
    CardPrice.setOnClickListener(
        v -> {
          final EditText edittext = new EditText(AdminOptionsActivity.this);
          edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
          edittext.setText(GroupPrice);
          AlertDialog.Builder alert = new AlertDialog.Builder(AdminOptionsActivity.this);
          alert.setMessage("Input new price below");
          alert.setTitle("Change party's entry price");

          alert.setView(edittext);

          alert.setPositiveButton(
              "Change price",
              (dialog, whichButton) -> {
                // if pressed changed name
                GroupPrice = edittext.getText().toString();
                DBRef.refGroups.child(GroupKey).child("groupPrice").setValue(GroupPrice);
                Toast.makeText(AdminOptionsActivity.this, "Name Changed", Toast.LENGTH_SHORT)
                    .show();
              });

          alert.setNegativeButton(
              "Back",
              (dialog, whichButton) -> {
                // what ever you want to do with Back.
              });

          alert.show();
        });

    CardLocation.setOnClickListener(
        v -> {
          mainContent.setVisibility(View.INVISIBLE);
          MapUtilities.centerMapOnChosenPlace(
              map,
              Place.builder()
                  .setLatLng(MapUtilities.decodeStringLocationToCoordinates(GroupLocation))
                  .build());
          mapContainer.setVisibility(View.VISIBLE);

          saveLocationButton.setOnClickListener(
              v1 -> {
                if (chosenLatLng != null) {
                  String locationValue =
                      MapUtilities.encodeCoordinatesToStringLocation(chosenLatLng);
                  DBRef.refGroups.child(GroupKey).child("groupLocation").setValue(locationValue);
                  GroupLocation = locationValue;
                  Toast.makeText(AdminOptionsActivity.this, "Location Changed", Toast.LENGTH_SHORT)
                      .show();
                } else {
                  Toast.makeText(
                          AdminOptionsActivity.this,
                          "Warning: You have not set an address for your party.",
                          Toast.LENGTH_LONG)
                      .show();
                }
                mainContent.setVisibility(View.VISIBLE);
                mapContainer.setVisibility(View.INVISIBLE);
              });
        });
  }

  private void wireAdminOptions(GridLayout MyGrid) {
    // Loop all child item of Main Grid
    for (int i = 0; i < MyGrid.getChildCount(); i++) {
      // You can see , all child item is CardView , so we just cast object to CardView
      final CardView cardView = (CardView) MyGrid.getChildAt(i);
      final int finalI = i;
      cardView.setOnClickListener(
          view -> {
            Intent intent = new Intent(getBaseContext(), AdminOptionsActivity.class);
            // if (finalI == 0)  open 1,1 (1) Change Location
            if (finalI == 1) // open 1,2 (2) Change Date
            {
              // intent from AdminOptions to ChangeDate
              intent = new Intent(getBaseContext(), ChangeDateActivity.class);
            } else if (finalI == 2) // open 2,1 (3) Delete People
            {
              intent = new Intent(getBaseContext(), FriendsRemoveActivity.class);
            }
            // else if (finalI == 3) // open 2,2 (4) Change Entry Price
            else if (finalI == 4) // open 3,1 (5) Group Options
            {
              // intent to GroupOptions Activity with Values
              intent = new Intent(getBaseContext(), AdminSettingsActivity.class);

            } else if (finalI == 5) // open 3,2 (6) Back
            {
              // intent back to GroupScreen Activity with Values
              intent = new Intent(getBaseContext(), GroupDetailsActivity.class);
            }
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

  private void wireMapComponents() {
    SupportMapFragment mapFrag =
        (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
    assert mapFrag != null;
    mapFrag.getMapAsync(this);
    AutocompleteSupportFragment autocompleteFragment =
        (AutocompleteSupportFragment)
            getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
    assert autocompleteFragment != null;
    autocompleteFragment.setPlaceFields(
        Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

    autocompleteFragment.setOnPlaceSelectedListener(
        new PlaceSelectionListener() {
          @Override
          public void onPlaceSelected(@NonNull Place place) {
            chosenLatLng = MapUtilities.centerMapOnChosenPlace(map, place);
          }

          @Override
          public void onError(@NonNull Status status) {
            Toast.makeText(
                    AdminOptionsActivity.this,
                    "Search error: " + status.getStatusMessage(),
                    Toast.LENGTH_SHORT)
                .show();
          }
        });
  }
}
