package org.example.clientsOCR;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.records.Company3Row;
import org.example.utils.Company3Stripper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Company3OCR extends BaseOCR<Company3Row>{

    private final int lineColor = -1842205;

    public Company3OCR() throws IOException {
        super();
    }

    @Override
    public List<String> provideLabels() {
        return List.of(
                "LP",
                "CN/PKWIU",
                "Indeks",
                "Nazwa",
                "Ilość",
                "JM",
                "Cena NETTO",
                "Wartość NETTO",
                "Stawka VAT",
                "Wartość VAT",
                "Wartość BRUTTO",
                "Marża"

        );
    }

    @Override
    public String provideInvoicePattern() {
        return "Faktura VAT";
    }

    @Override
    public List<Company3Row> getRows(PDDocument document, int pageIndex) throws IOException {

        log.info("Procesuje strone : " + pageIndex);

        PDPage page = document.getPage(pageIndex);
        PDFRenderer renderer = new PDFRenderer(document);
        BufferedImage img = renderer.renderImageWithDPI(pageIndex, 72);

        Company3Stripper company3StripperStripper = new Company3Stripper();
        company3StripperStripper.setStartPage(pageIndex + 1);
        company3StripperStripper.setEndPage(pageIndex + 1);
        company3StripperStripper.getText(document);

        int lpY = (int) company3StripperStripper.lpY;
        if (lpY == 0){
            return List.of();
        }
        int lpX = (int)company3StripperStripper.lpX;

        List<Integer> Y = calcY(img, lpX, lpY);
        log.info("Znaleziono pozycje Y : " + Y);

        List<Integer> X = clacX(img, lpX, lpY);
        log.info("Znaleziono pozycje X : " + X);


        List<List<String>> regionNames = new ArrayList<>();
        for (int y = 0; y < Y.size() - 1; y++){
            List<String> regionRowNames = new ArrayList<>();
            int yPos = Y.get(y);
            int h    = Y.get(y + 1) - Y.get(y);

            for(int x = 0; x < X.size() - 1; x++){
                String regionName = y + "-" + x;
                int xPos = X.get(x);
                int w = X.get(x + 1) - X.get(x);

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
    public List<Company3Row> buildRowRecords(PDPage page, List<List<String>> regionNames) throws IOException {
        areaStripper.extractRegions(page);

        List<Company3Row> rowRecords = new ArrayList<>();
        for (List<String> regionName : regionNames) {
            rowRecords.add(
                    new Company3Row(
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
                                    areaStripper.getTextForRegion(regionName.get(4)),
                                    areaStripper.getTextForRegion(regionName.get(6))
                            )
                    )
            );
        }

        log.info("Utworzono recordy");
        return rowRecords;
    }

    private List<Integer> calcY(BufferedImage img,int lpX,int lpY){

        List<Integer> Y = new ArrayList<>();
        boolean flag = false;
        for (int y = lpY; y < img.getHeight(); y++){
            int rgb = img.getRGB(lpX,y);
            if(rgb == -1){ flag = true;}
            if(!flag){continue;}
            if(rgb == lineColor){
                Y.add(y);
            }
        }
        int bottomLineY = calcBottomLineY(img, Y);
        Y.add(bottomLineY);
        Y.removeIf(v -> v > bottomLineY);

        return Y;
    }

    private List<Integer> clacX(BufferedImage img,int lpX,int lpY){

        List<Integer> X = new ArrayList<>();
        X.add(0);
        for (int x = lpX; x < img.getWidth(); x++){
            int rgb = img.getRGB(x, lpY);
            if(rgb == -1){
                X.add(x);
            }
        }
        int cutIndex = 0;
        for(int x = 1; x < X.size(); x++){
            if (X.get(x) - X.get(x - 1) == 1){
                cutIndex = x;
                break;
            }
        }
        return X.subList(0, cutIndex);
    }

    private int calcBottomLineY(BufferedImage img, List<Integer> Y){

        int columnLineX = 0;
        int columnLineY = Y.get(0) + 1;
        int endOfY = 0;
        for(int x = 0; x < img.getWidth() ; x++){
            int rgb = img.getRGB(x,columnLineY);
            if(rgb == lineColor){
                columnLineX = x;
                break;
            }
        }
        for(int y = columnLineY; y < img.getHeight(); y++){
            int rgb = img.getRGB(columnLineX,y);
            if (rgb != lineColor){
                endOfY = y;
                break;
            }
        }
        return endOfY;

    }

}
