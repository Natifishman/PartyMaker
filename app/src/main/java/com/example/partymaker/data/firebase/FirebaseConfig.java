package com.example.partymaker.data.firebase;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

/**
 * Class for managing Firebase configuration and handling connectivity issues
 */
public class FirebaseConfig {
    private static final String TAG = "FirebaseConfig";
    private static boolean offlineCapabilitiesEnabled = false;
    
    // Firebase configuration constants
    private static final String FIREBASE_DATABASE_URL = "https://partymaker-9c966-default-rtdb.firebaseio.com";
    private static final String FIREBASE_STORAGE_BUCKET = "partymaker-9c966.appspot.com";
    
    /**
     * Check if the device is connected to the internet
     * @param context Application context
     * @return true if internet connection is available, false otherwise
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            Log.e(TAG, "Context is null in isNetworkAvailable");
            return false;
        }
        
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager == null) {
                Log.e(TAG, "ConnectivityManager is null");
                return false;
            }
            
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
            
            Log.d(TAG, "Network availability check: " + isConnected);
            return isConnected;
        } catch (Exception e) {
            Log.e(TAG, "Error checking network availability", e);
            return false;
        }
    }
    
    /**
     * Enable Firebase Database offline capabilities
     */
    public static void enableOfflineCapabilities() {
        if (offlineCapabilitiesEnabled) {
            return;
        }
        
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            offlineCapabilitiesEnabled = true;
            Log.d(TAG, "Firebase offline persistence enabled");
        } catch (Exception e) {
            Log.e(TAG, "Error enabling Firebase persistence", e);
        }
    }
    
    /**
     * Check if Firebase is properly configured
     * @return true if Firebase is properly configured, false otherwise
     */
    public static boolean isFirebaseProperlyConfigured() {
        try {
            // Check if FirebaseApp is initialized
            FirebaseApp defaultApp = FirebaseApp.getInstance();
            if (defaultApp == null) {
                Log.e(TAG, "Firebase default app is null");
                return false;
            }
            
            // Check if options are valid
            FirebaseOptions options = defaultApp.getOptions();
            if (options == null) {
                Log.e(TAG, "Firebase options are null");
                return false;
            }
            
            Log.d(TAG, "Firebase is properly configured");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error checking Firebase configuration", e);
            return false;
        }
    }
    
    /**
     * Ensure database connection
     * @param context Application context
     * @return true if connected, false otherwise
     */
    public static boolean ensureDatabaseConnection(Context context) {
        try {
            // Check network connectivity
            boolean isConnected = isNetworkAvailable(context);
            
            if (isConnected) {
                // Go online if network is available
                FirebaseDatabase.getInstance().goOnline();
                Log.d(TAG, "Firebase connection established to: " + FIREBASE_DATABASE_URL);
                return true;
            } else {
                Log.w(TAG, "No network connection, Firebase operating in offline mode");
                FirebaseDatabase.getInstance().goOffline();
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error ensuring database connection", e);
            return false;
        }
    }
} 