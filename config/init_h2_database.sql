-- Create the duplicator table with optimized schema (only if it doesn't exist)
CREATE TABLE IF NOT EXISTS duplicator (
    duplicator_id INT AUTO_INCREMENT PRIMARY KEY,
    
    -- Required fields (NOT NULL)
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    id_no VARCHAR(50) NOT NULL,
    
    -- Optional fields (can be NULL)
    vehicle_no VARCHAR(50),
    key_no VARCHAR(50),
    key_type VARCHAR(50),
    purpose VARCHAR(50),
    date_added DATE,
    time_added TIME,
    remarks VARCHAR(500),
    quantity INT DEFAULT 1,
    amount DECIMAL(10, 2) DEFAULT 0.00,
    image_path VARCHAR(255),
    
    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for faster searches on commonly queried fields (if they don't exist)
CREATE INDEX IF NOT EXISTS idx_phone_number ON duplicator(phone_number);
CREATE INDEX IF NOT EXISTS idx_id_no ON duplicator(id_no);
CREATE INDEX IF NOT EXISTS idx_key_no ON duplicator(key_no);
CREATE INDEX IF NOT EXISTS idx_name ON duplicator(name);
