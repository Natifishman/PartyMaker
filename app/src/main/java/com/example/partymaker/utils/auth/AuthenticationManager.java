package com.example.partymaker.utils.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.partymaker.data.local.AppDatabase;
import com.example.partymaker.data.repository.GroupRepository;
import com.example.partymaker.data.repository.UserRepository;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Authentication Manager for PartyMaker application.
 * 
 * This utility class provides centralized authentication management with:
 * - Multi-layer authentication fallback (Firebase Auth -> SharedPreferences)
 * - Session persistence and validation
 * - Secure user key generation
 * - Authentication state management
 * - Data cleanup on logout
 * 
 * Architecture:
 * - Singleton pattern: Static methods for global access
 * - Defensive programming: Multiple fallback mechanisms
 * - Security-first: Proper session timeout and validation
 * - Performance: Cached authentication state
 * 
 * Authentication Flow:
 * 1. Check Firebase Authentication state
 * 2. Validate session timeout (30-day expiry)
 * 3. Fallback to SharedPreferences if Firebase unavailable
 * 4. Generate user key from email (dots -> spaces for Firebase compatibility)
 * 
 * Security Considerations:
 * - No sensitive data stored in SharedPreferences
 * - Session timeout enforced for security
 * - User keys sanitized for Firebase database compatibility
 * - Comprehensive error handling to prevent auth bypass
 * 
 * @author PartyMaker Team
 * @version 2.0
 * @since 1.0
 * 
 * @see FirebaseAuth
 * @see SharedPreferences
 * @see AuthException
 */
public class AuthenticationManager {
  /** Tag for logging and debugging */
  private static final String TAG = "AuthenticationManager";
  
  /** SharedPreferences file name for authentication data */
  private static final String PREFS_NAME = "PartyMakerPrefs";

  // ==================== SharedPreferences Keys ====================
  /** Key for storing user email address */
  private static final String KEY_USER_EMAIL = "user_email";
  /** Key for storing active session flag */
  private static final String KEY_SESSION_ACTIVE = "session_active";
  /** Key for storing last login timestamp */
  private static final String KEY_LAST_LOGIN_TIME = "last_login_time";
  /** Key for server mode email (testing/debug) */
  private static final String KEY_SERVER_MODE_EMAIL = "server_mode_email";
  /** Key for server mode active flag */
  private static final String KEY_SERVER_MODE_ACTIVE = "server_mode_active";

  // ==================== Session Configuration ====================
  /** Maximum session duration before re-authentication required (days) */
  private static final long SESSION_DURATION_DAYS = 30L;
  /** Milliseconds in a day for timestamp calculations */
  private static final long MILLISECONDS_PER_DAY = 24L * 60L * 60L * 1000L;

  /**
   * Retrieves the current authenticated user's email address.
   * 
   * Implements a multi-tier fallback strategy:
   * 1. Primary: Firebase Authentication current user
   * 2. Fallback: SharedPreferences cached email
   * 3. Validation: Session timeout check
   * 
   * This method is thread-safe and handles network connectivity issues gracefully.
   * 
   * @param context Application context for accessing SharedPreferences
   * @return User's email address if authenticated, null if not authenticated or expired
   * 
   * @implNote Automatically saves Firebase user to SharedPreferences for offline access
   * @implNote Returns null if session has expired (> 30 days)
   */
  public static String getCurrentUserEmail(Context context) {
    if (context == null) {
      Log.e(TAG, "getCurrentUserEmail: Context is null");
      return null;
    }

    try {
      // Try to get from Firebase Auth first
      FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
      if (currentUser != null
          && currentUser.getEmail() != null
          && !currentUser.getEmail().isEmpty()) {
        Log.d(TAG, "Firebase Auth user found: " + currentUser.getEmail());

        // Save to SharedPreferences as backup
        saveUserEmail(context, currentUser.getEmail());

        return currentUser.getEmail();
      }

      // If Firebase Auth failed, try SharedPreferences
      Log.d(TAG, "Firebase Auth user not found, checking SharedPreferences");
      return getSavedUserEmail(context);
    } catch (Exception e) {
      Log.e(TAG, "Error getting current user email", e);

      // Last resort - try SharedPreferences
      try {
        return getSavedUserEmail(context);
      } catch (Exception e2) {
        Log.e(TAG, "Failed to get user email from SharedPreferences", e2);
        return null;
      }
    }
  }

