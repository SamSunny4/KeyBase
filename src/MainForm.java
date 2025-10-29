package src;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;

public class MainForm extends JFrame {
    // Form components
    private JTextField txtName;
    private JTextField txtPhoneNumber;
    private JTextField txtVehicleNo;
    private JLabel lblVehicleNo;
    private JTextField txtIdNo;
    private JTextField txtKeyNo;
    private JComboBox<String> cmbKeyType;
    private JComboBox<String> cmbVehicleType;
    private JTextField txtRemarks;
    private JTextField txtQuantity;
    private JTextField txtAmount;
    private JLabel lblImagePreview;
    private JButton btnCaptureImage;
    private JButton btnDeleteImage;
    private JButton btnSave;
    private JButton btnReset;
    private JTable tblKeyEntries;
    private JLabel statusBar;
    private JDateChooser dateChooser;
    private JPanel imageButtonPanel;
    
    private String capturedImagePath = null;
    private String cachedImagePath = null;
    
    public MainForm() {
        initComponents();
        initMenuBar();
        initShortcuts();
        setTitle("KeyBase - Key Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Set application icon from splash.png if available
        setAppIcon();
        
        // Add window listener to clean cache on exit
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanupCache();
            }
        });
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
    
    private void cleanupCache() {
        // Clear cached images on application exit
        try {
            File cacheDir = new File("cache");
            if (cacheDir.exists() && cacheDir.isDirectory()) {
                File[] cacheFiles = cacheDir.listFiles();
                if (cacheFiles != null) {
                    for (File file : cacheFiles) {
                        if (file.isFile() && file.getName().endsWith(".jpg")) {
                            file.delete();
                        }
                    }
                }
            }
            
            // Also clean up the current cached image if exists
            if (cachedImagePath != null) {
                File cacheFile = new File(cachedImagePath);
                if (cacheFile.exists()) {
                    cacheFile.delete();
                }
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    private void initShortcuts() {
        // Ctrl+F => open search
        KeyStroke ksSearch = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ksSearch, "openSearch");
        getRootPane().getActionMap().put("openSearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SearchWindow sw = new SearchWindow();
                sw.setVisible(true);
            }
        });

        // Ctrl+E => export CSV
        KeyStroke ksExport = KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ksExport, "exportCsv");
        getRootPane().getActionMap().put("exportCsv", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportToCsv();
            }
        });
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
            JOptionPane.showMessageDialog(this, "Error loading record details: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void printSelectedRecord(int id) {
        try {
            Duplicator d = Duplicator.findById(id);
            if (d == null) {
                JOptionPane.showMessageDialog(this, "Record not found.", "Print Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create and show the dialog to use its print functionality
            RecordDetailsDialog dialog = new RecordDetailsDialog(this, d);
            dialog.setVisible(false); // Don't show the dialog, just use its print method
            dialog.dispose();
            
            // Alternatively, directly invoke print
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

    private void setStatus(String text) {
        if (statusBar != null) {
            statusBar.setText(text);
        }
    }
    
    private void initComponents() {
        // Main layout
        setLayout(new BorderLayout());
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Key Duplicator Information"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtName = new JTextField(20);
    txtName.setToolTipText("Customer full name");
        txtName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtPhoneNumber.requestFocus();
                }
            }
        });
        formPanel.add(txtName, gbc);
        
        // Phone Number field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Phone Number:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtPhoneNumber = new JTextField(20);
    txtPhoneNumber.setToolTipText("10-digit phone number (digits only)");
        txtPhoneNumber.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // Add phone validation
                    String phone = txtPhoneNumber.getText().trim();
                    if (phone.length() != 10 || !phone.matches("\\d{10}")) {
                        JOptionPane.showMessageDialog(MainForm.this, 
                            "Phone number must be exactly 10 digits.", 
                            "Validation Error", 
                            JOptionPane.ERROR_MESSAGE);
                        txtPhoneNumber.requestFocus();
                        return;
                    }
                    
                    // If validation passes, move to next field (Key Type combo)
                    cmbVehicleType.requestFocus();
                }
            }
        });
        formPanel.add(txtPhoneNumber, gbc);
        
        // Vehicle Type dropdown (Key Type)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Key Type:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        cmbVehicleType = new JComboBox<>(new String[] {"SELECT", "2 Wheeler", "4 Wheeler", "Other"});
        cmbVehicleType.setSelectedIndex(0); // Default to SELECT
        cmbVehicleType.setToolTipText("Select key type");
        
        // Use ItemListener instead of ActionListener to avoid interference with Enter key
        cmbVehicleType.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateVehicleNoVisibility();
                }
            }
        });
        
        // Override the default Enter key behavior completely
        InputMap vehicleInputMap = cmbVehicleType.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap vehicleActionMap = cmbVehicleType.getActionMap();
        
        // Remove default action if exists
        Object enterKey = vehicleInputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        if (enterKey != null) {
            vehicleActionMap.remove(enterKey);
        }
        
        // Set our custom action
        vehicleInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "moveToNext");
        vehicleActionMap.put("moveToNext", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Close the popup if it's open
                if (cmbVehicleType.isPopupVisible()) {
                    cmbVehicleType.setPopupVisible(false);
                }
                // Move focus to next field
                SwingUtilities.invokeLater(() -> {
                    if (txtVehicleNo.isVisible()) {
                        txtVehicleNo.requestFocus();
                    } else {
                        txtIdNo.requestFocus();
                    }
                });
            }
        });
        
        formPanel.add(cmbVehicleType, gbc);
        
        // Vehicle Number field
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        lblVehicleNo = new JLabel("Vehicle No:");
        formPanel.add(lblVehicleNo, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtVehicleNo = new JTextField(20);
    txtVehicleNo.setToolTipText("Vehicle registration number");
        txtVehicleNo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtIdNo.requestFocus();
                }
            }
        });
        formPanel.add(txtVehicleNo, gbc);
        
        // ID Number field (renamed from Aadhar No)
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("ID No:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtIdNo = new JTextField(20);
    txtIdNo.setToolTipText("ID / national ID number");
        txtIdNo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtKeyNo.requestFocus();
                }
            }
        });
        formPanel.add(txtIdNo, gbc);
        
        // Key Number field
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Key No:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtKeyNo = new JTextField(20);
    txtKeyNo.setToolTipText("Key identifier or serial number");
        txtKeyNo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    cmbKeyType.requestFocus();
                }
            }
        });
        formPanel.add(txtKeyNo, gbc);
        
        // Key For field
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Key For:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        cmbKeyType = new JComboBox<>(new String[] {"SELECT", "Personal", "Commercial", "Department", "Suspicious"});
        cmbKeyType.setSelectedIndex(0); // Default to SELECT
        cmbKeyType.setToolTipText("Purpose of the key");
        
        // Override the default Enter key behavior completely
        InputMap inputMap = cmbKeyType.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = cmbKeyType.getActionMap();
        
        // Remove default action if exists
        Object keyForEnterKey = inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        if (keyForEnterKey != null) {
            actionMap.remove(keyForEnterKey);
        }
        
        // Set our custom action
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "moveToRemarks");
        actionMap.put("moveToRemarks", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Close the popup if it's open
                if (cmbKeyType.isPopupVisible()) {
                    cmbKeyType.setPopupVisible(false);
                }
                // Move focus to next field
                SwingUtilities.invokeLater(() -> txtRemarks.requestFocus());
            }
        });
        
        formPanel.add(cmbKeyType, gbc);
        
        // Remarks field (single-line)
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Remarks:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        txtRemarks = new JTextField(20);
    txtRemarks.setToolTipText("Optional notes or remarks");
        txtRemarks.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Move focus to quantity field when Enter is pressed
                txtQuantity.requestFocus();
            }
        });
        formPanel.add(txtRemarks, gbc);
        
        // Quantity field with spinner
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Quantity:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 0, 999, 1);
        JSpinner spnQuantity = new JSpinner(spinnerModel);
        spnQuantity.setToolTipText("Number of keys (use arrow keys to increment/decrement)");
        ((JSpinner.DefaultEditor) spnQuantity.getEditor()).getTextField().setColumns(5);
        
        // Add Enter key support to spinner
        JComponent editor = spnQuantity.getEditor();
        JFormattedTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtAmount.requestFocus();
                }
            }
        });
        
        // Store reference for later use
        txtQuantity = textField;
        formPanel.add(spnQuantity, gbc);
        
        // Amount field
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Amount:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        txtAmount = new JTextField(10);
        txtAmount.setToolTipText("Amount in currency (e.g., 100.00)");
        txtAmount.setText("");
        txtAmount.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    dateChooser.getDateField().requestFocus();
                }
            }
        });
        formPanel.add(txtAmount, gbc);
        
        // Right side panel for Date and Image
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 10;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.3;
        gbc.weighty = 1.0;

        JPanel rightPanel = new JPanel(new GridBagLayout());
        GridBagConstraints rightGbc = new GridBagConstraints();
        rightGbc.insets = new Insets(5, 5, 5, 5);
        rightGbc.gridx = 0;
        rightGbc.weightx = 1.0;

        // Date field in right panel
        rightGbc.gridy = 0;
        rightGbc.weighty = 0.0;
        rightGbc.fill = GridBagConstraints.HORIZONTAL;
        rightGbc.anchor = GridBagConstraints.NORTHWEST;
        JPanel datePanel = new JPanel(new GridBagLayout());
        datePanel.setBorder(BorderFactory.createTitledBorder("Date"));
        GridBagConstraints datePanelGbc = new GridBagConstraints();
        datePanelGbc.insets = new Insets(5, 5, 5, 5);
        datePanelGbc.fill = GridBagConstraints.HORIZONTAL;
        datePanelGbc.weightx = 1.0;

        dateChooser = new JDateChooser();
        dateChooser.setToolTipText("Date when the key was created/received");
        dateChooser.setDate(new Date()); // Default to current date

        // Add key listener to the date chooser's text field
        dateChooser.getDateField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnCaptureImage.requestFocus();
                }
            }
        });

        // Also add action listener for Enter key
        dateChooser.getDateField().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnCaptureImage.requestFocus();
            }
        });

        datePanel.add(dateChooser, datePanelGbc);
        rightPanel.add(datePanel, rightGbc);

        // Image preview in right panel
        rightGbc.gridy = 1;
        rightGbc.weighty = 1.0;
        rightGbc.fill = GridBagConstraints.BOTH;

        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBorder(BorderFactory.createTitledBorder("Image"));

        lblImagePreview = new JLabel();
        lblImagePreview.setPreferredSize(new Dimension(150, 150));
        lblImagePreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        lblImagePreview.setHorizontalAlignment(JLabel.CENTER);
        lblImagePreview.setText("No Image");

        imagePanel.add(lblImagePreview, BorderLayout.CENTER);

        // Button panel for capture and delete
        imageButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        btnCaptureImage = new JButton("Capture");
    btnCaptureImage.setMnemonic(KeyEvent.VK_C);
    btnCaptureImage.setToolTipText("Open webcam to capture a customer image (Alt+C)");
        btnCaptureImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                captureImage();
            }
        });

        btnCaptureImage.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    captureImage();
                    btnSave.requestFocus();
                }
            }
        });

        btnDeleteImage = new JButton("Delete");
        btnDeleteImage.setToolTipText("Delete the captured image");
        btnDeleteImage.setVisible(false); // Hidden by default
        btnDeleteImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteImage();
            }
        });
        
        btnDeleteImage.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    deleteImage();
                }
            }
        });

        imageButtonPanel.add(btnCaptureImage);
        imageButtonPanel.add(btnDeleteImage);
        
        imagePanel.add(imageButtonPanel, BorderLayout.SOUTH);
        rightPanel.add(imagePanel, rightGbc);

        formPanel.add(rightPanel, gbc);
        
        // Button panel
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        btnSave = new JButton("Save");
    btnSave.setMnemonic(KeyEvent.VK_S);
    btnSave.setToolTipText("Save current record (Alt+S)");
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveRecord();
            }
        });
        
        btnSave.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    saveRecord();
                }
            }
        });
        
        btnReset = new JButton("Reset");
    btnReset.setMnemonic(KeyEvent.VK_R);
    btnReset.setToolTipText("Clear the form (Alt+R)");
        btnReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetForm();
            }
        });
        
        btnReset.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    resetForm();
                }
            }
        });
        
        buttonPanel.add(btnSave);
        buttonPanel.add(btnReset);
        
        formPanel.add(buttonPanel, gbc);
        
        // Add form panel to the top
        add(formPanel, BorderLayout.NORTH);
        
        // Create table for key entries at the bottom
        String[] columnNames = {"ID", "Name", "Phone", "Vehicle No", "Key No", "Key Type", "ID No", "Date", "Remarks", "Quantity", "Amount"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        tblKeyEntries = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(tblKeyEntries);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Key Entry Records"));
        
        add(scrollPane, BorderLayout.CENTER);

    // Status bar
    statusBar = new JLabel("Ready");
    statusBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
    add(statusBar, BorderLayout.SOUTH);
        
        // Load initial data
        loadKeyEntries();
        // UI tweaks for table
        tblKeyEntries.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tblKeyEntries.setRowHeight(22);
        tblKeyEntries.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        // set preferred column widths
        if (tblKeyEntries.getColumnModel().getColumnCount() >= 11) {
            tblKeyEntries.getColumnModel().getColumn(0).setPreferredWidth(50); // ID
            tblKeyEntries.getColumnModel().getColumn(1).setPreferredWidth(150); // Name
            tblKeyEntries.getColumnModel().getColumn(2).setPreferredWidth(100); // Phone
            tblKeyEntries.getColumnModel().getColumn(3).setPreferredWidth(110); // Vehicle No
            tblKeyEntries.getColumnModel().getColumn(4).setPreferredWidth(90); // Key No
            tblKeyEntries.getColumnModel().getColumn(5).setPreferredWidth(90); // Key Type
            tblKeyEntries.getColumnModel().getColumn(6).setPreferredWidth(110); // ID No
            tblKeyEntries.getColumnModel().getColumn(7).setPreferredWidth(90); // Date
            tblKeyEntries.getColumnModel().getColumn(8).setPreferredWidth(150); // Remarks
            tblKeyEntries.getColumnModel().getColumn(9).setPreferredWidth(70); // Quantity
            tblKeyEntries.getColumnModel().getColumn(10).setPreferredWidth(80); // Amount
        }

        // Double-click row to show details
        tblKeyEntries.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tblKeyEntries.getSelectedRow();
                    if (row >= 0) {
                        int modelRow = tblKeyEntries.convertRowIndexToModel(row);
                        Object idObj = tblKeyEntries.getModel().getValueAt(modelRow, 0);
                        if (idObj instanceof Integer) {
                            int id = (Integer) idObj;
                            showRecordDetails(id);
                        }
                    }
                }
            }
        });
        
        // Right-click context menu for selected record
        JPopupMenu contextMenu = new JPopupMenu();
        
        JMenuItem viewDetailsItem = new JMenuItem("View Details");
        viewDetailsItem.addActionListener(e -> {
            int row = tblKeyEntries.getSelectedRow();
            if (row >= 0) {
                int modelRow = tblKeyEntries.convertRowIndexToModel(row);
                Object idObj = tblKeyEntries.getModel().getValueAt(modelRow, 0);
                if (idObj instanceof Integer) {
                    showRecordDetails((Integer) idObj);
                }
            }
        });
        
        JMenuItem printRecordItem = new JMenuItem("Print Record");
        printRecordItem.addActionListener(e -> {
            int row = tblKeyEntries.getSelectedRow();
            if (row >= 0) {
                int modelRow = tblKeyEntries.convertRowIndexToModel(row);
                Object idObj = tblKeyEntries.getModel().getValueAt(modelRow, 0);
                if (idObj instanceof Integer) {
                    printSelectedRecord((Integer) idObj);
                }
            }
        });
        
        JMenuItem exportRecordItem = new JMenuItem("Export Record to CSV");
        exportRecordItem.addActionListener(e -> {
            int row = tblKeyEntries.getSelectedRow();
            if (row >= 0) {
                int modelRow = tblKeyEntries.convertRowIndexToModel(row);
                Object idObj = tblKeyEntries.getModel().getValueAt(modelRow, 0);
                if (idObj instanceof Integer) {
                    exportSelectedRecord((Integer) idObj);
                }
            }
        });
        
        contextMenu.add(viewDetailsItem);
        contextMenu.addSeparator();
        contextMenu.add(printRecordItem);
        contextMenu.add(exportRecordItem);
        
        tblKeyEntries.setComponentPopupMenu(contextMenu);

        // Selection listener to update status
        tblKeyEntries.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblKeyEntries.getSelectedRow() != -1) {
                int viewRow = tblKeyEntries.getSelectedRow();
                int modelRow = tblKeyEntries.convertRowIndexToModel(viewRow);
                Object idObj = tblKeyEntries.getModel().getValueAt(modelRow, 0);
                setStatus("Selected ID: " + idObj);
            }
        });
    }
    
    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        
    JMenuItem viewEntriesItem = new JMenuItem("Refresh");
    viewEntriesItem.setToolTipText("Refresh and view key entries");
    viewEntriesItem.setMnemonic(KeyEvent.VK_V);
        viewEntriesItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showKeyEntriesDialog();
            }
        });
        
        JMenuItem searchItem = new JMenuItem("Search Records");
        searchItem.setToolTipText("Open search dialog (Ctrl+F)");
        searchItem.setMnemonic(KeyEvent.VK_F);
        searchItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        searchItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SearchWindow searchWindow = new SearchWindow();
                searchWindow.setVisible(true);
            }
        });
        
    JMenuItem exportItem = new JMenuItem("Export to CSV");
    exportItem.setToolTipText("Export all records to a CSV file (Ctrl+E)");
    exportItem.setMnemonic(KeyEvent.VK_E);
    exportItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
        exportItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportToCsv();
            }
        });
        
    JMenuItem preferencesItem = new JMenuItem("Preferences");
    preferencesItem.setToolTipText("Application preferences");
    preferencesItem.setMnemonic(KeyEvent.VK_P);
        preferencesItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPreferencesDialog();
            }
        });
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        
        fileMenu.add(viewEntriesItem);
        fileMenu.add(searchItem);
        fileMenu.add(exportItem);
        fileMenu.add(preferencesItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        
        JMenuItem readmeItem = new JMenuItem("Readme");
        readmeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showReadmeDialog();
            }
        });
        
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(MainForm.this,
                    "KeyBase - Key Management System\nVersion 1.0\n© 2025",
                    "About KeyBase",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        helpMenu.add(readmeItem);
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void captureImage() {
        WebcamCapture captureDialog = new WebcamCapture(this);
        captureDialog.setVisible(true);
        
        if (captureDialog.isImageCaptured()) {
            cachedImagePath = captureDialog.getSavedImagePath();
            
            try {
                // Load and display the captured image
                BufferedImage img = ImageIO.read(new File(cachedImagePath));
                
                // Resize image to fit in the label
                Image scaledImg = img.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                lblImagePreview.setIcon(new ImageIcon(scaledImg));
                lblImagePreview.setText("");
                
                // Set the captured path to the cached path for now
                capturedImagePath = cachedImagePath;
                
                // Show the delete button
                btnDeleteImage.setVisible(true);
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error loading captured image: " + e.getMessage(),
                    "Image Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteImage() {
        int response = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete the captured image?",
            "Delete Image",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
            
        if (response == JOptionPane.YES_OPTION) {
            // Delete the cached file if it exists
            if (cachedImagePath != null) {
                try {
                    File cacheFile = new File(cachedImagePath);
                    if (cacheFile.exists()) {
                        cacheFile.delete();
                    }
                } catch (Exception e) {
                    // Ignore deletion errors
                }
            }
            
            // Clear the image preview
            lblImagePreview.setIcon(null);
            lblImagePreview.setText("No Image");
            
            // Clear image paths
            capturedImagePath = null;
            cachedImagePath = null;
            
            // Hide the delete button
            btnDeleteImage.setVisible(false);
            
            setStatus("Image deleted");
        }
    }
    
    private void saveRecord() {
        // Validate form
        if (!validateForm()) {
            return;
        }
        
        // Move cached image to actual location
        String finalImagePath = capturedImagePath;
        if (cachedImagePath != null && cachedImagePath.contains("cache")) {
            try {
                String imageDir = AppConfig.getImagesDirectory();
                File cacheFile = new File(cachedImagePath);
                String fileName = cacheFile.getName();
                File finalFile = new File(imageDir, fileName);
                
                // Copy the file from cache to actual directory
                BufferedImage img = ImageIO.read(cacheFile);
                ImageIO.write(img, "JPG", finalFile);
                
                finalImagePath = finalFile.getAbsolutePath();
                
                // Delete the cache file
                cacheFile.delete();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error moving image from cache: " + e.getMessage(),
                    "Image Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        
        // Create duplicator record
        // Make sure we have a date (use current date if null)
        Date currentDate = dateChooser.getDate();
        if (currentDate == null) {
            currentDate = new Date(); // Use current date as fallback
        }
        
        // Parse quantity and amount
        int quantity = 1;
        double amount = 0.00;
        try {
            String qtyText = txtQuantity.getText().trim();
            if (!qtyText.isEmpty()) {
                quantity = Integer.parseInt(qtyText);
            }
        } catch (NumberFormatException e) {
            quantity = 1;
        }
        try {
            String amtText = txtAmount.getText().trim();
            if (!amtText.isEmpty()) {
                amount = Double.parseDouble(amtText);
            }
        } catch (NumberFormatException e) {
            amount = 0.00;
        }
            
        Duplicator duplicator = new Duplicator(
            txtName.getText().trim(),
            txtPhoneNumber.getText().trim(),
            txtIdNo.getText().trim(),
            txtVehicleNo.getText().trim(),
            txtKeyNo.getText().trim(),
            (String) cmbVehicleType.getSelectedItem(),
            currentDate,
            txtRemarks.getText().trim(),
            quantity,
            amount,
            finalImagePath
        );
        
        // Save to database
        if (duplicator.save()) {
            cachedImagePath = null;
            
            JOptionPane.showMessageDialog(this,
                "Record saved successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh the table
            loadKeyEntries();
            
            // Reset form for next entry
            resetForm();
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to save record. Please try again.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean validateForm() {
        // Check name (required)
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a name.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            txtName.requestFocus();
            return false;
        }
        
        // Check phone number (required)
        String phoneNumber = txtPhoneNumber.getText().trim();
        if (phoneNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a phone number.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            txtPhoneNumber.requestFocus();
            return false;
        }
        
        // Validate phone number format (must be 10 digits)
        if (phoneNumber.length() != 10 || !phoneNumber.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this,
                "Phone number must be exactly 10 digits.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            txtPhoneNumber.requestFocus();
            return false;
        }
        
        // Check ID number (required)
        if (txtIdNo.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter an ID number.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            txtIdNo.requestFocus();
            return false;
        }
        
        // All other fields are optional - allow null/empty values
        return true;
    }
    
    private void resetForm() {
        txtName.setText("");
        txtPhoneNumber.setText("");
        txtVehicleNo.setText("");
        txtIdNo.setText("");
        txtKeyNo.setText("");
        cmbVehicleType.setSelectedIndex(0);
        cmbKeyType.setSelectedIndex(0);
        dateChooser.setDate(new Date()); // Reset to current date
        txtRemarks.setText("");
        txtQuantity.setText("1");
        txtAmount.setText("");
        lblImagePreview.setIcon(null);
        lblImagePreview.setText("No Image");
        capturedImagePath = null;
        
        // Clean up cached image if exists
        if (cachedImagePath != null) {
            File cachedFile = new File(cachedImagePath);
            if (cachedFile.exists()) {
                cachedFile.delete();
            }
            cachedImagePath = null;
        }
        
        // Hide delete button
        btnDeleteImage.setVisible(false);
        
        // Update vehicle number visibility
        updateVehicleNoVisibility();
        
        // Set focus to the first field
        txtName.requestFocus();
    }
    
    private void updateVehicleNoVisibility() {
        String selectedType = (String) cmbVehicleType.getSelectedItem();
        // Show Vehicle No only for 2 Wheeler and 4 Wheeler, hide for SELECT, Other, or empty
        boolean showVehicleNo = "2 Wheeler".equals(selectedType) || "4 Wheeler".equals(selectedType);
        lblVehicleNo.setVisible(showVehicleNo);
        txtVehicleNo.setVisible(showVehicleNo);
    }
    
    private void loadKeyEntries() {
        DefaultTableModel model = (DefaultTableModel) tblKeyEntries.getModel();
        model.setRowCount(0); // Clear existing rows
        
        try (ResultSet rs = Duplicator.getAllDuplicators()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            while (rs.next()) {
                String dateStr = "";
                if (rs.getDate("date_added") != null) {
                    dateStr = dateFormat.format(rs.getDate("date_added"));
                }
                
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
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading key entries: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showKeyEntriesDialog() {
        // Refresh and display the table (or open a new dialog with more details)
        loadKeyEntries();
        JOptionPane.showMessageDialog(this,
            "Key entries refreshed in the main window.",
            "Key Entries",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showPreferencesDialog() {
        PreferencesDialog prefsDialog = new PreferencesDialog(this);
        prefsDialog.setVisible(true);
    }
    
    private void showReadmeDialog() {
        ReadmeDialog readmeDialog = new ReadmeDialog(this);
        readmeDialog.setVisible(true);
    }
    
    private void exportToCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export to CSV");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setSelectedFile(new File("keybase_export.csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();
            
            // Add .csv extension if not present
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }
            
            // Export all records using the single record export loop
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM duplicator ORDER BY duplicator_id DESC")) {
                
                FileWriter writer = new FileWriter(filePath);
                int recordCount = 0;
                
                while (rs.next()) {
                    Duplicator d = new Duplicator();
                    d.setDuplicatorId(rs.getInt("duplicator_id"));
                    d.setName(rs.getString("name"));
                    d.setPhoneNumber(rs.getString("phone_number"));
                    d.setIdNo(rs.getString("id_no"));
                    d.setKeyNo(rs.getString("key_no"));
                    d.setKeyType(rs.getString("key_type"));
                    d.setDateAdded(rs.getDate("date_added"));
                    d.setRemarks(rs.getString("remarks"));
                    d.setQuantity(rs.getInt("quantity"));
                    d.setAmount(rs.getDouble("amount"));
                    d.setImagePath(rs.getString("image_path"));
                    
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
                    writer.append("Remarks,").append(escapeCsv(remarks != null ? remarks : "N/A")).append("\n");
                    writer.append("\n"); // Empty line between records
                    
                    recordCount++;
                }
                
                writer.close();
                
                JOptionPane.showMessageDialog(this, 
                    recordCount + " records exported successfully to " + filePath, 
                    "Export Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
                
            } catch (SQLException | IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error during export: " + e.getMessage(), 
                    "Export Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            // Set System Look and Feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error setting look and feel: " + e.getMessage());
        }
        
        // Load H2 database driver
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                "H2 JDBC driver not found. Please check your classpath.",
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        SwingUtilities.invokeLater(() -> new MainForm().setVisible(true));
    }
}