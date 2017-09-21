package hk.edu.hku.cs.citang.urfp.supp.label.gui;

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.FlowLayout;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import net.miginfocom.swing.MigLayout;
import javax.swing.JRadioButton;
import java.awt.event.InputMethodListener;
import java.awt.event.InputMethodEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.BoxLayout;
import java.awt.Component;
import javax.swing.Box;
import java.awt.Rectangle;
import java.awt.ComponentOrientation;

public class ClicklableMain {
    
    private static final String VERSION = "1.0";

    private JFrame frame;
    private DrawPanel drawPanel;
    
    
    private Point currentCursorPos;
    private JLabel currentCursorPosLb;
    private boolean changeMade = false;
    
    private String currentDirectory = System.getProperty("user.dir");
    private long currentImgID = -1;
    private String currentImgName = null;
    private String currentImgPath = null;
    private JLabel currentImgLb;
    private BufferedImage currentImg = null;
    private double currentZoom = 1;
    
    DatabaseManager db;
    private String databasePath = "data/db/filelable.db";
    
    private JSlider sliderZoom;
    private int currentZoomSliderPos = 10;
    
    private int currentEditMode = 1;
    private boolean snapEnabled = false;
    private int snapBound = 30;
    
    private HashMap<Point, Integer> pointLabelTable;
    private HashMap<Integer, String> labelNameTable;
    private HashMap<Integer, Color> labelColorTable;
    
    private int currentLabel = 1; 
    
    private int drawOvalSize = 16;
    private int drawOvalThickness = 8;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            // Set cross-platform Java L&F
            
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ClicklableMain window = new ClicklableMain();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public ClicklableMain() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setTitle("Clicklable V" + VERSION);
        frame.setBounds(100, 100, 660, 456);
        frame.setSize(1000, 600);
        frame.addWindowListener(new MainWindowCloseHandler());
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        drawPanel = new DrawPanel();
        drawPanel.setBackground(Color.white);
        
        pointLabelTable = new HashMap<Point, Integer>();
        labelColorTable = new HashMap<Integer, Color>();
        labelNameTable = new HashMap<Integer, String>();
        
        JPanel panel = new JPanel();
        frame.getContentPane().add(panel, BorderLayout.NORTH);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        
        JButton btnExport = new JButton("Export");
        btnExport.addActionListener(new ExportDatabaseHandler());

        btnExport.setAutoscrolls(true);
        panel.add(btnExport);
        
        JButton btnImport = new JButton("Import");
        btnImport.addActionListener(new ImportDatabaseHandler());
        panel.add(btnImport);
        
        currentImgLb = new JLabel("Current Image: None");
        panel.add(currentImgLb);
        
        Component horizontalGlue_1 = Box.createHorizontalGlue();
        panel.add(horizontalGlue_1);
        
        JButton btnSave = new JButton("Save Changes");
        btnSave.addActionListener(new saveCurrentLabelsHandler());
        
        panel.add(btnSave);
        
        JButton btnLoadFile = new JButton("Load Image");
        panel.add(btnLoadFile);
        btnLoadFile.addActionListener(new LoadImageHandler());
        
        JScrollPane scrollPane = new JScrollPane(drawPanel);
        scrollPane.setAutoscrolls(true);
        JPanel panelControl = new JPanel();
        JScrollPane scrollPane_2 = new JScrollPane(panelControl);
        panelControl.setLayout(new MigLayout("", "[][][]", "[][]"));
        
        
        
        JLabel lblNewLabel_1 = new JLabel("Mode");
        panelControl.add(lblNewLabel_1, "growx, wrap");
        
        ButtonGroup rdbtngFunction = new ButtonGroup();
        
        FunctionSelectHandler rdbtnFuncHandler = new FunctionSelectHandler();
        
        JRadioButton rdbtnFuncAdd = new JRadioButton("Add");
        rdbtnFuncAdd.addActionListener(rdbtnFuncHandler);
        rdbtnFuncAdd.setActionCommand("1");
        rdbtnFuncAdd.setSelected(true);
        rdbtngFunction.add(rdbtnFuncAdd);
        panelControl.add(rdbtnFuncAdd, "wrap");
        
