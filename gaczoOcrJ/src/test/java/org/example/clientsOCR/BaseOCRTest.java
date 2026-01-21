package org.example.clientsOCR;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseOCRTest {

    private static class TestOCR extends BaseOCR<Record> {
        protected TestOCR() throws IOException {
            super();
        }

        @Override
        public List<Record> getRows(PDDocument document, int pageIndex) throws IOException {
            return List.of();
        }

        @Override
        public List<Record> buildRowRecords(PDPage page, List<List<String>> regionNames) throws IOException {
            return List.of();
        }

        @Override
        public List<String> provideLabels() {
            return List.of("Label1");
        }

        @Override
        public String provideInvoicePattern() {
            return "Pattern";
        }
    }

    @Test
    void shouldCalculateMarginCorrectly() throws IOException {
        TestOCR ocr = new TestOCR();
        ocr.setMarginRate(10.0);

        String result = ocr.calcMargin("5", "20,00");
        assertEquals("110,00", result);
    }

    @Test
    void shouldHandlePolishFormattingInCalcMargin() throws IOException {
        TestOCR ocr = new TestOCR();
        ocr.setMarginRate(20.0);

        String result = ocr.calcMargin("2,5", "100,00");
        assertEquals("300,00", result);
    }

    @Test
    void shouldReturnZeroForInvalidNumbersInCalcMargin() throws IOException {
        TestOCR ocr = new TestOCR();
        ocr.setMarginRate(10.0);

        assertEquals("0", ocr.calcMargin("abc", "10.00"));
        assertEquals("0", ocr.calcMargin("5", ""));
        assertEquals("0", ocr.calcMargin(null, "10.00"));
    }
}
