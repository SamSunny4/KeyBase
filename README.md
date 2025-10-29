# KeyBase - Key Management System

KeyBase is a comprehensive Java Swing application designed to manage key duplicator records with an embedded H2 database. It provides a complete solution for key shops to track customer information, key details, and maintain visual records through webcam integration.

## Features

### Core Functionality
- **Customer Management**: Store detailed customer information including name, phone number, vehicle type, vehicle number, and ID number
- **Key Tracking**: Track key details with key type, purpose (Home, Office, Locker, Department, Suspicious), date, quantity, and amount
- **Image Capture**: Capture and store customer photos using integrated webcam
- **Phone Validation**: Automatic validation (must be exactly 10 digits)
- **Date Management**: Built-in date picker with current date as default

### Advanced Features
- **Smart Search**: Multi-criteria search with case-insensitive substring matching across all fields
- **Date Range Filtering**: Search records within specific date ranges
- **Image Preview**: View customer photos in search results and record details
- **CSV Export**: Export all records or filtered search results to CSV format
- **Record Management**: View, edit, print, and delete record data
- **Enhanced Details View**: Professional dialog showing all record information with image preview
- **Quantity & Amount Tracking**: Track multiple key copies and pricing

### User Experience
- **Keyboard Navigation**: Full Enter key navigation through all form fields
- **Combo Box Integration**: Smooth navigation with keyboard shortcuts (Alt+C, Alt+R, Alt+S, Alt+E)
- **Smart Visibility**: Vehicle number field shows/hides based on vehicle type selection
- **Status Bar**: Real-time feedback on application actions
- **Context Menus**: Right-click options for quick actions on records

## Requirements

- Java JDK 8 or higher
- Webcam compatible with Java (optional, for image capture)
- Windows, macOS, or Linux operating system
- Minimum 2GB RAM recommended
- 100MB free disk space

## Required Libraries

Place the following JAR files in the `lib` directory:

- `h2-2.1.214.jar` (or newer) - H2 Database Engine
- `webcam-capture-0.3.12.jar` - Sarxos Webcam Capture library
- `slf4j-api-1.7.36.jar` - Required by webcam-capture
- `slf4j-simple-1.7.36.jar` - Simple SLF4J implementation
- `bridj-0.7.0.jar` - Required by webcam-capture

## Build and Run

Simply double-click `run.bat` (Windows) to start the application. The script will:
- Compile the application automatically if needed (first run)
- Initialize the database if it doesn't exist
- Launch the KeyBase GUI

That's it! The application handles everything automatically.

### Manual Build (optional)

If you prefer manual compilation:

1. Ensure you have the required JAR files in the `lib` directory
2. Compile: `javac -cp "lib/*" -d build\classes "@build\sources.txt"`
3. Run: `java -cp "lib/*;build\classes" src.KeyBase`

## Usage Guide

### Adding a New Record

1. **Launch the application**
2. **Enter customer information**:
   - **Name** (required)
   - **Phone number** (required, exactly 10 digits)
   - **Key Type**: Select vehicle type (SELECT, 2 Wheeler, 4 Wheeler, Other)
   - **Vehicle No**: Shown only for 2 Wheeler and 4 Wheeler
   - **ID Number**: Customer identification
   - **Key Number**: Unique key identifier
   - **Key For**: Purpose (Home, Office, Locker, Department, Suspicious)
   - **Date**: Defaults to current date, click button to change
   - **Quantity**: Number of keys (use arrow keys to increment/decrement)
   - **Amount**: Price charged
   - **Remarks**: Additional notes
3. **Capture Image**: Click "Capture" button (Alt+C) to take customer photo
4. **Delete Image** (if needed): After capturing, "Delete" button appears to remove image
5. **Save Record**: Click "Save" (Alt+S) to store in database
6. **Reset Form**: Click "Reset" (Alt+R) to clear for next entry

### Viewing Records

**Main Table**: All records displayed at bottom of main window
- Double-click any record to view detailed information
- Right-click for context menu with options

**Record Details Dialog**:
- View all information with customer photo
- **Edit Details**: Modify any field and save changes
- **Delete Data**: Clear all fields except Name and ID (sets to "deleted")
- **Print**: Print formatted record with photo
- **Export CSV**: Export single record to CSV file

### Searching Records

