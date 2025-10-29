package src;

import java.awt.*;
import java.io.File;
import javax.swing.*;

public class SplashScreen extends JWindow {
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel logoLabel;
    private ImageIcon appIcon;
    
    public SplashScreen() {
        createSplashScreen();
    }
    
    private void createSplashScreen() {
        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());
        content.setBackground(new Color(255, 255, 255)); // White background
        content.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(109, 193, 210), 4), // #6DC1D2 thicker border
            BorderFactory.createEmptyBorder(10, 10, 10, 10) // Inner padding
        ));
        
        // Logo panel
        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(new Color(255, 255, 255));
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30)); // More padding
        
        // Load and display logo
        logoLabel = new JLabel();
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        try {
            // Try to load splash.png from resources folder
            File logoFile = new File("resources/splash.png");
            if (logoFile.exists()) {
                ImageIcon icon = new ImageIcon(logoFile.getAbsolutePath());
                appIcon = icon; // Store for title bar use
                
                // Scale the square logo larger
                Image img = icon.getImage();
                Image scaledImg = img.getScaledInstance(200, 200, Image.SCALE_SMOOTH); // Larger square logo
                logoLabel.setIcon(new ImageIcon(scaledImg));
            } else {
                // Fallback text if image not found
                logoLabel.setText("KeyBase");
                logoLabel.setFont(new Font("Arial", Font.BOLD, 48));
                logoLabel.setForeground(new Color(109, 193, 210)); // #6DC1D2
            }
        } catch (Exception e) {
            // Fallback text if image loading fails
            logoLabel.setText("KeyBase");
            logoLabel.setFont(new Font("Arial", Font.BOLD, 48));
            logoLabel.setForeground(new Color(109, 193, 210)); // #6DC1D2
        }
        
        logoPanel.add(logoLabel);
        
        // Status label with larger font
        statusLabel = new JLabel("Initializing...");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14)); // Larger font
        statusLabel.setForeground(new Color(60, 62, 128)); // #3C3E80
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(15, 30, 10, 30));
        
        // Progress bar - larger and more prominent
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBackground(Color.WHITE);
        progressPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 30, 30));
        
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setForeground(new Color(109, 193, 210)); // #6DC1D2
        progressBar.setBackground(new Color(230, 230, 230));
        progressBar.setPreferredSize(new Dimension(400, 20)); // Thicker progress bar
        progressBar.setBorderPainted(true);
        progressBar.setBorder(BorderFactory.createLineBorder(new Color(109, 193, 210), 2));
        
        progressPanel.add(progressBar, BorderLayout.CENTER);
        
        // Add components
        content.add(logoPanel, BorderLayout.NORTH);
        content.add(statusLabel, BorderLayout.CENTER);
        content.add(progressPanel, BorderLayout.SOUTH);
        
        setContentPane(content);
        setSize(550, 480); // Larger window size
        setLocationRelativeTo(null);
    }
    
    // Method to get the icon for use in title bar
    public ImageIcon getAppIcon() {
        return appIcon;
    }
    
    public void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(status);
        });
    }
    
    public void setProgress(int value) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(false);
            progressBar.setValue(value);
        });
    }
    
    public void close() {
        SwingUtilities.invokeLater(() -> {
            setVisible(false);
            dispose();
        });
    }
}
