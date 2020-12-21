package me.polymarsdev.teamsnake.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class Database {

    public enum DBType {MySQL, SQLite}

    /**
     * SQLite Data
     * Set this data if you use DBType#SQLite
     * <p>
     * field filePath - This can either be a relative or absolute path.
     * ex: teamsnake.db
     * or: C:/sqlite/db/teamsnake.db
     */
    private final String filePath = "teamsnake.db";

    /**
     * MySQL Data
     * Set this data if you use DBType#MySQL
     */
    private final String mysql_config = "teamsnake_mysql.conf";

    private Connection con = null;

    public Database(DBType dbType) {
        try {
            if (dbType == DBType.MySQL) {
                Properties prop = new Properties();
                File file = new File(mysql_config);
                boolean newFile = false;
                if (!file.exists()) {
                    boolean create = file.createNewFile();
                    if (!create) System.out.println("[ERROR] Could not create mysql config file");
                    else newFile = true;
                }
                InputStream is;
                try {
                    is = new FileInputStream(mysql_config);
                    prop.load(is);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (newFile) {
                    prop.setProperty("mysql.hostname", "127.0.0.1");
                    prop.setProperty("mysql.port", "3306");
                    prop.setProperty("mysql.database", "teamsnake");
                    prop.setProperty("mysql.username", "teamsnake");
                    prop.setProperty("mysql.password", "S€cUr€_P4$sW0rD");
                    prop.store(new FileOutputStream(mysql_config), "MySQL configuration");
                }
                Class.forName("com.mysql.cj.jdbc.Driver");
                final String mysql_hostname = prop.getProperty("mysql.hostname");
                final String mysql_port = prop.getProperty("mysql.port");
                final String mysql_database = prop.getProperty("mysql.database");
                final String mysql_username = prop.getProperty("mysql.username");
                final String mysql_password = prop.getProperty("mysql.password");
                con = DriverManager.getConnection(
                        "jdbc:mysql://" + mysql_hostname + ":" + mysql_port + "/" + mysql_database
                                + "?autoReconnect=true&useTimezone=true&serverTimezone=GMT", mysql_username,
                        mysql_password);
                System.out.println("[INFO] Successfully initialized database connection.");
            } else if (dbType == DBType.SQLite) {
                File sqliteFile = new File(filePath);
                if (!sqliteFile.exists()) {
                    System.out.println("[INFO] SQLite file \"" + filePath + "\" not found, creating file...");
                    boolean create = sqliteFile.createNewFile();
                    if (!create) System.out.println("[ERROR] Could not create SQLite file at " + filePath);
                }
                con = DriverManager.getConnection("jdbc:sqlite:" + filePath);
                System.out.println("[INFO] Successfully initialized database connection.");
            }
        } catch (Exception ex) {
            System.out.println("[ERROR] Error at creating database connection: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            con.clearWarnings();
            con.close();
            con = null;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public Connection getCon() {
        return con;
    }

    public ResultSet query(String sql, Object... preparedParameters) {
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            int id = 1;
            for (Object preparedParameter : preparedParameters) {
                ps.setObject(id, preparedParameter);
                id++;
            }
            return ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ResultSet query(String sql) {
        try {
            return con.prepareStatement(sql).executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    public void update(String sql, Object... preparedParameters) {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int id = 1;
            for (Object preparedParameter : preparedParameters) {
                ps.setObject(id, preparedParameter);
                id++;
            }
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void update(String sql) {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public boolean isConnected() {
        return con != null;
    }
}