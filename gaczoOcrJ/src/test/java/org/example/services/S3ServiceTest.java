package org.example.services;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class S3ServiceTest {

    private AmazonS3 amazonS3;
    private S3Service s3Service;
    private final String bucketName = "test-bucket";

    @BeforeEach
    void setUp() {
        amazonS3 = mock(AmazonS3.class);
        s3Service = new S3Service(amazonS3, bucketName);
    }

    @Test
    void shouldUploadFileSuccessfully() {
        String key = "test.txt";
        InputStream inputStream = new ByteArrayInputStream("content".getBytes());
        long contentLength = 7;

        s3Service.uploadFile(key, inputStream, contentLength);

        verify(amazonS3).putObject(eq(bucketName), eq(key), eq(inputStream), any(ObjectMetadata.class));
    }

    @Test
    void shouldThrowExceptionWhenUploadFails() {
        String key = "test.txt";
        InputStream inputStream = new ByteArrayInputStream("content".getBytes());
        
        when(amazonS3.putObject(any(), any(), any(), any())).thenThrow(new AmazonServiceException("S3 error"));

        assertThrows(IllegalStateException.class, () -> s3Service.uploadFile(key, inputStream, 7));
    }

    @Test
    void shouldDownloadFileSuccessfully() {
        String fileName = "test.txt";
        S3Object s3Object = mock(S3Object.class);
        when(amazonS3.getObject(bucketName, fileName)).thenReturn(s3Object);

        S3Object result = s3Service.downloadFile(fileName);

        assertEquals(s3Object, result);
        verify(amazonS3).getObject(bucketName, fileName);
    }
}
