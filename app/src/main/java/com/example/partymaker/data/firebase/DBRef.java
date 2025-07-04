package com.example.partymaker.data.firebase;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

import com.example.partymaker.data.model.User;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.data.model.ServerResponse;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.CompletableFuture;

/**
 * Class containing database references and operations
 * All database operations are routed through the server
 */
public class DBRef {
  private static final String TAG = "DBRef";
  private static FirebaseDatabase database;
  private static boolean isInitialized = false;
  private static DatabaseService databaseService;

  // Static block to initialize Firebase with offline support
  static {
    try {
      // Must call setPersistenceEnabled before any other Firebase Database usage
      FirebaseDatabase.getInstance().setPersistenceEnabled(true);
      
      // Set default cache size to 100MB
      FirebaseDatabase.getInstance().setPersistenceCacheSizeBytes(100 * 1024 * 1024);
      
      // Set connection timeout
      FirebaseDatabase.getInstance().setLogLevel(com.google.firebase.database.Logger.Level.DEBUG);
      
      Log.d(TAG, "Firebase offline persistence enabled with 100MB cache");
    } catch (Exception e) {
      Log.e(TAG, "Error enabling Firebase persistence", e);
    }
  }

  // Firebase references - these should only be used for authentication and path construction
  // Actual data operations should use DatabaseService
  public static FirebaseAuth Auth = FirebaseAuth.getInstance();
  private static FirebaseDatabase DataBase = FirebaseDatabase.getInstance();
  private static FirebaseStorage Storage = FirebaseStorage.getInstance();
  public static String CurrentUser;
  
  // Path constants
  public static final String PATH_GROUPS = "Groups";
  public static final String PATH_USERS = "Users";
  public static final String PATH_MESSAGES = "GroupsMessages";
  public static final String PATH_USER_IMAGES = "UsersImageProfile";
  
  // Legacy references - maintained for backward compatibility
  // These should be replaced with the getter methods in new code
  public static DatabaseReference refGroups = DataBase.getReference(PATH_GROUPS);
  public static DatabaseReference refUsers = DataBase.getReference(PATH_USERS);
  public static DatabaseReference refMessages = DataBase.getReference(PATH_MESSAGES);
  public static StorageReference refStorage = Storage.getReference();

  /**
   * Initialize the database and set offline mode
   * @param context Application context
   */
  public static void initialize(Context context) {
    if (isInitialized) {
      return;
    }
    
    try {
      // Enable offline capabilities
      FirebaseConfig.enableOfflineCapabilities();
      
      // Ensure database connection (will try to use emulator if needed)
      boolean connected = FirebaseConfig.ensureDatabaseConnection(context);
      Log.d(TAG, "Database connection initialized, status: " + (connected ? "Connected" : "Offline mode"));
      
      // Get database instance
      database = FirebaseDatabase.getInstance();
      
      // Initialize database service
      databaseService = DatabaseFactory.getDatabaseService(context);
      
      isInitialized = true;
    } catch (Exception e) {
      Log.e(TAG, "Error initializing database", e);
    }
  }

  /**
   * Get a reference to the database root
   * @return Reference to the database root
   */
  public static DatabaseReference getRootRef() {
    if (database == null) {
      database = FirebaseDatabase.getInstance();
    }
    return database.getReference();
  }

  /**
   * Get a reference to the users table
   * @return Reference to the users table
   */
  public static DatabaseReference getUsersRef() {
    return getRootRef().child(PATH_USERS);
  }

  /**
   * Get a reference to a specific user
   * @param userId User ID
   * @return Reference to the user
   */
  public static DatabaseReference getUserRef(String userId) {
    return getUsersRef().child(userId);
  }

  /**
   * Get a reference to the groups table
   * @return Reference to the groups table
   */
  public static DatabaseReference getGroupsRef() {
    return getRootRef().child(PATH_GROUPS);
  }

  /**
   * Get a reference to a specific group
   * @param groupId Group ID
   * @return Reference to the group
   */
  public static DatabaseReference getGroupRef(String groupId) {
    return getGroupsRef().child(groupId);
  }

  /**
   * Get a reference to the messages of a specific group
   * @param groupId Group ID
   * @return Reference to the group messages
   */
  public static DatabaseReference getGroupMessagesRef(String groupId) {
    return getRootRef().child(PATH_MESSAGES).child(groupId);
  }
  
