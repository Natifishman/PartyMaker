package com.example.partymaker.data.firebase;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Factory class for creating database service instances
 * This class ensures that the correct database service is used based on the environment
 */
public class DatabaseFactory {
    private static final String TAG = "DatabaseFactory";
    private static DatabaseService databaseService;
    
    /**
     * Get a database service instance
     * @param context Application context
     * @return DatabaseService instance
     */
    public static synchronized DatabaseService getDatabaseService(Context context) {
        if (databaseService != null) {
            return databaseService;
        }
        
        // Check if the server is available
        DatabaseUpdateServer.checkServerAvailability(isAvailable -> {
            if (isAvailable) {
                Log.d(TAG, "Server is available, using DatabaseUpdateServer");
                databaseService = new DatabaseUpdateServer();
            } else {
                Log.d(TAG, "Server is not available, using DatabaseUpdateFallback");
                databaseService = new DatabaseUpdateFallback();
            }
        });
        
        // Wait for the server check to complete
        int retries = 0;
        while (databaseService == null && retries < 5) {
            try {
                Thread.sleep(500);
                retries++;
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted while waiting for server check", e);
                break;
            }
        }
        
        // If the server check failed, use the fallback
        if (databaseService == null) {
            Log.w(TAG, "Server check timed out, using DatabaseUpdateFallback");
            databaseService = new DatabaseUpdateFallback();
        }
        
        return databaseService;
    }
    
    /**
     * Reset the database service instance
     * This is useful for testing or when the server availability changes
     */
    public static void resetDatabaseService() {
        databaseService = null;
    }
    
    /**
     * Alias for resetDatabaseService() to maintain compatibility
     */
    public static void resetInstance() {
        resetDatabaseService();
    }
} 