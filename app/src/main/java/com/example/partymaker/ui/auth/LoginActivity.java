package com.example.partymaker.ui.auth;

import static com.example.partymaker.utilities.Constants.IS_CHECKED;
import static com.example.partymaker.utilities.Constants.PREFS_NAME;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.R;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.firebase.DatabaseUpdateServer;
import com.example.partymaker.data.firebase.FirebaseConfig;
import com.example.partymaker.data.model.User;
import com.example.partymaker.ui.common.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.KeyStore;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;
//import javax.crypto.spec.KeyGenParameterSpec;

import android.util.Base64;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

public class LoginActivity extends AppCompatActivity {
  private static final String TAG = "LoginActivity";
  private TextInputEditText etEmail, etPassword;
  private ImageButton btnAbout;
  private MaterialButton btnLogin, btnPress, btnResetPass;
  private MaterialCheckBox cbRememberMe;
  private SignInButton btnGoogleSignIn;
  private GoogleSignInClient mGoogleSignInClient;
  private static final int RC_SIGN_IN = 9001;
  private FirebaseAuth mAuth;
  private ExecutorService networkExecutor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    // Initialize network executor for background tasks
    networkExecutor = Executors.newSingleThreadExecutor();

    // Enable offline capabilities for Firebase
    FirebaseConfig.enableOfflineCapabilities();
    
    // Make sure we can connect to the database
    boolean dbConnected = FirebaseConfig.ensureDatabaseConnection(this);
    Log.d(TAG, "Database connection status: " + (dbConnected ? "Connected" : "Offline mode"));

    // Initialize Firebase Auth
    mAuth = FirebaseAuth.getInstance();

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
    Animation animation = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
    btnAbout.startAnimation(animation);

    // read from SharedPreferences
    SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    cbRememberMe.setChecked(preferences.getBoolean(IS_CHECKED, false));
    if (cbRememberMe.isChecked()) {
      etEmail.setText(preferences.getString("pref_email", ""));
      etPassword.setText(preferences.getString("pref_password", ""));
    }

