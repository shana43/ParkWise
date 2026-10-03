package com.parkwise.dao;

import com.parkwise.db.DatabaseConnection;
import com.parkwise.model.ParkingRecord;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Parking Record operations.
 */
public class ParkingRecordDAO {

    /**
     * Create a new parking record
     */
    public boolean createRecord(ParkingRecord record) {
        String sql = "INSERT INTO parking_records (ticket_id, vehicle_number, owner_name, phone, vehicle_type, " +
                "slot_id, slot_number, entry_time, payment_status, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'Pending', 'Parked')";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, record.getTicketId());
            pstmt.setString(2, record.getVehicleNumber());
            pstmt.setString(3, record.getOwnerName());
            pstmt.setString(4, record.getPhone());
            pstmt.setString(5, record.getVehicleType());
            pstmt.setInt(6, record.getSlotId());
            pstmt.setString(7, record.getSlotNumber());
            pstmt.setTimestamp(8, Timestamp.valueOf(record.getEntryTime()));
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = pstmt.getGeneratedKeys();
                if (keys.next()) {
                    record.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating parking record: " + e.getMessage());
        }
        return false;
    }

    /**
     * Update record on vehicle exit
     */
    public boolean exitVehicle(String ticketId, LocalDateTime exitTime, int durationMinutes, double amount) {
        String sql = "UPDATE parking_records SET exit_time = ?, duration_minutes = ?, amount = ?, " +
                "payment_status = 'Paid', status = 'Exited' WHERE ticket_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(exitTime));
            pstmt.setInt(2, durationMinutes);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, ticketId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error exiting vehicle: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get active parking record by ticket ID
     */
    public ParkingRecord getActiveByTicketId(String ticketId) {
        String sql = "SELECT * FROM parking_records WHERE ticket_id = ? AND status = 'Parked'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ticketId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRecord(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting record by ticket: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get active parking record by vehicle number
     */
    public ParkingRecord getActiveByVehicleNumber(String vehicleNumber) {
        String sql = "SELECT * FROM parking_records WHERE vehicle_number = ? AND status = 'Parked'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, vehicleNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRecord(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting record by vehicle: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get record by ticket ID (any status)
     */
    public ParkingRecord getByTicketId(String ticketId) {
        String sql = "SELECT * FROM parking_records WHERE ticket_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ticketId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRecord(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting record: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all currently parked vehicles
     */
    public List<ParkingRecord> getParkedVehicles() {
        List<ParkingRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM parking_records WHERE status = 'Parked' ORDER BY entry_time DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                records.add(mapRecord(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting parked vehicles: " + e.getMessage());
        }
        return records;
    }

    /**
     * Get all records (history)
     */
    public List<ParkingRecord> getAllRecords() {
        List<ParkingRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM parking_records ORDER BY entry_time DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                records.add(mapRecord(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all records: " + e.getMessage());
        }
        return records;
    }

    /**
     * Search records by keyword
     */
    public List<ParkingRecord> searchRecords(String keyword) {
        List<ParkingRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM parking_records WHERE ticket_id LIKE ? OR vehicle_number LIKE ? OR owner_name LIKE ? ORDER BY entry_time DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                records.add(mapRecord(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching records: " + e.getMessage());
        }
        return records;
    }

    /**
     * Get records by date range
     */
    public List<ParkingRecord> getRecordsByDateRange(LocalDateTime from, LocalDateTime to) {
        List<ParkingRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM parking_records WHERE entry_time BETWEEN ? AND ? ORDER BY entry_time DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(from));
            pstmt.setTimestamp(2, Timestamp.valueOf(to));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                records.add(mapRecord(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting records by date: " + e.getMessage());
        }
        return records;
    }

    /**
     * Get records by date range and search keyword
     */
    public List<ParkingRecord> getRecordsByDateRangeAndKeyword(LocalDateTime from, LocalDateTime to, String keyword) {
        List<ParkingRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM parking_records WHERE entry_time BETWEEN ? AND ? " +
                "AND (ticket_id LIKE ? OR vehicle_number LIKE ? OR owner_name LIKE ?) ORDER BY entry_time DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(from));
            pstmt.setTimestamp(2, Timestamp.valueOf(to));
            String pattern = "%" + keyword + "%";
            pstmt.setString(3, pattern);
            pstmt.setString(4, pattern);
            pstmt.setString(5, pattern);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                records.add(mapRecord(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching records with date: " + e.getMessage());
        }
        return records;
    }

    /**
     * Get count of currently parked vehicles
     */
    public int getParkedVehicleCount() {
        String sql = "SELECT COUNT(*) as total FROM parking_records WHERE status = 'Parked'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            System.err.println("Error counting parked vehicles: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get today's total revenue
     */
    public double getTodayRevenue() {
        String sql = "SELECT COALESCE(SUM(amount), 0) as revenue FROM parking_records " +
                "WHERE status = 'Exited' AND CAST(exit_time AS DATE) = CURRENT_DATE";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble("revenue");
        } catch (SQLException e) {
            System.err.println("Error getting today's revenue: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Get today's entry count
     */
    public int getTodayEntries() {
        String sql = "SELECT COUNT(*) as total FROM parking_records WHERE CAST(entry_time AS DATE) = CURRENT_DATE";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            System.err.println("Error getting today's entries: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get today's exit count
     */
    public int getTodayExits() {
        String sql = "SELECT COUNT(*) as total FROM parking_records WHERE status = 'Exited' AND CAST(exit_time AS DATE) = CURRENT_DATE";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            System.err.println("Error getting today's exits: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get total revenue
     */
    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(amount), 0) as revenue FROM parking_records WHERE status = 'Exited'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble("revenue");
        } catch (SQLException e) {
            System.err.println("Error getting total revenue: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Get revenue by date range
     */
    public double getRevenueByDateRange(LocalDateTime from, LocalDateTime to) {
        String sql = "SELECT COALESCE(SUM(amount), 0) as revenue FROM parking_records " +
                "WHERE status = 'Exited' AND exit_time BETWEEN ? AND ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(from));
            pstmt.setTimestamp(2, Timestamp.valueOf(to));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getDouble("revenue");
        } catch (SQLException e) {
            System.err.println("Error getting revenue by date: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Get vehicle count by type for a date range
     */
    public int getVehicleCountByType(String type, LocalDateTime from, LocalDateTime to) {
        String sql = "SELECT COUNT(*) as total FROM parking_records WHERE vehicle_type = ? AND entry_time BETWEEN ? AND ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type);
            pstmt.setTimestamp(2, Timestamp.valueOf(from));
            pstmt.setTimestamp(3, Timestamp.valueOf(to));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            System.err.println("Error getting vehicle count by type: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get revenue by vehicle type for a date range
     */
    public double getRevenueByType(String type, LocalDateTime from, LocalDateTime to) {
        String sql = "SELECT COALESCE(SUM(amount), 0) as revenue FROM parking_records " +
                "WHERE vehicle_type = ? AND status = 'Exited' AND exit_time BETWEEN ? AND ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type);
            pstmt.setTimestamp(2, Timestamp.valueOf(from));
            pstmt.setTimestamp(3, Timestamp.valueOf(to));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getDouble("revenue");
        } catch (SQLException e) {
            System.err.println("Error getting revenue by type: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Generate next ticket ID
     */
    public String generateTicketId() {
        String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "TKT-" + dateStr + "-";
        String sql = "SELECT ticket_id FROM parking_records WHERE ticket_id LIKE ? ORDER BY ticket_id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, prefix + "%");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String lastTicket = rs.getString("ticket_id");
                int lastNum = Integer.parseInt(lastTicket.substring(lastTicket.lastIndexOf('-') + 1));
                return prefix + String.format("%03d", lastNum + 1);
            }
        } catch (SQLException e) {
            System.err.println("Error generating ticket ID: " + e.getMessage());
        }
        return prefix + "001";
    }

    /**
     * Check if vehicle is currently parked
     */
    public boolean isVehicleParked(String vehicleNumber) {
        String sql = "SELECT COUNT(*) as total FROM parking_records WHERE vehicle_number = ? AND status = 'Parked'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, vehicleNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("total") > 0;
        } catch (SQLException e) {
            System.err.println("Error checking parked vehicle: " + e.getMessage());
        }
        return false;
    }

    /**
     * Map ResultSet to ParkingRecord object
     */
    private ParkingRecord mapRecord(ResultSet rs) throws SQLException {
        ParkingRecord record = new ParkingRecord();
        record.setId(rs.getInt("id"));
        record.setTicketId(rs.getString("ticket_id"));
        record.setVehicleNumber(rs.getString("vehicle_number"));
        record.setOwnerName(rs.getString("owner_name"));
        record.setPhone(rs.getString("phone"));
        record.setVehicleType(rs.getString("vehicle_type"));
        record.setSlotId(rs.getInt("slot_id"));
        record.setSlotNumber(rs.getString("slot_number"));
        record.setEntryTime(rs.getTimestamp("entry_time").toLocalDateTime());
        Timestamp exitTs = rs.getTimestamp("exit_time");
        if (exitTs != null) {
            record.setExitTime(exitTs.toLocalDateTime());
        }
        record.setDurationMinutes(rs.getObject("duration_minutes") != null ? rs.getInt("duration_minutes") : null);
        record.setAmount(rs.getObject("amount") != null ? rs.getDouble("amount") : null);
        record.setPaymentStatus(rs.getString("payment_status"));
        record.setStatus(rs.getString("status"));
        record.setCreatedAt(rs.getTimestamp("created_at"));
        return record;
    }
}
