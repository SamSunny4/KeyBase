package src;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.swing.*;
import src.qrcodegen.QrCode;

/**
 * License Manager for KeyBase Application
 * Provides hardware-based license verification using CPU and Motherboard ID
 */
public class LicenseManager {
    
    private static final String LICENSE_FILE = "keybase.lic";
    private static final String ENCRYPTION_KEY_BASE = "KeyBase2025SecureApp";
    
    private String hardwareId;
    private String encryptionPin;
    
    public LicenseManager() {
        this.hardwareId = generateHardwareId();
        this.encryptionPin = generateEncryptionPin(hardwareId);
    }
    
    /**
     * Generate unique hardware ID based on CPU and Motherboard
     */
    private String generateHardwareId() {
        StringBuilder hwId = new StringBuilder();
        
        try {
            // Get CPU ID (processor identifier)
            String cpuId = getCPUId();
            hwId.append(cpuId);
            
            // Get Motherboard Serial Number
            String motherboardId = getMotherboardId();
            hwId.append(motherboardId);
            
        } catch (Exception e) {
            System.err.println("Error generating hardware ID: " + e.getMessage());
            // Fallback to user name and computer name
            hwId.append(System.getProperty("user.name"));
            hwId.append(System.getProperty("os.name"));
        }
        
        return hashString(hwId.toString());
    }
    
