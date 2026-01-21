package com.example.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadServiceTest {

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private UploadService uploadService;

    @Test
    void saveS3_shouldUploadFilesAndReturnFilenames() throws IOException {
        String requestId = "req-123";
        MockMultipartFile file1 = new MockMultipartFile("files", "test1.pdf", "application/pdf", "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "test_2.pdf", "application/pdf", "content2".getBytes());
        MockMultipartFile[] files = {file1, file2};

        List<String> result = uploadService.saveS3(files, requestId);

        assertEquals(2, result.size());
        assertEquals("test1.pdf", result.get(0));
        assertEquals("test-2.pdf", result.get(1));

        verify(s3Service).uploadFile(eq("req-123_test1.pdf"), any(InputStream.class), eq(8L));
        verify(s3Service).uploadFile(eq("req-123_test-2.pdf"), any(InputStream.class), eq(8L));
    }

    @Test
    void saveS3_shouldSkipEmptyFiles() throws IOException {
        String requestId = "req-123";
        MockMultipartFile emptyFile = new MockMultipartFile("files", "empty.pdf", "application/pdf", new byte[0]);
        MockMultipartFile[] files = {emptyFile};

        List<String> result = uploadService.saveS3(files, requestId);

        assertTrue(result.isEmpty());
        verify(s3Service, never()).uploadFile(any(), any(), anyLong());
    }
}
