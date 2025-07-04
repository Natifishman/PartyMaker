package com.example.partymaker.server.controller;

import com.example.partymaker.server.model.Message;
import com.example.partymaker.server.model.ServerResponse;
import com.example.partymaker.server.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);
    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<ServerResponse<Message>> sendMessage(
            @RequestHeader("Authorization") String token,
            @RequestBody Message message) {
        ServerResponse<Message> response = messageService.sendMessage(token, message);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/party/{partyId}")
    public ResponseEntity<ServerResponse<List<Message>>> getPartyMessages(
            @RequestHeader("Authorization") String token,
            @PathVariable String partyId) {
        ServerResponse<List<Message>> response = messageService.getPartyMessages(token, partyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    public ResponseEntity<ServerResponse<List<Message>>> getUserMessages(
            @RequestHeader("Authorization") String token) {
        ServerResponse<List<Message>> response = messageService.getUserMessages(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{messageId}/read")
    public ResponseEntity<ServerResponse<Message>> markMessageAsRead(
            @RequestHeader("Authorization") String token,
            @PathVariable String messageId) {
        ServerResponse<Message> response = messageService.markMessageAsRead(token, messageId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<ServerResponse<Void>> deleteMessage(
            @RequestHeader("Authorization") String token,
            @PathVariable String messageId) {
        ServerResponse<Void> response = messageService.deleteMessage(token, messageId);
        return ResponseEntity.ok(response);
    }
}