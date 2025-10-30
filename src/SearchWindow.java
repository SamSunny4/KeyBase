package src;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class SearchWindow extends JFrame {
    private JTextField txtSearch;
    private JComboBox<String> cmbSearchField;
    private JComboBox<String> cmbVehicleKeyType;
    private JComboBox<String> cmbKeyType;
    private JTable tblResults;
    private JLabel lblImagePreview;
    private DefaultTableModel tableModel;
    private JDateChooser dateFromChooser;
    private JDateChooser dateToChooser;
    
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
        gbc.insets = new Insets(8, 8, 8, 8);
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
        cmbSearchField = new JComboBox<>(new String[] {"All Fields", "Name", "Phone Number", "Vehicle Number", "ID Number", "Key Number", "Remarks"});
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
        
        // Filter by key for
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblKeyFor = new JLabel("Key For:");
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
        
        // Filter by vehicle key type
        gbc.gridx = 2;
        gbc.gridy = 1;
        JLabel lblKeyType = new JLabel("Key Type:");
        lblKeyType.setFont(new Font("Arial", Font.BOLD, 12));
        searchPanel.add(lblKeyType, gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 1;
        cmbVehicleKeyType = new JComboBox<>(new String[] {"Any", "2 Wheeler", "4 Wheeler", "Other"});
        cmbVehicleKeyType.setPreferredSize(new Dimension(150, 30));
        cmbVehicleKeyType.setBackground(new Color(250, 250, 250));
        cmbVehicleKeyType.setForeground(new Color(60, 62, 128));
        cmbVehicleKeyType.setFont(new Font("Arial", Font.PLAIN, 12));
        cmbVehicleKeyType.setBorder(BorderFactory.createLineBorder(new Color(109, 193, 210), 1));
        cmbVehicleKeyType.setToolTipText("Filter by vehicle key type");
        searchPanel.add(cmbVehicleKeyType, gbc);
        
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
        
        // Clear filters button
        gbc.gridx = 4;
        gbc.gridy = 2;
        JButton btnClear = new JButton("Clear Filters");
        btnClear.setPreferredSize(new Dimension(120, 30));
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
        splitPane.setDividerLocation(700);
        splitPane.setDividerSize(8);
        splitPane.setContinuousLayout(true);
        
        // Table for results
        String[] columnNames = {"ID", "Name", "Phone", "Vehicle No", "Key No", "Key Type", "ID No", "Date", "Remarks", "Quantity", "Amount"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
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
        lblImagePreview.setPreferredSize(new Dimension(350, 350));
        lblImagePreview.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        imagePanel.add(lblImagePreview, BorderLayout.CENTER);
        
        JButton btnExport = new JButton("Export Results to CSV");
        btnExport.setPreferredSize(new Dimension(200, 35));
        btnExport.setBackground(new Color(109, 193, 210));
        btnExport.setForeground(new Color(60, 62, 128));
        btnExport.setFont(new Font("Arial", Font.BOLD, 12));
        btnExport.setFocusPainted(false);
        btnExport.setToolTipText("Export current search results to CSV (Ctrl+E)");
        btnExport.setMnemonic(KeyEvent.VK_E);
        btnExport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportResultsToCsv();
            }
        });
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        exportPanel.setBackground(Color.WHITE);
        exportPanel.add(btnExport);
        imagePanel.add(exportPanel, BorderLayout.SOUTH);

        // Keyboard shortcut for export (Ctrl+E)
        KeyStroke ksExport = KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ksExport, "exportResults");
        getRootPane().getActionMap().put("exportResults", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportResultsToCsv();
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
        String keyType = (String) cmbKeyType.getSelectedItem();
        String vehicleKeyType = (String) cmbVehicleKeyType.getSelectedItem();
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
                case "Key Number":
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
        
        if (!keyType.equals("Any")) {
            queryBuilder.append("AND key_type = ? ");
        } else if (!vehicleKeyType.equals("Any")) {
            queryBuilder.append("AND key_type = ? ");
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
            
            if (!keyType.equals("Any")) {
                pstmt.setString(paramIndex++, keyType);
            } else if (!vehicleKeyType.equals("Any")) {
                pstmt.setString(paramIndex++, vehicleKeyType);
            }
            
            if (dateFrom != null) {
                pstmt.setDate(paramIndex++, new java.sql.Date(dateFrom.getTime()));
            }
            
            if (dateTo != null) {
                pstmt.setDate(paramIndex++, new java.sql.Date(dateTo.getTime()));
            }
            
            ResultSet rs = pstmt.executeQuery();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            
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
                if (duplicator != null && duplicator.getImagePath() != null) {
                    try {
                        File imageFile = new File(duplicator.getImagePath());
                        if (imageFile.exists()) {
                            BufferedImage img = ImageIO.read(imageFile);
                            Image scaledImg = img.getScaledInstance(250, 250, Image.SCALE_SMOOTH);
                            lblImagePreview.setIcon(new ImageIcon(scaledImg));
                            lblImagePreview.setText("");
                        } else {
                            lblImagePreview.setIcon(null);
                            lblImagePreview.setText("Image file not found");
                        }
                    } catch (Exception e) {
                        lblImagePreview.setIcon(null);
                        lblImagePreview.setText("Error loading image");
                    }
                } else {
                    lblImagePreview.setIcon(null);
                    lblImagePreview.setText("No image available");
                }
            } catch (Exception e) {
                lblImagePreview.setIcon(null);
                lblImagePreview.setText("Error loading image data");
            }
        }
    }
    
    private void exportResultsToCsv() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                "No data to export.", 
                "Export Error", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Filtered Results to CSV");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setSelectedFile(new File("search_results.csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();
            
            // Add .csv extension if not present
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }
            
            // Export only the filtered/visible results from the table
            try (java.io.FileWriter writer = new java.io.FileWriter(filePath)) {
                // Write header
                writer.append("ID,Name,Phone Number,Key Type,Vehicle No,ID Number,Key Number,Key For,Date Added,Remarks,Quantity,Amount\n");
                
                // Write each row from the current table view
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        Object value = tableModel.getValueAt(i, j);
                        String valueStr = "";
                        
                        if (value != null) {
                            if (value instanceof java.util.Date) {
                                valueStr = dateFormat.format((java.util.Date) value);
                            } else if (value instanceof Double) {
                                valueStr = String.format("%.2f", (Double) value);
                            } else {
                                valueStr = value.toString();
                            }
                        }
                        
                        // Escape CSV special characters
                        if (valueStr.contains(",") || valueStr.contains("\"") || valueStr.contains("\n")) {
                            valueStr = "\"" + valueStr.replace("\"", "\"\"") + "\"";
                        }
                        
                        writer.append(valueStr);
                        if (j < tableModel.getColumnCount() - 1) {
                            writer.append(",");
                        }
                    }
                    writer.append("\n");
                }
                
                writer.flush();
                
                JOptionPane.showMessageDialog(this, 
                    "Filtered results exported successfully!\n" + 
                    tableModel.getRowCount() + " records exported to:\n" + filePath, 
                    "Export Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error exporting data: " + e.getMessage(), 
                    "Export Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
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
                    writer.append("Record ID,").append(String.valueOf(d.getDuplicatorId())).append("\n");
                    writer.append("Name,").append(escapeCsv(d.getName())).append("\n");
                    writer.append("Phone Number,").append(escapeCsv(d.getPhoneNumber())).append("\n");
                    writer.append("ID Number,").append(escapeCsv(d.getIdNo())).append("\n");
                    writer.append("Key Number,").append(escapeCsv(d.getKeyNo())).append("\n");
                    
                    String keyFor = d.getKeyType();
                    writer.append("Key For,").append(escapeCsv(keyFor != null ? keyFor : "N/A")).append("\n");
                    
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
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