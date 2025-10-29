package src;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.*;

public class KeyBase {
    public static void main(String[] args) {
        // Create and show splash screen
        SplashScreen splash = new SplashScreen();
        splash.setVisible(true);
        
        // Run initialization in background thread
        new Thread(() -> {
            try {
                // Ensure proper directory structure exists
                splash.updateStatus("Setting up directories...");
                Thread.sleep(300);
                setupDirectories();
                
                // Set Look and Feel to match system
                splash.updateStatus("Loading UI components...");
                Thread.sleep(300);
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    System.err.println("Failed to set system look and feel: " + e.getMessage());
                }
                
                // Test database connection
                splash.updateStatus("Connecting to database...");
                Thread.sleep(300);
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    System.out.println("Database connection successful!");
                    DatabaseConnection.closeConnection();
                } catch (SQLException e) {
                    splash.close();
                    showDatabaseConnectionError(e);
                    return;
                }
                
                // Start the main application
                splash.updateStatus("Loading main window...");
                Thread.sleep(300);
                SwingUtilities.invokeLater(() -> {
                    MainForm mainForm = new MainForm();
                    mainForm.setVisible(true);
                    
                    // Close splash screen after main form is visible
                    splash.close();
                });
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                splash.close();
            }
        }).start();
    }
    
    private static void setupDirectories() {
        // Create images directory if it doesn't exist
        String imagesDirectory = AppConfig.getImagesDirectory();
        File imagesDir = new File(imagesDirectory);
        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
        }
        
        // Create config directory if it doesn't exist
        File configDir = new File("config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
    }
    
    private static void showDatabaseConnectionError(SQLException e) {
        String message = 
            "Failed to connect to the database.\n\n" +
            "Error: " + e.getMessage() + "\n\n" +
            "Please ensure that:\n" +
            "1. MySQL/MariaDB server is running\n" +
            "2. Database credentials in config/database.properties are correct\n" +
            "3. KeyBase database exists (run the SQL script in config/init_database.sql)\n\n" +
            "Would you like to continue without database connection?";
            
        int response = JOptionPane.showConfirmDialog(
            null, message, "Database Connection Error", 
            JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            
        if (response == JOptionPane.YES_OPTION) {
            // Continue with limited functionality
            SwingUtilities.invokeLater(() -> {
                MainForm mainForm = new MainForm();
                mainForm.setVisible(true);
            });
        }
    }
}