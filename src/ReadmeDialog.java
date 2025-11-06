package src;

import java.awt.*;
import java.nio.file.*;
import javax.swing.*;

public class ReadmeDialog extends JDialog {
    
    public ReadmeDialog(Frame owner) {
        super(owner, "KeyBase - User Guide & Documentation", true);
        setSize(900, 700);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        
        initComponents();
    }
    
    private void initComponents() {
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contentPanel.setBackground(Color.WHITE);
        
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(60, 62, 128));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("KeyBase - User Guide & Documentation");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Complete guide to using the Key Management System");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(236, 240, 241));
        
        JPanel titleTextPanel = new JPanel(new BorderLayout(5, 5));
        titleTextPanel.setOpaque(false);
        titleTextPanel.add(titleLabel, BorderLayout.NORTH);
        titleTextPanel.add(subtitleLabel, BorderLayout.CENTER);
        
        titlePanel.add(titleTextPanel, BorderLayout.CENTER);
        contentPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Create tabbed pane for organized content
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Tab 1: Quick Start
        tabbedPane.addTab("Quick Start", createQuickStartPanel());
        
        // Tab 2: Features
        tabbedPane.addTab("Features", createFeaturesPanel());
        
        // Tab 3: Usage Guide
        tabbedPane.addTab("Usage Guide", createUsagePanel());
        
        // Tab 4: Keyboard Shortcuts
        tabbedPane.addTab("Shortcuts", createShortcutsPanel());
        
        // Tab 5: Troubleshooting
        tabbedPane.addTab("Troubleshooting", createTroubleshootingPanel());
        
        // Tab 6: Full README
        tabbedPane.addTab("Full Documentation", createFullReadmePanel());
        
        contentPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("Arial", Font.BOLD, 12));
        btnClose.setBackground(new Color(109, 193, 210));
        btnClose.setForeground(new Color(60, 62, 128));
        btnClose.setFocusPainted(false);
        btnClose.setPreferredSize(new Dimension(100, 32));
        btnClose.addActionListener(e -> dispose());
        
        buttonPanel.add(btnClose);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(contentPanel);
    }
    
    private JPanel createQuickStartPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Arial", Font.PLAIN, 13));
        textArea.setMargin(new Insets(10, 10, 10, 10));
        
        textArea.setText(
            "QUICK START GUIDE\n" +
            "═══════════════════════════════════════════════════════════════\n\n" +
            "1. ADDING A NEW RECORD\n" +
            "   • Enter customer Name (required)\n" +
            "   • Enter Phone Number - exactly 10 digits (required)\n" +
            "   • Select Key Type (Bike, Car, Truck, Scooter, Auto, Machines, JCB, Hitachi)\n" +
            "   • Enter Vehicle No (shown only for vehicles)\n" +
            "   • Enter ID Number and Key No/Model\n" +
            "   • Select Key For purpose (Home, Office, Locker, Department, Suspicious)\n" +
            "   • Enter Quantity (use arrow keys to adjust)\n" +
            "   • Enter Amount charged\n" +
            "   • Add Remarks if needed\n" +
            "   • Click Capture (Alt+C) to take customer photo\n" +
            "   • Click Save (Alt+S) to store the record\n\n" +
            "2. SEARCHING RECORDS\n" +
            "   • Press Ctrl+F or File → Search Records\n" +
            "   • Select search field or use 'All Fields'\n" +
            "   • Enter search text (case-insensitive)\n" +
            "   • Apply filters: Vehicle Type, Key For, Date Range\n" +
            "   • Click Search to view results\n" +
            "   • Double-click any record to view details\n\n" +
            "3. VIEWING RECORD DETAILS\n" +
            "   • Double-click any record in the table\n" +
            "   • Or right-click → View Details\n" +
            "   • View all information with customer photo\n" +
            "   • Use Edit Details to modify the record\n" +
            "   • Use Print to print the record\n" +
            "   • Use Export CSV to save as file\n\n" +
            "4. KEYBOARD SHORTCUTS\n" +
            "   • Ctrl+F: Open Search\n" +
            "   • Ctrl+E: Export to CSV\n" +
            "   • Alt+C: Capture Image\n" +
            "   • Alt+S: Save Record\n" +
            "   • Alt+R: Reset Form\n" +
            "   • Enter: Navigate between fields\n\n" +
            "5. EXPORTING DATA\n" +
            "   • All Records: File → Export to CSV (Ctrl+E)\n" +
            "   • Filtered Results: In Search window → Export Results\n" +
            "   • Single Record: Right-click → Export Record to CSV\n\n" +
            "TIP: Use Enter key to quickly navigate through all form fields!\n"
        );
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFeaturesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Arial", Font.PLAIN, 13));
        textArea.setMargin(new Insets(10, 10, 10, 10));
        
        textArea.setText(
            "FEATURES\n" +
            "═══════════════════════════════════════════════════════════════\n\n" +
            "CORE FUNCTIONALITY\n" +
            "─────────────────────────────────────────────────────────────\n" +
            "✓ Customer Management\n" +
            "  • Store name, phone, ID, and vehicle details\n" +
            "  • Track multiple keys per customer\n" +
            "  • Maintain quantity and pricing information\n\n" +
            "✓ Key Tracking\n" +
            "  • Unique key numbers\n" +
            "  • Purpose classification (Home, Office, Locker, Department, Suspicious)\n" +
            "  • Date tracking with built-in picker\n" +
            "  • Remarks field for additional notes\n\n" +
            "✓ Image Management\n" +
            "  • Webcam integration for customer photos\n" +
            "  • Image preview in all views\n" +
            "  • Delete option before saving\n" +
            "  • JPEG format with automatic naming\n\n" +
            "ADVANCED FEATURES\n" +
            "─────────────────────────────────────────────────────────────\n" +
            "✓ Smart Search\n" +
            "  • Case-insensitive searching\n" +
            "  • Substring matching across all fields\n" +
            "  • Multiple filter criteria\n" +
            "  • Date range filtering\n" +
            "  • Result count display\n\n" +
            "✓ Record Management\n" +
            "  • View detailed record information\n" +
            "  • Edit any field with validation\n" +
            "  • Print formatted records with photos\n" +
            "  • Delete data while preserving Name and ID\n" +
            "  • Context menu for quick actions\n\n" +
            "✓ Data Export\n" +
            "  • Export all records to CSV\n" +
            "  • Export filtered search results\n" +
            "  • Export individual records\n" +
            "  • Proper CSV formatting with escaping\n\n" +
            "✓ User Experience\n" +
            "  • Full keyboard navigation\n" +
            "  • Enter key flow through all fields\n" +
            "  • Smart field visibility (vehicle number)\n" +
            "  • Status bar with real-time feedback\n" +
            "  • Mnemonic keys (Alt+C, Alt+S, etc.)\n\n" +
            "TECHNICAL FEATURES\n" +
            "─────────────────────────────────────────────────────────────\n" +
            "✓ H2 Embedded Database\n" +
            "  • No external server required\n" +
            "  • Automatic initialization\n" +
            "  • Transaction-safe operations\n" +
            "  • Persistent storage\n\n" +
            "✓ Data Validation\n" +
            "  • Phone number format (10 digits)\n" +
            "  • Required field checking\n" +
            "  • Number format validation\n" +
            "  • Date format handling\n\n" +
            "✓ Configuration\n" +
            "  • Configurable webcam device\n" +
            "  • Custom image storage location\n" +
            "  • Properties-based settings\n"
        );
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createUsagePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Arial", Font.PLAIN, 13));
        textArea.setMargin(new Insets(10, 10, 10, 10));
        
        textArea.setText(
            "DETAILED USAGE GUIDE\n" +
            "═══════════════════════════════════════════════════════════════\n\n" +
            "MAIN FORM - ADDING RECORDS\n" +
            "─────────────────────────────────────────────────────────────\n" +
            "1. Name: Customer's full name (required)\n" +
            "2. Phone Number: Exactly 10 digits, validated automatically\n" +
            "3. Key Type: SELECT, Bike, Car, Truck, Scooter, Auto, Machines, JCB, Hitachi\n" +
            "   • Vehicle No field appears for Bike, Car, Truck, Scooter, Auto, Machines, JCB, Hitachi\n" +
            "4. ID Number: Customer identification (license, Aadhar, etc.)\n" +
            "5. Key No/Model: Unique identifier for the key\n" +
            "6. Key For: Purpose of the key\n" +
            "   • Home: Residential keys\n" +
            "   • Office: Commercial/office keys\n" +
            "   • Locker: Safe/locker keys\n" +
            "   • Department: Organizational department keys\n" +
            "   • Suspicious: Keys requiring attention/investigation\n" +
            "7. Date: Click button to select, defaults to today\n" +
            "8. Quantity: Number of keys\n" +
            "   • Use up/down arrows to adjust\n" +
            "   • Or type directly\n" +
            "9. Amount: Price charged (in rupees)\n" +
            "10. Remarks: Additional notes or comments\n" +
            "11. Image Capture:\n" +
            "    • Click Capture button\n" +
            "    • Webcam preview appears\n" +
            "    • Click Capture in preview\n" +
            "    • Delete button appears if you want to retake\n" +
            "12. Save: Stores record in database\n" +
            "13. Reset: Clears form for next entry\n\n" +
            "SEARCH WINDOW\n" +
            "─────────────────────────────────────────────────────────────\n" +
            "Search Field Options:\n" +
            "  • All Fields: Searches across all text fields\n" +
            "  • Name: Customer name only\n" +
            "  • Phone: Phone number\n" +
            "  • Vehicle: Vehicle number\n" +
            "  • ID: ID number\n" +
            "  • Key: Key No/Model\n" +
            "  • Remarks: Remarks field\n\n" +
            "Filters:\n" +
            "  • Vehicle Type: Filter by Bike, Car, Truck, Scooter, Auto, Machines, JCB, Hitachi\n" +
            "  • Key For: Filter by purpose\n" +
            "  • Date From/To: Filter by date range\n\n" +
            "Search Tips:\n" +
            "  • Search is case-insensitive\n" +
            "  • Partial matches work (\"john\" finds \"Johnson\")\n" +
            "  • Combine search text with filters\n" +
            "  • Clear filters to see all results\n\n" +
            "RECORD DETAILS DIALOG\n" +
            "─────────────────────────────────────────────────────────────\n" +
            "Access: Double-click any record or right-click → View Details\n\n" +
            "Actions Available:\n" +
            "  • Edit Details: Opens edit form with current values\n" +
            "  • Delete Data: Clears all fields except Name and ID\n" +
            "    - Sets fields to \"deleted\" for tracking\n" +
            "    - Removes customer photo\n" +
            "    - Confirmation required\n" +
            "  • Print: Sends record to printer\n" +
            "    - Includes customer photo\n" +
            "    - Professional formatting\n" +
            "  • Export CSV: Saves record as CSV file\n" +
            "    - Field-value format\n" +
            "    - Choose save location\n\n" +
            "EDIT RECORD DIALOG\n" +
            "─────────────────────────────────────────────────────────────\n" +
            "  • All fields editable except ID\n" +
            "  • Name and Phone required\n" +
            "  • Same validations as new record\n" +
            "  • Image displayed but not editable\n" +
            "  • Save Changes updates database\n" +
            "  • Cancel discards changes\n\n" +
            "CONTEXT MENU (Right-click)\n" +
            "─────────────────────────────────────────────────────────────\n" +
            "Available on any record in Main Form or Search Window:\n" +
            "  • View Details: Opens detail view\n" +
            "  • Print Record: Direct print\n" +
            "  • Export Record to CSV: Export single record\n\n" +
            "EXPORT OPTIONS\n" +
            "─────────────────────────────────────────────────────────────\n" +
            "1. Export All Records:\n" +
            "   • File menu → Export to CSV (Ctrl+E)\n" +
            "   • Includes all database records\n" +
            "   • Standard CSV format\n\n" +
            "2. Export Filtered Results:\n" +
            "   • In Search window after applying filters\n" +
            "   • Exports only visible records\n" +
            "   • Shows count in success message\n\n" +
            "3. Export Single Record:\n" +
            "   • Right-click record → Export\n" +
            "   • Or in Record Details → Export CSV\n" +
            "   • Field-value format for readability\n"
        );
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createShortcutsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        textArea.setMargin(new Insets(10, 10, 10, 10));
        
        textArea.setText(
            "KEYBOARD SHORTCUTS\n" +
            "═══════════════════════════════════════════════════════════════\n\n" +
            "MAIN FORM\n" +
            "─────────────────────────────────────────────────────────────\n" +
            "  Ctrl+F         Open Search window\n" +
            "  Ctrl+E         Export all records to CSV\n" +
            "  Ctrl+V         Refresh record table\n" +
            "  Alt+C          Capture image\n" +
            "  Alt+S          Save record\n" +
            "  Alt+R          Reset form\n" +
            "  Enter          Move to next field\n\n" +
            "SEARCH WINDOW\n" +
            "─────────────────────────────────────────────────────────────\n" +
            "  Ctrl+E         Export filtered results to CSV\n" +
            "  Enter          Execute search\n" +
            "  Double-click   View record details\n" +
            "  Right-click    Show context menu\n\n" +
            "RECORD DETAILS DIALOG\n" +
            "─────────────────────────────────────────────────────────────\n" +
            "  Esc            Close dialog\n\n" +
            "EDIT RECORD DIALOG\n" +
            "─────────────────────────────────────────────────────────────\n" +
            "  Esc            Cancel editing\n" +
            "  Enter          Move to next field\n\n" +
            "FORM NAVIGATION (TAB ORDER)\n" +
            "─────────────────────────────────────────────────────────────\n" +
            "  1. Name\n" +
            "  2. Phone Number\n" +
            "  3. Key Type (Vehicle Type)\n" +
            "  4. Vehicle No (if visible)\n" +
            "     or ID No (if vehicle hidden)\n" +
            "  5. ID Number\n" +
            "  6. Key No/Model\n" +
            "  7. Key For\n" +
            "  8. Remarks\n" +
            "  9. Quantity\n" +
            " 10. Amount\n" +
            " 11. Date\n" +
            " 12. Capture Image\n" +
            " 13. Save Button\n\n" +
            "TIPS\n" +
            "─────────────────────────────────────────────────────────────\n" +
            "  • Use Enter key for fastest data entry\n" +
            "  • Arrow keys work in Quantity spinner\n" +
            "  • Right-click for quick access to common actions\n" +
            "  • Mnemonic keys (Alt+letter) work from anywhere\n" +
            "  • Tab key also works for navigation\n"
        );
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createTroubleshootingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Arial", Font.PLAIN, 13));
        textArea.setMargin(new Insets(10, 10, 10, 10));
        
        textArea.setText(
            "TROUBLESHOOTING GUIDE\n" +
            "═══════════════════════════════════════════════════════════════\n\n" +
            "APPLICATION WON'T START\n" +
            "─────────────────────────────────────────────────────────────\n" +
            "Problem: Application doesn't launch\n" +
            "Solutions:\n" +
            "  1. Check Java installation: Open terminal/cmd and type:\n" +
            "     java -version\n" +
            "  2. Verify all JAR files in lib/ directory:\n" +
            "     • h2-2.1.214.jar\n" +
            "     • webcam-capture-0.3.12.jar\n" +
            "     • slf4j-api-1.7.36.jar\n" +
            "     • slf4j-simple-1.7.36.jar\n" +
            "     • bridj-0.7.0.jar\n" +
            "  3. Try manual launch:\n" +
            "     java -cp \"lib/*;build\\classes\" src.KeyBase\n" +
            "  4. Check console for error messages\n\n" +
            "WEBCAM ISSUES\n" +
            "─────────────────────────────────────────────────────────────\n" +
            "Problem: Webcam not detected or not working\n" +
            "Solutions:\n" +
            "  1. Check webcam connection\n" +
            "  2. Test webcam with other applications\n" +
            "  3. Update webcam drivers\n" +
            "  4. Check OS permissions for camera access\n" +
            "  5. Try different webcam device:\n" +
            "     • Open config/app.properties\n" +
            "     • Change: webcam.device=0 to webcam.device=1\n" +
            "  6. Restart application after changes\n\n" +
            "Problem: Captured images are black or distorted\n" +
            "Solutions:\n" +
            "  1. Ensure good lighting\n" +
            "  2. Check webcam lens is clean\n" +
            "  3. Try different webcam device setting\n" +
            "  4. Update webcam firmware\n\n" +
            "DATABASE ERRORS\n" +
            "─────────────────────────────────────────────────────────────\n" +
            "Problem: Cannot save records or database errors\n" +
            "Solutions:\n" +
            "  1. Check disk space availability\n" +
            "  2. Verify write permissions in application directory\n" +
            "  3. Reset database (WARNING: Deletes all data):\n" +
            "     • Close application\n" +
            "     • Delete files in data/ directory\n" +
            "     • Restart application\n" +
            "  4. Check database files aren't read-only\n\n" +
            "Problem: Search returns no results\n" +
            "Solutions:\n" +
            "  1. Verify records exist in main table\n" +
            "  2. Clear all search filters\n" +
            "  3. Try searching \"All Fields\" with simple text\n" +
            "  4. Check database connection\n" +
            "  5. Restart application\n\n" +
            "IMAGE STORAGE ISSUES\n" +
            "─────────────────────────────────────────────────────────────\n" +
            "Problem: Cannot save captured images\n" +
            "Solutions:\n" +
            "  1. Check images/ directory exists\n" +
            "  2. Verify write permissions\n" +
            "  3. Check disk space\n" +
            "  4. Try different location:\n" +
            "     • Edit config/app.properties\n" +
            "     • Change: images.directory=C:/KeyBase/images\n" +
            "  5. Create directory manually if needed\n\n" +
            "Problem: Images not displaying\n" +
            "Solutions:\n" +
            "  1. Verify image file exists at path\n" +
            "  2. Check file permissions\n" +
            "  3. Ensure image format is JPEG\n" +
            "  4. Check image path in database is correct\n\n" +
            "VALIDATION ERRORS\n" +
            "─────────────────────────────────────────────────────────────\n" +
            "Problem: \"Phone number must be 10 digits\"\n" +
            "Solution: Enter exactly 10 numeric digits, no spaces or dashes\n\n" +
            "Problem: \"Name is required\"\n" +
            "Solution: Name field cannot be empty\n\n" +
            "Problem: Cannot save amount\n" +
            "Solution: Use numbers and decimal point only (e.g., 150.50)\n\n" +
            "EXPORT ISSUES\n" +
            "─────────────────────────────────────────────────────────────\n" +
            "Problem: CSV export fails\n" +
            "Solutions:\n" +
            "  1. Check write permissions in destination folder\n" +
            "  2. Close CSV file if already open in Excel\n" +
            "  3. Choose different save location\n" +
            "  4. Check disk space\n\n" +
            "Problem: CSV file corrupted or unreadable\n" +
            "Solutions:\n" +
            "  1. Try opening with different program (Excel, Notepad)\n" +
            "  2. Export again\n" +
            "  3. Check for special characters in data\n\n" +
            "PRINT ISSUES\n" +
            "─────────────────────────────────────────────────────────────\n" +
            "Problem: Print dialog doesn't appear\n" +
            "Solutions:\n" +
            "  1. Check printer is connected\n" +
            "  2. Verify printer drivers installed\n" +
            "  3. Check printer is set as default\n" +
            "  4. Try printing from another application\n\n" +
            "Problem: Image doesn't print\n" +
            "Solutions:\n" +
            "  1. Verify image file exists\n" +
            "  2. Check printer supports graphics\n" +
            "  3. Try different printer\n\n" +
            "PERFORMANCE ISSUES\n" +
            "─────────────────────────────────────────────────────────────\n" +
            "Problem: Application runs slowly\n" +
            "Solutions:\n" +
            "  1. Close other applications\n" +
            "  2. Check available RAM (minimum 2GB recommended)\n" +
            "  3. Reduce image size/resolution\n" +
            "  4. Archive old records periodically\n\n" +
            "NEED MORE HELP?\n" +
            "─────────────────────────────────────────────────────────────\n" +
            "If issues persist:\n" +
            "  1. Check TROUBLESHOOTING.md for detailed solutions\n" +
            "  2. Review console output for error messages\n" +
            "  3. Check Java version compatibility (8 or higher)\n" +
            "  4. Verify all system requirements are met\n"
        );
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFullReadmePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        textArea.setMargin(new Insets(10, 10, 10, 10));
        
        // Load README.md content
        try {
            String readmePath = "README.md";
            String content = new String(Files.readAllBytes(Paths.get(readmePath)));
            textArea.setText(content);
            textArea.setCaretPosition(0);
        } catch (Exception e) {
            textArea.setText(
                "Unable to load README.md file.\n\n" +
                "Please ensure README.md exists in the application directory.\n\n" +
                "Error: " + e.getMessage()
            );
        }
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
}
