-- Script to delete all data and insert test data
-- Use this for MySQL/MariaDB database

USE keybase;

-- Delete all existing data
DELETE FROM duplicator;

-- Reset the auto-increment counter (MySQL syntax)
ALTER TABLE duplicator AUTO_INCREMENT = 1;

-- Insert test data
INSERT INTO duplicator (name, phone_number, vehicle_type, vehicle_no, id_no, key_no, key_type, date_added, remarks, image_path) 
VALUES 
('Rajesh Kumar', '9876543210', '2 Wheeler', 'KA-01-AB-1234', 'AADHAAR123456', 'KEY001', 'Personal', '2025-10-20', 'Regular customer', NULL),
('Priya Sharma', '9876543211', '4 Wheeler', 'KA-02-CD-5678', 'AADHAAR234567', 'KEY002', 'Personal', '2025-10-21', 'First time customer', NULL),
('Ramesh Industries', '9876543212', '4 Wheeler', 'KA-03-EF-9012', 'GST987654321', 'KEY003', 'Commercial', '2025-10-22', 'Business fleet key', NULL),
('Anita Desai', '9876543213', '2 Wheeler', 'KA-04-GH-3456', 'AADHAAR345678', 'KEY004', 'Personal', '2025-10-23', 'Lost original key', NULL),
('Tech Solutions Ltd', '9876543214', '4 Wheeler', 'KA-05-IJ-7890', 'GST876543210', 'KEY005', 'Commercial', '2025-10-24', 'Company vehicle', NULL),
('Suresh Menon', '9876543215', 'Other', NULL, 'AADHAAR456789', 'KEY006', 'Personal', '2025-10-25', 'Locker key duplicate', NULL),
('Global Logistics', '9876543216', '4 Wheeler', 'KA-06-KL-2345', 'GST765432109', 'KEY007', 'Commercial', '2025-10-26', 'Delivery van key', NULL),
('Kavita Iyer', '9876543217', '2 Wheeler', 'KA-07-MN-6789', 'AADHAAR567890', 'KEY008', 'Personal', '2025-10-26', 'Spare key needed', NULL);

-- Display the inserted data
SELECT * FROM duplicator ORDER BY duplicator_id;
