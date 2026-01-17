package com.example.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
public class OcrListener {

    private static final Logger log = LoggerFactory.getLogger(OcrListener.class);

    private final ObjectMapper objectMapper;

    public OcrListener( ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${kafka.topics.ocr.download}", groupId = "${kafka.consumer.group-id}")
    public void handleMessageOcrEnd(String message) throws JsonProcessingException {
        log.info("Wiadomosc: {}", message);
    }
}
