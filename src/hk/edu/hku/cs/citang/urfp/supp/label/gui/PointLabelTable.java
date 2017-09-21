package hk.edu.hku.cs.citang.urfp.supp.label.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

public class PointLabelTable {
    private HashMap<Point, Integer> plt;
    
    public PointLabelTable(){
        plt = new HashMap<Point, Integer>();
        
    }
    
    public void PutPointLabel(Point p, Integer l){
        plt.put(p, l);
    }
 
    public void RemovePoint(Point p){
        plt.remove(p);
    }
    
    public void DrawAllPoints(Graphics p, HashMap<Integer, Color> mapping){
        for (Map.Entry<Point, Integer> entry : plt.entrySet()) {
            Point key = entry.getKey();
            Integer value = entry.getValue();
        }
    }
}