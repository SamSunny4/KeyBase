package src;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.swing.*;

public class EditRecordDialog extends JDialog {
    private Duplicator duplicator;
    private JTextField txtName;
    private JTextField txtPhoneNumber;
    private JTextField txtVehicleNo;
    private JLabel lblVehicleNo;
    private JTextField txtIdNo;
    private JTextField txtKeyNo;
    private JComboBox<String> cmbKeyType;
    private JComboBox<String> cmbKeyFor;
    private JTextField txtRemarks;
    private JSpinner spnQuantity;
    private JTextField txtAmount;
    private JLabel lblImagePreview;
    private JButton btnDeleteImage;
    private JDateChooser dateChooser;
    private String imagePath;
    private boolean saved = false;
    
    public EditRecordDialog(Frame owner, Duplicator duplicator) {
        super(owner, "Edit Record - ID: " + duplicator.getDuplicatorId(), true);
        this.duplicator = duplicator;
        this.imagePath = duplicator.getImagePath();
        
        setSize(850, 620);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        
        initComponents();
        loadRecordData();
    }
    
    private void initComponents() {
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contentPanel.setBackground(Color.WHITE);
        
        // Main panel with form on left and image on right
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(109, 193, 210), 2),
            "Edit Record Details",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 13),
            new Color(60, 62, 128)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        Font labelFont = new Font("Arial", Font.BOLD, 12);
        Color labelColor = new Color(60, 62, 128);
        
        // Name
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.0;
        JLabel lblName = new JLabel("Name:");
        lblName.setFont(labelFont);
        lblName.setForeground(labelColor);
        formPanel.add(lblName, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtName = new JTextField(25);
        txtName.setFont(new Font("Arial", Font.PLAIN, 12));
        txtName.setPreferredSize(new Dimension(300, 26));
        formPanel.add(txtName, gbc);
        row++;
        
        // Phone Number
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.0;
        JLabel lblPhone = new JLabel("Phone Number:");
        lblPhone.setFont(labelFont);
        lblPhone.setForeground(labelColor);
        formPanel.add(lblPhone, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtPhoneNumber = new JTextField(25);
        txtPhoneNumber.setFont(new Font("Arial", Font.PLAIN, 12));
        txtPhoneNumber.setPreferredSize(new Dimension(300, 26));
        formPanel.add(txtPhoneNumber, gbc);
        row++;
        
        // Key Type (Vehicle Type)
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.0;
        JLabel lblKeyType = new JLabel("Key Type:");
        lblKeyType.setFont(labelFont);
        lblKeyType.setForeground(labelColor);
        formPanel.add(lblKeyType, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        cmbKeyType = new JComboBox<>(new String[]{"SELECT", "2 Wheeler", "4 Wheeler", "Other"});
        cmbKeyType.setFont(new Font("Arial", Font.PLAIN, 12));
        cmbKeyType.setPreferredSize(new Dimension(300, 26));
        cmbKeyType.addItemListener(e -> updateVehicleNoVisibility());
        formPanel.add(cmbKeyType, gbc);
        row++;
        
        // Vehicle No
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.0;
        lblVehicleNo = new JLabel("Vehicle No:");
        lblVehicleNo.setFont(labelFont);
        lblVehicleNo.setForeground(labelColor);
        formPanel.add(lblVehicleNo, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtVehicleNo = new JTextField(25);
        txtVehicleNo.setFont(new Font("Arial", Font.PLAIN, 12));
        txtVehicleNo.setPreferredSize(new Dimension(300, 26));
        formPanel.add(txtVehicleNo, gbc);
        row++;
        
        // ID Number
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.0;
        JLabel lblIdNo = new JLabel("ID Number:");
        lblIdNo.setFont(labelFont);
        lblIdNo.setForeground(labelColor);
        formPanel.add(lblIdNo, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtIdNo = new JTextField(25);
        txtIdNo.setFont(new Font("Arial", Font.PLAIN, 12));
        txtIdNo.setPreferredSize(new Dimension(300, 26));
        formPanel.add(txtIdNo, gbc);
        row++;
        
        // Key Number
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.0;
        JLabel lblKeyNo = new JLabel("Key Number:");
        lblKeyNo.setFont(labelFont);
        lblKeyNo.setForeground(labelColor);
        formPanel.add(lblKeyNo, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtKeyNo = new JTextField(25);
        txtKeyNo.setFont(new Font("Arial", Font.PLAIN, 12));
        txtKeyNo.setPreferredSize(new Dimension(300, 26));
        formPanel.add(txtKeyNo, gbc);
        row++;
        
        // Purpose
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.0;
        JLabel lblKeyFor = new JLabel("Purpose:");
        lblKeyFor.setFont(labelFont);
        lblKeyFor.setForeground(labelColor);
        formPanel.add(lblKeyFor, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        cmbKeyFor = new JComboBox<>(new String[]{"SELECT", "Personal", "Commercial", "Department", "Suspicious"});
        cmbKeyFor.setFont(new Font("Arial", Font.PLAIN, 12));
        cmbKeyFor.setPreferredSize(new Dimension(300, 26));
        formPanel.add(cmbKeyFor, gbc);
        row++;
        
        // Date
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.0;
        JLabel lblDate = new JLabel("Date:");
        lblDate.setFont(labelFont);
        lblDate.setForeground(labelColor);
        formPanel.add(lblDate, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        dateChooser = new JDateChooser();
        formPanel.add(dateChooser, gbc);
        row++;
        
        // Quantity
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.0;
        JLabel lblQuantity = new JLabel("Quantity:");
        lblQuantity.setFont(labelFont);
        lblQuantity.setForeground(labelColor);
        formPanel.add(lblQuantity, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 0, 999, 1);
        spnQuantity = new JSpinner(spinnerModel);
        ((JSpinner.DefaultEditor) spnQuantity.getEditor()).getTextField().setFont(new Font("Arial", Font.PLAIN, 12));
        spnQuantity.setPreferredSize(new Dimension(40, 26));
        formPanel.add(spnQuantity, gbc);
        row++;
        
        // Amount
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.0;
        JLabel lblAmount = new JLabel("Amount:");
        lblAmount.setFont(labelFont);
        lblAmount.setForeground(labelColor);
        formPanel.add(lblAmount, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtAmount = new JTextField(25);
        txtAmount.setFont(new Font("Arial", Font.PLAIN, 12));
        txtAmount.setPreferredSize(new Dimension(150, 26));
        formPanel.add(txtAmount, gbc);
        row++;
        
        // Remarks
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.0;
        JLabel lblRemarks = new JLabel("Remarks:");
        lblRemarks.setFont(labelFont);
        lblRemarks.setForeground(labelColor);
        formPanel.add(lblRemarks, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtRemarks = new JTextField(25);
        txtRemarks.setFont(new Font("Arial", Font.PLAIN, 12));
        txtRemarks.setPreferredSize(new Dimension(300, 26));
        formPanel.add(txtRemarks, gbc);
        row++;
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Image panel on the right
        JPanel imagePanel = new JPanel(new BorderLayout(5, 5));
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(109, 193, 210), 2),
            "Customer Photo",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12),
            new Color(60, 62, 128)
        ));
        imagePanel.setPreferredSize(new Dimension(220, 0));
        
        lblImagePreview = new JLabel();
        lblImagePreview.setHorizontalAlignment(JLabel.CENTER);
        lblImagePreview.setVerticalAlignment(JLabel.CENTER);
        lblImagePreview.setPreferredSize(new Dimension(200, 200));
        lblImagePreview.setBackground(new Color(250, 250, 250));
        lblImagePreview.setOpaque(true);
        lblImagePreview.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(109, 193, 210), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        imagePanel.add(lblImagePreview, BorderLayout.CENTER);
        
        // Delete image button
        btnDeleteImage = new JButton("Delete Image");
        btnDeleteImage.setFont(new Font("Arial", Font.BOLD, 10));
        btnDeleteImage.setBackground(new Color(220, 80, 80));
        btnDeleteImage.setForeground(Color.RED);
        btnDeleteImage.setFocusPainted(false);
        btnDeleteImage.setPreferredSize(new Dimension(110, 26));
        btnDeleteImage.setToolTipText("Delete the captured image");
        btnDeleteImage.addActionListener(e -> deleteImage());
        imagePanel.add(btnDeleteImage, BorderLayout.SOUTH);
        
        mainPanel.add(imagePanel, BorderLayout.EAST);
        
        contentPanel.add(mainPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnSave = new JButton("Save Changes");
        btnSave.setFont(new Font("Arial", Font.BOLD, 12));
        btnSave.setBackground(new Color(109, 193, 210));
        btnSave.setForeground(new Color(60, 62, 128));
        btnSave.setFocusPainted(false);
        btnSave.setPreferredSize(new Dimension(130, 32));
        btnSave.addActionListener(e -> saveChanges());
        
        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Arial", Font.BOLD, 12));
        btnCancel.setBackground(new Color(240, 240, 240));
        btnCancel.setForeground(new Color(60, 62, 128));
        btnCancel.setFocusPainted(false);
        btnCancel.setPreferredSize(new Dimension(100, 32));
        btnCancel.addActionListener(e -> dispose());
        
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(contentPanel);
    }
    
    private void loadRecordData() {
        txtName.setText(duplicator.getName());
        txtPhoneNumber.setText(duplicator.getPhoneNumber());
        txtIdNo.setText(duplicator.getIdNo());
        txtKeyNo.setText(duplicator.getKeyNo());
        
        // Load vehicle number
        String vehicleNo = duplicator.getVehicleNo();
        if (vehicleNo != null && !vehicleNo.trim().isEmpty()) {
            txtVehicleNo.setText(vehicleNo);
            
            // Try to determine vehicle type from vehicle number
            if (!vehicleNo.equalsIgnoreCase("N/A") && !vehicleNo.equalsIgnoreCase("deleted")) {
                // If vehicle number exists, try to guess type or default to 2 Wheeler
                cmbKeyType.setSelectedItem("2 Wheeler");
            }
        }
        
        // Load Purpose
        String purpose = duplicator.getPurpose();
        if (purpose != null && !purpose.trim().isEmpty()) {
            cmbKeyFor.setSelectedItem(purpose);
        }
        
        if (duplicator.getDateAdded() != null) {
            dateChooser.setDate(duplicator.getDateAdded());
        }
        
        spnQuantity.setValue(duplicator.getQuantity());
        txtAmount.setText(String.format("%.2f", duplicator.getAmount()));
        txtRemarks.setText(duplicator.getRemarks());
        
        // Load image
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    BufferedImage img = ImageIO.read(imageFile);
                    Image scaledImg = img.getScaledInstance(180, 180, Image.SCALE_SMOOTH);
                    lblImagePreview.setIcon(new ImageIcon(scaledImg));
                    btnDeleteImage.setVisible(true);
                } else {
                    lblImagePreview.setText("Image not found");
                    btnDeleteImage.setVisible(false);
                }
            } catch (Exception e) {
                lblImagePreview.setText("Error loading image");
                btnDeleteImage.setVisible(false);
            }
        } else {
            lblImagePreview.setText("No image");
            btnDeleteImage.setVisible(false);
        }
        
        updateVehicleNoVisibility();
    }
    
    private void updateVehicleNoVisibility() {
        String selectedType = (String) cmbKeyType.getSelectedItem();
        boolean showVehicleNo = "2 Wheeler".equals(selectedType) || "4 Wheeler".equals(selectedType);
        lblVehicleNo.setVisible(showVehicleNo);
        txtVehicleNo.setVisible(showVehicleNo);
    }
    
    private void deleteImage() {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No image to delete.",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int response = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete the captured image?\n" +
            "This action cannot be undone.",
            "Confirm Image Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (response == JOptionPane.YES_OPTION) {
            // Delete the image file
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    if (imageFile.delete()) {
                        // Clear the image path
                        imagePath = null;
                        
                        // Update the preview
                        lblImagePreview.setIcon(null);
                        lblImagePreview.setText("Image deleted");
                        btnDeleteImage.setVisible(false);
                        
                        JOptionPane.showMessageDialog(this,
                            "Image deleted successfully.\n" +
                            "Click 'Save Changes' to update the record.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Failed to delete the image file.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    // File doesn't exist, just clear the path
                    imagePath = null;
                    lblImagePreview.setIcon(null);
                    lblImagePreview.setText("No image");
                    btnDeleteImage.setVisible(false);
                    
                    JOptionPane.showMessageDialog(this,
                        "Image file not found. Reference cleared.",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error deleting image: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void saveChanges() {
        // Validate
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtName.requestFocus();
            return;
        }
        
        if (txtPhoneNumber.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phone number is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtPhoneNumber.requestFocus();
            return;
        }
        
        if (txtIdNo.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID number is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtIdNo.requestFocus();
            return;
        }
        
        // Update duplicator object
        duplicator.setName(txtName.getText().trim());
        duplicator.setPhoneNumber(txtPhoneNumber.getText().trim());
        duplicator.setIdNo(txtIdNo.getText().trim());
        duplicator.setKeyNo(txtKeyNo.getText().trim());
        
        // Save vehicle number
        duplicator.setVehicleNo(txtVehicleNo.getText().trim());
        
        // Save Purpose
        String selectedPurpose = (String) cmbKeyFor.getSelectedItem();
        if (!"SELECT".equals(selectedPurpose)) {
            duplicator.setPurpose(selectedPurpose);
        } else {
            duplicator.setPurpose(null);
        }
        
        duplicator.setDateAdded(dateChooser.getDate());
        duplicator.setQuantity((Integer) spnQuantity.getValue());
        
        try {
            double amount = Double.parseDouble(txtAmount.getText().trim());
            duplicator.setAmount(amount);
        } catch (NumberFormatException e) {
            duplicator.setAmount(0.00);
        }
        
        duplicator.setRemarks(txtRemarks.getText().trim());
        
        // Update image path (in case it was deleted)
        duplicator.setImagePath(imagePath);
        
        // Save to database
        if (duplicator.update()) {
            saved = true;
            JOptionPane.showMessageDialog(this, 
                "Record updated successfully!", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Failed to update record!", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isSaved() {
        return saved;
    }
    
    // Inner class for JDateChooser
    private class JDateChooser extends JPanel {
        private JTextField dateField;
        private JButton calendarButton;
        private Date selectedDate;
        
        public JDateChooser() {
            setLayout(new BorderLayout());
            dateField = new JTextField(10);
            dateField.setEditable(false);
            calendarButton = new JButton("...");
            
            add(dateField, BorderLayout.CENTER);
            add(calendarButton, BorderLayout.EAST);
            
            calendarButton.addActionListener(e -> {
                String dateStr = JOptionPane.showInputDialog(this, 
                    "Enter date (yyyy-MM-dd):", 
                    dateField.getText());
                
                if (dateStr != null && !dateStr.isEmpty()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        selectedDate = sdf.parse(dateStr);
                        dateField.setText(sdf.format(selectedDate));
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, 
                            "Invalid date format. Please use yyyy-MM-dd", 
                            "Date Format Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        }
        
        public Date getDate() {
            return selectedDate;
        }
        
        public void setDate(Date date) {
            this.selectedDate = date;
            if (date != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                dateField.setText(sdf.format(date));
            }
        }
    }
}
