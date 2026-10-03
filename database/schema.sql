-- ============================================
-- ParkWise - Parking Lot Management System
-- MySQL Database Schema
-- ============================================

CREATE DATABASE IF NOT EXISTS parkwise;
USE parkwise;

-- Admin table
CREATE TABLE IF NOT EXISTS admin (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Parking slots table
CREATE TABLE IF NOT EXISTS parking_slots (
    slot_id INT AUTO_INCREMENT PRIMARY KEY,
    slot_number VARCHAR(10) NOT NULL UNIQUE,
    slot_type ENUM('Bike', 'Car') NOT NULL,
    status ENUM('Available', 'Occupied') NOT NULL DEFAULT 'Available',
    floor_number INT DEFAULT 1,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Vehicles table
CREATE TABLE IF NOT EXISTS vehicles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_number VARCHAR(20) NOT NULL,
    owner_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    vehicle_type ENUM('Bike', 'Car') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Parking records table
CREATE TABLE IF NOT EXISTS parking_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ticket_id VARCHAR(20) NOT NULL UNIQUE,
    vehicle_number VARCHAR(20) NOT NULL,
    owner_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    vehicle_type ENUM('Bike', 'Car') NOT NULL,
    slot_id INT NOT NULL,
    slot_number VARCHAR(10) NOT NULL,
    entry_time DATETIME NOT NULL,
    exit_time DATETIME DEFAULT NULL,
    duration_minutes INT DEFAULT NULL,
    amount DECIMAL(10,2) DEFAULT NULL,
    payment_status ENUM('Pending', 'Paid') DEFAULT 'Pending',
    status ENUM('Parked', 'Exited') DEFAULT 'Parked',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (slot_id) REFERENCES parking_slots(slot_id)
);

-- Payments table
CREATE TABLE IF NOT EXISTS payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ticket_id VARCHAR(20) NOT NULL,
    vehicle_number VARCHAR(20) NOT NULL,
    vehicle_type ENUM('Bike', 'Car') NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    duration_minutes INT NOT NULL,
    payment_method ENUM('Cash', 'Card', 'UPI') DEFAULT 'Cash',
    payment_time DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ticket_id) REFERENCES parking_records(ticket_id)
);

-- Rate configuration table
CREATE TABLE IF NOT EXISTS rate_config (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_type ENUM('Bike', 'Car') NOT NULL UNIQUE,
    rate_per_hour DECIMAL(10,2) NOT NULL,
    minimum_charge DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================
-- Insert default admin (password: admin123)
-- ============================================
INSERT INTO admin (username, password, full_name, email) VALUES
('admin', 'admin123', 'System Administrator', 'admin@parkwise.com');

-- ============================================
-- Insert rate configuration
-- ============================================
INSERT INTO rate_config (vehicle_type, rate_per_hour, minimum_charge) VALUES
('Bike', 10.00, 10.00),
('Car', 20.00, 20.00);

-- ============================================
-- Insert Bike parking slots (B01-B30)
-- ============================================
INSERT INTO parking_slots (slot_number, slot_type, floor_number) VALUES
('B01', 'Bike', 1), ('B02', 'Bike', 1), ('B03', 'Bike', 1), ('B04', 'Bike', 1), ('B05', 'Bike', 1),
('B06', 'Bike', 1), ('B07', 'Bike', 1), ('B08', 'Bike', 1), ('B09', 'Bike', 1), ('B10', 'Bike', 1),
('B11', 'Bike', 1), ('B12', 'Bike', 1), ('B13', 'Bike', 1), ('B14', 'Bike', 1), ('B15', 'Bike', 1),
('B16', 'Bike', 2), ('B17', 'Bike', 2), ('B18', 'Bike', 2), ('B19', 'Bike', 2), ('B20', 'Bike', 2),
('B21', 'Bike', 2), ('B22', 'Bike', 2), ('B23', 'Bike', 2), ('B24', 'Bike', 2), ('B25', 'Bike', 2),
('B26', 'Bike', 2), ('B27', 'Bike', 2), ('B28', 'Bike', 2), ('B29', 'Bike', 2), ('B30', 'Bike', 2);

-- ============================================
-- Insert Car parking slots (C01-C20)
-- ============================================
INSERT INTO parking_slots (slot_number, slot_type, floor_number) VALUES
('C01', 'Car', 1), ('C02', 'Car', 1), ('C03', 'Car', 1), ('C04', 'Car', 1), ('C05', 'Car', 1),
('C06', 'Car', 1), ('C07', 'Car', 1), ('C08', 'Car', 1), ('C09', 'Car', 1), ('C10', 'Car', 1),
('C11', 'Car', 2), ('C12', 'Car', 2), ('C13', 'Car', 2), ('C14', 'Car', 2), ('C15', 'Car', 2),
('C16', 'Car', 2), ('C17', 'Car', 2), ('C18', 'Car', 2), ('C19', 'Car', 2), ('C20', 'Car', 2);

