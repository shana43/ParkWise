package com.parkwise.service;

import com.parkwise.dao.*;
import com.parkwise.model.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service class that encapsulates business logic for parking operations.
 * Coordinates between DAOs and provides transaction-like operations.
 */
public class ParkingService {

    private final ParkingRecordDAO recordDAO;
    private final ParkingSlotDAO slotDAO;
    private final VehicleDAO vehicleDAO;
    private final PaymentDAO paymentDAO;
    private final AdminDAO adminDAO;

    public ParkingService() {
        this.recordDAO = new ParkingRecordDAO();
        this.slotDAO = new ParkingSlotDAO();
        this.vehicleDAO = new VehicleDAO();
        this.paymentDAO = new PaymentDAO();
        this.adminDAO = new AdminDAO();
    }

    // ==================== Authentication ====================

    public boolean authenticateAdmin(String username, String password) {
        return adminDAO.authenticate(username, password);
    }

    public String getAdminName(String username) {
        return adminDAO.getAdminName(username);
    }

    // ==================== Vehicle Entry ====================

    /**
     * Park a vehicle: allocates slot, creates record, saves vehicle info
     * @return ParkingRecord with ticket info, or null on failure
     */
    public ParkingRecord parkVehicle(String vehicleNumber, String ownerName, String phone, String vehicleType) {
        // Check if vehicle is already parked
        if (recordDAO.isVehicleParked(vehicleNumber)) {
            return null;
        }

        // Find available slot
        ParkingSlot slot = slotDAO.getFirstAvailableSlot(vehicleType);
        if (slot == null) {
            return null; // No available slot
        }

        // Generate ticket ID
        String ticketId = recordDAO.generateTicketId();

        // Create parking record
        ParkingRecord record = new ParkingRecord();
        record.setTicketId(ticketId);
        record.setVehicleNumber(vehicleNumber);
        record.setOwnerName(ownerName);
        record.setPhone(phone);
        record.setVehicleType(vehicleType);
        record.setSlotId(slot.getSlotId());
        record.setSlotNumber(slot.getSlotNumber());
        record.setEntryTime(LocalDateTime.now());

        if (recordDAO.createRecord(record)) {
            // Update slot status to Occupied
            slotDAO.updateSlotStatus(slot.getSlotId(), "Occupied");

            // Save/update vehicle info
            Vehicle existingVehicle = vehicleDAO.getByVehicleNumber(vehicleNumber);
            if (existingVehicle == null) {
                Vehicle vehicle = new Vehicle(vehicleNumber, ownerName, phone, vehicleType);
                vehicleDAO.addVehicle(vehicle);
            }

            return record;
        }
        return null;
    }

    // ==================== Vehicle Exit ====================

    /**
     * Exit a vehicle by ticket ID or vehicle number
     * @return ParkingRecord with exit details, or null on failure
     */
    public ParkingRecord exitVehicle(String identifier, String paymentMethod) {
        // Try finding by ticket ID first, then by vehicle number
        ParkingRecord record = recordDAO.getActiveByTicketId(identifier);
        if (record == null) {
            record = recordDAO.getActiveByVehicleNumber(identifier);
        }
        if (record == null) {
            return null;
        }

        LocalDateTime exitTime = LocalDateTime.now();
        long durationMinutes = ChronoUnit.MINUTES.between(record.getEntryTime(), exitTime);
        if (durationMinutes < 1) durationMinutes = 1;

        // Calculate fee
        double rate = paymentDAO.getRatePerHour(record.getVehicleType());
        double minCharge = paymentDAO.getMinimumCharge(record.getVehicleType());
        double hours = Math.ceil(durationMinutes / 60.0);
        double amount = hours * rate;
        if (amount < minCharge) amount = minCharge;

        // Update parking record
        if (recordDAO.exitVehicle(record.getTicketId(), exitTime, (int) durationMinutes, amount)) {
            // Free up the slot
            slotDAO.updateSlotStatus(record.getSlotId(), "Available");

            // Create payment record
            Payment payment = new Payment(
                    record.getTicketId(),
                    record.getVehicleNumber(),
                    record.getVehicleType(),
                    amount,
                    (int) durationMinutes,
                    paymentMethod,
                    exitTime
            );
            paymentDAO.createPayment(payment);

            // Update record with exit info
            record.setExitTime(exitTime);
            record.setDurationMinutes((int) durationMinutes);
            record.setAmount(amount);
            record.setPaymentStatus("Paid");
            record.setStatus("Exited");

            return record;
        }
        return null;
    }

    /**
     * Calculate fee for a parked vehicle (preview before exit)
     */
    public double calculateFee(ParkingRecord record) {
        LocalDateTime now = LocalDateTime.now();
        long durationMinutes = ChronoUnit.MINUTES.between(record.getEntryTime(), now);
        if (durationMinutes < 1) durationMinutes = 1;

        double rate = paymentDAO.getRatePerHour(record.getVehicleType());
        double minCharge = paymentDAO.getMinimumCharge(record.getVehicleType());
        double hours = Math.ceil(durationMinutes / 60.0);
        double amount = hours * rate;
        return Math.max(amount, minCharge);
    }

