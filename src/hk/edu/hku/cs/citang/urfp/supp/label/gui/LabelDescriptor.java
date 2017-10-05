package hk.edu.hku.cs.citang.urfp.supp.label.gui;

import java.awt.Color;

public class LabelDescriptor {
    public long id;
    public String name;
//    public long value;
    public Color draw_fill_color;
    public Color draw_border_color;
    public String shapeString;
    public int shapeSize;
    public int shapeThickness;
    
    public LabelDescriptor(){
        id = 1;
        name = "New";
        draw_fill_color = Color.white;
        draw_border_color = Color.black;
        shapeString = "Ring";
        shapeSize = 5;
        shapeThickness = 3;
    }
    

    public LabelDescriptor(long id, String name, Color draw_fill_color, Color draw_border_color, String shapeString, Integer shapeSize, Integer shapeThickness){
        this.id = id;
        this.name = (name == null) ? "New" : name;
        this.draw_fill_color = (draw_fill_color == null) ? Color.white : draw_fill_color;
        this.draw_border_color = (draw_border_color == null) ? Color.black : draw_border_color;
        this.shapeString = (shapeString == null) ? "Ring" : shapeString;
        this.shapeSize = (shapeSize == null) ? 5 : shapeSize;
        this.shapeThickness = (shapeThickness == null) ? 3 : shapeThickness;
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getDraw_fill_color() {
        return draw_fill_color;
    }

    public void setDraw_fill_color(Color draw_fill_color) {
        this.draw_fill_color = draw_fill_color;
    }

    public Color getDraw_border_color() {
        return draw_border_color;
    }

    public void setDraw_border_color(Color draw_border_color) {
        this.draw_border_color = draw_border_color;
    }


    public String getShapeString() {
        return shapeString;
    }

    public void setShapeString(String shapeString) {
        this.shapeString = shapeString;
    }

    public int getShapeSize() {
        return shapeSize;
    }

    public void setShapeSize(int shapeSize) {
        this.shapeSize = shapeSize;
    }

    public int getShapeThickness() {
        return shapeThickness;
    }

    public void setShapeThickness(int shapeThickness) {
        this.shapeThickness = shapeThickness;
    }
}
