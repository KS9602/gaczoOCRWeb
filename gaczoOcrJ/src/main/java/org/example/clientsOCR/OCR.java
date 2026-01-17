package org.example.clientsOCR;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public interface OCR <T extends Record>{

    List<T> processDocument(PDDocument document, double margin) throws IOException;
    List<T> getRows(PDDocument document, int pageIndex) throws IOException;
    List<T> buildRowRecords(PDPage page, List<List<String>> regionNames) throws IOException;

    List<String> getLabels();
    String getinvoiceNrPattern();
    List<String> provideLabels();
    String provideInvoicePattern();
    PDFTextStripperByArea provideAreaStriper() throws IOException;

    boolean checkRightPixelLine(BufferedImage img, int color, int y, int baseX);
    String calcMargin(String quantity, String unitPrice);

    void setMarginRate(double margin);
}
