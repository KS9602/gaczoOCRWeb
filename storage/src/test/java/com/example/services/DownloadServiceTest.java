package com.example.services;

import com.amazonaws.services.s3.model.S3Object;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DownloadServiceTest {

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private DownloadService downloadService;

    @Test
    void downloadExcel_shouldReturnS3ObjectFromS3Service() {
        String fileName = "test.zip";
        S3Object mockS3Object = mock(S3Object.class);
        when(s3Service.downloadFile(fileName)).thenReturn(mockS3Object);

        S3Object result = downloadService.downloadExcel(fileName);

        assertEquals(mockS3Object, result);
        verify(s3Service).downloadFile(fileName);
    }
}
