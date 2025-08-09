package com.example.partymaker.ui.features.groups.chat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.partymaker.R;
import com.example.partymaker.data.api.FirebaseServerClient;
import com.example.partymaker.data.api.FirebaseServerClient.OperationCallback;
import com.example.partymaker.data.api.OpenAiApi;
import com.example.partymaker.data.model.ChatMessage;
import com.example.partymaker.data.model.Group;
import com.example.partymaker.ui.adapters.ChatRecyclerAdapter;
import com.example.partymaker.ui.features.auxiliary.chatbot.GptChatActivity;
import com.example.partymaker.utils.auth.AuthenticationManager;
import com.example.partymaker.utils.core.ExtrasMetadata;
import com.example.partymaker.utils.core.IntentExtrasManager;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import com.example.partymaker.utils.security.encryption.GroupKeyManager;
import com.example.partymaker.utils.security.encryption.GroupMessageEncryption;
import com.example.partymaker.viewmodel.groups.GroupChatViewModel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ChatActivity - Real-time messaging interface for group conversations.
 * 
 * This activity provides a modern chat experience with:
 * - Real-time message synchronization
 * - End-to-end encryption support
 * - AI assistant integration
 * - Material Design 3 UI components
 * - Offline message queueing
 * 
 * Architecture:
 * - MVVM Pattern: Uses GroupChatViewModel for business logic
 * - Repository Pattern: Messages handled through FirebaseServerClient
 * - Observer Pattern: LiveData for real-time updates
 * - Encryption: GroupMessageEncryption for secure messaging
 * 
 * Key Features:
 * - RecyclerView-based message display
 * - Pull-to-refresh for message history
 * - Typing indicators
 * - Message status (sent/delivered/read)
 * - AI chatbot integration via attachment button
 * - Automatic retry mechanism for failed messages
 * 
 * Security:
 * - Messages encrypted using group-specific keys
 * - User authentication required
 * - Server-side message validation
 * 
 * @author PartyMaker Team
 * @version 2.0
 * @since 1.0
 */
public class ChatActivity extends AppCompatActivity {

  /** Tag for logging and debugging */
  private static final String TAG = "ChatActivity";
  
  // ==================== Retry Configuration ====================
  /** Maximum number of retry attempts for failed operations */
  private static final int MAX_RETRY_ATTEMPTS = 3;
  /** Initial delay before first retry (exponential backoff) */
  private static final int FIRST_RETRY_DELAY_MS = 1000;
  /** Range for generating random message IDs */
  private static final int RANDOM_RANGE = 10000;
  
  // ==================== Timeout Configuration ====================
  /** Timeout for GPT API requests */
  private static final int GPT_TIMEOUT_MS = 3000;
  /** Delay before hiding animation */
  private static final long ANIMATION_HIDE_DELAY_MS = 3000L;
  /** Delay for success animation display */
  private static final long SUCCESS_ANIMATION_DELAY_MS = 2500L;
  
  /** Handler for retry operations on main thread */
  private final Handler retryHandler = new Handler(Looper.getMainLooper());
  // ==================== UI Components ====================
  /** RecyclerView for displaying chat messages */
  private RecyclerView recyclerView;
  /** Input field for composing messages */
  private TextInputEditText messageEditText;
  /** FAB button for sending messages */
  private FloatingActionButton sendButton;
  /** Button for attachments and AI assistant */
  private ImageButton attachButton;
  /** TextView displaying party name in toolbar */
  private TextView tvPartyName;
  /** TextView displaying member count in toolbar */
  private TextView tvPartyMembers;
  
  // ==================== Data Components ====================
  /** Unique identifier for the current group/party */
  private String groupKey;
  /** Map of message IDs for this group */
  private HashMap<String, Object> messageKeys;
  /** Client for server communication */
  private FirebaseServerClient serverClient;
  /** Adapter for binding messages to RecyclerView */
  private ChatRecyclerAdapter adapter;
  /** Current authenticated user's ID */
  private String userKey;
  /** ViewModel for managing chat data */
  private GroupChatViewModel viewModel;
  
  // ==================== Security Components ====================
  /** Manager for group encryption keys */
  private GroupKeyManager groupKeyManager;
  /** Encryption handler for secure messaging */
  private GroupMessageEncryption groupEncryption;

