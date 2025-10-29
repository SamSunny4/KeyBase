-- Create the database
CREATE DATABASE IF NOT EXISTS keybase;
USE keybase;

-- Create the duplicator table
CREATE TABLE IF NOT EXISTS duplicator (
    duplicator_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    vehicle_no VARCHAR(20),
    aadhar_no VARCHAR(20) NOT NULL,
    key_no VARCHAR(50) NOT NULL,
    image_path VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create index for faster searches
CREATE INDEX idx_key_no ON duplicator(key_no);
CREATE INDEX idx_aadhar_no ON duplicator(aadhar_no);
CREATE INDEX idx_vehicle_no ON duplicator(vehicle_no);