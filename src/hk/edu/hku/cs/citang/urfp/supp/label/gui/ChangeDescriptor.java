package hk.edu.hku.cs.citang.urfp.supp.label.gui;

import java.awt.Point;

public class ChangeDescriptor {
    public static final String ACTION_ADD = "ADD";
    public static final String ACTION_MODIFY = "MODIFY";
    public static final String ACTION_REMOVE = "REMOVE";
    
    public String action;
    public LabelData before;
    public LabelData after;
    
    public ChangeDescriptor(String action, LabelData before, LabelData after){
        this.action = action;
        this.before = before;
        this.after = after;
    }
    
    public String getAction(){
        return action;
    }
    
    public LabelData getBefore(){
        return before;
    }
    
    public LabelData getAfter(){
        return after;
    }
}
class LabelData {
    public static final String TYPE_POINT = "POINT";
    public static final String TYPE_BOUNINGBOX = "BOUNDINGBOX";
    
    public Object data;
    public Object getData() {
        return data;
    }
    public void setData(Object data) {
        this.data = data;
    }
    public Long getLabel() {
        return label;
    }
    public void setLabel(Long label) {
        this.label = label;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public Long label;
    public String type;
    public LabelData(Object d, Long l, String t){
        this.data = d;
        this.label = l;
        this.type = t;
    }
}