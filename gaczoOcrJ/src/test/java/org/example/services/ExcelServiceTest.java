package org.example.services;

import org.example.records.Company1Row;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ExcelServiceTest {

    private S3Service s3Service;
    private ExcelService excelService;

    @BeforeEach
    void setUp() {
        s3Service = mock(S3Service.class);
        excelService = new ExcelService(s3Service);
    }

    @Test
    void shouldBuildExcelFileCorrectly() throws IOException {

        List<String> labels = Arrays.asList("LP", "Nazwa", "Ilość", "Cena", "Netto", "VAT", "Kwota VAT", "Brutto", "Marża");
        Company1Row row = new Company1Row("1", "Produkt A", "10", "5.00", "50.00", "23%", "11.50", "61.50", "0.0");
        List<Company1Row> rows = List.of(row);
        String filename = "test.xlsx";

        byte[] result = excelService.buildExcelFile(rows, filename, labels);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void shouldZipFilesAndUploadToS3() {

        String requestId = "request-123";
        Map<String, byte[]> excelFiles = new HashMap<>();
        excelFiles.put("file1.xlsx", "content1".getBytes());
        excelFiles.put("file2.xlsx", "content2".getBytes());

        excelService.zipFiles(excelFiles, requestId);

        verify(s3Service).uploadFile(eq(requestId + ".zip"), any(InputStream.class), any(Long.class));
    }
}
