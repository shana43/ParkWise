package com.parkwise.dao;

import com.parkwise.db.DatabaseConnection;
import com.parkwise.model.Payment;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Payment operations.
 */
public class PaymentDAO {

    /**
     * Create a new payment record
     */
    public boolean createPayment(Payment payment) {
        String sql = "INSERT INTO payments (ticket_id, vehicle_number, vehicle_type, amount, duration_minutes, " +
                "payment_method, payment_time) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, payment.getTicketId());
            pstmt.setString(2, payment.getVehicleNumber());
            pstmt.setString(3, payment.getVehicleType());
            pstmt.setDouble(4, payment.getAmount());
            pstmt.setInt(5, payment.getDurationMinutes());
            pstmt.setString(6, payment.getPaymentMethod());
            pstmt.setTimestamp(7, Timestamp.valueOf(payment.getPaymentTime()));
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = pstmt.getGeneratedKeys();
                if (keys.next()) {
                    payment.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating payment: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get payment by ticket ID
     */
    public Payment getByTicketId(String ticketId) {
        String sql = "SELECT * FROM payments WHERE ticket_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ticketId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapPayment(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting payment: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all payments
     */
    public List<Payment> getAllPayments() {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments ORDER BY payment_time DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                payments.add(mapPayment(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting payments: " + e.getMessage());
        }
        return payments;
    }

    /**
     * Get payments by date range
     */
    public List<Payment> getPaymentsByDateRange(LocalDateTime from, LocalDateTime to) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE payment_time BETWEEN ? AND ? ORDER BY payment_time DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(from));
            pstmt.setTimestamp(2, Timestamp.valueOf(to));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                payments.add(mapPayment(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting payments by date: " + e.getMessage());
        }
        return payments;
    }

    /**
     * Get rate per hour for vehicle type
     */
    public double getRatePerHour(String vehicleType) {
        String sql = "SELECT rate_per_hour FROM rate_config WHERE vehicle_type = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, vehicleType);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("rate_per_hour");
            }
        } catch (SQLException e) {
            System.err.println("Error getting rate: " + e.getMessage());
        }
        // Default rates
        return "Car".equals(vehicleType) ? 20.0 : 10.0;
    }

    /**
     * Get minimum charge for vehicle type
     */
    public double getMinimumCharge(String vehicleType) {
        String sql = "SELECT minimum_charge FROM rate_config WHERE vehicle_type = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, vehicleType);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("minimum_charge");
            }
        } catch (SQLException e) {
            System.err.println("Error getting minimum charge: " + e.getMessage());
        }
        return "Car".equals(vehicleType) ? 20.0 : 10.0;
    }

    /**
     * Map ResultSet to Payment object
     */
    private Payment mapPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setId(rs.getInt("id"));
        payment.setTicketId(rs.getString("ticket_id"));
        payment.setVehicleNumber(rs.getString("vehicle_number"));
        payment.setVehicleType(rs.getString("vehicle_type"));
        payment.setAmount(rs.getDouble("amount"));
        payment.setDurationMinutes(rs.getInt("duration_minutes"));
        payment.setPaymentMethod(rs.getString("payment_method"));
        payment.setPaymentTime(rs.getTimestamp("payment_time").toLocalDateTime());
        payment.setCreatedAt(rs.getTimestamp("created_at"));
        return payment;
    }
}
