package hk.edu.hku.cs.citang.urfp.supp.label.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class DatabaseManager {
    private static final String VERSION = "2.0";

    Connection connection;
    private String databasePath;

    private static char CSV_SEPARATOR = '|';
    private static char CSV_QUOTE = '\'';
    private static String CSV_DOC_SEPERATOR = "/////";

    private static final String FILES_TABLE_NAME = "FILES";
    private static final String LABEL_TABLE_NAME = "LABEL";
    private static final String LABELS_TABLE_NAME = "LABELS";
    private static final String BOUNDING_BOXES_TABLE_NAME = "BOUNDINGBOXES";

    private static final String FILES_TABLE_CREATE = "CREATE TABLE " + FILES_TABLE_NAME + "("
            + "file_id BIGINT NOT NULL primary key GENERATED ALWAYS AS IDENTITY "
            + "(START WITH 1, INCREMENT BY 1), "
            + "name VARCHAR(4096) NOT NULL, "
            + "path VARCHAR(4096) NOT NULL, "
            + "height INT DEFAULT 0 NOT NULL, "
            + "width INT DEFAULT 0 NOT NULL, "
            + "hash VARCHAR(128) NOT NULL)";

    private static final String LABEL_TABLE_CREATE = "CREATE TABLE " + LABEL_TABLE_NAME + "("
            + "label_id BIGINT NOT NULL primary key, " + "name VARCHAR(4096) NOT NULL)";

    private static final String LABELS_TABLE_CREATE = "CREATE TABLE " + LABELS_TABLE_NAME + "("
            + "file_id BIGINT NOT NULL, " 
            + "x_coor INT NOT NULL, "
            + "y_coor INT NOT NULL, "
            + "label BIGINT NOT NULL, "
            + "FOREIGN KEY(file_id) REFERENCES files (file_id), " 
            + "FOREIGN KEY(label) REFERENCES label (label_id) )";

    private static final String BOUNDING_BOXES_TABLE_CREATE = "CREATE TABLE " + BOUNDING_BOXES_TABLE_NAME + "("
            + "file_id BIGINT NOT NULL, " 
            + "x_coor INT NOT NULL, " 
            + "y_coor INT NOT NULL, " 
            + "width INT NOT NULL, "
            + "height INT NOT NULL, "
            + "label BIGINT NOT NULL, "
            + "FOREIGN KEY(file_id) REFERENCES files (file_id), " 
            + "FOREIGN KEY(label) REFERENCES label (label_id) )";
    
    private static final String FILE_SELECT_ALL = "SELECT * FROM files ORDER BY file_id ASC";
    private static final String FILE_COUNT_BY_PATH = "SELECT COUNT(*) FROM files WHERE path = ?";
    private static final String FILE_SELECT = "SELECT * FROM files";
    private static final String FILE_SELECT_CONSTRAINT_PATH = " path = ?";
    private static final String FILE_SELECT_CONSTRAINT_HASH = " hash = ?";
    private static final String FILE_SELECT_CONSTRAINT_NAME = " name = ?";
    private static final String FILE_SELECT_CONSTRAINT_ID = " file_id = ?";

    private static final String UPDATE_FILE = "UPDATE files SET name = ?, path = ?, height = ?, width = ?, hash = ? WHERE file_id = ?";
    private static final String INSERT_FILE = "INSERT INTO files(name, path, height, width, hash) VALUES (?,?,?,?,?)";
    private static final String INSERT_FILE_WITH_ID = "INSERT INTO files(file_id, name, path, height, width, hash) OVERRIDING SYSTEM VALUE VALUES ???";
    private static final String DELETE_FILE_ALL = "DELETE FROM files";

    private static final String LABEL_SELECT_ALL = "SELECT * FROM label ORDER BY label_id ASC";
    private static final String MERGE_LABEL = "MERGE INTO label AS T USING (SELECT * FROM ( VALUES ??? ) AS Source (id, name) ) As S "
            + "ON (T.label_id = S.id) " + "WHEN MATCHED THEN UPDATE SET T.name = S.name "
            + "WHEN NOT MATCHED THEN INSERT (label_id, name) VALUES (S.id, S.name) ";
    private static final String INSERT_LABEL = "INSERT INTO label (label_id, name) VALUES ???";
    private static final String DELETE_LABEL_ALL = "DELETE FROM label";
    
    private static final String POINT_LABEL_SELECT_ALL = "SELECT * FROM labels ORDER BY file_id ASC, x_coor ASC, y_coor ASC, label ASC";
    private static final String POINT_LABEL_SELECT_BY_ID = "SELECT * FROM labels WHERE file_id = ?";
    private static final String MERGE_POINT_LABEL = "MERGE INTO labels AS T USING (VALUES ???) "
            + "AS S (id, x, y, newlabel) ON (T.file_id = S.id AND T.x_coor = S.x AND T.y_coor = S.y) "
            + "WHEN MATCHED THEN UPDATE SET T.label = S.newlabel "
            + "WHEN NOT MATCHED BY SOURCE THEN INSERT (file_id, x_coor, y_coor, label) VALUES (S.id, S.x, S.y, S.newlabel) "
            + "WHEN NOT MATCHED BY TARGET THEN DELETE";
    private static final String DELETE_POINT_LABEL_BY_ID = "DELETE FROM labels WHERE file_id = ?";
    private static final String DELETE_POINT_LABEL_ALL = "DELETE FROM labels";
    private static final String INSERT_POINT_LABEL = "INSERT INTO labels (file_id, x_coor, y_coor, label) VALUES ???";

    private static final String BOUNDING_BOX_LABEL_SELECT_ALL = "SELECT * FROM boundingboxes ORDER BY file_id ASC, x_coor ASC, y_coor ASC, label ASC";
    private static final String BOUNDING_BOX_LABEL_SELECT_BY_ID = "SELECT * FROM boundingboxes WHERE file_id = ?";
    private static final String MERGE_BOUNDING_BOX_LABEL = "MERGE INTO boundingboxes AS T USING (VALUES ???) "
            + "AS S (id, x, y, w, h, newlabel) ON (T.file_id = S.id AND T.x_coor = S.x AND T.y_coor = S.y AND T.width = S.w AND T.height = S.h) "
            + "WHEN MATCHED THEN UPDATE SET T.label = S.newlabel "
            + "WHEN NOT MATCHED BY SOURCE THEN INSERT (file_id, x_coor, y_coor, width, height, label) VALUES (S.id, S.x, S.y, S.w, S.h, S.newlabel) "
            + "WHEN NOT MATCHED BY TARGET THEN DELETE";
    private static final String DELETE_BOUNDING_BOX_LABEL_BY_ID = "DELETE FROM boundingboxes WHERE file_id = ?";
    private static final String DELETE_BOUNDING_BOX_LABEL_ALL = "DELETE FROM boundingboxes";
    private static final String INSERT_BOUNDING_BOX_LABEL = "INSERT INTO boundingboxes (file_id, x_coor, y_coor, width, height, label) VALUES ???";

    
    
    // private static final String LOGININC= "UPDATE statistics SET value =
    // value+1 WHERE key='Total logins'";
    // private static final String MESINC= "UPDATE statistics SET value =
    // value+1 WHERE key='Total messages'";
    // private static final String MESINSERT= "INSERT INTO
    // MESSAGES(nick,message,timeposted) VALUES (?,?,?)";
    // private static final String MESREC = "SELECT nick,message,timeposted FROM
    // messages ORDER BY timeposted DESC LIMIT 10";
    //
    public DatabaseManager(String databasePath) throws SQLException {
        this.databasePath = databasePath;
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        connection = DriverManager.getConnection("jdbc:hsqldb:file:" + databasePath, "SA", "");
        Statement delayStmt = connection.createStatement();
        try {
            delayStmt.execute("SET WRITE_DELAY FALSE"); // Always update data on
                                                        // disk
        } finally {
            delayStmt.close();
        }

        connection.setAutoCommit(false);

        String[] tableTypes = { "TABLE" };
        ResultSet rs = connection.getMetaData().getTables(null, null, FILES_TABLE_NAME, tableTypes);
        if (!rs.isBeforeFirst()) {
            Statement sqlStmt = connection.createStatement();
            try {
                sqlStmt.execute(FILES_TABLE_CREATE);

            } catch (SQLException e) {
                e.printStackTrace();
                // System.out.println("Info: Database table \"files\" already
                // exists.");
            }
        }

        rs = connection.getMetaData().getTables(null, null, LABEL_TABLE_NAME, tableTypes);
        if (!rs.isBeforeFirst()) {
            Statement sqlStmt = connection.createStatement();
            try {
                sqlStmt.execute(LABEL_TABLE_CREATE);
            } catch (SQLException e) {
                e.printStackTrace();
                // System.out.println("Info: Database table \"labels\" already
                // exists.");
            } finally {
                sqlStmt.close();
            }
        }

        rs = connection.getMetaData().getTables(null, null, LABELS_TABLE_NAME, tableTypes);
        if (!rs.isBeforeFirst()) {
            Statement sqlStmt = connection.createStatement();
            try {
                sqlStmt.execute(LABELS_TABLE_CREATE);
            } catch (SQLException e) {
                e.printStackTrace();
                // System.out.println("Info: Database table \"labels\" already
                // exists.");
            } finally {
                sqlStmt.close();
            }
        }
        
        rs = connection.getMetaData().getTables(null, null, BOUNDING_BOXES_TABLE_NAME, tableTypes);
        if (!rs.isBeforeFirst()) {
            Statement sqlStmt = connection.createStatement();
            try {
                sqlStmt.execute(BOUNDING_BOXES_TABLE_CREATE);
            } catch (SQLException e) {
                e.printStackTrace();
                // System.out.println("Info: Database table \"labels\" already
                // exists.");
            } finally {
                sqlStmt.close();
            }
        }

        connection.commit();
    }

    public void backup() throws SQLException {
        Statement backupStmt = connection.createStatement();
        backupStmt.executeQuery("BACKUP DATABASE TO 'test_db/' BLOCKING");
    }

    public void close() throws SQLException {
        connection.commit();
        connection.close();
    }

    public boolean updateLabel(HashMap<Long, LabelDescriptor> labelTable) throws SQLException {
        //System.out.println("UPDATING LABEL TABLE");

        if (!labelTable.isEmpty()) {
            List<StringBuilder> sbs = new LinkedList<StringBuilder>();
            int count = 0;
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Long, LabelDescriptor> entry : labelTable.entrySet()) {
                if (count == 100) {
                    sbs.add(sb);
                    sb = new StringBuilder();
                    count = 0;
                }
                sb.append("(" + (int) (entry.getValue().getId()) + ", '" + (entry.getValue().getName()) + "'), ");
                count += 1;
            }
            sbs.add(sb);

            for (StringBuilder s : sbs) {
                String values = s.toString();
                PreparedStatement mergeLabelStm = connection
                        .prepareStatement(MERGE_LABEL.replace("???", values.substring(0, values.length() - 2)));
                try {
                    mergeLabelStm.executeUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    mergeLabelStm.close();
                }
            }
        }
        connection.commit();

        return true;
    }

    public File getFileByID(long file_id) throws SQLException {
        String queryStm = FILE_SELECT + " WHERE" + FILE_SELECT_CONSTRAINT_ID;
        PreparedStatement getFileByIDStm = connection.prepareStatement(queryStm);

        File result = null;
        try {
            getFileByIDStm.setString(1, String.valueOf(file_id));
            ResultSet rs = getFileByIDStm.executeQuery();
            try {
                while (rs.next()) {
                    result = new File(rs.getString("path"));
                }
            } finally {
                rs.close();
            }
        } finally {
            getFileByIDStm.close();
        }
        return result;
    }

    public List<Long> getFileIDByConstraints(File chosenFile, boolean pathConstraint, boolean hashConstraint,
            boolean nameConstraint) throws SQLException {
        String queryStm = "";

        if (pathConstraint) {
            queryStm += FILE_SELECT_CONSTRAINT_PATH + " AND";
        }
        if (hashConstraint) {
            queryStm += FILE_SELECT_CONSTRAINT_HASH + " AND";
        }
        if (nameConstraint) {
            queryStm += FILE_SELECT_CONSTRAINT_NAME + " AND";
        }
        if (pathConstraint || hashConstraint || nameConstraint) {
            queryStm = " WHERE" + queryStm;
            queryStm = queryStm.substring(0, queryStm.length() - " AND".length());
        }
        PreparedStatement getFileByConstraintsStm = connection.prepareStatement(FILE_SELECT + queryStm);

        List<Long> ids = new ArrayList<Long>();
        try {
            int i = 1;
            if (pathConstraint) {
                getFileByConstraintsStm.setString(i, chosenFile.getAbsolutePath());
                i += 1;
            }
            if (hashConstraint) {
                getFileByConstraintsStm.setString(i, getSHA256(chosenFile));
                i += 1;
            }
            if (nameConstraint) {
                getFileByConstraintsStm.setString(i, chosenFile.getName());
                i += 1;
            }
            ResultSet rs = getFileByConstraintsStm.executeQuery();
            try {
                while (rs.next()) {
                    ids.add(rs.getLong("file_id"));
                }
            } finally {
                rs.close();
            }
        } finally {
            getFileByConstraintsStm.close();
        }
        return ids;
    }

    // public List<Long> getFileIDByHash(File chosenFile) throws SQLException{
    // PreparedStatement getFileIDByHashStm =
    // connection.prepareStatement(FILE_SELECT_BY_HASH);
    //
    // List<Long> ids = new ArrayList<Long>();
    // try {
    // getFileIDByHashStm.setString(1, getSHA256(chosenFile));
    // ResultSet rs = getFileIDByHashStm.executeQuery();
    // try {
    // while (rs.next()){
    // ids.add(rs.getLong("file_id"));
    // }
    // } finally {
    // rs.close();
    // }
    // } finally {
    // getFileIDByHashStm.close();
    // }
    // return ids;
    // }

    public void updateFile(long fileID, File file) throws SQLException {
        PreparedStatement updateMessage = connection.prepareStatement(UPDATE_FILE);
        updateMessage.setString(1, file.getName());
        updateMessage.setString(2, file.getAbsolutePath());
        try {
            BufferedImage image = ImageIO.read(file);
            updateMessage.setInt(3, image.getHeight());
            updateMessage.setInt(4, image.getWidth());
        } catch (IOException e) {
            updateMessage.setInt(3, 0);
            updateMessage.setInt(4, 0);
        }
        updateMessage.setString(5, getSHA256(file));
        updateMessage.setLong(6, fileID);
        synchronized (this) {
            try {
                updateMessage.executeUpdate();
            } finally { // Notice use of finally clause here to finish statement
                updateMessage.close();
                connection.commit();
            }

        }

    }

    public long insertFile(File file) throws SQLException {
        PreparedStatement insertMessage = connection.prepareStatement(INSERT_FILE, Statement.RETURN_GENERATED_KEYS);
        insertMessage.setString(1, file.getName());
        insertMessage.setString(2, file.getAbsolutePath());
        try {
            BufferedImage image = ImageIO.read(file);
            insertMessage.setInt(3, image.getHeight());
            insertMessage.setInt(4, image.getWidth());
        } catch (IOException e) {
            insertMessage.setInt(3, 0);
            insertMessage.setInt(4, 0);
        }
        
        insertMessage.setString(5, getSHA256(file));
        synchronized (this) {
            try {
                int affectedRowsCount = insertMessage.executeUpdate();
                if (affectedRowsCount == 0) {
                    throw new SQLException("Creating user failed, no rows affected.");
                }
                try (ResultSet generatedKeys = insertMessage.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return (generatedKeys.getLong(1));
                    } else {
                        throw new SQLException("Creating user failed, no ID obtained.");
                    }
                }
            } finally { // Notice use of finally clause here to finish statement
                insertMessage.close();
                connection.commit();
            }

        }
    }

    public HashMap<Point, Long> getPointLabelsByFileID(Long file_id) throws SQLException {
        PreparedStatement getPointLabelStm = connection.prepareStatement(POINT_LABEL_SELECT_BY_ID);

        HashMap<Point, Long> plt = new HashMap<Point, Long>();
        try {
            getPointLabelStm.setLong(1, file_id);
            ResultSet rs = getPointLabelStm.executeQuery();
            try {
                while (rs.next()) {
                    plt.put(new Point(rs.getInt("x_coor"), rs.getInt("y_coor")), rs.getLong("label"));
                }
            } finally {
                rs.close();
            }
        } finally {
            getPointLabelStm.close();
        }
        return plt;
    }
    
    public HashMap<Rectangle, Long> getBoundingBoxLabelsByFileID(Long file_id) throws SQLException {
        PreparedStatement getBoundingBoxLabelStm = connection.prepareStatement(BOUNDING_BOX_LABEL_SELECT_BY_ID);

        HashMap<Rectangle, Long> bblt = new HashMap<Rectangle, Long>();
        try {
            getBoundingBoxLabelStm.setLong(1, file_id);
            ResultSet rs = getBoundingBoxLabelStm.executeQuery();
            try {
                while (rs.next()) {
                    bblt.put(new Rectangle(rs.getInt("x_coor"), rs.getInt("y_coor"), rs.getInt("width"), rs.getInt("height")), rs.getLong("label"));
                }
            } finally {
                rs.close();
            }
        } finally {
            getBoundingBoxLabelStm.close();
        }
        return bblt;
    }

    public boolean updatePointLabels(Long file_id, HashMap<Point, Long> plt) throws SQLException {

        PreparedStatement deletePointLabelStm = connection.prepareStatement(DELETE_POINT_LABEL_BY_ID);
        deletePointLabelStm.setLong(1, file_id);

        // PreparedStatement updatePointLabelStm =
        // connection.prepareStatement(MERGE_POINT_LABEL.replace("???",
        // values.substring(0, values.length() - 2)));
        try {
            deletePointLabelStm.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally { // Notice use of finally clause here to finish statement
            deletePointLabelStm.close();
        }

        if (!plt.isEmpty()) {
            List<StringBuilder> sbs = new LinkedList<StringBuilder>();
            int count = 0;
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Point, Long> entry : plt.entrySet()) {
                if (count == 100) {
                    sbs.add(sb);
                    sb = new StringBuilder();
                    count = 0;
                }
                sb.append("(" + file_id + "," + (int) (entry.getKey().getX()) + ", " + (int) (entry.getKey().getY())
                        + ", " + entry.getValue() + "), ");
                count += 1;
            }
            sbs.add(sb);

            for (StringBuilder s : sbs) {
                String values = s.toString();
                PreparedStatement insertPointLabelStm = connection
                        .prepareStatement(INSERT_POINT_LABEL.replace("???", values.substring(0, values.length() - 2)));
                try {
                    insertPointLabelStm.executeUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    insertPointLabelStm.close();
                }
            }
        }
        connection.commit();

        return true;
    }
    
    public boolean updateBoundingBoxLabels(Long file_id, HashMap<Rectangle, Long> bblt) throws SQLException {

        PreparedStatement deleteBoundingBoxLabelStm = connection.prepareStatement(DELETE_BOUNDING_BOX_LABEL_BY_ID);
        deleteBoundingBoxLabelStm.setLong(1, file_id);

        try {
            deleteBoundingBoxLabelStm.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally { // Notice use of finally clause here to finish statement
            deleteBoundingBoxLabelStm.close();
        }

        if (!bblt.isEmpty()) {
            List<StringBuilder> sbs = new LinkedList<StringBuilder>();
            int count = 0;
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Rectangle, Long> entry : bblt.entrySet()) {
                if (count == 100) {
                    sbs.add(sb);
                    sb = new StringBuilder();
                    count = 0;
                }
                sb.append("(" + file_id + "," 
                        + (int) (entry.getKey().getX()) + ", " 
                        + (int) (entry.getKey().getY()) + ", "
                        + (int) (entry.getKey().getWidth()) + ", "
                        + (int) (entry.getKey().getHeight()) + ", "
                        + entry.getValue() + "), ");
                count += 1;
            }
            sbs.add(sb);

            for (StringBuilder s : sbs) {
                String values = s.toString();
                PreparedStatement insertBoundingBoxLabelStm = connection
                        .prepareStatement(INSERT_BOUNDING_BOX_LABEL.replace("???", values.substring(0, values.length() - 2)));
                try {
                    insertBoundingBoxLabelStm.executeUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    insertBoundingBoxLabelStm.close();
                }
            }
        }
        connection.commit();

        return true;
    }

    public static String getSHA256(File file) {
        byte[] buffer = new byte[8192];
        int count;
        MessageDigest digest;
        BufferedInputStream bis = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            bis = new BufferedInputStream(new FileInputStream(file));
            while ((count = bis.read(buffer)) > 0) {
                digest.update(buffer, 0, count);
            }
            byte[] hash = digest.digest();
            String encodedHash = bytesToHex(hash);
            // System.out.println(encodedHash);
            return encodedHash;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;

    }

    public static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte byt : bytes)
            result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

    public void databaseExportAsCSV(String filePath) throws IOException, SQLException {
        FileWriter writer = new FileWriter(filePath);
        writer.append("DBVERSION:" + VERSION + "\n");
        writer.append(CSV_DOC_SEPERATOR + FILES_TABLE_NAME + CSV_DOC_SEPERATOR + "\n");
        PreparedStatement getAllFileStm = connection.prepareStatement(FILE_SELECT_ALL);
        List<String> rowArray = new ArrayList<String>();
        try {
            ResultSet rs = getAllFileStm.executeQuery();
            try {
                while (rs.next()) {
                    rowArray.add(String.valueOf(rs.getLong("file_id")));
                    rowArray.add(rs.getString("name"));
                    rowArray.add(rs.getString("path"));
                    rowArray.add(rs.getString("width"));
                    rowArray.add(rs.getString("height"));
                    rowArray.add(rs.getString("hash"));
                    CSVUtils.writeLine(writer, rowArray, CSV_SEPARATOR, CSV_QUOTE);
                    rowArray.clear();
                }
                rowArray.clear();
            } finally {
                rs.close();
            }
        } finally {
            getAllFileStm.close();
        }

        writer.append(CSV_DOC_SEPERATOR + LABEL_TABLE_NAME + CSV_DOC_SEPERATOR + "\n");

        PreparedStatement getAllLabelStm = connection.prepareStatement(LABEL_SELECT_ALL);

        try {
            ResultSet rs = getAllLabelStm.executeQuery();
            try {
                while (rs.next()) {
                    rowArray.add(String.valueOf(rs.getLong("label_id")));
                    rowArray.add(rs.getString("name"));
                    CSVUtils.writeLine(writer, rowArray, CSV_SEPARATOR, CSV_QUOTE);
                    rowArray.clear();
                }
                rowArray.clear();
            } finally {
                rs.close();
            }
        } finally {
            getAllLabelStm.close();
        }

        writer.append(CSV_DOC_SEPERATOR + LABELS_TABLE_NAME + CSV_DOC_SEPERATOR + "\n");

        PreparedStatement getAllPointLabelStm = connection.prepareStatement(POINT_LABEL_SELECT_ALL);

        try {
            ResultSet rs = getAllPointLabelStm.executeQuery();
            try {
                while (rs.next()) {
                    rowArray.add(String.valueOf(rs.getLong("file_id")));
                    rowArray.add(String.valueOf(rs.getInt("x_coor")));
                    rowArray.add(String.valueOf(rs.getInt("y_coor")));
                    rowArray.add(String.valueOf(rs.getLong("label")));
                    CSVUtils.writeLine(writer, rowArray, CSV_SEPARATOR, CSV_QUOTE);
                    rowArray.clear();
                }
                rowArray.clear();
            } finally {
                rs.close();
            }
        } finally {
            getAllPointLabelStm.close();
        }
        
        writer.append(CSV_DOC_SEPERATOR + BOUNDING_BOXES_TABLE_NAME + CSV_DOC_SEPERATOR + "\n");

        PreparedStatement getAllBoundingBoxLabelStm = connection.prepareStatement(BOUNDING_BOX_LABEL_SELECT_ALL);

        try {
            ResultSet rs = getAllBoundingBoxLabelStm.executeQuery();
            try {
                while (rs.next()) {
                    rowArray.add(String.valueOf(rs.getLong("file_id")));
                    rowArray.add(String.valueOf(rs.getInt("x_coor")));
                    rowArray.add(String.valueOf(rs.getInt("y_coor")));
                    rowArray.add(String.valueOf(rs.getInt("width")));
                    rowArray.add(String.valueOf(rs.getInt("height")));
                    rowArray.add(String.valueOf(rs.getInt("label")));
                    CSVUtils.writeLine(writer, rowArray, CSV_SEPARATOR, CSV_QUOTE);
                    rowArray.clear();
                }
                rowArray.clear();
            } finally {
                rs.close();
            }
        } finally {
            getAllBoundingBoxLabelStm.close();
        }


        writer.flush();
        writer.close();
    }

    public boolean databaseImportFromCSV(String filePath) throws IOException, SQLException {
        Scanner scanner = new Scanner(new File(filePath));

        String rowText = "";
        String importDBVersion = "";
        while (scanner.hasNext()) {
            rowText = scanner.nextLine();

            if (rowText.startsWith("DBVERSION:")) {
                importDBVersion = rowText.replace("DBVERSION:", "");
                if (importDBVersion.equals(VERSION)) {
                    
                } else {
                    System.out.println("Warning: Database Version Mismatch");
                }
            }

            if (rowText.startsWith(CSV_DOC_SEPERATOR + FILES_TABLE_NAME)) {

                int count = 0;
                StringBuilder sb = new StringBuilder();

                while (scanner.hasNext()) {
                    rowText = scanner.nextLine();

                    if (rowText.startsWith(CSV_DOC_SEPERATOR)) {

                        break;
                    }

                    if (count == 100) {
                        String files = sb.toString();
                        PreparedStatement insertFileStm = connection.prepareStatement(
                                INSERT_FILE_WITH_ID.replace("???", files.substring(0, files.length() - 2)));

                        try {
                            insertFileStm.executeUpdate();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        } finally { // Notice use of finally clause here to
                                    // finish statement
                            insertFileStm.close();
                        }

                        sb = new StringBuilder();

                        count = 0;
                    }

                    List<String> row = CSVUtils.parseLine(rowText, CSV_SEPARATOR, CSV_QUOTE);
                    if (importDBVersion.startsWith("1.")){
                        sb.append("(" + Long.valueOf(row.get(0)) + ", '" + row.get(1) + "', '" + row.get(2) + "', '" 
                                + 0 + "', '"  + 0 + "', '"
                                + row.get(3) + "'), ");
                    } else if (importDBVersion.startsWith("2.")){
                        sb.append("(" + Long.valueOf(row.get(0)) + ", '" + row.get(1) + "', '" + row.get(2) + "', '" 
                                + Integer.valueOf(row.get(3)) + "', '"  + Integer.valueOf(row.get(4)) + "', '"
                                + row.get(5) + "'), ");
                    }

                    count += 1;
                }

                if (count != 0) {
                    String files = sb.toString();
                    PreparedStatement insertFileStm = connection.prepareStatement(
                            INSERT_FILE_WITH_ID.replace("???", files.substring(0, files.length() - 2)));
                    //System.out.println(insertFileStm.toString());
                    try {
                        insertFileStm.executeUpdate();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    } finally { // Notice use of finally clause here to finish
                                // statement
                        insertFileStm.close();
                    }
                }
            }

            if (rowText.startsWith(CSV_DOC_SEPERATOR + LABEL_TABLE_NAME)) {

                int count = 0;
                StringBuilder sb = new StringBuilder();

                while (scanner.hasNext()) {
                    rowText = scanner.nextLine();

                    if (rowText.startsWith(CSV_DOC_SEPERATOR)) {
                        break;
                    }

                    if (count == 100) {
                        String label = sb.toString();
                        PreparedStatement insertLabelStm = connection.prepareStatement(
                                INSERT_LABEL.replace("???", label.substring(0, label.length() - 2)));

                        try {
                            insertLabelStm.executeUpdate();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        } finally { // Notice use of finally clause here to
                                    // finish statement
                            insertLabelStm.close();
                        }

                        sb = new StringBuilder();

                        count = 0;
                    }

                    List<String> row = CSVUtils.parseLine(rowText, CSV_SEPARATOR, CSV_QUOTE);

                    sb.append("(" + Long.valueOf(row.get(0)) + ", '" + row.get(1) + "'), ");
                    count += 1;
                }

                if (count != 0) {
                    String label = sb.toString();
                    PreparedStatement insertLabelStm = connection.prepareStatement(
                            INSERT_LABEL.replace("???", label.substring(0, label.length() - 2)));

                    try {
                        insertLabelStm.executeUpdate();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    } finally { // Notice use of finally clause here to
                                // finish statement
                        insertLabelStm.close();
                    }
                }

            }

            if (rowText.startsWith(CSV_DOC_SEPERATOR + LABELS_TABLE_NAME)) {

                int count = 0;
                StringBuilder sb = new StringBuilder();

                while (scanner.hasNext()) {
                    rowText = scanner.nextLine();

                    if (rowText.startsWith(CSV_DOC_SEPERATOR)) {

                        break;
                    }

                    if (count == 100) {
                        String labels = sb.toString();
                        PreparedStatement insertPointLabelStm = connection.prepareStatement(
                                INSERT_POINT_LABEL.replace("???", labels.substring(0, labels.length() - 2)));

                        try {
                            insertPointLabelStm.executeUpdate();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        } finally { // Notice use of finally clause here to
                                    // finish statement
                            insertPointLabelStm.close();
                        }

                        sb = new StringBuilder();

                        count = 0;
                    }

                    List<String> row = CSVUtils.parseLine(rowText, CSV_SEPARATOR, CSV_QUOTE);

                    sb.append("(" + Long.valueOf(row.get(0)) + "," + Integer.valueOf(row.get(1)) + ", "
                            + Integer.valueOf(row.get(2)) + ", " + Integer.valueOf(row.get(3)) + "), ");
                    count += 1;
                }

                if (count != 0) {
                    String labels = sb.toString();
                    PreparedStatement insertPointLabelStm = connection.prepareStatement(
                            INSERT_POINT_LABEL.replace("???", labels.substring(0, labels.length() - 2)));

                    try {
                        insertPointLabelStm.executeUpdate();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    } finally { // Notice use of finally clause here to
                                // finish statement
                        insertPointLabelStm.close();
                    }
                }

            }
            if (rowText.startsWith(CSV_DOC_SEPERATOR + BOUNDING_BOXES_TABLE_NAME)) {
                int count = 0;
                StringBuilder sb = new StringBuilder();

                while (scanner.hasNext()) {
                    rowText = scanner.nextLine();

                    if (rowText.startsWith(CSV_DOC_SEPERATOR)) {

                        break;
                    }

                    if (count == 100) {
                        String labels = sb.toString();
                        PreparedStatement insertBoundingBoxLabelStm = connection.prepareStatement(
                                INSERT_BOUNDING_BOX_LABEL.replace("???", labels.substring(0, labels.length() - 2)));

                        try {
                            insertBoundingBoxLabelStm.executeUpdate();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        } finally { // Notice use of finally clause here to
                                    // finish statement
                            insertBoundingBoxLabelStm.close();
                        }

                        sb = new StringBuilder();

                        count = 0;
                    }

                    List<String> row = CSVUtils.parseLine(rowText, CSV_SEPARATOR, CSV_QUOTE);
                    sb.append("(" + Long.valueOf(row.get(0)) + "," 
                            + Integer.valueOf(row.get(1)) + ", "
                            + Integer.valueOf(row.get(2)) + ", " 
                            + Integer.valueOf(row.get(3)) + ", " 
                            + Integer.valueOf(row.get(4)) + ", " 
                            + Long.valueOf(row.get(5)) + "), ");
                    
                    count += 1;
                }

                if (count != 0) {
                    String labels = sb.toString();
                    PreparedStatement insertBoundingBoxLabelStm = connection.prepareStatement(
                            INSERT_BOUNDING_BOX_LABEL.replace("???", labels.substring(0, labels.length() - 2)));

                    try {
                        insertBoundingBoxLabelStm.executeUpdate();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    } finally { // Notice use of finally clause here to
                                // finish statement
                        insertBoundingBoxLabelStm.close();
                    }
                }

            }
        }

        scanner.close();
        connection.commit();
        return true;
    }

    public void clearAllData() {
        Statement deleteFilesStm;
        Statement deletePointLabelsStm;
        Statement deleteBoundingBoxesStm;
        Statement deleteLabelsStm;
        try {
            deletePointLabelsStm = connection.createStatement();
            deletePointLabelsStm.executeQuery(DELETE_POINT_LABEL_ALL);

            deleteBoundingBoxesStm = connection.createStatement();
            deleteBoundingBoxesStm.executeQuery(DELETE_BOUNDING_BOX_LABEL_ALL);
            
            deleteFilesStm = connection.createStatement();
            deleteFilesStm.executeQuery(DELETE_FILE_ALL);
            

            deleteLabelsStm = connection.createStatement();
            deleteLabelsStm.executeQuery(DELETE_LABEL_ALL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