        JRadioButton rdbtnFuncMod = new JRadioButton("Modify");
        rdbtnFuncMod.addActionListener(rdbtnFuncHandler);
        rdbtnFuncMod.setActionCommand("2");
        rdbtngFunction.add(rdbtnFuncMod);
        panelControl.add(rdbtnFuncMod, "wrap");
        
        JRadioButton rdbtnFuncRemove = new JRadioButton("Remove");
        rdbtnFuncRemove.addActionListener(rdbtnFuncHandler);
        rdbtnFuncRemove.setActionCommand("4");
        rdbtngFunction.add(rdbtnFuncRemove);
        panelControl.add(rdbtnFuncRemove, "wrap");
        
        JCheckBox cbSnapEnabled = new JCheckBox("Snap");
        
        cbSnapEnabled.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                snapEnabled = cbSnapEnabled.isSelected();
            }
        });
        panelControl.add(cbSnapEnabled, "wrap");
        
        JSeparator separator = new JSeparator();
        panelControl.add(separator, "growx, span, wrap");
        
        JLabel lblNewLabel_3 = new JLabel("Bacterium Label");
        panelControl.add(lblNewLabel_3, "growx, wrap");
        
        ButtonGroup rdbtngLabel = new ButtonGroup();
        
        labelColorTable.put(1, Color.YELLOW);
        labelColorTable.put(2, Color.RED);
        labelColorTable.put(3, Color.GREEN);
        labelColorTable.put(4, Color.WHITE);
        labelNameTable.put(1, "Lactobacillus");
        labelNameTable.put(2, "Gardnerella");
        labelNameTable.put(3, "Curved Rod");
        labelNameTable.put(4, "Other");
        
        LabelSelectHandler rdbtnLabelHandler = new LabelSelectHandler();
        for (int i = 1; i <= 4; i++){
            JRadioButton rdbtnlabel = new JRadioButton(labelNameTable.get(i));
            if (i == 1){
                rdbtnlabel.setSelected(true);
            }
            rdbtnlabel.addActionListener(rdbtnLabelHandler);
            rdbtnlabel.setActionCommand("" + i);
            rdbtngLabel.add(rdbtnlabel);
            panelControl.add(rdbtnlabel, "wrap");
        }

        
//        JRadioButton rdbtnNewRadioButton_3 = new JRadioButton("Class 1");
//        panelControl.add(rdbtnNewRadioButton_3, "wrap");
//        
//        JRadioButton rdbtnNewRadioButton_4 = new JRadioButton("Class 2");
//        panelControl.add(rdbtnNewRadioButton_4, "wrap");
//        
//        JRadioButton rdbtnNewRadioButton_5 = new JRadioButton("Class 3");
//        panelControl.add(rdbtnNewRadioButton_5, "wrap");
//        
//        JRadioButton rdbtnNewRadioButton_6 = new JRadioButton("Class 4");
//        panelControl.add(rdbtnNewRadioButton_6, "wrap");
//        
        JSeparator separator2 = new JSeparator();
        panelControl.add(separator2, "growx, span, wrap");
        
//        JLabel lblNewLabel_4 = new JLabel("New label");
//        panelControl.add(lblNewLabel_4, "growx, wrap");
//        
//        JSlider slider = new JSlider();
//        slider.setMinorTickSpacing(5);
//        slider.setMajorTickSpacing(10);
//        slider.setSnapToTicks(true);
//        slider.setPaintTicks(true);
//        slider.setPaintLabels(true);
//        panelControl.add(slider, "span, wrap");
        
