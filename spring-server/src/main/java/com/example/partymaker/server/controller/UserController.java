package com.example.partymaker.server.controller;

import com.example.partymaker.server.model.ServerResponse;
import com.example.partymaker.server.model.User;
import com.example.partymaker.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ServerResponse<User>> getUser(@PathVariable String userId) {
        ServerResponse<User> response = userService.getUser(userId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ServerResponse<User>> updateUser(@PathVariable String userId, @RequestBody User user) {
        user.setUid(userId);
        ServerResponse<User> response = userService.updateUser(user);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ServerResponse<String>> deleteUser(@PathVariable String userId) {
        ServerResponse<String> response = userService.deleteUser(userId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping(value = "/{userId}/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ServerResponse<String>> uploadProfilePicture(@PathVariable String userId, @RequestParam("file") MultipartFile file) {
        ServerResponse<String> response = userService.uploadProfilePicture(userId, file);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
} 