package com.example.controllers;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.services.DownloadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DownloadController.class)
class DownloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DownloadService downloadService;

    @Test
    void downloadExcel_shouldReturnZipFile_whenFileExists() throws Exception {
        String key = "test-key";
        String filename = "test-key.zip";
        
        S3Object s3Object = mock(S3Object.class);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(10);
        
        S3ObjectInputStream inputStream = new S3ObjectInputStream(
                new ByteArrayInputStream("content123".getBytes()), null);

        when(downloadService.downloadExcel(filename)).thenReturn(s3Object);
        when(s3Object.getObjectContent()).thenReturn(inputStream);
        when(s3Object.getObjectMetadata()).thenReturn(metadata);

        mockMvc.perform(get("/downloads/zip/{key}", key))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + filename + "\""))
                .andExpect(content().bytes("content123".getBytes()));
    }

    @Test
    void downloadExcel_shouldReturnNotFound_whenFileDoesNotExist() throws Exception {
        String key = "non-existent";
        String filename = "non-existent.zip";

        when(downloadService.downloadExcel(filename)).thenReturn(null);

        mockMvc.perform(get("/downloads/zip/{key}", key))
                .andExpect(status().isNotFound());
    }

    @Test
    void downloadExcel_shouldReturnInternalError_whenServiceFails() throws Exception {
        String key = "error-key";
        String filename = "error-key.zip";

        when(downloadService.downloadExcel(filename)).thenThrow(new RuntimeException("S3 error"));

        mockMvc.perform(get("/downloads/zip/{key}", key))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("FAILED"));
    }
}
