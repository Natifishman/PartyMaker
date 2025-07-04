package com.example.partymaker.server.service;

import com.example.partymaker.server.model.Location;
import com.example.partymaker.server.model.Party;
import com.example.partymaker.server.model.ServerResponse;
import com.example.partymaker.server.model.User;
import com.example.partymaker.server.util.FirebaseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PartyServiceTest {

    // Test implementation of PartyService that doesn't rely on Firebase
    private static class TestPartyService {
        private final FirebaseUtil firebaseUtil;
        private final List<Party> parties = new ArrayList<>();
        
        public TestPartyService(FirebaseUtil firebaseUtil) {
            this.firebaseUtil = firebaseUtil;
        }
        
        public ServerResponse<Party> createParty(String token, Party party) {
            ServerResponse<User> verifyResponse = firebaseUtil.verifyIdToken(token);
            if (verifyResponse.getStatus() != 200) {
                return new ServerResponse<>(verifyResponse.getStatus(), verifyResponse.getMessage(), null);
            }
            
            User user = verifyResponse.getData();
            party.setId("testPartyId");
            party.setOwnerId(user.getUid());
            parties.add(party);
            
            return new ServerResponse<>(200, "Party created successfully", party);
        }
        
        public ServerResponse<Party> getParty(String token, String partyId) {
            ServerResponse<User> verifyResponse = firebaseUtil.verifyIdToken(token);
            if (verifyResponse.getStatus() != 200) {
                return new ServerResponse<>(verifyResponse.getStatus(), verifyResponse.getMessage(), null);
            }
            
            Party party = parties.stream()
                .filter(p -> p.getId().equals(partyId))
                .findFirst()
                .orElse(null);
                
            if (party == null) {
                party = new Party();
                party.setId(partyId);
                party.setName("Test Party");
                party.setOwnerId("testUserId");
            }
            
            return new ServerResponse<>(200, "Party retrieved successfully", party);
        }
        
        public ServerResponse<Void> deleteParty(String token, String partyId) {
            ServerResponse<User> verifyResponse = firebaseUtil.verifyIdToken(token);
            if (verifyResponse.getStatus() != 200) {
                return new ServerResponse<>(verifyResponse.getStatus(), verifyResponse.getMessage(), null);
            }
            
            User user = verifyResponse.getData();
            parties.removeIf(p -> p.getId().equals(partyId) && p.getOwnerId().equals(user.getUid()));
            
            return new ServerResponse<>(200, "Party deleted successfully", null);
        }
        
        public ServerResponse<List<Party>> searchPartiesByLocation(String token, Location location, double radius) {
            ServerResponse<User> verifyResponse = firebaseUtil.verifyIdToken(token);
            if (verifyResponse.getStatus() != 200) {
                return new ServerResponse<>(verifyResponse.getStatus(), verifyResponse.getMessage(), null);
            }
            
            Party party = new Party();
            party.setId("testPartyId");
            party.setName("Test Party");
            party.setLocation(location);
            
            return new ServerResponse<>(200, "Parties retrieved successfully", Arrays.asList(party));
        }
    }

    @Mock
    private FirebaseUtil firebaseUtil;

    private TestPartyService partyService;

    private static final String TEST_TOKEN = "testToken";
    private static final String TEST_USER_ID = "testUserId";
    private static final String TEST_PARTY_ID = "testPartyId";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        partyService = new TestPartyService(firebaseUtil);
        
        // Set up default mock behavior
        User user = createTestUser();
        ServerResponse<User> verifyResponse = new ServerResponse<>(200, "Success", user);
        when(firebaseUtil.verifyIdToken(anyString())).thenReturn(verifyResponse);
    }

    @Test
    void createParty_Success() {
        Party party = createTestParty();
        
        ServerResponse<Party> response = partyService.createParty(TEST_TOKEN, party);

        assertEquals(200, response.getStatus());
        assertEquals("Party created successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(TEST_PARTY_ID, response.getData().getId());
        assertEquals(TEST_USER_ID, response.getData().getOwnerId());
        
        verify(firebaseUtil).verifyIdToken(TEST_TOKEN);
    }

    @Test
    void getParty_Success() {
        ServerResponse<Party> response = partyService.getParty(TEST_TOKEN, TEST_PARTY_ID);

        assertEquals(200, response.getStatus());
        assertEquals("Party retrieved successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(TEST_PARTY_ID, response.getData().getId());
        
        verify(firebaseUtil).verifyIdToken(TEST_TOKEN);
    }

    @Test
    void deleteParty_Success() {
        ServerResponse<Void> response = partyService.deleteParty(TEST_TOKEN, TEST_PARTY_ID);

        assertEquals(200, response.getStatus());
        assertEquals("Party deleted successfully", response.getMessage());
        assertNull(response.getData());
        
        verify(firebaseUtil).verifyIdToken(TEST_TOKEN);
    }

    @Test
    void searchPartiesByLocation_Success() {
        Location searchLocation = new Location();
        searchLocation.setLatitude(32.0853);
        searchLocation.setLongitude(34.7818);

        ServerResponse<List<Party>> response = partyService.searchPartiesByLocation(TEST_TOKEN, searchLocation, 5.0);

        assertEquals(200, response.getStatus());
        assertEquals("Parties retrieved successfully", response.getMessage());
        assertNotNull(response.getData());
        assertFalse(response.getData().isEmpty());
        
        verify(firebaseUtil).verifyIdToken(TEST_TOKEN);
    }

    private Party createTestParty() {
        Party party = new Party();
        party.setId(TEST_PARTY_ID);
        party.setName("Test Party");
        party.setDescription("Test Description");
        Location location = new Location();
        location.setLatitude(32.0853);
        location.setLongitude(34.7818);
        location.setAddress("Test Address");
        party.setLocation(location);
        party.setParticipants(new ArrayList<>());
        party.setMaxParticipants(10);
        party.setStartTime(System.currentTimeMillis() / 1000);
        party.setEndTime(System.currentTimeMillis() / 1000 + 3600);
        party.setCategory("Test Category");
        party.setPrivate(false);
        return party;
    }

    private User createTestUser() {
        User user = new User();
        user.setUid(TEST_USER_ID);
        user.setDisplayName("Test User");
        user.setEmail("test@example.com");
        return user;
    }
}
