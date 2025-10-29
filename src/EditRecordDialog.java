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
    private JDateChooser dateChooser;
    private String imagePath;
    private boolean saved = false;
    
    public EditRecordDialog(Frame owner, Duplicator duplicator) {
        super(owner, "Edit Record - ID: " + duplicator.getDuplicatorId(), true);
        this.duplicator = duplicator;
        this.imagePath = duplicator.getImagePath();
        
        setSize(700, 650);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        
        initComponents();
        loadRecordData();
    }
    
    private void initComponents() {
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Edit Record Details"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Name
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        txtName = new JTextField(20);
        formPanel.add(txtName, gbc);
        row++;
        
        // Phone Number
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Phone Number:"), gbc);
        gbc.gridx = 1;
        txtPhoneNumber = new JTextField(20);
        formPanel.add(txtPhoneNumber, gbc);
        row++;
        
        // Key Type (Vehicle Type)
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Key Type:"), gbc);
        gbc.gridx = 1;
        cmbKeyType = new JComboBox<>(new String[]{"SELECT", "2 Wheeler", "4 Wheeler", "Other"});
        cmbKeyType.addItemListener(e -> updateVehicleNoVisibility());
        formPanel.add(cmbKeyType, gbc);
        row++;
        
        // Vehicle No
        gbc.gridx = 0; gbc.gridy = row;
        lblVehicleNo = new JLabel("Vehicle No:");
        formPanel.add(lblVehicleNo, gbc);
        gbc.gridx = 1;
        txtVehicleNo = new JTextField(20);
        formPanel.add(txtVehicleNo, gbc);
        row++;
        
        // ID Number
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("ID Number:"), gbc);
        gbc.gridx = 1;
        txtIdNo = new JTextField(20);
        formPanel.add(txtIdNo, gbc);
        row++;
        
        // Key Number
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Key Number:"), gbc);
        gbc.gridx = 1;
        txtKeyNo = new JTextField(20);
        formPanel.add(txtKeyNo, gbc);
        row++;
        
        // Key For
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Key For:"), gbc);
        gbc.gridx = 1;
        cmbKeyFor = new JComboBox<>(new String[]{"SELECT", "Home", "Office", "Locker", "Department", "Suspicious"});
        formPanel.add(cmbKeyFor, gbc);
        row++;
        
        // Date
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Date:"), gbc);
        gbc.gridx = 1;
        dateChooser = new JDateChooser();
        formPanel.add(dateChooser, gbc);
        row++;
        
        // Quantity
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 0, 999, 1);
        spnQuantity = new JSpinner(spinnerModel);
        formPanel.add(spnQuantity, gbc);
        row++;
        
        // Amount
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1;
        txtAmount = new JTextField(10);
        formPanel.add(txtAmount, gbc);
        row++;
        
        // Remarks
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Remarks:"), gbc);
        gbc.gridx = 1;
        txtRemarks = new JTextField(20);
        formPanel.add(txtRemarks, gbc);
        row++;
        
        // Image preview
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        JPanel imagePanel = new JPanel(new BorderLayout(5, 5));
        imagePanel.setBorder(BorderFactory.createTitledBorder("Customer Photo"));
        lblImagePreview = new JLabel();
        lblImagePreview.setHorizontalAlignment(JLabel.CENTER);
        lblImagePreview.setPreferredSize(new Dimension(150, 150));
        lblImagePreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        imagePanel.add(lblImagePreview, BorderLayout.CENTER);
        formPanel.add(imagePanel, gbc);
        
        contentPanel.add(formPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton btnSave = new JButton("Save Changes");
        btnSave.addActionListener(e -> saveChanges());
        
        JButton btnCancel = new JButton("Cancel");
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
        
        String keyFor = duplicator.getKeyType();
        if (keyFor != null && !keyFor.trim().isEmpty()) {
            cmbKeyFor.setSelectedItem(keyFor);
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
                    Image scaledImg = img.getScaledInstance(140, 140, Image.SCALE_SMOOTH);
                    lblImagePreview.setIcon(new ImageIcon(scaledImg));
                } else {
                    lblImagePreview.setText("Image not found");
                }
            } catch (Exception e) {
                lblImagePreview.setText("Error loading image");
            }
        } else {
            lblImagePreview.setText("No image");
        }
        
        updateVehicleNoVisibility();
    }
    
    private void updateVehicleNoVisibility() {
        String selectedType = (String) cmbKeyType.getSelectedItem();
        boolean showVehicleNo = "2 Wheeler".equals(selectedType) || "4 Wheeler".equals(selectedType);
        lblVehicleNo.setVisible(showVehicleNo);
        txtVehicleNo.setVisible(showVehicleNo);
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
        
        String selectedKeyFor = (String) cmbKeyFor.getSelectedItem();
        if (!"SELECT".equals(selectedKeyFor)) {
            duplicator.setKeyType(selectedKeyFor);
        } else {
            duplicator.setKeyType(null);
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
