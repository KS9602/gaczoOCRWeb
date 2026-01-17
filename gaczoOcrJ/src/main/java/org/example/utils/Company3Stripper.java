package org.example.utils;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.List;

public class Company3Stripper extends PDFTextStripper {

    private final String lpString = "Lp";
    public float lpY;
    public float lpX;

    public Company3Stripper() throws IOException {
        super();
    }


    protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
        int index = string.indexOf(lpString);

        if (index >= 0 && index < textPositions.size()) {
            TextPosition pos = textPositions.get(index);
            lpX = pos.getXDirAdj();
            lpY = pos.getYDirAdj();
        }

        super.writeString(string, textPositions);
    }
}