package com.example.partymaker.ui.features.auth;

import static com.example.partymaker.utils.core.AppConstants.Preferences.IS_CHECKED;
import static com.example.partymaker.utils.core.AppConstants.Preferences.PREFS_NAME;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import androidx.appcompat.app.ActionBar;
import com.example.partymaker.ui.base.BaseActivity;
import com.example.partymaker.R;
import com.example.partymaker.data.api.NetworkManager;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.ui.features.core.MainActivity;
import com.example.partymaker.utils.auth.AuthenticationManager;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import com.example.partymaker.utils.ui.components.LoadingStateManager;
import com.example.partymaker.utils.ui.components.UiStateManager;
import com.example.partymaker.viewmodel.auth.AuthViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Activity for user login, including email/password and Google sign-in. Handles authentication,
 * navigation, and UI state.
 */
public class LoginActivity extends BaseActivity<AuthViewModel> {

  /** Request code for Google sign-in. */
  private static final int RC_SIGN_IN = 9001;

  /** Email input field. */
  private TextInputEditText etEmail;

  /** Password input field. */
  private TextInputEditText etPassword;

  /** About button. */
  private ImageButton btnAbout;

  /** Login button. */
  private MaterialButton btnLogin;

  /** Register button. */
  private MaterialButton btnPress;

  /** Reset password button. */
  private MaterialButton btnResetPass;

  /** Remember me checkbox. */
  private MaterialCheckBox cbRememberMe;

  /** Google sign-in button. */
  private SignInButton btnGoogleSignIn;

  /** Google sign-in client. */
  private GoogleSignInClient mGoogleSignInClient;

  /** Firebase authentication instance. */
  private FirebaseAuth mAuth;


  /** Loading state manager */
  private LoadingStateManager loadingStateManager;

  /** Root view for UI feedback */
  private View rootView;

  // BaseActivity implementation methods
  
  @Override
  protected int getLayoutId() {
    return R.layout.activity_auth_login;
  }

  @Override
  protected Class<AuthViewModel> getViewModelClass() {
    return AuthViewModel.class;
  }

  @Override
  protected String getActivityTitle() {
    return "Login";
  }

  @Override
  protected boolean shouldShowBackButton() {
    return false; // Login screen shouldn't have back button
  }

  @Override
  protected void initViews() {
    // Force set server URL to Render
    forceSetServerUrl();

    // Initialize Firebase Auth
    mAuth = FirebaseAuth.getInstance();

    // Initialize UI components
    initializeUiComponents();

    // Clear any previous authentication state to prevent auto-login
    clearPreviousAuthState();

    // Add debug option to reset test user password
    resetTestUserPassword();

    // Check server connectivity
    checkServerConnectivity();
  }

  @Override
  protected void setupObservers() {
    setupViewModelObservers();
  }

  @Override
  protected void setupClickListeners() {
    // This will be called from onActivityStart where eventHandler() is called
    // No need to duplicate the setup here
  }

  @Override
  protected void onActivityStart() {
    super.onActivityStart();

    // Configure Google Sign In
    GoogleSignInOptions gso =
        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build();

    mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    // this 2 lines disables the action bar only in this activity
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }

    // connection between XML and Java
    btnAbout = findViewById(R.id.btnAbout);
    etEmail = findViewById(R.id.etEmailL);
    etPassword = findViewById(R.id.etPasswordL);
    btnLogin = findViewById(R.id.btnLogin);
    btnPress = findViewById(R.id.btnPressL);
    cbRememberMe = findViewById(R.id.cbRememberMe);
    btnResetPass = findViewById(R.id.btnResetPass);
    btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

