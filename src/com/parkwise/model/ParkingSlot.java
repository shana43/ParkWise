package com.parkwise.model;

import java.sql.Timestamp;

/**
 * Model class representing a Parking Slot entity.
 */
public class ParkingSlot {
    private int slotId;
    private String slotNumber;
    private String slotType; // "Bike" or "Car"
    private String status;   // "Available" or "Occupied"
    private int floorNumber;
    private Timestamp updatedAt;

    public ParkingSlot() {}

    public ParkingSlot(int slotId, String slotNumber, String slotType, String status, int floorNumber) {
        this.slotId = slotId;
        this.slotNumber = slotNumber;
        this.slotType = slotType;
        this.status = status;
        this.floorNumber = floorNumber;
    }

    // Getters and Setters
    public int getSlotId() { return slotId; }
    public void setSlotId(int slotId) { this.slotId = slotId; }

    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }

    public String getSlotType() { return slotType; }
    public void setSlotType(String slotType) { this.slotType = slotType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public boolean isAvailable() {
        return "Available".equals(status);
    }

    @Override
    public String toString() {
        return slotNumber + " [" + slotType + "] - " + status;
    }
}
