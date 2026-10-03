package com.parkwise.model;

import java.time.LocalDateTime;
import java.sql.Timestamp;

/**
 * Model class representing a Payment entity.
 */
public class Payment {
    private int id;
    private String ticketId;
    private String vehicleNumber;
    private String vehicleType;
    private double amount;
    private int durationMinutes;
    private String paymentMethod; // "Cash", "Card", "UPI"
    private LocalDateTime paymentTime;
    private Timestamp createdAt;

    public Payment() {}

    public Payment(String ticketId, String vehicleNumber, String vehicleType,
                   double amount, int durationMinutes, String paymentMethod, LocalDateTime paymentTime) {
        this.ticketId = ticketId;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.amount = amount;
        this.durationMinutes = durationMinutes;
        this.paymentMethod = paymentMethod;
        this.paymentTime = paymentTime;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public LocalDateTime getPaymentTime() { return paymentTime; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    /**
     * Get formatted duration string
     */
    public String getFormattedDuration() {
        int hours = durationMinutes / 60;
        int mins = durationMinutes % 60;
        if (hours > 0) {
            return hours + "h " + mins + "m";
        }
        return mins + "m";
    }

    @Override
    public String toString() {
        return ticketId + " | ₹" + String.format("%.2f", amount) + " | " + paymentMethod;
    }
}
