package org.example.services;

import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.function.IOSupplier;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.clientsOCR.*;
import org.example.config.NipConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ExcelService {
    private static final Logger log = LoggerFactory.getLogger(ExcelService.class);
    private final S3Service s3Service;

    private final Map<String, IOSupplier<OCR<? extends Record>>> NipToName = new HashMap<>();
    private final Map<String, IOSupplier<OCR<? extends Record>>> NipToNameCorrection = new HashMap<>();

    public ExcelService(S3Service s3Service) throws IOException {
        this.s3Service = s3Service;

        this.NipToName.put(NipConfig.nip("company.1.NIP"), Company1OCR::new);
            this.NipToName.put(NipConfig.nip("company.2.NIP"), Company2OCR::new);
            this.NipToName.put(NipConfig.nip("company.3.NIP"), Company3OCR::new);
            this.NipToName.put(NipConfig.nip("company.4.NIP"), Company4OCR::new);
            this.NipToName.put(NipConfig.nip("company.5.NIP"), Company5OCR::new);

            this.NipToNameCorrection.put(NipConfig.nip("company.3.K.NIP"), Company3KOCR::new);

    }

    private OCR<?> getOCRClass(String PDFText) throws IOException {
        for (String key : NipToNameCorrection.keySet()) {
            if (PDFText.contains(key)) {
                log.info("Znaleziono NIP: " + key);
                if(PDFText.contains(NipToNameCorrection.get(key).get().getinvoiceNrPattern())){
                    return NipToNameCorrection.get(key).get();
                }
            }
        }
        for (String key : NipToName.keySet()) {
            if (PDFText.contains(key)) {
                log.info("Znaleziono NIP: " + key);
                return NipToName.get(key).get();
            }
        }
        return null;
    }

    private String getText(PDDocument document) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        stripper.setStartPage(1);
        stripper.setEndPage(1);
        return stripper.getText(document);
    }

        public void generateExcel(String requestId, JsonNode filenames, String margin) throws JsonProcessingException {
        Map<String, byte[]> excelFiles = new LinkedHashMap<>();
        for( int i = 0; i < filenames.size(); i++){
            String filename = filenames.get(i).asText();
            S3Object object = s3Service.downloadFile(filename);
            if(object == null){
                log.warn("Nie znaleziono pliku z S3: " + filename);
                continue;
            }

            try (S3Object s3Object = object;
                 InputStream inputStream = s3Object.getObjectContent();
                 PDDocument document = PDDocument.load(inputStream)) {
                String PDFText = getText(document);
                OCR<?> ocr = getOCRClass(PDFText);
                log.info("Wybrano klase OCR: " + ocr);
                if (ocr == null) {
                    log.warn("Nie znaleziono klasy OCR");
                    continue;
                }
                String newFilename = filenames.get(i).asText().substring(0, filename.length() - 4) + ".xlsx";
                double marginDouble = Double.parseDouble(margin);
                List<?> rows = ocr.processDocument(document, marginDouble);
                byte[] excelBaos = buildExcelFile(rows, newFilename, ocr.getLabels());
                excelFiles.put(newFilename, excelBaos);

            } catch (IOException e) {
                throw new RuntimeException("Błąd podczas przetwarzania pliku z S3", e);
            }
        }
            zipFiles(excelFiles, requestId);
    }

    public void zipFiles(Map<String, byte[]> excelFiles, String requestId) {
        log.info("Rozpoczynam tworzenie zip dla requestId: " + requestId);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (Map.Entry<String, byte[]> entry : excelFiles.entrySet()) {
                String filename = entry.getKey();
                byte[] data = entry.getValue();

                ZipEntry zipEntry = new ZipEntry(filename);
                zos.putNextEntry(zipEntry);
                zos.write(data);
                zos.closeEntry();
            }

            zos.finish();
            byte[] zipBytes = baos.toByteArray();
            s3Service.uploadFile(requestId + ".zip", new ByteArrayInputStream(zipBytes), zipBytes.length);
            log.info("Zakończono tworzenie zip: " + requestId + ".zip");

        } catch (IOException e) {
            throw new RuntimeException("Błąd podczas tworzenia ZIP", e);
        }
    }

    public byte[] buildExcelFile(List<?> rows, String filename, List<String> labels) throws IOException {
        log.info("Rozpoczynam budowe excela");

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Faktura");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < labels.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(labels.get(i));
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Object data : rows) {
                Row row = sheet.createRow(rowIdx++);
                var comps = data.getClass().getRecordComponents();
                for (int i = 0; i < comps.length; i++) {
                    Object value;
                    try {
                        value = comps[i].getAccessor().invoke(data);
                    } catch (ReflectiveOperationException e) {
                        value = "ERROR";
                    }
                    row.createCell(i).setCellValue(value == null ? "" : value.toString());
                }
            }

            for (int i = 0; i < labels.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            log.info("Koncze budowe excela: " + filename);
            return baos.toByteArray();
        }
    }


}
