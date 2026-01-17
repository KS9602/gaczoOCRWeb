package org.example.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

public class Scaner {

    private final PDFTextStripper stripper = new PDFTextStripper();
    private PDPage page;
    private String text;

    public Scaner() throws IOException {}

    public void scan(File file) {
        try (PDDocument document = PDDocument.load(file)) {
            System.setProperty("pdfbox.fontcache", "false");

            page = document.getPage(0);
            ScanerLineDetector scanerLineDetector = new ScanerLineDetector(page);
            scanerLineDetector.processPage(page);

            text = getText(document);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getText(PDDocument document) throws IOException {
        return stripper.getText(document);
    }

}
