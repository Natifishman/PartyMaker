package com.example.partymaker.ui.auth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import java.util.Objects;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
  private TextInputLayout tilEmail, tilUsername, tilPassword;
  private TextInputEditText etEmail, etUsername, etPassword;
  private MaterialButton btnRegister, btnPress;
  private ImageView imgRegister;
  private MaterialCardView headerCard, formCard;
  private LinearLayout celebrationLayout;
  private ProgressBar passwordStrengthBar;
  private TextView passwordStrengthText, formProgressText;
  private View progressIndicator;

  private static final String CHANNEL_ID = "registration_channel";
  private boolean isAnimating = false;
  private int completedFields = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);

    // Hide action bar for cleaner look
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }

    // Initialize UI components
    initializeViews();

    // Create notification channel
    createNotificationChannel();

    // Set up animations
    setupInitialAnimations();

    // Set up event handlers
    setupEventHandlers();

    // Set up password strength indicator
    setupPasswordStrengthIndicator();

    // Set up form progress tracking
    setupFormProgressTracking();
  }

  private void initializeViews() {
    // TextInputLayouts for validation
    tilEmail = findViewById(R.id.tilEmail);
    tilUsername = findViewById(R.id.tilUsername);
    tilPassword = findViewById(R.id.tilPassword);

    // TextInputEditTexts
    etEmail = findViewById(R.id.etEmailR);
    etUsername = findViewById(R.id.etUsername);
    etPassword = findViewById(R.id.etPasswordR);

    // Buttons
    btnRegister = findViewById(R.id.btnRegister);
    btnPress = findViewById(R.id.btnPressR);

    // Other views
    imgRegister = findViewById(R.id.imgRegister);
    headerCard = findViewById(R.id.headerCard);
    formCard = findViewById(R.id.formCard);

    // Create password strength indicator
    createPasswordStrengthIndicator();

    // Create form progress indicator
    createFormProgressIndicator();

    // Create celebration layout
    createCelebrationLayout();
  }

  @SuppressLint("SetTextI18n")
  private void createPasswordStrengthIndicator() {
    // Create password strength bar and text
    LinearLayout passwordContainer = new LinearLayout(this);
    passwordContainer.setOrientation(LinearLayout.HORIZONTAL);
    passwordContainer.setPadding(0, 16, 0, 8);

    passwordStrengthBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
    passwordStrengthBar.setMax(100);
    passwordStrengthBar.setProgress(0);

    LinearLayout.LayoutParams barParams =
        new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
    barParams.weight = 1;
    barParams.rightMargin = 32;
    passwordStrengthBar.setLayoutParams(barParams);

    passwordStrengthText = new TextView(this);
    passwordStrengthText.setText("Password strength");
    passwordStrengthText.setTextSize(12);
    passwordStrengthText.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));

    passwordContainer.addView(passwordStrengthBar);
    passwordContainer.addView(passwordStrengthText);

    // Add to parent layout after password field
    ViewGroup parentLayout = (ViewGroup) tilPassword.getParent();
    int passwordIndex = parentLayout.indexOfChild(tilPassword);
    parentLayout.addView(passwordContainer, passwordIndex + 1);
  }

  @SuppressLint("SetTextI18n")
  private void createFormProgressIndicator() {
    formProgressText = new TextView(this);
    formProgressText.setText("progress: 0/3");
    formProgressText.setTextSize(14);
    formProgressText.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
    formProgressText.setPadding(0, 16, 0, 8);

    progressIndicator = new View(this);
    progressIndicator.setLayoutParams(new LinearLayout.LayoutParams(0, 8));
    progressIndicator.setBackgroundResource(android.R.color.holo_blue_light);

    ViewGroup parentLayout = (ViewGroup) tilEmail.getParent();
    parentLayout.addView(formProgressText, 0);
    parentLayout.addView(progressIndicator, 1);
  }

  @SuppressLint("SetTextI18n")
  private void createCelebrationLayout() {
    celebrationLayout = new LinearLayout(this);
    celebrationLayout.setOrientation(LinearLayout.VERTICAL);
    celebrationLayout.setGravity(android.view.Gravity.CENTER);
    celebrationLayout.setVisibility(View.GONE);
    celebrationLayout.setBackgroundColor(Color.parseColor("#88000000"));

    // Add celebration elements
    TextView celebrationText = new TextView(this);
    celebrationText.setText("🎉 Welcome! 🎉");
    celebrationText.setTextSize(24);
    celebrationText.setTextColor(Color.WHITE);
    celebrationText.setGravity(android.view.Gravity.CENTER);

    TextView welcomeText = new TextView(this);
    welcomeText.setText("Successful registration!");
    welcomeText.setTextSize(18);
    welcomeText.setTextColor(Color.WHITE);
    welcomeText.setGravity(android.view.Gravity.CENTER);
    welcomeText.setPadding(0, 16, 0, 0);

    celebrationLayout.addView(celebrationText);
    celebrationLayout.addView(welcomeText);

    // Add to root layout
    ViewGroup rootLayout = findViewById(android.R.id.content);
    ViewGroup.LayoutParams params =
        new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    rootLayout.addView(celebrationLayout, params);
  }

  private void setupInitialAnimations() {
    // Initially hide all elements
    headerCard.setAlpha(0f);
    headerCard.setTranslationY(-100f);

    formCard.setAlpha(0f);
    formCard.setTranslationY(100f);

    // Animate them in
    animateElementsIn();
  }

  private void animateElementsIn() {
    // Header animation
    ObjectAnimator headerFade = ObjectAnimator.ofFloat(headerCard, "alpha", 0f, 1f);
    ObjectAnimator headerSlide = ObjectAnimator.ofFloat(headerCard, "translationY", -100f, 0f);

    AnimatorSet headerSet = new AnimatorSet();
    headerSet.playTogether(headerFade, headerSlide);
    headerSet.setDuration(800);
    headerSet.setInterpolator(new OvershootInterpolator());

    // Form animation (delayed)
    ObjectAnimator formFade = ObjectAnimator.ofFloat(formCard, "alpha", 0f, 1f);
    ObjectAnimator formSlide = ObjectAnimator.ofFloat(formCard, "translationY", 100f, 0f);

    AnimatorSet formSet = new AnimatorSet();
    formSet.playTogether(formFade, formSlide);
    formSet.setDuration(800);
    formSet.setInterpolator(new OvershootInterpolator());
    formSet.setStartDelay(300);

    // Start animations
    headerSet.start();
    formSet.start();

    // Animate cake image
    animateCakeImage();
  }

  private void animateCakeImage() {
    // Gentle floating animation for the cake
    ObjectAnimator floatUp = ObjectAnimator.ofFloat(imgRegister, "translationY", 0f, -20f);
    floatUp.setDuration(2000);
    floatUp.setInterpolator(new AccelerateDecelerateInterpolator());
    floatUp.setRepeatCount(ObjectAnimator.INFINITE);
    floatUp.setRepeatMode(ObjectAnimator.REVERSE);
    floatUp.start();

    // Subtle rotation
    ObjectAnimator rotate = ObjectAnimator.ofFloat(imgRegister, "rotation", -2f, 2f);
    rotate.setDuration(3000);
    rotate.setInterpolator(new AccelerateDecelerateInterpolator());
    rotate.setRepeatCount(ObjectAnimator.INFINITE);
    rotate.setRepeatMode(ObjectAnimator.REVERSE);
    rotate.start();
  }

  private void setupEventHandlers() {
    // Register button click with animation
    btnRegister.setOnClickListener(
        view -> {
          if (!isAnimating && validateInputs()) {
            animateButtonClick(btnRegister, this::signUp);
          }
        });

    // Login link click with animation
    btnPress.setOnClickListener(
        view -> {
          if (!isAnimating) {
            animateButtonClick(
                btnPress,
                () -> {
                  Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                  startActivity(intent);
                  overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                  finish();
                });
          }
        });

    // Add focus change animations
    setupFieldFocusAnimations();
  }

  private void setupFieldFocusAnimations() {
    View.OnFocusChangeListener focusListener =
        (view, hasFocus) -> {
          TextInputLayout layout = (TextInputLayout) view.getParent().getParent();
          animateFieldFocus(layout, hasFocus);
        };

    etEmail.setOnFocusChangeListener(focusListener);
    etUsername.setOnFocusChangeListener(focusListener);
    etPassword.setOnFocusChangeListener(focusListener);
  }

  private void animateFieldFocus(TextInputLayout layout, boolean hasFocus) {
    float scale = hasFocus ? 1.02f : 1f;
    float elevation = hasFocus ? 8f : 4f;

    ObjectAnimator scaleX = ObjectAnimator.ofFloat(layout, "scaleX", scale);
    ObjectAnimator scaleY = ObjectAnimator.ofFloat(layout, "scaleY", scale);
    ObjectAnimator elevationAnim = ObjectAnimator.ofFloat(layout, "elevation", elevation);

    AnimatorSet set = new AnimatorSet();
    set.playTogether(scaleX, scaleY, elevationAnim);
    set.setDuration(200);
    set.setInterpolator(new AccelerateDecelerateInterpolator());
    set.start();
  }

  private void animateButtonClick(MaterialButton button, Runnable action) {
    isAnimating = true;

    // Scale down animation
    ObjectAnimator scaleDown = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f);
    ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f);

    AnimatorSet scaleDownSet = new AnimatorSet();
    scaleDownSet.playTogether(scaleDown, scaleDownY);
    scaleDownSet.setDuration(100);

    // Scale up animation
    ObjectAnimator scaleUp = ObjectAnimator.ofFloat(button, "scaleX", 0.95f, 1f);
    ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(button, "scaleY", 0.95f, 1f);

    AnimatorSet scaleUpSet = new AnimatorSet();
    scaleUpSet.playTogether(scaleUp, scaleUpY);
    scaleUpSet.setDuration(100);

    // Chain animations
    scaleDownSet.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            scaleUpSet.start();
          }
        });

    scaleUpSet.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            isAnimating = false;
            action.run();
          }
        });

    scaleDownSet.start();
  }

  private void setupPasswordStrengthIndicator() {
    etPassword.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            updatePasswordStrength(s.toString());
          }

          @Override
          public void afterTextChanged(Editable s) {}
        });
  }

  @SuppressLint("SetTextI18n")
  private void updatePasswordStrength(String password) {
    int strength = calculatePasswordStrength(password);

    // Animate progress bar
    ValueAnimator progressAnimator =
        ValueAnimator.ofInt(passwordStrengthBar.getProgress(), strength);
    progressAnimator.setDuration(300);
    progressAnimator.addUpdateListener(
        animation -> passwordStrengthBar.setProgress((Integer) animation.getAnimatedValue()));
    progressAnimator.start();

    // Update colors and text
    String strengthText;
    int color;

    if (strength < 25) {
      strengthText = "Weak";
      color = Color.RED;
    } else if (strength < 50) {
      strengthText = "Medium";
      color = Color.MAGENTA;
    } else if (strength < 75) {
      strengthText = "Strong";
      color = Color.BLUE;
    } else {
      strengthText = "Excellent";
      color = Color.GREEN;
    }

    passwordStrengthText.setText("Password strength:" + strengthText);
    passwordStrengthText.setTextColor(color);
    passwordStrengthBar
        .getProgressDrawable()
        .setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
  }

  private int calculatePasswordStrength(String password) {
    int strength = 0;

    if (password.length() >= 6) strength += 20;
    if (password.length() >= 8) strength += 10;
    if (password.length() >= 12) strength += 10;

    if (Pattern.compile("[a-z]").matcher(password).find()) strength += 10;
    if (Pattern.compile("[A-Z]").matcher(password).find()) strength += 15;
    if (Pattern.compile("[0-9]").matcher(password).find()) strength += 15;
    if (Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]").matcher(password).find())
      strength += 20;

    return Math.min(strength, 100);
  }

  private void setupFormProgressTracking() {
    TextWatcher progressWatcher =
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateFormProgress();
          }

          @Override
          public void afterTextChanged(Editable s) {}
        };

    etEmail.addTextChangedListener(progressWatcher);
    etUsername.addTextChangedListener(progressWatcher);
    etPassword.addTextChangedListener(progressWatcher);
  }

  private void updateFormProgress() {
    int newCompletedFields = 0;

    if (!TextUtils.isEmpty(etEmail.getText())
        && Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString()).matches()) {
      newCompletedFields++;
    }

    if (!TextUtils.isEmpty(etUsername.getText())
        && etUsername.getText().toString().trim().length() >= 3) {
      newCompletedFields++;
    }

    if (!TextUtils.isEmpty(etPassword.getText()) && etPassword.getText().toString().length() >= 6) {
      newCompletedFields++;
    }

    if (newCompletedFields != completedFields) {
      completedFields = newCompletedFields;
      animateFormProgress();
    }
  }

  @SuppressLint("SetTextI18n")
  private void animateFormProgress() {
    formProgressText.setText("Progress: " + completedFields + "/3");

    // Animate progress indicator width
    float targetWidth = (completedFields / 3f) * formCard.getWidth();

    ValueAnimator widthAnimator = ValueAnimator.ofFloat(progressIndicator.getWidth(), targetWidth);
    widthAnimator.setDuration(300);
    widthAnimator.addUpdateListener(
        animation -> {
          ViewGroup.LayoutParams params = progressIndicator.getLayoutParams();
          params.width = Math.round((Float) animation.getAnimatedValue());
          progressIndicator.setLayoutParams(params);
        });
    widthAnimator.start();

    // Change color based on progress
    int color =
        completedFields == 3
            ? Color.GREEN
            : completedFields >= 2 ? Color.BLUE : completedFields == 1 ? Color.RED : Color.GRAY;

    progressIndicator.setBackgroundColor(color);
  }

  private void createNotificationChannel() {
    CharSequence name = "Registration Notifications";
    String description = "Notifications for successful registration";
    int importance = NotificationManager.IMPORTANCE_DEFAULT;
    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
    channel.setDescription(description);

    NotificationManager notificationManager = getSystemService(NotificationManager.class);
    notificationManager.createNotificationChannel(channel);
  }

  private boolean validateInputs() {
    boolean isValid = true;

    // Reset errors
    tilEmail.setError(null);
    tilUsername.setError(null);
    tilPassword.setError(null);

    String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
    String username = Objects.requireNonNull(etUsername.getText()).toString().trim();
    String password = Objects.requireNonNull(etPassword.getText()).toString();

    // Email validation with animation
    if (TextUtils.isEmpty(email)) {
      setErrorWithAnimation(tilEmail, "Email is required");
      isValid = false;
    } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      setErrorWithAnimation(tilEmail, "Invalid email address");
      isValid = false;
    }

    // Username validation with animation
    if (TextUtils.isEmpty(username)) {
      setErrorWithAnimation(tilUsername, "Username is required");
      isValid = false;
    } else if (username.length() < 3) {
      setErrorWithAnimation(tilUsername, "Username must contain at least 3 characters");
      isValid = false;
    } else if (username.length() > 20) {
      setErrorWithAnimation(tilUsername, "Username cannot contain more than 20 characters");
      isValid = false;
    }

    // Password validation with animation
    if (TextUtils.isEmpty(password)) {
      setErrorWithAnimation(tilPassword, "Password is required");
      isValid = false;
    } else if (password.length() < 6) {
      setErrorWithAnimation(tilPassword, "Password must contain at least 6 characters");
      isValid = false;
    }

    return isValid;
  }

  private void setErrorWithAnimation(TextInputLayout layout, String error) {
    // Shake animation for error
    ObjectAnimator shake =
        ObjectAnimator.ofFloat(layout, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
    shake.setDuration(600);
    shake.start();

    layout.setError(error);
  }

  private void signUp() {
    // Disable button during registration with loading animation
    btnRegister.setEnabled(false);

    // Create loading animation
    animateLoadingButton();

    final String email = Objects.requireNonNull(etEmail.getText()).toString().trim().toLowerCase();
    final String username = Objects.requireNonNull(etUsername.getText()).toString().trim();
    String password = Objects.requireNonNull(etPassword.getText()).toString();

    DBRef.Auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(
            RegisterActivity.this,
            task -> {
              // Re-enable button
              btnRegister.setEnabled(true);
              btnRegister.setText(getString(R.string.register));

              if (task.isSuccessful()) {
                // Show celebration animation
                showCelebrationAnimation(username);

                // Send success notification
                sendSuccessNotification(username);

                // Save user data to database
                User user = new User(email, username);
                DBRef.refUsers.child(email.replace('.', ' ')).setValue(user);

                // Navigate to login screen after celebration
                new Handler(Looper.getMainLooper())
                    .postDelayed(
                        () -> {
                          Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                          startActivity(intent);
                          finish();
                        },
                        3000);

              } else {
                handleRegistrationError(task.getException());
              }
            });
  }

  @SuppressLint("SetTextI18n")
  private void animateLoadingButton() {
    btnRegister.setText("register...");

    // Pulsing animation
    ObjectAnimator pulse = ObjectAnimator.ofFloat(btnRegister, "alpha", 1f, 0.5f);
    pulse.setDuration(500);
    pulse.setRepeatCount(ObjectAnimator.INFINITE);
    pulse.setRepeatMode(ObjectAnimator.REVERSE);
    pulse.start();
  }

  @SuppressLint("SetTextI18n")
  private void showCelebrationAnimation(String username) {
    celebrationLayout.setVisibility(View.VISIBLE);

    // Fade in celebration
    ObjectAnimator fadeIn = ObjectAnimator.ofFloat(celebrationLayout, "alpha", 0f, 1f);
    fadeIn.setDuration(500);

    // Scale animation for celebration text
    TextView celebrationText = (TextView) celebrationLayout.getChildAt(0);
    TextView welcomeText = (TextView) celebrationLayout.getChildAt(1);

    ObjectAnimator scaleX = ObjectAnimator.ofFloat(celebrationText, "scaleX", 0f, 1.2f, 1f);
    ObjectAnimator scaleY = ObjectAnimator.ofFloat(celebrationText, "scaleY", 0f, 1.2f, 1f);

    AnimatorSet scaleSet = new AnimatorSet();
    scaleSet.playTogether(scaleX, scaleY);
    scaleSet.setDuration(800);
    scaleSet.setInterpolator(new BounceInterpolator());

    // Welcome text slide up
    ObjectAnimator slideUp = ObjectAnimator.ofFloat(welcomeText, "translationY", 100f, 0f);
    slideUp.setDuration(600);
    slideUp.setStartDelay(400);
    slideUp.setInterpolator(new OvershootInterpolator());

    // Start all animations
    fadeIn.start();
    scaleSet.start();
    slideUp.start();

    // Update welcome text with username
    welcomeText.setText("Welcome " + username + "!");

    // Confetti-like effect (simple rotation animation)
    createConfettiEffect();
  }

  private void createConfettiEffect() {
    // Create multiple small views for confetti effect
    ViewGroup rootLayout = findViewById(android.R.id.content);

    for (int i = 0; i < 10; i++) {
      View confetti = new View(this);
      confetti.setBackgroundColor(
          Color.HSVToColor(new float[] {(float) (Math.random() * 360), 1f, 1f}));
      confetti.setLayoutParams(new ViewGroup.LayoutParams(20, 20));

      // Random position
      confetti.setX((float) (Math.random() * getResources().getDisplayMetrics().widthPixels));
      confetti.setY((float) (Math.random() * getResources().getDisplayMetrics().heightPixels));

      rootLayout.addView(confetti);

      // Animate confetti
      ObjectAnimator fall = ObjectAnimator.ofFloat(confetti, "translationY", 0f, 500f);
      ObjectAnimator rotate = ObjectAnimator.ofFloat(confetti, "rotation", 0f, 360f * 3);
      ObjectAnimator fade = ObjectAnimator.ofFloat(confetti, "alpha", 1f, 0f);

      AnimatorSet confettiSet = new AnimatorSet();
      confettiSet.playTogether(fall, rotate, fade);
      confettiSet.setDuration(2000);
      confettiSet.setStartDelay((long) (Math.random() * 500));

      confettiSet.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              rootLayout.removeView(confetti);
            }
          });

      confettiSet.start();
    }
  }

  private void handleRegistrationError(Exception exception) {
    String errorMessage;

    if (exception instanceof FirebaseAuthUserCollisionException) {
      errorMessage = "Email already exists in the system. Try to sign in.";
      setErrorWithAnimation(tilEmail, errorMessage);
    } else if (exception instanceof FirebaseAuthWeakPasswordException) {
      errorMessage = "Password is too weak. Choose a stronger password (at least 6 characters).";
      setErrorWithAnimation(tilPassword, errorMessage);
    } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
      errorMessage = "Email address is invalid.";
      setErrorWithAnimation(tilEmail, errorMessage);
    } else if (exception instanceof FirebaseAuthException) {
      String errorCode = ((FirebaseAuthException) exception).getErrorCode();
      errorMessage = "Error: " + errorCode;
    } else {
      errorMessage = "Unknown registration error.";
    }

    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
  }

  private void sendSuccessNotification(String username) {
    NotificationCompat.Builder builder =
        new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_cake)
            .setContentTitle("Registration completed successfully!")
            .setContentText("Welcome " + username + "! Start creating amazing parties")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true);

    NotificationManager notificationManager =
        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    notificationManager.notify(1, builder.build());
  }
}
