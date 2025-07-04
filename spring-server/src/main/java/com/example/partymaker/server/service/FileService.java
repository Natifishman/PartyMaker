package com.example.partymaker.server.service;

import com.example.partymaker.server.config.StorageConfig;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class FileService {

    private final StorageConfig storageConfig;

    @Autowired
    public FileService(StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
    }

    public String uploadFile(MultipartFile file, String directory) {
        try {
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            String fullPath = directory + "/" + fileName;
            
            Storage storage = storageConfig.getStorage();
            String bucketName = storageConfig.getBucketName();
            
            BlobId blobId = BlobId.of(bucketName, fullPath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();
            
            Blob blob = storage.create(blobInfo, file.getBytes());
            
            return blob.getName();
        } catch (IOException e) {
            throw new RuntimeException("שגיאה בהעלאת קובץ: " + e.getMessage(), e);
        }
    }

    public String getFileUrl(String filePath) {
        Storage storage = storageConfig.getStorage();
        String bucketName = storageConfig.getBucketName();
        
        BlobId blobId = BlobId.of(bucketName, filePath);
        Blob blob = storage.get(blobId);
        
        if (blob == null) {
            return null;
        }
        
        // יצירת URL עם חתימה שתקף ל-24 שעות
        return blob.signUrl(24, TimeUnit.HOURS).toString();
    }

    public boolean deleteFile(String filePath) {
        Storage storage = storageConfig.getStorage();
        String bucketName = storageConfig.getBucketName();
        
        BlobId blobId = BlobId.of(bucketName, filePath);
        return storage.delete(blobId);
    }

    public List<String> listFiles(String prefix) {
        Storage storage = storageConfig.getStorage();
        String bucketName = storageConfig.getBucketName();
        
        List<String> fileNames = new ArrayList<>();
        
        // קבלת כל הקבצים בתיקייה
        for (Blob blob : storage.list(bucketName, Storage.BlobListOption.prefix(prefix)).iterateAll()) {
            fileNames.add(blob.getName());
        }
        
        return fileNames;
    }

    public String generateUniqueFileName(String originalFileName) {
        return UUID.randomUUID().toString() + "_" + originalFileName;
    }
} 