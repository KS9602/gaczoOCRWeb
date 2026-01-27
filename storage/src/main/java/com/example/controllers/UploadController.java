package com.example.controllers;

import com.example.kafka.OcrProducer;
import com.example.services.ControllerUtils;
import com.example.services.UploadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/uploads")
public class UploadController {

    private static final Logger log = LoggerFactory.getLogger(UploadController.class);

    private final UploadService uploadService;
    private final OcrProducer ocrProducer;
    private final ObjectMapper objectMapper;
    private final ControllerUtils controllerUtils;

    public UploadController(UploadService uploadService, OcrProducer ocrProducer, ObjectMapper objectMapper, ControllerUtils controllerUtils) {
        this.uploadService = uploadService;
        this.ocrProducer = ocrProducer;
        this.objectMapper = objectMapper;
        this.controllerUtils = controllerUtils;
    }

//    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/invoice")
    public ResponseEntity<?> uploadPdf(@RequestParam("files")  MultipartFile[] files, @RequestParam("margin") String margin) {
        for (MultipartFile file : files) {
            if (!"application/pdf".equals(file.getContentType()) || !controllerUtils.isPdf(file)) {
                return ResponseEntity.badRequest().body("Plik: " + file.getOriginalFilename() + " nie jest PDF-em");
            }
        }
        try {
            String requestId = UUID.randomUUID().toString();
            List<String> filenames = uploadService.saveS3(files, requestId);
            Map<String, Object> payloadMap = Map.of(
                    "requestId", requestId,
                    "filenames", filenames,
                    "margin", margin
            );
            String payload = objectMapper.writeValueAsString(payloadMap);
            ocrProducer.sendMessageOcr(payload);
            return ResponseEntity.ok(Map.of("requestId", requestId));
        } catch (Exception e) {
            log.error("Blad przy zapisie pliku", e);
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }



}
