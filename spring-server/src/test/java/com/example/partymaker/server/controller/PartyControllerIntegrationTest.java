package com.example.partymaker.server.controller;

import com.example.partymaker.server.model.Location;
import com.example.partymaker.server.model.Party;
import com.example.partymaker.server.model.ServerResponse;
import com.example.partymaker.server.model.User;
import com.example.partymaker.server.service.PartyService;
import com.example.partymaker.server.util.FirebaseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PartyControllerIntegrationTest {
    private MockMvc mockMvc;

    @Mock
    private PartyService partyService;

    @Mock
    private FirebaseUtil firebaseUtil;

    @InjectMocks
    private PartyController partyController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(partyController).build();
    }

    @Test
    public void testCreateParty() throws Exception {
        Party party = new Party();
        party.setName("Test Party");
        party.setDescription("Test Description");
        Location location = new Location();
        location.setLatitude(32.0853);
        location.setLongitude(34.7818);
        location.setAddress("Test Address");
        party.setLocation(location);
        party.setParticipants(new ArrayList<>());

        User user = new User();
        user.setUid("testUserId");
        ServerResponse<User> verifyResponse = new ServerResponse<>(200, "Success", user);
        when(firebaseUtil.verifyIdToken("testToken")).thenReturn(verifyResponse);

        ServerResponse<Party> response = new ServerResponse<>(200, "Party created successfully", party);
        when(partyService.createParty(eq("testToken"), any(Party.class))).thenReturn(response);

        mockMvc.perform(post("/api/parties")
                .header("Authorization", "testToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test Party\",\"description\":\"Test Description\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Party created successfully"));
    }

    @Test
    public void testGetParty() throws Exception {
        Party party = new Party();
        party.setId("testPartyId");
        party.setName("Test Party");

        ServerResponse<Party> response = new ServerResponse<>(200, "Party retrieved successfully", party);
        when(partyService.getParty("testToken", "testPartyId")).thenReturn(response);

        mockMvc.perform(get("/api/parties/testPartyId")
                .header("Authorization", "testToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Party retrieved successfully"));
    }

    @Test
    public void testDeleteParty() throws Exception {
        ServerResponse<Void> response = new ServerResponse<>(200, "Party deleted successfully", null);
        when(partyService.deleteParty("testToken", "testPartyId")).thenReturn(response);

        mockMvc.perform(delete("/api/parties/testPartyId")
                .header("Authorization", "testToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Party deleted successfully"));
    }

    @Test
    public void testJoinParty() throws Exception {
        Party party = new Party();
        party.setId("testPartyId");
        party.setName("Test Party");
        Location location = new Location();
        location.setLatitude(32.0853);
        location.setLongitude(34.7818);
        party.setLocation(location);

        ServerResponse<Party> response = new ServerResponse<>(200, "Successfully joined party", party);
        when(partyService.joinParty("testToken", "testPartyId")).thenReturn(response);

        mockMvc.perform(post("/api/parties/testPartyId/join")
                .header("Authorization", "testToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully joined party"));
    }

    @Test
    public void testLeaveParty() throws Exception {
        Party party = new Party();
        party.setId("testPartyId");
        party.setName("Test Party");
        Location location = new Location();
        location.setLatitude(32.0853);
        location.setLongitude(34.7818);
        party.setLocation(location);

        ServerResponse<Party> response = new ServerResponse<>(200, "Successfully left party", party);
        when(partyService.leaveParty("testToken", "testPartyId")).thenReturn(response);

        mockMvc.perform(post("/api/parties/testPartyId/leave")
                .header("Authorization", "testToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully left party"));
    }

    @Test
    public void testSearchPartiesByLocation() throws Exception {
        Party party = new Party();
        party.setId("testPartyId");
        party.setName("Test Party");
        Location partyLocation = new Location();
        partyLocation.setLatitude(32.0853);
        partyLocation.setLongitude(34.7818);
        party.setLocation(partyLocation);

        Location searchLocation = new Location();
        searchLocation.setLatitude(32.0853);
        searchLocation.setLongitude(34.7818);

        ServerResponse<List<Party>> response = new ServerResponse<>(200, "Parties retrieved successfully", Arrays.asList(party));
        when(partyService.searchPartiesByLocation(eq("testToken"), any(Location.class), eq(5.0))).thenReturn(response);

        mockMvc.perform(get("/api/parties/search")
                .header("Authorization", "testToken")
                .param("latitude", "32.0853")
                .param("longitude", "34.7818")
                .param("radius", "5.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Parties retrieved successfully"));
    }

    @Test
    public void testGetUpcomingParties() throws Exception {
        Party party = new Party();
        party.setId("testPartyId");
        party.setName("Test Party");

        ServerResponse<List<Party>> response = new ServerResponse<>(200, "Upcoming parties retrieved successfully", Arrays.asList(party));
        when(partyService.getUpcomingParties("testToken")).thenReturn(response);

        mockMvc.perform(get("/api/parties/upcoming")
                .header("Authorization", "testToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Upcoming parties retrieved successfully"));
    }

    @Test
    public void testGetUserParties() throws Exception {
        Party party = new Party();
        party.setId("testPartyId");
        party.setName("Test Party");

        ServerResponse<List<Party>> response = new ServerResponse<>(200, "User parties retrieved successfully", Arrays.asList(party));
        when(partyService.getUserParties("testToken")).thenReturn(response);

        mockMvc.perform(get("/api/parties/user")
                .header("Authorization", "testToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("User parties retrieved successfully"));
    }

    @Test
    public void testGetHostParties() throws Exception {
        Party party = new Party();
        party.setId("testPartyId");
        party.setName("Test Party");

        ServerResponse<List<Party>> response = new ServerResponse<>(200, "Host parties retrieved successfully", Arrays.asList(party));
        when(partyService.getHostParties("testToken")).thenReturn(response);

        mockMvc.perform(get("/api/parties/host")
                .header("Authorization", "testToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Host parties retrieved successfully"));
    }
}