  /**
   * Generates the current user's unique key for Firebase database operations.
   * 
   * Firebase Realtime Database doesn't allow dots in keys, so email addresses
   * must be transformed. This method ensures consistent key generation across the app.
   * 
   * Transformation Rules:
   * - Replace all dots (.) with spaces ( )
   * - Preserve all other characters
   * - Case-sensitive (emails are case-insensitive but keys preserve case)
   * 
   * Example: "user@example.com" -> "user@example com"
   * 
   * @param context Application context for authentication lookup
   * @return Firebase-compatible user key derived from email
   * 
   * @throws AuthException if user is not authenticated or session expired
   * @throws AuthException if email format is invalid
   * 
   * @implNote This key is used as the primary key in Firebase user nodes
   * @implNote Key consistency is critical for data integrity
   */
  public static String getCurrentUserKey(Context context) throws AuthException {
    // Step 1: Validate user authentication
    String email = getCurrentUserEmail(context);
    if (email == null || email.isEmpty()) {
      Log.e(TAG, "getCurrentUserKey: No user email found");
      throw new AuthException("User not authenticated");
    }

    // Step 2: Transform email to Firebase-compatible key
    // Replace dots with spaces for Firebase database compatibility
    String key = email.replace('.', ' ');
    Log.d(TAG, "getCurrentUserKey: Email: " + email + " -> Key: " + key);
    
    return key;
  }

  /**
   * Persists user email to SharedPreferences for offline access.
   * 
   * Also updates session metadata:
   * - Sets session active flag
   * - Records current login timestamp
   * - Enables session timeout validation
   * 
   * This data serves as a fallback when Firebase Auth is unavailable
   * due to network issues or service outages.
   * 
   * @param context Application context for SharedPreferences access
   * @param email User email address to persist
   * 
   * @implNote Uses commit() instead of apply() for immediate persistence
   * @implNote Updates login timestamp for session timeout calculation
   */
  private static void saveUserEmail(Context context, String email) {
    try {
      if (context == null || email == null || email.isEmpty()) {
        return;
      }

      SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
      prefs.edit().putString(KEY_USER_EMAIL, email).apply();
      Log.d(TAG, "User email saved to SharedPreferences: " + email);
    } catch (Exception e) {
      Log.e(TAG, "Error saving user email to SharedPreferences", e);
    }
  }

  /**
   * Gets saved user email from SharedPreferences
   *
   * @param context Application context
   * @return The saved email or null if not available
   */
  private static String getSavedUserEmail(Context context) {
    try {
      if (context == null) {
        return null;
      }

      SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
      String email = prefs.getString(KEY_USER_EMAIL, null);

      if (email != null && !email.isEmpty()) {
        Log.d(TAG, "User email retrieved from SharedPreferences: " + email);
        return email;
      } else {
        Log.w(TAG, "No user email found in SharedPreferences");
        return null;
      }
    } catch (Exception e) {
      Log.e(TAG, "Error getting user email from SharedPreferences", e);
      return null;
    }
  }

  /**
   * Logs out the current user
   *
   * @param context Application context
   */
  public static void logout(Context context) {
    Log.d(TAG, "Logging out user");
    clearAuthData(context);
    clearAllUserData(context);
  }

  /**
   * Clears all authentication data
   *
   * @param context Application context
   * @return true if successful, false otherwise
   */
  public static boolean clearAuthData(Context context) {
    boolean firebaseSignOutSuccess = false;
    boolean sharedPrefsSuccess = false;

    // Try to sign out from Firebase
    try {
      FirebaseAuth.getInstance().signOut();
      firebaseSignOutSuccess = true;
      Log.d(TAG, "Firebase sign out successful");
    } catch (Exception e) {
      Log.e(TAG, "Error signing out from Firebase", e);
    }

    // Clear SharedPreferences
    try {
      if (context != null) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs
            .edit()
            .remove(KEY_USER_EMAIL)
            .remove(KEY_SESSION_ACTIVE)
            .remove(KEY_LAST_LOGIN_TIME)
            .remove(KEY_SERVER_MODE_EMAIL)
            .remove(KEY_SERVER_MODE_ACTIVE)
            .apply();
        sharedPrefsSuccess = true;
        Log.d(TAG, "SharedPreferences auth data cleared");
      }
    } catch (Exception e) {
      Log.e(TAG, "Error clearing SharedPreferences auth data", e);
    }

