package com.example.partymaker.data.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/** Centralized access points for Firebase services used in the app. */
public class DBRef {
  /** Firebase Authentication instance. */
  public static FirebaseAuth Auth = FirebaseAuth.getInstance();
  /** Realtime database instance. */
  public static FirebaseDatabase DataBase = FirebaseDatabase.getInstance();
  /** Storage instance for profile images and group pictures. */
  public static FirebaseStorage Storage = FirebaseStorage.getInstance();
  /** Currently logged-in user key (email with dots replaced). */
  public static String CurrentUser;
  /** Reference to groups node. */
  public static DatabaseReference refGroups = DataBase.getReference("Groups");
  /** Reference to users node. */
  public static DatabaseReference refUsers = DataBase.getReference("Users");
  /** Reference to chat messages node. */
  public static DatabaseReference refMessages = DataBase.getReference("GroupsMessages");
  /** Reference to Firebase Storage root for profile images. */
  public static StorageReference refStorage = Storage.getReference("UsersImageProfile");

  /**
   * Utility helper to verify if a user/group image exists in storage.
   *
   * @param path Relative path inside {@code refStorage}
   * @param listener Callback invoked with {@code true} if the image exists
   */
  public static void checkImageExists(String path, OnImageExistsListener listener) {
    StorageReference imageRef = refStorage.child(path);
    imageRef
        .getDownloadUrl()
        .addOnSuccessListener(uri -> listener.onImageExists(true))
        .addOnFailureListener(exception -> listener.onImageExists(false));
  }

  /** Listener for {@link #checkImageExists(String, OnImageExistsListener)}. */
  public interface OnImageExistsListener {
    /** Called with the result of the image existence check. */
    void onImageExists(boolean exists);
  }
}
