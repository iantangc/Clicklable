package hk.edu.hku.cs.citang.urfp.supp.label.gui;

import java.awt.Color;

public class FunctionLabelDescriptor {
    public long id;
    public String name;
    public long value;
    public Color draw_fill_color;
    public Color draw_border_color;
    
    public FunctionLabelDescriptor(){
        id = 1;
        name = "New";
        value = 1;
        draw_fill_color = Color.white;
        draw_border_color = Color.black;
    }
    
    public FunctionLabelDescriptor(long id, String name, long value, Color draw_fill_color, Color draw_border_color){
        this.id = id;
        this.name = (name == null) ? "New" : name;
        this.value = value;
        this.draw_fill_color = (draw_fill_color == null) ? Color.white : draw_fill_color;
        this.draw_border_color = (draw_border_color == null) ? Color.black : draw_border_color;
    }
}
