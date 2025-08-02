package com.example.partymaker.ui.features.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.example.partymaker.ui.base.BaseActivity;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.partymaker.R;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.ui.adapters.GroupAdapter;
import com.example.partymaker.ui.features.auth.LoginActivity;
import com.example.partymaker.ui.features.auxiliary.chatbot.GptChatActivity;
import com.example.partymaker.ui.features.groups.main.PartyMainActivity;
import com.example.partymaker.ui.features.auxiliary.settings.ServerSettingsActivity;
import com.example.partymaker.utils.auth.AuthenticationManager;
import com.example.partymaker.utils.core.IntentExtrasManager;
import com.example.partymaker.utils.core.ExtrasMetadata;
import com.example.partymaker.utils.ui.navigation.NavigationManager;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import com.example.partymaker.utils.ui.components.LoadingStateManager;
import com.example.partymaker.utils.ui.components.UiStateManager;
import com.example.partymaker.utils.ui.feedback.UserFeedbackManager;
import com.example.partymaker.viewmodel.core.MainActivityViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.HashMap;

public class MainActivity extends BaseActivity<MainActivityViewModel> {

  // UI Components
  private RecyclerView lv1;
  private FloatingActionButton fabChat;
  private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;
  private View rootView;

  // Data Components
  private String UserKey;
  private GroupAdapter groupAdapter;

  // UI State Management
  private LoadingStateManager loadingStateManager;

  // Variable to track if we've shown the loading toast already
  private boolean loadingToastShown = false;

  // Track when we last refreshed to avoid excessive server calls
  private long lastRefreshTime = 0;
  private static final long REFRESH_COOLDOWN_MS = 30000; // 30 seconds cooldown
  
  // ActionBar styling constants
  private static final String ACTION_BAR_START_COLOR = "#0081d1";
  private static final String ACTION_BAR_END_COLOR = "#0066cc";
  private static final String ACTION_BAR_TITLE_COLOR = "#FFFFFF";
  private static final float ACTION_BAR_ELEVATION = 15f;

  // BaseActivity implementation methods
  
  @Override
  protected int getLayoutId() {
    return R.layout.activity_main;
  }

  @Override
  protected Class<MainActivityViewModel> getViewModelClass() {
    return MainActivityViewModel.class;
  }

  @Override
  protected String getActivityTitle() {
    return "PartyMaker";
  }

  @Override
  protected boolean shouldShowBackButton() {
    return false; // MainActivity shouldn't have back button
  }

  @Override
  protected void initViews() {
    try {
      // Force set server URL to Render
      forceSetServerUrl();

      if (!initializeUser()) {
        finish();
        return; // Exit if user initialization failed
      }

      initializeMainViews();
    } catch (Exception e) {
      showError("An unexpected error occurred. Please restart the app.");
      finish();
    }
  }

  @Override
  protected void setupObservers() {
    observeViewModel();
  }

  @Override
  protected void setupClickListeners() {
    setupEventHandlers();
    setupFloatingChatButton();
    setupBottomNavigation();
  }

  @Override
  protected void onActivityStart() {
    super.onActivityStart();
    
    // Show loading indicator without toast
    showLoading(true, false);

    // Load groups for current user
    if (UserKey != null && viewModel != null) {
      viewModel.loadUserGroups(UserKey, true);
      lastRefreshTime = System.currentTimeMillis();
    }
  }

  /**
   * Shows or hides a loading indicator
   *
   * @param show True to show loading, false to hide
   * @param showToast True to show a toast message, false to hide
   */
  private void showLoading(boolean show, boolean showToast) {
    try {
      // Simple implementation without dedicated loading view
      if (lv1 != null) {
        lv1.setVisibility(show ? View.GONE : View.VISIBLE);
      }

      // Show a toast only if requested and if we haven't shown it already
      if (show && showToast && !loadingToastShown) {
        Toast.makeText(this, "Loading groups...", Toast.LENGTH_SHORT).show();
        loadingToastShown = true;
      }
    } catch (Exception e) {
      Log.e(TAG, "Error toggling loading state", e);
    }
  }

  /**
   * Overloaded method for backward compatibility
   *
   * @param show True to show loading, false to hide
   */
  private void showLoading(boolean show) {
    showLoading(show, false);
  }

