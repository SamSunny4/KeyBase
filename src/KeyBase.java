package src;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;

public class KeyBase {
    public static void main(String[] args) {
        // Create and show splash screen
        SplashScreen splash = new SplashScreen();
        splash.setVisible(true);
        
        // Run initialization in background thread
        startInitialization(splash);
    }
    
    private static void startInitialization(SplashScreen splash) {
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
                
                // Verify license
                splash.updateStatus("Verifying license...");
                Thread.sleep(300);
                LicenseManager licenseManager = new LicenseManager();
                
                if (!licenseManager.isLicenseValid()) {
                    // Close splash and show license prompt
                    splash.close();
                    
                    SwingUtilities.invokeLater(() -> {
                        boolean licenseActivated = licenseManager.promptForLicenseKey(null);
                        
                        if (!licenseActivated) {
                            JOptionPane.showMessageDialog(null,
                                "KeyBase cannot start without a valid license.\nApplication will now exit.",
                                "License Required",
                                JOptionPane.ERROR_MESSAGE);
                            System.exit(0);
                        } else {
                            // License activated, restart the splash and continue
                            SplashScreen newSplash = new SplashScreen();
                            newSplash.setVisible(true);
                            startInitialization(newSplash);
                        }
                    });
                    return;
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
            } catch (Throwable t) {
                handleFatalStartupError(t, splash);
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

    private static void handleFatalStartupError(Throwable throwable, SplashScreen splash) {
        String logPath = logFatalError(throwable);
        SwingUtilities.invokeLater(() -> {
            if (splash != null) {
                splash.close();
            }
            StringBuilder message = new StringBuilder();
            message.append("KeyBase encountered a fatal error during startup.\n\n");
            message.append(throwable.getClass().getSimpleName()).append(": ").append(throwable.getMessage()).append("\n\n");
            if (logPath != null) {
                message.append("A detailed log was written to:\n").append(logPath);
            } else {
                message.append("Unable to write diagnostic log. Please contact support.");
            }
            JOptionPane.showMessageDialog(null, message.toString(), "Startup Error", JOptionPane.ERROR_MESSAGE);
        });
    }

    private static String logFatalError(Throwable throwable) {
        String localAppData = System.getenv("LOCALAPPDATA");
        Path logDirectory;

        if (localAppData != null && !localAppData.trim().isEmpty()) {
            logDirectory = Paths.get(localAppData, "KeyBase");
        } else {
            logDirectory = Paths.get(System.getProperty("user.home", "."), "KeyBaseLogs");
        }

        try {
            Files.createDirectories(logDirectory);
            Path logFile = logDirectory.resolve("keybase-startup-error.log");
            try (PrintWriter writer = new PrintWriter(new FileWriter(logFile.toFile(), true))) {
                writer.println("==== KeyBase startup failure ====");
                writer.println(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()));
                writer.println();
                throwable.printStackTrace(writer);
                writer.println();
            }
            return logFile.toAbsolutePath().toString();
        } catch (IOException ioException) {
            System.err.println("Unable to write fatal error log: " + ioException.getMessage());
            StringWriter stackWriter = new StringWriter();
            throwable.printStackTrace(new PrintWriter(stackWriter));
            System.err.println(stackWriter.toString());
            return null;
        }
    }
}