    // start animation on ImageButton btnAbout
    Animation myFadeInAnimation = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade_in);
    btnAbout.startAnimation(myFadeInAnimation);

    eventHandler();
  }

  /** Initialize UI components for better UX */
  private void initializeUiComponents() {
    rootView = findViewById(android.R.id.content);

    // Setup loading state manager for smooth transitions
    setupLoadingStateManager();
  }

  /** Setup loading state manager */
  private void setupLoadingStateManager() {
    android.widget.ProgressBar progressBar = findViewById(R.id.progressBar);
    if (progressBar == null) {
      progressBar = new android.widget.ProgressBar(this);
      progressBar.setVisibility(View.GONE);
    }

    loadingStateManager =
        new LoadingStateManager.Builder()
            .contentView(findViewById(R.id.etEmailL)) // Use existing view as content
            .progressBar(progressBar)
            .build();
  }

  /** Sets up observers for AuthViewModel LiveData */
  private void setupViewModelObservers() {
    viewModel
        .getIsLoading()
        .observe(
            this,
            isLoading -> {
              btnLogin.setEnabled(!isLoading);
              btnGoogleSignIn.setEnabled(!isLoading);

              if (isLoading) {
                loadingStateManager.showLoading("Signing in...");
              } else {
                loadingStateManager.showContent();
              }
            });

    viewModel
        .getErrorMessage()
        .observe(
            this,
            errorMessage -> {
              if (errorMessage != null) {
                UiStateManager.showError(
                    rootView,
                    errorMessage,
                    () -> {
                      // Retry logic - clear error and enable retry
                      viewModel.clearError();
                    });
                loadingStateManager.showError(errorMessage);
                btnResetPass.setVisibility(View.VISIBLE);
              }
            });

    viewModel
        .getSuccessMessage()
        .observe(
            this,
            successMessage -> {
              if (successMessage != null) {
                UiStateManager.showSuccess(rootView, successMessage);
              }
            });

    viewModel
        .getIsAuthenticated()
        .observe(
            this,
            isAuthenticated -> {
              if (isAuthenticated) {
                UiStateManager.showSuccess(rootView, "Login successful!");

                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(IS_CHECKED, cbRememberMe.isChecked());
                editor.apply();

                // Mark that user explicitly logged in
                SharedPreferences prefs =
                    getSharedPreferences("PartyMakerPrefs", Context.MODE_PRIVATE);
                prefs.edit().putBoolean("user_explicitly_logged_in", true).apply();

                // Navigate after short delay to show success message
                ThreadUtils.runOnMainThreadDelayed(
                    () -> {
                      Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                      btnAbout.clearAnimation();
                      startActivity(intent);
                      finish();
                    },
                    800);
              }
            });
  }

  /** Forces the server URL to be set to Render */
  private void forceSetServerUrl() {
    String renderUrl = "https://partymaker.onrender.com";
    androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        .edit()
        .putString("server_url", renderUrl)
        .apply();
    Log.d("LoginActivity", "Forced server URL to: " + renderUrl);
  }

  /** Resets password for test user (1@1.com) to 123456 This is for debugging purposes only */
  private void resetTestUserPassword() {
    String testEmail = "1@1.com";
    String testPassword = "123456";

    // Try to sign in with email and password directly
    mAuth
        .signInWithEmailAndPassword(testEmail, testPassword)
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                Log.d("LoginActivity", "Test user login successful");
              } else {
                Log.d("LoginActivity", "Test user login failed, attempting to create user");

                // If login fails, try to create the user
                mAuth
                    .createUserWithEmailAndPassword(testEmail, testPassword)
                    .addOnCompleteListener(
                        createTask -> {
                          if (createTask.isSuccessful()) {
                            Log.d("LoginActivity", "Test user created successfully");
                            // Create user in database using Map instead of User object
                            Map<String, Object> testUserData = new HashMap<>();
                            testUserData.put("username", "Test User");
                            testUserData.put("email", testEmail);
                            testUserData.put("userKey", java.util.UUID.randomUUID().toString());

                            DBRef.refUsers
                                .child(testEmail.replace('.', ' '))
                                .setValue(testUserData);
                          } else {
                            Log.d(
                                "LoginActivity",
                                "Test user creation failed: "
                                    + (createTask.getException() != null
                                        ? createTask.getException().getMessage()
                                        : "unknown error"));
                          }
                        });
              }
            });
  }

  /** Checks connectivity to the server using NetworkManager */
  private void checkServerConnectivity() {
    ThreadUtils.runInBackground(
        () -> {
          try {
            String serverUrl =
                androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
                    .getString("server_url", "https://partymaker.onrender.com");

            Log.d("LoginActivity", "Checking server connectivity to: " + serverUrl);

            NetworkManager networkManager = NetworkManager.getInstance();
            boolean isReachable =
                networkManager.isServerReachable(serverUrl + "/api/firebase/health", 10000);

            ThreadUtils.runOnMainThread(
                () -> {
                  if (isReachable) {
                    Log.d("LoginActivity", "Server is reachable");
                    // Don't show info message to avoid cluttering UI
                    // UiStateManager.showInfo(rootView, "Connected to server");
                  } else {
                    Log.w("LoginActivity", "Server is not reachable");
                    // Don't show warning message after logout to avoid confusing users
                    // UiStateManager.showWarning(
                    //     rootView, "Server connection issues - some features may be limited");
                  }
                });
          } catch (Exception e) {
            Log.e("LoginActivity", "Error checking server connectivity", e);
            ThreadUtils.runOnMainThread(
                () -> {
                  // Don't show warning message after logout to avoid confusing users
                  // UiStateManager.showWarning(rootView, "Unable to verify server connection");
                });
          }
        });
  }

  /** Handles all button click events and login logic. */
  private void eventHandler() {
    btnLogin.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            SignIn();
          }

          // connection between firebase and login button using ViewModel
          private void SignIn() {
            String email = Objects.requireNonNull(etEmail.getText()).toString();
            String password = Objects.requireNonNull(etPassword.getText()).toString();

            viewModel.loginWithEmail(email, password);

            // Set user session using AuthHelper when login succeeds
            if (!email.isEmpty()) {
              AuthenticationManager.setCurrentUserSession(LoginActivity.this, email);
            }
          }
        });

    // Google Sign In button click listener - use ViewModel's GoogleSignInClient
    btnGoogleSignIn.setOnClickListener(
        v -> {
          Intent signInIntent = viewModel.getGoogleSignInClient().getSignInIntent();
          startActivityForResult(signInIntent, RC_SIGN_IN);
        });

    // Press Here Onclick
    btnPress.setOnClickListener(
        view -> {
          // when click on "press here" it takes you to RegisterActivity
          Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
          btnAbout.clearAnimation();
          startActivity(i);
          overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
        });
    btnResetPass.setOnClickListener(
        v -> {
          Intent i = new Intent(LoginActivity.this, ResetPasswordActivity.class);
          btnAbout.clearAnimation();
          startActivity(i);
        });
    btnAbout.setOnClickListener(
        v -> {
          Intent i = new Intent(LoginActivity.this, IntroActivity.class);
          startActivity(i);
        });
  }

  /** Handles the result of Google sign-in intent using ViewModel. */
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == RC_SIGN_IN) {
      Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
      viewModel.signInWithGoogle(task);
    }
  }

  /** Clears previous authentication state to prevent auto-login */
  private void clearPreviousAuthState() {
    try {
      Log.d(TAG, "Clearing previous authentication state to prevent auto-login");

      // Reset the ViewModel's authentication state
      if (viewModel != null) {
        viewModel.clearAuthenticationState();
      }

      Log.d(TAG, "Authentication state cleared successfully");
    } catch (Exception e) {
      Log.e(TAG, "Error clearing authentication state", e);
    }
  }
}
