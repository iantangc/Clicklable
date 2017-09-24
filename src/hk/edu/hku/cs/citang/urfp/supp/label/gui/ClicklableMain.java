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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputListener;
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

    private static final String VERSION = "1.1";
    private static final String APPLICATION_NAME = "Clicklable";

    private JFrame frame;
    private DrawPanel drawPanel;

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

    private JSlider sliderZoom;
    private int currentZoomSliderPos = 10;

    private int currentEditMode = 1;
    private boolean snapEnabled = false;
    private int snapBound = 30;

    private HashMap<Point, Long> pointLabelTable;
    private HashMap<Long, FunctionLabelDescriptor> labelDescriptorTable;

    private long currentLabel = 1;

    private int drawOvalSize = 16;
    private int drawOvalThickness = 8;

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
        
        


        labelDescriptorTable = new HashMap<Long, FunctionLabelDescriptor>();
        config = new ConfigManager();
        File configFile = new File(configPath);
        if (configFile.exists()){
            config.parseXMLConfig(new File(configPath));
        } else {
            FunctionLabelDescriptor ld = new FunctionLabelDescriptor(1, "Lactobacillus", 1, Color.YELLOW, Color.BLACK);
            labelDescriptorTable.put(ld.id, ld);
            
            ld = new FunctionLabelDescriptor(2, "Gardnerella", 2, Color.RED, Color.BLACK);
            labelDescriptorTable.put(ld.id, ld);
            
            ld = new FunctionLabelDescriptor(3, "Curved Rod", 3, Color.GREEN, Color.BLACK);
            labelDescriptorTable.put(ld.id, ld);
            
            ld = new FunctionLabelDescriptor(4, "Other", 4, Color.WHITE, Color.BLACK);
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

        JButton btnSave = new JButton("Save Changes");
        btnSave.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCurrentLabels();
            }
        });

        panel.add(btnSave);

        JButton btnLoadFile = new JButton("Load Image");
        panel.add(btnLoadFile);
        btnLoadFile.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                loadImage();
            }
        });

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

        ButtonGroup rdbtngLabel = new ButtonGroup();




        LabelSelectHandler rdbtnLabelHandler = new LabelSelectHandler();
        
        for (Map.Entry<Long, FunctionLabelDescriptor> entry : labelDescriptorTable.entrySet()) {
            JRadioButton rdbtnlabel = new JRadioButton();
            if (entry.getKey() == 1) {
                rdbtnlabel.setSelected(true);
            }
            rdbtnlabel.setText(entry.getValue().name);
            rdbtnlabel.addActionListener(rdbtnLabelHandler);
            rdbtnlabel.setActionCommand(String.valueOf(entry.getKey()));
            rdbtngLabel.add(rdbtnlabel);
            
            JLabel sampleBox = new JLabel("    ");
            sampleBox.setOpaque(true);
            sampleBox.setBackground(entry.getValue().draw_fill_color);
            sampleBox.setBorder(BorderFactory.createLineBorder(entry.getValue().draw_border_color));
            panelControl.add(rdbtnlabel);
            panelControl.add(sampleBox, "wrap");
        }

        // JRadioButton rdbtnNewRadioButton_3 = new JRadioButton("Class 1");
        // panelControl.add(rdbtnNewRadioButton_3, "wrap");
        //
        // JRadioButton rdbtnNewRadioButton_4 = new JRadioButton("Class 2");
        // panelControl.add(rdbtnNewRadioButton_4, "wrap");
        //
        // JRadioButton rdbtnNewRadioButton_5 = new JRadioButton("Class 3");
        // panelControl.add(rdbtnNewRadioButton_5, "wrap");
        //
        // JRadioButton rdbtnNewRadioButton_6 = new JRadioButton("Class 4");
        // panelControl.add(rdbtnNewRadioButton_6, "wrap");
        //
        JSeparator separator2 = new JSeparator();
        panelControl.add(separator2, "growx, span, wrap");

        // JLabel lblNewLabel_4 = new JLabel("New label");
        // panelControl.add(lblNewLabel_4, "growx, wrap");
        //
        // JSlider slider = new JSlider();
        // slider.setMinorTickSpacing(5);
        // slider.setMajorTickSpacing(10);
        // slider.setSnapToTicks(true);
        // slider.setPaintTicks(true);
        // slider.setPaintLabels(true);
        // panelControl.add(slider, "span, wrap");

        // JSeparator separator3 = new JSeparator();
        // panelControl.add(separator3, "growx, span, wrap");
        //
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
                    Point newPos = new Point((int) (prevPos.getX() * (currentZoom / prevZoom)
                            + scroll.getViewport().getExtentSize().getWidth() / 2 * (currentZoom / prevZoom - 1)),
                            (int) (prevPos.getY() * (currentZoom / prevZoom)
                                    + scroll.getViewport().getExtentSize().getHeight() / 2
                                            * (currentZoom / prevZoom - 1)));
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

    public void terminateApplication() {
        if (changeMade) {
            int dialogResult = JOptionPane.showConfirmDialog(frame, "Do you want to save the changes before quitting?",
                    "Reminder", JOptionPane.YES_NO_CANCEL_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
                try {
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
                db.clearAllData();
                db.databaseImportFromCSV(chosenFile.getAbsolutePath());
                changesHistory.clear();
                undoHistory.clear();
                changeMade = false;
                currentImgID = -1;
                currentImgName = null;
                currentImgPath = null;
                currentImgLb.setText("Current Image: None");
                ;
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
            } catch (Exception e) {
                e.printStackTrace();
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
        JOptionPane.showMessageDialog(frame, "Clicklable\nBy Chi Ian Tang\nVersion: " + VERSION,
                "About", JOptionPane.PLAIN_MESSAGE);
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
            currentLabel = Long.valueOf(e.getActionCommand());
        }

    }

    class DrawPanel extends JPanel implements MouseInputListener, MouseWheelListener {

        /**
         * 
         */
        private static final long serialVersionUID = -377915463140971553L;
        
        private Point snapPoint = null;
        private Color colorHighLight = new Color(255, 255, 255, 128);
        // private Color colorHighLight = Color.BLACK;

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
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            g2d.setColor(Color.gray);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            if (currentImg != null) {
                AffineTransform at = AffineTransform.getScaleInstance(currentZoom, currentZoom);
                AffineTransformOp atop = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
                g2d.drawImage(currentImg, atop, 0, 0);

                for (Map.Entry<Point, Long> entry : pointLabelTable.entrySet()) {
                    Point key = entry.getKey();
                    Long value = entry.getValue();

                    Shape ring = createRingShape(key.getX() * currentZoom, key.getY() * currentZoom,
                            drawOvalSize * currentZoom, drawOvalThickness * currentZoom);
                    g2d.setColor(labelDescriptorTable.get(value).draw_fill_color);
                    g2d.fill(ring);
                    g2d.setColor(labelDescriptorTable.get(value).draw_border_color);
                    g2d.draw(ring);
                }

                if (snapEnabled && snapPoint != null) {
                    // System.out.println(snapPoint);
                    g2d.setColor(colorHighLight);
                    g2d.fillOval((int) ((snapPoint.getX() - snapBound) * currentZoom),
                            (int) ((snapPoint.getY() - snapBound) * currentZoom), (int) (snapBound * 2 * currentZoom),
                            (int) (snapBound * 2 * currentZoom));
                    drawPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    drawPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }

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

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent arg0) {

        }

        @Override
        public void mouseExited(MouseEvent arg0) {

        }

        @Override
        public void mousePressed(MouseEvent e) {

            if (currentImg == null){
                return;
            }
            Point editP;
            if (snapEnabled && snapPoint != null) {
                editP = snapPoint;
            } else {
                editP = new Point((int) (e.getPoint().getX() / currentZoom), (int) (e.getPoint().getY() / currentZoom));
            }

            ChangeDescriptor thisChange = null;
            if (currentEditMode == 1 || currentEditMode == 2) {
                
                if (pointLabelTable.containsKey(editP)){
                    thisChange = new ChangeDescriptor(ChangeDescriptor.ACTION_MODIFY, editP, pointLabelTable.get(editP), editP, currentLabel);
                } else {
                    thisChange = new ChangeDescriptor(ChangeDescriptor.ACTION_ADD, null, null, editP, currentLabel);
                }
                pointLabelTable.put(editP, currentLabel);
                
            } else if (currentEditMode == 4) {
                
                thisChange = new ChangeDescriptor(ChangeDescriptor.ACTION_REMOVE, editP, pointLabelTable.get(editP), null, null);
                pointLabelTable.remove(editP);
            }
            if (changesHistory.size() >= historyCountMax){
                changesHistory.removeFirst();
            }
            changesHistory.addLast(thisChange);
            undoHistory.clear();
            changeMade = true;
            frame.setTitle(APPLICATION_NAME + " - " + currentImgName + "*");
            //System.out.println(e.getPoint().toString() + " " + pointLabelTable.size());
            repaint();
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
            currentCursorPosLb.setText("Current Position: (" + (int) (e.getX() / currentZoom) + ", "
                    + (int) (e.getY() / currentZoom) + ")");
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

        private Shape createRingShape(double centerX, double centerY, double outerRadius, double thickness) {
            Ellipse2D outer = new Ellipse2D.Double(centerX - outerRadius, centerY - outerRadius,
                    outerRadius + outerRadius, outerRadius + outerRadius);
            Ellipse2D inner = new Ellipse2D.Double(centerX - outerRadius + thickness, centerY - outerRadius + thickness,
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
            
            JMenu helpMenu = new JMenu("About");
            JMenuItem itemAbout = new JMenuItem("About " + APPLICATION_NAME);
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

        private static final String CONFIG_SHAPE_SIZE = "shape_size";
        private static final String CONFIG_SHAPE_THICKNESS = "shape_thickness";

        private static final String CONFIG_LABELS = "labels";
        private static final String CONFIG_LABEL = "label";
        private static final String CONFIG_LABEL_ID = "id";
        private static final String CONFIG_LABEL_NAME = "name";
        private static final String CONFIG_LABEL_VALUE = "value";
        private static final String CONFIG_LABEL_FILL_COLOR = "fill_color";
        private static final String CONFIG_LABEL_BORDER_COLOR = "border_color";

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

                xMLStreamWriter.writeStartElement(CONFIG_SHAPE_SIZE);
                xMLStreamWriter.writeCharacters(String.valueOf(drawOvalSize));
                xMLStreamWriter.writeEndElement();

                xMLStreamWriter.writeStartElement(CONFIG_SHAPE_THICKNESS);
                xMLStreamWriter.writeCharacters(String.valueOf(drawOvalThickness));
                xMLStreamWriter.writeEndElement();

                xMLStreamWriter.writeStartElement(CONFIG_LABELS);

                for (Map.Entry<Long, FunctionLabelDescriptor> entry : labelDescriptorTable.entrySet()) {
                    Long id = entry.getKey();
                    FunctionLabelDescriptor labelDescriptor = entry.getValue();

                    xMLStreamWriter.writeStartElement(CONFIG_LABEL);

                    xMLStreamWriter.writeStartElement(CONFIG_LABEL_ID);
                    xMLStreamWriter.writeCharacters(String.valueOf(labelDescriptor.id));
                    xMLStreamWriter.writeEndElement();

                    xMLStreamWriter.writeStartElement(CONFIG_LABEL_NAME);
                    xMLStreamWriter.writeCharacters(labelDescriptor.name);
                    xMLStreamWriter.writeEndElement();

                    xMLStreamWriter.writeStartElement(CONFIG_LABEL_VALUE);
                    xMLStreamWriter.writeCharacters(String.valueOf(labelDescriptor.value));
                    xMLStreamWriter.writeEndElement();

                    xMLStreamWriter.writeStartElement(CONFIG_LABEL_FILL_COLOR);
                    xMLStreamWriter.writeCharacters(String.valueOf(labelDescriptor.draw_fill_color.getRGB()));
                    xMLStreamWriter.writeEndElement();

                    xMLStreamWriter.writeStartElement(CONFIG_LABEL_BORDER_COLOR);
                    xMLStreamWriter.writeCharacters(String.valueOf(labelDescriptor.draw_border_color.getRGB()));
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
                                } else if (qName.equalsIgnoreCase(CONFIG_SHAPE_SIZE)) {
                                    event = eventReader.nextEvent();
                                    drawOvalSize = Integer.valueOf(event.asCharacters().getData());
                                } else if (qName.equalsIgnoreCase(CONFIG_SHAPE_THICKNESS)) {
                                    event = eventReader.nextEvent();
                                    drawOvalThickness = Integer.valueOf(event.asCharacters().getData());
                                } else if (qName.equalsIgnoreCase(CONFIG_LABELS)) {
                                    bLabel = true;
                                    labelDescriptorTable.clear();
                                } else if (qName.equalsIgnoreCase(CONFIG_LABEL)){
                                    FunctionLabelDescriptor ld = parseLabelXML(eventReader);
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
        
        public FunctionLabelDescriptor parseLabelXML(XMLEventReader eventReader) throws XMLStreamException{
            FunctionLabelDescriptor labelDescriptor = new FunctionLabelDescriptor();
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
                        } else if (qName.equalsIgnoreCase(CONFIG_LABEL_VALUE)) {
                            event = eventReader.nextEvent();
                            labelDescriptor.value = Long.valueOf(event.asCharacters().getData());
                        } else if (qName.equalsIgnoreCase(CONFIG_LABEL_FILL_COLOR)) {
                            event = eventReader.nextEvent();
                            labelDescriptor.draw_fill_color = new Color(Integer.valueOf(event.asCharacters().getData()), true);
                        } else if (qName.equalsIgnoreCase(CONFIG_LABEL_BORDER_COLOR)) {
                            event = eventReader.nextEvent();
                            labelDescriptor.draw_border_color = new Color(Integer.valueOf(event.asCharacters().getData()), true);
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
