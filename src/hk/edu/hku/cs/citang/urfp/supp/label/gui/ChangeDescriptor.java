package hk.edu.hku.cs.citang.urfp.supp.label.gui;

import java.awt.Point;

public class ChangeDescriptor {
    public static final String ACTION_ADD = "ADD";
    public static final String ACTION_MODIFY = "MODIFY";
    public static final String ACTION_REMOVE = "REMOVE";
    
    public String action;
    public PointLabel before;
    public PointLabel after;
    
    public ChangeDescriptor(String action, Point p_before, Long l_before, Point p_after, Long l_after){
        this.action = action;
        before = new PointLabel(p_before, l_before);
        after = new PointLabel(p_after, l_after);
    }
    
    public String getAction(){
        return action;
    }
    
    public Point getPointBefore(){
        return before.point;
    }
    
    public Point getPointAfter(){
        return after.point;
    }
    
    public long getLabelBefore(){
        return before.label;
    }
    
    public long getLabelAfter(){
        return after.label;
    }
    
    class PointLabel {
        public Point point;
        public Long label;
        public PointLabel(Point p, Long l){
            this.point = p;
            this.label = l;
        }
    }
}
