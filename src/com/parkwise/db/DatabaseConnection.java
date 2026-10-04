package com.parkwise.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Properties;

/**
 * Database connection manager using Singleton pattern.
 * Reads credentials from db.properties (never hardcoded).
 */
public class DatabaseConnection {

    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;

    private static DatabaseConnection instance;

    static {
        try {
            Properties props = new Properties();
            // Look for db.properties in classpath root (copied from project root)
            InputStream is = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties");
            if (is == null) {
                // Fallback: read from working directory
                is = new java.io.FileInputStream("db.properties");
            }
            props.load(is);
            is.close();
            URL = props.getProperty("db.url");
            USERNAME = props.getProperty("db.username");
            PASSWORD = props.getProperty("db.password", "");
        } catch (Exception e) {
            System.err.println("[DB] Could not load db.properties: " + e.getMessage());
            URL = "jdbc:h2:./parkwise;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1";
            USERNAME = "sa";
            PASSWORD = "";
        }

        try {
            if (URL.startsWith("jdbc:h2")) {
                Class.forName("org.h2.Driver");
            } else {
                Class.forName("com.mysql.cj.jdbc.Driver");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] JDBC Driver not found: " + e.getMessage());
        }
    }

    private DatabaseConnection() {}

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static boolean testConnection() {
        try (Connection conn = getInstance().getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("[DB] Connection failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean isH2() {
        return URL != null && URL.startsWith("jdbc:h2");
    }

    public static void initializeDatabase() {
        try (Connection conn = getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS admin (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50) NOT NULL UNIQUE, " +
                "password VARCHAR(255) NOT NULL, full_name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(100), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS parking_slots (" +
                "slot_id INT AUTO_INCREMENT PRIMARY KEY, slot_number VARCHAR(10) NOT NULL UNIQUE, " +
                "slot_type VARCHAR(10) NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'Available', " +
                "floor_number INT DEFAULT 1, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS vehicles (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, vehicle_number VARCHAR(20) NOT NULL, " +
                "owner_name VARCHAR(100) NOT NULL, phone VARCHAR(15) NOT NULL, " +
                "vehicle_type VARCHAR(10) NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS parking_records (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, ticket_id VARCHAR(20) NOT NULL UNIQUE, " +
                "vehicle_number VARCHAR(20) NOT NULL, owner_name VARCHAR(100) NOT NULL, " +
                "phone VARCHAR(15) NOT NULL, vehicle_type VARCHAR(10) NOT NULL, " +
                "slot_id INT NOT NULL, slot_number VARCHAR(10) NOT NULL, " +
                "entry_time DATETIME NOT NULL, exit_time DATETIME DEFAULT NULL, " +
                "duration_minutes INT DEFAULT NULL, amount DECIMAL(10,2) DEFAULT NULL, " +
                "payment_status VARCHAR(20) DEFAULT 'Pending', status VARCHAR(20) DEFAULT 'Parked', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS payments (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, ticket_id VARCHAR(20) NOT NULL, " +
                "vehicle_number VARCHAR(20) NOT NULL, vehicle_type VARCHAR(10) NOT NULL, " +
                "amount DECIMAL(10,2) NOT NULL, duration_minutes INT NOT NULL, " +
                "payment_method VARCHAR(20) DEFAULT 'Cash', payment_time DATETIME NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS rate_config (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, vehicle_type VARCHAR(10) NOT NULL UNIQUE, " +
                "rate_per_hour DECIMAL(10,2) NOT NULL, minimum_charge DECIMAL(10,2) NOT NULL DEFAULT 0.00, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // Seed default admin (only if not exists)
            seedIfNotExists(conn, "admin", "username", "'admin'",
                "INSERT INTO admin (username, password, full_name, email) " +
                "VALUES ('admin', 'admin123', 'System Administrator', 'admin@parkwise.com')");

            // Seed rate config
            seedIfNotExists(conn, "rate_config", "vehicle_type", "'Bike'",
                "INSERT INTO rate_config (vehicle_type, rate_per_hour, minimum_charge) VALUES ('Bike', 10.00, 10.00)");
            seedIfNotExists(conn, "rate_config", "vehicle_type", "'Car'",
                "INSERT INTO rate_config (vehicle_type, rate_per_hour, minimum_charge) VALUES ('Car', 20.00, 20.00)");

            // Seed parking slots (Bike B01-B30, Car C01-C20)
            for (int i = 1; i <= 30; i++) {
                String sn = "B" + String.format("%02d", i);
                int floor = i <= 15 ? 1 : 2;
                seedIfNotExists(conn, "parking_slots", "slot_number", "'" + sn + "'",
                    "INSERT INTO parking_slots (slot_number, slot_type, floor_number) VALUES ('" + sn + "', 'Bike', " + floor + ")");
            }
            for (int i = 1; i <= 20; i++) {
                String sn = "C" + String.format("%02d", i);
                int floor = i <= 10 ? 1 : 2;
                seedIfNotExists(conn, "parking_slots", "slot_number", "'" + sn + "'",
                    "INSERT INTO parking_slots (slot_number, slot_type, floor_number) VALUES ('" + sn + "', 'Car', " + floor + ")");
            }

            System.out.println("[DB] Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("[DB] Init error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Insert a row only if it doesn't already exist (works for both H2 and MySQL).
     */
    private static void seedIfNotExists(Connection conn, String table, String keyCol, String keyVal, String insertSql) {
        try (Statement check = conn.createStatement()) {
            ResultSet rs = check.executeQuery("SELECT COUNT(*) FROM " + table + " WHERE " + keyCol + " = " + keyVal);
            rs.next();
            if (rs.getInt(1) == 0) {
                check.executeUpdate(insertSql);
            }
            rs.close();
        } catch (SQLException e) {
            // Ignore duplicate key errors
            if (!e.getMessage().contains("Unique index") && !e.getMessage().contains("Duplicate")) {
                System.err.println("[DB] Seed error for " + table + ": " + e.getMessage());
            }
        }
    }
}
