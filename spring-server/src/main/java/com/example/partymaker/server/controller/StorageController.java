package com.example.partymaker.server.controller;

import com.example.partymaker.server.model.ServerResponse;
import com.example.partymaker.server.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller for handling file storage operations
 */
@RestController
@RequestMapping("/api/storage")
public class StorageController {
    private static final Logger logger = LoggerFactory.getLogger(StorageController.class);
    
    private final StorageService storageService;
    
    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }
    
    /**
     * Upload a file
     */
    @PostMapping("/upload")
    public ResponseEntity<ServerResponse<String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("path") String path,
            @RequestHeader("Authorization") String token) {
        logger.info("Uploading file to path: {}", path);
        ServerResponse<String> response = storageService.uploadFile(file, path, token);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a file
     */
    @DeleteMapping("/delete")
    public ResponseEntity<ServerResponse<Void>> deleteFile(
            @RequestParam("path") String path,
            @RequestHeader("Authorization") String token) {
        logger.info("Deleting file at path: {}", path);
        ServerResponse<Void> response = storageService.deleteFile(path, token);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Download a file
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(
            @RequestParam("path") String path,
            @RequestHeader("Authorization") String token) {
        logger.info("Downloading file from path: {}", path);
        ServerResponse<byte[]> response = storageService.downloadFile(path, token);
        if (!"success".equals(response.getMessage()) || response.getData() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(response.getData());
    }
    
    /**
     * Get download URL for a file
     */
    @GetMapping("/url")
    public ResponseEntity<ServerResponse<String>> getDownloadUrl(
            @RequestParam("path") String path,
            @RequestHeader("Authorization") String token) {
        logger.info("Getting download URL for path: {}", path);
        ServerResponse<String> response = storageService.getDownloadUrl(path, token);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if a file exists in storage
     */
    @GetMapping("/exists")
    public ResponseEntity<ServerResponse<Boolean>> checkFileExists(
            @RequestParam("path") String path,
            @RequestHeader("Authorization") String token) {
        logger.info("Checking if file exists at path: {}", path);
        ServerResponse<Boolean> response = storageService.checkFileExists(path, token);
        return ResponseEntity.ok(response);
    }
    
    /**
     * List files in a directory
     */
    @GetMapping("/list")
    public ResponseEntity<ServerResponse<List<String>>> listFiles(
            @RequestParam("path") String path,
            @RequestHeader("Authorization") String token) {
        logger.info("Listing files in path: {}", path);
        ServerResponse<List<String>> response = storageService.listFiles(path, token);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Health check endpoint
     * 
     * @return Response with service status
     */
    @GetMapping("/health")
    public ResponseEntity<ServerResponse<String>> health() {
        logger.info("Health check requested");
        return ResponseEntity.ok(new ServerResponse<>("success", "Storage service is healthy"));
    }
}