# KeyBase - Key Management System

KeyBase is a comprehensive Java Swing application designed to manage key duplicator records with an embedded H2 database. It provides a complete solution for key shops to track customer information, key details, and maintain visual records through webcam integration.

> Recent updates (2025): Portable JRE bundling (no Java install needed), enhanced image staging (captures preview immediately but only committed on save), service/payment tagging, and improved search filters.

## Features
Target runtime: Java 17 (LTS). The distributed installer or staged `dist/KeyBase` folder includes a portable JRE so end users do NOT need Java pre-installed.
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
 - **Service Type Tracking**: Remarks are auto-suffixed when marked In-shop or On-site (duplicate by default)
 - **Payment Marker**: UPI payments automatically add a " - UPI" suffix in remarks; search can filter Cash vs UPI
 - **Deferred Image Commit**: New or recaptured images appear immediately but only replace the stored file after you click Save in Edit dialog
Application defaults are provided in `config/app.properties` which is packaged with the installer. At runtime the application writes per-user overrides to:

- Windows: `%LOCALAPPDATA%\KeyBase\app.properties`

Configure defaults in `config/app.properties` (these are copied/merged into the per-user file on first run):
- **CSV Export**: Export all records or filtered search results to CSV format
- **Record Management**: View, edit, print, and delete record data
- **Enhanced Details View**: Professional dialog showing all record information with image preview
- **Daily Print Shortcut**: Press Ctrl+P to print a formatted report of today's records directly from the main form.
- **Smart Placeholder Images**: Missing customer photos automatically swap to themed placeholders, including a special "no results" graphic in the search window.

### User Experience
- **Keyboard Navigation**: Full Enter key navigation through all form fields
- **Combo Box Integration**: Smooth navigation with keyboard shortcuts (Ctrl+S, Ctrl+P, Alt+C, Alt+R, Ctrl+E)
Packaging notes (for maintainers):

- The staging script is `installer/prepare-dist.ps1`. It builds the jar, copies resources into `dist\KeyBase`, and bundles a portable JRE from `installer/packages/jre-portable` (or extracts `portable-jre.zip`) so no system Java is required.
- The Inno Setup script `installer/keybase.iss` builds the Windows installer and will include everything under `dist\KeyBase` including the bundled JRE.
- To change runtime version, replace the contents of `installer/packages/jre-portable` (or supply a new `portable-jre.zip`) before staging.

If you'd like a reproducible build or CI integration, open an issue and I can add a simple build script or GitHub Actions workflow later.
- **Smart Visibility**: Vehicle number field shows/hides based on vehicle type selection
- **Status Bar**: Real-time feedback on application actions
- **Context Menus**: Right-click options for quick actions on records
- **Camera Toggle**: Preferences now include a disable-camera option that swaps the capture pane for a splash placeholder when live video is not desired.

## Requirements

For developers:
- Java JDK 17 (LTS) for building
- PowerShell (on Windows) to run the staging script
- Inno Setup 6 (optional) to produce the installer (`ISCC.exe`)

For end users (portable distribution / installer):
- Windows 64-bit
- No Java installation needed (bundled JRE)
- Webcam hardware (optional for photos)
- ~250MB free disk space for app + JRE + data growth

## Required Libraries

Place the following JAR files in the `lib` directory:

- `h2-2.1.214.jar` (or newer) - H2 Database Engine
- `webcam-capture-0.3.12.jar` - Sarxos Webcam Capture library
- `slf4j-api-1.7.36.jar` - Required by webcam-capture
- `slf4j-simple-1.7.36.jar` - Simple SLF4J implementation
- `bridj-0.7.0.jar` - Required by webcam-capture
- `zxing-core-3.5.3.jar` - ZXing core library for QR code generation
- `zxing-javase-3.5.3.jar` - ZXing Java SE helpers (BufferedImage conversion)

## Build and Run

### Running (End User)
Double-click `KeyBase.exe` inside the distributed folder or installed directory. If the EXE is unavailable, `run.bat` will fall back and launch using the bundled JRE.

