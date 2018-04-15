package hk.edu.hku.cs.citang.urfp.supp.label.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.AffineTransformOp;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JSeparator;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JTextField;

public class SettingFrame extends JFrame {

    private SettingFrame thisFrame;
    private JPanel contentPane;
    private ClicklableMain mainFrame;
    
    private boolean settingChanged = false;
    

    boolean shapeScaleToZoom = false;
    int snapSensitivity = 10;
    int zoomSensitivity = 10;

    
    LinkedHashMap<Long, LabelDescriptor> labelDescriptorTable;
    DefaultListModel<ListSelectionWrapper> labelListModel;
    JList<ListSelectionWrapper> listLabels;
    
    ListSelectionWrapper selectedListItem;
    int selectedListIndex;
    
    Color fillColor = Color.white;
    Color borderColor = Color.black;
    int drawShapeSize = 16;
    int drawShapeThicknessPercent = 50;
    String currentShapeString = "Ring";
    ShapeDisplayPanel shapeDisplayPanel;
    
    
    private JTextField textFieldId;
    private JTextField textFieldName;
//    private JTextField textFieldValue;
    /**
     * Create the frame.
     */
    public SettingFrame(ClicklableMain main) {
        thisFrame = this;
        mainFrame = main;
        importSettings();
        shapeDisplayPanel = new ShapeDisplayPanel();
        
        setTitle("Settings");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setBounds(100, 100, 620, 400);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        contentPane.add(tabbedPane, BorderLayout.CENTER);
        thisFrame.addWindowListener(new WindowListener() {

            @Override
            public void windowClosed(WindowEvent arg0) {
            }

            @Override
            public void windowClosing(WindowEvent arg0) {
                if (settingChanged){
                    int dialogResult = JOptionPane.showConfirmDialog(thisFrame, "Do you want to save the changes before closing?",
                            "Reminder", JOptionPane.YES_NO_CANCEL_OPTION);
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        exportSettings();
                    } else if (dialogResult == JOptionPane.CANCEL_OPTION) {
                        return;
                    }
                } 
                thisFrame.setVisible(false);
                thisFrame.dispose();
                
            }

            @Override
            public void windowDeactivated(WindowEvent arg0) {
            }

            @Override
            public void windowDeiconified(WindowEvent arg0) {
            }

            @Override
            public void windowIconified(WindowEvent arg0) {
            }

            @Override
            public void windowOpened(WindowEvent arg0) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }
        });
        

        
        JPanel panel = new JPanel();
        
        
        
        JScrollPane scrollPane = new JScrollPane(panel);
        panel.setLayout(new MigLayout("", "[grow]", "[][][][][][][][][][][]"));
        
        tabbedPane.addTab("General", null, scrollPane, null);
      

        JCheckBox chckbxNewCheckBox = new JCheckBox("Label Size Scale to Zoom");
        chckbxNewCheckBox.setSelected(shapeScaleToZoom);
        chckbxNewCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                shapeScaleToZoom = chckbxNewCheckBox.isSelected();
                settingChanged = true;
            }
            
        });
        panel.add(chckbxNewCheckBox, "cell 0 0");
        
        JLabel lblSnapSensitivityText = new JLabel("Snap Sensitivity");
        panel.add(lblSnapSensitivityText, "cell 0 1");
        
        JSlider sliderSnapSensitvity = new JSlider();
        JLabel lblSnapSensitivity = new JLabel("px");
        
        sliderSnapSensitvity.setPaintTicks(true);
        sliderSnapSensitvity.setMajorTickSpacing(10);
        sliderSnapSensitvity.setMinorTickSpacing(5);
        sliderSnapSensitvity.setValue(snapSensitivity);
        lblSnapSensitivity.setText(snapSensitivity + "px");
        sliderSnapSensitvity.addChangeListener(new ChangeListener() {
            
            @Override
            public void stateChanged(ChangeEvent e) {
                snapSensitivity = ((JSlider) (e.getSource())).getValue();
                lblSnapSensitivity.setText(snapSensitivity + "px");
                settingChanged = true;
            }
        });
        
        panel.add(sliderSnapSensitvity, "flowx,cell 0 2");
        

        
        
        panel.add(lblSnapSensitivity, "cell 0 2");
        
        JLabel lblZoomSensitivity_1 = new JLabel("Zoom Sensitivity");
        panel.add(lblZoomSensitivity_1, "cell 0 3");
        
        JSlider sliderZoomSensitivity = new JSlider();
        JLabel lblZoomSensitivity = new JLabel("10");
        
        sliderZoomSensitivity.setPaintTicks(true);
        sliderZoomSensitivity.setMinorTickSpacing(1);
        sliderZoomSensitivity.setMajorTickSpacing(2);
        sliderZoomSensitivity.setValue(10);
        sliderZoomSensitivity.setMaximum(20);
        
        sliderZoomSensitivity.setValue(zoomSensitivity);
        lblZoomSensitivity.setText("" + zoomSensitivity);
        sliderZoomSensitivity.addChangeListener(new ChangeListener() {
            
            @Override
            public void stateChanged(ChangeEvent e) {
                zoomSensitivity = ((JSlider) (e.getSource())).getValue();
                lblZoomSensitivity.setText("" + zoomSensitivity);
                settingChanged = true;
            }
        });
        
        panel.add(sliderZoomSensitivity, "flowx,cell 0 4");
        
        
        panel.add(lblZoomSensitivity, "cell 0 4");
        

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.8);
        tabbedPane.addTab("Bacterium Labels", null, splitPane, null);

        JScrollPane scrollPane_1 = new JScrollPane(shapeDisplayPanel);
        scrollPane_1.setPreferredSize(new Dimension(600, 0));
        splitPane.setRightComponent(scrollPane_1);
        splitPane.setDividerLocation(400);
        
        JPanel panel_1 = new JPanel();
        
        splitPane.setLeftComponent(panel_1);
        
        //tabbedPane.addTab("Bacterium Labels", null, panel_1, null);
        

        panel_1.setLayout(new BorderLayout(0, 0));
        
        JPanel panel_2 = new JPanel();
        AlphaContainer panel_2_wrap = new AlphaContainer(panel_2);
        JScrollPane scrollPane_2 = new JScrollPane(panel_2_wrap);
        panel_1.add(scrollPane_2, BorderLayout.CENTER);

        panel_2.setLayout(new MigLayout("", "[][grow]", "[][][][][][][][][][][]"));
        
        JLabel lblCurrentSelctionText = new JLabel("Current Selction:");
        panel_2.add(lblCurrentSelctionText, "cell 0 0");
        
        JLabel lblCurrentSelction = new JLabel("None");
        panel_2.add(lblCurrentSelction, "cell 1 0");
        
        JLabel lblId = new JLabel("ID");
        panel_2.add(lblId, "cell 0 1,alignx trailing");
        
        textFieldId = new JTextField();
        textFieldId.setEnabled(false);
        panel_2.add(textFieldId, "cell 1 1,growx");
        textFieldId.setColumns(10);
        
        JLabel lblNewLabel_6 = new JLabel("Name");
        panel_2.add(lblNewLabel_6, "cell 0 2,alignx trailing");
        
        textFieldName = new JTextField();
        panel_2.add(textFieldName, "cell 1 2,growx");
        textFieldName.setColumns(10);
        