  /**
   * Check if the database is initialized
   * @return true if the database is initialized, false otherwise
   */
  public static boolean isInitialized() {
    return isInitialized;
  }

  /**
   * Set a value in the database through the server
   * 
   * @param reference Database reference
   * @param value Value to set
   * @return Task that completes when the update is done
   */
  public static Task<Void> setValue(DatabaseReference reference, Object value) {
    return setValue(getPathFromReference(reference), value);
  }

  /**
   * Update multiple values in the database through the server
   * 
   * @param reference Database reference
   * @param updates Map of updates
   * @return Task that completes when the update is done
   */
  public static Task<Void> updateChildren(DatabaseReference reference, Map<String, Object> updates) {
    return updateChildren(getPathFromReference(reference), updates);
  }

  /**
   * Remove a value from the database through the server
   * 
   * @param reference Database reference
   * @return Task that completes when the deletion is done
   */
  public static Task<Void> removeValue(DatabaseReference reference) {
    return removeValue(getPathFromReference(reference));
  }

  /**
   * Check if an image exists in storage
   * @param path Image path
   * @param listener Callback for the result
   */
  public static void checkImageExists(String path, OnImageExistsListener listener) {
    StorageReference imageRef = Storage.getReference(PATH_USER_IMAGES).child(path);
    StorageUpdateServer.checkFileExists(imageRef, listener);
  }

  public interface OnImageExistsListener {
    void onImageExists(boolean exists);
  }

  /**
   * Upload a file to storage through the server
   * 
   * @param reference Storage reference
   * @param uri URI of the file to upload
   * @return Task that completes when the upload is done
   */
  public static Task<Uri> putFile(StorageReference reference, Uri uri) {
    return StorageUpdateServer.putFile(reference, uri);
  }

  /**
   * Get a download URL from storage through the server
   * 
   * @param reference Storage reference
   * @return Task that completes when the URL is received
   */
  public static Task<Uri> getDownloadUrl(StorageReference reference) {
    return StorageUpdateServer.getDownloadUrl(reference);
  }

  /**
   * Get the current user's email in a format suitable for database keys
   * @return Formatted email or empty string if no user is logged in
   */
  public static String getCurrentUserEmail() {
    FirebaseUser user = Auth.getCurrentUser();
    if (user != null && user.getEmail() != null) {
      return user.getEmail().replace('.', ' ');
    }
    return "";
  }

