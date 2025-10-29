-- Sample/test data for distribution copy
INSERT INTO duplicator (name, phone_number, vehicle_type, vehicle_no, aadhar_no, key_no, key_type, date_added, remarks, image_path) VALUES
('Alice Tester', '9876543210', 'Car', 'KA01AA1111', '111122223333', 'K-A1', 'Home', DATE '2025-10-01', 'Test record for Alice', NULL);

INSERT INTO duplicator (name, phone_number, vehicle_type, vehicle_no, aadhar_no, key_no, key_type, date_added, remarks, image_path) VALUES
('Bob Example', '9123456789', 'Bike', NULL, '444455556666', 'K-B2', 'Office', DATE '2025-09-15', 'No vehicle number provided', NULL);
