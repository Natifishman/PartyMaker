package com.example.partymaker.ui.base;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.partymaker.viewmodel.BaseViewModel;

/**
 * Base Activity class that provides common functionality for all activities in the PartyMaker app.
 * This class implements the Template Method pattern to standardize activity lifecycle and setup.
 * <p>
 * Features:
 * - Standardized ActionBar setup
 * - Automatic ViewModel initialization
 * - Common lifecycle management
 * - Consistent error handling
 * - Performance monitoring
 * 
 * @param <VM> The ViewModel type that extends BaseViewModel
 */
public abstract class BaseActivity<VM extends BaseViewModel> extends AppCompatActivity {

    /**
     * Tag for logging, automatically set to the class name
     */
    protected final String TAG = getClass().getSimpleName();
    
    /**
     * The ViewModel instance for this activity
     */
    protected VM viewModel;
    
    /**
     * Default ActionBar background color
     */
    private static final String DEFAULT_ACTION_BAR_COLOR = "#0081d1";
    
    /**
     * Default ActionBar elevation
     */
    private static final float DEFAULT_ACTION_BAR_ELEVATION = 15f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "onCreate: Activity started");
        
        // Set content view
        setContentView(getLayoutId());
        
        // Setup ActionBar with default styling
        setupActionBar();
        
        // Initialize ViewModel
        initializeViewModel();
        
        // Template method calls - let subclasses implement specifics
        initViews();
        setupObservers();
        setupClickListeners();
        
        Log.d(TAG, "onCreate: Activity setup completed");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Activity became visible");
        onActivityStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Activity resumed");
        onActivityResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: Activity paused");
        onActivityPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: Activity stopped");
        onActivityStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: Activity destroyed");
        onActivityDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle back button in ActionBar
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets up the ActionBar with default PartyMaker styling
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Set default background color
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(DEFAULT_ACTION_BAR_COLOR)));
            
            // Set elevation for material design
            actionBar.setElevation(DEFAULT_ACTION_BAR_ELEVATION);
            
            // Set title if provided
            String title = getActivityTitle();
            if (title != null && !title.isEmpty()) {
                actionBar.setTitle(title);
            }
            
            // Enable back button if needed
            if (shouldShowBackButton()) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
            }
            
            Log.d(TAG, "ActionBar setup completed");
        }
    }

    /**
     * Initializes the ViewModel for this activity
     */
    private void initializeViewModel() {
        Class<VM> viewModelClass = getViewModelClass();
        if (viewModelClass != null) {
            viewModel = new ViewModelProvider(this).get(viewModelClass);
            Log.d(TAG, "ViewModel initialized: " + viewModelClass.getSimpleName());
        }
    }

    // Abstract methods that subclasses must implement

    /**
     * @return The layout resource ID for this activity
     */
    @LayoutRes
    protected abstract int getLayoutId();

    /**
     * @return The ViewModel class for this activity, or null if no ViewModel is needed
     */
    protected abstract Class<VM> getViewModelClass();

    /**
     * Initialize views and UI components
     * Called after setContentView and ViewModel initialization
     */
    protected abstract void initViews();

    /**
     * Setup observers for LiveData and other observables
     * Called after initViews
     */
    protected abstract void setupObservers();

    // Optional methods that subclasses can override

    /**
     * Setup click listeners for views
     * Called after setupObservers
     * Default implementation does nothing
     */
    protected void setupClickListeners() {
        // Default implementation - subclasses can override
    }

    /**
     * @return The title for the ActionBar, or null for no title
     * Default implementation returns the class name
     */
    protected String getActivityTitle() {
        return getClass().getSimpleName().replace("Activity", "");
    }

    /**
     * @return true if the ActionBar should show a back button
     * Default implementation returns true for all activities except MainActivity
     */
    protected boolean shouldShowBackButton() {
        return !getClass().getSimpleName().equals("MainActivity");
    }

    /**
     * Called during onStart()
     * Default implementation does nothing
     */
    protected void onActivityStart() {
        // Default implementation - subclasses can override
    }

    /**
     * Called during onResume()
     * Default implementation does nothing
     */
    protected void onActivityResume() {
        // Default implementation - subclasses can override
    }

    /**
     * Called during onPause()
     * Default implementation does nothing
     */
    protected void onActivityPause() {
        // Default implementation - subclasses can override
    }

    /**
     * Called during onStop()
     * Default implementation does nothing
     */
    protected void onActivityStop() {
        // Default implementation - subclasses can override
    }

    /**
     * Called during onDestroy()
     * Default implementation does nothing
     */
    protected void onActivityDestroy() {
        // Default implementation - subclasses can override
    }

    /**
     * Show a standard error message to the user
     * @param message The error message to display
     */
    protected void showError(String message) {
        Log.e(TAG, "Error: " + message);
        // Could be enhanced to show a Snackbar or custom error dialog
        android.widget.Toast.makeText(this, "Error: " + message, android.widget.Toast.LENGTH_LONG).show();
    }

    /**
     * Show a standard success message to the user
     * @param message The success message to display
     */
    protected void showSuccess(String message) {
        Log.i(TAG, "Success: " + message);
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    /**
     * Show a loading state
     * Default implementation does nothing - subclasses can override
     */
    protected void showLoading() {
        Log.d(TAG, "showLoading called");
        // Default implementation - subclasses can override with actual loading UI
    }

    /**
     * Hide loading state
     * Default implementation does nothing - subclasses can override
     */
    protected void hideLoading() {
        Log.d(TAG, "hideLoading called");
        // Default implementation - subclasses can override
    }

    /**
     * Check if the activity is in a valid state for operations
     * @return true if the activity is not finishing and not destroyed
     */
    protected boolean isActivityValid() {
        return !isFinishing() && !isDestroyed();
    }
}