package com.parkwise.dao;

import com.parkwise.db.DatabaseConnection;
import java.sql.*;

/**
 * Data Access Object for Admin authentication operations.
 */
public class AdminDAO {

    /**
     * Authenticate admin user
     */
    public boolean authenticate(String username, String password) {
        String sql = "SELECT * FROM admin WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get admin full name
     */
    public String getAdminName(String username) {
        String sql = "SELECT full_name FROM admin WHERE username = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("full_name");
            }
        } catch (SQLException e) {
            System.err.println("Error getting admin name: " + e.getMessage());
        }
        return "Admin";
    }

    /**
     * Update admin password
     */
    public boolean updatePassword(String username, String oldPassword, String newPassword) {
        if (!authenticate(username, oldPassword)) {
            return false;
        }
        String sql = "UPDATE admin SET password = ? WHERE username = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            return false;
        }
    }
}