  /**
   * Add a value event listener with logging
   * 
   * @param reference The database reference
   * @param listener The value event listener
   * @return The value event listener for later removal
   */
  public static com.google.firebase.database.ValueEventListener addLoggingValueEventListener(
      DatabaseReference reference, 
      com.google.firebase.database.ValueEventListener listener) {
    
    String path = reference.toString();
    Log.d(TAG, "Adding value event listener to path: " + path);
    
    com.google.firebase.database.ValueEventListener wrappedListener = new com.google.firebase.database.ValueEventListener() {
      @Override
      public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
        Log.d(TAG, "Value event received for path: " + path);
        listener.onDataChange(dataSnapshot);
      }
      
      @Override
      public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
        Log.e(TAG, "Value event cancelled for path: " + path + ", error: " + databaseError.getMessage());
        listener.onCancelled(databaseError);
      }
    };
    
    reference.addValueEventListener(wrappedListener);
    return wrappedListener;
  }

  /**
   * Get a value from the database through the server
   * 
   * @param path Database path
   * @return Task that completes when the value is received
   */
  public static Task<ServerDataSnapshot> getValue(String path) {
    TaskCompletionSource<ServerDataSnapshot> taskCompletionSource = new TaskCompletionSource<>();
    
    if (databaseService == null) {
      taskCompletionSource.setException(new Exception("Database service not initialized"));
      return taskCompletionSource.getTask();
    }
    
    databaseService.getValue(path)
        .thenAccept(response -> {
          if (response.isSuccess()) {
            taskCompletionSource.setResult(new ServerDataSnapshot(response.getData()));
          } else {
            taskCompletionSource.setException(new Exception(response.getMessage()));
          }
        })
        .exceptionally(throwable -> {
          Log.e(TAG, "Error getting value at path: " + path, throwable);
          taskCompletionSource.setException(new Exception(throwable.getMessage()));
          return null;
        });
    
    return taskCompletionSource.getTask();
  }

  /**
   * Get children from the database through the server
   * 
   * @param path Database path
   * @return Task that completes when the children are received
   */
  public static Task<ServerDataSnapshot> getChildren(String path) {
    TaskCompletionSource<ServerDataSnapshot> taskCompletionSource = new TaskCompletionSource<>();
    
    if (databaseService == null) {
      taskCompletionSource.setException(new Exception("Database service not initialized"));
      return taskCompletionSource.getTask();
    }
    
    databaseService.getChildren(path)
        .thenAccept(response -> {
          if (response.isSuccess()) {
            taskCompletionSource.setResult(new ServerDataSnapshot(response.getData()));
          } else {
            taskCompletionSource.setException(new Exception(response.getMessage()));
          }
        })
        .exceptionally(throwable -> {
          Log.e(TAG, "Error getting children at path: " + path, throwable);
          taskCompletionSource.setException(new Exception(throwable.getMessage()));
          return null;
        });
    
    return taskCompletionSource.getTask();
  }

  /**
   * Set a value in the database through the server
   * 
   * @param path Database path
   * @param value Value to set
   * @return Task that completes when the update is done
   */
  public static Task<Void> setValue(String path, Object value) {
    TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
    
    if (databaseService == null) {
      taskCompletionSource.setException(new Exception("Database service not initialized"));
      return taskCompletionSource.getTask();
    }
    
    databaseService.setValue(path, value)
        .thenAccept(response -> {
          if (response.isSuccess()) {
            taskCompletionSource.setResult(null);
          } else {
            taskCompletionSource.setException(new Exception(response.getMessage()));
          }
        })
        .exceptionally(throwable -> {
          Log.e(TAG, "Error setting value at path: " + path, throwable);
          taskCompletionSource.setException(new Exception(throwable.getMessage()));
          return null;
        });
    
    return taskCompletionSource.getTask();
  }

  /**
   * Update multiple values in the database through the server
   * 
   * @param path Database path
   * @param updates Map of updates
   * @return Task that completes when the update is done
   */
  public static Task<Void> updateChildren(String path, Map<String, Object> updates) {
    TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
    
    if (databaseService == null) {
      taskCompletionSource.setException(new Exception("Database service not initialized"));
      return taskCompletionSource.getTask();
    }
    
    databaseService.updateChildren(path, updates)
        .thenAccept(response -> {
          if (response.isSuccess()) {
            taskCompletionSource.setResult(null);
          } else {
            taskCompletionSource.setException(new Exception(response.getMessage()));
          }
        })
        .exceptionally(throwable -> {
          Log.e(TAG, "Error updating children at path: " + path, throwable);
          taskCompletionSource.setException(new Exception(throwable.getMessage()));
          return null;
        });
    
    return taskCompletionSource.getTask();
  }

  /**
   * Remove a value from the database through the server
   * 
   * @param path Database path
   * @return Task that completes when the deletion is done
   */
  public static Task<Void> removeValue(String path) {
    TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
    
    if (databaseService == null) {
      taskCompletionSource.setException(new Exception("Database service not initialized"));
      return taskCompletionSource.getTask();
    }
    
    databaseService.removeValue(path)
        .thenAccept(response -> {
          if (response.isSuccess()) {
            taskCompletionSource.setResult(null);
          } else {
            taskCompletionSource.setException(new Exception(response.getMessage()));
          }
        })
        .exceptionally(throwable -> {
          Log.e(TAG, "Error removing value at path: " + path, throwable);
          taskCompletionSource.setException(new Exception(throwable.getMessage()));
          return null;
        });
    
    return taskCompletionSource.getTask();
  }

  /**
   * Get a database reference for a path
   * Note: This should only be used for path construction, not for direct database access
   * 
   * @param path Database path
   * @return Database reference
   */
  public static DatabaseReference getReference(String path) {
    if (database == null) {
      database = FirebaseDatabase.getInstance();
    }
    return database.getReference(path);
  }

  /**
   * Convert a database reference to a path string
   * 
   * @param reference Database reference
   * @return Path string
   */
  public static String getPathFromReference(DatabaseReference reference) {
    String path = reference.toString();
    String databaseUrl = FirebaseDatabase.getInstance().getReference().toString();
    
    if (path.startsWith(databaseUrl)) {
      path = path.substring(databaseUrl.length());
    }
    
    // Clean up the path
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    
    // Log for debugging
    Log.d(TAG, "Converting reference to path: " + reference.toString());
    Log.d(TAG, "Database URL: " + databaseUrl);
    Log.d(TAG, "Extracted path: " + path);
    
    return path;
  }
}
