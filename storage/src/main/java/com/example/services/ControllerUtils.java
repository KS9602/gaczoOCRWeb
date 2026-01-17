package com.example.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class ControllerUtils {

    public boolean isPdf(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[5];
            if (is.read(header) != 5) return false;

            String magic = new String(header, StandardCharsets.US_ASCII);
            return magic.startsWith("%PDF-");
        } catch (IOException e) {
            return false;
        }
    }
}
