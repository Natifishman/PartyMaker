package com.example.partymaker.server.service;

import com.example.partymaker.server.exception.ResourceNotFoundException;
import com.example.partymaker.server.model.ServerResponse;
import com.example.partymaker.server.model.User;
import com.example.partymaker.server.util.FirebaseUtil;
import com.google.firebase.database.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * שירות לטיפול בפעולות מסד נתונים
 */
@Service
public class DatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    
    private final FirebaseDatabase firebaseDatabase;
    private final FirebaseUtil firebaseUtil;
    private final DatabaseReference database;
    
    @Autowired
    public DatabaseService(FirebaseDatabase firebaseDatabase, FirebaseUtil firebaseUtil) {
        this.firebaseDatabase = firebaseDatabase;
        this.firebaseUtil = firebaseUtil;
        this.database = FirebaseDatabase.getInstance().getReference();
    }
    
    /**
     * מגדיר ערך בנתיב מסוים במסד הנתונים
     * 
     * @param path הנתיב במסד הנתונים
     * @param value הערך לשמירה
     * @return תגובת שרת עם סטטוס הפעולה
     */
    public CompletableFuture<ServerResponse> setValue(String path, Object value) {
        CompletableFuture<ServerResponse> future = new CompletableFuture<>();
        DatabaseReference ref = firebaseDatabase.getReference(path);
        
        ref.setValue(value, (error, ref1) -> {
            if (error != null) {
                logger.error("Error setting value at {}: {}", path, error.getMessage());
                future.complete(new ServerResponse("error", error.getMessage()));
            } else {
                future.complete(new ServerResponse("success", null));
            }
        });
        
        return future;
    }
    
    /**
     * מעדכן מספר ערכים בנתיב מסוים במסד הנתונים
     * 
     * @param path הנתיב במסד הנתונים
     * @param updates מפה של עדכונים לביצוע
     * @return תגובת שרת עם סטטוס הפעולה
     */
    public CompletableFuture<ServerResponse> updateChildren(String path, Map<String, Object> updates) {
        CompletableFuture<ServerResponse> future = new CompletableFuture<>();
        DatabaseReference ref = firebaseDatabase.getReference(path);
        
        ref.updateChildren(updates, (error, ref1) -> {
            if (error != null) {
                logger.error("Error updating children at {}: {}", path, error.getMessage());
                future.complete(new ServerResponse("error", error.getMessage()));
            } else {
                future.complete(new ServerResponse("success", null));
            }
        });
        
        return future;
    }
    
    /**
     * מוחק ערך בנתיב מסוים במסד הנתונים
     * 
     * @param path הנתיב במסד הנתונים
     * @return תגובת שרת עם סטטוס הפעולה
     */
    public CompletableFuture<ServerResponse> removeValue(String path) {
        CompletableFuture<ServerResponse> future = new CompletableFuture<>();
        DatabaseReference ref = firebaseDatabase.getReference(path);
        
        ref.removeValue((error, ref1) -> {
            if (error != null) {
                logger.error("Error removing value at {}: {}", path, error.getMessage());
                future.complete(new ServerResponse("error", error.getMessage()));
            } else {
                future.complete(new ServerResponse("success", null));
            }
        });
        
        return future;
    }
    
    /**
     * קורא ערך מנתיב מסוים
     * 
     * @param path הנתיב במסד הנתונים
     * @return תגובה עם הערך שנקרא
     */
    public CompletableFuture<ServerResponse> getValue(String path) {
        CompletableFuture<ServerResponse> future = new CompletableFuture<>();
        DatabaseReference ref = firebaseDatabase.getReference(path);
        
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                future.complete(new ServerResponse("success", snapshot.getValue()));
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                logger.error("Error getting value at {}: {}", path, error.getMessage());
                future.complete(new ServerResponse("error", error.getMessage()));
            }
        });
        
        return future;
    }
    
    /**
     * קורא את כל הילדים בנתיב מסוים
     * 
     * @param path הנתיב במסד הנתונים
     * @return תגובה עם מפת הילדים שנקראו
     */
    public CompletableFuture<ServerResponse> getChildren(String path) {
        CompletableFuture<ServerResponse> future = new CompletableFuture<>();
        DatabaseReference ref = firebaseDatabase.getReference(path);
        
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> children = new HashMap<>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    children.put(child.getKey(), child.getValue());
                }
                future.complete(new ServerResponse("success", children));
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                logger.error("Failed to read children at path {}: {}", path, databaseError.getMessage());
                future.complete(new ServerResponse("error", "Failed to read children: " + databaseError.getMessage()));
            }
        });
        
        return future;
    }
    
    /**
     * מוסיף ערך חדש עם מפתח אוטומטי
     * 
     * @param path הנתיב במסד הנתונים
     * @param value הערך לשמירה
     * @return מזהה הערך החדש
     */
    public CompletableFuture<String> push(String path, Object value) {
        CompletableFuture<String> future = new CompletableFuture<>();
        DatabaseReference ref = firebaseDatabase.getReference(path).push();
        
        ref.setValue(value, (error, ref1) -> {
            if (error != null) {
                logger.error("Error pushing value to {}: {}", path, error.getMessage());
                future.completeExceptionally(new RuntimeException(error.getMessage()));
            } else {
                future.complete(ref.getKey());
            }
        });
        
        return future;
    }

    public ServerResponse<Object> getData(String path) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<ServerResponse<Object>> response = new AtomicReference<>();
            
            database.child(path).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Object value = snapshot.getValue();
                    if (value == null) {
                        response.set(new ServerResponse<>(404, "Data not found", null));
                    } else {
                        response.set(new ServerResponse<>(200, "Data retrieved successfully", value));
                    }
                    latch.countDown();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    response.set(new ServerResponse<>(500, "Failed to retrieve data: " + error.getMessage(), null));
                    latch.countDown();
                }
            });
            
            latch.await(5, TimeUnit.SECONDS);
            return response.get() != null ? response.get() : new ServerResponse<>(500, "Operation timed out", null);
        } catch (Exception e) {
            logger.error("Error getting data", e);
            return new ServerResponse<>(500, "Internal server error", null);
        }
    }

    public ServerResponse<Object> setData(String path, Object data) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<ServerResponse<Object>> response = new AtomicReference<>();
            
            database.child(path).setValue(data, (error, ref) -> {
                if (error != null) {
                    response.set(new ServerResponse<>(500, "Failed to set data: " + error.getMessage(), null));
                } else {
                    response.set(new ServerResponse<>(200, "Data set successfully", data));
                }
                latch.countDown();
            });
            
            latch.await(5, TimeUnit.SECONDS);
            return response.get() != null ? response.get() : new ServerResponse<>(500, "Operation timed out", null);
        } catch (Exception e) {
            logger.error("Error setting data", e);
            return new ServerResponse<>(500, "Internal server error", null);
        }
    }

    public ServerResponse<Object> updateData(String path, Object data) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<ServerResponse<Object>> response = new AtomicReference<>();
            
            database.child(path).updateChildren((Map<String, Object>) data, (error, ref) -> {
                if (error != null) {
                    response.set(new ServerResponse<>(500, "Failed to update data: " + error.getMessage(), null));
                } else {
                    response.set(new ServerResponse<>(200, "Data updated successfully", data));
                }
                latch.countDown();
            });
            
            latch.await(5, TimeUnit.SECONDS);
            return response.get() != null ? response.get() : new ServerResponse<>(500, "Operation timed out", null);
        } catch (Exception e) {
            logger.error("Error updating data", e);
            return new ServerResponse<>(500, "Internal server error", null);
        }
    }

    public ServerResponse<Object> deleteData(String path) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<ServerResponse<Object>> response = new AtomicReference<>();
            
            database.child(path).removeValue((error, ref) -> {
                if (error != null) {
                    response.set(new ServerResponse<>(500, "Failed to delete data: " + error.getMessage(), null));
                } else {
                    response.set(new ServerResponse<>(200, "Data deleted successfully", null));
                }
                latch.countDown();
            });
            
            latch.await(5, TimeUnit.SECONDS);
            return response.get() != null ? response.get() : new ServerResponse<>(500, "Operation timed out", null);
        } catch (Exception e) {
            logger.error("Error deleting data", e);
            return new ServerResponse<>(500, "Internal server error", null);
        }
    }
} 