package dao;

import db.DatabaseConnection;
import model.Admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDAO {
    public Admin authenticate(String username, String password) {
        String sql = "SELECT id, username FROM Admins WHERE username = ? AND password = ?";
        // Connection is now obtained and closed within this try-with-resources
        try (Connection conn = DatabaseConnection.getConnection(); // This might throw SQLException
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Admin(rs.getInt("id"), rs.getString("username"));
            }
        } catch (SQLException e) { // Catches SQLException from getConnection() or DB operations
            System.err.println("Error during Admin authentication: " + e.getMessage());
            e.printStackTrace(); // Or handle more gracefully
        }
        return null;
    }
}