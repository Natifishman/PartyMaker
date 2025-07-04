package com.example.partymaker.ui.profile;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.ui.auth.LoginActivity;
import com.example.partymaker.ui.common.MainActivity;
import com.example.partymaker.ui.group.CreateGroupActivity;
import com.example.partymaker.ui.group.PublicGroupsActivity;
import com.squareup.picasso.Picasso;
import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {

  private ImageView imgProfile;
  private final ActivityResultLauncher<String> imagePickerLauncher =
      registerForActivityResult(
          new ActivityResultContracts.GetContent(), this::uploadImageToFirebase);
  private static final String TAG = "EditProfileActivity";
  private static final String ACTION_BAR_START_COLOR = "#0E81D1";
  private static final String ACTION_BAR_END_COLOR = "#0E81D1";
  private static final String ACTION_BAR_TITLE_COLOR = "#FFFFFF";
  private static final float ACTION_BAR_ELEVATION = 15f;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_edit_profile);

    setupActionBar();
    initViews();
    loadProfileImage();
    setListeners();
  }

  // Sets up the action bar with custom gradient background and styling.
  private void setupActionBar() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar == null) {
      Log.w(TAG, "ActionBar not available");
      return;
    }

    try {
      GradientDrawable gradient = createActionBarGradient();
      actionBar.setBackgroundDrawable(gradient);

      String styledTitle = createStyledTitle();
      actionBar.setTitle(Html.fromHtml(styledTitle, Html.FROM_HTML_MODE_LEGACY));

      configureActionBarProperties(actionBar);

      Log.d(TAG, "ActionBar setup completed");

    } catch (Exception e) {
      Log.e(TAG, "Error setting up ActionBar", e);
    }
  }

  // Creates a gradient drawable for the action bar background.
  private GradientDrawable createActionBarGradient() {
    GradientDrawable gradient = new GradientDrawable();
    gradient.setShape(GradientDrawable.RECTANGLE);
    gradient.setColors(
        new int[] {
          Color.parseColor(ACTION_BAR_START_COLOR), Color.parseColor(ACTION_BAR_END_COLOR)
        });
    gradient.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
    return gradient;
  }

  // Creates a styled HTML title string.
  private String createStyledTitle() {
    return String.format(
        "<font color='%s'><b>%s</b></font>",
        EditProfileActivity.ACTION_BAR_TITLE_COLOR, "Edit Profile");
  }

  // Configures action bar properties.
  private void configureActionBarProperties(ActionBar actionBar) {
    actionBar.setElevation(ACTION_BAR_ELEVATION);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(false);
  }

  private void initViews() {
    imgProfile = findViewById(R.id.imgProfile);
  }

  private void setListeners() {
    imgProfile.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
  }

  private void loadProfileImage() {
    String userEmail = Objects.requireNonNull(DBRef.Auth.getCurrentUser()).getEmail();
    assert userEmail != null;
    String safeEmail = userEmail.replace(".", " ");

    DBRef.refStorage
        .child("Users/" + safeEmail)
        .getDownloadUrl()
        .addOnSuccessListener(
            uri -> Picasso.get().load(uri).placeholder(R.drawable.ic_profile).into(imgProfile))
        .addOnFailureListener(
            e -> Toast.makeText(this, "Failed to load profile picture", Toast.LENGTH_SHORT).show());
  }

  private void uploadImageToFirebase(Uri uri) {
    if (uri == null) return;

    imgProfile.setImageURI(uri);
    String userEmail = Objects.requireNonNull(DBRef.Auth.getCurrentUser()).getEmail();
    assert userEmail != null;
    String safeEmail = userEmail.replace(".", " ");

    DBRef.refStorage
        .child("Users/" + safeEmail)
        .putFile(uri)
        .addOnSuccessListener(
            taskSnapshot ->
                Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show())
        .addOnFailureListener(
            e -> Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show());
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }

  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
}
