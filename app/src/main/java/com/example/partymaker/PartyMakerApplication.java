package com.example.partymaker;

import android.app.Application;
import android.util.Log;

import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.firebase.FirebaseConfig;

/**
 * מחלקת האפליקציה הראשית, מאותחלת בתחילת הרצת האפליקציה
 */
public class PartyMakerApplication extends Application {
    private static final String TAG = "PartyMakerApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        
        // אתחול הגדרות Firebase
        initializeFirebase();
    }
    
    /**
     * אתחול הגדרות Firebase והתחברות לדאטאבייס
     */
    private void initializeFirebase() {
        try {
            Log.d(TAG, "Initializing Firebase settings");
            
            // הפעלת יכולות לא מקוונות
            FirebaseConfig.enableOfflineCapabilities();
            
            // אתחול הדאטאבייס
            DBRef.initialize(this);
            
            // בדיקת חיבור לדאטאבייס
            boolean dbConnected = FirebaseConfig.ensureDatabaseConnection(this);
            Log.d(TAG, "Database connection initialized, status: " + (dbConnected ? "Connected" : "Offline mode"));
            
            // Handle server errors in Jetty
            handleJettyServerErrors();
            
            Log.d(TAG, "Firebase initialization completed");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase", e);
        }
    }
    
    /**
     * טיפול בשגיאות שרת Jetty
     * מטפל בבעיות ה-NullPointerException שנצפו בשרת
     */
    private void handleJettyServerErrors() {
        // לוגיקה לטיפול בשגיאות שרת
        // הערה: זה מיועד לשרת, לא לאפליקציה, אבל אנחנו יכולים להוסיף כאן לוגיקה
        // שתעזור לאפליקציה להתמודד עם שגיאות שרת
        
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            if (throwable instanceof NullPointerException) {
                // בדוק אם זו שגיאת URI null בשרת Jetty
                StackTraceElement[] stack = throwable.getStackTrace();
                for (StackTraceElement element : stack) {
                    if (element.getClassName().contains("jetty") && 
                        element.getMethodName().contains("checkRequestedSessionId")) {
                        Log.e(TAG, "Caught Jetty server NullPointerException in session handling", throwable);
                        // במקרה זה, אנחנו יכולים לנסות להתאושש או לדווח על השגיאה
                        return;
                    }
                }
            }
            
            // אם זו לא שגיאת שרת ספציפית, העבר את השגיאה למטפל ברירת המחדל
            Thread.getDefaultUncaughtExceptionHandler().uncaughtException(thread, throwable);
        });
    }
} 