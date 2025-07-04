package com.example.partymaker.server.controller;

import com.example.partymaker.server.model.Location;
import com.example.partymaker.server.model.Party;
import com.example.partymaker.server.model.ServerResponse;
import com.example.partymaker.server.service.PartyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * בקר לטיפול במסיבות
 */
@RestController
@RequestMapping("/api/parties")
@CrossOrigin(origins = "*")
public class PartyController {
    private static final Logger logger = LoggerFactory.getLogger(PartyController.class);
    
    private final PartyService partyService;
    
    @Autowired
    public PartyController(PartyService partyService) {
        this.partyService = partyService;
    }
    
    /**
     * יוצר מסיבה חדשה
     */
    @PostMapping
    public ResponseEntity<ServerResponse<Party>> createParty(
            @RequestHeader("Authorization") String token,
            @RequestBody Party party) {
        logger.info("Creating party: {}", party.getName());
        ServerResponse<Party> response = partyService.createParty(token, party);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    /**
     * מקבל את כל המסיבות
     */
    @GetMapping
    public ResponseEntity<ServerResponse<List<Party>>> listParties(
            @RequestHeader("Authorization") String token) {
        logger.info("Listing all parties");
        ServerResponse<List<Party>> response = partyService.listParties(token);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    /**
     * מקבל מסיבה לפי מזהה
     */
    @GetMapping("/{partyId}")
    public ResponseEntity<ServerResponse<Party>> getParty(
            @RequestHeader("Authorization") String token,
            @PathVariable String partyId) {
        logger.info("Getting party with id: {}", partyId);
        ServerResponse<Party> response = partyService.getParty(token, partyId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    /**
     * מעדכן מסיבה קיימת
     */
    @PutMapping("/{partyId}")
    public ResponseEntity<ServerResponse<Party>> updateParty(
            @RequestHeader("Authorization") String token,
            @PathVariable String partyId,
            @RequestBody Party party) {
        logger.info("Updating party with id: {}", partyId);
        ServerResponse<Party> response = partyService.updateParty(token, partyId, party);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    /**
     * מוחק מסיבה
     */
    @DeleteMapping("/{partyId}")
    public ResponseEntity<ServerResponse<Void>> deleteParty(
            @RequestHeader("Authorization") String token,
            @PathVariable String partyId) {
        logger.info("Deleting party with id: {}", partyId);
        ServerResponse<Void> response = partyService.deleteParty(token, partyId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    /**
     * מוסיף משתתף למסיבה
     */
    @PostMapping("/{partyId}/join")
    public ResponseEntity<ServerResponse<Party>> joinParty(
            @RequestHeader("Authorization") String token,
            @PathVariable String partyId) {
        logger.info("User joining party: {}", partyId);
        ServerResponse<Party> response = partyService.joinParty(token, partyId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    /**
     * מסיר משתתף ממסיבה
     */
    @PostMapping("/{partyId}/leave")
    public ResponseEntity<ServerResponse<Party>> leaveParty(
            @RequestHeader("Authorization") String token,
            @PathVariable String partyId) {
        logger.info("User leaving party: {}", partyId);
        ServerResponse<Party> response = partyService.leaveParty(token, partyId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    /**
     * מחפש מסיבות לפי מיקום ורדיוס
     */
    @GetMapping("/search")
    public ResponseEntity<ServerResponse<List<Party>>> searchPartiesByLocation(
            @RequestHeader("Authorization") String token,
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double radius) {
        logger.info("Searching parties near location: ({}, {})", latitude, longitude);
        Location location = new Location();
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        ServerResponse<List<Party>> response = partyService.searchPartiesByLocation(token, location, radius);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    /**
     * מקבל מסיבות עתידיות
     */
    @GetMapping("/upcoming")
    public ResponseEntity<ServerResponse<List<Party>>> getUpcomingParties(
            @RequestHeader("Authorization") String token) {
        logger.info("Getting upcoming parties");
        ServerResponse<List<Party>> response = partyService.getUpcomingParties(token);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    @GetMapping("/user")
    public ResponseEntity<ServerResponse<List<Party>>> getUserParties(
            @RequestHeader("Authorization") String token) {
        logger.info("Getting parties for user");
        ServerResponse<List<Party>> response = partyService.getUserParties(token);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    @GetMapping("/host")
    public ResponseEntity<ServerResponse<List<Party>>> getHostParties(
            @RequestHeader("Authorization") String token) {
        logger.info("Getting parties hosted by");
        ServerResponse<List<Party>> response = partyService.getHostParties(token);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
} 