    // ==================== Dashboard Stats ====================

    public int getTotalSlots() { return slotDAO.getTotalSlots(); }
    public int getAvailableSlots() { return slotDAO.getAvailableSlots(); }
    public int getOccupiedSlots() { return slotDAO.getOccupiedSlots(); }
    public int getParkedVehicleCount() { return recordDAO.getParkedVehicleCount(); }
    public double getTodayRevenue() { return recordDAO.getTodayRevenue(); }
    public int getTodayEntries() { return recordDAO.getTodayEntries(); }
    public int getTodayExits() { return recordDAO.getTodayExits(); }
    public double getTotalRevenue() { return recordDAO.getTotalRevenue(); }

    public int getAvailableBikeSlots() { return slotDAO.getAvailableSlotsByType("Bike"); }
    public int getAvailableCarSlots() { return slotDAO.getAvailableSlotsByType("Car"); }
    public int getTotalBikeSlots() { return slotDAO.getTotalSlotsByType("Bike"); }
    public int getTotalCarSlots() { return slotDAO.getTotalSlotsByType("Car"); }

    // ==================== Slot Operations ====================

    public List<ParkingSlot> getAllSlots() { return slotDAO.getAllSlots(); }
    public List<ParkingSlot> getSlotsByType(String type) { return slotDAO.getSlotsByType(type); }

    // ==================== Record Operations ====================

    public List<ParkingRecord> getParkedVehicles() { return recordDAO.getParkedVehicles(); }
    public List<ParkingRecord> getAllRecords() { return recordDAO.getAllRecords(); }
    public List<ParkingRecord> searchRecords(String keyword) { return recordDAO.searchRecords(keyword); }

    public List<ParkingRecord> getRecordsByDateRange(LocalDateTime from, LocalDateTime to) {
        return recordDAO.getRecordsByDateRange(from, to);
    }

    public List<ParkingRecord> getRecordsByDateRangeAndKeyword(LocalDateTime from, LocalDateTime to, String keyword) {
        return recordDAO.getRecordsByDateRangeAndKeyword(from, to, keyword);
    }

    public ParkingRecord getRecordByTicketId(String ticketId) { return recordDAO.getByTicketId(ticketId); }

    public ParkingRecord getActiveByTicketId(String ticketId) { return recordDAO.getActiveByTicketId(ticketId); }

    public ParkingRecord getActiveByVehicleNumber(String vehicleNumber) { return recordDAO.getActiveByVehicleNumber(vehicleNumber); }

    /**
     * Find active record by either ticket ID or vehicle number
     */
    public ParkingRecord findActiveRecord(String identifier) {
        ParkingRecord record = recordDAO.getActiveByTicketId(identifier);
        if (record == null) {
            record = recordDAO.getActiveByVehicleNumber(identifier);
        }
        return record;
    }

    // ==================== Vehicle CRUD ====================

    public List<Vehicle> getAllVehicles() { return vehicleDAO.getAllVehicles(); }
    public List<Vehicle> searchVehicles(String keyword) { return vehicleDAO.searchVehicles(keyword); }
    public boolean addVehicle(Vehicle vehicle) { return vehicleDAO.addVehicle(vehicle); }
    public boolean updateVehicle(Vehicle vehicle) { return vehicleDAO.updateVehicle(vehicle); }
    public boolean deleteVehicle(int id) { return vehicleDAO.deleteVehicle(id); }
    public Vehicle getVehicleById(int id) { return vehicleDAO.getById(id); }

    // ==================== Payment Operations ====================

    public List<Payment> getAllPayments() { return paymentDAO.getAllPayments(); }
    public Payment getPaymentByTicketId(String ticketId) { return paymentDAO.getByTicketId(ticketId); }

    public List<Payment> getPaymentsByDateRange(LocalDateTime from, LocalDateTime to) {
        return paymentDAO.getPaymentsByDateRange(from, to);
    }

    // ==================== Reports ====================

    public double getRevenueByDateRange(LocalDateTime from, LocalDateTime to) {
        return recordDAO.getRevenueByDateRange(from, to);
    }

    public int getVehicleCountByType(String type, LocalDateTime from, LocalDateTime to) {
        return recordDAO.getVehicleCountByType(type, from, to);
    }

    public double getRevenueByType(String type, LocalDateTime from, LocalDateTime to) {
        return recordDAO.getRevenueByType(type, from, to);
    }

    public double getBikeRate() { return paymentDAO.getRatePerHour("Bike"); }
    public double getCarRate() { return paymentDAO.getRatePerHour("Car"); }
}
