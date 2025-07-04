package com.example.partymaker.ui.chatbot;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.LocaleList;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.partymaker.R;
import com.example.partymaker.data.api.OpenAiApi;
import com.example.partymaker.data.model.ChatMessageGpt;
import com.example.partymaker.ui.adapters.ChatbotAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GptChatActivity extends AppCompatActivity {

  // ---------- Views ----------
  private RecyclerView chatRecyclerView;
  private EditText messageInput;

  // ---------- Lists ----------
  // Only what is displayed to the user
  private final List<ChatMessageGpt> visibleMessages = new ArrayList<>(); // NEW
  // All history sent to API (including system)
  private final List<ChatMessageGpt> history = new ArrayList<>(); // NEW

  private ChatbotAdapter chatAdapter;
  private OpenAiApi openAiApi;
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  // ---------- System prompt ----------
  private static final ChatMessageGpt SYSTEM_PROMPT = // NEW
      new ChatMessageGpt(
          "system", "Always answer in English, even if the question is in another language.");

  // ------------------------------------------------------------------------
  // onCreate
  // ------------------------------------------------------------------------
  @SuppressLint("NotifyDataSetChanged")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chatbot);

    // ---------- Init OpenAI helper ----------
    String apiKey = getApiKey();
    openAiApi = new OpenAiApi(apiKey);

    // ---------- Hide action bar (only this activity) ----------
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) actionBar.hide();

    // ---------- Init views ----------
    chatRecyclerView = findViewById(R.id.chatRecyclerView);
    messageInput = findViewById(R.id.messageInput);
    ImageButton sendButton = findViewById(R.id.sendButton);
    MaterialToolbar toolbar = findViewById(R.id.chatToolbar);

    // Toolbar back button
    toolbar.setNavigationOnClickListener(v -> finish());

    // ---------- RecyclerView ----------
    chatAdapter = new ChatbotAdapter(visibleMessages); // CHANGED
    chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    chatRecyclerView.setAdapter(chatAdapter);

    // ---------- Add system prompt (not displayed) ----------
    history.add(SYSTEM_PROMPT); // NEW

    // ---------- Assistant welcome (displayed) ----------
    ChatMessageGpt welcome =
        new ChatMessageGpt(
            "assistant",
            "🎉 Welcome to PartyMaker app help – the perfect app for planning parties!\n\n"
                + "I'm here to help you with any question or issue. Ask me how to add friends, create a group, manage an event, or anything else – and I'll explain it to you step-by-step in English.\n\n"
                + "How can I help?");
    history.add(welcome);
    visibleMessages.add(welcome); // NEW
    chatAdapter.notifyDataSetChanged();

    // ---------- Keyboard hint to English ----------
    messageInput.setImeHintLocales(new LocaleList(new Locale("en")));

    // ---------- Send button ----------
    sendButton.setOnClickListener(
        v -> {
          String userText = messageInput.getText().toString().trim();
          if (!userText.isEmpty()) {
            sendMessage(userText);
            messageInput.setText("");
          }
        });
  }

  // ------------------------------------------------------------------------
  // Get API key from assets/local.properties
  // ------------------------------------------------------------------------
  private String getApiKey() {
    try (InputStream inputStream = getAssets().open("local.properties")) {
      Properties properties = new Properties();
      properties.load(inputStream);
      return properties.getProperty("OPENAI_API_KEY", "");
    } catch (IOException e) {
      Toast.makeText(this, "API key error", Toast.LENGTH_SHORT).show();
      return "";
    }
  }

  // ------------------------------------------------------------------------
  // Send user message
  // ------------------------------------------------------------------------
  @SuppressLint("NotifyDataSetChanged")
  private void sendMessage(String userText) {
    // ---------- User message ----------
    ChatMessageGpt userMsg = new ChatMessageGpt("user", userText);
    visibleMessages.add(userMsg); // display
    history.add(userMsg); // for history
    chatAdapter.notifyDataSetChanged();
    chatRecyclerView.scrollToPosition(visibleMessages.size() - 1);

    // ---------- Call OpenAI ----------
    executor.execute(
        () -> {
          try {
            String answer = openAiApi.sendMessageWithHistory(history);

            ChatMessageGpt assistantMsg = new ChatMessageGpt("assistant", answer);

            runOnUiThread(
                () -> {
                  history.add(assistantMsg); // for continued context
                  visibleMessages.add(assistantMsg); // for display
                  chatAdapter.notifyDataSetChanged();
                  chatRecyclerView.scrollToPosition(visibleMessages.size() - 1);
                });

          } catch (Exception e) {
            runOnUiThread(
                () -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
          }
        });
  }
}