1. **Open Search**: Press Ctrl+F or File → Search Records
2. **Search Options**:
   - **Search Field**: Select specific field or "All Fields"
   - **Search Text**: Enter search term (case-insensitive)
   - **Vehicle Type Filter**: Filter by vehicle type
   - **Key For Filter**: Filter by key purpose
   - **Date Range**: Set From/To dates
3. **Click Search**: Results shown in table with count in title
4. **View Images**: Select record to preview customer photo
5. **Export Results**: Click "Export Results to CSV" or press Ctrl+E (exports only filtered results)

### Context Menu Actions (Right-click)

Available in both Main Form and Search Window tables:
- **View Details**: Open detailed record view
- **Print Record**: Print individual record
- **Export Record to CSV**: Export single record

## Menu Options

### File Menu
- **View Key Entries** (Ctrl+V) - Refresh table with latest records
- **Search Records** (Ctrl+F) - Open advanced search window
- **Export to CSV** (Ctrl+E) - Export all records to CSV file
- **Preferences** - Configure webcam device and image storage location
- **Exit** - Close application

### Help Menu
- **Readme** - Display this help documentation
- **About** - Show application information and version

## Keyboard Shortcuts

### Main Form
- **Ctrl+F**: Open Search window
- **Ctrl+E**: Export all records to CSV
- **Alt+C**: Capture image
- **Alt+S**: Save record
- **Alt+R**: Reset form
- **Enter**: Navigate to next field

### Search Window
- **Ctrl+E**: Export filtered results to CSV
- **Double-click**: View record details
- **Right-click**: Show context menu

## Database Structure

### Main Table: duplicator

| Field | Type | Description |
|-------|------|-------------|
| duplicator_id | INT | Auto-increment primary key |
| name | VARCHAR(100) | Customer name |
| phone_number | VARCHAR(15) | 10-digit phone number |
| vehicle_type | VARCHAR(20) | Key Type (2 Wheeler, 4 Wheeler, Other) |
| vehicle_no | VARCHAR(20) | Vehicle registration number |
| id_no | VARCHAR(50) | Customer ID number |
| key_no | VARCHAR(50) | Unique key identifier |
| key_type | VARCHAR(20) | Key For (Home, Office, Locker, Department, Suspicious) |
| date_added | DATE | Date of record creation |
| remarks | TEXT | Additional notes |
| quantity | INT | Number of keys (default: 1) |
| amount | DECIMAL(10,2) | Price charged (default: 0.00) |
| image_path | VARCHAR(255) | Path to customer photo |
| created_at | TIMESTAMP | Auto-generated creation timestamp |
| updated_at | TIMESTAMP | Auto-updated modification timestamp |

## Search Functionality

### Search Features
- **Case-insensitive**: Searches ignore letter case
- **Substring Matching**: Finds partial matches (e.g., "john" finds "Johnson")
- **Multiple Fields**: Search across all fields simultaneously
- **Date Filtering**: Filter records by date range
- **Type Filtering**: Filter by vehicle type and key purpose
- **Result Count**: Shows number of matching records in title
- **Image Preview**: See customer photos in results

### Searchable Fields
1. All Fields (searches across all text fields)
2. Name
3. Phone Number
4. Vehicle Number
5. ID Number
6. Key Number
7. Remarks

## Export Options

### Full Export
- File → Export to CSV (Ctrl+E)
- Exports all records in database
- Excludes internal timestamps (created_at, updated_at)

### Filtered Export
- Search Window → Export Results to CSV
- Exports only visible/filtered records
- Shows record count in success message

### Single Record Export
- Right-click record → Export Record to CSV
- Or in Record Details → Export CSV button
- Exports individual record in field-value format

## Print Functionality

### Record Printing
- Open record details dialog
- Click "Print" button or use context menu
- Features:
  - Professional formatted layout
  - Includes customer photo (150x150)
  - All record fields
  - Print timestamp in footer
  - Standard printer dialog for selection

## Image Management

### Capturing Images
1. Click "Capture" button in main form
2. Webcam preview window opens
3. Click "Capture" to take photo
4. Image saved to cache temporarily
5. "Delete" button appears to remove if needed
6. Image moved to permanent location on Save

### Image Storage
- Default location: `images/` directory
- Configurable in: `config/app.properties`
- Format: JPEG
- Naming: Auto-generated timestamp-based names

### Deleting Images
- **Before Save**: Click "Delete" button next to "Capture"
- **After Save**: Use "Delete Data" in record details (removes all data except name and ID)

