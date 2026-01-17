package org.example.services;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class StorageListener {

    private static final Logger log = LoggerFactory.getLogger(StorageListener.class);
    private final ExcelService excelService;
    private final ObjectMapper objectMapper;

    public StorageListener(ExcelService excelService, ObjectMapper objectMapper) {
        this.excelService = excelService;
        this.objectMapper = objectMapper;
    }


    @KafkaListener(topics = "${kafka.topics.ocr.upload}", groupId = "${kafka.consumer.group-id}")
    public void handleMessageOcrPdf(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String requestId = jsonNode.get("requestId").asText();
            String margin = jsonNode.get("margin").asText();
            JsonNode filenames = jsonNode.get("filenames");

            log.info("filenames: {}", filenames);
            excelService.generateExcel(requestId, filenames, margin);
        } catch (Exception e) {
            log.error("Błąd parsowania JSON", e);
        }
    }
}
