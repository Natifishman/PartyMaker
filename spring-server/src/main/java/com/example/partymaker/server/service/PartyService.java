package com.example.partymaker.server.service;

import com.example.partymaker.server.model.Location;
import com.example.partymaker.server.model.Party;
import com.example.partymaker.server.model.ServerResponse;
import com.example.partymaker.server.model.User;
import com.example.partymaker.server.util.FirebaseUtil;
import com.google.firebase.database.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class PartyService {
    private static final Logger logger = LoggerFactory.getLogger(PartyService.class);
    
    private final FirebaseUtil firebaseUtil;
    private DatabaseReference database;
    
    @Autowired
    public PartyService(FirebaseUtil firebaseUtil, @Value("${firebase.database.url}") String databaseUrl) {
        this.firebaseUtil = firebaseUtil;
        this.database = FirebaseDatabase.getInstance().getReference();
    }
    
    // For testing purposes
    public void setDatabase(DatabaseReference database) {
        this.database = database;
    }
    
    public ServerResponse<Party> createParty(String token, Party party) {
        try {
            ServerResponse<User> verifyResponse = firebaseUtil.verifyIdToken(token);
            if (verifyResponse.getStatus() != 200) {
                return new ServerResponse<>(verifyResponse.getStatus(), verifyResponse.getMessage(), null);
            }

            User user = verifyResponse.getData();
            party.setOwnerId(user.getUid());
            party.setCreatedAt(Instant.now().getEpochSecond());
            
            if (party.getId() == null) {
                party.setId(database.child("parties").push().getKey());
            }

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<ServerResponse<Party>> response = new AtomicReference<>();
            
            database.child("parties").child(party.getId()).setValue(party, (error, ref) -> {
                if (error != null) {
                    response.set(new ServerResponse<>(500, "Failed to create party: " + error.getMessage(), null));
                } else {
                    response.set(new ServerResponse<>(200, "Party created successfully", party));
                }
                latch.countDown();
            });
            
            latch.await(5, TimeUnit.SECONDS);
            return response.get() != null ? response.get() : new ServerResponse<>(500, "Operation timed out", null);
        } catch (Exception e) {
            logger.error("Error creating party", e);
            return new ServerResponse<>(500, "Internal server error", null);
        }
    }
    
    public ServerResponse<Party> updateParty(String token, String partyId, Party updatedParty) {
        try {
            ServerResponse<User> verifyResponse = firebaseUtil.verifyIdToken(token);
            if (verifyResponse.getStatus() != 200) {
                return new ServerResponse<>(verifyResponse.getStatus(), verifyResponse.getMessage(), null);
            }

            User user = verifyResponse.getData();
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<ServerResponse<Party>> response = new AtomicReference<>();
            
            database.child("parties").child(partyId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Party party = snapshot.getValue(Party.class);
                    if (party == null) {
                        response.set(new ServerResponse<>(404, "Party not found", null));
                        latch.countDown();
                        return;
                    }

                    if (!party.getOwnerId().equals(user.getUid())) {
                        response.set(new ServerResponse<>(403, "Not authorized to update this party", null));
                        latch.countDown();
                        return;
                    }

                    // Update only allowed fields
                    if (updatedParty.getName() != null) party.setName(updatedParty.getName());
                    if (updatedParty.getDescription() != null) party.setDescription(updatedParty.getDescription());
                    if (updatedParty.getLocation() != null) party.setLocation(updatedParty.getLocation());
                    if (updatedParty.getLatitude() != 0) party.setLatitude(updatedParty.getLatitude());
                    if (updatedParty.getLongitude() != 0) party.setLongitude(updatedParty.getLongitude());
                    if (updatedParty.getDate() != null) party.setDate(updatedParty.getDate());
                    if (updatedParty.getMaxParticipants() > 0) party.setMaxParticipants(updatedParty.getMaxParticipants());
                    if (updatedParty.getStartTime() != null) party.setStartTime(updatedParty.getStartTime());
                    if (updatedParty.getEndTime() != null) party.setEndTime(updatedParty.getEndTime());
                    if (updatedParty.getImageUrls() != null) party.setImageUrls(updatedParty.getImageUrls());
                    if (updatedParty.getCategory() != null) party.setCategory(updatedParty.getCategory());

                    database.child("parties").child(partyId).setValue(party, (error, ref) -> {
                        if (error != null) {
                            response.set(new ServerResponse<>(500, "Failed to update party: " + error.getMessage(), null));
                        } else {
                            response.set(new ServerResponse<>(200, "Party updated successfully", party));
                        }
                        latch.countDown();
                    });
                }
                
                @Override
                public void onCancelled(DatabaseError error) {
                    response.set(new ServerResponse<>(500, "Failed to update party: " + error.getMessage(), null));
                    latch.countDown();
                }
            });
            
            latch.await(5, TimeUnit.SECONDS);
            return response.get() != null ? response.get() : new ServerResponse<>(500, "Operation timed out", null);
        } catch (Exception e) {
            logger.error("Error updating party", e);
            return new ServerResponse<>(500, "Internal server error", null);
        }
    }
    
    public ServerResponse<Void> deleteParty(String token, String partyId) {
        try {
            ServerResponse<User> verifyResponse = firebaseUtil.verifyIdToken(token);
            if (verifyResponse.getStatus() != 200) {
                return new ServerResponse<>(verifyResponse.getStatus(), verifyResponse.getMessage(), null);
            }

            User user = verifyResponse.getData();
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<ServerResponse<Void>> response = new AtomicReference<>();
            
            database.child("parties").child(partyId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Party party = snapshot.getValue(Party.class);
                    if (party == null) {
                        response.set(new ServerResponse<>(404, "Party not found", null));
                        latch.countDown();
                        return;
                    }

                    if (!party.getOwnerId().equals(user.getUid())) {
                        response.set(new ServerResponse<>(403, "Not authorized to delete this party", null));
                        latch.countDown();
                        return;
                    }

                    database.child("parties").child(partyId).removeValue((error, ref) -> {
                        if (error != null) {
                            response.set(new ServerResponse<>(500, "Failed to delete party: " + error.getMessage(), null));
                        } else {
                            response.set(new ServerResponse<>(200, "Party deleted successfully", null));
                        }
                        latch.countDown();
                    });
                }
                
                @Override
                public void onCancelled(DatabaseError error) {
                    response.set(new ServerResponse<>(500, "Failed to delete party: " + error.getMessage(), null));
                    latch.countDown();
                }
            });
            
            latch.await(5, TimeUnit.SECONDS);
            return response.get() != null ? response.get() : new ServerResponse<>(500, "Operation timed out", null);
        } catch (Exception e) {
            logger.error("Error deleting party", e);
            return new ServerResponse<>(500, "Internal server error", null);
        }
    }
    
    public ServerResponse<Party> joinParty(String token, String partyId) {
        try {
            ServerResponse<User> verifyResponse = firebaseUtil.verifyIdToken(token);
            if (verifyResponse.getStatus() != 200) {
                return new ServerResponse<>(verifyResponse.getStatus(), verifyResponse.getMessage(), null);
            }

            User user = verifyResponse.getData();
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<ServerResponse<Party>> response = new AtomicReference<>();
            
            database.child("parties").child(partyId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Party party = snapshot.getValue(Party.class);
                    if (party == null) {
                        response.set(new ServerResponse<>(404, "Party not found", null));
                        latch.countDown();
                        return;
                    }

                    if (party.getParticipants() == null) {
                        party.setParticipants(new ArrayList<>());
                    }

                    if (party.getParticipants().contains(user.getUid())) {
                        response.set(new ServerResponse<>(400, "User already joined this party", party));
                        latch.countDown();
                        return;
                    }

                    if (party.getMaxParticipants() != null && 
                        party.getParticipants().size() >= party.getMaxParticipants()) {
                        response.set(new ServerResponse<>(400, "Party is full", party));
                        latch.countDown();
                        return;
                    }

                    party.getParticipants().add(user.getUid());
                    database.child("parties").child(partyId).setValue(party, (error, ref) -> {
                        if (error != null) {
                            response.set(new ServerResponse<>(500, "Failed to join party: " + error.getMessage(), null));
                        } else {
                            response.set(new ServerResponse<>(200, "Successfully joined party", party));
                        }
                        latch.countDown();
                    });
                }
                
                @Override
                public void onCancelled(DatabaseError error) {
                    response.set(new ServerResponse<>(500, "Failed to join party: " + error.getMessage(), null));
                    latch.countDown();
                }
            });
            
            latch.await(5, TimeUnit.SECONDS);
            return response.get() != null ? response.get() : new ServerResponse<>(500, "Operation timed out", null);
        } catch (Exception e) {
            logger.error("Error joining party", e);
            return new ServerResponse<>(500, "Internal server error", null);
        }
    }
    
    public ServerResponse<Party> leaveParty(String token, String partyId) {
        try {
            ServerResponse<User> verifyResponse = firebaseUtil.verifyIdToken(token);
            if (verifyResponse.getStatus() != 200) {
                return new ServerResponse<>(verifyResponse.getStatus(), verifyResponse.getMessage(), null);
            }

            User user = verifyResponse.getData();
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<ServerResponse<Party>> response = new AtomicReference<>();
            
            database.child("parties").child(partyId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Party party = snapshot.getValue(Party.class);
                    if (party == null) {
                        response.set(new ServerResponse<>(404, "Party not found", null));
                        latch.countDown();
                        return;
                    }

                    if (party.getParticipants() == null || !party.getParticipants().contains(user.getUid())) {
                        response.set(new ServerResponse<>(400, "User is not a participant of this party", party));
                        latch.countDown();
                        return;
                    }

                    party.getParticipants().remove(user.getUid());
                    database.child("parties").child(partyId).setValue(party, (error, ref) -> {
                        if (error != null) {
                            response.set(new ServerResponse<>(500, "Failed to leave party: " + error.getMessage(), null));
                        } else {
                            response.set(new ServerResponse<>(200, "Successfully left party", party));
                        }
                        latch.countDown();
                    });
                }
                
                @Override
                public void onCancelled(DatabaseError error) {
                    response.set(new ServerResponse<>(500, "Failed to leave party: " + error.getMessage(), null));
                    latch.countDown();
                }
            });
            
            latch.await(5, TimeUnit.SECONDS);
            return response.get() != null ? response.get() : new ServerResponse<>(500, "Operation timed out", null);
        } catch (Exception e) {
            logger.error("Error leaving party", e);
            return new ServerResponse<>(500, "Internal server error", null);
        }
    }
    
    public ServerResponse<List<Party>> listParties(String token) {
        try {
            ServerResponse<User> verifyResponse = firebaseUtil.verifyIdToken(token);
            if (verifyResponse.getStatus() != 200) {
                return new ServerResponse<>(verifyResponse.getStatus(), verifyResponse.getMessage(), null);
            }

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<ServerResponse<List<Party>>> response = new AtomicReference<>();
            
            database.child("parties").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    List<Party> parties = new ArrayList<>();
                    for (DataSnapshot partySnapshot : snapshot.getChildren()) {
                        Party party = partySnapshot.getValue(Party.class);
                        if (party != null) {
                            parties.add(party);
                        }
                    }
                    response.set(new ServerResponse<>(200, "Parties retrieved successfully", parties));
                    latch.countDown();
                }
                
                @Override
                public void onCancelled(DatabaseError error) {
                    response.set(new ServerResponse<>(500, "Failed to retrieve parties: " + error.getMessage(), null));
                    latch.countDown();
                }
            });
            
            latch.await(5, TimeUnit.SECONDS);
            return response.get() != null ? response.get() : new ServerResponse<>(500, "Operation timed out", null);
        } catch (Exception e) {
            logger.error("Error listing parties", e);
            return new ServerResponse<>(500, "Internal server error", null);
        }
    }
    
    public ServerResponse<List<Party>> searchPartiesByLocation(String token, Location location, double radius) {
        try {
            ServerResponse<User> verifyResponse = firebaseUtil.verifyIdToken(token);
            if (verifyResponse.getStatus() != 200) {
                return new ServerResponse<>(verifyResponse.getStatus(), verifyResponse.getMessage(), null);
            }

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<ServerResponse<List<Party>>> response = new AtomicReference<>();
            
            database.child("parties").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    List<Party> parties = new ArrayList<>();
                    for (DataSnapshot partySnapshot : snapshot.getChildren()) {
                        Party party = partySnapshot.getValue(Party.class);
                        if (party != null && party.getLocation() != null && 
                            isWithinRadius(party.getLocation(), location, radius)) {
                            parties.add(party);
                        }
                    }
                    response.set(new ServerResponse<>(200, "Parties retrieved successfully", parties));
                    latch.countDown();
                }
                
                @Override
                public void onCancelled(DatabaseError error) {
                    response.set(new ServerResponse<>(500, "Failed to search parties: " + error.getMessage(), null));
                    latch.countDown();
                }
            });
            
            latch.await(5, TimeUnit.SECONDS);
            return response.get() != null ? response.get() : new ServerResponse<>(500, "Operation timed out", null);
        } catch (Exception e) {
            logger.error("Error searching parties by location", e);
            return new ServerResponse<>(500, "Internal server error", null);
        }
    }
    
    private boolean isWithinRadius(Location partyLocation, Location searchLocation, double radius) {
        double lat1 = Math.toRadians(partyLocation.getLatitude());
        double lon1 = Math.toRadians(partyLocation.getLongitude());
        double lat2 = Math.toRadians(searchLocation.getLatitude());
        double lon2 = Math.toRadians(searchLocation.getLongitude());

        double earthRadius = 6371; // kilometers
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        return distance <= radius;
    }
    
    public ServerResponse<Party> getParty(String token, String partyId) {
        try {
            ServerResponse<User> verifyResponse = firebaseUtil.verifyIdToken(token);
            if (verifyResponse.getStatus() != 200) {
                return new ServerResponse<>(verifyResponse.getStatus(), verifyResponse.getMessage(), null);
            }

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<ServerResponse<Party>> response = new AtomicReference<>();
            
            database.child("parties").child(partyId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Party party = snapshot.getValue(Party.class);
                    if (party == null) {
                        response.set(new ServerResponse<>(404, "Party not found", null));
                    } else {
                        response.set(new ServerResponse<>(200, "Party retrieved successfully", party));
                    }
                    latch.countDown();
                }
                
                @Override
                public void onCancelled(DatabaseError error) {
                    response.set(new ServerResponse<>(500, "Failed to retrieve party: " + error.getMessage(), null));
                    latch.countDown();
                }
            });
            
            latch.await(5, TimeUnit.SECONDS);
            return response.get() != null ? response.get() : new ServerResponse<>(500, "Operation timed out", null);
        } catch (Exception e) {
            logger.error("Error getting party", e);
            return new ServerResponse<>(500, "Internal server error", null);
        }
    }
    
    public ServerResponse<List<Party>> getUpcomingParties(String token) {
        try {
            ServerResponse<User> verifyResponse = firebaseUtil.verifyIdToken(token);
            if (verifyResponse.getStatus() != 200) {
                return new ServerResponse<>(verifyResponse.getStatus(), verifyResponse.getMessage(), null);
            }

            long currentTime = Instant.now().getEpochSecond();
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<ServerResponse<List<Party>>> response = new AtomicReference<>();
            
            database.child("parties")
                    .orderByChild("startTime")
                    .startAt(currentTime)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            List<Party> parties = new ArrayList<>();
                            for (DataSnapshot partySnapshot : snapshot.getChildren()) {
                                Party party = partySnapshot.getValue(Party.class);
                                if (party != null) {
                                    parties.add(party);
                                }
                            }
                            response.set(new ServerResponse<>(200, "Upcoming parties retrieved successfully", parties));
                            latch.countDown();
                        }
                        
                        @Override
                        public void onCancelled(DatabaseError error) {
                            response.set(new ServerResponse<>(500, "Failed to retrieve upcoming parties: " + error.getMessage(), null));
                            latch.countDown();
                        }
                    });

            latch.await(5, TimeUnit.SECONDS);
            return response.get() != null ? response.get() : new ServerResponse<>(500, "Operation timed out", null);
        } catch (Exception e) {
            logger.error("Error getting upcoming parties", e);
            return new ServerResponse<>(500, "Internal server error", null);
        }
    }
    
    public ServerResponse<List<Party>> getUserParties(String token) {
        try {
            ServerResponse<User> verifyResponse = firebaseUtil.verifyIdToken(token);
            if (verifyResponse.getStatus() != 200) {
                return new ServerResponse<>(verifyResponse.getStatus(), verifyResponse.getMessage(), null);
            }

            User user = verifyResponse.getData();
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<ServerResponse<List<Party>>> response = new AtomicReference<>();
            
            database.child("parties").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    List<Party> parties = new ArrayList<>();
                    for (DataSnapshot partySnapshot : snapshot.getChildren()) {
                        Party party = partySnapshot.getValue(Party.class);
                        if (party != null && party.getParticipants() != null && 
                            party.getParticipants().contains(user.getUid())) {
                            parties.add(party);
                        }
                    }
                    response.set(new ServerResponse<>(200, "User parties retrieved successfully", parties));
                    latch.countDown();
                }
                
                @Override
                public void onCancelled(DatabaseError error) {
                    response.set(new ServerResponse<>(500, "Failed to retrieve user parties: " + error.getMessage(), null));
                    latch.countDown();
                }
            });
            
            latch.await(5, TimeUnit.SECONDS);
            return response.get() != null ? response.get() : new ServerResponse<>(500, "Operation timed out", null);
        } catch (Exception e) {
            logger.error("Error getting user parties", e);
            return new ServerResponse<>(500, "Internal server error", null);
        }
    }
    
    public ServerResponse<List<Party>> getHostParties(String token) {
        try {
            ServerResponse<User> verifyResponse = firebaseUtil.verifyIdToken(token);
            if (verifyResponse.getStatus() != 200) {
                return new ServerResponse<>(verifyResponse.getStatus(), verifyResponse.getMessage(), null);
            }

            User user = verifyResponse.getData();
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<ServerResponse<List<Party>>> response = new AtomicReference<>();
            
            database.child("parties")
                    .orderByChild("ownerId")
                    .equalTo(user.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            List<Party> parties = new ArrayList<>();
                            for (DataSnapshot partySnapshot : snapshot.getChildren()) {
                                Party party = partySnapshot.getValue(Party.class);
                                if (party != null) {
                                    parties.add(party);
                                }
                            }
                            response.set(new ServerResponse<>(200, "Host parties retrieved successfully", parties));
                            latch.countDown();
                        }
                        
                        @Override
                        public void onCancelled(DatabaseError error) {
                            response.set(new ServerResponse<>(500, "Failed to retrieve host parties: " + error.getMessage(), null));
                            latch.countDown();
                        }
                    });

            latch.await(5, TimeUnit.SECONDS);
            return response.get() != null ? response.get() : new ServerResponse<>(500, "Operation timed out", null);
        } catch (Exception e) {
            logger.error("Error getting host parties", e);
            return new ServerResponse<>(500, "Internal server error", null);
        }
    }
}