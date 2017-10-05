package hk.edu.hku.cs.citang.urfp.supp.label.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputListener;
import javax.swing.event.PopupMenuListener;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import net.miginfocom.swing.MigLayout;

public class ClicklableMain {

    private static final String VERSION = "1.2";
    private static final String APPLICATION_NAME = "Clicklable";

    private ClicklableMain window;
    
    private JFrame frame;
    private DrawPanel drawPanel;
    private LabelControlPanel panelLabelControl;

    private Point currentCursorPos;
    private JLabel currentCursorPosLb;
    
    private boolean changeMade = false;
    private int historyCountMax = 20;
    private Deque<ChangeDescriptor> changesHistory = new LinkedList<ChangeDescriptor>();
    private Deque<ChangeDescriptor> undoHistory = new LinkedList<ChangeDescriptor>();
    
    private String currentWorkingDirectory = System.getProperty("user.dir");

    private String currentImgDirectory = currentWorkingDirectory;
    private long currentImgID = -1;
    private String currentImgName = null;
    private String currentImgPath = null;
    private JLabel currentImgLb;
    private BufferedImage currentImg = null;
    private double currentZoom = 1;

    DatabaseManager db;
    ConfigManager config;
    private String databasePath = "data/db/filelabel.db";
    private String configPath = "data/user.config";

    private JLabel lblZoom;
    private JSlider sliderZoom;
    private int currentZoomSliderPos = 10;

    private int currentEditMode = 1;
    JCheckBox cbSnapEnabled;
    private boolean snapEnabled = false;
    int snapBound = 30;
    int zoomSensitivity = 10;

    private HashMap<Point, Long> pointLabelTable;
    HashMap<Long, LabelDescriptor> labelDescriptorTable;
    


    private long currentLabel = 1;

    //int drawShapeSize = 16;
    //int drawShapeThickness = 8;
    boolean drawShapeScaleToZoom = true;
    //String currentShapeString = "Ring";

