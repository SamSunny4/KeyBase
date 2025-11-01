-- Create the database
CREATE DATABASE IF NOT EXISTS keybase;
USE keybase;

-- Drop existing table to reset database
DROP TABLE IF EXISTS duplicator;

-- Create the duplicator table with optimized schema
CREATE TABLE duplicator (
    duplicator_id INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Required fields (NOT NULL)
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    id_no VARCHAR(50) NOT NULL,
    
    -- Optional fields (can be NULL)
    vehicle_no VARCHAR(50),
    key_no VARCHAR(50),
    key_type VARCHAR(50),
    date_added DATE,
    remarks VARCHAR(500),
    quantity INT DEFAULT 1,
    amount DECIMAL(10, 2) DEFAULT 0.00,
    image_path VARCHAR(255),
    
    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create indexes for faster searches on commonly queried fields
CREATE INDEX idx_phone_number ON duplicator(phone_number);
CREATE INDEX idx_id_no ON duplicator(id_no);
CREATE INDEX idx_key_no ON duplicator(key_no);
CREATE INDEX idx_name ON duplicator(name);