  /** Forces the server URL to be set to Render */
  private void forceSetServerUrl() {
    try {
      String renderUrl = "https://partymaker.onrender.com";
      androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
          .edit()
          .putString("server_url", renderUrl)
          .apply();
      Log.d(TAG, "Forced server URL to: " + renderUrl);
    } catch (Exception e) {
      Log.e(TAG, "Error setting server URL", e);
      // Non-critical error, continue execution
    }
  }

  /**
   * Initializes and validates the current user.
   *
   * @return true if user is valid and initialized, false otherwise
   */
  private boolean initializeUser() {
    try {
      // Check if user explicitly logged in
      SharedPreferences prefs = getSharedPreferences("PartyMakerPrefs", Context.MODE_PRIVATE);
      boolean userExplicitlyLoggedIn = prefs.getBoolean("user_explicitly_logged_in", false);

      if (!userExplicitlyLoggedIn) {
        Log.d(TAG, "User did not explicitly log in - redirecting to login");
        navigateToLogin();
        return false;
      }

      String userEmail = AuthenticationManager.getCurrentUserEmail(this);
      if (userEmail != null) {
        UserKey = userEmail.replace('.', ' ');
        Log.d(
            TAG,
            "User initialized successfully. Original email: "
                + userEmail
                + ", UserKey: "
                + UserKey);
        return true;
      } else {
        Log.e(TAG, "User not logged in or email is null");
        showError("Authentication error. Please login again.");
        navigateToLogin();
        return false;
      }

    } catch (Exception e) {
      Log.e(TAG, "Error initializing user", e);
      showError("User initialization failed");
      navigateToLogin();
      return false;
    }
  }

  // Initializes all view components.
  private void initializeMainViews() {
    rootView = findViewById(android.R.id.content);
    lv1 = findViewById(R.id.lv1);
    fabChat = findViewById(R.id.fabChat);
    swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

    if (lv1 == null) {
      Log.e(TAG, "Critical view lv1 not found");
      UserFeedbackManager.showErrorDialog(this, "UI initialization failed");
      finish();
      return;
    }

    // RecyclerView setup
    lv1.setLayoutManager(new LinearLayoutManager(this));
    groupAdapter = new GroupAdapter(this, this::navigateToGroupScreen);
    lv1.setAdapter(groupAdapter);

    // Initialize loading state manager
    setupLoadingStateManager();

    // Setup pull-to-refresh functionality
    setupSwipeRefresh();
  }

  private void setupLoadingStateManager() {
    // Find or create loading views
    android.widget.ProgressBar progressBar = findViewById(R.id.progressBar);
    android.widget.TextView loadingText = null; // Optional loading text view

    // Create progress bar if not found
    if (progressBar == null) {
      progressBar = new android.widget.ProgressBar(this);
      progressBar.setId(R.id.progressBar);
      progressBar.setVisibility(View.GONE);

      // Add to root layout
      if (rootView instanceof android.widget.FrameLayout) {
        ((android.widget.FrameLayout) rootView).addView(progressBar);
      }
    }

    loadingStateManager =
        new LoadingStateManager.Builder()
            .contentView(lv1)
            .progressBar(progressBar)
            .loadingText(loadingText)
            .build();
  }

  private void setupSwipeRefresh() {
    if (swipeRefreshLayout != null) {
      // Set the colors for the refresh indicator
      swipeRefreshLayout.setColorSchemeColors(
          getResources().getColor(android.R.color.holo_blue_bright),
          getResources().getColor(android.R.color.holo_green_light),
          getResources().getColor(android.R.color.holo_orange_light),
          getResources().getColor(android.R.color.holo_red_light));

      // Set the refresh listener
      swipeRefreshLayout.setOnRefreshListener(
          () -> {
            Log.d(TAG, "Pull-to-refresh triggered: refreshing user groups");
            if (viewModel != null && UserKey != null) {
              viewModel.loadUserGroups(UserKey, true); // Force refresh from server
              lastRefreshTime = System.currentTimeMillis(); // Update refresh time
            }
          });
    }
  }