    /**
     * Get CPU Identifier
     */
    private String getCPUId() {
        String cpuId = "";
        try {
            Process process = Runtime.getRuntime().exec(
                new String[]{"wmic", "cpu", "get", "ProcessorId"}
            );
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.equalsIgnoreCase("ProcessorId")) {
                    cpuId = line;
                    break;
                }
            }
            reader.close();
        } catch (Exception e) {
            System.err.println("Error getting CPU ID: " + e.getMessage());
        }
        return cpuId;
    }
    
    /**
     * Get Motherboard Serial Number
     */
    private String getMotherboardId() {
        String motherboardId = "";
        try {
            Process process = Runtime.getRuntime().exec(
                new String[]{"wmic", "baseboard", "get", "SerialNumber"}
            );
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.equalsIgnoreCase("SerialNumber")) {
                    motherboardId = line;
                    break;
                }
            }
            reader.close();
        } catch (Exception e) {
            System.err.println("Error getting Motherboard ID: " + e.getMessage());
        }
        return motherboardId;
    }
    
    /**
     * Generate encryption PIN from hardware ID
     */
    private String generateEncryptionPin(String hwId) {
        return hashString(hwId + ENCRYPTION_KEY_BASE).substring(0, 32);
    }
    
    /**
     * Hash a string using SHA-256
     */
    private String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            System.err.println("Error hashing string: " + e.getMessage());
            return input;
        }
    }
    
    /**
     * Check if license file exists and is valid
     */
    public boolean isLicenseValid() {
        File licenseFile = new File(LICENSE_FILE);
        
        if (!licenseFile.exists()) {
            return false;
        }
        
        try {
            String licenseKey = readLicenseFile();
            
            // Verify the 14-character license key
            return verifyShortKey(licenseKey, hardwareId);
            
        } catch (Exception e) {
            System.err.println("License validation error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Create license file from license key
     */
    public boolean createLicenseFromKey(String licenseKey) {
        try {
            // Verify the 14-character license key
            if (!verifyShortKey(licenseKey, hardwareId)) {
                return false;
            }
            
            // Save the license key to file
            writeLicenseFile(licenseKey);
            return true;
            
        } catch (Exception e) {
            System.err.println("Error creating license: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate a license key for this hardware
     * This method would typically be used by the license generator tool
     */
    public String generateLicenseKey() {
        try {
            // Generate 14-character license key from hardware ID
            return generateShortKey(hardwareId);
        } catch (Exception e) {
            System.err.println("Error generating license key: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Generate a 14-character license key from hardware ID
     */
    private String generateShortKey(String hwId) {
        // Combine hardware ID with encryption key base
        String combined = hwId + ENCRYPTION_KEY_BASE;
        String hash = hashString(combined);
        
        // Take first 14 characters of hash and format with dashes
        // Format: XXXX-XXXX-XXXX-XX (14 chars including dashes)
        String key = hash.substring(0, 14).toUpperCase();
        return String.format("%s-%s-%s-%s", 
            key.substring(0, 4),
            key.substring(4, 8),
            key.substring(8, 12),
            key.substring(12, 14));
    }
    
    /**
     * Verify a 14-character license key
     */
    private boolean verifyShortKey(String licenseKey, String hwId) {
        try {
            // Remove dashes and convert to lowercase for comparison
            String cleanKey = licenseKey.replace("-", "").toLowerCase();
            String expectedKey = generateShortKey(hwId).replace("-", "").toLowerCase();
            
            return cleanKey.equals(expectedKey);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Read license from file
     */
    private String readLicenseFile() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(LICENSE_FILE));
        String license = reader.readLine();
        reader.close();
        return license;
    }
    
    /**
     * Write license to file
     */
    private void writeLicenseFile(String license) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(LICENSE_FILE));
        writer.write(license);
        writer.close();
    }
    
    /**
     * Show license key input dialog
     */
    public boolean promptForLicenseKey(JFrame parent) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel messageLabel = new JLabel("<html><b>License Key Required</b><br><br>" +
            "Please enter your license key to activate KeyBase:<br>" +
            "<i>(Click 'Get License' to generate QR code for your administrator)</i></html>");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(messageLabel, gbc);
        
        JLabel keyLabel = new JLabel("License Key:");
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        panel.add(keyLabel, gbc);
        
        JTextField keyField = new JTextField(30);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(keyField, gbc);
        
        // Get License button (generates QR code with hardware ID)
        JButton getLicenseButton = new JButton("Get License");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(10, 5, 5, 5);
        getLicenseButton.addActionListener(e -> showQRCodeDialog(parent));
        panel.add(getLicenseButton, gbc);
        
        while (true) {
            int result = JOptionPane.showConfirmDialog(
                parent,
                panel,
                "KeyBase License Activation",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );
            
            if (result == JOptionPane.OK_OPTION) {
                String licenseKey = keyField.getText().trim();
                
                if (licenseKey.isEmpty()) {
                    JOptionPane.showMessageDialog(parent,
                        "Please enter a license key.",
                        "Invalid Input",
                        JOptionPane.WARNING_MESSAGE);
                    continue;
                }
                
                if (createLicenseFromKey(licenseKey)) {
                    JOptionPane.showMessageDialog(parent,
                        "License activated successfully!\nKeyBase will now start.",
                        "Activation Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                    return true;
                } else {
                    JOptionPane.showMessageDialog(parent,
                        "Invalid license key for this computer.\n" +
                        "Please contact support with your Hardware ID.",
                        "Activation Failed",
                        JOptionPane.ERROR_MESSAGE);
                    continue;
                }
            } else {
                // User cancelled
                return false;
            }
        }
    }
    
    /**
     * Show QR code dialog with hardware ID
     */
    private void showQRCodeDialog(JFrame parent) {
        JDialog qrDialog = new JDialog(parent, "Get License - Hardware ID", true);
        qrDialog.setLayout(new BorderLayout(10, 10));
        qrDialog.setSize(700, 450);
        qrDialog.setLocationRelativeTo(parent);
        
        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(60, 62, 128));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel headerLabel = new JLabel("<html><b style='font-size:14px;'>Send this QR Code to your Administrator</b></html>");
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        
        qrDialog.add(headerPanel, BorderLayout.NORTH);
        
        // Main panel with horizontal split
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Left panel - Instructions and Hardware ID
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBackground(Color.WHITE);
        
        // Instructions
        JTextArea instructions = new JTextArea(
            "1. Take a screenshot of the QR code on the right\n\n" +
            "2. Send it to your system administrator\n\n" +
            "3. They will generate a license key for you\n\n" +
            "4. Enter the license key in the activation dialog"
        );
        instructions.setEditable(false);
        instructions.setBackground(new Color(255, 255, 220));
        instructions.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 150)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        instructions.setFont(new Font("Arial", Font.PLAIN, 12));
        instructions.setLineWrap(true);
        instructions.setWrapStyleWord(true);
        leftPanel.add(instructions, BorderLayout.NORTH);
        
        // Hardware ID text (fallback)
        JPanel idPanel = new JPanel(new BorderLayout(5, 5));
        idPanel.setBackground(Color.WHITE);
        idPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JLabel idLabel = new JLabel("Hardware ID (if QR code fails):");
        idLabel.setFont(new Font("Arial", Font.BOLD, 11));
        idPanel.add(idLabel, BorderLayout.NORTH);
        
        JTextArea idField = new JTextArea(hardwareId);
        idField.setEditable(false);
        idField.setFont(new Font("Monospaced", Font.PLAIN, 10));
        idField.setLineWrap(true);
        idField.setWrapStyleWord(true);
        idField.setBackground(new Color(240, 240, 240));
        idField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        JScrollPane scrollPane = new JScrollPane(idField);
        scrollPane.setPreferredSize(new Dimension(300, 80));
        idPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Copy button
        JButton copyButton = new JButton("Copy Hardware ID");
        copyButton.addActionListener(e -> {
            java.awt.datatransfer.StringSelection stringSelection = 
                new java.awt.datatransfer.StringSelection(hardwareId);
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            JOptionPane.showMessageDialog(qrDialog, "Hardware ID copied to clipboard!", 
                "Copied", JOptionPane.INFORMATION_MESSAGE);
        });
        idPanel.add(copyButton, BorderLayout.SOUTH);
        
        leftPanel.add(idPanel, BorderLayout.CENTER);
        
        mainPanel.add(leftPanel, BorderLayout.CENTER);
        
        // Right panel - QR Code (200x200)
        JPanel qrPanel = new JPanel(new BorderLayout());
        qrPanel.setBackground(Color.WHITE);
        qrPanel.setPreferredSize(new Dimension(220, 220));
        
        JPanel qrContainer = new JPanel();
        qrContainer.setBackground(Color.WHITE);
        qrContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        try {
            BufferedImage qrImage = generateQRCode(hardwareId, 200);
            JLabel qrLabel = new JLabel(new ImageIcon(qrImage));
            qrContainer.add(qrLabel);
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("<html><center>QR Code generation failed<br>Please use Hardware ID</center></html>");
            errorLabel.setForeground(Color.RED);
            qrContainer.add(errorLabel);
        }
        
        qrPanel.add(qrContainer, BorderLayout.CENTER);
        
        mainPanel.add(qrPanel, BorderLayout.EAST);
        
        qrDialog.add(mainPanel, BorderLayout.CENTER);
        
        // Close button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> qrDialog.dispose());
        buttonPanel.add(closeButton);
        qrDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        qrDialog.setVisible(true);
    }
    
    /**
     * Generate a scannable QR Code for the given text using QrCode library
     */
    private BufferedImage generateQRCode(String text, int size) throws Exception {
        // Generate QR code using the QrCode library
        QrCode qr = QrCode.encodeText(text, QrCode.Ecc.MEDIUM);
        
        // Convert to BufferedImage with specified size
        int scale = size / qr.size;
        int border = 2; // Quiet zone
        
        return qr.toImage(scale, border);
    }
    
    /**
     * Get hardware ID (for license generation)
     */
    public String getHardwareId() {
        return hardwareId;
    }
    
    /**
     * Get encryption PIN (for debugging/license generation)
     */
    public String getEncryptionPin() {
        return encryptionPin;
    }
}
