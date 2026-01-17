package org.example.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class StorageProducer {
    private static final Logger log = LoggerFactory.getLogger(StorageProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.ocr.download}")
    private String ocrTopic;

    public StorageProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

//    public void sendMessageExcelDone(Map<String, String> payloadMap) throws JsonProcessingException {
//        String payload = objectMapper.writeValueAsString(payloadMap);
//        kafkaTemplate.send(ocrTopic, payload);
//    }
}