  // Sets up the action bar with custom gradient background and styling.
  private void setupActionBar() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar == null) {
      Log.w(TAG, "ActionBar not available");
      return;
    }

    try {
      GradientDrawable gradient = createActionBarGradient();
      actionBar.setBackgroundDrawable(gradient);

      String styledTitle = createStyledTitle();
      actionBar.setTitle(Html.fromHtml(styledTitle, Html.FROM_HTML_MODE_LEGACY));

      configureActionBarProperties(actionBar);

      Log.d(TAG, "ActionBar setup completed");

    } catch (Exception e) {
      Log.e(TAG, "Error setting up ActionBar", e);
    }
  }

  // Creates a gradient drawable for the action bar background.
  private GradientDrawable createActionBarGradient() {
    GradientDrawable gradient = new GradientDrawable();
    gradient.setShape(GradientDrawable.RECTANGLE);
    gradient.setColors(
        new int[] {
          Color.parseColor(ACTION_BAR_START_COLOR), Color.parseColor(ACTION_BAR_END_COLOR)
        });
    gradient.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
    return gradient;
  }

  // Creates a styled HTML title string.
  private String createStyledTitle() {
    return String.format(
        "<font color='%s'><b>%s</b></font>", MainActivity.ACTION_BAR_TITLE_COLOR, "My Parties");
  }

  // Configures action bar properties.
  private void configureActionBarProperties(ActionBar actionBar) {
    actionBar.setElevation(ACTION_BAR_ELEVATION);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(false);
  }

  // Observes LiveData from the ViewModel
  private void observeViewModel() {
    try {
      // Observe group list
      viewModel
          .getGroups()
          .observe(
              this,
              groups -> {
                try {
                  Log.d(TAG, "observeViewModel: received groups data, groups = " + groups);
                  if (groups != null) {
                    Log.d(TAG, "Group list size: " + groups.size());
                    for (int i = 0; i < groups.size() && i < 3; i++) {
                      Group g = groups.get(i);
                      Log.d(
                          TAG,
                          "Group "
                              + i
                              + ": "
                              + g.getGroupName()
                              + " (key: "
                              + g.getGroupKey()
                              + ")");
                    }

                    groupAdapter.updateItems(groups);
                    Log.d(TAG, "Group list updated with " + groups.size() + " groups");

                    // Update UI state
                    if (groups.isEmpty()) {
                      Log.d(TAG, "Groups list is empty - showing empty state");
                      loadingStateManager.showEmpty();
                      showEmptyState();
                    } else {
                      Log.d(TAG, "Groups list has " + groups.size() + " items - showing content");
                      loadingStateManager.showContent();
                      hideEmptyState();
                    }
                  } else {
                    Log.d(
                        TAG,
                        "Received null groups list from ViewModel - data not loaded yet, staying in loading state");
                    // Don't show empty state when data is null - it means data hasn't loaded yet
                    // Keep showing loading state until we get actual data (empty list or populated
                    // list)
                  }
                } catch (Exception e) {
                  Log.e(TAG, "Error processing groups data", e);
                  UiStateManager.showError(
                      rootView,
                      "Error displaying groups",
                      () -> viewModel.loadUserGroups(UserKey, true));
                  loadingStateManager.showError("Error loading groups");
                }
              });

      // Observe loading state
      viewModel
          .getIsLoading()
          .observe(
              this,
              isLoading -> {
                try {
                  Log.d(TAG, "observeViewModel: loading state changed to: " + isLoading);
                  if (isLoading) {
                    loadingStateManager.showLoading("Loading your parties...");
                  } else {
                    // Stop pull-to-refresh indicator when loading is done
                    if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                      swipeRefreshLayout.setRefreshing(false);
                    }
                  }
                } catch (Exception e) {
                  Log.e(TAG, "Error updating loading state", e);
                }
              });

      // Observe error messages
      viewModel
          .getErrorMessage()
          .observe(
              this,
              errorMsg -> {
                try {
                  Log.d(TAG, "observeViewModel: received error message: " + errorMsg);
                  if (errorMsg != null && !errorMsg.isEmpty()) {
                    Log.e(TAG, "Showing error to user: " + errorMsg);
                    UiStateManager.showError(
                        rootView, errorMsg, () -> viewModel.loadUserGroups(UserKey, true));
                    loadingStateManager.showError(errorMsg);
                    viewModel.clearError(); // Clear the error after showing it
                  }
                } catch (Exception e) {
                  Log.e(TAG, "Error displaying error message", e);
                }
              });
    } catch (Exception e) {
      Log.e(TAG, "Error setting up observers", e);
      UserFeedbackManager.showErrorDialog(this, "Error initializing data observers");
    }
  }

  // Sets up all event handlers for UI components.
  private void setupEventHandlers() {
    // אין צורך ב-setOnItemClickListener, הכל עובר דרך ה-Listener של GroupAdapter החדש
  }

  // Sets up the floating chat button with click and touch handlers.
  @SuppressLint("ClickableViewAccessibility")
  private void setupFloatingChatButton() {
    if (fabChat == null) {
      Log.w(TAG, "FloatingActionButton not found");
      return;
    }

    fabChat.setOnClickListener(view -> navigateToChat());
    fabChat.setOnTouchListener(IntentExtrasManager::dragChatButtonOnTouch);
  }

  private void setupBottomNavigation() {
    NavigationManager.setupBottomNavigation(this, "myparties");
  }

  // Navigates to the chat activity.
  private void navigateToChat() {
    try {
      Intent intent = new Intent(MainActivity.this, GptChatActivity.class);
      startActivity(intent);
    } catch (Exception e) {
      Log.e(TAG, "Error navigating to chat", e);
      showError("Failed to open chat");
    }
  }

  // Navigates to the group screen with all necessary data.
  private void navigateToGroupScreen(Group group) {
    if (group == null) {
      Log.e(TAG, "Cannot navigate to null group");
      return;
    }

    try {
      Log.d(
          TAG,
          "Attempting to navigate to PartyMainActivity for group: "
              + group.getGroupName()
              + " with key: "
              + group.getGroupKey());
      Intent intent = new Intent(getBaseContext(), PartyMainActivity.class);

      // Add GroupKey directly to intent
      intent.putExtra("GroupKey", group.getGroupKey());
      Log.d(TAG, "Added GroupKey to intent: " + group.getGroupKey());

      // Also add ExtrasMetadata for backward compatibility
      ExtrasMetadata extras = createExtrasFromGroup(group);
      IntentExtrasManager.addExtrasToIntent(intent, extras);

      // Add UserKey to intent
      intent.putExtra("UserKey", UserKey);
      Log.d(TAG, "Added UserKey to intent: " + UserKey);

      Log.d(TAG, "Starting PartyMainActivity...");
      startActivity(intent);
      Log.d(TAG, "PartyMainActivity started successfully");

    } catch (Exception e) {
      Log.e(TAG, "Error navigating to group screen", e);
      showError("Failed to open group: " + e.getMessage());
    }
  }

  // Creates ExtrasMetadata object from Group data.
  private ExtrasMetadata createExtrasFromGroup(Group group) {
    return new ExtrasMetadata(
        group.getGroupName(),
        group.getGroupKey(),
        group.getGroupDays(),
        group.getGroupMonths(),
        group.getGroupYears(),
        group.getGroupHours(),
        group.getGroupLocation(),
        group.getAdminKey(),
        group.getCreatedAt(),
        group.getGroupPrice(),
        group.getGroupType(),
        group.isCanAdd(),
        group.getFriendKeys(),
        group.getComingKeys(),
        group.getMessageKeys());
  }

  // Shows an empty state when no groups are available
  private void showEmptyState() {
    try {
      ThreadUtils.runOnMainThread(
          () -> {
            if (lv1 != null && groupAdapter != null && groupAdapter.getItemCount() == 0) {
              // Check if we're still loading
              if (viewModel.getIsLoading().getValue() != null
                  && viewModel.getIsLoading().getValue()) {
                // Don't show empty state while loading
                return;
              }

              // Show empty state message
              TextView emptyView = findViewById(R.id.emptyGroupsView);
              if (emptyView != null) {
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText("לא נמצאו קבוצות. לחץ על + ליצירת קבוצה חדשה");
              } else {
                // Show snackbar as fallback
                UiStateManager.showInfo(rootView, "לא נמצאו קבוצות. לחץ על + ליצירת קבוצה חדשה");
              }
            }
          });
    } catch (Exception e) {
      Log.e(TAG, "Error showing empty state", e);
    }
  }

  // Hides the empty state
  private void hideEmptyState() {
    try {
      ThreadUtils.runOnMainThread(
          () -> {
            TextView emptyView = findViewById(R.id.emptyGroupsView);
            if (emptyView != null) {
              emptyView.setVisibility(View.GONE);
            }
          });
    } catch (Exception e) {
      Log.e(TAG, "Error hiding empty state", e);
    }
  }

  // Checks if the current user is a member of the given group.
  private boolean isUserInGroup(Group group) {
    HashMap<String, Object> userKeys = group.getFriendKeys();
    if (userKeys == null || UserKey == null) {
      return false;
    }

    return userKeys.containsKey(UserKey);
  }

  // Sorts groups by creation date and updates the adapter.
  private void sortAndDisplayGroups() {
    // הסר את כל השימושים ב-groupList ו-GroupAdapter הישן (כולל updateUI, sortAndDisplayGroups וכו')
  }

  // Validates if the given position is within bounds.
  private boolean isValidPosition(int position) {
    return groupAdapter != null && position >= 0 && position < groupAdapter.getItemCount();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    Intent goToNextActivity;

    if (itemId == R.id.settings) {
      goToNextActivity = new Intent(getApplicationContext(), ServerSettingsActivity.class);
      startActivity(goToNextActivity);
    } else if (itemId == R.id.logout) {
      handleLogout();
    }

    return true;
  }

  // Navigates to the specified activity class.
  private void navigateToActivity(Class<?> activityClass) {
    Intent intent = new Intent(getApplicationContext(), activityClass);
    startActivity(intent);
  }

  // Handles user logout process
  private void handleLogout() {
    UserFeedbackManager.showConfirmationDialog(
        this,
        "Logout",
        "Are you sure you want to logout?",
        () -> {
          try {
            // Show logout progress
            loadingStateManager.showLoading("Logging out...");

            // Clear ViewModel data first
            if (viewModel != null) {
              viewModel.clearAllData();
            }

            // Clear all user data including Room database
            AuthenticationManager.logout(this);

            // Clear the explicit login flag
            SharedPreferences prefs = getSharedPreferences("PartyMakerPrefs", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("user_explicitly_logged_in", false).apply();

            // Show success message briefly before navigating
            UiStateManager.showSuccess(rootView, "Logged out successfully");

            // Navigate after a short delay
            ThreadUtils.runOnMainThreadDelayed(
                () -> {
                  navigateToLogin();
                  Log.d(TAG, "User logged out successfully");
                },
                1000);

          } catch (Exception e) {
            Log.e(TAG, "Error during logout", e);
            UiStateManager.showError(rootView, "Logout failed", this::handleLogout);
            loadingStateManager.showError("Logout failed");
          }
        });
  }

  // Navigates to the login activity and finishes current activity.
  private void navigateToLogin() {
    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
    startActivity(intent);
    finish();
  }

  /**
   * Shows an error message to the user.
   *
   * @param message The error message to display
   */
  protected void showError(String message) {
    try {
      UiStateManager.showError(rootView, message);
      Log.e(TAG, "Error shown to user: " + message);
    } catch (Exception e) {
      Log.e(TAG, "Error showing error message", e);
      // Fallback to toast if snackbar fails
      Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }

  // Update the UI with the current group list
  private void updateUI() {
    // הסר את כל השימושים ב-groupList ו-GroupAdapter הישן (כולל updateUI, sortAndDisplayGroups וכו')
  }

  @Override
  protected void onResume() {
    super.onResume();

    // Refresh groups when returning to the activity, but only if enough time has passed
    // This catches cases where user was added to a group while away
    if (viewModel != null && UserKey != null) {
      long currentTime = System.currentTimeMillis();
      long timeSinceLastRefresh = currentTime - lastRefreshTime;

      if (timeSinceLastRefresh > REFRESH_COOLDOWN_MS) {
        Log.d(
            TAG,
            "onResume: Refreshing user groups to catch any new invitations (last refresh: "
                + timeSinceLastRefresh
                + "ms ago)");
        viewModel.loadUserGroups(UserKey, true); // Force refresh from server
        lastRefreshTime = currentTime;
      } else {
        Log.d(
            TAG,
            "onResume: Skipping refresh - too soon since last refresh ("
                + timeSinceLastRefresh
                + "ms ago, cooldown: "
                + REFRESH_COOLDOWN_MS
                + "ms)");
      }
    }
  }
}
