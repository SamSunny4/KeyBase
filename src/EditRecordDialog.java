package src;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
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
    private JComboBox<String> cmbKeyCategory; // Parent Category
    private JComboBox<String> cmbKeyType; // Child Category
    private JComboBox<String> cmbKeyFor;
    private JTextField txtRemarks;
    private JSpinner spnQuantity;
    private JTextField txtAmount;
    private JRadioButton rbDuplicate;
    private JRadioButton rbInShop;
    private JRadioButton rbOnSite;
    private ButtonGroup serviceTypeGroup;
    private JRadioButton rbPaymentCash;
    private JRadioButton rbPaymentUpi;
    private ButtonGroup paymentTypeGroup;
    private JLabel lblImagePreview;
    private JButton btnDeleteImage;
    private JButton btnRecaptureImage;
    private JButton btnCaptureImage;
    private JPanel imageButtonsPanel;
    private JDateChooser dateChooser;
    private String imagePath;
    private boolean saved = false;
    // Holds a newly captured image path that is not yet committed (user hasn't clicked Save).
    private String pendingImagePath;
    
    public EditRecordDialog(Frame owner, Duplicator duplicator) {
        super(owner, "Edit Record - ID: " + duplicator.getDuplicatorId(), true);
        this.duplicator = duplicator;
        this.imagePath = duplicator.getImagePath();
        
        setSize(1000, 750);
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
        
        // Parent Category
        cmbKeyCategory = new JComboBox<>();
        for (String p : AppConfig.getParentCategories()) {
            cmbKeyCategory.addItem(p);
        }
        cmbKeyCategory.setFont(new Font("Arial", Font.PLAIN, 12));
        cmbKeyCategory.setPreferredSize(new Dimension(145, 26));
        
        // Child Category
        cmbKeyType = new JComboBox<>();
        cmbKeyType.setFont(new Font("Arial", Font.PLAIN, 12));
        cmbKeyType.setPreferredSize(new Dimension(145, 26));
        
        // Panel
        JPanel keyTypePanel = new JPanel(new GridLayout(1, 2, 5, 0));
        keyTypePanel.setBackground(Color.WHITE);
        keyTypePanel.add(cmbKeyCategory);
        keyTypePanel.add(cmbKeyType);
        
        formPanel.add(keyTypePanel, gbc);
        
        // Listeners
        cmbKeyCategory.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateChildCategories();
                updateVehicleNoVisibility();
            }
        });
        
        // Initial population
        updateChildCategories();
        
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
        
    // Key Number / Model
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.0;
    JLabel lblKeyNo = new JLabel("Key No/Model:");
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
    cmbKeyFor.setSelectedItem("Personal"); // Default to Personal
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
    dateChooser.setPreferredSize(new Dimension(160, 30));
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
    gbc.weightx = 0.0;
    gbc.fill = GridBagConstraints.NONE;
    SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 0, 999, 1);
    spnQuantity = new JSpinner(spinnerModel);
    JSpinner.DefaultEditor quantityEditor = (JSpinner.DefaultEditor) spnQuantity.getEditor();
    quantityEditor.getTextField().setFont(new Font("Arial", Font.PLAIN, 12));
    quantityEditor.getTextField().setColumns(3);
    spnQuantity.setPreferredSize(new Dimension(60, 26));
        formPanel.add(spnQuantity, gbc);
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
        row++;

        // Service Type
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.0;
        JLabel lblServiceType = new JLabel("Service Type:");
        lblServiceType.setFont(labelFont);
        lblServiceType.setForeground(labelColor);
        formPanel.add(lblServiceType, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPanel serviceTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        serviceTypePanel.setBackground(Color.WHITE);

        serviceTypeGroup = new ButtonGroup();

        rbDuplicate = new JRadioButton("Duplicate");
        rbDuplicate.setBackground(Color.WHITE);
        rbDuplicate.setFont(new Font("Arial", Font.PLAIN, 12));
    rbDuplicate.setSelected(true);

        rbInShop = new JRadioButton("In-shop");
        rbInShop.setBackground(Color.WHITE);
        rbInShop.setFont(new Font("Arial", Font.PLAIN, 12));

        rbOnSite = new JRadioButton("On-site");
        rbOnSite.setBackground(Color.WHITE);
        rbOnSite.setFont(new Font("Arial", Font.PLAIN, 12));

        serviceTypeGroup.add(rbDuplicate);
        serviceTypeGroup.add(rbInShop);
        serviceTypeGroup.add(rbOnSite);

        serviceTypePanel.add(rbDuplicate);
        serviceTypePanel.add(rbInShop);
        serviceTypePanel.add(rbOnSite);

        formPanel.add(serviceTypePanel, gbc);
        row++;

    // Payment
    gbc.gridx = 0; gbc.gridy = row;
    gbc.weightx = 0.0;
    JLabel lblPayment = new JLabel("Payment:");
    lblPayment.setFont(labelFont);
    lblPayment.setForeground(labelColor);
    formPanel.add(lblPayment, gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    paymentPanel.setBackground(Color.WHITE);

    paymentTypeGroup = new ButtonGroup();
    rbPaymentCash = new JRadioButton("Cash");
    rbPaymentCash.setBackground(Color.WHITE);
    rbPaymentCash.setFont(new Font("Arial", Font.PLAIN, 12));
    rbPaymentUpi = new JRadioButton("UPI");
    rbPaymentUpi.setBackground(Color.WHITE);
    rbPaymentUpi.setFont(new Font("Arial", Font.PLAIN, 12));
    paymentTypeGroup.add(rbPaymentCash);
    paymentTypeGroup.add(rbPaymentUpi);
    rbPaymentCash.setSelected(true);
    paymentPanel.add(rbPaymentCash);
    paymentPanel.add(rbPaymentUpi);
    formPanel.add(paymentPanel, gbc);
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
    imagePanel.setPreferredSize(new Dimension(320, 0));
        
        lblImagePreview = new JLabel();
        lblImagePreview.setHorizontalAlignment(JLabel.CENTER);
        lblImagePreview.setVerticalAlignment(JLabel.CENTER);
    lblImagePreview.setPreferredSize(new Dimension(280, 280));
        lblImagePreview.setBackground(new Color(250, 250, 250));
        lblImagePreview.setOpaque(true);
        lblImagePreview.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(109, 193, 210), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        imagePanel.add(lblImagePreview, BorderLayout.CENTER);
        
    // Image action buttons panel (dynamic: Capture OR Recapture/Delete)
    imageButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
    imageButtonsPanel.setBackground(Color.WHITE);

    btnCaptureImage = new JButton("Capture");
    btnCaptureImage.setFont(new Font("Arial", Font.BOLD, 10));
    btnCaptureImage.setBackground(new Color(109, 193, 210));
    btnCaptureImage.setForeground(new Color(60, 62, 128));
    btnCaptureImage.setFocusPainted(false);
    btnCaptureImage.setPreferredSize(new Dimension(110, 26));
    btnCaptureImage.setToolTipText("Capture a new image");
    btnCaptureImage.addActionListener(e -> captureNewImage());

    btnRecaptureImage = new JButton("Recapture");
    btnRecaptureImage.setFont(new Font("Arial", Font.BOLD, 10));
    btnRecaptureImage.setBackground(new Color(109, 193, 210));
    btnRecaptureImage.setForeground(new Color(60, 62, 128));
    btnRecaptureImage.setFocusPainted(false);
    btnRecaptureImage.setPreferredSize(new Dimension(110, 26));
    btnRecaptureImage.setToolTipText("Capture a new image to replace the current one");
    btnRecaptureImage.addActionListener(e -> recaptureImage());

    btnDeleteImage = new JButton("Delete Image");
    btnDeleteImage.setFont(new Font("Arial", Font.BOLD, 10));
    btnDeleteImage.setBackground(new Color(220, 80, 80));
    btnDeleteImage.setForeground(Color.RED);
    btnDeleteImage.setFocusPainted(false);
    btnDeleteImage.setPreferredSize(new Dimension(110, 26));
    btnDeleteImage.setToolTipText("Delete the captured image");
    btnDeleteImage.addActionListener(e -> deleteImage());

    // Add all; visibility toggled via updateImageButtonsState()
    imageButtonsPanel.add(btnCaptureImage);
    imageButtonsPanel.add(btnRecaptureImage);
    imageButtonsPanel.add(btnDeleteImage);
    imagePanel.add(imageButtonsPanel, BorderLayout.SOUTH);
        
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
        
        setupEnterNavigation();
    }

    private void setupEnterNavigation() {
        // Name -> Phone
        txtName.addActionListener(e -> txtPhoneNumber.requestFocus());
        
        // Phone -> Category
        txtPhoneNumber.addActionListener(e -> cmbKeyCategory.requestFocus());
        
        // Category -> Type (using InputMap for JComboBox)
        InputMap categoryInputMap = cmbKeyCategory.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap categoryActionMap = cmbKeyCategory.getActionMap();
        categoryInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterPressed");
        categoryActionMap.put("enterPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cmbKeyCategory.isPopupVisible()) cmbKeyCategory.setPopupVisible(false);
                cmbKeyType.requestFocus();
            }
        });
        
        // Type -> Vehicle No (if visible) or ID No
        InputMap typeInputMap = cmbKeyType.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap typeActionMap = cmbKeyType.getActionMap();
        typeInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterPressed");
        typeActionMap.put("enterPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cmbKeyType.isPopupVisible()) cmbKeyType.setPopupVisible(false);
                if (txtVehicleNo.isVisible()) {
                    txtVehicleNo.requestFocus();
                } else {
                    txtIdNo.requestFocus();
                }
            }
        });
        
        // Vehicle No -> ID No
        txtVehicleNo.addActionListener(e -> txtIdNo.requestFocus());
        
        // ID No -> Key No
        txtIdNo.addActionListener(e -> txtKeyNo.requestFocus());
        
        // Key No -> Purpose
        txtKeyNo.addActionListener(e -> cmbKeyFor.requestFocus());
        
        // Purpose -> Date
        InputMap purposeInputMap = cmbKeyFor.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap purposeActionMap = cmbKeyFor.getActionMap();
        purposeInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterPressed");
        purposeActionMap.put("enterPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cmbKeyFor.isPopupVisible()) cmbKeyFor.setPopupVisible(false);
                dateChooser.getDateField().requestFocus();
            }
        });
        
        // Date -> Quantity
        dateChooser.getDateField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    spnQuantity.requestFocus();
                }
            }
        });
        
        // Quantity -> Amount
        if (spnQuantity.getEditor() instanceof JSpinner.DefaultEditor) {
            JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spnQuantity.getEditor();
            editor.getTextField().addActionListener(e -> txtAmount.requestFocus());
        }
        
        // Amount -> Remarks
        txtAmount.addActionListener(e -> txtRemarks.requestFocus());
        
        // Remarks -> Save
        txtRemarks.addActionListener(e -> saveChanges());
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
        }

        String keyType = duplicator.getKeyType();
        if (keyType != null && !keyType.trim().isEmpty()) {
            String parent = AppConfig.findParentForChild(keyType);
            if (parent != null) {
                cmbKeyCategory.setSelectedItem(parent);
            }
            // Ensure the child is selected (listener updates list if parent changed)
            cmbKeyType.setSelectedItem(keyType);
        }
        
        // Load Purpose
        String purpose = duplicator.getPurpose();
        if (purpose != null && !purpose.trim().isEmpty()) {
            cmbKeyFor.setSelectedItem(purpose);
        }
        
        if (duplicator.getDateAdded() != null) {
            dateChooser.setDate(duplicator.getDateAdded());
        } else {
            dateChooser.setDate(new Date());
        }
        
        spnQuantity.setValue(duplicator.getQuantity());
        txtAmount.setText(String.format("%.2f", duplicator.getAmount()));

        ServiceTypeHelper.ServiceType serviceType = ServiceTypeHelper.detectServiceType(duplicator.getRemarks());
        if (serviceType == ServiceTypeHelper.ServiceType.IN_SHOP) {
            rbInShop.setSelected(true);
        } else if (serviceType == ServiceTypeHelper.ServiceType.ON_SITE) {
            rbOnSite.setSelected(true);
        } else {
            rbDuplicate.setSelected(true);
        }

        // Detect payment (UPI) and preselect
        boolean hasUpi = ServiceTypeHelper.hasUpi(duplicator.getRemarks());
        if (hasUpi) {
            rbPaymentUpi.setSelected(true);
        } else {
            rbPaymentCash.setSelected(true);
        }

        // Show remarks without service/payment suffixes
        String withoutService = ServiceTypeHelper.stripServiceSuffix(duplicator.getRemarks());
        String withoutPayment = ServiceTypeHelper.stripPaymentSuffix(withoutService);
        txtRemarks.setText(withoutPayment);
        
        // Load image
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    BufferedImage img = ImageIO.read(imageFile);
                    Image scaledImg = img.getScaledInstance(260, 260, Image.SCALE_SMOOTH);
                    lblImagePreview.setIcon(new ImageIcon(scaledImg));
                    // image present
                } else {
                    lblImagePreview.setText("Image not found");
                    imagePath = null; // treat as no image
                }
            } catch (Exception e) {
                lblImagePreview.setText("Error loading image");
                imagePath = null;
            }
        } else {
            lblImagePreview.setText("No image");
        }
        
        updateVehicleNoVisibility();
        updateImageButtonsState();
    }
    
    private void updateChildCategories() {
        String parent = (String) cmbKeyCategory.getSelectedItem();
        cmbKeyType.removeAllItems();
        if (parent != null) {
            for (String child : AppConfig.getChildCategories(parent)) {
                cmbKeyType.addItem(child);
            }
        }
    }

    private void updateVehicleNoVisibility() {
        String selectedParent = (String) cmbKeyCategory.getSelectedItem();
        boolean showVehicleNo = "Vehicles".equals(selectedParent);
        if (lblVehicleNo != null) lblVehicleNo.setVisible(showVehicleNo);
        if (txtVehicleNo != null) txtVehicleNo.setVisible(showVehicleNo);
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
                        updateImageButtonsState();
                        
                        JOptionPane.showMessageDialog(this,
                            "Image deleted successfully.",
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
                    updateImageButtonsState();
                    
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

    private void recaptureImage() {
        // Launch capture for replacing existing image but delay commit until Save.
        WebcamCapture captureDialog = new WebcamCapture(this);
        captureDialog.setVisible(true);
        if (!captureDialog.isImageCaptured()) return;

        String newImagePath = captureDialog.getSavedImagePath();
        if (newImagePath == null || newImagePath.trim().isEmpty()) return;

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Save this new image as a replacement? It will apply only after you click 'Save Changes'.",
            "Confirm Recapture",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            try { new File(newImagePath).delete(); } catch (Exception ignore) {}
            return;
        }

        // Clean up any previous pending file
        discardPendingImageFile();
        pendingImagePath = newImagePath;

        try {
            BufferedImage newImg = ImageIO.read(new File(pendingImagePath));
            Image scaledImg = newImg.getScaledInstance(260, 260, Image.SCALE_SMOOTH);
            lblImagePreview.setIcon(new ImageIcon(scaledImg));
            lblImagePreview.setText("");
            updateImageButtonsState();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Failed to load staged image: " + ex.getMessage(),
                "Image Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void captureNewImage() {
        // First capture when no existing image; stage pending until Save.
        WebcamCapture captureDialog = new WebcamCapture(this);
        captureDialog.setVisible(true);
        if (!captureDialog.isImageCaptured()) return;
        String newImagePath = captureDialog.getSavedImagePath();
        if (newImagePath == null || newImagePath.trim().isEmpty()) return;
        discardPendingImageFile();
        pendingImagePath = newImagePath;
        try {
            BufferedImage newImg = ImageIO.read(new File(pendingImagePath));
            Image scaledImg = newImg.getScaledInstance(260, 260, Image.SCALE_SMOOTH);
            lblImagePreview.setIcon(new ImageIcon(scaledImg));
            lblImagePreview.setText("");
            updateImageButtonsState();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Failed to load captured image: " + ex.getMessage(),
                "Image Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateImageButtonsState() {
        // Effective image shown: either committed (imagePath) or staged (pendingImagePath)
        boolean hasEffectiveImage = (pendingImagePath != null && !pendingImagePath.trim().isEmpty()) || (pendingImagePath == null && imagePath != null && !imagePath.trim().isEmpty());
        if (btnCaptureImage != null) btnCaptureImage.setVisible(!hasEffectiveImage);
        if (btnRecaptureImage != null) btnRecaptureImage.setVisible(hasEffectiveImage);
        if (btnDeleteImage != null) {
            btnDeleteImage.setVisible(hasEffectiveImage);
            btnDeleteImage.setEnabled(hasEffectiveImage);
        }
        if (imageButtonsPanel != null) {
            imageButtonsPanel.revalidate();
            imageButtonsPanel.repaint();
        }
    }

    private void saveChanges() {
        // Validate using AppConfig required fields
        java.util.Set<String> required = AppConfig.getRequiredFields();
        
        if (required.contains("NAME") && txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtName.requestFocus();
            return;
        }
        
        if (required.contains("PHONE") && txtPhoneNumber.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phone number is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtPhoneNumber.requestFocus();
            return;
        }
        
        if (required.contains("ID_NO") && txtIdNo.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID number is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtIdNo.requestFocus();
            return;
        }
        
        if (required.contains("VEHICLE_NO") && txtVehicleNo.isVisible() && txtVehicleNo.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vehicle number is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtVehicleNo.requestFocus();
            return;
        }
        
        if (required.contains("KEY_NO") && txtKeyNo.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Key number is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtKeyNo.requestFocus();
            return;
        }
        
        if (required.contains("PURPOSE") && "SELECT".equals(cmbKeyFor.getSelectedItem())) {
            JOptionPane.showMessageDialog(this, "Purpose is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            cmbKeyFor.requestFocus();
            return;
        }
        
        if (required.contains("REMARKS") && txtRemarks.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Remarks is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtRemarks.requestFocus();
            return;
        }
        
        if (required.contains("AMOUNT")) {
            try {
                double amt = Double.parseDouble(txtAmount.getText().trim());
                if (amt < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Valid amount is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                txtAmount.requestFocus();
                return;
            }
        }

        // Update duplicator object
        duplicator.setName(txtName.getText().trim());
        duplicator.setPhoneNumber(txtPhoneNumber.getText().trim());
        duplicator.setIdNo(txtIdNo.getText().trim());
        duplicator.setKeyNo(txtKeyNo.getText().trim());
        String selectedKeyType = (String) cmbKeyType.getSelectedItem();
        if (!"SELECT".equals(selectedKeyType)) {
            duplicator.setKeyType(selectedKeyType);
        } else {
            duplicator.setKeyType(null);
        }
        
        // Save vehicle number
        duplicator.setVehicleNo(txtVehicleNo.getText().trim());
        
        // Save Purpose
        String selectedPurpose = (String) cmbKeyFor.getSelectedItem();
        if (!"SELECT".equals(selectedPurpose)) {
            duplicator.setPurpose(selectedPurpose);
        } else {
            duplicator.setPurpose(null);
        }
        
        Date selectedDate = dateChooser.getDate();
        if (selectedDate == null) {
            selectedDate = new Date();
        }
        duplicator.setDateAdded(selectedDate);
        duplicator.setQuantity((Integer) spnQuantity.getValue());
        
        try {
            double amount = Double.parseDouble(txtAmount.getText().trim());
            duplicator.setAmount(amount);
        } catch (NumberFormatException e) {
            duplicator.setAmount(0.00);
        }
        
        ServiceTypeHelper.ServiceType selectedServiceType;
        if (rbInShop.isSelected()) {
            selectedServiceType = ServiceTypeHelper.ServiceType.IN_SHOP;
        } else if (rbOnSite.isSelected()) {
            selectedServiceType = ServiceTypeHelper.ServiceType.ON_SITE;
        } else {
            selectedServiceType = ServiceTypeHelper.ServiceType.DUPLICATE;
        }
        String base = txtRemarks.getText().trim();
        String finalRemarks = ServiceTypeHelper.applyServiceType(base, selectedServiceType);
        // apply payment suffix if needed
        if (rbPaymentUpi.isSelected()) {
            finalRemarks = ServiceTypeHelper.applyPaymentSuffix(finalRemarks, true);
        }
        duplicator.setRemarks(finalRemarks);
        
        // Commit pending image if present
        if (pendingImagePath != null && !pendingImagePath.trim().isEmpty()) {
            try {
                if (imagePath != null && !imagePath.trim().isEmpty()) {
                    // Overwrite existing image file contents to keep same path reference
                    BufferedImage stagedImg = ImageIO.read(new File(pendingImagePath));
                    ImageIO.write(stagedImg, "JPG", new File(imagePath));
                    // Remove the staged file if different
                    if (!pendingImagePath.equals(imagePath)) {
                        discardPendingImageFile();
                    }
                } else {
                    // No existing image; adopt pending as official
                    imagePath = pendingImagePath;
                    pendingImagePath = null; // adopted
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Failed to commit staged image: " + ex.getMessage(),
                    "Image Commit Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }

        // Update image path on duplicator
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

    @Override
    public void dispose() {
        // If dialog closed without saving, discard any staged image
        if (!saved) {
            discardPendingImageFile();
        }
        super.dispose();
    }

    private void discardPendingImageFile() {
        if (pendingImagePath != null) {
            try {
                File f = new File(pendingImagePath);
                if (f.exists()) f.delete();
            } catch (Exception ignore) {}
            pendingImagePath = null;
        }
    }
}
