package com.example.partymaker.data.api;

import com.example.partymaker.data.model.ServerResponse;
import com.example.partymaker.data.model.SetValueRequest;
import com.example.partymaker.data.model.UpdateChildrenRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Retrofit interface for API calls to the server.
 */
public interface ApiService {
    String API_PATH_DATABASE = "api/database";
    String API_PATH_GET_VALUE = API_PATH_DATABASE + "/getValue";
    String API_PATH_GET_CHILDREN = API_PATH_DATABASE + "/getChildren";
    String API_PATH_SET_VALUE = API_PATH_DATABASE + "/setValue";
    String API_PATH_UPDATE_CHILDREN = API_PATH_DATABASE + "/updateChildren";
    String API_PATH_REMOVE = API_PATH_DATABASE + "/remove";
    String API_PATH_AUTH = "api/auth/signin";
    
    @GET(API_PATH_GET_VALUE + "/{path}")
    Call<ServerResponse<Object>> getValue(@Path("path") String path);
    
    @GET(API_PATH_GET_CHILDREN + "/{path}")
    Call<ServerResponse<Object>> getChildren(@Path("path") String path);
    
    @POST(API_PATH_SET_VALUE)
    Call<ServerResponse<Object>> setValue(@Body SetValueRequest request);
    
    @POST(API_PATH_UPDATE_CHILDREN)
    Call<ServerResponse<Object>> updateChildren(@Body UpdateChildrenRequest request);
    
    @DELETE(API_PATH_REMOVE + "/{path}")
    Call<ServerResponse<Object>> removeValue(@Path("path") String path);
} 