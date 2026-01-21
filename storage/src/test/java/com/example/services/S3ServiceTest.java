package com.example.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private AmazonS3 amazonS3;

    @InjectMocks
    private S3Service s3Service;

    private final String bucketName = "test-bucket";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Service, "bucketName", bucketName);
    }

    @Test
    void uploadFile_shouldCallPutObject_whenSuccessful() {
        String key = "test-key";
        InputStream inputStream = new ByteArrayInputStream("content".getBytes());
        long contentLength = 7;

        s3Service.uploadFile(key, inputStream, contentLength);

        verify(amazonS3).putObject(eq(bucketName), eq(key), eq(inputStream), any(ObjectMetadata.class));
    }

    @Test
    void uploadFile_shouldThrowAmazonS3Exception_whenAmazonS3Fails() {
        String key = "test-key";
        InputStream inputStream = new ByteArrayInputStream("content".getBytes());
        
        when(amazonS3.putObject(eq(bucketName), eq(key), eq(inputStream), any(ObjectMetadata.class)))
                .thenThrow(new RuntimeException("S3 Error"));

        assertThrows(AmazonS3Exception.class, () -> s3Service.uploadFile(key, inputStream, 7));
    }

    @Test
    void downloadFile_shouldReturnS3Object() {
        String fileName = "test-file";
        S3Object mockS3Object = mock(S3Object.class);
        when(amazonS3.getObject(bucketName, fileName)).thenReturn(mockS3Object);

        S3Object result = s3Service.downloadFile(fileName);

        assertEquals(mockS3Object, result);
        verify(amazonS3).getObject(bucketName, fileName);
    }
}
