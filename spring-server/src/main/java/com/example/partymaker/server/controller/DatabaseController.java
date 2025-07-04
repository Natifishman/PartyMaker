package com.example.partymaker.server.controller;

import com.example.partymaker.server.model.ServerResponse;
import com.example.partymaker.server.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DatabaseController {

    private final DatabaseService databaseService;

    @Autowired
    public DatabaseController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    // נתיבי API מקוריים
    @GetMapping("/database/{path}")
    public ResponseEntity<ServerResponse<Object>> getData(@PathVariable String path) {
        ServerResponse<Object> response = databaseService.getData(path);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/database/{path}")
    public ResponseEntity<ServerResponse<Object>> setData(@PathVariable String path, @RequestBody Object data) {
        ServerResponse<Object> response = databaseService.setData(path, data);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping("/database/{path}")
    public ResponseEntity<ServerResponse<Object>> updateData(@PathVariable String path, @RequestBody Object data) {
        ServerResponse<Object> response = databaseService.updateData(path, data);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/database/{path}")
    public ResponseEntity<ServerResponse<Object>> deleteData(@PathVariable String path) {
        ServerResponse<Object> response = databaseService.deleteData(path);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    // נתיבי API חדשים המותאמים לקריאות מצד הלקוח
    @GetMapping("/getValue/{path}")
    public ResponseEntity<ServerResponse> getValue(@PathVariable String path) {
        try {
            return ResponseEntity.ok(databaseService.getValue(path).get());
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(500).body(new ServerResponse("error", e.getMessage()));
        }
    }
    
    @GetMapping("/getChildren/{path}")
    public ResponseEntity<ServerResponse> getChildren(@PathVariable String path) {
        try {
            return ResponseEntity.ok(databaseService.getChildren(path).get());
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(500).body(new ServerResponse("error", e.getMessage()));
        }
    }
    
    @PostMapping("/setValue")
    public ResponseEntity<ServerResponse> setValue(@RequestBody Map<String, Object> request) {
        String path = (String) request.get("path");
        Object value = request.get("value");
        
        if (path == null) {
            return ResponseEntity.badRequest().body(new ServerResponse("error", "Path is required"));
        }
        
        try {
            return ResponseEntity.ok(databaseService.setValue(path, value).get());
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(500).body(new ServerResponse("error", e.getMessage()));
        }
    }
    
    @PostMapping("/updateChildren")
    public ResponseEntity<ServerResponse> updateChildren(@RequestBody Map<String, Object> request) {
        String path = (String) request.get("path");
        @SuppressWarnings("unchecked")
        Map<String, Object> updates = (Map<String, Object>) request.get("value");
        
        if (path == null || updates == null) {
            return ResponseEntity.badRequest().body(new ServerResponse("error", "Path and updates are required"));
        }
        
        try {
            return ResponseEntity.ok(databaseService.updateChildren(path, updates).get());
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(500).body(new ServerResponse("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/database/remove/{path}")
    public ResponseEntity<ServerResponse> removeValue(@PathVariable String path) {
        try {
            return ResponseEntity.ok(databaseService.removeValue(path).get());
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(500).body(new ServerResponse("error", e.getMessage()));
        }
    }
} 