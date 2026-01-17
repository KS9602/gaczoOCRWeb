package org.example.utils;

import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Company3LineDetector extends PDFGraphicsStreamEngine {

    private Point2D.Float currentPoint;
    public List<Point2D> lPPoints = new ArrayList<Point2D>();
    private static final float EPS = 1.0f;
    private final float lPXPossiton = 16.86615f;

    public Company3LineDetector(PDPage page) {super(page);}


    @Override
    public void moveTo(float x, float y) {
        currentPoint = new Point2D.Float(x, y);
    }
    @Override
    public void lineTo(float x, float y) {
        currentPoint = new Point2D.Float(x, y);
    }

    public double getHighestLpY(){
        double highestY = 10000;
        for (Point2D p: lPPoints){
            if(p.getY() < highestY){
                highestY = p.getY();
            }
        }
        return highestY;
    }

    @Override
    public void appendRectangle(Point2D p1, Point2D p2, Point2D p3, Point2D p4) {
        for (Point2D p : List.of(p1, p2, p3, p4)){
//            System.out.println(p1 + " || " + p2 + " || " + p3 + " || " + p4);
            if (p.getX() == lPXPossiton){
                lPPoints.add(p);
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