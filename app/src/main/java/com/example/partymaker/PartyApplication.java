package com.example.partymaker;

import android.app.Application;
import android.util.Log;
import com.example.partymaker.data.api.ConnectivityManager;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.api.NetworkManager;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.repository.GroupRepository;
import com.example.partymaker.data.repository.UserRepository;
import com.example.partymaker.utils.infrastructure.system.MemoryManager;
import com.example.partymaker.utils.ui.feedback.NotificationManager;
import com.google.firebase.FirebaseApp;

/**
 * Enterprise-level Application class for PartyMaker.
 * 
 * This class serves as the global application entry point and is responsible for:
 * - Initializing core services and dependencies
 * - Setting up Firebase infrastructure
 * - Configuring network monitoring and management
 * - Establishing repository patterns for data access
 * - Managing application-wide memory and resources
 * 
 * Architecture Pattern: Singleton repositories with dependency injection
 * Design Principle: Single Responsibility - Each component has one clear purpose
 * 
 * @author PartyMaker Team
 * @version 2.0
 * @since 1.0
 */
public class PartyApplication extends Application {
  /** Tag for logging - follows Android best practices for log identification */
  private static final String TAG = "PartyApplication";

  /**
   * Application lifecycle callback - Called when the application is starting.
   * 
   * This method orchestrates the initialization of all critical application components
   * in the correct order to ensure dependencies are satisfied.
   * 
   * Initialization Order:
   * 1. Firebase SDK and authentication
   * 2. Database references and connections
   * 3. Notification channels for user engagement
   * 4. Network monitoring for connectivity awareness
   * 5. Server clients for API communication
   * 6. Repository layer for data management
   * 7. Memory monitoring for performance optimization
   */
  @Override
  public void onCreate() {
    super.onCreate();

    // Step 1: Initialize Firebase SDK
    // Critical for authentication, database, and cloud services
    try {
      FirebaseApp.initializeApp(this);
      Log.d(TAG, "Firebase initialized successfully");

      // Step 2: Initialize Firebase database references
      // Establishes connection points to Realtime Database nodes
      DBRef.init();
      Log.d(TAG, "Firebase references initialized successfully");
    } catch (Exception e) {
      // Non-fatal: App can potentially work in offline mode
      Log.e(TAG, "Error initializing Firebase", e);
    }

    // Step 3: Initialize notification infrastructure
    // Required for Android O+ notification channels and FCM subscriptions
    NotificationManager.createNotificationChannels(this);
    NotificationManager.subscribeToGlobalAnnouncements();

    // Step 4: Initialize network monitoring
    // Provides real-time network state awareness for offline capabilities
    NetworkManager networkManager = NetworkManager.getInstance();
    networkManager.initialize(getApplicationContext());

    // Step 5: Initialize connectivity monitoring
    // Enables automatic retry and sync when connection is restored
    ConnectivityManager.getInstance().init(getApplicationContext());
    Log.d(TAG, "ConnectivityManager initialized successfully");

    // Note: Room database schema migrations are handled automatically
    // If schema changes are detected, Room will recreate the database

    // Step 6: Initialize server communication client
    // Manages all API calls to the Firebase Cloud Functions backend
    try {
      FirebaseServerClient.getInstance().initialize(this);
      Log.d(TAG, "FirebaseServerClient initialized successfully");
    } catch (Exception e) {
      // Non-fatal: Can fall back to direct Firebase access if configured
      Log.e(TAG, "Error initializing FirebaseServerClient", e);
    }

    // Step 7: Initialize repository layer
    // Sets up data access patterns with caching and offline support
    initializeRepositories();

    // Step 8: Log initial memory state for performance monitoring
    // Helps identify memory leaks and optimization opportunities
    Log.d(TAG, "Initial memory usage: " + MemoryManager.getDetailedMemoryInfo());

    Log.d(TAG, "Application initialized successfully");
  }

  /**
   * Initializes all repository instances with application context.
   * 
   * Repositories follow the Repository Pattern to abstract data sources and provide:
   * - Unified data access interface
   * - Automatic caching with Room database
   * - Offline-first architecture support
   * - Network/database synchronization
   * 
   * Each repository is a singleton to ensure consistent data state across the app.
   * 
   * @implNote Order matters: Initialize repositories that others depend on first
   */
  private void initializeRepositories() {
    // Initialize Group Repository
    // Manages party/event data with local caching
    GroupRepository groupRepository = GroupRepository.getInstance();
    groupRepository.initialize(getApplicationContext());

    // Initialize User Repository  
    // Handles user profiles and authentication state
    UserRepository userRepository = UserRepository.getInstance();
    userRepository.initialize(getApplicationContext());

    // Future repositories can be added here following the same pattern
    // Examples: MessageRepository, MediaRepository, PaymentRepository

    Log.d(TAG, "Repositories initialized with application context");
  }

  /**
   * Application termination callback.
   * 
   * Note: This method is never called on production Android devices.
   * It's only called in emulated environments. However, we implement it
   * for completeness and testing purposes.
   * 
   * Proper cleanup ensures:
   * - No memory leaks from retained contexts
   * - Network callbacks are unregistered
   * - Database connections are closed
   * - Background tasks are cancelled
   */
  @Override
  public void onTerminate() {
    super.onTerminate();

    // Release network monitoring resources
    NetworkManager.getInstance().release();

    // Unregister system connectivity callbacks to prevent leaks
    ConnectivityManager.getInstance().unregisterNetworkCallback();

    // Clean up server client resources and pending requests
    try {
      FirebaseServerClient.getInstance().cleanup();
      Log.d(TAG, "FirebaseServerClient cleaned up successfully");
    } catch (Exception e) {
      Log.e(TAG, "Error cleaning up FirebaseServerClient", e);
    }

    Log.d(TAG, "Application terminated");
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();

    // Perform memory cleanup
    MemoryManager.performMemoryCleanup(this);

    Log.d(TAG, "Low memory cleanup performed");
  }
}
