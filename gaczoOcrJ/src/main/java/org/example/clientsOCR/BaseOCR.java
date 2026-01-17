package org.example.clientsOCR;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseOCR <T extends Record> implements OCR<T> {

    protected final String invoiceNrPattern;
    protected final List<String> labels;
    protected final PDFTextStripperByArea areaStripper;
    protected final Logger log;
    protected double marginRate;


    protected BaseOCR() throws IOException {
        this.log = LoggerFactory.getLogger(getClass());;
        this.invoiceNrPattern = provideInvoicePattern();
        this.labels = provideLabels();
        this.areaStripper = provideAreaStriper();
    }

    public abstract List<T> getRows(PDDocument document, int pageIndex) throws IOException;
    public abstract List<T> buildRowRecords(PDPage page, List<List<String>> regionNames) throws IOException;
    public abstract List<String> provideLabels();
    public abstract String provideInvoicePattern();
    public String getinvoiceNrPattern(){ return invoiceNrPattern;}

    @Override
    public void setMarginRate(double margin){
        marginRate = margin;
    }

    @Override
    public PDFTextStripperByArea provideAreaStriper() throws IOException {
        return new PDFTextStripperByArea();
    }

    @Override
    public List<T> processDocument (PDDocument document, double margin) throws IOException{
        this.setMarginRate(margin);
        int pageCount = document.getNumberOfPages();
        log.info("Wykryto liczbe stron: " + pageCount);
        List<T> rows = new ArrayList<>();
        for (int i = 0; i < pageCount; i++){
            rows.addAll(getRows(document, i));
        }
        return rows;
    }

    @Override
    public List<String> getLabels(){
        return labels;
    };

    @Override
    public boolean checkRightPixelLine(BufferedImage img,int color, int y, int baseX){
        for(int i = 0; i <= 500; i += 50){
            if(!(img.getRGB(baseX + 50,y) == color)){
                return false;
            }
        }
        return true;
    }

    @Override
    public String calcMargin(String quantity, String unitPrice) {

        if (quantity == null || quantity.isBlank()) {
            return "0";
        }
        if (unitPrice == null || unitPrice.isBlank()) {
            return "0";
        }

        String normalizedQuantity = quantity
                .replace("\u00A0", "")
                .replace(" ", "")
                .replace(",", ".")
                .trim();
        String normalizedUnitPrice = unitPrice
                .replace("\u00A0", "")
                .replace(" ", "")
                .replace(",", ".")
                .trim();

        try {
            BigDecimal valueQuantity = new BigDecimal(normalizedQuantity);
            BigDecimal valueUnitPrice = new BigDecimal(normalizedUnitPrice);
            BigDecimal value = valueQuantity.multiply(valueUnitPrice);
            BigDecimal multiplier = BigDecimal.valueOf(1).add(BigDecimal.valueOf(marginRate).divide(BigDecimal.valueOf(100)));
            BigDecimal margin = value.multiply(multiplier);
            margin = margin.setScale(2, RoundingMode.HALF_UP);
            return margin.toString().replace(".", ",");
        } catch (NumberFormatException e) {
            return "0";
        }
    }


}
