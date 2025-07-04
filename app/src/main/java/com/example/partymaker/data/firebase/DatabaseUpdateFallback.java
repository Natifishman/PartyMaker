package com.example.partymaker.data.firebase;

import android.util.Log;

import com.example.partymaker.data.model.ServerResponse;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Fallback implementation of DatabaseService that uses Firebase directly
 * This is used when the server is not available
 */
public class DatabaseUpdateFallback implements DatabaseService {
    private static final String TAG = "DatabaseUpdateFallback";
    private final FirebaseDatabase database;
    
    /**
     * Constructor
     */
    public DatabaseUpdateFallback() {
        database = FirebaseDatabase.getInstance();
        Log.d(TAG, "DatabaseUpdateFallback initialized - using direct Firebase access");
    }
    
    /**
     * Set a value at a specific path
     * 
     * @param path Database path
     * @param value Value to set
     * @return CompletableFuture with server response
     */
    @Override
    public CompletableFuture<ServerResponse<Object>> setValue(String path, Object value) {
        CompletableFuture<ServerResponse<Object>> future = new CompletableFuture<>();
        Log.d(TAG, "Setting value at path: " + path + " (direct Firebase access)");
        
        DatabaseReference reference = database.getReference(path);
        reference.setValue(value, (error, ref) -> {
            if (error != null) {
                Log.e(TAG, "Error setting value at path: " + path, error.toException());
                future.complete(new ServerResponse<>(500, "Error: " + error.getMessage(), null));
            } else {
                Log.d(TAG, "Successfully set value at path: " + path);
                future.complete(new ServerResponse<>(200, "Success", value));
            }
        });
        
        return future;
    }
    
    /**
     * Get a value from a specific path
     * 
     * @param path Database path
     * @return CompletableFuture with server response
     */
    @Override
    public CompletableFuture<ServerResponse<Object>> getValue(String path) {
        CompletableFuture<ServerResponse<Object>> future = new CompletableFuture<>();
        Log.d(TAG, "Getting value at path: " + path + " (direct Firebase access)");
        
        DatabaseReference reference = database.getReference(path);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Successfully retrieved value at path: " + path);
                future.complete(new ServerResponse<>(200, "Success", dataSnapshot.getValue()));
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error retrieving value at path: " + path, databaseError.toException());
                future.complete(new ServerResponse<>(500, "Error: " + databaseError.getMessage(), null));
            }
        });
        
        return future;
    }
    
    /**
     * Update multiple values at a specific path
     * 
     * @param path Database path
     * @param updates Map of updates to apply
     * @return CompletableFuture with server response
     */
    @Override
    public CompletableFuture<ServerResponse<Object>> updateChildren(String path, Map<String, Object> updates) {
        CompletableFuture<ServerResponse<Object>> future = new CompletableFuture<>();
        Log.d(TAG, "Updating children at path: " + path + " (direct Firebase access)");
        
        DatabaseReference reference = database.getReference(path);
        reference.updateChildren(updates, (error, ref) -> {
            if (error != null) {
                Log.e(TAG, "Error updating children at path: " + path, error.toException());
                future.complete(new ServerResponse<>(500, "Error: " + error.getMessage(), null));
            } else {
                Log.d(TAG, "Successfully updated children at path: " + path);
                future.complete(new ServerResponse<>(200, "Success", updates));
            }
        });
        
        return future;
    }
    
    /**
     * Get all children at a specific path
     * 
     * @param path Database path
     * @return CompletableFuture with server response
     */
    @Override
    public CompletableFuture<ServerResponse<Object>> getChildren(String path) {
        CompletableFuture<ServerResponse<Object>> future = new CompletableFuture<>();
        Log.d(TAG, "Getting children at path: " + path + " (direct Firebase access)");
        
        DatabaseReference reference = database.getReference(path);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Successfully retrieved children at path: " + path);
                future.complete(new ServerResponse<>(200, "Success", dataSnapshot.getValue()));
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error retrieving children at path: " + path, databaseError.toException());
                future.complete(new ServerResponse<>(500, "Error: " + databaseError.getMessage(), null));
            }
        });
        
        return future;
    }
    
    /**
     * Remove a value at a specific path
     * 
     * @param path Database path
     * @return CompletableFuture with server response
     */
    @Override
    public CompletableFuture<ServerResponse<Object>> removeValue(String path) {
        CompletableFuture<ServerResponse<Object>> future = new CompletableFuture<>();
        Log.d(TAG, "Removing value at path: " + path + " (direct Firebase access)");
        
        DatabaseReference reference = database.getReference(path);
        reference.removeValue((error, ref) -> {
            if (error != null) {
                Log.e(TAG, "Error removing value at path: " + path, error.toException());
                future.complete(new ServerResponse<>(500, "Error: " + error.getMessage(), null));
            } else {
                Log.d(TAG, "Successfully removed value at path: " + path);
                future.complete(new ServerResponse<>(200, "Success", null));
            }
        });
        
        return future;
    }
} 