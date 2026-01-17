package com.example.controllers;

import com.amazonaws.services.s3.model.S3Object;
import com.example.services.DownloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/downloads")
public class DownloadController {

    private static final Logger log = LoggerFactory.getLogger(DownloadController.class);
    private final DownloadService downloadService;

    public DownloadController(DownloadService downloadService) {
        this.downloadService = downloadService;
    }


    @GetMapping(value = "zip/{key}", produces="application/zip")
    public ResponseEntity<?> downloadExcel(@PathVariable String key) {

        String filename = key + ".zip";
        log.info("Download request dla key={}", key);

        try {
            S3Object s3Object = downloadService.downloadExcel(filename);

            if (s3Object == null) {
                log.warn("Nie znaleziono pliku: {}", filename);
                return ResponseEntity.notFound().build();
            }

            InputStreamResource resource = new InputStreamResource(s3Object.getObjectContent());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .contentLength(s3Object.getObjectMetadata().getContentLength())
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\""
                    )
                    .body(resource);

        } catch (Exception e) {
            log.error("Blad przy pobieraniu, key={}", key, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("FAILED");
        }
    }


}
