package com.example.partymaker.ui.group;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.partymaker.R;
import com.example.partymaker.data.api.OpenAiApi;
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.ChatMessage;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.ui.adapters.ChatAdapter;
import com.example.partymaker.utilities.Common;
import com.example.partymaker.utilities.ExtrasMetadata;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

  private ListView lv4;
  private EditText etMessage;
  private ImageButton btnSend;
  private ImageButton btnGpt;
  private String GroupKey;
  private HashMap<String, Object> MessageKeys;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_party_chat);

    // Actionbar settings
    ActionBar actionBar = getSupportActionBar();
    assert actionBar != null;
    actionBar.setTitle("Chat");
    actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0081d1")));

    // connection between values from intent
    ExtrasMetadata extras = Common.getExtrasMetadataFromIntent(getIntent());
    if (extras == null) {
      Toast.makeText(this, "Missing intent data", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }
    MessageKeys = extras.getMessageKeys();
    GroupKey = extras.getGroupKey();

    // connection
    lv4 = findViewById(R.id.lv4);
    etMessage = findViewById(R.id.etMessage);
    btnSend = findViewById(R.id.btnSend);
    btnGpt = findViewById(R.id.btnGpt);

    ShowData();
    eventHandler();
    setupGptButton();
  }

  private void eventHandler() {
    lv4.setOnItemClickListener((parent, view, position, id) -> {});
    lv4.setOnItemLongClickListener((parent, view, position, id) -> false);
    btnSend.setOnClickListener(
        v -> {
          ChatMessage msg = new ChatMessage();
          String Text = etMessage.getText().toString();
          String User = Objects.requireNonNull(DBRef.Auth.getCurrentUser()).getEmail();
          Calendar c = Calendar.getInstance();
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          String strDate = sdf.format(c.getTime());
          String MessageKey = DBRef.refMessages.push().getKey();
          msg.setMessageUser(User);
          msg.setMessageTime(strDate);
          msg.setMessageText(Text);
          msg.setMessageKey(MessageKey);
          DBRef.refMessages.child(Objects.requireNonNull(MessageKey)).setValue(msg);
          MessageKeys.put(MessageKey, "true");
          DBRef.refGroups.child(GroupKey).child("MessageKeys").updateChildren(MessageKeys);
          // GPT detection
          if (Text.trim().toLowerCase().startsWith("@gpt")
              || Text.trim().toLowerCase().startsWith("gpt,")
              || Text.trim().toLowerCase().startsWith("gpt:")) {
            new Thread(
                    () -> {
                      try {
                        String prompt =
                            "You are a party assistant. Your role is to provide details and help with whatever you can for this party. Here are the party details: "
                                + getGroupDetails();
                        OpenAiApi openAiApi = new OpenAiApi(getApiKey());
                        String gptAnswer = openAiApi.sendMessage(prompt + Text);
                        runOnUiThread(() -> sendBotMessage(gptAnswer));
                      } catch (Exception e) {
                        e.printStackTrace();
                      }
                    })
                .start();
          }
          etMessage.setText(null);
        });
  }

  private void setupGptButton() {
    btnGpt.setOnClickListener(
        v -> {
          android.app.AlertDialog.Builder builder =
              new android.app.AlertDialog.Builder(ChatActivity.this);
          builder.setTitle("Ask GPT");
          final EditText input = new EditText(ChatActivity.this);
          input.setHint("Write your question here...");
          builder.setView(input);
          builder.setPositiveButton(
              "Send",
              (dialog, which) -> {
                String gptQuestion = input.getText().toString();
                if (!gptQuestion.isEmpty()) {
                  new Thread(
                          () -> {
                            try {
                              String prompt =
                                  "You are a party assistant. Your role is to provide details and help with whatever you can for this party. Here are the party details: "
                                      + getGroupDetails();
                              OpenAiApi openAiApi = new OpenAiApi(getApiKey());
                              String gptAnswer = openAiApi.sendMessage(prompt + gptQuestion);
                              runOnUiThread(() -> sendBotMessage(gptAnswer));
                            } catch (Exception e) {
                              e.printStackTrace();
                            }
                          })
                      .start();
                }
              });
          builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
          builder.show();
        });
  }

  private void sendBotMessage(String answer) {
    ChatMessage botMsg = new ChatMessage();
    botMsg.setMessageUser("PartyBot");
    Calendar c = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String strDate = sdf.format(c.getTime());
    String MessageKey = DBRef.refMessages.push().getKey();
    botMsg.setMessageTime(strDate);
    botMsg.setMessageText(answer);
    botMsg.setMessageKey(MessageKey);
    DBRef.refMessages.child(Objects.requireNonNull(MessageKey)).setValue(botMsg);
    MessageKeys.put(MessageKey, "true");
    DBRef.refGroups.child(GroupKey).child("MessageKeys").updateChildren(MessageKeys);
  }

  private String getApiKey() {
    try {
      java.util.Properties properties = new java.util.Properties();
      java.io.InputStream inputStream = getAssets().open("local.properties");
      properties.load(inputStream);
      return properties.getProperty("OPENAI_API_KEY");
    } catch (java.io.IOException e) {
      return "";
    }
  }

  private void ShowData() {
    DBRef.refMessages.addValueEventListener(
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            ArrayList<ChatMessage> ArrMessages = new ArrayList<>();
            for (DataSnapshot data : dataSnapshot.getChildren()) {
              ChatMessage GroupMessage = data.getValue(ChatMessage.class);
              String GroupMessageKey =
                  Objects.requireNonNull(data.getValue(ChatMessage.class)).getMessageKey();
              for (String MessageKey : MessageKeys.keySet()) {
                if (MessageKey.equals(GroupMessageKey)) ArrMessages.add(GroupMessage);
              }
            }
            ChatAdapter adapt = new ChatAdapter(ChatActivity.this, 0, 0, ArrMessages);
            lv4.setAdapter(adapt);
          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
  }

  public String getGroupDetails() {
    StringBuilder details = new StringBuilder();
    details.append("Party Details:\n");
    details.append("Party Name: ").append(GroupKey).append("\n");

    DBRef.refGroups
        .child(GroupKey)
        .addListenerForSingleValueEvent(
            new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                  Group group = dataSnapshot.getValue(Group.class);
                  if (group != null) {
                    details.append("Admin: ").append(group.getAdminKey()).append("\n");
                    details
                        .append("Date: ")
                        .append(group.getGroupDays())
                        .append("/")
                        .append(group.getGroupMonths())
                        .append("/")
                        .append(group.getGroupYears())
                        .append("\n");
                    details.append("Time: ").append(group.getGroupHours()).append("\n");
                    details.append("Price: ").append(group.getGroupPrice()).append(" ₪\n");
                    if (group.getGroupLocation() != null) {
                      details.append("Location: ").append(group.getGroupLocation()).append("\n");
                    }
                    details
                        .append("Group Type: ")
                        .append(group.getGroupType() == 0 ? "Public" : "Private");
                  }
                }
              }

              @Override
              public void onCancelled(@NonNull DatabaseError databaseError) {
                details.append("Error loading group details");
              }
            });

    return details.toString();
  }
}
