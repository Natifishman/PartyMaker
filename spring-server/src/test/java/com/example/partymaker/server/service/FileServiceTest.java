package com.example.partymaker.server.service;

import com.example.partymaker.server.config.StorageConfig;
import com.google.cloud.storage.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class FileServiceTest {

    @Mock
    private Storage storage;

    @Mock
    private StorageConfig storageConfig;

    @Mock
    private Bucket bucket;

    @Mock
    private BlobInfo blobInfo;

    @Mock
    private Blob blob;

    @InjectMocks
    private FileService fileService;

    private MockMultipartFile testFile;
    private final String TEST_BUCKET_NAME = "test-bucket";
    private final String TEST_FILE_NAME = "test-file.jpg";
    private final String TEST_FILE_PATH = "users/123/test-file.jpg";

    @BeforeEach
    public void setUp() {
        testFile = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );
        
        when(storageConfig.getStorage()).thenReturn(storage);
        when(storageConfig.getBucketName()).thenReturn(TEST_BUCKET_NAME);
        
        // Setup mock behavior using lenient to avoid unnecessary stubbing issues
        lenient().when(storage.get(anyString())).thenReturn(bucket);
        lenient().when(storage.create(any(BlobInfo.class), any(byte[].class))).thenReturn(blob);
    }

    @Test
    public void uploadFile_Success() throws IOException {
        // Setup
        when(blob.getName()).thenReturn(TEST_FILE_NAME);
        
        // Execute
        String result = fileService.uploadFile(testFile, "users/123");
        
        // Verify
        assertNotNull(result);
        assertTrue(result.contains(TEST_FILE_NAME));
        verify(storage).create(any(BlobInfo.class), any(byte[].class));
    }

    @Test
    public void uploadFile_IOException() {
        // Setup
        when(storage.create(any(BlobInfo.class), any(byte[].class))).thenThrow(new RuntimeException("Upload error"));
        
        // Execute and Verify
        Exception exception = assertThrows(RuntimeException.class, () -> {
            fileService.uploadFile(testFile, "users/123");
        });
        
        assertTrue(exception.getMessage().contains("Upload error"));
    }

    @Test
    public void deleteFile_Success() {
        // Setup
        when(storage.delete(any(BlobId.class))).thenReturn(true);
        
        // Execute
        boolean result = fileService.deleteFile(TEST_FILE_PATH);
        
        // Verify
        assertTrue(result);
        verify(storage).delete(any(BlobId.class));
    }

    @Test
    public void deleteFile_Failure() {
        // Setup
        when(storage.delete(any(BlobId.class))).thenReturn(false);
        
        // Execute
        boolean result = fileService.deleteFile(TEST_FILE_PATH);
        
        // Verify
        assertFalse(result);
        verify(storage).delete(any(BlobId.class));
    }

    @Test
    public void getFileUrl_Success() {
        // Setup
        String expectedUrl = "https://storage.googleapis.com/" + TEST_BUCKET_NAME + "/" + TEST_FILE_PATH;
        when(storage.get(any(BlobId.class))).thenReturn(blob);
        
        // Use doAnswer instead of mocking URL
        doAnswer(invocation -> {
            return new URL(expectedUrl);
        }).when(blob).signUrl(anyLong(), any(TimeUnit.class));
        
        // Execute
        String result = fileService.getFileUrl(TEST_FILE_PATH);
        
        // Verify
        assertNotNull(result);
        assertEquals(expectedUrl, result);
        verify(storage).get(any(BlobId.class));
    }

    @Test
    public void getFileUrl_NotFound() {
        // Setup
        when(storage.get(any(BlobId.class))).thenReturn(null);
        
        // Execute
        String result = fileService.getFileUrl("users/123/nonexistent.jpg");
        
        // Verify
        assertNull(result);
    }

    @Test
    public void generateUniqueFileName_Success() {
        // Execute
        String result = fileService.generateUniqueFileName("test.jpg");
        
        // Verify
        assertNotNull(result);
        assertTrue(result.endsWith(".jpg"));
        assertNotEquals("test.jpg", result);
    }
}
