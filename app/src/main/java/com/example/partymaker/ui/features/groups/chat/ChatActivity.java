package com.example.partymaker.ui.features.groups.chat;

import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import androidx.annotation.NonNull;
import android.widget.Toast;
import com.example.partymaker.R;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.api.FirebaseServerClient.OperationCallback;
import com.example.partymaker.data.api.OpenAiApi;
import com.example.partymaker.data.model.ChatMessage;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.ui.adapters.ChatAdapter;
import com.example.partymaker.ui.base.BaseActivity;
import com.example.partymaker.utils.auth.AuthenticationManager;
import com.example.partymaker.utils.core.IntentExtrasManager;
import com.example.partymaker.utils.core.ExtrasMetadata;
import com.example.partymaker.utils.security.encryption.GroupKeyManager;
import com.example.partymaker.utils.security.encryption.GroupMessageEncryption;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import com.example.partymaker.viewmodel.groups.GroupChatViewModel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends BaseActivity<GroupChatViewModel> {

  private static final int MAX_RETRY_ATTEMPTS = 3;
  private final Handler retryHandler = new Handler(Looper.getMainLooper());
  private ListView lv4;
  private EditText etMessage;
  private ImageButton btnSend;
  private ImageButton btnGpt;
  private String GroupKey;
  private HashMap<String, Object> MessageKeys;
  private FirebaseServerClient serverClient;
  private ChatAdapter adapter;
  private String UserKey;
  private GroupKeyManager groupKeyManager;
  private GroupMessageEncryption groupEncryption;

  // BaseActivity implementation methods
  
  @Override
  protected int getLayoutId() {
    return R.layout.activity_party_chat;
  }

  @Override
  protected Class<GroupChatViewModel> getViewModelClass() {
    return GroupChatViewModel.class;
  }

  @Override
  protected String getActivityTitle() {
    return "Chat";
  }

  @Override
  protected void initViews() {
    // Initialize server client
    serverClient = FirebaseServerClient.getInstance();
    
    // Get data from intent and validate
    extractIntentData();
    
    // Initialize UI components
    lv4 = findViewById(R.id.lv4);
    etMessage = findViewById(R.id.etMessage);
    btnSend = findViewById(R.id.btnSend);
    btnGpt = findViewById(R.id.btnGpt);
  }

  @Override
  protected void setupObservers() {
    setupViewModelObservers();
  }

  @Override
  protected void setupClickListeners() {
    setupEventHandlers();
    setupGptButton();
  }

  @Override
  protected void onActivityStart() {
    super.onActivityStart();
    
    // Set the group key in ViewModel
    if (viewModel != null && GroupKey != null) {
      viewModel.setGroupKey(GroupKey);
    }
    
    // Initialize encryption for this group
    initializeGroupEncryption();
    
    // Load data
    ShowData();
  }

  /**
   * Extract and validate data from intent
   */
  private void extractIntentData() {
    Intent intent = getIntent();

    // Get GroupKey directly from intent first
    GroupKey = intent.getStringExtra("GroupKey");

    // Try to get data from ExtrasMetadata only if direct intent extra is null
    if (GroupKey == null || GroupKey.isEmpty()) {
      ExtrasMetadata extras = IntentExtrasManager.getExtrasMetadataFromIntent(intent);
      if (extras != null) {
        MessageKeys = extras.getMessageKeys();
        GroupKey = extras.getGroupKey();
      } else {
        MessageKeys = new HashMap<>();
      }
    } else {
      MessageKeys = new HashMap<>();
    }

    // Check if we have the required data
    if (GroupKey == null || GroupKey.isEmpty()) {
      showError("Missing group data");
      finish();
      return;
    }

    // Get UserKey from AuthHelper
    try {
      UserKey = AuthenticationManager.getCurrentUserKey(this);
    } catch (Exception e) {
      UserKey = intent.getStringExtra("UserKey"); // Fallback to intent if auth fails

      if (UserKey == null) {
        showError("Missing user information");
        finish();
        return;
      }
    }
  }

  /** Initialize group encryption for secure messaging */
  private void initializeGroupEncryption() {
    if (UserKey == null || GroupKey == null) {
      Log.w(TAG, "Cannot initialize encryption: missing UserKey or GroupKey");
      return;
    }
    
    try {
      // Initialize encryption managers
      groupKeyManager = new GroupKeyManager(this, UserKey);
      groupEncryption = groupKeyManager.getEncryptionManager();
      
      // Check if user is already a member of this group's encryption
      groupKeyManager.isGroupMember(GroupKey).thenAccept(isMember -> {
        if (!isMember) {
          Log.i(TAG, "User not in group encryption, adding automatically");
          // Auto-add current user to group encryption
          groupKeyManager.addUserToGroupEncryption(GroupKey, UserKey).thenAccept(success -> {
            if (success) {
              Log.i(TAG, "Successfully added user to group encryption");
              // Now initialize for existing group
              groupKeyManager.initializeForExistingGroup(GroupKey);
            } else {
              Log.e(TAG, "Failed to add user to group encryption");
              Toast.makeText(this, "Warning: Could not setup message encryption", Toast.LENGTH_SHORT).show();
            }
          });
        } else {
          Log.i(TAG, "User already in group encryption, initializing");
          // Initialize encryption for existing group
          groupKeyManager.initializeForExistingGroup(GroupKey);
        }
      });
      
      Log.i(TAG, "Group encryption initialization started for: " + GroupKey);
      
    } catch (Exception e) {
      Log.e(TAG, "Failed to initialize group encryption", e);
      Toast.makeText(this, "Warning: Message encryption not available", Toast.LENGTH_SHORT).show();
    }
  }

  /** Sets up observers for ViewModel LiveData */
  private void setupViewModelObservers() {
    viewModel
        .getMessages()
        .observe(
            this,
            messages -> {
              if (messages != null && adapter != null) {
                // Refresh the adapter with new messages
                ShowData();
              }
            });

    viewModel
        .getIsMessageSent()
        .observe(
            this,
            isSent -> {
              if (isSent) {
                etMessage.setText("");
                viewModel.resetMessageSentFlag();
              }
            });

    viewModel
        .getIsLoading()
        .observe(
            this,
            isLoading -> {
              btnSend.setEnabled(!isLoading);
            });
  }

  private void setupEventHandlers() {
    Log.d(TAG, "Setting up event handlers");
    lv4.setOnItemClickListener((parent, view, position, id) -> {});
    lv4.setOnItemLongClickListener((parent, view, position, id) -> false);

    Log.d(TAG, "Setting up btnSend click listener");
    btnSend.setOnClickListener(
        v -> {
          Log.d(TAG, "Send button clicked");
          String messageText = etMessage.getText().toString().trim();
          Log.d(TAG, "Message text: '" + messageText + "'");

          if (!messageText.isEmpty()) {
            Log.d(TAG, "Message is not empty, sending message");
            sendMessage(messageText);
            etMessage.setText("");
          } else {
            Log.d(TAG, "Message is empty, not sending");
            Toast.makeText(this, "Cannot send empty message", Toast.LENGTH_SHORT).show();
          }
        });

    Log.d(TAG, "Event handlers setup completed");
  }

  private String generateUniqueKey() {
    // Generate a timestamp-based unique key
    return "msg_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 10000);
  }

  private void saveMessageToServer(String messageKey, ChatMessage message) {
    serverClient.saveMessage(
        GroupKey,
        messageKey,
        message,
        new OperationCallback() {
          @Override
          public void onSuccess() {
            Log.d(TAG, "Message saved successfully");
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Failed to save message: " + errorMessage);
            Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
          }
        });
  }

  private void updateGroupMessageKeys() {
    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();
    Map<String, Object> updates = new HashMap<>();
    updates.put("MessageKeys", MessageKeys);
    serverClient.updateGroup(
        GroupKey,
        updates,
        new FirebaseServerClient.OperationCallback() {
          @Override
          public void onSuccess() {
            Log.d(TAG, "Group message keys updated successfully");
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Failed to update group message keys: " + errorMessage);
          }
        });
  }

  private void setupGptButton() {
    btnGpt.setOnClickListener(
        v -> {
          android.app.AlertDialog.Builder builder =
              new android.app.AlertDialog.Builder(ChatActivity.this);
          builder.setTitle("Ask GPT");
          final EditText input = new EditText(ChatActivity.this);
          input.setHint("Type your question here...");
          builder.setView(input);
          builder.setPositiveButton(
              "Send",
              (dialog, which) -> {
                String gptQuestion = input.getText().toString();
                if (!gptQuestion.isEmpty()) {
                  // Show loading message
                  ThreadUtils.runOnMainThread(
                      () ->
                          Toast.makeText(
                                  ChatActivity.this,
                                  "Sending question to ChatGPT",
                                  Toast.LENGTH_SHORT)
                              .show());

                  ThreadUtils.runInBackground(
                      () -> {
                        try {
                          String prompt =
                              "You are a party assistant. Your role is to provide details and help with whatever you can for this party. Here are the party details: "
                                  + getGroupDetails()
                                  + "\n\nQuestion: ";
                          OpenAiApi openAiApi = new OpenAiApi(getApiKey());
                          String gptAnswer = openAiApi.sendMessage(prompt + gptQuestion);
                          ThreadUtils.runOnMainThread(() -> sendBotMessage(gptAnswer));
                        } catch (java.net.UnknownHostException e) {
                          ThreadUtils.runOnMainThread(
                              () -> {
                                Toast.makeText(
                                        ChatActivity.this,
                                        "Internet connection error, check your connection and try again.",
                                        Toast.LENGTH_LONG)
                                    .show();
                                // Send a fallback message
                                sendBotMessage(
                                    "Sorry I can not connect to the internet at the moment, check your connection and try again.");
                              });
                        } catch (Exception e) {
                          ThreadUtils.runOnMainThread(
                              () -> {
                                Toast.makeText(
                                        ChatActivity.this,
                                        "GPT Service Error: " + e.getMessage(),
                                        Toast.LENGTH_LONG)
                                    .show();
                                // Send a fallback message
                                sendBotMessage("Internal Service Error, try again later.");
                              });
                        }
                      });
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
    android.icu.text.SimpleDateFormat sdf = new android.icu.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
    String strDate = sdf.format(c.getTime());

    // Generate a unique key for the bot message
    String messageKey = generateUniqueKey();
    botMsg.setMessageTime(strDate);
    botMsg.setMessageText(answer);
    botMsg.setMessageKey(messageKey);

    // Save bot message to server
    saveMessageToServer(messageKey, botMsg);

    // Update group's message keys
    MessageKeys.put(messageKey, "true");
    updateGroupMessageKeys();
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
    Log.d(TAG, "ShowData: Starting to load messages");
    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();
    Log.d(TAG, "ShowData: Got FirebaseServerClient instance: " + serverClient);

    // Initialize adapter if it's null
    if (adapter == null) {
      Log.d(TAG, "ShowData: Adapter is null, initializing new adapter");
      adapter = new ChatAdapter(this, R.layout.item_chat_message, new ArrayList<>());
      lv4.setAdapter(adapter);
      Log.d(TAG, "ShowData: Adapter set on ListView");
    } else {
      Log.d(TAG, "ShowData: Adapter already initialized");
    }

    // We can't cast ListView to ProgressBar, so let's use the ListView directly
    lv4.setVisibility(View.VISIBLE);

    // Add debug logging for GroupKey
    Log.d(TAG, "ShowData: Fetching messages for group: " + GroupKey);

    serverClient.getMessages(
        GroupKey,
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(List<ChatMessage> messages) {
            // Show the ListView
            ThreadUtils.runOnMainThread(() -> lv4.setVisibility(View.VISIBLE));

            if (messages != null && !messages.isEmpty()) {
              Log.d(TAG, "ShowData: Received " + messages.size() + " messages from server");

              // Ensure messageTime is populated from timestamp for sorting
              for (ChatMessage message : messages) {
                if (message != null && message.getMessageTime() == null) {
                  // Convert timestamp to messageTime for sorting
                  android.icu.text.SimpleDateFormat dateFormat = new android.icu.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                  String timeString = dateFormat.format(new Date(message.getTimestamp()));
                  message.setMessageTime(timeString);
                  Log.d(TAG, "ShowData: Set messageTime from timestamp: " + timeString);
                }
              }

              // Decrypt messages if encryption is available
              if (groupEncryption != null && groupEncryption.hasGroupKey(GroupKey)) {
                Log.d(TAG, "ShowData: Decrypting messages for group: " + GroupKey);
                for (int i = 0; i < messages.size(); i++) {
                  ChatMessage message = messages.get(i);
                  if (message != null && message.isEncrypted()) {
                    ChatMessage decryptedMessage = groupEncryption.decryptChatMessage(message);
                    if (decryptedMessage != null) {
                      messages.set(i, decryptedMessage);
                      Log.d(TAG, "ShowData: Message decrypted: " + message.getLogSafeSummary());
                    } else {
                      Log.w(TAG, "ShowData: Failed to decrypt message: " + message.getLogSafeSummary());
                    }
                  }
                }
              } else {
                Log.d(TAG, "ShowData: Group encryption not available, displaying messages as-is");
              }

              // Sort messages by timestamp (oldest first, newest last)
              messages.sort(
                  (m1, m2) -> {
                    // Use timestamp for reliable sorting
                    long t1 = m1.getTimestamp();
                    long t2 = m2.getTimestamp();
                    return Long.compare(t1, t2); // Oldest first (ascending order)
                  });

              // Debug: Log message order
              Log.d(TAG, "ShowData: Message order after sorting:");
              for (int i = 0; i < Math.min(5, messages.size()); i++) {
                ChatMessage msg = messages.get(i);
                Log.d(TAG, "  [" + i + "] " + new Date(msg.getTimestamp()) + " - " + 
                      (msg.getMessage() != null ? msg.getMessage().substring(0, Math.min(20, msg.getMessage().length())) : "null"));
              }

              // Update the adapter
              ThreadUtils.runOnMainThread(
                  () -> {
                    Log.d(TAG, "ShowData: Updating adapter with " + messages.size() + " messages");
                    adapter.clear();
                    adapter.addAll(messages);
                    adapter.notifyDataSetChanged();

                    // Scroll to the bottom (newest message)
                    if (adapter.getCount() > 0) {
                      int lastPosition = adapter.getCount() - 1;
                      Log.d(TAG, "ShowData: Scrolling to position " + lastPosition + " out of " + adapter.getCount() + " messages");
                      
                      // Check what's the last message
                      ChatMessage lastMessage = messages.get(lastPosition);
                      Log.d(TAG, "ShowData: Last message at position " + lastPosition + ": " + 
                            new Date(lastMessage.getTimestamp()) + " - " + 
                            (lastMessage.getMessage() != null ? lastMessage.getMessage().substring(0, Math.min(20, lastMessage.getMessage().length())) : "null"));
                      
                      lv4.post(() -> {
                        lv4.setSelection(lastPosition);
                        lv4.smoothScrollToPosition(lastPosition);
                      });
                    }
                    Log.d(TAG, "ShowData: Adapter updated successfully");
                  });
            } else {
              Log.d(TAG, "ShowData: No messages found or messages list is null");
              // Ensure we have an empty adapter to allow sending new messages
              ThreadUtils.runOnMainThread(
                  () -> {
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "ShowData: Cleared adapter as no messages were found");
                  });
            }
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "ShowData: Error fetching messages: " + errorMessage);
            ThreadUtils.runOnMainThread(
                () -> {
                  Toast.makeText(
                          ChatActivity.this,
                          "Error loading messages: " + errorMessage,
                          Toast.LENGTH_SHORT)
                      .show();
                  lv4.setVisibility(View.VISIBLE);
                });
          }
        });
  }

  public String getGroupDetails() {
    StringBuilder details = new StringBuilder();
    details.append("Party Details:\n");
    details.append("Party Name: ").append(GroupKey).append("\n");

    serverClient.getGroup(
        GroupKey,
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(Group group) {
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

          @Override
          public void onError(String errorMessage) {
            details.append("Error loading group details: ").append(errorMessage);
            Log.e(TAG, "Failed to load group details: " + errorMessage);
          }
        });

    return details.toString();
  }

  private void sendMessage(String messageText) {
    Log.d(TAG, "sendMessage called with text: " + messageText);

    if (GroupKey == null || UserKey == null) {
      Log.e(TAG, "Cannot send message: GroupKey or UserKey is null");
      Toast.makeText(this, "Error: Missing group or user information", Toast.LENGTH_SHORT).show();
      return;
    }

    try {
      Log.d(TAG, "Creating message object");
      // Generate a unique message key
      String messageId = "msg_" + System.currentTimeMillis();
      Log.d(TAG, "Generated messageId: " + messageId);

      // Create the message object
      ChatMessage message = new ChatMessage();
      message.setMessageKey(messageId);
      message.setGroupKey(GroupKey);
      message.setSenderKey(UserKey);
      message.setSenderName(UserKey); // For now, use UserKey as display name
      message.setMessage(messageText);
      message.setTimestamp(System.currentTimeMillis());
      
      // Format the current time for legacy compatibility
      android.icu.text.SimpleDateFormat dateFormat =
          new android.icu.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
      String currentTime = dateFormat.format(new Date());
      message.setMessageTime(currentTime);
      
      // Set legacy fields for compatibility
      message.setMessageText(messageText);
      message.setMessageUser(UserKey);
      message.setGroupId(GroupKey);

      // Encrypt the message if encryption is available
      ChatMessage messageToSend = message;
      if (groupEncryption != null && groupEncryption.hasGroupKey(GroupKey)) {
        Log.d(TAG, "Encrypting message for group: " + GroupKey);
        ChatMessage encryptedMessage = groupEncryption.encryptChatMessage(message);
        if (encryptedMessage != null) {
          messageToSend = encryptedMessage;
          Log.d(TAG, "Message encrypted successfully");
        } else {
          Log.w(TAG, "Message encryption failed, sending plain text");
        }
      } else {
        Log.d(TAG, "Group encryption not available, sending plain text");
      }

      Log.d(
          TAG,
          "Message object created: "
              + message.getMessageText()
              + ", time: "
              + message.getMessageTime());

      // Save the message using FirebaseServerClient
      FirebaseServerClient serverClient = FirebaseServerClient.getInstance();
      Log.d(TAG, "Got FirebaseServerClient instance: " + serverClient);

      Log.d(TAG, "About to call serverClient.saveMessage");
      serverClient.saveMessage(
          messageToSend,
          new FirebaseServerClient.DataCallback<>() {
            @Override
            public void onSuccess(Boolean success) {
              Log.d(TAG, "Message saved successfully: " + success);
              if (success) {
                // Refresh messages
                ShowData();
              } else {
                Log.e(TAG, "Failed to save message (success=false)");
                Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT)
                    .show();
              }
            }

            @Override
            public void onError(String errorMessage) {
              Log.e(TAG, "Error saving message: " + errorMessage);
              Toast.makeText(
                      ChatActivity.this,
                      "Error sending message: " + errorMessage,
                      Toast.LENGTH_SHORT)
                  .show();
            }
          });
      Log.d(TAG, "serverClient.saveMessage call completed");

    } catch (Exception e) {
      Log.e(TAG, "Exception in sendMessage", e);
      Toast.makeText(this, "Error sending message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
  }

  private void sendMessageWithRetry(
      final String messageId, final ChatMessage message, final int retryCount) {
    Log.d(
        TAG,
        "AUTO TEST: sendMessageWithRetry called for message ID: "
            + messageId
            + ", attempt: "
            + (retryCount + 1));
    Log.d(
        TAG,
        "AUTO TEST: Message content: "
            + message.getMessageText()
            + ", from user: "
            + message.getMessageUser());
    Log.d(TAG, "AUTO TEST: Using GroupKey: " + GroupKey);

    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();
    Log.d(TAG, "AUTO TEST: Got FirebaseServerClient instance: " + serverClient);

    // Set groupId in the message
    message.setGroupId(GroupKey);
    Log.d(TAG, "AUTO TEST: Set groupId in message: " + GroupKey);

    // Set messageKey in the message
    message.setMessageKey(messageId);
    Log.d(TAG, "AUTO TEST: Set messageKey in message: " + messageId);

    Log.d(
        TAG,
        "AUTO TEST: About to call serverClient.saveMessage with GroupKey: "
            + GroupKey
            + ", messageId: "
            + messageId);
    try {
      serverClient.saveMessage(
          GroupKey,
          messageId,
          message,
          new FirebaseServerClient.OperationCallback() {
            @Override
            public void onSuccess() {
              Log.d(TAG, "AUTO TEST: Message saved successfully with ID: " + messageId);

              // Update the message in the UI to remove "Sending..." text
              if (adapter != null) {
                for (int i = 0; i < adapter.getCount(); i++) {
                  ChatMessage msg = adapter.getItem(i);
                  if (msg != null && messageId.equals(msg.getMessageKey())) {
                    Log.d(TAG, "AUTO TEST: Found message in adapter, updating text");
                    msg.setMessageText(message.getMessageText());
                    ThreadUtils.runOnMainThread(() -> adapter.notifyDataSetChanged());
                    break;
                  }
                }
              } else {
                Log.e(TAG, "AUTO TEST: adapter is null, cannot update message in UI");
              }

              // Update group message keys
              Log.d(TAG, "AUTO TEST: Updating group message keys for message ID: " + messageId);
              updateGroupMessageKeys(messageId);
            }

            @Override
            public void onError(String errorMessage) {
              Log.e(
                  TAG,
                  "AUTO TEST: Failed to save message: " + messageId + ", error: " + errorMessage);

              if (retryCount < MAX_RETRY_ATTEMPTS) {
                // Retry after a delay (exponential backoff)
                int delayMillis = 1000 * (1 << retryCount); // 1s, 2s, 4s
                Log.d(
                    TAG,
                    "AUTO TEST: Retrying to send message in "
                        + delayMillis
                        + "ms (attempt "
                        + (retryCount + 1)
                        + ")");

                ThreadUtils.runOnMainThreadDelayed(
                    () -> sendMessageWithRetry(messageId, message, retryCount + 1), delayMillis);
              } else {
                // Max retries reached, show error to user
                Log.e(TAG, "AUTO TEST: Max retry attempts reached for message ID: " + messageId);
                ThreadUtils.runOnMainThread(
                    () -> {
                      Toast.makeText(
                              ChatActivity.this,
                              "Failed to send message: " + errorMessage,
                              Toast.LENGTH_SHORT)
                          .show();

                      // Update the message in the UI to show error
                      if (adapter != null) {
                        for (int i = 0; i < adapter.getCount(); i++) {
                          ChatMessage msg = adapter.getItem(i);
                          if (msg != null && messageId.equals(msg.getMessageKey())) {
                            Log.d(
                                TAG,
                                "AUTO TEST: Found message in adapter, updating text to show error");
                            msg.setMessageText(message.getMessageText() + " (Failed to send)");
                            adapter.notifyDataSetChanged();
                            break;
                          }
                        }
                      } else {
                        Log.e(TAG, "AUTO TEST: adapter is null, cannot update message in UI");
                      }
                    });
              }
            }
          });
      Log.d(TAG, "AUTO TEST: serverClient.saveMessage call completed");
    } catch (Exception e) {
      Log.e(TAG, "AUTO TEST: Exception when calling serverClient.saveMessage", e);
    }
  }

  private void updateGroupMessageKeys(final String messageId) {
    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();

    // Get the current group
    serverClient.getGroup(
        GroupKey,
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(Group group) {
            if (group == null) {
              Log.e(TAG, "Failed to update group message keys: Group is null");
              return;
            }

            // Add the new message key to the group's message keys
            Map<String, Object> messageKeys = group.getMessageKeys();
            if (messageKeys == null) {
              messageKeys = new HashMap<>();
            }

            messageKeys.put(messageId, true);

            // Create a new HashMap with the updated keys
            HashMap<String, Object> updatedMessageKeys = new HashMap<>(messageKeys);
            group.setMessageKeys(updatedMessageKeys);

            // Update the group
            updateGroupWithRetry(group, 0);
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Failed to get group for updating message keys: " + errorMessage);
          }
        });
  }

  private void updateGroupWithRetry(final Group group, final int retryCount) {
    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();

    // Create a map of updates instead of sending the whole group
    Map<String, Object> updates = new HashMap<>();
    updates.put("MessageKeys", group.getMessageKeys());

    // Use updateGroup instead of saveGroup
    serverClient.updateGroup(
        GroupKey,
        updates,
        new FirebaseServerClient.OperationCallback() {
          @Override
          public void onSuccess() {
            Log.d(TAG, "Group message keys updated successfully");
          }

          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Failed to update group message keys: " + errorMessage);

            if (retryCount < MAX_RETRY_ATTEMPTS) {
              // Retry after a delay
              int delayMillis = 1000 * (1 << retryCount); // 1s, 2s, 4s
              Log.d(
                  TAG,
                  "Retrying to update group in "
                      + delayMillis
                      + "ms (attempt "
                      + (retryCount + 1)
                      + ")");

              ThreadUtils.runOnMainThreadDelayed(
                  () -> updateGroupWithRetry(group, retryCount + 1), delayMillis);
            }
          }
        });
  }
}
