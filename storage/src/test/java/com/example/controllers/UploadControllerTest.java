package com.example.controllers;

import com.example.kafka.OcrProducer;
import com.example.services.ControllerUtils;
import com.example.services.UploadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UploadController.class)
class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UploadService uploadService;

    @MockBean
    private OcrProducer ocrProducer;

    @MockBean
    private ControllerUtils controllerUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void uploadPdf_shouldReturnOk_whenFilesAreValid() throws Exception {
        MockMultipartFile file = new MockMultipartFile("files", "test.pdf", "application/pdf", "%PDF-1.4".getBytes());
        
        when(controllerUtils.isPdf(any())).thenReturn(true);
        when(uploadService.saveS3(any(), any())).thenReturn(List.of("test.pdf"));

        mockMvc.perform(multipart("/uploads/invoice")
                        .file(file)
                        .param("margin", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").exists());

        verify(uploadService).saveS3(any(), any());
        verify(ocrProducer).sendMessageOcr(anyString());
    }

    @Test
    void uploadPdf_shouldReturnBadRequest_whenFileIsNotPdf() throws Exception {
        MockMultipartFile file = new MockMultipartFile("files", "test.txt", "text/plain", "not a pdf".getBytes());

        when(controllerUtils.isPdf(any())).thenReturn(false);

        mockMvc.perform(multipart("/uploads/invoice")
                        .file(file)
                        .param("margin", "10"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(uploadService);
        verifyNoInteractions(ocrProducer);
    }

    @Test
    void uploadPdf_shouldReturnInternalError_whenUploadServiceFails() throws Exception {
        MockMultipartFile file = new MockMultipartFile("files", "test.pdf", "application/pdf", "%PDF-1.4".getBytes());

        when(controllerUtils.isPdf(any())).thenReturn(true);
        when(uploadService.saveS3(any(), any())).thenThrow(new RuntimeException("S3 error"));

        mockMvc.perform(multipart("/uploads/invoice")
                        .file(file)
                        .param("margin", "10"))
                .andExpect(status().isInternalServerError());
    }
}
