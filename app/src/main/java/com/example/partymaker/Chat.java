package com.example.partymaker;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.example.partymaker.data.ChatAdpter;
import com.example.partymaker.data.ChatMessage;
import com.example.partymaker.data.DBref;

import java.util.ArrayList;
import java.util.HashMap;

public class Chat extends AppCompatActivity {

    private ListView lv4;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageButton btnGpt;
    private String GroupKey;
    private HashMap<String, Object> MessageKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //this 2 lines changes title's name
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Chat");
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0081d1")));

        //connection between values from intent
        HashMap<String, Object> hashMessageKeys = (HashMap<String, Object>) getIntent().getSerializableExtra("MessageKeys");
        MessageKeys = hashMessageKeys;
        String strGroupKey = getIntent().getExtras().getString("groupKey", "defaultKey");
        GroupKey = strGroupKey;

        //connection
        lv4 = (ListView) findViewById(R.id.lv4);
        etMessage = (EditText) findViewById(R.id.etMessage);
        btnSend = (ImageButton) findViewById(R.id.btnSend);
        btnGpt = (ImageButton) findViewById(R.id.btnGpt);

        ShowData();
        eventHandler();
        setupGptButton();
    }

    private void eventHandler() {
        lv4.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        lv4.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                ChatMessage msg = new ChatMessage();
                String Text = etMessage.getText().toString();
                String User = DBref.Auth.getCurrentUser().getEmail();
                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String strDate = sdf.format(c.getTime());
                String MessageKey = DBref.refMessages.push().getKey();
                msg.setMessageUser(User);
                msg.setMessageTime(strDate);
                msg.setMessageText(Text);
                msg.setMessageKey(MessageKey);
                DBref.refMessages.child(MessageKey).setValue(msg);
                MessageKeys.put(MessageKey, "true");
                DBref.refGroups.child(GroupKey).child("MessageKeys").updateChildren(MessageKeys);
                // GPT detection
                if (Text.trim().toLowerCase().startsWith("@gpt") || Text.trim().toLowerCase().startsWith("gpt,") || Text.trim().toLowerCase().startsWith("gpt:")) {
                    new Thread(() -> {
                        try {
                            OpenAiApi openAiApi = new OpenAiApi(getApiKey());
                            String gptAnswer = openAiApi.sendMessage(Text);
                            runOnUiThread(() -> sendBotMessage(gptAnswer));
                        } catch (Exception e) { e.printStackTrace(); }
                    }).start();
                }
                etMessage.setText(null);
            }
        });
    }

    private void setupGptButton() {
        btnGpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(Chat.this);
                builder.setTitle("שאל את GPT");
                final EditText input = new EditText(Chat.this);
                input.setHint("כתוב כאן את השאלה שלך...");
                builder.setView(input);
                builder.setPositiveButton("שלח", (dialog, which) -> {
                    String gptQuestion = input.getText().toString();
                    if (!gptQuestion.isEmpty()) {
                        new Thread(() -> {
                            try {
                                OpenAiApi openAiApi = new OpenAiApi(getApiKey());
                                String gptAnswer = openAiApi.sendMessage(gptQuestion);
                                runOnUiThread(() -> sendBotMessage(gptAnswer));
                            } catch (Exception e) { e.printStackTrace(); }
                        }).start();
                    }
                });
                builder.setNegativeButton("ביטול", (dialog, which) -> dialog.cancel());
                builder.show();
            }
        });
    }

    private void sendBotMessage(String answer) {
        ChatMessage botMsg = new ChatMessage();
        botMsg.setMessageUser("PartyBot");
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = sdf.format(c.getTime());
        String MessageKey = DBref.refMessages.push().getKey();
        botMsg.setMessageTime(strDate);
        botMsg.setMessageText(answer);
        botMsg.setMessageKey(MessageKey);
        DBref.refMessages.child(MessageKey).setValue(botMsg);
        MessageKeys.put(MessageKey, "true");
        DBref.refGroups.child(GroupKey).child("MessageKeys").updateChildren(MessageKeys);
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
        DBref.refMessages.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<ChatMessage> ArrMessages = new ArrayList<ChatMessage>();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    ChatMessage GroupMessage = data.getValue(ChatMessage.class);
                    String GroupMessageKey = data.getValue(ChatMessage.class).getMessageKey();
                    for (String MessageKey : MessageKeys.keySet()) {
                        if (MessageKey.equals(GroupMessageKey))
                            ArrMessages.add(GroupMessage);
                    }
                }
                ChatAdpter adpt = new ChatAdpter(Chat.this, 0, 0, ArrMessages);
                lv4.setAdapter(adpt);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