//        JLabel lblValue = new JLabel("Value");
//        panel_2.add(lblValue, "cell 0 3,alignx trailing");
//        
//        textFieldValue = new JTextField();
//        textFieldValue.setEnabled(false);
//        panel_2.add(textFieldValue, "cell 1 3,growx");
//        textFieldValue.setColumns(10);
        
        JLabel lblFillColorText = new JLabel("Fill Color");
        panel_2.add(lblFillColorText, "cell 0 3, alignx trailing");
        
        JLabel lblFillColor = new JLabel("      ");
        lblFillColor.setOpaque(true);
        lblFillColor.setBackground(Color.white);
        lblFillColor.setBorder(BorderFactory.createLineBorder(Color.black));
        lblFillColor.addMouseListener(new MouseListener() {
            
            @Override
            public void mouseReleased(MouseEvent e) {

            }
            
            @Override
            public void mousePressed(MouseEvent e) {
          
            }
            
            @Override
            public void mouseExited(MouseEvent e) {

            }
            
            @Override
            public void mouseEntered(MouseEvent e) {

            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                Color newColor = JColorChooser.showDialog(
                        thisFrame,
                        "Choose Fill Color",
                        fillColor);
                if (newColor != null) {
                    fillColor = newColor;
                    lblFillColor.setBackground(newColor);
                    shapeDisplayPanel.repaint();
                }
            }
        });
        
        panel_2.add(lblFillColor, "cell 1 3, growx");
        
        JLabel lblBorderColorText = new JLabel("Border Color");
        panel_2.add(lblBorderColorText, "cell 0 4, alignx trailing");
        
        
        JLabel lblBorderColor = new JLabel("      ");
        lblBorderColor.setOpaque(true);
        lblBorderColor.setBackground(Color.white);
        lblBorderColor.setBorder(BorderFactory.createLineBorder(Color.black));
        panel_2.add(lblBorderColor, "cell 1 4, growx");
        lblBorderColor.addMouseListener(new MouseListener() {
            
            @Override
            public void mouseReleased(MouseEvent e) {

            }
            
            @Override
            public void mousePressed(MouseEvent e) {
          
            }
            
            @Override
            public void mouseExited(MouseEvent e) {

            }
            
            @Override
            public void mouseEntered(MouseEvent e) {

            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                Color newColor = JColorChooser.showDialog(
                        thisFrame,
                        "Choose Fill Color",
                        borderColor);
                if (newColor != null) {
                    borderColor = newColor;
                    lblBorderColor.setBackground(newColor);
                    shapeDisplayPanel.repaint();
                }
            }
        });
        
        JLabel label_4 = new JLabel("Shape");
        panel_2.add(label_4, "cell 0 5,alignx trailing");
        
        JComboBox<String> comboBox = new JComboBox<String>();
        comboBox.setPreferredSize(new Dimension(100, 0));
        for (String shapeStr : ShapeManager.SHAPE_STRING_LIST){
            comboBox.addItem(shapeStr);
        }
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 currentShapeString = (String) comboBox.getSelectedItem();
                 shapeDisplayPanel.repaint();
            }
        });
        panel_2.add(comboBox, "cell 1 5,growx");
        
        JLabel label = new JLabel("Shape Size");
        panel_2.add(label, "cell 0 6");
        
        JLabel lblShapeSize = new JLabel("0px");
        JSlider sliderShapeSize = new JSlider();
        sliderShapeSize.setValue(0);
        sliderShapeSize.setPaintTicks(true);
        sliderShapeSize.setMinorTickSpacing(5);
        sliderShapeSize.setMajorTickSpacing(10);
        sliderShapeSize.addChangeListener(new ChangeListener() {
            
            @Override
            public void stateChanged(ChangeEvent e) {
                drawShapeSize = ((JSlider) (e.getSource())).getValue();
                lblShapeSize.setText(drawShapeSize + "px");
                shapeDisplayPanel.repaint();
            }
        });
        panel_2.add(sliderShapeSize, "flowx,cell 1 6");
        
        JLabel label_2 = new JLabel("Shape Thickness");
        panel_2.add(label_2, "cell 0 7");
        
        JLabel lblShapeThickness = new JLabel("0%");
        
        JSlider sliderShapeThickness = new JSlider();
        sliderShapeThickness.setValue(0);
        sliderShapeThickness.setPaintTicks(true);
        sliderShapeThickness.setMinorTickSpacing(5);
        sliderShapeThickness.setMajorTickSpacing(10);
        sliderShapeThickness.addChangeListener(new ChangeListener() {
            
            @Override
            public void stateChanged(ChangeEvent e) {
                drawShapeThicknessPercent = ((JSlider) (e.getSource())).getValue();
                lblShapeThickness.setText(drawShapeThicknessPercent + "%");
                shapeDisplayPanel.repaint();
            }
        });
        panel_2.add(sliderShapeThickness, "flowx,cell 1 7");
        
        
        
        panel_2.add(lblShapeSize, "cell 1 6");
        

        panel_2.add(lblShapeThickness, "cell 1 7");
        
        JButton btnApply = new JButton("Apply");
        btnApply.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (selectedListItem != null){
                    settingChanged = true;
                    LabelDescriptor newDescriptor = new LabelDescriptor(
                            Long.valueOf(textFieldId.getText()),
                            textFieldName.getText(),
                            fillColor,
                            borderColor,
                            currentShapeString,
                            drawShapeSize,
                            (int) Math.round(drawShapeThicknessPercent * drawShapeSize / 100.0) );
                    labelListModel.remove(selectedListIndex);
                    labelListModel.insertElementAt(new ListSelectionWrapper(newDescriptor.getId(), newDescriptor.getName()), selectedListIndex);
                    
                    labelDescriptorTable.replace(selectedListItem.getId(), newDescriptor);
//                    labelDescriptorTable.remove(selectedListItem.getId());
//                    labelDescriptorTable.put(newDescriptor.getId(), newDescriptor);
                    listLabels.revalidate();
                    
                }
            }
        });
        
        
        panel_2.add(btnApply, "cell 0 8, span, growx");
        
        labelListModel = createLabelListModel(labelDescriptorTable);
        listLabels = new JList<ListSelectionWrapper>(labelListModel);
        listLabels.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listLabels.setLayoutOrientation(JList.VERTICAL);
        listLabels.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (renderer instanceof JLabel && value instanceof ListSelectionWrapper) {
                    // Here value will be of the Type 'CD'
                    ((JLabel) renderer).setText(labelDescriptorTable.get(((ListSelectionWrapper) value).getId()).getName());
                }
                return renderer;
            }
        });
        listLabels.addListSelectionListener(new ListSelectionListener() {
            
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getSource() instanceof JList<?>){
                    if (((JList<ListSelectionWrapper>) e.getSource()).getSelectedIndex() != -1){
                        selectedListItem = ((JList<ListSelectionWrapper>) e.getSource()).getSelectedValue();
                        selectedListIndex = ((JList<ListSelectionWrapper>) e.getSource()).getSelectedIndex();
                        
                        LabelDescriptor selectedDescriptor = labelDescriptorTable.get(selectedListItem.getId());
                        lblCurrentSelction.setText(selectedListItem.getName());
                        textFieldId.setText(String.valueOf(selectedListItem.getId()));
                        textFieldName.setText(selectedDescriptor.getName());
//                        textFieldValue.setText(String.valueOf(selectedDescriptor.getValue()));
                        
                        fillColor = selectedDescriptor.getDraw_fill_color();
                        borderColor = selectedDescriptor.getDraw_border_color();
                        lblFillColor.setBackground(fillColor);
                        lblBorderColor.setBackground(borderColor);
                        //TODO
                        currentShapeString = selectedDescriptor.getShapeString();
                        drawShapeSize = selectedDescriptor.getShapeSize();
                        drawShapeThicknessPercent = (int) Math.round(selectedDescriptor.getShapeThickness() * 100.0 / selectedDescriptor.getShapeSize());
                        sliderShapeSize.setValue(drawShapeSize);
                        lblShapeSize.setText(drawShapeSize + "px");
                        sliderShapeThickness.setValue(drawShapeThicknessPercent);
                        lblShapeThickness.setText(drawShapeThicknessPercent + "%");
                        comboBox.setSelectedItem(currentShapeString);
                        panel_2_wrap.repaint();
                        shapeDisplayPanel.repaint();
                    }
                }

            }
        });
        panel_1.add(listLabels, BorderLayout.WEST);
        
        
        
        JButton btnNewButton = new JButton("Save All Settings", new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClicklableMain.class.getResource("/images/save_icon_A_18.png"))));
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                exportSettings();
                settingChanged = false;
            }
        });
        contentPane.add(btnNewButton, BorderLayout.SOUTH);
        settingChanged = false;
        shapeDisplayPanel.repaint();

    }
    
    private DefaultListModel<ListSelectionWrapper> createLabelListModel(HashMap<Long, LabelDescriptor> labelDescriptorTable){
        DefaultListModel<ListSelectionWrapper> listModel = new DefaultListModel();
        for (Map.Entry<Long, LabelDescriptor> entry : labelDescriptorTable.entrySet()) {
            listModel.addElement(new ListSelectionWrapper(entry.getValue().getId(), entry.getValue().getName()));
        }
        return listModel;
    }
    
    public void importSettings(){
        //drawShapeSize = mainFrame.drawShapeSize;
        //drawShapeThicknessPercent = mainFrame.drawShapeThickness * 100 / drawShapeSize;
        shapeScaleToZoom = mainFrame.drawShapeScaleToZoom;
        snapSensitivity = mainFrame.snapBound;
        zoomSensitivity = mainFrame.zoomSensitivity;
        //currentShapeString = mainFrame.currentShapeString;
        labelDescriptorTable = mainFrame.labelDescriptorTable;
    }
    
    
    public void exportSettings(){
        //mainFrame.drawShapeSize = drawShapeSize;
        //mainFrame.drawShapeThickness = drawShapeSize * drawShapeThicknessPercent / 100;
        mainFrame.drawShapeScaleToZoom = shapeScaleToZoom;
        mainFrame.snapBound = snapSensitivity;
        mainFrame.zoomSensitivity = zoomSensitivity;
        //mainFrame.currentShapeString = currentShapeString;
        mainFrame.refreshUI();
    }
    
    class ListSelectionWrapper {
        long id;
        String name;
        
        public ListSelectionWrapper(long id, String name){
            this.id = id;
            this.name = name;
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
    }

    
    class ShapeDisplayPanel extends JPanel{
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.lightGray);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            Shape shape = ShapeManager.getShapeByString(currentShapeString, this.getWidth() / 2, this.getHeight() / 2,
                    drawShapeSize, drawShapeSize * drawShapeThicknessPercent / 100);
            g2d.setColor(fillColor);
            g2d.fill(shape);
            g2d.setColor(borderColor);
            g2d.draw(shape);


        }
        
    }
    
    /***************************************************************************************
    *    Title: Backgrounds With Transparency
    *    Author: Rob Camick
    *    Date: May 31, 2009
    *    Code version: Unknown
    *    Availability: https://tips4java.wordpress.com/2009/05/31/backgrounds-with-transparency/
    *
    ***************************************************************************************/
    
    /**
     *  A wrapper Container for holding components that use a background Color
     *  containing an alpha value with some transparency.
     *
     *  A Component that uses a transparent background should really have its
     *  opaque property set to false so that the area it occupies is first painted
     *  by its opaque ancestor (to make sure no painting artifacts exist). However,
     *  if the property is set to false, then most Swing components will not paint
     *  the background at all, so you lose the transparent background Color.
     *
     *  This components attempts to get around this problem by doing the
     *  background painting on behalf of its contained Component, using the
     *  background Color of the Component.
     */
    public class AlphaContainer extends JComponent
    {
        private JComponent component;

        public AlphaContainer(JComponent component)
        {
            this.component = component;
            setLayout( new BorderLayout() );
            setOpaque( false );
            component.setOpaque( false );
            add( component );
        }

        /**
         *  Paint the background using the background Color of the
         *  contained component
         */
        @Override
        public void paintComponent(Graphics g)
        {
            g.setColor( component.getBackground() );
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

}
