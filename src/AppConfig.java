package src;

import java.io.*;
import java.util.*;

public class AppConfig {
    private static final String CONFIG_FILE = "config/app.properties";
    private static Properties properties;

    static {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            properties.load(fis);
        } catch (IOException e) {
            // If file doesn't exist, create with default values
            setDefaultProperties();
            saveProperties();
        }
    }

    private static void setDefaultProperties() {
        // Default webcam device (first available)
        properties.setProperty("webcam.device", "0");
        
        // Default image storage location
        String userHome = System.getProperty("user.home");
        properties.setProperty("images.directory", userHome + File.separator + "KeyBase" + File.separator + "images");
        
        // Create the directory if it doesn't exist
        new File(properties.getProperty("images.directory")).mkdirs();
    }

    public static String getWebcamDevice() {
        return properties.getProperty("webcam.device");
    }

    public static void setWebcamDevice(String deviceIndex) {
        properties.setProperty("webcam.device", deviceIndex);
        saveProperties();
    }

    public static String getImagesDirectory() {
        return properties.getProperty("images.directory");
    }

    public static void setImagesDirectory(String directory) {
        properties.setProperty("images.directory", directory);
        
        // Create the directory if it doesn't exist
        new File(directory).mkdirs();
        
        saveProperties();
    }

    private static void saveProperties() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "KeyBase Application Settings");
        } catch (IOException e) {
            System.err.println("Error saving application properties: " + e.getMessage());
        }
    }
}