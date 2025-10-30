package src;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import javax.swing.*;

/**
 * License Key Generator Utility
 * Used by administrators to generate license keys for specific computers
 */
public class LicenseKeyGenerator extends JFrame {
    
    private JTextField hardwareIdField;
    private JTextArea licenseKeyArea;
    private JButton generateButton;
    private JButton copyButton;
    private JButton testButton;
    
    public LicenseKeyGenerator() {
        setTitle("KeyBase License Key Generator");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(60, 62, 128));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("KeyBase License Key Generator");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.BLACK);
        headerPanel.add(titleLabel);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main Panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Instructions
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel instructionsLabel = new JLabel("<html><b>Instructions:</b><br>" +
            "1. Get the Hardware ID from the client computer<br>" +
            "2. Enter it below and click 'Generate License Key'<br>" +
            "3. Send the generated key to the client</html>");
        mainPanel.add(instructionsLabel, gbc);
        
        // Hardware ID input
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        JLabel hwIdLabel = new JLabel("Hardware ID:");
        hwIdLabel.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(hwIdLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        hardwareIdField = new JTextField(30);
        hardwareIdField.setFont(new Font("Monospaced", Font.PLAIN, 12));
        mainPanel.add(hardwareIdField, gbc);
        
        // Generate button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        generateButton = new JButton("Generate License Key");
        generateButton.setFont(new Font("Arial", Font.BOLD, 14));
        generateButton.setBackground(new Color(60, 62, 128));
        generateButton.setForeground(Color.BLUE);
        generateButton.setFocusPainted(false);
        generateButton.addActionListener(e -> generateLicenseKey());
        mainPanel.add(generateButton, gbc);
        
        // License Key output
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        JLabel keyLabel = new JLabel("License Key:");
        keyLabel.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(keyLabel, gbc);
        
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        licenseKeyArea = new JTextArea(5, 30);
        licenseKeyArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        licenseKeyArea.setLineWrap(true);
        licenseKeyArea.setWrapStyleWord(true);
        licenseKeyArea.setEditable(false);
        licenseKeyArea.setBackground(new Color(250, 250, 250));
        JScrollPane scrollPane = new JScrollPane(licenseKeyArea);
        mainPanel.add(scrollPane, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        copyButton = new JButton("Copy to Clipboard");
        copyButton.setEnabled(false);
        copyButton.addActionListener(e -> copyToClipboard());
        buttonPanel.add(copyButton);
        
        testButton = new JButton("Test Current Machine");
        testButton.addActionListener(e -> testCurrentMachine());
        buttonPanel.add(testButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void generateLicenseKey() {
        String hardwareId = hardwareIdField.getText().trim();
        
        if (hardwareId.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a Hardware ID.",
                "Missing Input",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Create a temporary license manager with the provided hardware ID
            String licenseKey = generateKeyForHardwareId(hardwareId);
            
            if (licenseKey != null) {
                licenseKeyArea.setText(licenseKey);
                copyButton.setEnabled(true);
                
                JOptionPane.showMessageDialog(this,
                    "License key generated successfully!\nYou can now copy it to clipboard.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error generating license key.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String generateKeyForHardwareId(String hardwareId) {
        try {
            // Generate 14-character license key from hardware ID
            return generateShortKey(hardwareId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Generate a 14-character license key from hardware ID
     */
    private String generateShortKey(String hwId) {
        String ENCRYPTION_KEY_BASE = "KeyBase2025SecureApp";
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
    
    private String hashString(String input) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return input;
        }
    }
    
    private void copyToClipboard() {
        String licenseKey = licenseKeyArea.getText();
        if (!licenseKey.isEmpty()) {
            StringSelection stringSelection = new StringSelection(licenseKey);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            
            JOptionPane.showMessageDialog(this,
                "License key copied to clipboard!",
                "Copied",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void testCurrentMachine() {
        LicenseManager licenseManager = new LicenseManager();
        String hardwareId = licenseManager.getHardwareId();
        String licenseKey = licenseManager.generateLicenseKey();
        
        hardwareIdField.setText(hardwareId);
        licenseKeyArea.setText(licenseKey);
        copyButton.setEnabled(true);
        
        JOptionPane.showMessageDialog(this,
            "Generated license key for THIS computer.\n" +
            "Hardware ID: " + hardwareId.substring(0, Math.min(16, hardwareId.length())) + "...",
            "Test Mode",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            LicenseKeyGenerator generator = new LicenseKeyGenerator();
            generator.setVisible(true);
        });
    }
}
