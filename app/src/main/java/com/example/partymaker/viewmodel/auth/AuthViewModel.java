package com.example.partymaker.viewmodel.auth;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.User;
import com.example.partymaker.utils.auth.SecureAuthenticationManager;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import com.example.partymaker.utils.security.core.PasswordValidator;
import com.example.partymaker.viewmodel.BaseViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ViewModel for managing user authentication operations.
 *
 * <p>This ViewModel handles all authentication-related functionality including:
 *
 * <ul>
 *   <li>Email/password authentication (login and registration)
 *   <li>Google Sign-In integration
 *   <li>Password reset functionality
 *   <li>User profile management
 *   <li>Authentication state management
 * </ul>
 *
 * <p>Extends BaseViewModel to inherit common UI state management functionality.
 *
 * @author PartyMaker Team
 * @version 2.0
 * @see BaseViewModel
 * @since 1.0
 */
public class AuthViewModel extends BaseViewModel {
  /** Tag for logging specific to AuthViewModel */
  private static final String TAG = "AuthViewModel";

  /** Request code for Google Sign-In */
  private static final int RC_SIGN_IN = 9001;

  /** Minimum username length */
  private static final int MIN_USERNAME_LENGTH = 2;

  private final MutableLiveData<Boolean> isAuthenticated = new MutableLiveData<>(false);
  private final MutableLiveData<FirebaseUser> currentUser = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isRegistrationMode = new MutableLiveData<>(false);
  private final MutableLiveData<Boolean> isPasswordResetSent = new MutableLiveData<>(false);

  private final FirebaseAuth auth;
  private GoogleSignInClient googleSignInClient;

  /**
   * Constructor for AuthViewModel.
   *
   * @param application The application context
   */
  public AuthViewModel(@NonNull Application application) {
    super(application);
    auth = FirebaseAuth.getInstance();

    // Don't automatically authenticate - let the user choose to login
    // This prevents auto-login when navigating from registration to login screen
    FirebaseUser user = auth.getCurrentUser();
    if (user != null) {
      Log.d(TAG, "User already authenticated: " + user.getEmail());
      currentUser.setValue(user);
      // Don't set isAuthenticated to true automatically - user must explicitly login
      // isAuthenticated.setValue(true);
    }

    setupGoogleSignIn();
  }

  /** Sets up Google Sign-In configuration. */
  private void setupGoogleSignIn() {
    try {
      GoogleSignInOptions gso =
          new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
              .requestIdToken(
                  getApplication().getString(com.example.partymaker.R.string.default_web_client_id))
              .requestEmail()
              .build();

      googleSignInClient = GoogleSignIn.getClient(getApplication(), gso);
      Log.d(TAG, "Google Sign-In configured successfully");
    } catch (Exception e) {
      Log.e(TAG, "Failed to setup Google Sign-In", e);
    }
  }

  /**
   * Returns the authentication state as LiveData.
   *
   * @return LiveData<Boolean> indicating whether user is authenticated
   */
  public LiveData<Boolean> getIsAuthenticated() {
    return isAuthenticated;
  }

  /**
   * Returns the current FirebaseUser as LiveData.
   *
   * @return LiveData<FirebaseUser> containing current user, null if not authenticated
   */
  public LiveData<FirebaseUser> getCurrentUser() {
    return currentUser;
  }

  /**
   * Returns whether the auth UI is in registration mode.
   *
   * @return LiveData<Boolean> indicating registration mode state
   */
  public LiveData<Boolean> getIsRegistrationMode() {
    return isRegistrationMode;
  }

  /**
   * Returns whether a password reset email has been sent.
   *
   * @return LiveData<Boolean> indicating password reset email status
   */
  public LiveData<Boolean> getIsPasswordResetSent() {
    return isPasswordResetSent;
  }

  /**
   * Returns the configured Google Sign-In client.
   *
   * @return GoogleSignInClient for initiating Google Sign-In flow
   */
  public GoogleSignInClient getGoogleSignInClient() {
    return googleSignInClient;
  }

  /**
   * Sets the registration mode state.
   *
   * @param isRegistration true to show registration UI, false for login UI
   */
  public void setRegistrationMode(boolean isRegistration) {
    Log.d(TAG, "Setting registration mode: " + isRegistration);
    isRegistrationMode.setValue(isRegistration);
  }

