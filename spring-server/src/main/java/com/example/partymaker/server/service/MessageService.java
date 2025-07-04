package com.example.partymaker.server.service;

import com.example.partymaker.server.model.Message;
import com.example.partymaker.server.model.ServerResponse;
import com.example.partymaker.server.model.User;
import com.example.partymaker.server.util.FirebaseUtil;
import com.google.firebase.database.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class MessageService {
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);
    
    private final FirebaseUtil firebaseUtil;
    private final DatabaseReference database;
    
    @Autowired
    public MessageService(FirebaseUtil firebaseUtil) {
        this.firebaseUtil = firebaseUtil;
        this.database = FirebaseDatabase.getInstance().getReference();
    }
    
    public ServerResponse<Message> sendMessage(String token, Message message) {
        try {
            ServerResponse<User> verifyResponse = firebaseUtil.verifyIdToken(token);
            if (verifyResponse.getStatus() != 200) {
                return new ServerResponse<>(verifyResponse.getStatus(), verifyResponse.getMessage(), null);
            }

            User user = verifyResponse.getData();
            message.setSenderId(user.getUid());
            message.setTimestamp(Instant.now().getEpochSecond());
            
            if (message.getId() == null) {
                message.setId(database.child("messages").push().getKey());
            }

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<ServerResponse<Message>> response = new AtomicReference<>();
            
            database.child("messages").child(message.getId()).setValue(message, (error, ref) -> {
                if (error != null) {
                    response.set(new ServerResponse<>(500, "Failed to send message: " + error.getMessage(), null));
                } else {
                    response.set(new ServerResponse<>(200, "Message sent successfully", message));
                }
                latch.countDown();
            });

            latch.await(5, TimeUnit.SECONDS);
            return response.get() != null ? response.get() : new ServerResponse<>(500, "Operation timed out", null);
        } catch (Exception e) {
            logger.error("Error sending message", e);
            return new ServerResponse<>(500, "Internal server error", null);
        }
    }
    
    public ServerResponse<List<Message>> getPartyMessages(String token, String partyId) {
        try {
            ServerResponse<User> verifyResponse = firebaseUtil.verifyIdToken(token);
            if (verifyResponse.getStatus() != 200) {
                return new ServerResponse<>(verifyResponse.getStatus(), verifyResponse.getMessage(), null);
            }

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<ServerResponse<List<Message>>> response = new AtomicReference<>();
            
            database.child("messages")
                    .orderByChild("partyId")
                    .equalTo(partyId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            List<Message> messages = new ArrayList<>();
                            for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                                Message message = messageSnapshot.getValue(Message.class);
                                if (message != null) {
                                    messages.add(message);
                                }
                            }
                            response.set(new ServerResponse<>(200, "Messages retrieved successfully", messages));
                            latch.countDown();
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            response.set(new ServerResponse<>(500, "Failed to retrieve messages: " + error.getMessage(), null));
                            latch.countDown();
                        }
                    });

            latch.await(5, TimeUnit.SECONDS);
            return response.get() != null ? response.get() : new ServerResponse<>(500, "Operation timed out", null);
        } catch (Exception e) {
            logger.error("Error getting party messages", e);
            return new ServerResponse<>(500, "Internal server error", null);
        }
    }
    
    public ServerResponse<List<Message>> getUserMessages(String token) {
        try {
            ServerResponse<User> verifyResponse = firebaseUtil.verifyIdToken(token);
            if (verifyResponse.getStatus() != 200) {
                return new ServerResponse<>(verifyResponse.getStatus(), verifyResponse.getMessage(), null);
            }

            User user = verifyResponse.getData();
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<ServerResponse<List<Message>>> response = new AtomicReference<>();
            
            database.child("messages")
                    .orderByChild("senderId")
                    .equalTo(user.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            List<Message> messages = new ArrayList<>();
                            for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                                Message message = messageSnapshot.getValue(Message.class);
                                if (message != null) {
                                    messages.add(message);
                                }
                            }
                            response.set(new ServerResponse<>(200, "Messages retrieved successfully", messages));
                            latch.countDown();
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            response.set(new ServerResponse<>(500, "Failed to retrieve messages: " + error.getMessage(), null));
                            latch.countDown();
                        }
                    });

            latch.await(5, TimeUnit.SECONDS);
            return response.get() != null ? response.get() : new ServerResponse<>(500, "Operation timed out", null);
        } catch (Exception e) {
            logger.error("Error getting user messages", e);
            return new ServerResponse<>(500, "Internal server error", null);
        }
    }
    
    public ServerResponse<Message> markMessageAsRead(String token, String messageId) {
        try {
            ServerResponse<User> verifyResponse = firebaseUtil.verifyIdToken(token);
            if (verifyResponse.getStatus() != 200) {
                return new ServerResponse<>(verifyResponse.getStatus(), verifyResponse.getMessage(), null);
            }

            User user = verifyResponse.getData();
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<ServerResponse<Message>> response = new AtomicReference<>();
            
            database.child("messages").child(messageId).child("readBy").child(user.getUid()).setValue(true, (error, ref) -> {
                if (error != null) {
                    response.set(new ServerResponse<>(500, "Failed to mark message as read: " + error.getMessage(), null));
                    latch.countDown();
                } else {
                    // Get the updated message
                    database.child("messages").child(messageId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            Message message = snapshot.getValue(Message.class);
                            if (message == null) {
                                response.set(new ServerResponse<>(404, "Message not found", null));
                            } else {
                                response.set(new ServerResponse<>(200, "Message marked as read successfully", message));
                            }
                            latch.countDown();
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            response.set(new ServerResponse<>(500, "Failed to retrieve message: " + error.getMessage(), null));
                            latch.countDown();
                        }
                    });
                }
            });

            latch.await(5, TimeUnit.SECONDS);
            return response.get() != null ? response.get() : new ServerResponse<>(500, "Operation timed out", null);
        } catch (Exception e) {
            logger.error("Error marking message as read", e);
            return new ServerResponse<>(500, "Internal server error", null);
        }
    }
    
    public ServerResponse<Message> getMessage(String token, String messageId) {
        try {
            ServerResponse<User> verifyResponse = firebaseUtil.verifyIdToken(token);
            if (verifyResponse.getStatus() != 200) {
                return new ServerResponse<>(verifyResponse.getStatus(), verifyResponse.getMessage(), null);
            }

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<ServerResponse<Message>> response = new AtomicReference<>();
            
            database.child("messages").child(messageId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Message message = snapshot.getValue(Message.class);
                    if (message == null) {
                        response.set(new ServerResponse<>(404, "Message not found", null));
                    } else {
                        response.set(new ServerResponse<>(200, "Message retrieved successfully", message));
                    }
                    latch.countDown();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    response.set(new ServerResponse<>(500, "Failed to retrieve message: " + error.getMessage(), null));
                    latch.countDown();
                }
            });

            latch.await(5, TimeUnit.SECONDS);
            return response.get() != null ? response.get() : new ServerResponse<>(500, "Operation timed out", null);
        } catch (Exception e) {
            logger.error("Error getting message", e);
            return new ServerResponse<>(500, "Internal server error", null);
        }
    }
    
    public ServerResponse<Void> deleteMessage(String token, String messageId) {
        try {
            ServerResponse<User> verifyResponse = firebaseUtil.verifyIdToken(token);
            if (verifyResponse.getStatus() != 200) {
                return new ServerResponse<>(verifyResponse.getStatus(), verifyResponse.getMessage(), null);
            }

            User user = verifyResponse.getData();
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<ServerResponse<Void>> response = new AtomicReference<>();
            
            database.child("messages").child(messageId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Message message = snapshot.getValue(Message.class);
                    if (message == null) {
                        response.set(new ServerResponse<>(404, "Message not found", null));
                        latch.countDown();
                        return;
                    }

                    if (!message.getSenderId().equals(user.getUid())) {
                        response.set(new ServerResponse<>(403, "Not authorized to delete this message", null));
                        latch.countDown();
                        return;
                    }

                    database.child("messages").child(messageId).removeValue((error, ref) -> {
                        if (error != null) {
                            response.set(new ServerResponse<>(500, "Failed to delete message: " + error.getMessage(), null));
                        } else {
                            response.set(new ServerResponse<>(200, "Message deleted successfully", null));
                        }
                        latch.countDown();
                    });
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    response.set(new ServerResponse<>(500, "Failed to delete message: " + error.getMessage(), null));
                    latch.countDown();
                }
            });

            latch.await(5, TimeUnit.SECONDS);
            return response.get() != null ? response.get() : new ServerResponse<>(500, "Operation timed out", null);
        } catch (Exception e) {
            logger.error("Error deleting message", e);
            return new ServerResponse<>(500, "Internal server error", null);
        }
    }
}