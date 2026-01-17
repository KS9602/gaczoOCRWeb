package org.example.clientsOCR;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.example.records.Company1Row;
import org.example.utils.Company1LineDetector;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Company1OCR extends BaseOCR<Company1Row> {


    private final int endX = 558;
    private final List<Integer> xParams = List.of(
            36,
            56,
            250,
            298,
            352,
            408,
            445,
            501
    );


    public Company1OCR() throws IOException {
        super();
    }


    @Override
    public List<String> provideLabels() {
        return List.of(
                "LP",
                "Nazwa towaru / usługi",
                "Ilość",
                "Cena netto",
                "Wartość netto",
                "VAT %",
                "Wartość VAT",
                "Wartość brutto",
                "Marża"
        );
    }

    @Override
    public String provideInvoicePattern() {
        return "Faktura numer";
    }

    @Override
    public List<Company1Row> getRows(PDDocument document, int pageIndex) throws IOException {

        log.info("Procesuje strone : " + pageIndex);

        PDPage page = document.getPage(pageIndex);
        Company1LineDetector detector = new Company1LineDetector(page);
        PDRectangle mediaBox = page.getMediaBox();
        float pageHeight = mediaBox.getHeight();
        detector.processPage(page);
        List<Point2D> heights = detector.rowsHeightPoints;

        List<List<String>> regionNames = new ArrayList<>();

        for (int i = 1; i < heights.size() - 1; i++) {
            List<String> regionRowNames = new ArrayList<>();
            for (int j = 0; j < xParams.size(); j++) {
                String regionName = i + "-" + j;
                int x = xParams.get(j);
                int y = (int) (pageHeight - heights.get(i).getY());
                int w = j != xParams.size() - 1 ? xParams.get(j + 1) - xParams.get(j) : endX - xParams.get(j);
                int h = ((int) heights.get(i).getY() - (int) heights.get(i - 1).getY()) * -1;
                areaStripper.addRegion(
                        regionName,
                        new Rectangle(x, y, w, h));

                regionRowNames.add(regionName);
                if (j == xParams.size() - 1) {
                    regionNames.add(regionRowNames);
                }

            }
        }

        log.info("Utworzono regiony");
        return buildRowRecords(page, regionNames);
    }

    public List<Company1Row> buildRowRecords(PDPage page, List<List<String>> regionNames) throws IOException {
        areaStripper.extractRegions(page);
        List<Company1Row> rowRecords = new ArrayList<>();
        for (List<String> regionName : regionNames) {
            rowRecords.add(
                    new Company1Row(
                            areaStripper.getTextForRegion(regionName.get(0)),
                            areaStripper.getTextForRegion(regionName.get(1)),
                            areaStripper.getTextForRegion(regionName.get(2)),
                            areaStripper.getTextForRegion(regionName.get(3)),
                            areaStripper.getTextForRegion(regionName.get(4)),
                            areaStripper.getTextForRegion(regionName.get(5)),
                            areaStripper.getTextForRegion(regionName.get(6)),
                            areaStripper.getTextForRegion(regionName.get(7)),
                            calcMargin(
                                    normalizedQuantity(areaStripper.getTextForRegion(regionName.get(2))),
                                    areaStripper.getTextForRegion(regionName.get(3))
                            )
                    )
            );
        }

        log.info("Utworzono recordy");
        return rowRecords;
    }

    private String normalizedQuantity(String quantity){
        return quantity.split(" ")[0];
    }

}