  /**
   * Authenticates user with email and password.
   *
   * @param email The user's email address
   * @param password The user's password
   */
  public void loginWithEmail(@NonNull String email, @NonNull String password) {
    // Input validation
    if (email.trim().isEmpty()) {
      setError("Email is required");
      return;
    }

    if (password.trim().isEmpty()) {
      setError("Password is required");
      return;
    }

    if (!isValidEmail(email)) {
      setError("Please enter a valid email address");
      return;
    }

    Log.d(TAG, "Attempting to login with email: " + email);

    executeIfNotLoading(
        () -> {
          setLoading(true);
          clearMessages();

          auth.signInWithEmailAndPassword(email.trim(), password)
              .addOnCompleteListener(
                  task ->
                      ThreadUtils.runOnMainThread(
                          () -> {
                            setLoading(false);
                            if (task.isSuccessful()) {
                              handleSuccessfulLogin("");
                            } else {
                              handleAuthError("Email login failed", task.getException());
                            }
                          }));
        });
  }

  public void registerWithEmail(
      String email, String password, String confirmPassword, String username) {
    if (email == null || email.trim().isEmpty()) {
      setError("Email is required");
      return;
    }

    if (password == null || password.trim().isEmpty()) {
      setError("Password is required");
      return;
    }

    if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
      setError("Please confirm your password");
      return;
    }

    if (username == null || username.trim().isEmpty()) {
      setError("Username is required");
      return;
    }

    if (!password.equals(confirmPassword)) {
      setError("Passwords do not match");
      return;
    }

