package com.example.partymaker.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.partymaker.viewmodel.BaseViewModel;

/**
 * Base Fragment class that provides common functionality for all fragments in the PartyMaker app.
 * This class implements the Template Method pattern to standardize fragment lifecycle and setup.
 * 
 * Features:
 * - Standardized lifecycle management
 * - Automatic ViewModel initialization
 * - Common error handling
 * - Performance monitoring
 * - Memory leak prevention
 * 
 * @param <VM> The ViewModel type that extends BaseViewModel
 */
public abstract class BaseFragment<VM extends BaseViewModel> extends Fragment {

    /**
     * Tag for logging, automatically set to the class name
     */
    protected final String TAG = getClass().getSimpleName();
    
    /**
     * The ViewModel instance for this fragment
     */
    protected VM viewModel;
    
    /**
     * Reference to the host activity
     */
    protected BaseActivity<?> baseActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: Fragment attached to context");
        
        // Store reference to host activity if it's a BaseActivity
        if (context instanceof BaseActivity) {
            baseActivity = (BaseActivity<?>) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Creating fragment view");
        
        // Inflate the layout
        View view = inflater.inflate(getLayoutId(), container, false);
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        Log.d(TAG, "onViewCreated: View created, setting up fragment");
        
        // Initialize ViewModel
        initializeViewModel();
        
        // Template method calls - let subclasses implement specifics
        initViews(view);
        setupObservers();
        setupClickListeners(view);
        
        Log.d(TAG, "onViewCreated: Fragment setup completed");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Fragment became visible");
        onFragmentStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Fragment resumed");
        onFragmentResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: Fragment paused");
        onFragmentPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: Fragment stopped");
        onFragmentStop();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView: Fragment view destroyed");
        onFragmentDestroyView();
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach: Fragment detached");
        baseActivity = null; // Prevent memory leaks
        super.onDetach();
    }

    /**
     * Initializes the ViewModel for this fragment
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
     * @return The layout resource ID for this fragment
     */
    @LayoutRes
    protected abstract int getLayoutId();

    /**
     * @return The ViewModel class for this fragment, or null if no ViewModel is needed
     */
    protected abstract Class<VM> getViewModelClass();

    /**
     * Initialize views and UI components
     * Called after onViewCreated and ViewModel initialization
     * 
     * @param view The root view of the fragment
     */
    protected abstract void initViews(@NonNull View view);

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
     * 
     * @param view The root view of the fragment
     */
    protected void setupClickListeners(@NonNull View view) {
        // Default implementation - subclasses can override
    }

    /**
     * Called during onStart()
     * Default implementation does nothing
     */
    protected void onFragmentStart() {
        // Default implementation - subclasses can override
    }

    /**
     * Called during onResume()
     * Default implementation does nothing
     */
    protected void onFragmentResume() {
        // Default implementation - subclasses can override
    }

    /**
     * Called during onPause()
     * Default implementation does nothing
     */
    protected void onFragmentPause() {
        // Default implementation - subclasses can override
    }

    /**
     * Called during onStop()
     * Default implementation does nothing
     */
    protected void onFragmentStop() {
        // Default implementation - subclasses can override
    }

    /**
     * Called during onDestroyView()
     * Use this to clean up view-specific resources
     * Default implementation does nothing
     */
    protected void onFragmentDestroyView() {
        // Default implementation - subclasses can override
    }

    /**
     * Show a standard error message to the user
     * @param message The error message to display
     */
    protected void showError(String message) {
        Log.e(TAG, "Error: " + message);
        if (baseActivity != null) {
            baseActivity.showError(message);
        } else if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), "Error: " + message, android.widget.Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Show a standard success message to the user
     * @param message The success message to display
     */
    protected void showSuccess(String message) {
        Log.i(TAG, "Success: " + message);
        if (baseActivity != null) {
            baseActivity.showSuccess(message);
        } else if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show a loading state
     * Default implementation delegates to BaseActivity if available
     */
    protected void showLoading() {
        Log.d(TAG, "showLoading called");
        if (baseActivity != null) {
            baseActivity.showLoading();
        }
    }

    /**
     * Hide loading state
     * Default implementation delegates to BaseActivity if available
     */
    protected void hideLoading() {
        Log.d(TAG, "hideLoading called");
        if (baseActivity != null) {
            baseActivity.hideLoading();
        }
    }

    /**
     * Check if the fragment is in a valid state for operations
     * @return true if the fragment is added and has a context
     */
    protected boolean isFragmentValid() {
        return isAdded() && getContext() != null && !isDetached();
    }

    /**
     * Safely get a string resource
     * @param resId The string resource ID
     * @return The string value, or empty string if not available
     */
    protected String safeGetString(int resId) {
        try {
            return getString(resId);
        } catch (Exception e) {
            Log.e(TAG, "Error getting string resource: " + resId, e);
            return "";
        }
    }
}