package org.example.services;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
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

        } catch (AmazonServiceException e) {
            throw new IllegalStateException("S3 aws error", e);

        } catch (SdkClientException e) {
            throw new IllegalStateException("S3 sdk error", e);
        }
    }

    public S3Object downloadFile(String fileName) {
        return amazonS3.getObject(bucketName, fileName);
    }
}