    setupEventHandlers();
  }

  private void setupEventHandlers() {
    // Login button click listener
    btnLogin.setOnClickListener(view -> signIn());
    
    // Google Sign In button click listener
    btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
    
    // Register button click handler
    btnPress.setOnClickListener(view -> {
      Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
      btnAbout.clearAnimation();
      startActivity(i);
      overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
    });
    
    // Reset password button click handler
    btnResetPass.setOnClickListener(v -> {
      Intent i = new Intent(LoginActivity.this, ResetPasswordActivity.class);
      btnAbout.clearAnimation();
      startActivity(i);
    });
    
    // About button click handler
    btnAbout.setOnClickListener(view -> {
      Intent i = new Intent(LoginActivity.this, IntroActivity.class);
      btnAbout.clearAnimation();
      startActivity(i);
    });
  }

  private void signIn() {
    String email = Objects.requireNonNull(etEmail.getText()).toString();
    String password = Objects.requireNonNull(etPassword.getText()).toString();
    
    if (email.isEmpty() || password.isEmpty()) {
      Toast.makeText(LoginActivity.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
      return;
    }
    
    final ProgressDialog pd = ProgressDialog.show(
        LoginActivity.this, "Connecting", "Please wait...", true);
    pd.show();

    // Log the login attempt
    Log.i(TAG, "Logging in as " + email);
    Log.d(TAG, "=== AUTHENTICATION FLOW STARTED ===");
    Log.d(TAG, "Email: " + email);
    Log.d(TAG, "Remember me checked: " + cbRememberMe.isChecked());
    
    // Check network connectivity
    boolean isNetworkAvailable = FirebaseConfig.isNetworkAvailable(LoginActivity.this);
    Log.d(TAG, "Network availability: " + isNetworkAvailable);
    
    // Check Firebase configuration
    boolean isFirebaseConfigured = FirebaseConfig.isFirebaseProperlyConfigured();
    Log.d(TAG, "Firebase properly configured: " + isFirebaseConfigured);
    
    // Ensure database connection
    boolean dbConnected = FirebaseConfig.ensureDatabaseConnection(LoginActivity.this);
    Log.d(TAG, "Database connection status: " + (dbConnected ? "Connected" : "Offline mode"));
    
    // Authenticate through the server
    authenticateThroughServer(email, password, pd);
  }

  /**
   * Authenticate through the server using DatabaseUpdateServer
   */
  private void authenticateThroughServer(String email, String password, ProgressDialog pd) {
    Log.d(TAG, "Attempting to authenticate through server");
    
    // Use DatabaseUpdateServer to authenticate
    DatabaseUpdateServer.signInWithEmailAndPassword(mAuth, email, password)
        .addOnCompleteListener(task -> {
          if (task.isSuccessful()) {
            Log.d(TAG, "Server authentication successful");
            
            // Save credentials if remember me is checked
            if (cbRememberMe.isChecked()) {
              savePreferences(email, password);
            }
            
            pd.dismiss();
            handleSuccessfulLogin();
          } else {
            Log.w(TAG, "Server authentication failed", task.getException());
            pd.dismiss();
            
            // Handle authentication through AuthController
            if (task.getException() != null && 
                task.getException().getMessage() != null &&
                task.getException().getMessage().contains("use AuthController")) {
              
              Log.d(TAG, "Falling back to AuthController for authentication");
              authenticateWithAuthController(email, password);
            } else {
              handleFailedLogin(task.getException(), email, password);
            }
          }
        });
  }
  
  /**
   * Authenticate using the AuthController (Firebase Auth)
   */
  private void authenticateWithAuthController(String email, String password) {
    Log.d(TAG, "Authenticating with AuthController");
    
    final ProgressDialog pd = ProgressDialog.show(
        LoginActivity.this, "Authenticating", "Please wait...", true);
    pd.show();
    
    mAuth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(task -> {
          pd.dismiss();
          
          if (task.isSuccessful()) {
            Log.d(TAG, "AuthController authentication successful");
            
            // Save credentials if remember me is checked
            if (cbRememberMe.isChecked()) {
              savePreferences(email, password);
            }
            
            handleSuccessfulLogin();
          } else {
            Log.w(TAG, "AuthController authentication failed", task.getException());
            handleFailedLogin(task.getException(), email, password);
          }
        });
  }

  private void savePreferences(String email, String password) {
    // Create SharedPreferences to save email and password
    SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    SharedPreferences.Editor editor = preferences.edit();
    
    try {
      // Save the "remember me" checkbox state
      editor.putBoolean(IS_CHECKED, true);
      
      // Save the email
      editor.putString("pref_email", email);
      
      // Encrypt and save the password
      String encryptedPassword = encryptToken(password);
      editor.putString("pref_password", encryptedPassword);
      
      editor.apply();
      Log.d(TAG, "Credentials saved successfully");
    } catch (Exception e) {
      Log.e(TAG, "Error saving credentials", e);
      
      // Fallback to saving just the email if encryption fails
      editor.putString("pref_email", email);
      editor.putString("pref_password", "");
      editor.apply();
    }
  }
  
  private String encryptToken(String token) {
    try {
        // Use Android Keystore System to encrypt the token
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        
        if (!keyStore.containsAlias("auth_key")) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            );
            
            android.security.keystore.KeyGenParameterSpec keyGenParameterSpec = 
                new android.security.keystore.KeyGenParameterSpec.Builder(
                    "auth_key",
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build();
            
            keyGenerator.init(keyGenParameterSpec);
            keyGenerator.generateKey();
        }
        
        Key key = keyStore.getKey("auth_key", null);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        
        byte[] encryptedBytes = cipher.doFinal(token.getBytes());
        byte[] iv = cipher.getIV();
        
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedBytes.length);
        byteBuffer.put(iv);
        byteBuffer.put(encryptedBytes);
        
        return Base64.encodeToString(byteBuffer.array(), Base64.DEFAULT);
    } catch (Exception e) {
        Log.e(TAG, "Error encrypting token", e);
        return null;
    }
  }

  private String decryptToken(String encryptedToken) {
    try {
        ByteBuffer byteBuffer = ByteBuffer.wrap(Base64.decode(encryptedToken, Base64.DEFAULT));
        
        byte[] iv = new byte[12]; // GCM IV length
        byteBuffer.get(iv);
        
        byte[] encryptedBytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(encryptedBytes);
        
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        
        Key key = keyStore.getKey("auth_key", null);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        
        return new String(cipher.doFinal(encryptedBytes));
    } catch (Exception e) {
        Log.e(TAG, "Error decrypting token", e);
        return null;
    }
  }
  
  private void handleFailedLogin(Exception exception, String email, String password) {
    String errorMessage = "Invalid Email or Password";

    if (exception != null) {
      Log.e(TAG, "Login failed", exception);
      String exceptionMessage = exception.getMessage();
      Log.e(TAG, "Exception class: " + exception.getClass().getName());
      Log.e(TAG, "Exception message: " + exceptionMessage);
      
      if (exceptionMessage != null) {
        if (exceptionMessage.contains("network") || 
            exceptionMessage.contains("unreachable") || 
            exceptionMessage.contains("timed out") ||
            exceptionMessage.contains("unexpected end of stream") ||
            exceptionMessage.contains("reCAPTCHA")) {
          
          errorMessage = "Network error. Please check your connection.";
          Log.e(TAG, "Identified as network or reCAPTCHA error");
          
          // Try direct authentication as a fallback for network issues
          directAuthentication(email, password);
          return;
        }
      }
    }

    Log.e(TAG, "Authentication failed with message: " + errorMessage);
    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
    btnResetPass.setVisibility(View.VISIBLE);
  }

  /**
   * התחברות ישירה לפיירבייס עם משתמש הטסט, עוקפת את השרת ואת בעיות ה-reCAPTCHA
   */
  private void directAuthentication(String email, String password) {
    Log.d(TAG, "=== DIRECT AUTHENTICATION STARTED ===");
    Log.d(TAG, "Email: " + email);
    
    // Check if running on emulator
    boolean isEmulator = android.os.Build.MODEL.contains("sdk_gphone") || 
                        android.os.Build.PRODUCT.contains("sdk");
    
    if (isEmulator) {
      tryEmulatorAuthentication(email, password);
      return;
    }
    
    // Try direct Firebase authentication
    Log.d(TAG, "Attempting direct Firebase authentication");
    
    // First, check DNS resolution in background
    checkFirebaseDnsResolution();
    
    // Attempt authentication
    FirebaseAuth directAuth = FirebaseAuth.getInstance();
    directAuth.signInWithEmailAndPassword(email, password)
      .addOnCompleteListener(LoginActivity.this, task -> {
        Log.d(TAG, "Direct authentication task completed, success: " + task.isSuccessful());
        
        if (task.isSuccessful()) {
          handleSuccessfulLogin();
        } else {
          Exception exception = task.getException();
          Log.e(TAG, "Direct authentication failed", exception);
          
          if (isNetworkOrRecaptchaError(exception)) {
            retryWithDelay(email, password);
          } else {
            showAuthenticationError(exception);
          }
        }
      });
  }
  
  private boolean isNetworkOrRecaptchaError(Exception exception) {
    if (exception == null || exception.getMessage() == null) {
      return false;
    }
    
    String message = exception.getMessage();
    return message.contains("network") || 
           message.contains("unreachable") || 
           message.contains("timed out") ||
           message.contains("unexpected end of stream") ||
           message.contains("reCAPTCHA");
  }
  
  private void tryEmulatorAuthentication(String email, String password) {
    Log.d(TAG, "Running on emulator, using local Firebase emulator");
    try {
      // Use Firebase emulator for authentication
      FirebaseAuth emulatorAuth = FirebaseAuth.getInstance();
      emulatorAuth.useEmulator("10.0.2.2", 9099);
      
      emulatorAuth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(LoginActivity.this, task -> {
          if (task.isSuccessful()) {
            Log.d(TAG, "Emulator authentication successful");
            handleSuccessfulLogin();
          } else {
            Log.e(TAG, "Emulator authentication failed", task.getException());
            fallbackToRegularAuth(email, password);
          }
        });
    } catch (Exception e) {
      Log.e(TAG, "Failed to use emulator", e);
      fallbackToRegularAuth(email, password);
    }
  }
  
  private void retryWithDelay(String email, String password) {
    Log.e(TAG, "Authentication failed due to network/reCAPTCHA issue, retrying with delay");
    
    // Wait and try again
    new Handler().postDelayed(() -> {
      Log.d(TAG, "Retrying authentication after delay");
      
      // Try with a new FirebaseAuth instance
      FirebaseAuth retryAuth = FirebaseAuth.getInstance();
      retryAuth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(LoginActivity.this, retryTask -> {
          Log.d(TAG, "Delayed authentication task completed, success: " + retryTask.isSuccessful());
          
          if (retryTask.isSuccessful()) {
            handleSuccessfulLogin();
          } else {
            Log.e(TAG, "Delayed authentication also failed", retryTask.getException());
            
            // Show error message
            Toast.makeText(
                LoginActivity.this,
                "Authentication failed after multiple attempts. Please try again later.",
                Toast.LENGTH_LONG).show();
          }
        });
    }, 3000); // 3 second delay
  }
  
  private void showAuthenticationError(Exception exception) {
    String errorMessage = "Authentication failed";
    
    if (exception != null && exception.getMessage() != null) {
      errorMessage = "Authentication failed: " + exception.getMessage();
    }
    
    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
  }
  
  private void fallbackToRegularAuth(String email, String password) {
    Log.d(TAG, "Falling back to regular Firebase authentication");
    FirebaseAuth fallbackAuth = FirebaseAuth.getInstance();
    fallbackAuth.signInWithEmailAndPassword(email, password)
      .addOnCompleteListener(LoginActivity.this, task -> {
        if (task.isSuccessful()) {
          Log.d(TAG, "Fallback authentication successful");
          handleSuccessfulLogin();
        } else {
          Log.e(TAG, "Fallback authentication failed", task.getException());
          Toast.makeText(
              LoginActivity.this,
              "Authentication failed after multiple attempts",
              Toast.LENGTH_SHORT).show();
        }
      });
  }
  
  private void handleSuccessfulLogin() {
    FirebaseUser user = mAuth.getCurrentUser();
    if (user != null) {
      DBRef.CurrentUser = user.getEmail();
      Log.d(TAG, "Current user set to: " + DBRef.CurrentUser);
      Toast.makeText(
          LoginActivity.this,
          "Authentication Successful",
          Toast.LENGTH_SHORT).show();
      
      Intent intent = new Intent(LoginActivity.this, MainActivity.class);
      startActivity(intent);
      finish();
    } else {
      Log.w(TAG, "User is null after successful authentication");
      Toast.makeText(
          LoginActivity.this,
          "Authentication error: User is null",
          Toast.LENGTH_SHORT).show();
    }
  }
  
  private void checkFirebaseDnsResolution() {
    networkExecutor.execute(() -> {
      try {
        Log.d(TAG, "Checking DNS resolution for Firebase hosts");
        
        // Check Firebase auth domain
        try {
          InetAddress authAddress = InetAddress.getByName("www.googleapis.com");
          Log.d(TAG, "Firebase auth DNS resolved: " + authAddress.getHostAddress());
        } catch (UnknownHostException e) {
          Log.e(TAG, "Failed to resolve Firebase auth domain", e);
        }
        
        // Check Firebase database domain
        try {
          InetAddress dbAddress = InetAddress.getByName("firebaseio.com");
          Log.d(TAG, "Firebase database DNS resolved: " + dbAddress.getHostAddress());
        } catch (UnknownHostException e) {
          Log.e(TAG, "Failed to resolve Firebase database domain", e);
        }
        
        // Check Firebase storage domain
        try {
          InetAddress storageAddress = InetAddress.getByName("firebasestorage.googleapis.com");
          Log.d(TAG, "Firebase storage DNS resolved: " + storageAddress.getHostAddress());
        } catch (UnknownHostException e) {
          Log.e(TAG, "Failed to resolve Firebase storage domain", e);
        }
      } catch (Exception e) {
        Log.e(TAG, "Error checking DNS resolution", e);
      }
    });
  }

  private void signInWithGoogle() {
    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
    startActivityForResult(signInIntent, RC_SIGN_IN);
  }

  @Override
  public void onStart() {
    super.onStart();
    // Check if user is signed in (non-null) and update UI accordingly.
    FirebaseUser currentUser = mAuth.getCurrentUser();
    if (currentUser != null) {
      DBRef.CurrentUser = currentUser.getEmail();
      Log.d(TAG, "User already signed in: " + currentUser.getEmail());
      Intent intent = new Intent(LoginActivity.this, MainActivity.class);
      startActivity(intent);
      finish();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
    if (requestCode == RC_SIGN_IN) {
      Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
      try {
        // Google Sign In was successful, authenticate with Firebase
        GoogleSignInAccount account = task.getResult(ApiException.class);
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
        firebaseAuthWithGoogle(account.getIdToken());
      } catch (ApiException e) {
        // Google Sign In failed, update UI appropriately
        Log.w(TAG, "Google sign in failed", e);
      }
    }
  }

  private void firebaseAuthWithGoogle(String idToken) {
    AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
    mAuth.signInWithCredential(credential)
        .addOnCompleteListener(
            this,
            task -> {
              if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithCredential:success");
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                  DBRef.CurrentUser = user.getEmail();
                  Toast.makeText(LoginActivity.this, "Authentication Successful.", Toast.LENGTH_SHORT)
                      .show();
                  Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                  startActivity(intent);
                  finish();
                }
              } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInWithCredential:failure", task.getException());
                Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT)
                    .show();
              }
            });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // Shutdown the executor service
    if (networkExecutor != null) {
      try {
        networkExecutor.shutdown();
        if (!networkExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
          networkExecutor.shutdownNow();
        }
      } catch (InterruptedException e) {
        networkExecutor.shutdownNow();
      }
    }
  }
}
