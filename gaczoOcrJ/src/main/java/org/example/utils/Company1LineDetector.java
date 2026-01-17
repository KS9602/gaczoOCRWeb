package org.example.utils;

import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Company1LineDetector extends PDFGraphicsStreamEngine {

    private Point2D.Float currentPoint;
    public List<Point2D> rowsHeightPoints = new ArrayList<Point2D>();
    private final Double startXpossition = 56.25;
    private static final float EPS = 1.0f;

    public Company1LineDetector(PDPage page) {super(page);}


    @Override
    public void moveTo(float x, float y) {
        currentPoint = new Point2D.Float(x, y);
    }
    @Override
    public void lineTo(float x, float y) {
        currentPoint = new Point2D.Float(x, y);
    }

    private boolean isUniqueY(Point2D p) {
        float y = (float) p.getY();

        for (Point2D existing : rowsHeightPoints) {
            if (Math.abs(existing.getY() - y) <= EPS) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void appendRectangle(Point2D p1, Point2D p2, Point2D p3, Point2D p4) {
        for (Point2D p : List.of(p1, p2, p3, p4)){
//            System.out.println(p1 + " || " + p2 + " || " + p3 + " || " + p4);
            if (p.getX() == startXpossition && isUniqueY(p)){
                rowsHeightPoints.add(p);
            }
        }
    }


    @Override public void fillPath(int windingRule) {}
    @Override public void strokePath() {}
    @Override public void closePath() {}
    @Override public void endPath() {}
    @Override public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) {}
    @Override public Point2D getCurrentPoint() { return currentPoint; }
    @Override public void fillAndStrokePath(int windingRule) {}
    @Override public void shadingFill(COSName shadingName) {}
    @Override public void drawImage(PDImage pdImage) throws IOException {}
    @Override public void clip(int i) throws IOException {}

}
