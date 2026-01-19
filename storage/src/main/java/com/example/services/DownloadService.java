package com.example.services;

import com.amazonaws.services.s3.model.S3Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DownloadService {

    private static final Logger log = LoggerFactory.getLogger(DownloadService.class);
    private final S3Service s3Service;

    public DownloadService(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    public S3Object downloadExcel(String fileName){
        log.info("Pobieram plik z S3: {}", fileName);
        return s3Service.downloadFile(fileName);
    }
}
