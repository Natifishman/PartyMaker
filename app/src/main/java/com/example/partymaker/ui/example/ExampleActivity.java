package com.example.partymaker.ui.example;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.partymaker.R;
import com.example.partymaker.data.model.ServerResponse;
import com.example.partymaker.data.repository.DataRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Example activity demonstrating how to use the DataRepository for database operations.
 * All operations are routed through the Spring Boot server.
 */
public class ExampleActivity extends AppCompatActivity {
    private static final String TAG = "ExampleActivity";
    
    private DataRepository dataRepository;
    private EditText pathEditText;
    private EditText valueEditText;
    private TextView resultTextView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);
        
        // Initialize the DataRepository
        dataRepository = DataRepository.getInstance(this);
        
        // Initialize UI components
        pathEditText = findViewById(R.id.path_edit_text);
        valueEditText = findViewById(R.id.value_edit_text);
        resultTextView = findViewById(R.id.result_text_view);
        
        // Set up button click listeners
        Button getValueButton = findViewById(R.id.get_value_button);
        Button setValueButton = findViewById(R.id.set_value_button);
        Button getChildrenButton = findViewById(R.id.get_children_button);
        Button updateChildrenButton = findViewById(R.id.update_children_button);
        Button removeValueButton = findViewById(R.id.remove_value_button);
        
        getValueButton.setOnClickListener(v -> getValue());
        setValueButton.setOnClickListener(v -> setValue());
        getChildrenButton.setOnClickListener(v -> getChildren());
        updateChildrenButton.setOnClickListener(v -> updateChildren());
        removeValueButton.setOnClickListener(v -> removeValue());
    }
    
    /**
     * Get a value from the database
     */
    private void getValue() {
        String path = pathEditText.getText().toString().trim();
        if (path.isEmpty()) {
            showToast("Please enter a path");
            return;
        }
        
        showLoading();
        dataRepository.getValue(path)
                .thenAccept(response -> runOnUiThread(() -> {
                    hideLoading();
                    if (response.isSuccess()) {
                        resultTextView.setText("Value: " + response.getData());
                    } else {
                        resultTextView.setText("Error: " + response.getMessage());
                    }
                }))
                .exceptionally(e -> {
                    Log.e(TAG, "Error getting value", e);
                    runOnUiThread(() -> {
                        hideLoading();
                        resultTextView.setText("Error: " + e.getMessage());
                    });
                    return null;
                });
    }
    
    /**
     * Set a value in the database
     */
    private void setValue() {
        String path = pathEditText.getText().toString().trim();
        String value = valueEditText.getText().toString().trim();
        if (path.isEmpty() || value.isEmpty()) {
            showToast("Please enter both path and value");
            return;
        }
        
        showLoading();
        dataRepository.setValue(path, value)
                .thenAccept(response -> runOnUiThread(() -> {
                    hideLoading();
                    if (response.isSuccess()) {
                        resultTextView.setText("Value set successfully");
                        showToast("Value set successfully");
                    } else {
                        resultTextView.setText("Error: " + response.getMessage());
                    }
                }))
                .exceptionally(e -> {
                    Log.e(TAG, "Error setting value", e);
                    runOnUiThread(() -> {
                        hideLoading();
                        resultTextView.setText("Error: " + e.getMessage());
                    });
                    return null;
                });
    }
    
    /**
     * Get children from the database
     */
    private void getChildren() {
        String path = pathEditText.getText().toString().trim();
        if (path.isEmpty()) {
            showToast("Please enter a path");
            return;
        }
        
        showLoading();
        dataRepository.getChildren(path)
                .thenAccept(response -> runOnUiThread(() -> {
                    hideLoading();
                    if (response.isSuccess()) {
                        resultTextView.setText("Children: " + response.getData());
                    } else {
                        resultTextView.setText("Error: " + response.getMessage());
                    }
                }))
                .exceptionally(e -> {
                    Log.e(TAG, "Error getting children", e);
                    runOnUiThread(() -> {
                        hideLoading();
                        resultTextView.setText("Error: " + e.getMessage());
                    });
                    return null;
                });
    }
    
    /**
     * Update children in the database
     */
    private void updateChildren() {
        String path = pathEditText.getText().toString().trim();
        String value = valueEditText.getText().toString().trim();
        if (path.isEmpty() || value.isEmpty()) {
            showToast("Please enter both path and value");
            return;
        }
        
        // Create a simple update map
        Map<String, Object> updates = new HashMap<>();
        updates.put("exampleKey", value);
        
        showLoading();
        dataRepository.updateChildren(path, updates)
                .thenAccept(response -> runOnUiThread(() -> {
                    hideLoading();
                    if (response.isSuccess()) {
                        resultTextView.setText("Children updated successfully");
                        showToast("Children updated successfully");
                    } else {
                        resultTextView.setText("Error: " + response.getMessage());
                    }
                }))
                .exceptionally(e -> {
                    Log.e(TAG, "Error updating children", e);
                    runOnUiThread(() -> {
                        hideLoading();
                        resultTextView.setText("Error: " + e.getMessage());
                    });
                    return null;
                });
    }
    
    /**
     * Remove a value from the database
     */
    private void removeValue() {
        String path = pathEditText.getText().toString().trim();
        if (path.isEmpty()) {
            showToast("Please enter a path");
            return;
        }
        
        showLoading();
        dataRepository.removeValue(path)
                .thenAccept(response -> runOnUiThread(() -> {
                    hideLoading();
                    if (response.isSuccess()) {
                        resultTextView.setText("Value removed successfully");
                        showToast("Value removed successfully");
                    } else {
                        resultTextView.setText("Error: " + response.getMessage());
                    }
                }))
                .exceptionally(e -> {
                    Log.e(TAG, "Error removing value", e);
                    runOnUiThread(() -> {
                        hideLoading();
                        resultTextView.setText("Error: " + e.getMessage());
                    });
                    return null;
                });
    }
    
    /**
     * Show a toast message
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Show loading indicator
     */
    private void showLoading() {
        findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
    }
    
    /**
     * Hide loading indicator
     */
    private void hideLoading() {
        findViewById(R.id.progress_bar).setVisibility(View.GONE);
    }
} 