## Application Settings

Configure in `config/app.properties`:
```properties
webcam.device=0
images.directory=images
```

### Configuration Options
- **webcam.device**: Webcam index (0 for default, 1+ for additional cameras)
- **images.directory**: Directory for storing captured photos
  - Relative path: `images` (in application directory)
  - Absolute path: `C:/KeyBase/images` or `/home/user/keybase/images`

## Database Configuration

The application uses **H2 embedded database**:
- **No external server** required
- **Automatic initialization** on first run
- **Persistent storage** - data saved between runs
- **File location**: `data/` directory
- **Schema**: Defined in `config/init_h2_database.sql`

### Database Files
- `keybase.mv.db` - Main database file
- `keybase.trace.db` - Trace log (optional)

### Database Operations
- **Auto-backup**: Enabled by default
- **Transaction safe**: All operations are ACID-compliant
- **Connection pooling**: Efficient resource management

## Troubleshooting

### Application Won't Start
- Verify all JAR files in `lib/` directory
- Check Java installation: `java -version`
- Check PATH includes Java bin directory
- Review console output for errors
- Try: `java -cp "lib/*;build\classes" src.KeyBase`

### Webcam Issues
- Ensure webcam is connected and recognized
- Check webcam permissions in OS settings
- Try different webcam.device value (0, 1, 2...)
- Test webcam with other applications
- Update webcam drivers

### Database Errors
- Delete `data/*.db` files to reset database
- Check write permissions in application directory
- Verify disk space availability
- Review `config/init_h2_database.sql` for schema errors

### Cannot Save Images
- Verify `images/` directory exists
- Check write permissions
- Try different images.directory in config
- Ensure sufficient disk space

### Compilation Errors
- Verify all source files in `src/` directory
- Check `build/sources.txt` includes all Java files
- Clean and rebuild: Delete `build/classes/` directory
- Update Java compiler if using old version

### Search Not Working
- Check database connection
- Verify search field has valid data
- Try searching "All Fields" with simple text
- Clear search filters and try again

## Tips and Best Practices

1. **Regular Backups**: Periodically backup the `data/` directory
2. **Image Organization**: Keep images.directory on drive with sufficient space
3. **Phone Numbers**: Ensure 10-digit format for consistency
4. **Key Numbers**: Use consistent numbering scheme
5. **Remarks Field**: Add detailed notes for future reference
6. **Date Selection**: Use date picker for accuracy
7. **Quantity Tracking**: Update quantity when making multiple copies
8. **Amount Entry**: Record pricing for accounting purposes
9. **Image Capture**: Ensure good lighting for clear photos
10. **Regular Updates**: Export CSV periodically for external backup

## Technical Details

### Architecture
- **Language**: Java 8+
- **GUI Framework**: Swing
- **Database**: H2 Embedded
- **Build System**: Command-line javac
- **Image Format**: JPEG
- **Date Format**: yyyy-MM-dd (ISO 8601)

### Performance
- **Startup Time**: ~2-3 seconds
- **Record Retrieval**: Near-instant for < 10,000 records
- **Search Speed**: Optimized with SQL LOWER() function
- **Memory Usage**: ~50-100MB typical

### Security Considerations
- Database uses file-based storage (consider encryption for sensitive data)
- No network exposure by default
- Images stored as regular files
- Recommend file system permissions for production use

## Version History

### Version 2.0 (Current)
- Added Edit Record functionality
- Enhanced Record Details dialog with image display
- Added Print Record feature
- Individual record CSV export
- Delete Data functionality (preserves Name and ID)
- Context menus for quick actions
- Improved Key For dropdown (Department, Suspicious)
- Quantity and Amount fields with spinner
- Delete captured image before save
- Filtered CSV export in search
- Enhanced keyboard navigation

### Version 1.0
- Initial release
- Basic CRUD operations
- Webcam integration
- Search functionality
- CSV export

## Quick Start

**Windows**: Double-click `run.bat`

**macOS / Linux**: 
```bash
javac -cp "lib/*" -d build/classes @build/sources.txt
java -cp "lib/*:build/classes" src.KeyBase
```

## Support and Contributing

For issues, questions, or contributions:
1. Check this README for solutions
2. Review `TROUBLESHOOTING.md` for detailed help
3. Examine source code comments for implementation details

## License

This project is licensed under the MIT License.

---

**KeyBase - Efficient Key Management Made Simple**