package src;

import java.sql.*;
import java.util.Date;
import javax.swing.JOptionPane;

public class Duplicator {
    private int duplicatorId;
    private String name;
    private String phoneNumber;
    private String idNo;
    private String vehicleNo;
    private String keyNo;
    private String keyType;
    private String purpose;
    private Date dateAdded;
    private String remarks;
    private int quantity;
    private double amount;
    private String imagePath;

    // Default constructor
    public Duplicator() {
    }

    // Constructor with all fields except ID (which is auto-incremented)
    public Duplicator(String name, String phoneNumber, String idNo, String vehicleNo,
                      String keyNo, String keyType, String purpose, Date dateAdded, String remarks, 
                      int quantity, double amount, String imagePath) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.idNo = idNo;
        this.vehicleNo = vehicleNo;
        this.keyNo = keyNo;
        this.keyType = keyType;
        this.purpose = purpose;
        this.dateAdded = dateAdded;
        this.remarks = remarks;
        this.quantity = quantity;
        this.amount = amount;
        this.imagePath = imagePath;
    }

    // Getters and setters
    public int getDuplicatorId() {
        return duplicatorId;
    }

    public void setDuplicatorId(int duplicatorId) {
        this.duplicatorId = duplicatorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    public String getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }

    public String getKeyNo() {
        return keyNo;
    }

    public void setKeyNo(String keyNo) {
        this.keyNo = keyNo;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    public String getKeyType() {
        return keyType;
    }
    
    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }
    
    public String getPurpose() {
        return purpose;
    }
    
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
    
    public Date getDateAdded() {
        return dateAdded;
    }
    
    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }

    // Save to database
    public boolean save() {
        String sql = "INSERT INTO duplicator (name, phone_number, id_no, vehicle_no, key_no, key_type, purpose, date_added, remarks, quantity, amount, image_path) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Required fields - name, phone, and ID
            pstmt.setString(1, name);
            pstmt.setString(2, phoneNumber);
            
            // ID No is required - never save as NULL
            pstmt.setString(3, idNo);
            
            // Optional vehicle number field
            if (vehicleNo == null || vehicleNo.trim().isEmpty()) {
                pstmt.setNull(4, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(4, vehicleNo);
            }
            
            // Optional fields - save as NULL if empty
            if (keyNo == null || keyNo.trim().isEmpty()) {
                pstmt.setNull(5, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(5, keyNo);
            }
            
            if (keyType == null || keyType.trim().isEmpty() || keyType.equals("SELECT")) {
                pstmt.setNull(6, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(6, keyType);
            }
            
            if (purpose == null || purpose.trim().isEmpty() || purpose.equals("SELECT")) {
                pstmt.setNull(7, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(7, purpose);
            }
            
            if (dateAdded != null) {
                pstmt.setDate(8, new java.sql.Date(dateAdded.getTime()));
            } else {
                pstmt.setNull(8, java.sql.Types.DATE);
            }
            
            if (remarks == null || remarks.trim().isEmpty()) {
                pstmt.setNull(9, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(9, remarks);
            }
            
            pstmt.setInt(10, quantity);
            pstmt.setDouble(11, amount);
            
            if (imagePath == null || imagePath.trim().isEmpty()) {
                pstmt.setNull(12, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(12, imagePath);
            }
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // Get the auto-generated ID
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        duplicatorId = generatedKeys.getInt(1);
                    }
                }
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Error saving record: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    // Find a duplicator by ID
    public static Duplicator findById(int id) {
        String sql = "SELECT * FROM duplicator WHERE duplicator_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Duplicator duplicator = new Duplicator();
                duplicator.setDuplicatorId(rs.getInt("duplicator_id"));
                duplicator.setName(rs.getString("name"));
                duplicator.setPhoneNumber(rs.getString("phone_number"));
                duplicator.setIdNo(rs.getString("id_no"));
                duplicator.setVehicleNo(rs.getString("vehicle_no"));
                duplicator.setKeyNo(rs.getString("key_no"));
                duplicator.setKeyType(rs.getString("key_type"));
                duplicator.setPurpose(rs.getString("purpose"));
                duplicator.setDateAdded(rs.getDate("date_added"));
                duplicator.setRemarks(rs.getString("remarks"));
                duplicator.setQuantity(rs.getInt("quantity"));
                duplicator.setAmount(rs.getDouble("amount"));
                duplicator.setImagePath(rs.getString("image_path"));
                return duplicator;
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Error finding record: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
        
        return null;
    }
    
    // Update existing record
    public boolean update() {
        String sql = "UPDATE duplicator SET name = ?, phone_number = ?, id_no = ?, vehicle_no = ?, key_no = ?, " +
                     "key_type = ?, purpose = ?, date_added = ?, remarks = ?, quantity = ?, amount = ?, image_path = ? " +
                     "WHERE duplicator_id = ?";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Required fields - name, phone, and ID
            pstmt.setString(1, name);
            pstmt.setString(2, phoneNumber);
            
            // ID No is required - never save as NULL
            pstmt.setString(3, idNo);
            
            // Optional vehicle number field
            if (vehicleNo == null || vehicleNo.trim().isEmpty()) {
                pstmt.setNull(4, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(4, vehicleNo);
            }
            
            // Optional fields - save as NULL if empty
            if (keyNo == null || keyNo.trim().isEmpty()) {
                pstmt.setNull(5, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(5, keyNo);
            }
            
            if (keyType == null || keyType.trim().isEmpty() || keyType.equals("SELECT")) {
                pstmt.setNull(6, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(6, keyType);
            }
            
            if (purpose == null || purpose.trim().isEmpty() || purpose.equals("SELECT")) {
                pstmt.setNull(7, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(7, purpose);
            }
            
            if (dateAdded != null) {
                pstmt.setDate(8, new java.sql.Date(dateAdded.getTime()));
            } else {
                pstmt.setNull(8, java.sql.Types.DATE);
            }
            
            if (remarks == null || remarks.trim().isEmpty()) {
                pstmt.setNull(9, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(9, remarks);
            }
            
            pstmt.setInt(10, quantity);
            pstmt.setDouble(11, amount);
            
            if (imagePath == null || imagePath.trim().isEmpty()) {
                pstmt.setNull(12, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(12, imagePath);
            }
            
            pstmt.setInt(13, duplicatorId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Error updating record: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    // Update existing record (clear data except name and ID)
    public boolean clearDataExceptNameAndId() {
        String sql = "UPDATE duplicator SET phone_number = 'deleted', id_no = 'deleted', " +
                     "key_no = 'deleted', key_type = 'deleted', date_added = NULL, remarks = 'deleted', " +
                     "quantity = 0, amount = 0.00, image_path = NULL " +
                     "WHERE duplicator_id = ?";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, duplicatorId);
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // Update the current object to reflect changes
                this.phoneNumber = "deleted";
                this.idNo = "deleted";
                this.keyNo = "deleted";
                this.keyType = "deleted";
                this.dateAdded = null;
                this.remarks = "deleted";
                this.quantity = 0;
                this.amount = 0.00;
                this.imagePath = null;
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Error clearing record data: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    // Get all duplicators
    public static ResultSet getAllDuplicators() throws SQLException {
        String sql = "SELECT * FROM duplicator ORDER BY duplicator_id DESC";
        Connection conn = DatabaseConnection.getConnection();
        Statement stmt = conn.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        return stmt.executeQuery(sql);
    }
}