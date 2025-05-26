package com.example.partymaker;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

public class OpenAiApi {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final String apiKey;
    private final OkHttpClient client = new OkHttpClient();

    public OpenAiApi(String apiKey) {
        this.apiKey = apiKey;
    }

    public String sendMessage(String userMessage) throws Exception {
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", userMessage);

        JSONArray messages = new JSONArray();
        messages.put(message);

        JSONObject body = new JSONObject();
        body.put("model", "gpt-3.5-turbo");
        body.put("messages", messages);

        RequestBody requestBody = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            body.toString()
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);
            return json.getJSONArray("choices")
                       .getJSONObject(0)
                       .getJSONObject("message")
                       .getString("content");
        }
    }

    public String sendMessageWithHistory(List<SimpleChatMessage> history) throws Exception {
        JSONArray messages = new JSONArray();
        for (SimpleChatMessage msg : history) {
            JSONObject message = new JSONObject();
            message.put("role", msg.role);
            message.put("content", msg.content);
            messages.put(message);
        }
        JSONObject body = new JSONObject();
        body.put("model", "gpt-3.5-turbo");
        body.put("messages", messages);

        RequestBody requestBody = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            body.toString()
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);
            return json.getJSONArray("choices")
                       .getJSONObject(0)
                       .getJSONObject("message")
                       .getString("content");
        }
    }
} 