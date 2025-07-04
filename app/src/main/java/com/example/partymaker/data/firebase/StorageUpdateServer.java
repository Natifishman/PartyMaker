package com.example.partymaker.data.firebase;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Class for performing storage operations through the server.
 * All storage operations are routed through the Spring Boot server.
 */
public class StorageUpdateServer {
    private static final String TAG = "StorageUpdateServer";
    
    // Use the same server URLs as in DatabaseUpdateServer
    private static final String[] SERVER_URLS = {
        "http://10.0.2.2:8085",  // Server address in emulator
        "http://localhost:8085", // Local address
        "http://127.0.0.1:8085"  // Alternative local address
    };
    private static String currentServerUrl = SERVER_URLS[0]; // Start with the first address

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    // Configure OkHttpClient with improved network error handling
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)  // Longer connection timeout for file uploads
            .readTimeout(30, TimeUnit.SECONDS)     // Longer read timeout for file uploads
            .writeTimeout(30, TimeUnit.SECONDS)    // Longer write timeout for file uploads
            .retryOnConnectionFailure(true)        // Retry on connection failure
            .connectionPool(new ConnectionPool(5, 30, TimeUnit.SECONDS)) // Improved connection management
            .build();
            
    private static final Gson gson = new Gson();

    /**
     * Upload a file to storage through the server
     * @param reference Storage reference
     * @param uri URI of the file to upload
     * @return Task that completes when the upload is done
     */
    public static Task<Uri> putFile(StorageReference reference, Uri uri) {
        TaskCompletionSource<Uri> taskCompletionSource = new TaskCompletionSource<>();
        
        try {
            // Check if the server is available
            DatabaseUpdateServer.checkServerAvailability(isAvailable -> {
                if (!isAvailable) {
                    Log.w(TAG, "Server is not available for file upload");
                    taskCompletionSource.setException(new Exception("Server is not available for file upload"));
                    return;
                }
                
                Log.d(TAG, "Server is available, attempting to upload file via server");
                // TODO: Implement server-side file upload when the server supports it
                // For now, we'll use a placeholder implementation that returns a failure
                // This should be replaced with actual server communication code
                
                String path = getPathFromReference(reference);
                Log.d(TAG, "Uploading file to path: " + path);
                
                // Create a JSON request for the server to handle the upload
                JsonObject jsonRequest = new JsonObject();
                jsonRequest.addProperty("path", path);
                jsonRequest.addProperty("uri", uri.toString());
                String jsonBody = jsonRequest.toString();
                
                // Create the request
                RequestBody body = RequestBody.create(jsonBody, JSON);
                String serverEndpoint = currentServerUrl + "/api/storage/upload";
                Request request = new Request.Builder()
                        .url(serverEndpoint)
                        .post(body)
                        .build();
                
                // Send the request
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Server file upload request failed", e);
                        taskCompletionSource.setException(new Exception("Server file upload failed: " + e.getMessage()));
                    }
                    
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful() && response.body() != null) {
                            String responseBody = response.body().string();
                            Log.d(TAG, "Server file upload successful, response: " + responseBody);
                            
                            try {
                                // Parse the response to get the download URL
                                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                                String downloadUrl = jsonResponse.get("downloadUrl").getAsString();
                                taskCompletionSource.setResult(Uri.parse(downloadUrl));
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing server response", e);
                                taskCompletionSource.setException(new Exception("Error parsing server response: " + e.getMessage()));
                            }
                        } else {
                            Log.e(TAG, "Server returned error for file upload: " + response.code());
                            taskCompletionSource.setException(new Exception("Server returned error for file upload: " + response.code()));
                        }
                    }
                });
            });
        } catch (Exception e) {
            Log.e(TAG, "Error uploading file", e);
            taskCompletionSource.setException(e);
        }
        
        return taskCompletionSource.getTask();
    }
    
    /**
     * Get a download URL from storage through the server
     * @param reference Storage reference
     * @return Task that completes when the URL is received
     */
    public static Task<Uri> getDownloadUrl(StorageReference reference) {
        TaskCompletionSource<Uri> taskCompletionSource = new TaskCompletionSource<>();
        
        try {
            // Check if the server is available
            DatabaseUpdateServer.checkServerAvailability(isAvailable -> {
                if (!isAvailable) {
                    Log.w(TAG, "Server is not available for getting download URL");
                    taskCompletionSource.setException(new Exception("Server is not available for getting download URL"));
                    return;
                }
                
                Log.d(TAG, "Server is available, attempting to get download URL via server");
                // TODO: Implement server-side download URL retrieval when the server supports it
                // For now, we'll use a placeholder implementation that returns a failure
                // This should be replaced with actual server communication code
                
                String path = getPathFromReference(reference);
                Log.d(TAG, "Getting download URL for path: " + path);
                
                // Create a request to get the download URL
                String serverEndpoint = currentServerUrl + "/api/storage/url?path=" + path;
                Request request = new Request.Builder()
                        .url(serverEndpoint)
                        .get()
                        .build();
                
                // Send the request
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Server download URL request failed", e);
                        taskCompletionSource.setException(new Exception("Server download URL request failed: " + e.getMessage()));
                    }
                    
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful() && response.body() != null) {
                            String responseBody = response.body().string();
                            Log.d(TAG, "Server download URL request successful, response: " + responseBody);
                            
                            try {
                                // Parse the response to get the download URL
                                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                                String downloadUrl = jsonResponse.get("url").getAsString();
                                taskCompletionSource.setResult(Uri.parse(downloadUrl));
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing server response", e);
                                taskCompletionSource.setException(new Exception("Error parsing server response: " + e.getMessage()));
                            }
                        } else {
                            Log.e(TAG, "Server returned error for download URL request: " + response.code());
                            taskCompletionSource.setException(new Exception("Server returned error for download URL request: " + response.code()));
                        }
                    }
                });
            });
        } catch (Exception e) {
            Log.e(TAG, "Error getting download URL", e);
            taskCompletionSource.setException(e);
        }
        
        return taskCompletionSource.getTask();
    }
    
    /**
     * Check if a file exists in storage
     * @param reference Storage reference
     * @param listener Callback for the result
     */
    public static void checkFileExists(StorageReference reference, DBRef.OnImageExistsListener listener) {
        try {
            // Check if the server is available
            DatabaseUpdateServer.checkServerAvailability(isAvailable -> {
                if (!isAvailable) {
                    Log.w(TAG, "Server is not available for checking if file exists");
                    listener.onImageExists(false);
                    return;
                }
                
                Log.d(TAG, "Server is available, attempting to check if file exists via server");
                // TODO: Implement server-side file existence check when the server supports it
                // For now, we'll use a placeholder implementation that returns false
                // This should be replaced with actual server communication code
                
                String path = getPathFromReference(reference);
                Log.d(TAG, "Checking if file exists at path: " + path);
                
                // Create a request to check if the file exists
                String serverEndpoint = currentServerUrl + "/api/storage/exists?path=" + path;
                Request request = new Request.Builder()
                        .url(serverEndpoint)
                        .get()
                        .build();
                
                // Send the request
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Server file exists check failed", e);
                        listener.onImageExists(false);
                    }
                    
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful() && response.body() != null) {
                            String responseBody = response.body().string();
                            Log.d(TAG, "Server file exists check successful, response: " + responseBody);
                            
                            try {
                                // Parse the response to get the result
                                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                                boolean exists = jsonResponse.get("exists").getAsBoolean();
                                listener.onImageExists(exists);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing server response", e);
                                listener.onImageExists(false);
                            }
                        } else {
                            Log.e(TAG, "Server returned error for file exists check: " + response.code());
                            listener.onImageExists(false);
                        }
                    }
                });
            });
        } catch (Exception e) {
            Log.e(TAG, "Error checking if file exists", e);
            listener.onImageExists(false);
        }
    }
    
    /**
     * Convert a storage reference to a path string
     * @param reference Storage reference
     * @return Path string
     */
    private static String getPathFromReference(StorageReference reference) {
        String path = reference.getPath();
        
        // Detailed logging for debugging
        Log.d(TAG, "Converting storage reference to path: " + reference.toString());
        Log.d(TAG, "Storage path: " + path);
        
        // Remove the / from the beginning of the path if it exists
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        
        Log.d(TAG, "Final storage path: " + path);
        return path;
    }
} 