  /**
   * Activity lifecycle - onCreate
   * 
   * Initializes the chat interface and establishes connections.
   * Order of initialization is critical for proper functionality.
   * 
   * @param savedInstanceState Bundle containing saved state
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "ChatActivity onCreate called");
    setContentView(R.layout.activity_party_chat);

    // Step 1: Initialize server communication client
    serverClient = FirebaseServerClient.getInstance();

    // Step 2: Initialize ViewModel for data management
    viewModel = new ViewModelProvider(this).get(GroupChatViewModel.class);
    setupViewModelObservers();

    // Step 3: Configure action bar appearance
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle("Chat");
      actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0081d1")));
    }

    // Step 4: Extract data from intent
    // Priority: Direct intent extras > ExtrasMetadata
    Log.d(TAG, "Getting extras from intent");
    Intent intent = getIntent();

    // Try direct intent extra first (backwards compatibility)
    groupKey = intent.getStringExtra("GroupKey");
    Log.d(TAG, "groupKey from direct intent extra: " + groupKey);

    // Try to get data from ExtrasMetadata only if direct intent extra is null
    if (groupKey == null || groupKey.isEmpty()) {
      ExtrasMetadata extras = IntentExtrasManager.getExtrasMetadataFromIntent(intent);
      if (extras != null) {
        Log.d(TAG, "Found ExtrasMetadata in intent");
        messageKeys = extras.getMessageKeys();
        groupKey = extras.getGroupKey();
      } else {
        // Initialize empty messageKeys
        messageKeys = new HashMap<>();
      }
    } else {
      // Initialize empty messageKeys
      messageKeys = new HashMap<>();
    }

    // Check if we have the required data
    if (groupKey == null || groupKey.isEmpty()) {
      Log.e(TAG, "Missing groupKey in intent");
      Toast.makeText(this, "Missing group data", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    Log.d(TAG, "groupKey retrieved: " + groupKey);

    // Get userKey from AuthHelper instead of Firebase Auth
    try {
      userKey = AuthenticationManager.getCurrentUserKey(this);
      Log.d(TAG, "userKey initialized from AuthHelper: " + userKey);
    } catch (Exception e) {
      Log.e(TAG, "Failed to get current user from AuthHelper", e);
      userKey = intent.getStringExtra("UserKey"); // Fallback to intent if auth fails
      Log.d(TAG, "Using fallback userKey from intent: " + userKey);

      if (userKey == null) {
        Log.e(TAG, "userKey is null after fallback");
        Toast.makeText(this, "Error: Missing user information", Toast.LENGTH_SHORT).show();
        finish();
        return;
      }
    }

    // Initialize UI components
    Log.d(TAG, "Initializing UI components");
    recyclerView = findViewById(R.id.recyclerViewMessages);
    messageEditText = findViewById(R.id.etMessage);
    sendButton = findViewById(R.id.fabSend);
    attachButton = findViewById(R.id.btnAttach);
    tvPartyName = findViewById(R.id.tvPartyName);
    tvPartyMembers = findViewById(R.id.tvPartyMembers);
    
    // Setup RecyclerView
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    layoutManager.setStackFromEnd(false); // Start from top, oldest messages first
    recyclerView.setLayoutManager(layoutManager);

    ShowData();
    eventHandler();
    setupAttachButton();

    // Set the group key in ViewModel
    viewModel.setGroupKey(groupKey);

    // Initialize encryption for this group
    initializeGroupEncryption();
    
    // Load group info for toolbar
    loadGroupInfo();
  }

  /** Initialize group encryption for secure messaging */
  private void initializeGroupEncryption() {
    if (userKey == null || groupKey == null) {
      Log.w(TAG, "Cannot initialize encryption: missing userKey or groupKey");
      return;
    }

    try {
      // Initialize encryption managers
      groupKeyManager = new GroupKeyManager(this, userKey);
      groupEncryption = groupKeyManager.getEncryptionManager();

      // Check if user is already a member of this group's encryption
      groupKeyManager
          .isGroupMember(groupKey)
          .thenAccept(
              isMember -> {
                if (!isMember) {
                  Log.i(TAG, "User not in group encryption, adding automatically");
                  // Auto-add current user to group encryption
                  groupKeyManager
                      .addUserToGroupEncryption(groupKey, userKey)
                      .thenAccept(
                          success -> {
                            if (success) {
                              Log.i(TAG, "Successfully added user to group encryption");
                              // Now initialize for existing group
                              groupKeyManager.initializeForExistingGroup(groupKey);
                            } else {
                              Log.e(TAG, "Failed to add user to group encryption");
                              Toast.makeText(
                                      this,
                                      "Warning: Could not setup message encryption",
                                      Toast.LENGTH_SHORT)
                                  .show();
                            }
                          });
                } else {
                  Log.i(TAG, "User already in group encryption, initializing");
                  // Initialize encryption for existing group
                  groupKeyManager.initializeForExistingGroup(groupKey);
                }
              });

      Log.i(TAG, "Group encryption initialization started for: " + groupKey);

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
                messageEditText.setText("");
                viewModel.resetMessageSentFlag();
              }
            });

    viewModel.getIsLoading().observe(this, isLoading -> sendButton.setEnabled(!isLoading));
  }

  private void eventHandler() {
    Log.d(TAG, "Setting up event handlers");

    Log.d(TAG, "Setting up send button click listener");
    sendButton.setOnClickListener(
        v -> {
          Log.d(TAG, "Send button clicked");
          String messageText = messageEditText.getText().toString().trim();
          Log.d(TAG, "Message text: '" + messageText + "'");

          if (!messageText.isEmpty()) {
            Log.d(TAG, "Message is not empty, sending message");
            sendMessage(messageText);
            messageEditText.setText("");
          } else {
            Log.d(TAG, "Message is empty, not sending");
            Toast.makeText(this, "Cannot send empty message", Toast.LENGTH_SHORT).show();
          }
        });

    Log.d(TAG, "Event handlers setup completed");
  }

  private String generateUniqueKey() {
    // Generate a timestamp-based unique key
    return "msg_" + System.currentTimeMillis() + "_" + (int) (Math.random() * RANDOM_RANGE);
  }

  private void saveMessageToServer(String messageKey, ChatMessage message) {
    serverClient.saveMessage(
        groupKey,
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
    updates.put("MessageKeys", messageKeys);
    serverClient.updateGroup(
        groupKey,
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

  private void setupAttachButton() {
    attachButton.setOnClickListener(
        v -> {
          // Open the GptChatActivity instead of showing dialog
          Intent intent = new Intent(ChatActivity.this, GptChatActivity.class);
          intent.putExtra("groupKey", groupKey);
          intent.putExtra("groupDetails", getGroupDetails());
          startActivity(intent);
        });
  }

  private void sendBotMessage(String answer) {
    ChatMessage botMsg = new ChatMessage();
    botMsg.setMessageUser("PartyBot");
    Calendar c = Calendar.getInstance();
    android.icu.text.SimpleDateFormat sdf =
        new android.icu.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
    String strDate = sdf.format(c.getTime());

    // Generate a unique key for the bot message
    String messageKey = generateUniqueKey();
    botMsg.setMessageTime(strDate);
    botMsg.setMessageText(answer);
    botMsg.setMessageKey(messageKey);

    // Save bot message to server
    saveMessageToServer(messageKey, botMsg);

    // Update group's message keys
    messageKeys.put(messageKey, "true");
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
      adapter = new ChatRecyclerAdapter(this);
      recyclerView.setAdapter(adapter);
      Log.d(TAG, "ShowData: Adapter set on RecyclerView");
    } else {
      Log.d(TAG, "ShowData: Adapter already initialized");
    }

    // Show the RecyclerView
    recyclerView.setVisibility(View.VISIBLE);

    // Add debug logging for groupKey
    Log.d(TAG, "ShowData: Fetching messages for group: " + groupKey);

    serverClient.getMessages(
        groupKey,
        new FirebaseServerClient.DataCallback<>() {
          @Override
          public void onSuccess(List<ChatMessage> messages) {
            // Show the RecyclerView
            ThreadUtils.runOnMainThread(() -> recyclerView.setVisibility(View.VISIBLE));

            if (messages != null && !messages.isEmpty()) {
              Log.d(TAG, "ShowData: Received " + messages.size() + " messages from server");

              // Ensure messageTime is populated from timestamp for sorting
              for (ChatMessage message : messages) {
                if (message != null && message.getMessageTime() == null) {
                  // Convert timestamp to messageTime for sorting
                  android.icu.text.SimpleDateFormat dateFormat =
                      new android.icu.text.SimpleDateFormat(
                          "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                  String timeString = dateFormat.format(new Date(message.getTimestamp()));
                  message.setMessageTime(timeString);
                  Log.d(TAG, "ShowData: Set messageTime from timestamp: " + timeString);
                }
              }

              // Decrypt messages if encryption is available
              if (groupEncryption != null && groupEncryption.hasGroupKey(groupKey)) {
                Log.d(TAG, "ShowData: Decrypting messages for group: " + groupKey);
                for (int i = 0; i < messages.size(); i++) {
                  ChatMessage message = messages.get(i);
                  if (message != null && message.isEncrypted()) {
                    ChatMessage decryptedMessage = groupEncryption.decryptChatMessage(message);
                    if (decryptedMessage != null) {
                      messages.set(i, decryptedMessage);
                      Log.d(TAG, "ShowData: Message decrypted: " + message.getLogSafeSummary());
                    } else {
                      Log.w(
                          TAG,
                          "ShowData: Failed to decrypt message: " + message.getLogSafeSummary());
                    }
                  }
                }
              } else {
                Log.d(TAG, "ShowData: Group encryption not available, displaying messages as-is");
              }

              // Sort messages by timestamp (oldest first at top, newest at bottom)
              messages.sort(
                  (m1, m2) -> {
                    // Use timestamp for reliable sorting
                    long t1 = m1.getTimestamp();
                    long t2 = m2.getTimestamp();
                    return Long.compare(t1, t2); // Ascending order - oldest at top
                  });

              // Debug: Log message order
              Log.d(TAG, "ShowData: Message order after sorting:");
              for (int i = 0; i < Math.min(5, messages.size()); i++) {
                ChatMessage msg = messages.get(i);
                Log.d(
                    TAG,
                    "  ["
                        + i
                        + "] "
                        + new Date(msg.getTimestamp())
                        + " - "
                        + (msg.getMessage() != null
                            ? msg.getMessage().substring(0, Math.min(20, msg.getMessage().length()))
                            : "null"));
              }

              // Update the adapter
              ThreadUtils.runOnMainThread(
                  () -> {
                    Log.d(TAG, "ShowData: Updating adapter with " + messages.size() + " messages");
                    adapter.setMessages(messages);

                    // Scroll to the bottom (newest message)
                    if (messages.size() > 0) {
                      int lastPosition = messages.size() - 1;
                      Log.d(
                          TAG,
                          "ShowData: Scrolling to position "
                              + lastPosition
                              + " out of "
                              + messages.size()
                              + " messages");

                      // Check what's the last message
                      ChatMessage lastMessage = messages.get(lastPosition);
                      Log.d(
                          TAG,
                          "ShowData: Last message at position "
                              + lastPosition
                              + ": "
                              + new Date(lastMessage.getTimestamp())
                              + " - "
                              + (lastMessage.getMessage() != null
                                  ? lastMessage
                                      .getMessage()
                                      .substring(0, Math.min(20, lastMessage.getMessage().length()))
                                  : "null"));

                      // Scroll to bottom to show newest message
                      recyclerView.post(
                          () -> {
                            recyclerView.smoothScrollToPosition(lastPosition);
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
                  recyclerView.setVisibility(View.VISIBLE);
                });
          }
        });
  }

  public String getGroupDetails() {
    StringBuilder details = new StringBuilder();
    details.append("Party Details:\n");
    details.append("Party Name: ").append(groupKey).append("\n");

    serverClient.getGroup(
        groupKey,
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

    if (groupKey == null || userKey == null) {
      Log.e(TAG, "Cannot send message: groupKey or userKey is null");
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
      message.setGroupKey(groupKey);
      message.setSenderKey(userKey);
      message.setSenderName(userKey); // For now, use userKey as display name
      message.setMessage(messageText);
      message.setTimestamp(System.currentTimeMillis());

      // Format the current time for legacy compatibility
      android.icu.text.SimpleDateFormat dateFormat =
          new android.icu.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
      String currentTime = dateFormat.format(new Date());
      message.setMessageTime(currentTime);

      // Set legacy fields for compatibility
      message.setMessageText(messageText);
      message.setMessageUser(userKey);
      message.setGroupId(groupKey);

      // Encrypt the message if encryption is available
      ChatMessage messageToSend = message;
      if (groupEncryption != null && groupEncryption.hasGroupKey(groupKey)) {
        Log.d(TAG, "Encrypting message for group: " + groupKey);
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
                // Refresh messages and scroll to bottom
                ShowData();
                // Scroll to bottom after sending
                recyclerView.postDelayed(
                    () -> {
                      if (adapter != null && adapter.getItemCount() > 0) {
                        recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                      }
                    }, 200);
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
    Log.d(TAG, "AUTO TEST: Using groupKey: " + groupKey);

    FirebaseServerClient serverClient = FirebaseServerClient.getInstance();
    Log.d(TAG, "AUTO TEST: Got FirebaseServerClient instance: " + serverClient);

    // Set groupId in the message
    message.setGroupId(groupKey);
    Log.d(TAG, "AUTO TEST: Set groupId in message: " + groupKey);

    // Set messageKey in the message
    message.setMessageKey(messageId);
    Log.d(TAG, "AUTO TEST: Set messageKey in message: " + messageId);

    Log.d(
        TAG,
        "AUTO TEST: About to call serverClient.saveMessage with groupKey: "
            + groupKey
            + ", messageId: "
            + messageId);
    try {
      serverClient.saveMessage(
          groupKey,
          messageId,
          message,
          new FirebaseServerClient.OperationCallback() {
            @Override
            public void onSuccess() {
              Log.d(TAG, "AUTO TEST: Message saved successfully with ID: " + messageId);

              // Update the message in the UI to remove "Sending..." text
              if (adapter != null) {
                // For RecyclerView adapter, we need to refresh the data
                Log.d(TAG, "AUTO TEST: Message sent successfully, refreshing data");
                ShowData();
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
                        Log.d(TAG, "AUTO TEST: Message failed to send, refreshing data");
                        ShowData();
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
        groupKey,
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
        groupKey,
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
              int delayMillis = FIRST_RETRY_DELAY_MS * (1 << retryCount); // 1s, 2s, 4s
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

  /**
   * Process GPT request with proper error handling
   *
   * @param gptQuestion the question to ask GPT
   */
  private void processGptRequest(String gptQuestion) {
    try {
      String answer = getGptResponse(gptQuestion);
      ThreadUtils.runOnMainThread(() -> sendBotMessage(answer));
    } catch (java.net.UnknownHostException e) {
      handleGptNetworkError();
    } catch (Exception e) {
      handleGptServiceError(e);
    }
  }

  /**
   * Gets response from GPT API
   *
   * @param gptQuestion the question to ask
   * @return GPT's response
   * @throws Exception if API call fails
   */
  private String getGptResponse(String gptQuestion) throws Exception {
    String prompt = buildGptPrompt();
    OpenAiApi openAiApi = new OpenAiApi(getApiKey());
    return openAiApi.sendMessage(prompt + gptQuestion);
  }

  /**
   * Builds the prompt for GPT with party details
   *
   * @return formatted prompt string
   */
  private String buildGptPrompt() {
    return "You are a party assistant. Your role is to provide details and help with whatever you can for this party. Here are the party details: "
        + getGroupDetails()
        + "\n\nQuestion: ";
  }

  /** Handles network errors when communicating with GPT */
  private void handleGptNetworkError() {
    ThreadUtils.runOnMainThread(
        () -> {
          Toast.makeText(
                  ChatActivity.this,
                  "Internet connection error, check your connection and try again.",
                  Toast.LENGTH_LONG)
              .show();
          sendBotMessage(
              "Sorry I can not connect to the internet at the moment, check your connection and try again.");
        });
  }

  /**
   * Handles service errors when communicating with GPT
   *
   * @param exception the exception that occurred
   */
  private void handleGptServiceError(Exception exception) {
    ThreadUtils.runOnMainThread(
        () -> {
          Toast.makeText(
                  ChatActivity.this,
                  "GPT Service Error: " + exception.getMessage(),
                  Toast.LENGTH_LONG)
              .show();
          sendBotMessage("Internal Service Error, try again later.");
        });
  }

  /**
   * Handle GPT processing errors
   *
   * @param exception the exception that occurred during GPT processing
   */
  private void handleGptError(Exception exception) {
    ThreadUtils.runOnMainThread(
        () -> {
          Toast.makeText(
                  ChatActivity.this,
                  "Error processing GPT request: " + exception.getMessage(),
                  Toast.LENGTH_LONG)
              .show();
          Log.e(TAG, "GPT request processing failed", exception);
        });
  }
  
  private void loadGroupInfo() {
    if (groupKey == null) return;
    
    serverClient.getGroup(
        groupKey,
        new FirebaseServerClient.DataCallback<Group>() {
          @Override
          public void onSuccess(Group group) {
            if (group != null) {
              ThreadUtils.runOnMainThread(() -> {
                // Update party name in toolbar
                if (tvPartyName != null) {
                  tvPartyName.setText(group.getGroupName());
                }
                
                // Update member count
                if (tvPartyMembers != null) {
                  int memberCount = 1; // At least the current user
                  // Count members from the FriendKeys map if available
                  if (group.getFriendKeys() != null) {
                    memberCount = group.getFriendKeys().size();
                  }
                  tvPartyMembers.setText(memberCount + " members");
                }
                
                // Update action bar title
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                  actionBar.setTitle(group.getGroupName());
                }
              });
            }
          }
          
          @Override
          public void onError(String errorMessage) {
            Log.e(TAG, "Failed to load group info: " + errorMessage);
          }
        });
  }
}