//        JSeparator separator3 = new JSeparator();
//        panelControl.add(separator3, "growx, span, wrap");
//        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, scrollPane_2);
        splitPane.setResizeWeight(0.7);
        splitPane.setContinuousLayout(true);
        frame.getContentPane().add(splitPane, BorderLayout.CENTER);
        
        
        
        JPanel panel_1 = new JPanel();
        panel_1.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        panel_1.setBounds(new Rectangle(10, 0, 0, 0));
        frame.getContentPane().add(panel_1, BorderLayout.SOUTH);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
        
        currentCursorPosLb = new JLabel("Current Position: ");
        panel_1.add(currentCursorPosLb);
        
        Component horizontalGlue = Box.createHorizontalGlue();
        panel_1.add(horizontalGlue);
        
        FlowLayout fl_panel_2 = new FlowLayout();
        fl_panel_2.setAlignment(FlowLayout.RIGHT);
        JPanel panel_2 = new JPanel(fl_panel_2);
        panel_2.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        
        panel_1.add(panel_2);
        
        JLabel lblNewLabel = new JLabel("Zoom: 100.00%");
        lblNewLabel.setPreferredSize(new Dimension(100, 26));
        
        sliderZoom = new JSlider();
        panel_2.add(sliderZoom);
        sliderZoom.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (currentZoomSliderPos != source.getValue()) {
                    currentZoomSliderPos = source.getValue();
                    
                    JScrollPane scroll = (JScrollPane) drawPanel.getParent().getParent();
                    Point prevPos = scroll.getViewport().getViewPosition();
                    double prevZoom = currentZoom;
                    
                    currentZoom = Math.pow(2, (source.getValue() - 10) / 2f);
                    lblNewLabel.setText(String.format("Zoom: %.2f%%", currentZoom * 100));

                    drawPanel.repaint();

                    scroll.revalidate();
                    Point newPos = new Point(
                            (int) (prevPos.getX() * (currentZoom / prevZoom) + scroll.getViewport().getExtentSize().getWidth() / 2 * (currentZoom / prevZoom - 1)), 
                            (int) (prevPos.getY() * (currentZoom / prevZoom) + scroll.getViewport().getExtentSize().getHeight() / 2 * (currentZoom / prevZoom - 1)));
                    scroll.getViewport().setViewPosition(newPos);

                }
                
            }
        });
        sliderZoom.setMaximum(20);
        sliderZoom.setValue(10);
        sliderZoom.setPaintTicks(true);
        sliderZoom.setMinorTickSpacing(1);
        sliderZoom.setMajorTickSpacing(2);
        sliderZoom.setSnapToTicks(true);
        

        panel_2.add(lblNewLabel);
        panel_2.setPreferredSize(panel_2.getPreferredSize());
        
        try {
            db = new DatabaseManager(databasePath);
        } catch (SQLException e1) {
            e1.printStackTrace();
        }

    }
    
    class MainWindowCloseHandler implements WindowListener {

        @Override
        public void windowActivated(WindowEvent arg0) {
        }

        @Override
        public void windowClosed(WindowEvent arg0) {
        }

        @Override
        public void windowClosing(WindowEvent arg0) {
            if (changeMade){
                int dialogResult = JOptionPane.showConfirmDialog(frame, "Do you want to save the changes before quitting?",
                        "Reminder", JOptionPane.YES_NO_CANCEL_OPTION);
                if (dialogResult == JOptionPane.YES_OPTION){
                    try {
                        db.updatePointLabels(currentImgID, pointLabelTable);
                        db.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    System.exit(0);
                } else if (dialogResult == JOptionPane.NO_OPTION) {
                    try {
                        db.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    System.exit(0);
                } else if (dialogResult == JOptionPane.CANCEL_OPTION) {

                }
                return;
            } else {
                System.exit(0);
            }
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
        
    }
    
    class ImportDatabaseHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (changeMade){
                int dialogResult = JOptionPane.showConfirmDialog(frame, "Are you sure that you want to import a new database? Your current data will be overwritten.",
                        "Reminder", JOptionPane.OK_CANCEL_OPTION);
                if (dialogResult == JOptionPane.OK_OPTION){
                    try {
                        db.updatePointLabels(currentImgID, pointLabelTable);
                        db.databaseExportAsCSV("data/backup/backup.csv");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (dialogResult == JOptionPane.CANCEL_OPTION) {
                    return;
                }
            }
            JFileChooser chooser = new JFileChooser();
            int returnVal = chooser.showSaveDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                
                File chosenFile = chooser.getSelectedFile();
                try {
                    db.clearAllData();
                    db.databaseImportFromCSV(chosenFile.getAbsolutePath());
                    changeMade = false;
                    currentImgID = -1;
                    currentImgName = null;
                    currentImgPath = null;
                    currentImgLb.setText("Current Image: None");;
                    currentImg = null;
                    frame.setTitle("Clicklable V" + VERSION);
                    pointLabelTable.clear();
                    drawPanel.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
    }
    
    class ExportDatabaseHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (changeMade){
                int dialogResult = JOptionPane.showConfirmDialog(frame, "Do you want to save the changes before exporting the database?",
                        "Reminder", JOptionPane.YES_NO_CANCEL_OPTION);
                if (dialogResult == JOptionPane.YES_OPTION){
                    try {
                        db.updatePointLabels(currentImgID, pointLabelTable);
                        changeMade = false;
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else if (dialogResult == JOptionPane.NO_OPTION) {

                } else if (dialogResult == JOptionPane.CANCEL_OPTION) {
                    return;
                }
            }
            JFileChooser chooser = new JFileChooser();
            int returnVal = chooser.showSaveDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                
                File chosenFile = chooser.getSelectedFile();
                try {
                    db.databaseExportAsCSV(chosenFile.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
    }
    
    
    class LoadImageHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (changeMade){
                int dialogResult = JOptionPane.showConfirmDialog(frame, "Do you want to save the changes before loading the new image?",
                        "Reminder", JOptionPane.YES_NO_CANCEL_OPTION);
                if (dialogResult == JOptionPane.YES_OPTION){
                    try {
                        db.updatePointLabels(currentImgID, pointLabelTable);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else if (dialogResult == JOptionPane.NO_OPTION) {

                } else if (dialogResult == JOptionPane.CANCEL_OPTION) {
                    return;
                }
            }
            
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(currentDirectory));
            int returnVal = chooser.showSaveDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                
                File chosenFile = chooser.getSelectedFile();

                //System.out.println(chosenFile.getAbsolutePath());
                try {
                    
                    currentImg = ImageIO.read(chosenFile);
                    drawPanel.setPreferredSize(new Dimension(currentImg.getWidth(), currentImg.getHeight()));
                    drawPanel.repaint();
                    
                    List<Long> filesByPath = db.getFileIDByPath(chosenFile);
                    if (filesByPath.size() == 1){
                        currentImgID = filesByPath.get(0);
                        db.updateFile(currentImgID, chosenFile);
                        System.out.println("Open File Path Match");
                    } else {
                        List<Long> filesByHash = db.getFileIDByHash(chosenFile);
                        if (filesByHash.size() == 1){
                            currentImgID = filesByHash.get(0);
                            db.updateFile(currentImgID, chosenFile);
                            System.out.println("Open File Hash Match");
                        } else {
                            currentImgID = db.insertFile(chosenFile);
                            System.out.println("New File Inserted");
                        }
                    }
                    pointLabelTable = db.getPointLabelsByFileID(currentImgID);
                    currentImgPath = chosenFile.getPath();
                    currentImgName = chosenFile.getName();
                    currentImgLb.setText(currentImgPath);
                    changeMade = false;
                    frame.setTitle("Clicklable V" + VERSION + " - " + currentImgName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    class saveCurrentLabelsHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            try {
                db.updatePointLabels(currentImgID, pointLabelTable);
                changeMade = false;
                frame.setTitle("Clicklable V" + VERSION + " - " + currentImgName);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }
    
    class FunctionSelectHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            currentEditMode = Integer.valueOf(e.getActionCommand());
        }
        
    }
    
    class LabelSelectHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            currentLabel = Integer.valueOf(e.getActionCommand());
        }
        
    }
    
    class DrawPanel extends JPanel implements MouseInputListener, MouseWheelListener {
        
        private Point snapPoint = null;
        private Color colorHighLight = new Color(255, 255, 255, 128);
        //private Color colorHighLight = Color.BLACK;
        
        public Dimension getPreferredSize() {
            return currentImg == null ? new Dimension(500, 500) : new Dimension((int) (currentImg.getWidth() * currentZoom), (int) (currentImg.getHeight() * currentZoom));
        }
        
        public DrawPanel(){
            super();
            addMouseListener(this);
            addMouseMotionListener(this);
            addMouseWheelListener(this);
            setPreferredSize(new Dimension(500,500));
        }
        
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            g2d.setColor(Color.white);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            if (currentImg != null){
                  AffineTransform at = AffineTransform.getScaleInstance(currentZoom, currentZoom);
                  AffineTransformOp atop = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
                  g2d.drawImage(currentImg, atop, 0, 0);
                  
                  for (Map.Entry<Point, Integer> entry : pointLabelTable.entrySet()) {
                      Point key = entry.getKey();
                      Integer value = entry.getValue();
                      
                      Shape ring = createRingShape(key.getX() * currentZoom, key.getY() * currentZoom, drawOvalSize * currentZoom, drawOvalThickness * currentZoom); 
                      g2d.setColor(labelColorTable.get(value));
                      g2d.fill(ring);
                      g2d.setColor(Color.BLACK);
                      g2d.draw(ring);
                  }
                  
                  if (snapEnabled && snapPoint != null){
                      //System.out.println(snapPoint);
                      g2d.setColor(colorHighLight);
                      g2d.fillOval((int) ((snapPoint.getX() - snapBound) * currentZoom), (int) ((snapPoint.getY() - snapBound) * currentZoom), (int) (snapBound * 2 * currentZoom), (int) (snapBound * 2 * currentZoom));
                  }
            }
            
            

            
        }
        
        public Point getClosestPoint(Point q){
            double minDist = snapBound;
            Point closestP = null;
            for (Point p : pointLabelTable.keySet()) {
                double d = p.distance(q);
                if (d <= minDist){
                    closestP = p;
                    minDist = d;
                }                
            }
            return closestP;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            
            Point editP;
            if (snapEnabled && snapPoint != null){
                editP = snapPoint;
            } else {
                editP = new Point((int) (e.getPoint().getX() / currentZoom), (int) (e.getPoint().getY() / currentZoom));
            }
            
            if (currentEditMode == 1 || currentEditMode == 2){
                pointLabelTable.put(editP, currentLabel);
            } else if (currentEditMode == 4){
                pointLabelTable.remove(editP);
            }
            
            changeMade = true;
            frame.setTitle("Clicklable V" + VERSION + " - " + currentImgName + "*");
            System.out.println(e.getPoint().toString() + " " + pointLabelTable.size());
            repaint();
        }

        @Override
        public void mouseEntered(MouseEvent arg0) {

        }

        @Override
        public void mouseExited(MouseEvent arg0) {

        }

        @Override
        public void mousePressed(MouseEvent arg0) {

        }

        @Override
        public void mouseReleased(MouseEvent arg0) {

        }

        @Override
        public void mouseDragged(MouseEvent arg0) {

        }

        @Override
        public void mouseMoved(MouseEvent e) {
            currentCursorPos = new Point((int) (e.getX() / currentZoom), (int) (e.getY() / currentZoom));
            currentCursorPosLb.setText("Current Position: (" + (int) (e.getX() / currentZoom) + ", " + (int) (e.getY() / currentZoom) + ")");
            if (snapEnabled && currentImg != null){
                Point newSnapPoint = getClosestPoint(currentCursorPos);
                
                if (newSnapPoint != null && snapPoint == null || newSnapPoint == null && snapPoint != null){
                    snapPoint = newSnapPoint;
                    repaint();
                } else if (newSnapPoint != null && snapPoint != null){
                    if (!snapPoint.equals(newSnapPoint)){
                        snapPoint = newSnapPoint;
                        repaint();
                    }
                }
                
            }
        }
        
        private Shape createRingShape(
                double centerX, double centerY, double outerRadius, double thickness)
        {
            Ellipse2D outer = new Ellipse2D.Double(
                centerX - outerRadius, 
                centerY - outerRadius,
                outerRadius + outerRadius, 
                outerRadius + outerRadius);
            Ellipse2D inner = new Ellipse2D.Double(
                centerX - outerRadius + thickness, 
                centerY - outerRadius + thickness,
                outerRadius + outerRadius - thickness - thickness, 
                outerRadius + outerRadius - thickness - thickness);
            Area area = new Area(outer);
            area.subtract(new Area(inner));
            return area;
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            sliderZoom.setValue(sliderZoom.getValue() - e.getWheelRotation());
        }
        
    }
}
