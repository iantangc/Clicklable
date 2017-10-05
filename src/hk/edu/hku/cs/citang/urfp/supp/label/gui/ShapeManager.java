package hk.edu.hku.cs.citang.urfp.supp.label.gui;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

public class ShapeManager {
    
    public static String[] SHAPE_STRING_LIST = {
      "Ring",
      "Square",
      "UprightCross"
    };
    
    public static Shape getShapeByString(String shapeStr, double centerX, double centerY, double size, double thickness){
        if (shapeStr.equals(SHAPE_STRING_LIST[0])){
            return createRingShape(centerX, centerY, size, thickness);
        } else if (shapeStr.equals(SHAPE_STRING_LIST[1])){
            return createSquareShape(centerX, centerY, size, thickness);
        } else if (shapeStr.equals(SHAPE_STRING_LIST[2])){
            return createUprightCrossShape(centerX, centerY, size, thickness);
        }
        return null;
    }
    
    public static Shape createUprightCrossShape(double centerX, double centerY, double armLength, double thickness) {
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 12);
        path.moveTo(centerX + armLength, centerY + thickness);
        path.lineTo(centerX + thickness, centerY + thickness);
        path.lineTo(centerX + thickness, centerY + armLength);
        path.lineTo(centerX - thickness, centerY + armLength);
        path.lineTo(centerX - thickness, centerY + thickness);
        path.lineTo(centerX - armLength, centerY + thickness);
        path.lineTo(centerX - armLength, centerY - thickness);
        path.lineTo(centerX - thickness, centerY - thickness);
        path.lineTo(centerX - thickness, centerY - armLength);
        path.lineTo(centerX + thickness, centerY - armLength);
        path.lineTo(centerX + thickness, centerY - thickness);
        path.lineTo(centerX + armLength, centerY - thickness);
        path.closePath();
        return path;
    }

    public static Shape createSquareShape(double centerX, double centerY, double halfWidth, double thickness) {
        Rectangle2D outer = new Rectangle2D.Double(centerX - halfWidth, centerY - halfWidth,
                halfWidth + halfWidth, halfWidth + halfWidth);
        Rectangle2D inner = new Rectangle2D.Double(centerX - halfWidth + thickness, centerY - halfWidth + thickness,
                halfWidth + halfWidth - thickness - thickness,
                halfWidth + halfWidth - thickness - thickness);
        Area area = new Area(outer);
        area.subtract(new Area(inner));
        return area;
    }
    
    public static Shape createRingShape(double centerX, double centerY, double outerRadius, double thickness) {
        Ellipse2D outer = new Ellipse2D.Double(centerX - outerRadius, centerY - outerRadius,
                outerRadius + outerRadius, outerRadius + outerRadius);
        Ellipse2D inner = new Ellipse2D.Double(centerX - outerRadius + thickness, centerY - outerRadius + thickness,
                outerRadius + outerRadius - thickness - thickness,
                outerRadius + outerRadius - thickness - thickness);
        Area area = new Area(outer);
        area.subtract(new Area(inner));
        return area;
    }
    
    public static void fillOval(Graphics2D g2d, double centerX, double centerY, double radiusX, double radiusY){
        g2d.fillOval((int) (centerX - radiusX), (int) (centerY - radiusY), (int) (2 * radiusX), (int) (2 * radiusY));
    }
}
