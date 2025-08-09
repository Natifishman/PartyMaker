package com.example.partymaker.utils.accessibility;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;

/**
 * Helper class for implementing keyboard navigation support.
 * Ensures keyboard users can navigate the app effectively.
 */
public class KeyboardNavigationHelper {

    private final Activity activity;
    private OnEscapeKeyListener escapeKeyListener;

    /**
     * Interface for handling escape key events.
     */
    public interface OnEscapeKeyListener {
        /**
         * Called when the escape key is pressed.
         * @return true if the event was handled, false otherwise
         */
        boolean onEscapePressed();
    }

    /**
     * Creates a new keyboard navigation helper.
     *
     * @param activity The activity to set up navigation for
     */
    public KeyboardNavigationHelper(@NonNull Activity activity) {
        this.activity = activity;
    }

    /**
     * Sets up keyboard navigation for the activity.
     */
    public void setupKeyboardNavigation() {
        View decorView = activity.getWindow().getDecorView();
        decorView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_TAB:
                            return handleTabNavigation(event);
                        case KeyEvent.KEYCODE_ENTER:
                            return handleEnterKey();
                        case KeyEvent.KEYCODE_ESCAPE:
                            return handleEscapeKey();
                        case KeyEvent.KEYCODE_DPAD_UP:
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            return handleDpadNavigation(keyCode);
                    }
                }
                return false;
            }
        });
    }

    /**
     * Handles tab navigation between focusable views.
     *
     * @param event The key event
     * @return true if handled, false otherwise
     */
    private boolean handleTabNavigation(@NonNull KeyEvent event) {
        View currentFocus = activity.getCurrentFocus();
        if (currentFocus != null) {
            int direction = event.isShiftPressed() ? View.FOCUS_BACKWARD : View.FOCUS_FORWARD;
            View nextFocus = currentFocus.focusSearch(direction);
            if (nextFocus != null && nextFocus.requestFocus()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles enter key press on focused views.
     *
     * @return true if handled, false otherwise
     */
    private boolean handleEnterKey() {
        View currentFocus = activity.getCurrentFocus();
        if (currentFocus != null && !(currentFocus instanceof EditText)) {
            // Perform click on non-EditText views
            return currentFocus.performClick();
        }
        return false;
    }

    /**
     * Handles escape key press (typically to close dialogs or go back).
     *
     * @return true if handled, false otherwise
     */
    private boolean handleEscapeKey() {
        if (escapeKeyListener != null && escapeKeyListener.onEscapePressed()) {
            return true;
        }
        // Default behavior: go back
        activity.onBackPressed();
        return true;
    }

    /**
     * Handles D-pad navigation for TV and keyboard users.
     *
     * @param keyCode The D-pad key code
     * @return true if handled, false otherwise
     */
    private boolean handleDpadNavigation(int keyCode) {
        View currentFocus = activity.getCurrentFocus();
        if (currentFocus != null) {
            int direction = View.FOCUS_DOWN;
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    direction = View.FOCUS_UP;
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    direction = View.FOCUS_DOWN;
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    direction = View.FOCUS_LEFT;
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    direction = View.FOCUS_RIGHT;
                    break;
            }
            
            View nextFocus = currentFocus.focusSearch(direction);
            if (nextFocus != null && nextFocus.requestFocus()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets up a skip link to jump to main content.
     *
     * @param mainContentId The ID of the main content view
     */
    public void setupSkipToMainContent(int mainContentId) {
        // This would typically be implemented as the first focusable element
        View mainContent = activity.findViewById(mainContentId);
        if (mainContent != null) {
            // Make sure main content is focusable
            mainContent.setFocusable(true);
            mainContent.setFocusableInTouchMode(false);
        }
    }

    /**
     * Sets up focus order for a form with multiple fields.
     *
     * @param fieldIds Array of field IDs in desired focus order
     */
    public void setupFormFocusOrder(@NonNull int... fieldIds) {
        for (int i = 0; i < fieldIds.length - 1; i++) {
            View currentField = activity.findViewById(fieldIds[i]);
            View nextField = activity.findViewById(fieldIds[i + 1]);
            
            if (currentField != null && nextField != null) {
                currentField.setNextFocusDownId(fieldIds[i + 1]);
                currentField.setNextFocusForwardId(fieldIds[i + 1]);
                nextField.setNextFocusUpId(fieldIds[i]);
            }
        }
        
        // Request focus on the first field
        if (fieldIds.length > 0) {
            View firstField = activity.findViewById(fieldIds[0]);
            if (firstField != null) {
                firstField.requestFocus();
            }
        }
    }

    /**
     * Sets a listener for escape key events.
     *
     * @param listener The escape key listener
     */
    public void setEscapeKeyListener(OnEscapeKeyListener listener) {
        this.escapeKeyListener = listener;
    }

    /**
     * Makes all buttons in a view group properly focusable for keyboard navigation.
     *
     * @param viewGroup The view group containing buttons
     */
    public void makeButtonsKeyboardNavigable(@NonNull ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                makeButtonsKeyboardNavigable((ViewGroup) child);
            } else if (child.isClickable()) {
                child.setFocusable(true);
                child.setFocusableInTouchMode(false);
            }
        }
    }

    /**
     * Ensures proper focus indicators are visible.
     *
     * @param view The view to set focus indicators for
     */
    public static void ensureFocusIndicators(@NonNull View view) {
        // Set a focus change listener to ensure visibility
        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Ensure the view is visible when focused
                    v.setAlpha(1.0f);
                    
                    // You could also add a custom focus indicator here
                    // For example, changing the background or adding an outline
                }
            }
        });
    }
}