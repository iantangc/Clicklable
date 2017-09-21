package hk.edu.hku.cs.citang.urfp.supp.label.gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.Shape;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class DatabaseManager {
    private Connection connection;
    private String databasePath;
    
    private static char CSV_SEPARATOR = '|';
    
    private static char CSV_QUOTE = '\'';
    private static String CSV_DOC_SEPERATOR = "/////";
    
    private static final String FILE_SELECT_ALL = "SELECT * FROM files ORDER BY file_id ASC";
    private static final String FILE_COUNT_BY_PATH = "SELECT COUNT(*) FROM files WHERE path = ?";
    private static final String FILE_SELECT_BY_PATH = "SELECT * FROM files WHERE path = ?";
    private static final String FILE_SELECT_BY_NAME = "SELECT * FROM files WHERE name = ?";
    private static final String FILE_SELECT_BY_HASH = "SELECT * FROM files WHERE hash = ?";
    
    private static final String UPDATE_FILE = "UPDATE files SET name = ?, path = ?, hash = ? WHERE file_id = ?";
    private static final String INSERT_FILE = "INSERT INTO files(name, path, hash) VALUES (?,?,?)";
    private static final String INSERT_FILE_WITH_ID = "INSERT INTO files(file_id, name, path, hash) OVERRIDING SYSTEM VALUE VALUES ???";
    private static final String DELETE_FILE_ALL = "DELETE FROM files";
    
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
    
//    private static final String LOGININC= "UPDATE statistics SET value = value+1 WHERE key='Total logins'";
//    private static final String MESINC= "UPDATE statistics SET value = value+1 WHERE key='Total messages'";
//    private static final String MESINSERT= "INSERT INTO MESSAGES(nick,message,timeposted) VALUES (?,?,?)";
//    private static final String MESREC = "SELECT nick,message,timeposted FROM messages ORDER BY timeposted DESC LIMIT 10";
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
            delayStmt.execute("SET WRITE_DELAY FALSE"); // Always update data on disk
        } 
        finally {
            delayStmt.close();
        }

        connection.setAutoCommit(false);

        Statement sqlStmt = connection.createStatement();
        
        try {
            sqlStmt.execute("CREATE TABLE files(file_id BIGINT NOT NULL primary key "
                    + "GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1), name VARCHAR(4096) NOT NULL,"
                    + "path VARCHAR(4096) NOT NULL, hash VARCHAR(128) NOT NULL)");

            sqlStmt.execute("CREATE TABLE labels(file_id BIGINT NOT NULL, "
                    + "x_coor INT NOT NULL, y_coor INT NOT NULL, label INT NOT NULL, "
                    + "FOREIGN KEY(file_id) REFERENCES files (file_id))");
        } catch (SQLException e) {
            System.out.println("Info: Database tables \"labels\" already exists.");
        } finally {
            sqlStmt.close();
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
    
    public List<Long> getFileIDByPath(File chosenFile) throws SQLException{
        PreparedStatement getFileByPathStm = connection.prepareStatement(FILE_SELECT_BY_PATH);
        List<Long> ids = new ArrayList<Long>();
        try {
            getFileByPathStm.setString(1, chosenFile.getAbsolutePath());
            ResultSet rs = getFileByPathStm.executeQuery();
            try {
                while (rs.next()){
                    ids.add(rs.getLong("file_id"));
                }
            } finally {
                rs.close();
            }
        } finally {
            getFileByPathStm.close();
        }
        return ids;
    }
    
    public List<Long> getFileIDByHash(File chosenFile) throws SQLException{
        PreparedStatement getFileIDByHashStm = connection.prepareStatement(FILE_SELECT_BY_HASH);
        
        List<Long> ids = new ArrayList<Long>();
        try {
            getFileIDByHashStm.setString(1, getSHA256(chosenFile));
            ResultSet rs = getFileIDByHashStm.executeQuery();
            try {
                while (rs.next()){
                    ids.add(rs.getLong("file_id"));
                }
            } finally {
                rs.close();
            }
        } finally {
            getFileIDByHashStm.close();
        }
        return ids;
    }
    
    public void updateFile(long fileID, File file) throws SQLException{
        PreparedStatement updateMessage = connection.prepareStatement(UPDATE_FILE);
        updateMessage.setString(1, file.getName());
        updateMessage.setString(2, file.getAbsolutePath());
        updateMessage.setString(3, getSHA256(file));  
        updateMessage.setLong(4, fileID);
        synchronized(this){
            try {
                updateMessage.executeUpdate();
            } finally { // Notice use of finally clause here to finish statement
                updateMessage.close();
                connection.commit();
            }
            
        }
    
    }
    
    public long insertFile(File file) throws SQLException{
        PreparedStatement insertMessage = connection.prepareStatement(INSERT_FILE, Statement.RETURN_GENERATED_KEYS);
        insertMessage.setString(1, file.getName());
        insertMessage.setString(2, file.getAbsolutePath());
        insertMessage.setString(3, getSHA256(file));   
        synchronized(this){
            try {
                int affectedRowsCount = insertMessage.executeUpdate();
                if (affectedRowsCount == 0) {
                    throw new SQLException("Creating user failed, no rows affected.");
                }
                try (ResultSet generatedKeys = insertMessage.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return (generatedKeys.getLong(1));
                    }
                    else {
                        throw new SQLException("Creating user failed, no ID obtained.");
                    }
                }
            } finally { // Notice use of finally clause here to finish statement
                insertMessage.close();
                connection.commit();
            }
            
        }
    }
    
    public HashMap<Point, Integer> getPointLabelsByFileID(Long file_id) throws SQLException{
        PreparedStatement getPointLabelStm = connection.prepareStatement(POINT_LABEL_SELECT_BY_ID);
        
        HashMap<Point, Integer> plt = new HashMap<Point, Integer>();
        try {
            getPointLabelStm.setLong(1, file_id);
            ResultSet rs = getPointLabelStm.executeQuery();
            try {
                while (rs.next()){
                    plt.put(new Point(rs.getInt("x_coor"), rs.getInt("y_coor")), rs.getInt("label"));
                }
            } finally {
                rs.close();
            }
        } finally {
            getPointLabelStm.close();
        }
        return plt;
    }
    
    public boolean updatePointLabels(Long file_id, HashMap<Point, Integer> plt) throws SQLException{
        
        PreparedStatement deletePointLabelStm = connection.prepareStatement(DELETE_POINT_LABEL_BY_ID);
        deletePointLabelStm.setLong(1, file_id);
        
        // TODO Fix 1000 limit
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Point, Integer> entry : plt.entrySet()) {
            sb.append("(" + file_id + "," + (int) (entry.getKey().getX()) + ", " + (int) (entry.getKey().getY()) + ", " + entry.getValue() + "), ");
        }
        String values = sb.toString();
        
        PreparedStatement insertPointLabelStm = connection.prepareStatement(INSERT_POINT_LABEL.replace("???", values.substring(0, values.length() - 2)));
        
