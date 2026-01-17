package com.example.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OcrProducer {
    private static final Logger log = LoggerFactory.getLogger(OcrProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topics.ocr.upload}")
    private String ocrTopic;

    public OcrProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessageOcr( String message) {
        log.info("Wysylam wiadomosc na topic: {} \n message: {}", ocrTopic, message);
        kafkaTemplate.send(ocrTopic, message);
    }


}
