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
        log.info("Zapisuje pliki do S3 dla requesta: {}", requestId);
        List<String> filenames = new ArrayList<>();
        for (MultipartFile file : files) {
            if(file == null || file.isEmpty()){
                continue;
            }
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().replace("_","-") : UUID.randomUUID().toString();
            String newFilename = requestId + "_" + filename;
            s3Service.uploadFile(newFilename, file.getInputStream(), file.getSize());
            filenames.add(filename);
        }
        return filenames;

    }
}
