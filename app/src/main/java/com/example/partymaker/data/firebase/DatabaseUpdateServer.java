package com.example.partymaker.data.firebase;

import android.util.Log;

import com.example.partymaker.data.api.ApiService;
import com.example.partymaker.data.model.ServerResponse;
import com.example.partymaker.data.model.SetValueRequest;
import com.example.partymaker.data.model.UpdateChildrenRequest;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Class for performing database operations through the server.
 * All database operations are routed through the Spring Boot server.
 */
public class DatabaseUpdateServer implements DatabaseService {
    private static final String TAG = "DatabaseUpdateServer";
    
    // API paths
    private static final String API_PATH_DATABASE = "api/database";
    private static final String API_PATH_GET_VALUE = API_PATH_DATABASE + "/getValue";
    private static final String API_PATH_GET_CHILDREN = API_PATH_DATABASE + "/getChildren";
    private static final String API_PATH_SET_VALUE = API_PATH_DATABASE + "/setValue";
    private static final String API_PATH_UPDATE_CHILDREN = API_PATH_DATABASE + "/updateChildren";
    private static final String API_PATH_REMOVE = API_PATH_DATABASE + "/remove";
    private static final String API_PATH_AUTH = "api/auth/signin";
    
    // Try different server URLs
    private static final String[] SERVER_URLS = {
        "http://10.0.2.2:8085",  // Server address in emulator
        "http://localhost:8085", // Local address
        "http://127.0.0.1:8085"  // Alternative local address
    };
    private static String currentServerUrl = SERVER_URLS[0]; // Start with the first address
    
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    // Configure OkHttpClient with improved network error handling
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)  // Increased connection timeout
            .readTimeout(30, TimeUnit.SECONDS)     // Increased read timeout
            .writeTimeout(30, TimeUnit.SECONDS)    // Increased write timeout
            .retryOnConnectionFailure(true)        // Retry on connection failure
            .connectionPool(new ConnectionPool(5, 30, TimeUnit.SECONDS)) // Improved connection management
            .build();
            
    private static final Gson gson = new Gson();

    private final ApiService apiService;
    
    /**
     * Constructor initializes Retrofit with appropriate timeouts and converters
     */
    public DatabaseUpdateServer() {
        // Build Retrofit instance using the shared client
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(currentServerUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();
            
        // Create API service
        apiService = retrofit.create(ApiService.class);
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
        Log.d(TAG, "Setting value at path: " + path);
        
        // Create request body
        SetValueRequest request = new SetValueRequest(path, value);
        
        apiService.setValue(request).enqueue(new retrofit2.Callback<ServerResponse<Object>>() {
            @Override
            public void onResponse(retrofit2.Call<ServerResponse<Object>> call, retrofit2.Response<ServerResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Successfully set value at path: " + path);
                    future.complete(response.body());
                } else {
                    Log.e(TAG, "Error setting value at path: " + path + ", code: " + response.code());
                    future.complete(new ServerResponse<>(
                        response.code(),
                        "Error: " + (response.errorBody() != null ? response.errorBody().toString() : response.message()),
                        null
                    ));
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<ServerResponse<Object>> call, Throwable t) {
                Log.e(TAG, "Network error setting value at path: " + path, t);
                future.complete(new ServerResponse<>(500, "Network error: " + t.getMessage(), null));
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
        Log.d(TAG, "Getting value at path: " + path);
        
        String cleanedPath = cleanPath(path);
        apiService.getValue(cleanedPath).enqueue(new retrofit2.Callback<ServerResponse<Object>>() {
            @Override
            public void onResponse(retrofit2.Call<ServerResponse<Object>> call, retrofit2.Response<ServerResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Successfully retrieved value at path: " + path);
                    future.complete(response.body());
                } else {
                    Log.e(TAG, "Error retrieving value at path: " + path + ", code: " + response.code());
                    future.complete(new ServerResponse<>(
                        response.code(),
                        "Error: " + (response.errorBody() != null ? response.errorBody().toString() : response.message()),
                        null
                    ));
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<ServerResponse<Object>> call, Throwable t) {
                Log.e(TAG, "Network error retrieving value at path: " + path, t);
                future.complete(new ServerResponse<>(500, "Network error: " + t.getMessage(), null));
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
        Log.d(TAG, "Updating children at path: " + path);
        
        // Create request body
        UpdateChildrenRequest request = new UpdateChildrenRequest(path, updates);
        
        apiService.updateChildren(request).enqueue(new retrofit2.Callback<ServerResponse<Object>>() {
            @Override
            public void onResponse(retrofit2.Call<ServerResponse<Object>> call, retrofit2.Response<ServerResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Successfully updated children at path: " + path);
                    future.complete(response.body());
                } else {
                    Log.e(TAG, "Error updating children at path: " + path + ", code: " + response.code());
                    future.complete(new ServerResponse<>(
                        response.code(),
                        "Error: " + (response.errorBody() != null ? response.errorBody().toString() : response.message()),
                        null
                    ));
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<ServerResponse<Object>> call, Throwable t) {
                Log.e(TAG, "Network error updating children at path: " + path, t);
                future.complete(new ServerResponse<>(500, "Network error: " + t.getMessage(), null));
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
        Log.d(TAG, "Getting children at path: " + path);
        
        String cleanedPath = cleanPath(path);
        apiService.getChildren(cleanedPath).enqueue(new retrofit2.Callback<ServerResponse<Object>>() {
            @Override
            public void onResponse(retrofit2.Call<ServerResponse<Object>> call, retrofit2.Response<ServerResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Successfully retrieved children at path: " + path);
                    future.complete(response.body());
                } else {
                    Log.e(TAG, "Error retrieving children at path: " + path + ", code: " + response.code());
                    future.complete(new ServerResponse<>(
                        response.code(),
                        "Error: " + (response.errorBody() != null ? response.errorBody().toString() : response.message()),
                        null
                    ));
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<ServerResponse<Object>> call, Throwable t) {
                Log.e(TAG, "Network error retrieving children at path: " + path, t);
                future.complete(new ServerResponse<>(500, "Network error: " + t.getMessage(), null));
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
        Log.d(TAG, "Removing value at path: " + path);
        
        String cleanedPath = cleanPath(path);
        apiService.removeValue(cleanedPath).enqueue(new retrofit2.Callback<ServerResponse<Object>>() {
            @Override
            public void onResponse(retrofit2.Call<ServerResponse<Object>> call, retrofit2.Response<ServerResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Successfully removed value at path: " + path);
                    future.complete(response.body());
                } else {
                    Log.e(TAG, "Error removing value at path: " + path + ", code: " + response.code());
                    future.complete(new ServerResponse<>(
                        response.code(),
                        "Error: " + (response.errorBody() != null ? response.errorBody().toString() : response.message()),
                        null
                    ));
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<ServerResponse<Object>> call, Throwable t) {
                Log.e(TAG, "Network error removing value at path: " + path, t);
                future.complete(new ServerResponse<>(500, "Network error: " + t.getMessage(), null));
            }
        });
        
        return future;
    }

    /**
     * Check if the server is available - do not use this method directly from the main thread
     * Use checkServerAvailability with a listener interface instead
     * @return true if the server is available, false otherwise
     */
    private static boolean isServerAvailable() {
        Log.d(TAG, "Checking server availability at " + currentServerUrl);
        
        try {
            OkHttpClient testClient = client.newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
                
            Request request = new Request.Builder()
                .url(currentServerUrl + "/actuator/health")
                .get()
                .build();
                
            Response response = testClient.newCall(request).execute();
            boolean isAvailable = response.isSuccessful();
            
            if (isAvailable) {
                Log.d(TAG, "Server is available at " + currentServerUrl);
            } else {
                Log.w(TAG, "Server returned status code: " + response.code());
            }
            
            response.close();
            return isAvailable;
        } catch (Exception e) {
            Log.e(TAG, "Error checking server availability: " + e.getMessage());
            
            // Try alternative server URLs
            for (String url : SERVER_URLS) {
                if (!url.equals(currentServerUrl)) {
                    try {
                        Log.d(TAG, "Trying alternative server URL: " + url);
                        OkHttpClient testClient = client.newBuilder()
                            .connectTimeout(3, TimeUnit.SECONDS)
                            .readTimeout(3, TimeUnit.SECONDS)
                            .build();
                            
                        Request request = new Request.Builder()
                            .url(url + "/actuator/health")
                            .get()
                            .build();
                            
                        Response response = testClient.newCall(request).execute();
                        boolean isAvailable = response.isSuccessful();
                        
                        if (isAvailable) {
                            Log.d(TAG, "Alternative server is available at " + url);
                            currentServerUrl = url;
                            response.close();
                            return true;
                        }
                        
                        response.close();
                    } catch (Exception ex) {
                        Log.e(TAG, "Error checking alternative server: " + ex.getMessage());
                    }
                }
            }
            
            return false;
        }
    }
    
    public interface ServerAvailabilityListener {
        void onResult(boolean isAvailable);
    }
    
    /**
     * Check if the server is available
     * @param listener Callback for the result
     */
    public static void checkServerAvailability(ServerAvailabilityListener listener) {
        new Thread(() -> {
            boolean available = isServerAvailable();
            
            // Return result on main thread
            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
            mainHandler.post(() -> 
                listener.onResult(available)
            );
        }).start();
    }

    // Synchronous version that runs on a separate thread and waits for the result
    private static boolean checkServerAvailabilitySync() {
        final boolean[] result = {false};
        final boolean[] done = {false};
        
        checkServerAvailability(isAvailable -> {
            result[0] = isAvailable;
            synchronized (done) {
                done[0] = true;
                done.notify();
            }
        });
        
        // Wait for the result
        synchronized (done) {
            try {
                if (!done[0]) {
                    done.wait(10000); // Wait up to 10 seconds
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted while waiting for server check", e);
            }
        }
        
        return result[0];
    }

    /**
     * Sign in with email and password through the server
     * @param auth Firebase Auth instance
     * @param email Email address
     * @param password Password
     * @return Task that completes when the sign-in is done
     */
    public static Task<AuthResult> signInWithEmailAndPassword(FirebaseAuth auth, String email, String password) {
        TaskCompletionSource<AuthResult> taskCompletionSource = new TaskCompletionSource<>();
        
        // Check if the server is available before attempting to sign in through it
        checkServerAvailability(isAvailable -> {
            if (!isAvailable) {
                Log.w(TAG, "Server is not available, authentication will be handled by AuthController");
                // Let the AuthController handle authentication
                taskCompletionSource.setException(new Exception("Server unavailable, use AuthController instead"));
                return;
            }
            
            Log.d(TAG, "Server is available at " + currentServerUrl + ", attempting authentication via server");
            // Build JSON for the request
            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("email", email);
            jsonRequest.addProperty("password", password);
            String jsonBody = jsonRequest.toString();
            
            Log.d(TAG, "Preparing authentication request with email: " + email);
            
            // Create the request
            RequestBody body = RequestBody.create(JSON, jsonBody);
            String serverEndpoint = currentServerUrl + "/" + API_PATH_AUTH;
            Request request = new Request.Builder()
                    .url(serverEndpoint)
                    .post(body)
                    .build();
            
            Log.d(TAG, "=== SENDING SERVER AUTHENTICATION REQUEST ===");
            Log.d(TAG, "Server URL: " + serverEndpoint);
            
            // Send the request with OkHttpClient customized for this request
            OkHttpClient authClient = client.newBuilder()
                .connectTimeout(20, TimeUnit.SECONDS)  // Longer connection timeout for authentication
                .readTimeout(20, TimeUnit.SECONDS)     // Longer read timeout for authentication
                .build();
                
            authClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "=== SERVER AUTHENTICATION REQUEST FAILED ===");
                    Log.e(TAG, "Request URL: " + serverEndpoint);
                    Log.e(TAG, "Error message: " + e.getMessage());
                    
                    // Authentication failed, let the AuthController handle it
                    taskCompletionSource.setException(new Exception("Server authentication failed: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    int responseCode = response.code();
                    String responseBody = response.body() != null ? response.body().string() : "null body";
                    
                    Log.d(TAG, "=== SERVER AUTHENTICATION RESPONSE RECEIVED ===");
                    Log.d(TAG, "Response code: " + responseCode);
                    
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Server authentication successful");
                        // Let the AuthController handle the actual Firebase authentication
                        taskCompletionSource.setException(new Exception("Server authentication successful, use AuthController to complete"));
                    } else {
                        Log.w(TAG, "Server returned error: " + response.code());
                        // Authentication failed, let the AuthController handle it
                        taskCompletionSource.setException(new Exception("Server authentication failed with code: " + responseCode));
                    }
                }
            });
        });
        
        return taskCompletionSource.getTask();
    }
    
    /**
     * Clean a path for use in the API
     * 
     * @param path The path to clean
     * @return The cleaned path
     */
    private static String cleanPath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        
        // Remove leading and trailing slashes
        String cleaned = path.trim();
        if (cleaned.startsWith("/")) {
            cleaned = cleaned.substring(1);
        }
        if (cleaned.endsWith("/")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        
        // Replace special characters that might cause issues in URLs
        cleaned = cleaned.replace("/", "%2F");
        cleaned = cleaned.replace(".", "%2E");
        cleaned = cleaned.replace("#", "%23");
        cleaned = cleaned.replace("$", "%24");
        cleaned = cleaned.replace("[", "%5B");
        cleaned = cleaned.replace("]", "%5D");
        
        return cleaned;
    }
} 