//        PreparedStatement updatePointLabelStm = connection.prepareStatement(MERGE_POINT_LABEL.replace("???", values.substring(0, values.length() - 2)));
        try {
            deletePointLabelStm.executeUpdate();
            insertPointLabelStm.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally { // Notice use of finally clause here to finish statement
            deletePointLabelStm.close();
            insertPointLabelStm.close();
        }
        connection.commit();
        
        return true;
    }
    
    public static String getSHA256(File file){
        byte[] buffer = new byte[8192];
        int count;
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            while ((count = bis.read(buffer)) > 0) {
                digest.update(buffer, 0, count);
            }
            byte[] hash = digest.digest();
            String encodedHash = bytesToHex(hash);
            System.out.println(encodedHash);
            return encodedHash;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
    
    public static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte byt : bytes) result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

    public void databaseExportAsCSV(String filePath) throws IOException, SQLException{
        FileWriter writer = new FileWriter(filePath);
        
        PreparedStatement getAllFileStm = connection.prepareStatement(FILE_SELECT_ALL);
        List<String> rowArray = new ArrayList<String>();
        try {
            ResultSet rs = getAllFileStm.executeQuery();
            try {
                while (rs.next()){
                    rowArray.add(String.valueOf(rs.getLong("file_id")));
                    rowArray.add(rs.getString("name"));
                    rowArray.add(rs.getString("path"));
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
        writer.append(CSV_DOC_SEPERATOR + "\n");
        
        PreparedStatement getAllPointLabelStm = connection.prepareStatement(POINT_LABEL_SELECT_ALL);

        try {
            ResultSet rs = getAllPointLabelStm.executeQuery();
            try {
                while (rs.next()){
                    rowArray.add(String.valueOf(rs.getLong("file_id")));
                    rowArray.add(String.valueOf(rs.getInt("x_coor")));
                    rowArray.add(String.valueOf(rs.getInt("y_coor")));
                    rowArray.add(String.valueOf(rs.getInt("label")));
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
        
        
        writer.flush();
        writer.close();
    }
    
    public boolean databaseImportFromCSV(String filePath) throws IOException, SQLException{
        Scanner scanner = new Scanner(new File(filePath));
        
        StringBuilder sb = new StringBuilder();
        while (scanner.hasNext()) {
            String rowText = scanner.nextLine();
            if (rowText.startsWith(CSV_DOC_SEPERATOR)){
                break;
            }
            List<String> row = CSVUtils.parseLine(rowText, CSV_SEPARATOR, CSV_QUOTE);
            
            sb.append("(" + Long.valueOf(row.get(0)) + ", '" + row.get(1) + "', '" + row.get(2) + "', '" + row.get(3) + "'), ");
            System.out.println("FILE [file_id= " + row.get(0) + ", name= " + row.get(1) + ", path=" + row.get(2) + ", hash=" + row.get(3) + "]");
        }
        
        String files = sb.toString();
        
        sb = new StringBuilder();
        while (scanner.hasNext()) {
            String rowText = scanner.nextLine();
            if (rowText.startsWith(CSV_DOC_SEPERATOR)){
                break;
            }
            List<String> row = CSVUtils.parseLine(rowText, CSV_SEPARATOR, CSV_QUOTE);
            
            sb.append("(" + Long.valueOf(row.get(0)) + "," + Integer.valueOf(row.get(1)) + ", " + Integer.valueOf(row.get(2)) + ", " + Integer.valueOf(row.get(3)) + "), ");
            System.out.println("LABEL [file_id= " + row.get(0) + ", x_coor= " + row.get(1) + ", y_coor=" + row.get(2) + ", label=" + row.get(3) + "]");
        }
        String labels = sb.toString();
        scanner.close();
        
        PreparedStatement insertFileStm = connection.prepareStatement(INSERT_FILE_WITH_ID.replace("???", files.substring(0, files.length() - 2)));
        PreparedStatement insertPointLabelStm = connection.prepareStatement(INSERT_POINT_LABEL.replace("???", labels.substring(0, labels.length() - 2)));
        
        try {
            insertFileStm.executeUpdate();
            insertPointLabelStm.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally { // Notice use of finally clause here to finish statement
            insertFileStm.close();
            insertPointLabelStm.close();
        }
        connection.commit();
        return true;
    }
    
    public void clearAllData(){
        Statement deleteFileStm;
        Statement deleteLabelStm;
        try {
            deleteLabelStm = connection.createStatement();
            deleteLabelStm.executeQuery(DELETE_POINT_LABEL_ALL);
            
            deleteFileStm = connection.createStatement();
            deleteFileStm.executeQuery(DELETE_FILE_ALL);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
}