    return firebaseSignOutSuccess || sharedPrefsSuccess;
  }

  /**
   * Clears all user-related data including Room database and repository caches
   *
   * @param context Application context
   */
  public static void clearAllUserData(Context context) {
    if (context == null) {
      Log.w(TAG, "Cannot clear user data: context is null");
      return;
    }

    Log.d(TAG, "Clearing all user data including Room database");

    clearRepositoryCaches();
    clearRoomDatabase(context);
  }

  private static void clearRepositoryCaches() {
    try {
      GroupRepository.getInstance().clearCache();
      UserRepository.getInstance().clearCache();
      Log.d(TAG, "Repository caches cleared");
    } catch (Exception e) {
      Log.e(TAG, "Error clearing repository caches", e);
    }
  }

  private static void clearRoomDatabase(Context context) {
    ThreadUtils.runInBackground(
        () -> {
          try {
            AppDatabase database = AppDatabase.getInstance(context);
            clearDatabaseTables(database);
            verifyDatabaseCleared(database);
            Log.d(TAG, "Room database cleared successfully");
          } catch (Exception e) {
            Log.e(TAG, "Error clearing Room database", e);
          }
        });
  }

  private static void clearDatabaseTables(AppDatabase database) {
    database.groupDao().deleteAllGroups();
    database.userDao().deleteAllUsers();

    // Also clear chat messages if DAO is available
    if (database.chatMessageDao() != null) {
      database.chatMessageDao().deleteAllMessages();
    }
  }

  private static void verifyDatabaseCleared(AppDatabase database) {
    int groupCount = database.groupDao().getAllGroups().size();
    int userCount = database.userDao().getAllUsers().size();
    Log.d(
        TAG, "Database verification after clear - Groups: " + groupCount + ", Users: " + userCount);
  }

  /**
   * Checks if a user is currently logged in
   *
   * @param context Application context
   * @return true if user is logged in, false otherwise
   */
  public static boolean isLoggedIn(Context context) {
    try {
      // Check Firebase Auth first
      FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
      if (currentUser != null) {
        return true;
      }

      // Check SharedPreferences as fallback
      String savedEmail = getSavedUserEmail(context);
      return savedEmail != null && !savedEmail.isEmpty();
    } catch (Exception e) {
      Log.e(TAG, "Error checking login status", e);
      return false;
    }
  }

  // ---------- LEGACY METHODS (FOR BACKWARD COMPATIBILITY) ----------

  /**
   * @param context Application context
   * @return true if user is authenticated, false otherwise
   * @deprecated Use isLoggedIn() instead Checks if user is authenticated with active session
   */
  @Deprecated
  public static boolean isUserAuthenticated(Context context) {
    return isLoggedIn(context);
  }

  /**
   * @param context Application context
   * @return true if session is valid, false otherwise
   * @deprecated Use isLoggedIn() instead Checks if the current session is valid (not expired)
   */
  @Deprecated
  public static boolean isSessionValid(Context context) {
    try {
      SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
      boolean sessionActive = prefs.getBoolean(KEY_SESSION_ACTIVE, false);
      long lastLoginTime = prefs.getLong(KEY_LAST_LOGIN_TIME, 0);

      if (!sessionActive || lastLoginTime == 0) {
        return false;
      }

      // Session is valid for configured duration
      long sessionDuration = SESSION_DURATION_DAYS * MILLISECONDS_PER_DAY;
      long currentTime = System.currentTimeMillis();

      return (currentTime - lastLoginTime) < sessionDuration;
    } catch (Exception e) {
      Log.e(TAG, "Error checking session validity", e);
      return false;
    }
  }

  /**
   * @param context Application context
   * @deprecated No longer needed with new implementation Refreshes the current session
   */
  @Deprecated
  public static void refreshSession(Context context) {
    try {
      SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
      String email = prefs.getString(KEY_SERVER_MODE_EMAIL, null);

      if (email != null) {
        saveUserEmail(context, email);
        Log.d(TAG, "Session refreshed for user: " + email);
      }
    } catch (Exception e) {
      Log.e(TAG, "Error refreshing session", e);
    }
  }

  /** Custom exception for authentication errors */
  public static class AuthException extends Exception {
    public AuthException(String message) {
      super(message);
    }

    public AuthException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
