package com.parkwise.dao;

import com.parkwise.db.DatabaseConnection;
import com.parkwise.model.ParkingSlot;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Parking Slot operations.
 */
public class ParkingSlotDAO {

    /**
     * Get all parking slots
     */
    public List<ParkingSlot> getAllSlots() {
        List<ParkingSlot> slots = new ArrayList<>();
        String sql = "SELECT * FROM parking_slots ORDER BY slot_type, slot_number";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                slots.add(mapSlot(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting slots: " + e.getMessage());
        }
        return slots;
    }

    /**
     * Get slots by type
     */
    public List<ParkingSlot> getSlotsByType(String type) {
        List<ParkingSlot> slots = new ArrayList<>();
        String sql = "SELECT * FROM parking_slots WHERE slot_type = ? ORDER BY slot_number";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                slots.add(mapSlot(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting slots by type: " + e.getMessage());
        }
        return slots;
    }

    /**
     * Get first available slot by vehicle type
     */
    public ParkingSlot getFirstAvailableSlot(String vehicleType) {
        String sql = "SELECT * FROM parking_slots WHERE slot_type = ? AND status = 'Available' ORDER BY slot_number LIMIT 1";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, vehicleType);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapSlot(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding available slot: " + e.getMessage());
        }
        return null;
    }

    /**
     * Update slot status
     */
    public boolean updateSlotStatus(int slotId, String status) {
        String sql = "UPDATE parking_slots SET status = ? WHERE slot_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, slotId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating slot status: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get total slots count
     */
    public int getTotalSlots() {
        String sql = "SELECT COUNT(*) as total FROM parking_slots";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            System.err.println("Error counting total slots: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get available slots count
     */
    public int getAvailableSlots() {
        String sql = "SELECT COUNT(*) as total FROM parking_slots WHERE status = 'Available'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            System.err.println("Error counting available slots: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get occupied slots count
     */
    public int getOccupiedSlots() {
        String sql = "SELECT COUNT(*) as total FROM parking_slots WHERE status = 'Occupied'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            System.err.println("Error counting occupied slots: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get available slots count by type
     */
    public int getAvailableSlotsByType(String type) {
        String sql = "SELECT COUNT(*) as total FROM parking_slots WHERE slot_type = ? AND status = 'Available'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            System.err.println("Error counting available slots by type: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get total slots count by type
     */
    public int getTotalSlotsByType(String type) {
        String sql = "SELECT COUNT(*) as total FROM parking_slots WHERE slot_type = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            System.err.println("Error counting total slots by type: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get slot by ID
     */
    public ParkingSlot getSlotById(int slotId) {
        String sql = "SELECT * FROM parking_slots WHERE slot_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, slotId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapSlot(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting slot: " + e.getMessage());
        }
        return null;
    }

    /**
     * Map ResultSet to ParkingSlot object
     */
    private ParkingSlot mapSlot(ResultSet rs) throws SQLException {
        ParkingSlot slot = new ParkingSlot();
        slot.setSlotId(rs.getInt("slot_id"));
        slot.setSlotNumber(rs.getString("slot_number"));
        slot.setSlotType(rs.getString("slot_type"));
        slot.setStatus(rs.getString("status"));
        slot.setFloorNumber(rs.getInt("floor_number"));
        slot.setUpdatedAt(rs.getTimestamp("updated_at"));
        return slot;
    }
}
