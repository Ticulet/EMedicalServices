package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/ServiciiMedicaleDB"; // YOUR DB NAME
    private static final String USER = "root";     // YOUR MYSQL USER
    private static final String PASSWORD = "1234"; // YOUR MYSQL PASSWORD

    public static Connection getConnection() throws SQLException {
        System.out.println("DatabaseConnection: Attempting to connect with URL: " + URL); // <<< ADD THIS LINE
        System.out.println("DatabaseConnection: User: " + USER); // <<< ADD THIS LINE
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        if (conn != null && !conn.isClosed()) {
            System.out.println("DatabaseConnection: Connection successful. Catalog: " + conn.getCatalog()); // <<< ADD THIS LINE
        } else {
            System.out.println("DatabaseConnection: Connection failed or is closed immediately."); // <<< ADD THIS LINE
        }
        return conn;
    }

    public static void main(String[] args) {
        Connection conn = null;
        try {
            conn = getConnection();
            if (conn != null) {
                System.out.println("Successfully connected to the database (from main test)!");
                System.out.println("Catalog from main test connection: " + conn.getCatalog()); // <<< ADD THIS LINE
                try (java.sql.Statement stmt = conn.createStatement()) {
                    stmt.executeQuery("SELECT 1 FROM Admins LIMIT 1");
                    System.out.println("Database selected and Admins table accessible from main test.");
                } catch (SQLException e) {
                    System.err.println("Error executing test query in main: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("Failed to connect to the database (from main test).");
            }
        } catch (SQLException e) {
            System.err.println("Exception in main during DB connection: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                    System.out.println("Connection closed (from main test).");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}