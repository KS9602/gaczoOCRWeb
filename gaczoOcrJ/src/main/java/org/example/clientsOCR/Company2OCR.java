package org.example.clientsOCR;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.example.records.Company2Row;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Company2OCR extends BaseOCR<Company2Row> {


    private static final int lPXStart = 16;
    private static final int lPXend = 23;

    private static final int smallRowHeight = 12;

    private static final int nameXStart = 32;
    private static final int nameXEnd = 340;
    private static final int quantityXStart = 378;
    private static final int quantityXEnd = 401;
    private static final int quantityUnitXStart = 405;
    private static final int quantityUnitXEnd = 415;
    private static final int unitPriceNettoXStart = 455;
    private static final int unitPriceNettoXEnd = 484;
    private static final int nettoValueXStart = 520;
    private static final int nettoValueXEnds = 545;
    private static final int vatXStart = 553;
    private static final int vatXend = 563;

    private static final int endOfBlackLine = 573;
    private static final int blackLine = -4210753;
    private static final int noneRowLinesEnd = 1;



    public Company2OCR() throws IOException {
        super();
    }



    @Override
    public List<String> provideLabels() {
        return List.of(
                "LP",
                "Nazwa",
                "Ilość",
                "JM",
                "Cena jedn. netto",
                "Wartość netto",
                "Stawka VAT",
                "Marża"
        );
    }

    @Override
    public String provideInvoicePattern() {
        return "Faktura VAT nr";
    }

    @Override
    public List<Company2Row> getRows(PDDocument document, int pageIndex) throws IOException {

        log.info("Procesuje strone :{}", pageIndex);
        PDPage page = document.getPage(pageIndex);
        if(!isRow(document, pageIndex)){
            log.info("Strona bez tabeli, przerywam dzialanie:{}", pageIndex);
            return List.of();
        }

        List<Integer> rowYpos = findBlackLines(document, pageIndex);
        log.info("Znaleziono pozycje Y :{}",rowYpos);

        List<List<String>> regionNames = new ArrayList<>();
        for(int i = 0; i < rowYpos.size(); i++){
            int y = rowYpos.get(i);
            String name = buildNameRegion2(y, nameXStart, smallRowHeight, nameXEnd);
            String lp = buildCommonRegion2(y, "LP", lPXStart, lPXend, smallRowHeight);
            String quantity = buildCommonRegion2(y, "QUANTITY", quantityXStart, quantityXEnd, smallRowHeight);
            String quantityUnit = buildCommonRegion2(y, "QUANTITY_UNIT", quantityUnitXStart, quantityUnitXEnd, smallRowHeight);
            String unitPriceNetto = buildCommonRegion2(y, "UNIT_PRICE_NETTO", unitPriceNettoXStart, unitPriceNettoXEnd, smallRowHeight);
            String nettoValue = buildCommonRegion2(y, "NETTO_VALUE", nettoValueXStart, nettoValueXEnds, smallRowHeight);
            String vat = buildCommonRegion2(y, "VAT", vatXStart, vatXend, smallRowHeight);
            regionNames.add(List.of(
                    lp,
                    name,
                    quantity,
                    quantityUnit,
                    unitPriceNetto,
                    nettoValue,
                    vat
            ));
        }

        log.info("Utworzono regiony");
        return buildRowRecords(page, regionNames);
    }

    public List<Company2Row> buildRowRecords(PDPage page, List<List<String>> regionNames) throws IOException {
        areaStripper.extractRegions(page);
        List<Company2Row> rowRecords = new ArrayList<>();
        for (int i = 0; i < regionNames.size(); i++) {
            rowRecords.add(
                    new Company2Row(
                            areaStripper.getTextForRegion(regionNames.get(i).get(0)),
                            areaStripper.getTextForRegion(regionNames.get(i).get(1)),
                            areaStripper.getTextForRegion(regionNames.get(i).get(2)),
                            areaStripper.getTextForRegion(regionNames.get(i).get(3)),
                            areaStripper.getTextForRegion(regionNames.get(i).get(4)),
                            areaStripper.getTextForRegion(regionNames.get(i).get(5)),
                            areaStripper.getTextForRegion(regionNames.get(i).get(6)),
                            calcMargin(
                                    calcQuantity(
                                            areaStripper.getTextForRegion(regionNames.get(i).get(1)),
                                            areaStripper.getTextForRegion(regionNames.get(i).get(2))
                                    ),
                                    areaStripper.getTextForRegion(regionNames.get(i).get(4))
                            )
                    )
            );
        }
        log.info("Utworzono recordy");
        return rowRecords;
    }

    public boolean isRow (PDDocument document, int pageIndex) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        stripper.setStartPage(pageIndex + 1);
        stripper.setEndPage(pageIndex + 1);
        return stripper.getText(document).contains("Lp");
    }


    private List<Integer> findBlackLines(PDDocument document, int pageIndex) throws IOException {
        PDFRenderer renderer = new PDFRenderer(document);
        BufferedImage img = renderer.renderImageWithDPI(pageIndex, 72);
        List<Integer> rowYpos = new ArrayList<>();
        int noneRowLinesStart = pageIndex == 0 ? 4 : 1;

        for (int y = 0; y < img.getHeight(); y++){
            int rgbY = img.getRGB(endOfBlackLine,y);
            if(rgbY == blackLine){
                int middleRGB = img.getRGB(img.getWidth() / 2, y);
                if(middleRGB == blackLine){
                    rowYpos.add(y);
                }
            }
        }
        rowYpos.subList(0, noneRowLinesStart).clear();
        rowYpos.remove(rowYpos.size() - noneRowLinesEnd);
        return rowYpos;
    }


    public String buildNameRegion2(
            int y,
            int nameXStart,
            int smallRowHeight,
            int nameXEnd
    ){
        String name = "NAME_" + y;
        areaStripper.addRegion(
                name,
                new Rectangle(
                        nameXStart,
                        y + smallRowHeight,
                        nameXEnd - nameXStart,
                        smallRowHeight
                )
        );
        return name;
    }

    public String buildCommonRegion2(
            int y,
            String colName,
            int xStart,
            int xEnd,
            int smallRowHeight
    ) {

        String name = colName + "_" + y;
        areaStripper.addRegion(
                name,
                new Rectangle(
                        xStart,
                        y,
                        xEnd - xStart,
                        smallRowHeight
                )
        );
        return name;
    }

    private String calcQuantity(String name, String quantity){
        if (quantity == null || quantity.isBlank()) {
            return "0";
        }

        Pattern pattern = Pattern.compile("\\b(\\d+)\\s*[xX]\\s*\\d+[a-zA-Z]*\\b");
        Matcher matcher = pattern.matcher(name);

        String quantityPart = matcher.find()
                ? matcher.group(1)
                : "1";


        String normalizedQuantity = quantity
                .replace("\u00A0", "")
                .replace(" ", "")
                .replace(",", ".")
                .trim();

        try {
            BigDecimal valueQuantity = new BigDecimal(normalizedQuantity);
            BigDecimal valueUnitPrice = new BigDecimal(quantityPart);

            BigDecimal value = valueQuantity.multiply(valueUnitPrice);
            value = value.setScale(2, RoundingMode.HALF_UP);
            return value.toString().replace(".", ",");
        } catch (NumberFormatException e) {
            return "0";
        }
    }

}
