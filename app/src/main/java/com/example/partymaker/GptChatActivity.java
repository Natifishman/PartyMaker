package com.example.partymaker;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Locale;

import com.google.android.material.appbar.MaterialToolbar;

public class GptChatActivity extends AppCompatActivity {
    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private List<SimpleChatMessage> messages;
    private ChatAdapter2 chatAdapter;
    private OpenAiApi openAiApi;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_dialog);

        // Initialize OpenAI API helper
        String apiKey = getApiKey();
        openAiApi = new OpenAiApi(apiKey);

        // Initialize views
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        MaterialToolbar toolbar = findViewById(R.id.chatToolbar);

        // Toolbar back button
        toolbar.setNavigationOnClickListener(v -> finish());

        // Setup RecyclerView
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter2(messages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Add system prompt for Hebrew and app info
        messages.add(new SimpleChatMessage("system", "אתה עוזר חכם באפליקציה לתכנון מסיבות בשם PartyMaker. תסביר ותדריך את המשתמשים על כל מסך, כפתור ואפשרות באפליקציה, תענה תמיד בעברית, ותהיה סבלני ומפורט. אם שואלים על תכנון מסיבה, הוספת חברים, ניהול קבוצות, או כל פעולה באפליקציה - תסביר שלב אחרי שלב בעברית פשוטה."));
        messages.add(new SimpleChatMessage("assistant", "🎉 ברוכים הבאים לעזרה באפליקציית PartyMaker – האפליקציה המושלמת לתכנון מסיבות!\n\nאני כאן כדי לעזור לך בכל שאלה או בעיה. שאל/י אותי איך מוסיפים חברים, יוצרים קבוצה, מנהלים אירוע, או כל דבר אחר – ואסביר לך שלב-אחר-שלב בעברית.\n\nאיך אפשר לעזור?"));
        chatAdapter.notifyDataSetChanged();

        // Set keyboard to Hebrew if possible (API 24+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            messageInput.setImeHintLocales(new android.os.LocaleList(new Locale("he")));
        }

        // Setup send button click listener
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userMessage = messageInput.getText().toString().trim();
                if (!userMessage.isEmpty()) {
                    sendMessage(userMessage);
                    messageInput.setText("");
                }
            }
        });
    }

    private String getApiKey() {
        try {
            Properties properties = new Properties();
            InputStream inputStream = getAssets().open("local.properties");
            properties.load(inputStream);
            return properties.getProperty("OPENAI_API_KEY");
        } catch (IOException e) {
            System.out.println("error");
            return "";
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void sendMessage(String userMessage) {
        // Add user message to the chat
        messages.add(new SimpleChatMessage("user", userMessage));
        chatAdapter.notifyDataSetChanged();
        chatRecyclerView.scrollToPosition(messages.size() - 1);

        // Send request to OpenAI in background, always include system prompt
        executor.execute(() -> {
            try {
                // Always send the system prompt and all messages
                List<SimpleChatMessage> allMessages = new ArrayList<>();
                allMessages.add(new SimpleChatMessage("system", "ענה תמיד בעברית, גם אם השאלה באנגלית."));
                for (SimpleChatMessage m : messages) {
                    if (!"system".equals(m.role)) allMessages.add(m);
                }
                String assistantMessage = openAiApi.sendMessageWithHistory(allMessages);
                runOnUiThread(() -> {
                    messages.add(new SimpleChatMessage("assistant", assistantMessage));
                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.scrollToPosition(messages.size() - 1);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
} 