### Developer Staging
Run:
```powershell
pwsh -File installer/prepare-dist.ps1
```
This produces `dist\KeyBase` containing:
- `KeyBase.jar` (main application)
- `KeyBase.exe` (wrapped GUI launcher via Launch4j, portable JRE aware)
- `jre/` (portable Java runtime)
- Support folders: `config/`, `images/`, `resources/`, `lib/`, `data/`

### Installer Creation (Optional)
Install Inno Setup 6, then:
```powershell
& "C:\Program Files (x86)\Inno Setup 6\ISCC.exe" installer\keybase.iss
```
Generates `KeyBase-Setup.exe` in the project root or `installer` output directory.

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
5. **Save Record**: Click "Save" or press Ctrl+S to store in database
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
4. **View Images**: Select record to preview the customer photo or an automatic placeholder when no image is stored
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
- **Preferences** - Configure webcam device, image storage location, or disable camera features
- **Exit** - Close application

### Help Menu
- **Readme** - Display this help documentation
- **About** - Show application information and version

## Keyboard Shortcuts

### Main Form
- **Ctrl+F**: Open Search window
- **Ctrl+E**: Export all records to CSV
- **Ctrl+S**: Save the current record
- **Ctrl+P**: Print today's records summary
- **Alt+C**: Capture image
- **Alt+R**: Reset form
- **Enter**: Navigate to next field

### Search Window
- **Ctrl+E**: Export filtered results to CSV
- **Purpose Dropdown**: Narrow results to Personal, Commercial, Department, or Suspicious entries.
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
- **Type Filtering**: Filter by vehicle type
- **Purpose Filtering**: Use the dedicated dropdown to match personal, commercial, department, or suspicious records
- **Result Count**: Shows number of matching records in title
- **Image Preview**: See customer photos in results, with automatic fallback placeholders when no photo is available

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
- Use Ctrl+P from the main form to print a daily summary of records created today.

## Image Management

### Capturing Images
1. Click "Capture" (only shown if no current image)
2. Webcam preview dialog appears
3. Click "Capture" to take photo (stored in `images/cache` initially)
4. Preview updates immediately (staged image)
5. Optionally use "Recapture" to stage a replacement or "Delete Image" to remove staged/committed image
6. Final commit happens only when you click "Save Changes" in Edit; staging file is merged (overwriting existing image file contents or adopted as new).

### Image Storage
- Default location: `images/` directory
- Configurable in: `config/app.properties`
- Format: JPEG
- Naming: Auto-generated timestamp-based names
- Fallback placeholders automatically display when no photo is available, retaining layout consistency in search and detail views.

### Deleting Images
 - **Staged or Existing**: Click "Delete Image" in the Edit dialog (removes staged file or committed file and reverts to no-image state).
 - **Full Record Purge**: Use "Delete Data" in record details to clear record fields (retains minimal identity fields).

## Application Settings

Configure in `config/app.properties`:
```properties
webcam.device=0
images.directory=images
camera.disabled=false
```

### Configuration Options
- **webcam.device**: Webcam index (0 for default, 1+ for additional cameras)
- **images.directory**: Directory for storing captured photos
  - Relative path: `images` (in application directory)
  - Absolute path: `C:/KeyBase/images` or `/home/user/keybase/images`
- **camera.disabled**: Set to `true` to disable all live camera features and show splash placeholders instead

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

### Version 2.3 (Current)
- Bundled portable JRE (no system Java required) integrated into staging and installer
- Launch4j configuration updated to support embedded runtime path
- Image capture now uses a staging (pending) approach: commit occurs on Save to avoid accidental overwrite
- Search window enhanced with Payment filter (Any / Cash / UPI) and service type recognition via remarks
- Service and payment markers standardized through helper (`ServiceTypeHelper`)
- Larger image preview area in Edit dialog with conditional buttons: Capture vs Recapture/Delete

### Version 2.2
 - Purpose dropdown in the search window now filters by stored record purpose
 - Added Ctrl+S shortcut to save records and Ctrl+P to print today's entries
 - Randomized placeholder artwork appears when photos are missing, including a dedicated "no results" image in the search panel
 - Preferences dialog gained a camera disable switch with splash fallback support
 - Edit Record dialog preserves the previously saved purpose selection
 - QR code generation now uses the ZXing library for higher compatibility with mobile scanners

### Version 2.0
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