    private String recentImportDirectory = currentWorkingDirectory;
    private String recentExportDirectory = currentWorkingDirectory;

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
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ClicklableMain window = new ClicklableMain();
                    window.window = window;
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
        //frame.setIconImage(Toolkit.getDefaultToolkit().getImage(ClicklableMain.class.getResource("/javax/swing/plaf/basic/icons/JavaCup16.png")));
        frame.setTitle(APPLICATION_NAME);
        frame.setBounds(100, 100, 660, 456);
        frame.setSize(1000, 600);
        frame.setJMenuBar(new ControlMenuBar());
        frame.setFocusable(true);
        //frame.addKeyListener(new MainKeyListener());
        frame.addWindowListener(new WindowListener() {

            @Override
            public void windowClosed(WindowEvent arg0) {
            }

            @Override
            public void windowClosing(WindowEvent arg0) {
                terminateApplication();
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
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        labelDescriptorTable = new HashMap<Long, LabelDescriptor>();
        config = new ConfigManager();
        File configFile = new File(configPath);
        if (configFile.exists()){
            config.parseXMLConfig(new File(configPath));
        } else {
            LabelDescriptor ld = new LabelDescriptor(1, "Lactobacillus", new Color(-2130706688, true), Color.BLACK, "UprightCross", 16, 2);
            labelDescriptorTable.put(ld.id, ld);
            
            ld = new LabelDescriptor(2, "Gardnerella", new Color(-2130771968, true), Color.BLACK, "UprightCross", 16, 2);
            labelDescriptorTable.put(ld.id, ld);
            
            ld = new LabelDescriptor(3, "Curved Rod", new Color(-2147418368, true), Color.BLACK, "UprightCross", 16, 2);
            labelDescriptorTable.put(ld.id, ld);
            
            ld = new LabelDescriptor(4, "Unknown A", new Color(-2130706433, true), Color.BLACK, "Ring", 16, 8);
            labelDescriptorTable.put(ld.id, ld);
            
            ld = new LabelDescriptor(5, "Unknown B", new Color(-2130706433, true), Color.BLACK, "Square", 12, 4);
            labelDescriptorTable.put(ld.id, ld);
            
            ld = new LabelDescriptor(6, "Unknown C", new Color(-2130706433, true), Color.BLACK, "UprightCross", 8, 3);
            labelDescriptorTable.put(ld.id, ld);
            
        }
        drawPanel = new DrawPanel();
        drawPanel.setBackground(Color.white);

        pointLabelTable = new HashMap<Point, Long>();
        

        JPanel panel = new JPanel();
        frame.getContentPane().add(panel, BorderLayout.NORTH);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        currentImgLb = new JLabel("Current Image: None");
        panel.add(currentImgLb);

        Component horizontalGlue_1 = Box.createHorizontalGlue();
        panel.add(horizontalGlue_1);

        JButton btnSave = new JButton("Save Changes",  new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClicklableMain.class.getResource("/images/save_icon_A_18.png"))));
        btnSave.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCurrentLabels();
            }
        });

        panel.add(btnSave);

        JButton btnLoadFile = new JButton("Load Image", new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClicklableMain.class.getResource("/images/open_icon_A_18.png"))) );
        panel.add(btnLoadFile);
        btnLoadFile.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                loadImage();
            }
        });

        JScrollPane scrollPane = new JScrollPane(drawPanel);
        drawPanel.setParentScrollPane(scrollPane);
        scrollPane.setAutoscrolls(true);
        JPanel panelControl = new JPanel();
        JScrollPane scrollPane_2 = new JScrollPane(panelControl);
        panelControl.setLayout(new MigLayout("", "[][][]", "[][]"));

        JLabel lblNewLabel_1 = new JLabel("Edit Mode");
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

        cbSnapEnabled = new JCheckBox("Snap");

        cbSnapEnabled.addActionListener(new ActionListener() {
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

        


        panelLabelControl = new LabelControlPanel(labelDescriptorTable);
        panelControl.add(panelLabelControl, "growx, span, wrap");
        
        JSeparator separator2 = new JSeparator();
        panelControl.add(separator2, "growx, span, wrap");

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, scrollPane_2);
        splitPane.setResizeWeight(1.0);
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

        lblZoom = new JLabel("Zoom: 100.00%");
        lblZoom.setPreferredSize(new Dimension(100, 26));

        sliderZoom = new JSlider();
        panel_2.add(sliderZoom);
        sliderZoom.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (currentZoomSliderPos != source.getValue()) {
                    currentZoomSliderPos = source.getValue();
                    double newZoom = getZoomFactor(source.getValue());
                    drawPanel.zoomAtCenter(newZoom);
                }
            }
        });
        sliderZoom.setMaximum(20);
        sliderZoom.setValue(10);
        sliderZoom.setPaintTicks(true);
        sliderZoom.setMinorTickSpacing(1);
        sliderZoom.setMajorTickSpacing(2);
        sliderZoom.setSnapToTicks(true);

        panel_2.add(lblZoom);
        panel_2.setPreferredSize(panel_2.getPreferredSize());

        
        try {
            db = new DatabaseManager(databasePath);
            db.updateLabel(labelDescriptorTable);
        } catch (SQLException e1) {
            e1.printStackTrace();
        }

    }

    public void terminateApplication() {
        if (changeMade) {
            int dialogResult = JOptionPane.showConfirmDialog(frame, "Do you want to save the changes before quitting?",
                    "Reminder", JOptionPane.YES_NO_CANCEL_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
                try {
                    db.updateLabel(labelDescriptorTable);
                    db.updatePointLabels(currentImgID, pointLabelTable);
                    
                    db.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else if (dialogResult == JOptionPane.NO_OPTION) {
                try {
                    db.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else if (dialogResult == JOptionPane.CANCEL_OPTION) {
                return;
            }
            
        }
        try {
            config.createXMLConfig(new File(configPath));
        } catch (Exception e){
            e.printStackTrace();
        }
        
        System.exit(0);
    }
    
    
    public void refreshUI(){
        panelLabelControl.createNewLabels(labelDescriptorTable);
        drawPanel.createNewLabelMenu(labelDescriptorTable);
        try {
            db.updateLabel(labelDescriptorTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        drawPanel.repaint();
    }
    
    public void revertChange(){
        if (changesHistory.size() != 0){
            //System.out.println("REVERT");
            ChangeDescriptor prevChange = changesHistory.getLast();
            if (prevChange.getAction() == ChangeDescriptor.ACTION_ADD){
                pointLabelTable.remove(prevChange.getPointAfter());
            } else if (prevChange.getAction() == ChangeDescriptor.ACTION_MODIFY){
                pointLabelTable.remove(prevChange.getPointAfter(), prevChange.getLabelAfter());
                pointLabelTable.put(prevChange.getPointBefore(), prevChange.getLabelBefore());
            } else if (prevChange.getAction() == ChangeDescriptor.ACTION_REMOVE){
                pointLabelTable.put(prevChange.getPointBefore(), prevChange.getLabelBefore());
            }
            undoHistory.addFirst(prevChange);;
            changesHistory.removeLast();
            changeMade = true;
            frame.setTitle(APPLICATION_NAME + " - " + currentImgName + "*");
        }
        drawPanel.repaint();
    }
    
    public void redoChange(){
        if (undoHistory.size() != 0){
            //System.out.println("REVERT");
            ChangeDescriptor prevChange = undoHistory.getFirst();
            if (prevChange.getAction() == ChangeDescriptor.ACTION_ADD){
                pointLabelTable.put(prevChange.getPointAfter(), prevChange.getLabelAfter());
            } else if (prevChange.getAction() == ChangeDescriptor.ACTION_MODIFY){
                pointLabelTable.remove(prevChange.getPointBefore(), prevChange.getLabelBefore());
                pointLabelTable.put(prevChange.getPointAfter(), prevChange.getLabelAfter());
            } else if (prevChange.getAction() == ChangeDescriptor.ACTION_REMOVE){
                pointLabelTable.remove(prevChange.getPointBefore(), prevChange.getLabelBefore());
            }
            changesHistory.addLast(prevChange);
            undoHistory.removeFirst();
            changeMade = true;
            frame.setTitle(APPLICATION_NAME + " - " + currentImgName + "*");
        }
        drawPanel.repaint();
    }
    
    public double getZoomFactor(int zoomValue){
        double exponent = Math.pow(2, (zoomSensitivity - 15) / 5f);
        return Math.pow(2, (zoomValue - 10) * exponent);
    }

    class MainKeyListener implements KeyListener{

        public void keyPressed(KeyEvent e) {
            //System.out.println("Pressed: "  + e.getModifiers() + " " + e.getKeyChar());
            if (e.isControlDown()){
                if (e.getKeyCode() == KeyEvent.VK_S){
                    //System.out.println("Typed: "  + e.getModifiers() + " " + e.getKeyChar());
                    saveCurrentLabels();
                } else if (e.getKeyCode() == KeyEvent.VK_O){
                    loadImage();
                }
            }
        }

        public void keyReleased(KeyEvent e) {
            //System.out.println("Released: "  + e.getModifiers() + " " + e.getKeyChar());
        }
        public void keyTyped(KeyEvent e) {
            //System.out.println("Typed: "  + e.getModifiers() + " " + e.getKeyChar());

        }
        
    }

    
    public void importDatabase(){
        if (changeMade) {
            int dialogResult = JOptionPane.showConfirmDialog(frame,
                    "Are you sure that you want to import a new database? Your current data will be overwritten.",
                    "Reminder", JOptionPane.OK_CANCEL_OPTION);
            if (dialogResult == JOptionPane.OK_OPTION) {
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
        File dir = new File(recentImportDirectory);
        if (!dir.exists()){
            dir = new File(currentWorkingDirectory);
        }
        chooser.setCurrentDirectory(dir);
        int returnVal = chooser.showSaveDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            File chosenFile = chooser.getSelectedFile();
            recentImportDirectory = chosenFile.getParent();
            try {
                Savepoint save1 = db.connection.setSavepoint();
                db.clearAllData();
                if (db.databaseImportFromCSV(chosenFile.getAbsolutePath())){
                    JOptionPane.showMessageDialog(frame, "All data are imported successfully from " + chosenFile.getPath(),
                            "Database Import", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame, "Error encountered during import, please try again or report the problem.",
                            "Database Import", JOptionPane.INFORMATION_MESSAGE);
                    db.connection.rollback(save1);
                    return;
                }
                changesHistory.clear();
                undoHistory.clear();
                changeMade = false;
                currentImgID = -1;
                currentImgName = null;
                currentImgPath = null;
                currentImgLb.setText("Current Image: None");
                
                currentImg = null;
                frame.setTitle(APPLICATION_NAME);
                pointLabelTable.clear();
                drawPanel.repaint();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void exportDatabase(){
        if (changeMade) {
            int dialogResult = JOptionPane.showConfirmDialog(frame,
                    "Do you want to save the changes before exporting the database?", "Reminder",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
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
        File dir = new File(recentExportDirectory);
        if (!dir.exists()){
            dir = new File(currentWorkingDirectory);
        }
        chooser.setCurrentDirectory(dir);
        int returnVal = chooser.showSaveDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
            File chosenFile = chooser.getSelectedFile();
            recentExportDirectory = chosenFile.getParent();
            try {
                db.databaseExportAsCSV(chosenFile.getAbsolutePath());
                JOptionPane.showMessageDialog(frame, "All data are successfully exported to " + chosenFile.getPath(),
                        "Database Export", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error encountered during export, please try again or report the problem.\nError Message:" + e.getMessage(),
                        "Database Export", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    public void loadImage(){
        if (changeMade) {
            int dialogResult = JOptionPane.showConfirmDialog(frame,
                    "Do you want to save the changes before loading the new image?", "Reminder",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
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
        File dir = new File(currentImgDirectory);
        if (!dir.exists()){
            dir = new File(currentWorkingDirectory);
        }
        chooser.setCurrentDirectory(dir);
        int returnVal = chooser.showSaveDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            File chosenFile = chooser.getSelectedFile();

            // System.out.println(chosenFile.getAbsolutePath());
            try {

                boolean matched = false;
                boolean newFileEnabled = false;

                List<Long> filesByPathHash = db.getFileIDByConstraints(chosenFile, true, true, false);
                if (filesByPathHash.size() == 1) {
                    currentImgID = filesByPathHash.get(0);
                    db.updateFile(currentImgID, chosenFile);
                    matched = true;
                    System.out.println("Open File Path & Hash Match");
                } 
                if (!matched && !newFileEnabled){
                    List<Long> filesByNameHash = db.getFileIDByConstraints(chosenFile, false, true, true);
                    if (filesByNameHash.size() == 1) {
                        long imageID = filesByNameHash.get(0);
                        File matchFile = db.getFileByID(imageID);
                        
                        int dialogResult = JOptionPane.showConfirmDialog(frame,
                                "An image previously located at " + matchFile.getAbsolutePath() + " with the same name and hash is detected.\nDo you want to load the corresponding data?",
                                "Reminder", JOptionPane.YES_NO_CANCEL_OPTION);
                        if (dialogResult == JOptionPane.YES_OPTION) {
                            try {
                                currentImgID = imageID;
                                db.updateFile(currentImgID, chosenFile);
                                matched = true;
                                System.out.println("Open File Name Hash Match");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (dialogResult == JOptionPane.NO_OPTION) {
                            newFileEnabled = true;
                        } else if (dialogResult == JOptionPane.CANCEL_OPTION) {
                            return;
                        }
                        
                    }
                }
                
                if (!matched && !newFileEnabled){
                    List<Long> filesByHash = db.getFileIDByConstraints(chosenFile, false, true, false);
                    if (filesByHash.size() == 1) {
                        long imageID = filesByHash.get(0);
                        File matchFile = db.getFileByID(imageID);
                        
                        int dialogResult = JOptionPane.showConfirmDialog(frame,
                                "An image previously located at " + matchFile.getAbsolutePath() + " with the same hash but different name is detected.\nDo you want to load the corresponding data?",
                                "Reminder", JOptionPane.YES_NO_CANCEL_OPTION);
                        if (dialogResult == JOptionPane.YES_OPTION) {
                            try {
                                currentImgID = imageID;
                                db.updateFile(currentImgID, chosenFile);
                                matched = true;
                                System.out.println("Open File Hash Match");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (dialogResult == JOptionPane.NO_OPTION) {
                            newFileEnabled = true;
                        } else if (dialogResult == JOptionPane.CANCEL_OPTION) {
                            return;
                        }
                        
                    }
                }
                
                if (newFileEnabled || !matched){
                    currentImgID = db.insertFile(chosenFile);
                    System.out.println("New File Inserted");
                }
                //System.out.println("ID:" + currentImgID);
                currentImg = ImageIO.read(chosenFile);
                drawPanel.setPreferredSize(new Dimension(currentImg.getWidth(), currentImg.getHeight()));
                drawPanel.repaint();
                pointLabelTable = db.getPointLabelsByFileID(currentImgID);
                currentImgDirectory = chosenFile.getParent();
                currentImgPath = chosenFile.getPath();
                currentImgName = chosenFile.getName();
                currentImgLb.setText("Current Image: " + currentImgPath);
                changeMade = false;
                changesHistory.clear();
                undoHistory.clear();
                drawPanel.parentScrollPane.revalidate();
                frame.setTitle(APPLICATION_NAME + " - " + currentImgName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void saveCurrentLabels(){
        try {
            db.updatePointLabels(currentImgID, pointLabelTable);
            changeMade = false;
            frame.setTitle(APPLICATION_NAME + " - " + currentImgName);
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }
    
    public void showAbout(){
        JOptionPane.showMessageDialog(frame, "Clicklable\nVersion: " + VERSION + "\nCopyright 2017 Chi Ian Tang. All rights reserved.",
                "About", JOptionPane.PLAIN_MESSAGE);
    }

    class FunctionSelectHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            currentEditMode = Integer.valueOf(e.getActionCommand());
            if (currentEditMode == 1){
                snapEnabled = false;
                cbSnapEnabled.setSelected(false);
            } else if (currentEditMode == 2 || currentEditMode == 4) {
                snapEnabled = true;
                cbSnapEnabled.setSelected(true);
            }
            drawPanel.repaint();
        }

    }
    
    class LabelControlPanel extends JPanel {
        
        LabelSelectHandler rdbtnLabelHandler;
        
        class LabelSelectHandler implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                currentLabel = Long.valueOf(e.getActionCommand());
            }

        }
        
        public LabelControlPanel(HashMap<Long, LabelDescriptor> labelDescriptorTable){
            this.setLayout(new MigLayout("", "[][][]", "[][]"));
            rdbtnLabelHandler = new LabelSelectHandler();
            createNewLabels(labelDescriptorTable);
        }
        
        public void createNewLabels(HashMap<Long, LabelDescriptor> labelDescriptorTable){
            this.removeAll();

            ButtonGroup rdbtngLabel = new ButtonGroup();
            for (Map.Entry<Long, LabelDescriptor> entry : labelDescriptorTable.entrySet()) {
                JRadioButton rdbtnlabel = new JRadioButton();
                if (entry.getKey() == 1) {
                    rdbtnlabel.setSelected(true);
                    currentLabel = 1;  
                }
                rdbtnlabel.setText(entry.getValue().name);
                rdbtnlabel.addActionListener(rdbtnLabelHandler);
                rdbtnlabel.setActionCommand(String.valueOf(entry.getKey()));
                rdbtngLabel.add(rdbtnlabel);
                
                JLabel sampleBox = new JLabel("    ");
                sampleBox.setOpaque(true);
                sampleBox.setBackground(entry.getValue().draw_fill_color);
                sampleBox.setBorder(BorderFactory.createLineBorder(entry.getValue().draw_border_color));
                this.add(rdbtnlabel);
                this.add(sampleBox, "wrap");
            }
            this.revalidate();
            this.repaint();
        }
    }



    class DrawPanel extends JPanel implements MouseInputListener, MouseWheelListener {

        /**
         * 
         */
        private static final long serialVersionUID = -377915463140971553L;
        
        private JScrollPane parentScrollPane = null;
        


        private Point snapPoint = null;
        private Color colorHighLight = new Color(255, 255, 255, 128);
        private Color colorCenter = new Color (255, 255, 255, 196);
        private int centerSize = 2;
        
        private boolean isPanDragging = false;
        
        private boolean isPointDragging = false;
        private Point dragPoint = null;
        private long dragPointLabel = -1;
        private Point dragCursorStartPoint = null;
        private Point currentCursorPointReal = null;
        
        
        JPopupMenu popupNewPointMenu;
        JPopupMenu popupChangePointMenu;
        
        JMenu addSelectionMenu;
        JMenu changeSelectionMenu;
        
        JMenuItem lockedPointPosItem;
        JMenuItem lockedPointInfoItem;
        JMenuItem newPointInfoItem;
        
        Point menuLockedPoint;
        
        boolean isMenuShown = false;
        
        

        public Dimension getPreferredSize() {
            return currentImg == null ? new Dimension(500, 500)
                    : new Dimension((int) (currentImg.getWidth() * currentZoom),
                            (int) (currentImg.getHeight() * currentZoom));
        }

        public DrawPanel() {
            super();
            addMouseListener(this);
            addMouseMotionListener(this);
            addMouseWheelListener(this);
            setPreferredSize(new Dimension(500, 500));
            
            popupNewPointMenu = new JPopupMenu();
            popupChangePointMenu = new JPopupMenu();
            
            addSelectionMenu = new JMenu("Add label");
            addSelectionMenu.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClicklableMain.class.getResource("/images/add_icon_A_18.png"))));
            
            changeSelectionMenu = new JMenu("Change label to");
            changeSelectionMenu.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClicklableMain.class.getResource("/images/modify_icon_A_18.png"))));
            
            
            lockedPointPosItem = new JMenuItem("(,)");
            lockedPointPosItem.setEnabled(false);
            
            lockedPointInfoItem = new JMenuItem("No label point here");
            lockedPointInfoItem.setEnabled(false);
            
            newPointInfoItem = new JMenuItem("No label point here");
            newPointInfoItem.setEnabled(false);
            
            createNewLabelMenu(labelDescriptorTable);
            
            JMenuItem removeLabelItem = new JMenuItem("Remove label");
            removeLabelItem.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClicklableMain.class.getResource("/images/delete_icon_A_18.png"))));
            
            removeLabelItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ChangeDescriptor thisChange = new ChangeDescriptor(ChangeDescriptor.ACTION_REMOVE, menuLockedPoint, pointLabelTable.get(menuLockedPoint), null, null);
                    updateLabelPoint(thisChange);
                }
            });
            
            JMenuItem zoomInItem = new JMenuItem("Zoom in");
            zoomInItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int newZoomValue = sliderZoom.getValue() + 1;
                    double newZoom = getZoomFactor(newZoomValue);
                    drawPanel.zoomAtPoint(newZoom, menuLockedPoint);
                    currentZoomSliderPos = newZoomValue;
                    sliderZoom.setValue(newZoomValue);
                }
            });
            
            JMenuItem zoomOutItem = new JMenuItem("Zoom out");
            zoomOutItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int newZoomValue = sliderZoom.getValue() - 1;
                    double newZoom = getZoomFactor(newZoomValue);
                    drawPanel.zoomAtPoint(newZoom, menuLockedPoint);
                    currentZoomSliderPos = newZoomValue;
                    sliderZoom.setValue(newZoomValue);
                }
            });
            
            popupNewPointMenu.add(newPointInfoItem);
            popupNewPointMenu.addSeparator();
            popupNewPointMenu.add(addSelectionMenu);
            popupNewPointMenu.addSeparator();
            popupNewPointMenu.add(zoomInItem);
            popupNewPointMenu.add(zoomOutItem);
            
            zoomInItem = new JMenuItem("Zoom in");
            zoomInItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int newZoomValue = sliderZoom.getValue() + 1;
                    double newZoom = getZoomFactor(newZoomValue);
                    drawPanel.zoomAtPoint(newZoom, menuLockedPoint);
                    currentZoomSliderPos = newZoomValue;
                    sliderZoom.setValue(newZoomValue);
                }
            });
            
            zoomOutItem = new JMenuItem("Zoom out");
            zoomOutItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int newZoomValue = sliderZoom.getValue() - 1;
                    double newZoom = getZoomFactor(newZoomValue);
                    drawPanel.zoomAtPoint(newZoom, menuLockedPoint);
                    currentZoomSliderPos = newZoomValue;
                    sliderZoom.setValue(newZoomValue);
                }
            });
            
            popupChangePointMenu.add(lockedPointPosItem);
            popupChangePointMenu.add(lockedPointInfoItem);
            popupChangePointMenu.addSeparator();
            popupChangePointMenu.add(changeSelectionMenu);
            popupChangePointMenu.add(removeLabelItem);
            popupChangePointMenu.addSeparator();
            popupChangePointMenu.add(zoomInItem);
            popupChangePointMenu.add(zoomOutItem);
            
        }
        
        public void createNewLabelMenu(HashMap<Long, LabelDescriptor> labelDescriptorTable){
            addSelectionMenu.removeAll();
            changeSelectionMenu.removeAll();
            for (Map.Entry<Long, LabelDescriptor> entry : labelDescriptorTable.entrySet()) {
                
                JMenuItem addLabelItem = new JMenuItem(entry.getValue().getName());
                addLabelItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        ChangeDescriptor thisChange = new ChangeDescriptor(ChangeDescriptor.ACTION_ADD, null, null, menuLockedPoint, entry.getValue().getId());
                        updateLabelPoint(thisChange);
                    }
                });
                addSelectionMenu.add(addLabelItem);
                
                JMenuItem changeLabelItem = new JMenuItem(entry.getValue().getName());
                changeLabelItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        ChangeDescriptor thisChange = new ChangeDescriptor(ChangeDescriptor.ACTION_MODIFY, menuLockedPoint, pointLabelTable.get(menuLockedPoint), menuLockedPoint, entry.getValue().getId());
                        updateLabelPoint(thisChange);
                    }
                });
                changeSelectionMenu.add(changeLabelItem);
                
            }
        }
        
        public JScrollPane getParentScrollPane() {
            return parentScrollPane;
        }

        public void setParentScrollPane(JScrollPane parentScrollPane) {
            this.parentScrollPane = parentScrollPane;
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.gray);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            if (currentImg != null) {
                AffineTransform at = AffineTransform.getScaleInstance(currentZoom, currentZoom);
                AffineTransformOp atop = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
                g2d.drawImage(currentImg, atop, 0, 0);

                for (Map.Entry<Point, Long> entry : pointLabelTable.entrySet()) {
                    Point key = entry.getKey();
                    Long value = entry.getValue();
                    
                    Shape shape = null;
                    if (drawShapeScaleToZoom){
                        shape = ShapeManager.getShapeByString(labelDescriptorTable.get(value).getShapeString(), 
                                key.getX() * currentZoom, 
                                key.getY() * currentZoom,
                                labelDescriptorTable.get(value).getShapeSize() * currentZoom, 
                                labelDescriptorTable.get(value).getShapeThickness() * currentZoom);
                    } else {
                        shape = ShapeManager.getShapeByString(labelDescriptorTable.get(value).getShapeString(), 
                                key.getX() * currentZoom, 
                                key.getY() * currentZoom,
                                labelDescriptorTable.get(value).getShapeSize(), 
                                labelDescriptorTable.get(value).getShapeThickness());
                    }

                    g2d.setColor(labelDescriptorTable.get(value).draw_fill_color);
                    g2d.fill(shape);
                    g2d.setColor(labelDescriptorTable.get(value).draw_border_color);
                    g2d.draw(shape);
                }
                
                if (isPointDragging){
                    Point key = dragPoint;
                    Long value = dragPointLabel;
                    Shape shape = null;
                    if (drawShapeScaleToZoom){
                        shape = ShapeManager.getShapeByString(labelDescriptorTable.get(value).getShapeString(), 
                                key.getX() * currentZoom + currentCursorPointReal.getX() - dragCursorStartPoint.getX(), 
                                key.getY() * currentZoom + currentCursorPointReal.getY() - dragCursorStartPoint.getY(),
                                labelDescriptorTable.get(value).getShapeSize() * currentZoom, 
                                labelDescriptorTable.get(value).getShapeThickness() * currentZoom);
                    } else {
                        shape = ShapeManager.getShapeByString(labelDescriptorTable.get(value).getShapeString(), 
                                key.getX() * currentZoom + currentCursorPointReal.getX() - dragCursorStartPoint.getX(), 
                                key.getY() * currentZoom + currentCursorPointReal.getY() - dragCursorStartPoint.getY(),
                                labelDescriptorTable.get(value).getShapeSize(), 
                                labelDescriptorTable.get(value).getShapeThickness());
                    }

                    g2d.setColor(labelDescriptorTable.get(value).draw_fill_color);
                    g2d.fill(shape);
                    g2d.setColor(labelDescriptorTable.get(value).draw_border_color);
                    g2d.draw(shape);
                    
                    
                    g2d.setColor(colorHighLight);
                    ShapeManager.fillOval(g2d, 
                            key.getX() * currentZoom + currentCursorPointReal.getX() - dragCursorStartPoint.getX(), 
                            key.getY() * currentZoom + currentCursorPointReal.getY() - dragCursorStartPoint.getY(),
                            snapBound * currentZoom, 
                            snapBound * currentZoom);
                    g2d.setColor(colorCenter);
                    ShapeManager.fillOval(g2d, 
                            key.getX() * currentZoom + currentCursorPointReal.getX() - dragCursorStartPoint.getX(), 
                            key.getY() * currentZoom + currentCursorPointReal.getY() - dragCursorStartPoint.getY(),
                            centerSize, 
                            centerSize);
                    drawPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    
                } else if (snapEnabled && snapPoint != null) {
                    // System.out.println(snapPoint);
                    g2d.setColor(colorHighLight);
                    ShapeManager.fillOval(g2d, snapPoint.getX() * currentZoom, snapPoint.getY() * currentZoom, snapBound * currentZoom, snapBound * currentZoom);
                    g2d.setColor(colorCenter);
                    ShapeManager.fillOval(g2d, snapPoint.getX() * currentZoom, snapPoint.getY() * currentZoom, centerSize, centerSize);
                    drawPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else if (currentEditMode == 1){
                    drawPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                } else {
                    drawPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
                
                
            }

        }

        public void zoomAtCenter(double newZoom){
            
            double prevZoom = currentZoom;
            currentZoom = newZoom;
            
            Point prevPos = parentScrollPane.getViewport().getViewPosition();
            drawPanel.repaint();

            parentScrollPane.revalidate();
            
            Point newPos = new Point((int) (prevPos.getX() * (currentZoom / prevZoom)
                    + parentScrollPane.getViewport().getExtentSize().getWidth() / 2 * (currentZoom / prevZoom - 1)),
                    (int) (prevPos.getY() * (currentZoom / prevZoom)
                            + parentScrollPane.getViewport().getExtentSize().getHeight() / 2
                                    * (currentZoom / prevZoom - 1)));
            parentScrollPane.getViewport().setViewPosition(newPos);
            
            
            lblZoom.setText(String.format("Zoom: %.2f%%", currentZoom * 100));
        }
        
        public void zoomAtPoint(double newZoom, Point zoomCenter){
            
            double prevZoom = currentZoom;
            currentZoom = newZoom;
            
            Point prevPos = parentScrollPane.getViewport().getViewPosition();
            drawPanel.repaint();

            parentScrollPane.revalidate();
            Point newPos = new Point(
                    (int) (prevPos.getX() * (currentZoom / prevZoom)
                    + (zoomCenter.getX() - prevPos.getX()) * (currentZoom / prevZoom - 1)),
                    (int) (prevPos.getY() * (currentZoom / prevZoom)
                    + (zoomCenter.getY() - prevPos.getY()) * (currentZoom / prevZoom - 1)));
            parentScrollPane.getViewport().setViewPosition(newPos);
            
            
            lblZoom.setText(String.format("Zoom: %.2f%%", currentZoom * 100));
        }

        public Point getClosestPoint(Point q) {
            double minDist = snapBound;
            Point closestP = null;
            for (Point p : pointLabelTable.keySet()) {
                double d = p.distance(q);
                if (d <= minDist) {
                    closestP = p;
                    minDist = d;
                }
            }
            return closestP;
        }
        
        public void updateCursorPosInfo(Point e){
            currentCursorPointReal = e;
            currentCursorPos = new Point((int) (e.getX() / currentZoom), (int) (e.getY() / currentZoom));
            currentCursorPosLb.setText("Current Position: (" + (int) (e.getX() / currentZoom) + ", "
                    + (int) (e.getY() / currentZoom) + ")");
        }
        
        public void updateLabelPoint(ChangeDescriptor thisChange){
            if (thisChange.getAction() == ChangeDescriptor.ACTION_ADD){
                pointLabelTable.put(thisChange.getPointAfter(), thisChange.getLabelAfter());
            } else if (thisChange.getAction() == ChangeDescriptor.ACTION_MODIFY){
                if (pointLabelTable.containsKey(thisChange.getPointBefore())){
                    pointLabelTable.remove(thisChange.getPointBefore());
                }      
                pointLabelTable.put(thisChange.getPointAfter(), thisChange.getLabelAfter());
            } else if (thisChange.getAction() == ChangeDescriptor.ACTION_REMOVE){
                if (pointLabelTable.containsKey(thisChange.getPointBefore())){
                    pointLabelTable.remove(thisChange.getPointBefore());
                }    
            }
            
            if (changesHistory.size() >= historyCountMax){
                changesHistory.removeFirst();
            }
            changesHistory.addLast(thisChange);
            undoHistory.clear();
            changeMade = true;
            frame.setTitle(APPLICATION_NAME + " - " + currentImgName + "*");
            repaint();
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            
            if (currentImg == null){
                return;
            }


            Point editP;
            if (snapEnabled && snapPoint != null) {
                editP = snapPoint;
            } else {
                editP = new Point((int) (e.getPoint().getX() / currentZoom), (int) (e.getPoint().getY() / currentZoom));
            }
            
            if (SwingUtilities.isRightMouseButton(e)){
                menuLockedPoint = editP;
                isMenuShown = true;
                
                
                if (pointLabelTable.containsKey(editP)) {
                    lockedPointPosItem.setText("At (" + (int) editP.getX() + ", " + (int) editP.getY() + ")");
                    lockedPointInfoItem.setText(labelDescriptorTable.get(pointLabelTable.get(editP)).getName());
                    popupChangePointMenu.show(this, e.getX(), e.getY());
                } else {
                    newPointInfoItem.setText("No label point at (" + (int) editP.getX() + ", " + (int) editP.getY() + ")");
                    popupNewPointMenu.show(this, e.getX(), e.getY());
                }
                return;
            }
            
            if (isMenuShown){
                isMenuShown = false;
                return;
            }
            

            ChangeDescriptor thisChange = null;
            if (currentEditMode == 1 || currentEditMode == 2) {
                if (pointLabelTable.containsKey(editP)){
                    thisChange = new ChangeDescriptor(ChangeDescriptor.ACTION_MODIFY, editP, pointLabelTable.get(editP), editP, currentLabel);
                } else {
                    thisChange = new ChangeDescriptor(ChangeDescriptor.ACTION_ADD, null, null, editP, currentLabel);
                }
                
            } else if (currentEditMode == 4) {
                thisChange = new ChangeDescriptor(ChangeDescriptor.ACTION_REMOVE, editP, pointLabelTable.get(editP), null, null);
                snapPoint = null;
            }
            updateLabelPoint(thisChange);
            
            //System.out.println(e.getPoint().toString() + " " + pointLabelTable.size());
            repaint();
        }

        @Override
        public void mouseEntered(MouseEvent arg0) {

        }

        @Override
        public void mouseExited(MouseEvent arg0) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            
            
            if (currentEditMode == 2 && snapEnabled && snapPoint != null && pointLabelTable.containsKey(snapPoint)){
                isPointDragging = true;
                dragPoint = snapPoint;
                dragPointLabel = pointLabelTable.get(dragPoint);
                pointLabelTable.remove(dragPoint);
                dragCursorStartPoint = e.getPoint();
            } else {
                isPanDragging = true;
                dragCursorStartPoint = e.getPoint();
            }
            
            
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            updateCursorPosInfo(e.getPoint());
            if (isPointDragging){
                isPointDragging = false;
                
                
                Point editP = new Point((int) ((dragPoint.getX() * currentZoom + currentCursorPointReal.getX() - dragCursorStartPoint.getX()) / currentZoom), 
                        (int) ((dragPoint.getY() * currentZoom + currentCursorPointReal.getY() - dragCursorStartPoint.getY()) / currentZoom));
                
                ChangeDescriptor thisChange = new ChangeDescriptor(ChangeDescriptor.ACTION_MODIFY, dragPoint, dragPointLabel, editP, dragPointLabel);

                updateLabelPoint(thisChange);
                dragPoint = null;
                dragPointLabel = -1;
                dragCursorStartPoint = null;
                snapPoint = editP;

                repaint();
            } else if (isPanDragging){
                isPanDragging = false;
                dragCursorStartPoint = null;
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            updateCursorPosInfo(e.getPoint());
            if (isPointDragging){
                repaint();
            } else if (isPanDragging){
                
                if (dragCursorStartPoint != null && parentScrollPane != null) {
                    JViewport viewPort = parentScrollPane.getViewport();
                    if (viewPort != null) {
                        
                        int deltaX = dragCursorStartPoint.x - e.getX();
                        int deltaY = dragCursorStartPoint.y - e.getY();
                        //System.out.println(deltaX + " " + deltaY);
                        Rectangle view = viewPort.getViewRect();
                        view.translate(deltaX, deltaY);
                        this.scrollRectToVisible(view);
                    }
                }
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            updateCursorPosInfo(e.getPoint());
            if (snapEnabled && currentImg != null) {
                Point newSnapPoint = getClosestPoint(currentCursorPos);

                if (newSnapPoint != null && snapPoint == null || newSnapPoint == null && snapPoint != null) {
                    snapPoint = newSnapPoint;
                    repaint();
                } else if (newSnapPoint != null && snapPoint != null) {
                    if (!snapPoint.equals(newSnapPoint)) {
                        snapPoint = newSnapPoint;
                        repaint();
                    }
                }

            }
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {

            int newZoomValue = sliderZoom.getValue() - e.getWheelRotation();
            double newZoom = getZoomFactor(newZoomValue);
            drawPanel.zoomAtPoint(newZoom, e.getPoint());
            
            currentZoomSliderPos = newZoomValue;
            sliderZoom.setValue(newZoomValue);
        }
        


        
    }
    
    

    class ControlMenuBar extends JMenuBar {


        /**
         * 
         */
        private static final long serialVersionUID = -1532048887446689256L;

        public ControlMenuBar() {
            
            JMenu fileMenu = new JMenu("File");
            
            JMenuItem itemLoadImg = new JMenuItem("Load Image...");
            itemLoadImg.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClicklableMain.class.getResource("/images/open_icon_A_18.png"))));
            itemLoadImg.setAccelerator(KeyStroke.getKeyStroke(
                    java.awt.event.KeyEvent.VK_O, 
                    java.awt.Event.CTRL_MASK));
            itemLoadImg.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    loadImage();
                }
            });
            
            JMenuItem itemSave = new JMenuItem("Save changes");
            itemSave.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClicklableMain.class.getResource("/images/save_icon_A_18.png"))));
            itemSave.setAccelerator(KeyStroke.getKeyStroke(
                    java.awt.event.KeyEvent.VK_S, 
                    java.awt.Event.CTRL_MASK));
            itemSave.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    saveCurrentLabels();
                }
            });
            JMenuItem itemExport = new JMenuItem("Export Database...");
            itemExport.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClicklableMain.class.getResource("/images/export_icon_A_18.png"))));
            itemExport.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    exportDatabase();
                }
            });
            JMenuItem itemImport = new JMenuItem("Import Database...");
            itemImport.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClicklableMain.class.getResource("/images/import_icon_A_18.png"))));
            itemImport.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    importDatabase();
                }
            });
            JMenuItem itemExit = new JMenuItem("Exit");
            itemExit.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    terminateApplication();
                }
            });
            fileMenu.add(itemLoadImg);
            fileMenu.add(itemSave);
            fileMenu.addSeparator();
            fileMenu.add(itemExport);
            fileMenu.add(itemImport);
            fileMenu.addSeparator();
            fileMenu.add(itemExit);
            
            JMenu editMenu = new JMenu("Edit");
            JMenuItem itemUndo = new JMenuItem("Undo Action");
            itemUndo.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClicklableMain.class.getResource("/images/undo_icon_A_18.png"))));
            itemUndo.setAccelerator(KeyStroke.getKeyStroke(
                    java.awt.event.KeyEvent.VK_Z, 
                    java.awt.Event.CTRL_MASK));
            itemUndo.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    revertChange();
                }
            });
            JMenuItem itemRedo = new JMenuItem("Redo Action");
            itemRedo.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClicklableMain.class.getResource("/images/redo_icon_A_18.png"))));
            itemRedo.setAccelerator(KeyStroke.getKeyStroke(
                    java.awt.event.KeyEvent.VK_Y, 
                    java.awt.Event.CTRL_MASK));
            itemRedo.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    redoChange();
                }
            });
            editMenu.add(itemUndo);
            editMenu.add(itemRedo);
            
            JMenu prefMenu = new JMenu("Preferences");
            JMenuItem itemSettings = new JMenuItem("Settings");
            itemSettings.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClicklableMain.class.getResource("/images/settings_icon_A_18.png"))));
            itemSettings.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    SettingFrame settingFrame = new SettingFrame(window);
                    settingFrame.setVisible(true);
                }
                
            });
            prefMenu.add(itemSettings);
            
            JMenu helpMenu = new JMenu("About");
            JMenuItem itemAbout = new JMenuItem("About " + APPLICATION_NAME);
            itemAbout.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClicklableMain.class.getResource("/images/info_icon_A_18.png"))));
            itemAbout.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    showAbout();
                }
            });
            helpMenu.add(itemAbout);
            
            add(fileMenu);
            add(Box.createRigidArea(new Dimension(5, 5)));
            add(editMenu);
            add(Box.createRigidArea(new Dimension(5, 5)));
            add(prefMenu);
            add(Box.createRigidArea(new Dimension(5, 5)));
            add(helpMenu);
        }
    }

    class ConfigManager {
        private static final String CONFIG = "config";

        private static final String CONFIG_DB_PATH = "db_path";
        private static final String CONFIG_RECENT_LOAD_IMAGE_DIRECTORY = "recent_load_image_dir";
        private static final String CONFIG_RECENT_DB_IMPORT_DIRECTORY = "recent_db_import_dir";
        private static final String CONFIG_RECENT_DB_EXPORT_DIRECTORY = "recent_db_export_dir";

        //private static final String CONFIG_SNAP_ENABLED = "snap_enabled";
        private static final String CONFIG_SNAP_BOUND = "snap_bound";
        private static final String CONFIG_ZOOM_SENSITIVITY = "zoom_sensitivity";
        
        private static final String CONFIG_LABELS = "labels";
        private static final String CONFIG_LABEL = "label";
        private static final String CONFIG_LABEL_ID = "id";
        private static final String CONFIG_LABEL_NAME = "name";
//        private static final String CONFIG_LABEL_VALUE = "value";
        private static final String CONFIG_LABEL_FILL_COLOR = "fill_color";
        private static final String CONFIG_LABEL_BORDER_COLOR = "border_color";
        private static final String CONFIG_LABEL_SHAPE_NAME = "shape_name";
        private static final String CONFIG_LABEL_SHAPE_SIZE = "shape_size";
        private static final String CONFIG_LABEL_SHAPE_THICKNESS = "shape_thickness";

        public void createXMLConfig(File file) {
            try {
                FileWriter fileWriter = new FileWriter(file);

                XMLOutputFactory xMLOutputFactory = XMLOutputFactory.newInstance();
                XMLStreamWriter xMLStreamWriter = xMLOutputFactory.createXMLStreamWriter(fileWriter);
                
                xMLStreamWriter.writeStartDocument();
                xMLStreamWriter.writeStartElement(CONFIG);

                xMLStreamWriter.writeStartElement(CONFIG_DB_PATH);
                xMLStreamWriter.writeCharacters(databasePath);
                xMLStreamWriter.writeEndElement();

                xMLStreamWriter.writeStartElement(CONFIG_RECENT_LOAD_IMAGE_DIRECTORY);
                xMLStreamWriter.writeCharacters(currentImgDirectory);
                xMLStreamWriter.writeEndElement();

                xMLStreamWriter.writeStartElement(CONFIG_RECENT_DB_IMPORT_DIRECTORY);
                xMLStreamWriter.writeCharacters(recentImportDirectory);
                xMLStreamWriter.writeEndElement();

                xMLStreamWriter.writeStartElement(CONFIG_RECENT_DB_EXPORT_DIRECTORY);
                xMLStreamWriter.writeCharacters(recentExportDirectory);
                xMLStreamWriter.writeEndElement();

//                xMLStreamWriter.writeStartElement(CONFIG_SNAP_ENABLED);
//                xMLStreamWriter.writeCharacters(String.valueOf(snapEnabled));
//                xMLStreamWriter.writeEndElement();

                xMLStreamWriter.writeStartElement(CONFIG_SNAP_BOUND);
                xMLStreamWriter.writeCharacters(String.valueOf(snapBound));
                xMLStreamWriter.writeEndElement();
                
                xMLStreamWriter.writeStartElement(CONFIG_ZOOM_SENSITIVITY);
                xMLStreamWriter.writeCharacters(String.valueOf(zoomSensitivity));
                xMLStreamWriter.writeEndElement();
                
                xMLStreamWriter.writeStartElement(CONFIG_LABELS);

                for (Map.Entry<Long, LabelDescriptor> entry : labelDescriptorTable.entrySet()) {
                    Long id = entry.getKey();
                    LabelDescriptor labelDescriptor = entry.getValue();

                    xMLStreamWriter.writeStartElement(CONFIG_LABEL);

                    xMLStreamWriter.writeStartElement(CONFIG_LABEL_ID);
                    xMLStreamWriter.writeCharacters(String.valueOf(labelDescriptor.id));
                    xMLStreamWriter.writeEndElement();

                    xMLStreamWriter.writeStartElement(CONFIG_LABEL_NAME);
                    xMLStreamWriter.writeCharacters(labelDescriptor.name);
                    xMLStreamWriter.writeEndElement();

//                    xMLStreamWriter.writeStartElement(CONFIG_LABEL_VALUE);
//                    xMLStreamWriter.writeCharacters(String.valueOf(labelDescriptor.value));
//                    xMLStreamWriter.writeEndElement();

                    xMLStreamWriter.writeStartElement(CONFIG_LABEL_FILL_COLOR);
                    xMLStreamWriter.writeCharacters(String.valueOf(labelDescriptor.draw_fill_color.getRGB()));
                    xMLStreamWriter.writeEndElement();

                    xMLStreamWriter.writeStartElement(CONFIG_LABEL_BORDER_COLOR);
                    xMLStreamWriter.writeCharacters(String.valueOf(labelDescriptor.draw_border_color.getRGB()));
                    xMLStreamWriter.writeEndElement();
                    
                    xMLStreamWriter.writeStartElement(CONFIG_LABEL_SHAPE_NAME);
                    xMLStreamWriter.writeCharacters(labelDescriptor.shapeString);
                    xMLStreamWriter.writeEndElement();
                    
                    xMLStreamWriter.writeStartElement(CONFIG_LABEL_SHAPE_SIZE);
                    xMLStreamWriter.writeCharacters(String.valueOf(labelDescriptor.shapeSize));
                    xMLStreamWriter.writeEndElement();
                    
                    xMLStreamWriter.writeStartElement(CONFIG_LABEL_SHAPE_THICKNESS);
                    xMLStreamWriter.writeCharacters(String.valueOf(labelDescriptor.shapeThickness));
                    xMLStreamWriter.writeEndElement();

                    xMLStreamWriter.writeEndElement();
                }

                xMLStreamWriter.writeEndElement();
                xMLStreamWriter.writeEndDocument();

                xMLStreamWriter.flush();
                xMLStreamWriter.close();

                // String xmlString = stringWriter.getBuffer().toString();
                fileWriter.flush();
                fileWriter.close();

            } catch (XMLStreamException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void parseXMLConfig(File file) {

            boolean bConfig = false;
            boolean bLabel = false;

            try {
                XMLInputFactory factory = XMLInputFactory.newInstance();
                XMLEventReader eventReader = factory.createXMLEventReader(new FileReader(file));

                while (eventReader.hasNext()) {
                    XMLEvent event = eventReader.nextEvent();

                    switch (event.getEventType()) {

                        case XMLStreamConstants.START_ELEMENT:
                            StartElement startElement = event.asStartElement();
                            String qName = startElement.getName().getLocalPart();

                            if (qName.equalsIgnoreCase(CONFIG)) {
                                bConfig = true;
                            } else if (bConfig) {
                                if (qName.equalsIgnoreCase(CONFIG_DB_PATH)) {
                                    event = eventReader.nextEvent();
                                    databasePath = event.asCharacters().getData();
                                } else if (qName.equalsIgnoreCase(CONFIG_RECENT_LOAD_IMAGE_DIRECTORY)) {
                                    event = eventReader.nextEvent();
                                    currentImgDirectory = event.asCharacters().getData();
                                } else if (qName.equalsIgnoreCase(CONFIG_RECENT_DB_IMPORT_DIRECTORY)) {
                                    event = eventReader.nextEvent();
                                    recentImportDirectory = event.asCharacters().getData();
                                } else if (qName.equalsIgnoreCase(CONFIG_RECENT_DB_EXPORT_DIRECTORY)) {
                                    event = eventReader.nextEvent();
                                    recentExportDirectory = event.asCharacters().getData();
//                                } else if (qName.equalsIgnoreCase(CONFIG_SNAP_ENABLED)) {
//                                    event = eventReader.nextEvent();
//                                    snapEnabled = Boolean.valueOf(event.asCharacters().getData());
                                } else if (qName.equalsIgnoreCase(CONFIG_SNAP_BOUND)) {
                                    event = eventReader.nextEvent();
                                    snapBound = Integer.valueOf(event.asCharacters().getData());
                                } else if (qName.equalsIgnoreCase(CONFIG_ZOOM_SENSITIVITY)) {
                                    event = eventReader.nextEvent();
                                    zoomSensitivity = Integer.valueOf(event.asCharacters().getData());
                                } else if (qName.equalsIgnoreCase(CONFIG_LABELS)) {
                                    bLabel = true;
                                    labelDescriptorTable.clear();
                                } else if (qName.equalsIgnoreCase(CONFIG_LABEL)){
                                    LabelDescriptor ld = parseLabelXML(eventReader);
                                    labelDescriptorTable.put(ld.id, ld);
                                }
                            }
                            break;

                        case XMLStreamConstants.CHARACTERS:
                            break;

                        case XMLStreamConstants.END_ELEMENT:
                            EndElement endElement = event.asEndElement();

                            if (endElement.getName().getLocalPart().equalsIgnoreCase(CONFIG_LABELS)) {
                                bLabel = false;
                            } else if (endElement.getName().getLocalPart().equalsIgnoreCase(CONFIG)) {
                                bConfig = false;
                            }
                            break;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
        
        public LabelDescriptor parseLabelXML(XMLEventReader eventReader) throws XMLStreamException{
            LabelDescriptor labelDescriptor = new LabelDescriptor();
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                switch (event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        StartElement startElement = event.asStartElement();
                        String qName = startElement.getName().getLocalPart();

                        if (qName.equalsIgnoreCase(CONFIG_LABEL_ID)) {
                            event = eventReader.nextEvent();
                            labelDescriptor.id = Long.valueOf(event.asCharacters().getData());
                        } else if (qName.equalsIgnoreCase(CONFIG_LABEL_NAME)) {
                            event = eventReader.nextEvent();
                            labelDescriptor.name = event.asCharacters().getData();
                        } else if (qName.equalsIgnoreCase(CONFIG_LABEL_FILL_COLOR)) {
                            event = eventReader.nextEvent();
                            labelDescriptor.draw_fill_color = new Color(Integer.valueOf(event.asCharacters().getData()), true);
                        } else if (qName.equalsIgnoreCase(CONFIG_LABEL_BORDER_COLOR)) {
                            event = eventReader.nextEvent();
                            labelDescriptor.draw_border_color = new Color(Integer.valueOf(event.asCharacters().getData()), true);
                        } else if (qName.equalsIgnoreCase(CONFIG_LABEL_SHAPE_NAME)) {
                            event = eventReader.nextEvent();
                            labelDescriptor.shapeString = event.asCharacters().getData();
                        } else if (qName.equalsIgnoreCase(CONFIG_LABEL_SHAPE_SIZE)) {
                            event = eventReader.nextEvent();
                            labelDescriptor.shapeSize = Integer.valueOf(event.asCharacters().getData());
                        } else if (qName.equalsIgnoreCase(CONFIG_LABEL_SHAPE_THICKNESS)) {
                            event = eventReader.nextEvent();
                            labelDescriptor.shapeThickness = Integer.valueOf(event.asCharacters().getData());
                        }
            
                        break;

                    case XMLStreamConstants.CHARACTERS:
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        EndElement endElement = event.asEndElement();

                        if (endElement.getName().getLocalPart().equalsIgnoreCase(CONFIG_LABEL)) {
                            return labelDescriptor;
                        } 
                        break;
                }
        
            }
            return labelDescriptor;
        }
    }
}
