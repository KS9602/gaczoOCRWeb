package com.example.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket.storage}")
    private String bucketName;

    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public void uploadFile(String key, InputStream inputStream, long contentLength) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(contentLength);
            amazonS3.putObject(bucketName, key, inputStream, metadata);
        } catch (Exception e) {
            throw new AmazonS3Exception("Error S3: " + e.getMessage(), e);
        }
    }

    public S3Object downloadFile(String fileName) {
        return amazonS3.getObject(bucketName, fileName);
    }
}