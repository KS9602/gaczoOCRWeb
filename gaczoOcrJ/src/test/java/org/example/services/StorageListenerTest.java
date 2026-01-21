package org.example.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class StorageListenerTest {

    private ExcelService excelService;
    private ObjectMapper objectMapper;
    private StorageListener storageListener;

    @BeforeEach
    void setUp() {
        excelService = mock(ExcelService.class);
        objectMapper = new ObjectMapper();
        storageListener = new StorageListener(excelService, objectMapper);
    }

    @Test
    void shouldHandleMessageOcrPdfCorrectly() {
        String requestId = "req-456";
        String margin = "15.0";
        ObjectNode messageNode = objectMapper.createObjectNode();
        messageNode.put("requestId", requestId);
        messageNode.put("margin", margin);
        ArrayNode filenamesNode = messageNode.putArray("filenames");
        filenamesNode.add("file1.pdf");
        filenamesNode.add("file2.pdf");

        String message = messageNode.toString();

        storageListener.handleMessageOcrPdf(message);

        ArgumentCaptor<JsonNode> filenamesCaptor = ArgumentCaptor.forClass(JsonNode.class);
        verify(excelService).generateExcel(eq(requestId), filenamesCaptor.capture(), eq(margin));

        JsonNode capturedFilenames = filenamesCaptor.getValue();
        assertEquals(2, capturedFilenames.size());
        assertEquals("file1.pdf", capturedFilenames.get(0).asText());
        assertEquals("file2.pdf", capturedFilenames.get(1).asText());
    }

    @Test
    void shouldNotBreakOnInvalidJson() {
        String invalidMessage = "{ invalid json }";

        storageListener.handleMessageOcrPdf(invalidMessage);

        verifyNoInteractions(excelService);
    }
}
