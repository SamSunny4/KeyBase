# KeyBase Refactoring Plan - Remove Vehicle Fields

## Overview
Remove vehicle_type and vehicle_no fields from the entire application.
Keep only: name, phone_number, id_no, key_no, key_type, date_added, remarks, quantity, amount, image_path

## Completed Changes ✅

### 1. Database Schema
- ✅ Updated `config/init_h2_database.sql` - removed vehicle_type and vehicle_no columns
- ✅ Updated `config/init_database.sql` - removed vehicle_type and vehicle_no columns
- ✅ Removed vehicle_no index

### 2. Duplicator Model
- ✅ Removed `vehicleType` and `vehicleNo` fields
- ✅ Updated constructor signature (removed 2 parameters)
- ✅ Removed getVehicleType(), setVehicleType(), getVehicleNo(), setVehicleNo()
- ✅ Updated save() method SQL and parameters
- ✅ Updated findById() method - removed setVehicleType/setVehicleNo calls
- ✅ Updated update() method SQL and parameters
- ✅ Updated clearDataExceptNameAndId() method

### 3. Removed Unused Classes
- ✅ Deleted `DbQueryTest.java`
- ✅ Deleted `CsvExporter.java`
- ✅ Deleted `ResetDatabase.java`
- ✅ Updated `build/sources.txt`

## Remaining Changes Needed ⚠️

### 4. MainForm.java (CRITICAL - Manual fixes required)

**Remove these field declarations (around line 22-27):**
```java
private JTextField txtVehicleNo;
private JLabel lblVehicleNo;
private JComboBox<String> cmbVehicleType;
```

**In saveRecord() method (around line 1031):**
- Remove: `String vehicleType = (String) cmbVehicleType.getSelectedItem();`

**In initializeComponents() method (around line 336):**
- Remove entire cmbVehicleType initialization block
- Remove txtVehicleNo initialization
- Remove lblVehicleNo initialization
- Adjust GridBagConstraints rows accordingly

**In resetForm() method:**
- Remove: `txtVehicleNo.setText("");`
- Remove: `cmbVehicleType.setSelectedIndex(0);`

**In validateForm() method (around line 316-320):**
- Remove any validation code for vehicleNo or vehicleType

**In loadKeyEntries() method:**
- Update table model to remove vehicle columns
- Adjust column indexes

**In exportAllRecords() method (around line 194):**
- Remove: `writer.append("Key Type,").append(escapeCsv(d.getVehicleType())).append("\n");`
- Remove CsvExporter reference

**In event handlers:**
- Remove updateVehicleNoVisibility() method calls
- Remove cmbVehicleType action listeners

### 5. SearchWindow.java (CRITICAL)

**Remove field declaration:**
```java
private JComboBox<String> cmbVehicleType;
```

**In initComponents():**
- Remove cmbVehicleType initialization (around line 101-103)

**In performSearch() method:**
- Remove: `String vehicleType = (String) cmbVehicleType.getSelectedItem();`
- Remove vehicle_type from SQL WHERE clause
- Remove vehicle_type parameter setting
- Remove: `rs.getString("vehicle_type")` from table model
- Adjust column indexes

**In resetFilters():**
- Remove: `cmbVehicleType.setSelectedIndex(0);`

**In exportResultsToCsv():**
- Remove: `writer.append("Key Type,").append(escapeCsv(d.getVehicleType())).append("\n");`

### 6. RecordDetailsDialog.java

**In constructor's addDetailRow() calls:**
- Remove: `addDetailRow(detailsPanel, gbc, row++, "Key Type:", duplicator.getVehicleType());`

**In print() method:**
- Remove the line that prints vehicleType

**In exportToCsv():**
- Remove: `writer.append("Key Type,").append(escapeCsv(duplicator.getVehicleType())).append("\n");`

### 7. EditRecordDialog.java

**Remove field declarations:**
```java
private JTextField txtVehicleNo;
private JComboBox<String> cmbKeyType; // This is vehicle type, misnamed
```

**In initComponents():**
- Remove txtVehicleNo and cmbKeyType (vehicle type) initialization
- Adjust grid layout rows

**In loadRecordData():**
- Remove: `txtVehicleNo.setText(duplicator.getVehicleNo());`
- Remove: `cmbKeyType.setSelectedItem(duplicator.getVehicleType());`

**In saveChanges():**
- Remove: `duplicator.setVehicleType((String) cmbKeyType.getSelectedItem());`
- Remove: `duplicator.setVehicleNo(txtVehicleNo.getText().trim());`

## Step-by-Step Execution Plan

1. **Backup** current working code
2. Apply remaining MainForm.java changes (largest file, most complex)
3. Apply SearchWindow.java changes
4. Apply RecordDetailsDialog.java changes  
5. Apply EditRecordDialog.java changes
6. Delete old database: `del e:\KeyBase\data\keybase.mv.db`
7. Recompile: `javac -cp "lib/*" -d build\classes "@build\sources.txt"`
8. Test application startup
9. Test record creation (only name, phone, ID required)
10. Test search functionality
11. Test record viewing and editing

## Database Reset Required
After code changes, delete `e:\KeyBase\data\keybase.mv.db` to force recreation with new schema.

## Testing Checklist
- [ ] Application starts without errors
- [ ] Can create record with name, phone, ID only
- [ ] Can add optional fields: key_no, key_type, date, remarks, quantity, amount, image
- [ ] Search works without vehicle fields
- [ ] Edit record works
- [ ] Delete data works
- [ ] Print record works
- [ ] Export CSV works
- [ ] View record details works

## Notes
- The cmbKeyType in EditRecordDialog appears to be for vehicle type (not key type) - needs verification
- MainForm has correct cmbKeyType for key purpose (Home, Office, Locker, Department, Suspicious)
- All CSV export functions need vehicle fields removed
