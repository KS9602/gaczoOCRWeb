package org.example.clientsOCR;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.records.Company5Row;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Company5OCR extends BaseOCR<Company5Row> {

    private final int startYpos = 350;
    private final int bottomGraySpaceColor = -4144960;

    private final Integer[] colsXPos = {
            29,
            44,
            250,
            320,
            340,
            362,
            385,
            418,
            456,
            494,
            531,
            565
    };


    public Company5OCR() throws IOException {
        super();
    }

    @Override
    public List<String> provideLabels() {
        return List.of(
                "Lp",
                "Nazwa towaru/usługi",
                "Kod EAN",
                "Ilość",
                "j.m",
                "VAT",
                "Rabat",
                "Cena Netto",
                "Cena Brutto",
                "Wartość Netto",
                "Wartość Brutto",
                "Marża"

        );
    }

    @Override
    public String provideInvoicePattern() {
        return "nr FA";
    }

    @Override
    public List<Company5Row> getRows(PDDocument document, int pageIndex) throws IOException {

        log.info("Procesuje strone : " + pageIndex);

        PDPage page = document.getPage(pageIndex);
        PDFRenderer renderer = new PDFRenderer(document);
        BufferedImage img = renderer.renderImageWithDPI(pageIndex, 72);

        List<Integer> Y = calcY(img);
        log.info("Znaleziono pozycje Y : " + Y);

        List<List<String>> regionNames = new ArrayList<>();
        for (int y = 0; y < Y.size() - 1; y++) {
            List<String> regionRowNames = new ArrayList<>();
            int yPos = Y.get(y);
            int h = Y.get(y + 1) - Y.get(y);

            for (int x = 0; x < colsXPos.length - 1; x++) {
                String regionName = y + "-" + x;
                int xPos = colsXPos[x];
                int w = colsXPos[x + 1] - colsXPos[x];
                areaStripper.addRegion(
                        regionName,
                        new Rectangle(xPos, yPos, w, h));

                regionRowNames.add(regionName);
            }
            regionNames.add(regionRowNames);
        }

        log.info("Utworzono regiony");
        return buildRowRecords(page, regionNames);
    }

    @Override
    public List<Company5Row> buildRowRecords(PDPage page, List<List<String>> regionNames) throws IOException {
        areaStripper.extractRegions(page);

        List<Company5Row> rowRecords = new ArrayList<>();
        for (List<String> regionName : regionNames) {
            rowRecords.add(
                    new Company5Row(
                            areaStripper.getTextForRegion(regionName.get(0)),
                            areaStripper.getTextForRegion(regionName.get(1)),
                            areaStripper.getTextForRegion(regionName.get(2)),
                            areaStripper.getTextForRegion(regionName.get(3)),
                            areaStripper.getTextForRegion(regionName.get(4)),
                            areaStripper.getTextForRegion(regionName.get(5)),
                            areaStripper.getTextForRegion(regionName.get(6)),
                            areaStripper.getTextForRegion(regionName.get(7)),
                            areaStripper.getTextForRegion(regionName.get(8)),
                            areaStripper.getTextForRegion(regionName.get(9)),
                            areaStripper.getTextForRegion(regionName.get(10)),
                            calcMargin(
                                    areaStripper.getTextForRegion(regionName.get(3)),
                                    areaStripper.getTextForRegion(regionName.get(7))
                            )
                    )
            );
        }

        log.info("Utworzono recordy");
        return rowRecords;
    }

    private List<Integer> calcY(BufferedImage img) {

        boolean sameChar = false;
        List<Integer> packList = new ArrayList<>();
        List<Integer> Y = new ArrayList<>();
        for (int y = startYpos; y < img.getHeight(); y++) {
            for(int width = 0; width < 10; width++){
                int rgb = img.getRGB(colsXPos[0] + width, y);
                packList.add(rgb);
                if(rgb != -1 && !sameChar){
                    Y.add(y);
                    sameChar = true;
                }
            }
            if(packList.stream().allMatch(i -> i == -1)){
                sameChar = false;
                }
            if(packList.stream().filter(i -> i == bottomGraySpaceColor).count() > 7){
                break;
                }
            packList.clear();
            }

        return Y;
    }

}