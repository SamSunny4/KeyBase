package src;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class SearchWindow extends JFrame {
    private JTextField txtSearch;
    private JComboBox<String> cmbSearchField;
    private JComboBox<String> cmbVehicleKeyType;
    private JComboBox<String> cmbKeyType;
    private JComboBox<String> cmbServiceType;
    private JComboBox<String> cmbPayment;
    private JTable tblResults;
    private JLabel lblImagePreview;
    private DefaultTableModel tableModel;
    private JDateChooser dateFromChooser;
    private JDateChooser dateToChooser;
    private static final int IMAGE_PREVIEW_SIZE = 250;
    
    public SearchWindow() {
        setTitle("Search Key Records");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Set application icon
        setAppIcon();
        
        initComponents();
    }
    
    private void setAppIcon() {
        try {
            File logoFile = new File("resources/splash.png");
            if (logoFile.exists()) {
                ImageIcon icon = new ImageIcon(logoFile.getAbsolutePath());
                setIconImage(icon.getImage());
            }
        } catch (Exception e) {
            // Icon not critical, ignore if fails
        }
    }
    
    private void initComponents() {
        // Main layout
        setLayout(new BorderLayout(10, 10));
        
        // Search Panel
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(109, 193, 210), 2),
                "Search Criteria",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                new Color(60, 62, 128)
            )
        ));
        searchPanel.setBackground(Color.WHITE);
        
    GridBagConstraints gbc = new GridBagConstraints();
    // Reduce horizontal inset so labels and controls sit closer together
    gbc.insets = new Insets(8, 4, 8, 4);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Search field selector
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblSearchBy = new JLabel("Search by:");
        lblSearchBy.setFont(new Font("Arial", Font.BOLD, 12));
        searchPanel.add(lblSearchBy, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
    cmbSearchField = new JComboBox<>(new String[] {"All Fields", "Name", "Phone Number", "Vehicle Number", "ID Number", "Key No/Model", "Remarks"});
        cmbSearchField.setPreferredSize(new Dimension(150, 30));
        cmbSearchField.setBackground(new Color(250, 250, 250));
        cmbSearchField.setForeground(new Color(60, 62, 128));
        cmbSearchField.setFont(new Font("Arial", Font.PLAIN, 12));
        cmbSearchField.setBorder(BorderFactory.createLineBorder(new Color(109, 193, 210), 1));
        cmbSearchField.setToolTipText("Field to search in");
        searchPanel.add(cmbSearchField, gbc);
        
        // Search text field
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        txtSearch = new JTextField(25);
        txtSearch.setPreferredSize(new Dimension(300, 30));
        txtSearch.setBackground(new Color(250, 250, 250));
        txtSearch.setForeground(new Color(60, 62, 128));
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 12));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(109, 193, 210), 1),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        txtSearch.setToolTipText("Enter search text and press Enter to search");
        searchPanel.add(txtSearch, gbc);
        
        // Search button
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        JButton btnSearch = new JButton("Search");
        btnSearch.setPreferredSize(new Dimension(100, 30));
        btnSearch.setBackground(new Color(109, 193, 210));
        btnSearch.setForeground(new Color(60, 62, 128));
        btnSearch.setFont(new Font("Arial", Font.BOLD, 12));
        btnSearch.setFocusPainted(false);
        btnSearch.setToolTipText("Search records (Enter)");
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
        searchPanel.add(btnSearch, gbc);

        // Enter key on the search field triggers the search
        txtSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
        
        // Filter by purpose
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblKeyFor = new JLabel("Purpose:");
        lblKeyFor.setFont(new Font("Arial", Font.BOLD, 12));
        searchPanel.add(lblKeyFor, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        cmbKeyType = new JComboBox<>(new String[] {"Any", "Personal", "Commercial", "Department", "Suspicious"});
        cmbKeyType.setPreferredSize(new Dimension(150, 30));
        cmbKeyType.setBackground(new Color(250, 250, 250));
        cmbKeyType.setForeground(new Color(60, 62, 128));
        cmbKeyType.setFont(new Font("Arial", Font.PLAIN, 12));
        cmbKeyType.setBorder(BorderFactory.createLineBorder(new Color(109, 193, 210), 1));
        cmbKeyType.setToolTipText("Filter by key purpose");
        searchPanel.add(cmbKeyType, gbc);
        
        // Filter by vehicle key type (made smaller)
        gbc.gridx = 2;
        gbc.gridy = 1;
        JLabel lblKeyType = new JLabel("Key Type:");
        lblKeyType.setFont(new Font("Arial", Font.BOLD, 12));
        searchPanel.add(lblKeyType, gbc);
        
    gbc.gridx = 3;
    gbc.gridy = 1;
    gbc.gridwidth = 1;
    // Ensure this column/component does not expand to fill available space
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0.0;
    cmbVehicleKeyType = new JComboBox<>(new String[] {"Any", "Bike", "Car", "Truck", "Scooter", "Auto", "Machines", "JCB", "Hitachi"});
    // Make vehicle key type smaller (150px) to occupy less horizontal space
    cmbVehicleKeyType.setPreferredSize(new Dimension(150, 30));
    // Prevent unwanted stretching by capping maximum size (helps with some LAFs)
    cmbVehicleKeyType.setMaximumSize(cmbVehicleKeyType.getPreferredSize());
        cmbVehicleKeyType.setBackground(new Color(250, 250, 250));
        cmbVehicleKeyType.setForeground(new Color(60, 62, 128));
        cmbVehicleKeyType.setFont(new Font("Arial", Font.PLAIN, 12));
        cmbVehicleKeyType.setBorder(BorderFactory.createLineBorder(new Color(109, 193, 210), 1));
        cmbVehicleKeyType.setToolTipText("Filter by vehicle key type");
        searchPanel.add(cmbVehicleKeyType, gbc);
        
    // Filter by service type (label + dropdown)
    gbc.gridx = 4;
    gbc.gridy = 1;
    JLabel lblServiceType = new JLabel("Service Type:");
    lblServiceType.setFont(new Font("Arial", Font.BOLD, 12));
    searchPanel.add(lblServiceType, gbc);

    gbc.gridx = 5;
    gbc.gridy = 1;
    // Place service combo immediately after Key Type and style it similar to other filters
    cmbServiceType = new JComboBox<>(new String[] {"Any", "Duplicate", "In-shop", "On-site"});
    // Match preferred size to vehicle key type (small) so controls align
    cmbServiceType.setPreferredSize(cmbVehicleKeyType.getPreferredSize());
    cmbServiceType.setMaximumSize(cmbServiceType.getPreferredSize());
    cmbServiceType.setBackground(cmbKeyType.getBackground());
    cmbServiceType.setForeground(cmbKeyType.getForeground());
    cmbServiceType.setFont(cmbKeyType.getFont());
    cmbServiceType.setBorder(cmbKeyType.getBorder());
    cmbServiceType.setToolTipText("Filter by service type");
    // Prevent this component from stretching
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0.0;
    searchPanel.add(cmbServiceType, gbc);
    // Restore fill/weightx for subsequent components
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
        
    // Date range filter
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel lblDateFrom = new JLabel("Date From:");
        lblDateFrom.setFont(new Font("Arial", Font.BOLD, 12));
        searchPanel.add(lblDateFrom, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        dateFromChooser = new JDateChooser();
        dateFromChooser.setPreferredSize(new Dimension(150, 30));
        dateFromChooser.setBackground(new Color(250, 250, 250));
        dateFromChooser.setForeground(new Color(60, 62, 128));
        dateFromChooser.setBorder(BorderFactory.createLineBorder(new Color(109, 193, 210), 1));
        dateFromChooser.setToolTipText("Start date for filtering (yyyy-MM-dd)");
        searchPanel.add(dateFromChooser, gbc);
        
        gbc.gridx = 2;
        gbc.gridy = 2;
        JLabel lblDateTo = new JLabel("Date To:");
        lblDateTo.setFont(new Font("Arial", Font.BOLD, 12));
        searchPanel.add(lblDateTo, gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 2;
        dateToChooser = new JDateChooser();
        dateToChooser.setPreferredSize(new Dimension(150, 30));
        dateToChooser.setBackground(new Color(250, 250, 250));
        dateToChooser.setForeground(new Color(60, 62, 128));
        dateToChooser.setBorder(BorderFactory.createLineBorder(new Color(109, 193, 210), 1));
        dateToChooser.setToolTipText("End date for filtering (yyyy-MM-dd)");
        searchPanel.add(dateToChooser, gbc);

    // Payment filter (placed on the same row as Service Type)
    gbc.gridx = 6;
    gbc.gridy = 1;
    JLabel lblPayment = new JLabel("Payment:");
    lblPayment.setFont(new Font("Arial", Font.BOLD, 12));
    searchPanel.add(lblPayment, gbc);

    gbc.gridx = 7;
    gbc.gridy = 1;
    cmbPayment = new JComboBox<>(new String[] {"Any", "Cash", "UPI"});
    // Match size and style of other small combo boxes
    cmbPayment.setPreferredSize(new Dimension(150, 30));
    cmbPayment.setMaximumSize(cmbPayment.getPreferredSize());
    cmbPayment.setBackground(new Color(250, 250, 250));
    cmbPayment.setForeground(new Color(60, 62, 128));
    cmbPayment.setFont(new Font("Arial", Font.PLAIN, 12));
    cmbPayment.setBorder(BorderFactory.createLineBorder(new Color(109, 193, 210), 1));
    cmbPayment.setToolTipText("Filter by payment method");
    // Prevent stretching
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0.0;
    searchPanel.add(cmbPayment, gbc);
    // Restore for later
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
        
    // Clear filters button (placed on same row as Search)
    gbc.gridx = 5;
    gbc.gridy = 0;
    JButton btnClear = new JButton("Clear Filters");
    btnClear.setPreferredSize(new Dimension(100, 30));
    btnClear.setBackground(new Color(109, 193, 210));
    btnClear.setForeground(new Color(60, 62, 128));
    btnClear.setFont(new Font("Arial", Font.BOLD, 12));
    btnClear.setFocusPainted(false);
    btnClear.setToolTipText("Reset all filters");
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFilters();
            }
        });
        searchPanel.add(btnClear, gbc);
        
        // Add search panel to top
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        topContainer.setBackground(Color.WHITE);
        topContainer.add(searchPanel, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);
        
        // Split pane for results and image preview
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(850);
        splitPane.setDividerSize(8);
        splitPane.setContinuousLayout(true);
        
        // Table for results
    String[] columnNames = {"SN", "Name", "Phone", "Vehicle No", "Key No/Model", "Key Type", "Purpose", "ID No", "Date", "Remarks", "Quantity", "Amount"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            private final Class<?>[] columnTypes = new Class<?>[] {
                Integer.class, String.class, String.class, String.class, String.class, String.class,
                String.class, String.class, String.class, String.class, Integer.class, String.class
            };

            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex >= 0 && columnIndex < columnTypes.length) {
                    return columnTypes[columnIndex];
                }
                return Object.class;
            }
        };
        
        tblResults = new JTable(tableModel);
        tblResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblResults.setRowHeight(28);
        tblResults.setFont(new Font("Arial", Font.PLAIN, 12));
        tblResults.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        tblResults.getTableHeader().setBackground(new Color(60, 62, 128));
        tblResults.getTableHeader().setForeground(Color.BLACK);
        tblResults.getTableHeader().setPreferredSize(new Dimension(tblResults.getTableHeader().getWidth(), 35));
        tblResults.setSelectionBackground(new Color(109, 193, 210, 100));
        tblResults.setSelectionForeground(new Color(60, 62, 128));
        tblResults.setGridColor(new Color(220, 220, 220));
        tblResults.setShowGrid(true);
        tblResults.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblResults.getSelectedRow() != -1) {
                displaySelectedImage();
            }
        });
        
        // Add double-click listener to show record details
        tblResults.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblResults.getSelectedRow() != -1) {
                    int selectedRow = tblResults.getSelectedRow();
                    int id = (Integer) tblResults.getValueAt(selectedRow, 0);
                    showRecordDetails(id);
                }
            }
        });
        
        // Right-click context menu for selected record
        JPopupMenu contextMenu = new JPopupMenu();
        
        JMenuItem viewDetailsItem = new JMenuItem("View Details");
        viewDetailsItem.addActionListener(e -> {
            int row = tblResults.getSelectedRow();
            if (row >= 0) {
                int id = (Integer) tblResults.getValueAt(row, 0);
                showRecordDetails(id);
            }
        });
        
        JMenuItem printRecordItem = new JMenuItem("Print Record");
        printRecordItem.addActionListener(e -> {
            int row = tblResults.getSelectedRow();
            if (row >= 0) {
                int id = (Integer) tblResults.getValueAt(row, 0);
                printSelectedRecord(id);
            }
        });
        
        JMenuItem exportRecordItem = new JMenuItem("Export Record to CSV");
        exportRecordItem.addActionListener(e -> {
            int row = tblResults.getSelectedRow();
            if (row >= 0) {
                int id = (Integer) tblResults.getValueAt(row, 0);
                exportSelectedRecord(id);
            }
        });
        
        contextMenu.add(viewDetailsItem);
        contextMenu.addSeparator();
        contextMenu.add(printRecordItem);
        contextMenu.add(exportRecordItem);
        
        tblResults.setComponentPopupMenu(contextMenu);
        
        // Add row sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        tblResults.setRowSorter(sorter);
        
        JScrollPane tableScrollPane = new JScrollPane(tblResults);
        tableScrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 10, 10, 5),
            BorderFactory.createLineBorder(new Color(109, 193, 210), 1)
        ));
        splitPane.setLeftComponent(tableScrollPane);
        
        // Image preview panel
        JPanel imagePanel = new JPanel(new BorderLayout(5, 5));
        imagePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 10, 10),
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(109, 193, 210), 2),
                "Image Preview",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                new Color(60, 62, 128)
            )
        ));
        imagePanel.setBackground(Color.WHITE);
        
        lblImagePreview = new JLabel("No Image Selected", SwingConstants.CENTER);
        lblImagePreview.setFont(new Font("Arial", Font.ITALIC, 12));
        lblImagePreview.setForeground(Color.GRAY);
        lblImagePreview.setPreferredSize(new Dimension(220, 220));
        lblImagePreview.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        imagePanel.add(lblImagePreview, BorderLayout.CENTER);
        
        JButton btnExport = new JButton("Export Results to Excel");
        btnExport.setPreferredSize(new Dimension(200, 35));
        btnExport.setBackground(new Color(109, 193, 210));
        btnExport.setForeground(new Color(60, 62, 128));
        btnExport.setFont(new Font("Arial", Font.BOLD, 12));
        btnExport.setFocusPainted(false);
        btnExport.setToolTipText("Export current search results to Excel (Ctrl+E)");
        btnExport.setMnemonic(KeyEvent.VK_E);
        btnExport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportResultsToExcel();
            }
        });
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        exportPanel.setBackground(Color.WHITE);
        exportPanel.add(btnExport);
        imagePanel.add(exportPanel, BorderLayout.SOUTH);

    showNoResultsImage();

        // Keyboard shortcut for export (Ctrl+E)
        KeyStroke ksExport = KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ksExport, "exportResults");
        getRootPane().getActionMap().put("exportResults", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportResultsToExcel();
            }
        });

        // Set default focus
        SwingUtilities.invokeLater(() -> txtSearch.requestFocusInWindow());
        
        splitPane.setRightComponent(imagePanel);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private void performSearch() {
        tableModel.setRowCount(0); // Clear existing results
        lblImagePreview.setIcon(null);
        lblImagePreview.setText("No Image Selected");
        
        String searchText = txtSearch.getText().trim();
        String searchField = (String) cmbSearchField.getSelectedItem();
    String purposeFilter = (String) cmbKeyType.getSelectedItem();
    String keyTypeFilter = (String) cmbVehicleKeyType.getSelectedItem();
    String serviceTypeFilter = (String) cmbServiceType.getSelectedItem();
    String paymentFilter = cmbPayment != null ? (String) cmbPayment.getSelectedItem() : "Any";
        java.util.Date dateFrom = dateFromChooser.getDate();
        java.util.Date dateTo = dateToChooser.getDate();
        
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT * FROM duplicator WHERE 1=1 ");
        
        if (!searchText.isEmpty()) {
            switch(searchField) {
                case "Name":
                    queryBuilder.append("AND LOWER(name) LIKE LOWER(?) ");
                    break;
                case "Phone Number":
                    queryBuilder.append("AND LOWER(phone_number) LIKE LOWER(?) ");
                    break;
                case "Vehicle Number":
                    queryBuilder.append("AND LOWER(vehicle_no) LIKE LOWER(?) ");
                    break;
                case "ID Number":
                    queryBuilder.append("AND LOWER(id_no) LIKE LOWER(?) ");
                    break;
                case "Key No/Model":
                    queryBuilder.append("AND LOWER(key_no) LIKE LOWER(?) ");
                    break;
                case "Remarks":
                    queryBuilder.append("AND LOWER(remarks) LIKE LOWER(?) ");
                    break;
                default: // All Fields
                    queryBuilder.append("AND (LOWER(name) LIKE LOWER(?) OR LOWER(phone_number) LIKE LOWER(?) OR LOWER(vehicle_no) LIKE LOWER(?) OR LOWER(id_no) LIKE LOWER(?) OR LOWER(key_no) LIKE LOWER(?) OR LOWER(remarks) LIKE LOWER(?)) ");
                    break;
            }
        }
        
        if (!"Any".equals(purposeFilter)) {
            queryBuilder.append("AND purpose = ? ");
        }

        if (!"Any".equals(keyTypeFilter)) {
            queryBuilder.append("AND key_type = ? ");
        }
        
        if (!"Any".equals(serviceTypeFilter)) {
            switch (serviceTypeFilter) {
                case "Duplicate":
                    queryBuilder.append("AND (remarks IS NULL OR remarks = '' OR (LOWER(remarks) NOT LIKE '%")
                        .append(ServiceTypeHelper.IN_SHOP_KEYWORD)
                        .append("%' AND LOWER(remarks) NOT LIKE '%")
                        .append(ServiceTypeHelper.ON_SITE_KEYWORD)
                        .append("%')) ");
                    break;
                case "In-shop":
                    queryBuilder.append("AND LOWER(remarks) LIKE '%")
                        .append(ServiceTypeHelper.IN_SHOP_KEYWORD)
                        .append("%' ");
                    break;
                case "On-site":
                    queryBuilder.append("AND LOWER(remarks) LIKE '%")
                        .append(ServiceTypeHelper.ON_SITE_KEYWORD)
                        .append("%' ");
                    break;
            }
        }
        
        if ("UPI".equals(paymentFilter)) {
            queryBuilder.append("AND LOWER(remarks) LIKE '%")
                .append(ServiceTypeHelper.UPI_KEYWORD)
                .append("%' ");
        } else if ("Cash".equals(paymentFilter)) {
            queryBuilder.append("AND (remarks IS NULL OR remarks = '' OR LOWER(remarks) NOT LIKE '%")
                .append(ServiceTypeHelper.UPI_KEYWORD)
                .append("%') ");
        }

        if (dateFrom != null) {
            queryBuilder.append("AND date_added >= ? ");
        }
        
        if (dateTo != null) {
            queryBuilder.append("AND date_added <= ? ");
        }
        
        queryBuilder.append("ORDER BY duplicator_id DESC");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {
            
            int paramIndex = 1;
            
            if (!searchText.isEmpty()) {
                String searchPattern = "%" + searchText + "%";
                
                if (searchField.equals("All Fields")) {
                    pstmt.setString(paramIndex++, searchPattern);
                    pstmt.setString(paramIndex++, searchPattern);
                    pstmt.setString(paramIndex++, searchPattern);
                    pstmt.setString(paramIndex++, searchPattern);
                    pstmt.setString(paramIndex++, searchPattern);
                    pstmt.setString(paramIndex++, searchPattern);
                } else {
                    pstmt.setString(paramIndex++, searchPattern);
                }
            }
            
            if (!"Any".equals(purposeFilter)) {
                pstmt.setString(paramIndex++, purposeFilter);
            }

            if (!"Any".equals(keyTypeFilter)) {
                pstmt.setString(paramIndex++, keyTypeFilter);
            }
            
            if (dateFrom != null) {
                pstmt.setDate(paramIndex++, new java.sql.Date(dateFrom.getTime()));
            }
            
            if (dateTo != null) {
                pstmt.setDate(paramIndex++, new java.sql.Date(dateTo.getTime()));
            }
            
            ResultSet rs = pstmt.executeQuery();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            
            while (rs.next()) {
                java.sql.Date dateAdded = rs.getDate("date_added");
                String dateStr = (dateAdded != null) ? dateFormat.format(dateAdded) : "";
                
                Object[] row = {
                    rs.getInt("duplicator_id"),
                    rs.getString("name"),
                    rs.getString("phone_number"),
                    rs.getString("vehicle_no"),
                    rs.getString("key_no"),
                    rs.getString("key_type"),
                    rs.getString("purpose"),
                    rs.getString("id_no"),
                    dateStr,
                    rs.getString("remarks"),
                    rs.getInt("quantity"),
                    String.format("%.2f", rs.getDouble("amount"))
                };
                tableModel.addRow(row);
            }
            
            int resultCount = tableModel.getRowCount();
            if (resultCount > 0) {
                tblResults.setRowSelectionInterval(0, 0);
                // Update window title with result count
                setTitle("Search Key Records - " + resultCount + " record(s) found");
            } else {
                setTitle("Search Key Records - No results");
                JOptionPane.showMessageDialog(this, 
                    "No records found matching your criteria.", 
                    "Search Results", 
                    JOptionPane.INFORMATION_MESSAGE);
                showNoResultsImage();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error searching records: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearFilters() {
        txtSearch.setText("");
        cmbSearchField.setSelectedIndex(0);
        cmbKeyType.setSelectedIndex(0);
        cmbVehicleKeyType.setSelectedIndex(0);
        cmbServiceType.setSelectedIndex(0);
        if (cmbPayment != null) cmbPayment.setSelectedIndex(0);
        dateFromChooser.setDate(null);
        dateToChooser.setDate(null);
    }
    
    private void displaySelectedImage() {
        int row = tblResults.getSelectedRow();
        if (row >= 0) {
            // Convert view row index to model row index
            int modelRow = tblResults.convertRowIndexToModel(row);
            
            // Get the ID of the selected record
            int id = (int) tableModel.getValueAt(modelRow, 0);
            
            try {
                Duplicator duplicator = Duplicator.findById(id);
                if (duplicator != null) {
                    String imagePath = duplicator.getImagePath();
                    if (imagePath != null && !imagePath.trim().isEmpty()) {
                        try {
                            File imageFile = new File(imagePath);
                            if (imageFile.exists()) {
                                BufferedImage img = ImageIO.read(imageFile);
                                Image scaledImg = img.getScaledInstance(IMAGE_PREVIEW_SIZE, IMAGE_PREVIEW_SIZE, Image.SCALE_SMOOTH);
                                lblImagePreview.setIcon(new ImageIcon(scaledImg));
                                lblImagePreview.setText("");
                                return;
                            }
                        } catch (Exception e) {
                            // Fall through and display placeholder image
                        }
                    }
                }
                showRandomPlaceholderImage();
            } catch (Exception e) {
                showRandomPlaceholderImage();
            }
        }
    }

    private void showRandomPlaceholderImage() {
        ImageIcon placeholderIcon = ImagePlaceholderHelper.loadRandomPlaceholder(IMAGE_PREVIEW_SIZE, IMAGE_PREVIEW_SIZE);
        if (placeholderIcon != null) {
            lblImagePreview.setIcon(placeholderIcon);
            lblImagePreview.setText("");
        } else {
            lblImagePreview.setIcon(null);
            lblImagePreview.setText("No image available");
        }
    }

    private void showNoResultsImage() {
        ImageIcon placeholderIcon = ImagePlaceholderHelper.loadNoResultsPlaceholder(IMAGE_PREVIEW_SIZE, IMAGE_PREVIEW_SIZE);
        if (placeholderIcon != null) {
            lblImagePreview.setIcon(placeholderIcon);
            lblImagePreview.setText("");
        } else {
            lblImagePreview.setIcon(null);
            lblImagePreview.setText("No image available");
        }
    }
    
    private void exportResultsToExcel() {
        int visibleRowCount = tblResults.getRowCount();
        if (visibleRowCount == 0) {
            JOptionPane.showMessageDialog(this,
                "No data to export.",
                "Export Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Filtered Results to Excel");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setSelectedFile(new File("search_results.xlsx"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filePath = ensureXlsxExtension(file.getAbsolutePath());

            List<String> fieldKeys = AppConfig.getExportFields();
            List<ExportField> selectedFields = ExportField.resolve(fieldKeys);
            if (selectedFields.isEmpty()) {
                selectedFields = new ArrayList<>(Arrays.asList(ExportField.values()));
            }

            List<SimpleXlsxExporter.ColumnSpec> columns = new ArrayList<>(selectedFields.size());
            for (ExportField field : selectedFields) {
                columns.add(new SimpleXlsxExporter.ColumnSpec(field.getHeader(), field.getWidth(), field.getCellType()));
            }

            List<List<Object>> rows = new ArrayList<>(visibleRowCount);
            for (int viewRow = 0; viewRow < visibleRowCount; viewRow++) {
                int modelRow = tblResults.convertRowIndexToModel(viewRow);
                List<Object> row = new ArrayList<>(selectedFields.size());
                for (ExportField field : selectedFields) {
                    row.add(resolveTableValue(field, modelRow));
                }
                rows.add(row);
            }

            SimpleXlsxExporter.Orientation orientation = SimpleXlsxExporter.parseOrientation(AppConfig.getExportOrientation());

            try {
                SimpleXlsxExporter.export(filePath, "Search Results", columns, rows, orientation);
                JOptionPane.showMessageDialog(this,
                    "Filtered results exported successfully!\n" +
                    visibleRowCount + " record(s) exported to:\n" + filePath,
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error exporting data: " + ex.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String ensureXlsxExtension(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "export.xlsx";
        }
        if (path.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            return path;
        }
        return path + ".xlsx";
    }

    private String safeString(Object value) {
        return value == null ? "" : value.toString();
    }

    private BigDecimal toAmount(Object value) {
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).setScale(2, RoundingMode.HALF_UP);
        }
        if (value instanceof Number) {
            BigDecimal decimal = BigDecimal.valueOf(((Number) value).doubleValue());
            return decimal.setScale(2, RoundingMode.HALF_UP);
        }
        if (value instanceof String) {
            String sanitized = ((String) value).replace(",", "").trim();
            if (sanitized.isEmpty()) {
                return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }
            try {
                BigDecimal decimal = new BigDecimal(sanitized);
                return decimal.setScale(2, RoundingMode.HALF_UP);
            } catch (NumberFormatException ex) {
                return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private Object resolveTableValue(ExportField field, int modelRow) {
        return switch (field) {
            case ID -> tableModel.getValueAt(modelRow, 0);
            case NAME -> safeString(tableModel.getValueAt(modelRow, 1));
            case PHONE -> safeString(tableModel.getValueAt(modelRow, 2));
            case KEY_NO -> safeString(tableModel.getValueAt(modelRow, 4));
            case VEHICLE_NO -> safeString(tableModel.getValueAt(modelRow, 3));
            case KEY_TYPE -> safeString(tableModel.getValueAt(modelRow, 5));
            case PURPOSE -> safeString(tableModel.getValueAt(modelRow, 6));
            case DATE -> safeString(tableModel.getValueAt(modelRow, 8));
            case ID_NO -> safeString(tableModel.getValueAt(modelRow, 7));
            case REMARKS -> safeString(tableModel.getValueAt(modelRow, 9));
            case QUANTITY -> {
                Object value = tableModel.getValueAt(modelRow, 10);
                if (value instanceof Number number) {
                    yield number.intValue();
                }
                if (value instanceof String text) {
                    try {
                        yield Integer.parseInt(text.trim());
                    } catch (NumberFormatException ignored) {
                        yield 0;
                    }
                }
                yield 0;
            }
            case AMOUNT -> {
                Object value = tableModel.getValueAt(modelRow, 11);
                yield toAmount(value);
            }
        };
    }
    
    private void showRecordDetails(int id) {
        try {
            Duplicator d = Duplicator.findById(id);
            if (d == null) {
                JOptionPane.showMessageDialog(this, "Record not found.", "Details", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            RecordDetailsDialog dialog = new RecordDetailsDialog(this, d);
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error loading record details: " + ex.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void printSelectedRecord(int id) {
        try {
            Duplicator d = Duplicator.findById(id);
            if (d == null) {
                JOptionPane.showMessageDialog(this, "Record not found.", "Print Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
            job.setPrintable(new RecordDetailsDialog(this, d));
            
            if (job.printDialog()) {
                try {
                    job.print();
                    JOptionPane.showMessageDialog(this,
                        "Record sent to printer successfully!",
                        "Print Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (java.awt.print.PrinterException pe) {
                    JOptionPane.showMessageDialog(this,
                        "Error printing record: " + pe.getMessage(),
                        "Print Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error printing record: " + ex.getMessage(), 
                "Print Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportSelectedRecord(int id) {
        try {
            Duplicator d = Duplicator.findById(id);
            if (d == null) {
                JOptionPane.showMessageDialog(this, "Record not found.", "Export Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Record to CSV");
            fileChooser.setSelectedFile(new File("record_" + id + ".csv"));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String filePath = file.getAbsolutePath();
                
                if (!filePath.toLowerCase().endsWith(".csv")) {
                    filePath += ".csv";
                }
                
                try (java.io.FileWriter writer = new java.io.FileWriter(filePath)) {
                    // Write header
                    writer.append("Field,Value\n");
                    
                    // Write data
                    writer.append("SN,").append(String.valueOf(d.getDuplicatorId())).append("\n");
                    writer.append("Name,").append(escapeCsv(d.getName())).append("\n");
                    writer.append("Phone Number,").append(escapeCsv(d.getPhoneNumber())).append("\n");
                    writer.append("ID Number,").append(escapeCsv(d.getIdNo())).append("\n");
                    writer.append("Key No/Model,").append(escapeCsv(d.getKeyNo())).append("\n");
                    
                    String purpose = d.getPurpose();
                    writer.append("Purpose,").append(escapeCsv(purpose != null ? purpose : "N/A")).append("\n");
                    
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    String dateStr = (d.getDateAdded() != null) 
                        ? dateFormat.format(d.getDateAdded()) : "N/A";
                    writer.append("Date Added,").append(dateStr).append("\n");
                    
                    writer.append("Quantity,").append(String.valueOf(d.getQuantity())).append("\n");
                    writer.append("Amount,").append(String.format("%.2f", d.getAmount())).append("\n");
                    
                    String remarks = d.getRemarks();
                    writer.append("Remarks,").append(escapeCsv(remarks != null ? remarks : "")).append("\n");
                    
                    String imagePath = d.getImagePath();
                    writer.append("Image Path,").append(escapeCsv(imagePath != null ? imagePath : "")).append("\n");
                    
                    writer.flush();
                    
                    JOptionPane.showMessageDialog(this,
                        "Record exported successfully to:\n" + filePath,
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                        
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                        "Error writing CSV file: " + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error exporting record: " + ex.getMessage(), 
                "Export Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}