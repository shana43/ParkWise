package com.parkwise.dao;

import com.parkwise.db.DatabaseConnection;
import com.parkwise.model.Vehicle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Vehicle CRUD operations.
 */
public class VehicleDAO {

    /**
     * Add a new vehicle
     */
    public boolean addVehicle(Vehicle vehicle) {
        String sql = "INSERT INTO vehicles (vehicle_number, owner_name, phone, vehicle_type) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, vehicle.getVehicleNumber());
            pstmt.setString(2, vehicle.getOwnerName());
            pstmt.setString(3, vehicle.getPhone());
            pstmt.setString(4, vehicle.getVehicleType());
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = pstmt.getGeneratedKeys();
                if (keys.next()) {
                    vehicle.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding vehicle: " + e.getMessage());
        }
        return false;
    }

    /**
     * Update an existing vehicle
     */
    public boolean updateVehicle(Vehicle vehicle) {
        String sql = "UPDATE vehicles SET vehicle_number = ?, owner_name = ?, phone = ?, vehicle_type = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, vehicle.getVehicleNumber());
            pstmt.setString(2, vehicle.getOwnerName());
            pstmt.setString(3, vehicle.getPhone());
            pstmt.setString(4, vehicle.getVehicleType());
            pstmt.setInt(5, vehicle.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating vehicle: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete a vehicle by ID
     */
    public boolean deleteVehicle(int id) {
        String sql = "DELETE FROM vehicles WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting vehicle: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get all vehicles
     */
    public List<Vehicle> getAllVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT * FROM vehicles ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                vehicles.add(mapVehicle(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting vehicles: " + e.getMessage());
        }
        return vehicles;
    }

    /**
     * Search vehicles by keyword (vehicle number, owner name, or phone)
     */
    public List<Vehicle> searchVehicles(String keyword) {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT * FROM vehicles WHERE vehicle_number LIKE ? OR owner_name LIKE ? OR phone LIKE ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                vehicles.add(mapVehicle(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching vehicles: " + e.getMessage());
        }
        return vehicles;
    }

    /**
     * Get vehicle by vehicle number
     */
    public Vehicle getByVehicleNumber(String vehicleNumber) {
        String sql = "SELECT * FROM vehicles WHERE vehicle_number = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, vehicleNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapVehicle(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting vehicle: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get vehicle by ID
     */
    public Vehicle getById(int id) {
        String sql = "SELECT * FROM vehicles WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapVehicle(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting vehicle: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get total vehicle count
     */
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) as total FROM vehicles";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error counting vehicles: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Map ResultSet to Vehicle object
     */
    private Vehicle mapVehicle(ResultSet rs) throws SQLException {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(rs.getInt("id"));
        vehicle.setVehicleNumber(rs.getString("vehicle_number"));
        vehicle.setOwnerName(rs.getString("owner_name"));
        vehicle.setPhone(rs.getString("phone"));
        vehicle.setVehicleType(rs.getString("vehicle_type"));
        vehicle.setCreatedAt(rs.getTimestamp("created_at"));
        vehicle.setUpdatedAt(rs.getTimestamp("updated_at"));
        return vehicle;
    }
}
