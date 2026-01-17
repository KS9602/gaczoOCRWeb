package org.example.clientsOCR;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.records.Company4Row;
import org.example.utils.Company4Stripper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Company4OCR extends BaseOCR<Company4Row>{

    private final int firstLineColor = -11184811;
    private final int secondLineColor = -8355712;
    private final int thirdLineColor = -12237499;

    private final Integer[] colsXPos = {
            39,
            58,
            129,
            271,
            310,
            330,
            359,
            401,
            424,
            469,
            511,
            568
    };



    public Company4OCR() throws IOException {
        super();
    }
    @Override
    public List<String> provideLabels() {
        return List.of(
                "Lp",
                "Symbol",
                "Nazwa",
                "Ilość",
                "j.m",
                "Rabat %",
                "Cena netto",
                "VAT %",
                "Wartość etto",
                "VAT",
                "Wartość brutto",
                "Marża"

        );
    }

    @Override
    public String provideInvoicePattern() {
        return "Faktura VAT FS";
    }

    @Override
    public List<Company4Row> getRows(PDDocument document, int pageIndex) throws IOException {

        log.info("Procesuje strone : " + pageIndex);
        PDPage page = document.getPage(pageIndex);
        PDFRenderer renderer = new PDFRenderer(document);
        BufferedImage img = renderer.renderImageWithDPI(pageIndex, 72);

        Company4Stripper company4Stripper = new Company4Stripper();
        company4Stripper.setStartPage(pageIndex + 1);
        company4Stripper.setEndPage(pageIndex + 1);
        company4Stripper.getText(document);

        int lpY = (int) company4Stripper.lpY;
        if (lpY == 0){
            return List.of();
        }
        List<Integer> Y = calcY(img, lpY);
        log.info("Znaleziono pozycje Y : " + Y);

        List<List<String>> regionNames = new ArrayList<>();
        for(int y = 0; y < Y.size() -1; y++){
            List<String> regionRowNames = new ArrayList<>();
            int yPos = Y.get(y);
            int h    = Y.get(y + 1) - Y.get(y);

            for (int x = 0; x < colsXPos.length - 1; x++){
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
        return buildRowRecords(page,regionNames);
    }

    @Override
    public List<Company4Row> buildRowRecords(PDPage page, List<List<String>> regionNames) throws IOException {
        areaStripper.extractRegions(page);
        List<Company4Row> rowRecords = new ArrayList<>();
        for (List<String> regionName : regionNames) {
            rowRecords.add(
                    new Company4Row(
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
                                    areaStripper.getTextForRegion(regionName.get(6))
                            )
                    )
            );
        }
        log.info("Utworzono recordy");
        return rowRecords;
    }

    private List<Integer> calcY(BufferedImage img, int lpY){

        List<Integer> Y = new ArrayList<>();
        for (int y = lpY; y < img.getHeight(); y++){
            int rgb = img.getRGB(colsXPos[0],y);
            boolean rowLine = switch (rgb) {
                case firstLineColor -> checkRightPixelLine(img, firstLineColor, y, colsXPos[0]);
                case secondLineColor -> checkRightPixelLine(img, secondLineColor, y, colsXPos[0]);
                case thirdLineColor -> checkRightPixelLine(img, thirdLineColor, y, colsXPos[0]);
                default -> false;
            };
            if(rowLine){
                Y.add(y);
            }
        }
        return Y;
    }



}