    // Enhanced password validation
    PasswordValidator.ValidationResult passwordValidation = PasswordValidator.validate(password);
    if (!passwordValidation.isValid) {
      setError(String.join(". ", passwordValidation.errors));
      return;
    }

    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      setError("Please enter a valid email address");
      return;
    }

    Log.d(TAG, "Attempting to register with email: " + email);
    setLoading(true);
    clearMessages();

    auth.createUserWithEmailAndPassword(email.trim(), password)
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful()) {
                Log.d(TAG, "Email registration successful");
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                  createUserProfile(user, username.trim());
                }
              } else {
                setLoading(false);
                Log.e(TAG, "Email registration failed", task.getException());
                String error =
                    task.getException() != null
                        ? task.getException().getMessage()
                        : "Registration failed";
                setError(error);
              }
            });
  }

  public void signInWithGoogle(Task<GoogleSignInAccount> completedTask) {
    try {
      GoogleSignInAccount account = completedTask.getResult(ApiException.class);
      Log.d(TAG, "Google sign in successful, authenticating with Firebase");
      firebaseAuthWithGoogle(account.getIdToken());
    } catch (ApiException e) {
      Log.e(TAG, "Google sign in failed", e);
      setLoading(false);
      setError("Google sign in failed: " + e.getMessage());
    }
  }

  private void firebaseAuthWithGoogle(String idToken) {
    AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

    auth.signInWithCredential(credential)
        .addOnCompleteListener(
            task -> {
              setLoading(false);
              if (task.isSuccessful()) {
                Log.d(TAG, "Firebase authentication with Google successful");
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                  currentUser.setValue(user);
                  isAuthenticated.setValue(true);

                  String username =
                      user.getDisplayName() != null ? user.getDisplayName() : "Google User";
                  checkAndCreateUserProfile(user, username);
                }
              } else {
                Log.e(TAG, "Firebase authentication with Google failed", task.getException());
                String error =
                    task.getException() != null
                        ? task.getException().getMessage()
                        : "Google authentication failed";
                setError(error);
              }
            });
  }

  private void checkAndCreateUserProfile(FirebaseUser firebaseUser, String username) {
    String userKey = Objects.requireNonNull(firebaseUser.getEmail()).replace('.', ' ');

    // First check if user already exists
    DBRef.refUsers
        .child(userKey)
        .get()
        .addOnCompleteListener(
            task -> {
              setLoading(false);
              if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                  // User already exists, just sign in
                  Log.d(TAG, "Existing user signed in with Google");
                  currentUser.setValue(firebaseUser);
                  isAuthenticated.setValue(true);
                  // Success message handled by LoginActivity
                } else {
                  // New user, create profile
                  Log.d(TAG, "New user, creating profile");
                  createNewUserProfile(firebaseUser, username);
                }
              } else {
                Log.e(TAG, "Failed to check if user exists", task.getException());
                setError("Failed to verify user profile");
              }
            });
  }

  private void createNewUserProfile(FirebaseUser firebaseUser, String username) {
    String userKey = Objects.requireNonNull(firebaseUser.getEmail()).replace('.', ' ');

    User user = new User();
    user.setUserKey(userKey);
    user.setEmail(firebaseUser.getEmail());
    user.setUsername(username);

    if (firebaseUser.getPhotoUrl() != null) {
      user.setProfileImageUrl(firebaseUser.getPhotoUrl().toString());
    }

    Map<String, Object> userMap = new HashMap<>();
    userMap.put("email", user.getEmail());
    userMap.put("username", user.getUsername());
    if (user.getProfileImageUrl() != null) {
      userMap.put("profileImageUrl", user.getProfileImageUrl());
    }

    DBRef.refUsers
        .child(userKey)
        .setValue(userMap)
        .addOnCompleteListener(
            task -> {
              setLoading(false); // Always stop loading regardless of success/failure
              if (task.isSuccessful()) {
                Log.d(TAG, "New user profile created successfully");
                currentUser.setValue(firebaseUser);
                isAuthenticated.setValue(true);
                setSuccess("Account created successfully");
              } else {
                Log.e(TAG, "Failed to create user profile", task.getException());
                setError("Failed to create user profile");
              }
            });
  }

  private void createUserProfile(FirebaseUser firebaseUser, String username) {
    // This method is used for email registration - always creates new profile
    createNewUserProfile(firebaseUser, username);
  }

  public void resetPassword(String email) {
    if (email == null || email.trim().isEmpty()) {
      setError("Email is required");
      return;
    }

    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      setError("Please enter a valid email address");
      return;
    }

    Log.d(TAG, "Sending password reset email to: " + email);
    setLoading(true);
    clearMessages();

    auth.sendPasswordResetEmail(email.trim())
        .addOnCompleteListener(
            task -> {
              setLoading(false);
              if (task.isSuccessful()) {
                Log.d(TAG, "Password reset email sent successfully");
                isPasswordResetSent.setValue(true);
                setSuccess("Password reset email sent");
              } else {
                Log.e(TAG, "Failed to send password reset email", task.getException());
                String error =
                    task.getException() != null
                        ? task.getException().getMessage()
                        : "Failed to send reset email";
                setError(error);
              }
            });
  }

  /**
   * Signs out the current user and clears all authentication data.
   *
   * @param context The application context for clearing secure data
   */
  public void signOut(@NonNull Context context) {
    Log.d(TAG, "Signing out user");

    executeIfNotLoading(
        () -> {
          setLoading(true);

          // Sign out from Firebase
          auth.signOut();

          // Sign out from Google if available
          if (googleSignInClient != null) {
            googleSignInClient
                .signOut()
                .addOnCompleteListener(task -> Log.d(TAG, "Google sign out completed"));
          }

          // Clear secure storage
          SecureAuthenticationManager.clearAuthData(context);

          // Update UI state
          currentUser.setValue(null);
          isAuthenticated.setValue(false);
          clearMessages();
          setLoading(false);
          setSuccess("Signed out successfully");
        });
  }

  public void clearPasswordResetStatus() {
    isPasswordResetSent.setValue(false);
  }

  public boolean isValidEmail(String email) {
    return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
  }

  public boolean isValidPassword(String password) {
    if (password == null) return false;
    PasswordValidator.ValidationResult result = PasswordValidator.validate(password);
    return result.isValid;
  }

  public boolean isValidUsername(String username) {
    return username != null && username.trim().length() >= 2;
  }

  /**
   * Handles successful login operations.
   *
   * @param message Success message to display
   */
  private void handleSuccessfulLogin(String message) {
    FirebaseUser user = auth.getCurrentUser();
    if (user != null) {
      currentUser.setValue(user);
      isAuthenticated.setValue(true);
      // Success message now handled by LoginActivity to avoid duplicates
      Log.d(TAG, "Login successful: " + (message.isEmpty() ? "via ViewModel" : message));
    } else {
      setError("Authentication error: User not found");
      Log.e(TAG, "Login success but user is null");
    }
  }

  /**
   * Handles authentication errors with proper error messaging.
   *
   * @param baseMessage Base error message
   * @param exception The exception that occurred
   */
  private void handleAuthError(String baseMessage, Exception exception) {
    String errorMessage = baseMessage;
    if (exception != null) {
      String exceptionMessage = exception.getMessage();
      if (exceptionMessage != null && !exceptionMessage.isEmpty()) {
        errorMessage = exceptionMessage;
      }
      Log.e(TAG, baseMessage, exception);
    } else {
      Log.e(TAG, baseMessage);
    }
    setError(errorMessage);
  }

  /** Clears authentication state to prevent auto-login */
  public void clearAuthenticationState() {
    Log.d(TAG, "Clearing authentication state");
    isAuthenticated.setValue(false);
    currentUser.setValue(null);
    clearError();
    clearMessages();
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    Log.d(TAG, "AuthViewModel cleared");
  }
}
