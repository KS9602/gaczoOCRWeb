package com.example.services;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class ControllerUtilsTest {

    private final ControllerUtils controllerUtils = new ControllerUtils();

    @Test
    void isPdf_shouldReturnTrue_whenFileIsPdf() {
        byte[] pdfContent = "%PDF-1.4\ncontent".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", pdfContent);

        assertTrue(controllerUtils.isPdf(file));
    }

    @Test
    void isPdf_shouldReturnFalse_whenFileIsNotPdf() {
        byte[] textContent = "not a pdf".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", textContent);

        assertFalse(controllerUtils.isPdf(file));
    }

    @Test
    void isPdf_shouldReturnFalse_whenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        assertFalse(controllerUtils.isPdf(file));
    }

    @Test
    void isPdf_shouldReturnFalse_whenFileIsShort() {
        byte[] shortContent = "%PDF".getBytes(); // Tylko 4 bajty, a szukamy 5
        MockMultipartFile file = new MockMultipartFile("file", "short.pdf", "application/pdf", shortContent);

        assertFalse(controllerUtils.isPdf(file));
    }
}
