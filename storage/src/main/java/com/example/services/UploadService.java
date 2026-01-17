package com.example.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class UploadService {

    private static final Logger log = LoggerFactory.getLogger(UploadService.class);

    private final S3Service s3Service;


    public UploadService(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    public List<String> saveS3(MultipartFile[] files, String requestId) throws IOException{

        List<String> filenames = new ArrayList<>();
        for (MultipartFile file : files) {
            String filename = requestId + "_" + file.getOriginalFilename().replace("_","-");
            s3Service.uploadFile(filename, file.getInputStream(), file.getSize());
            filenames.add(filename);
        }
        return filenames;

    }
}
