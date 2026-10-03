package com.parkwise.model;

import java.sql.Timestamp;

/**
 * Model class representing a Vehicle entity.
 */
public class Vehicle {
    private int id;
    private String vehicleNumber;
    private String ownerName;
    private String phone;
    private String vehicleType; // "Bike" or "Car"
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Vehicle() {}

    public Vehicle(String vehicleNumber, String ownerName, String phone, String vehicleType) {
        this.vehicleNumber = vehicleNumber;
        this.ownerName = ownerName;
        this.phone = phone;
        this.vehicleType = vehicleType;
    }

    public Vehicle(int id, String vehicleNumber, String ownerName, String phone, String vehicleType) {
        this.id = id;
        this.vehicleNumber = vehicleNumber;
        this.ownerName = ownerName;
        this.phone = phone;
        this.vehicleType = vehicleType;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return vehicleNumber + " - " + ownerName + " (" + vehicleType + ")";
    }
}
