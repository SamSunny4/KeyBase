package src;

import java.io.*;
import java.util.*;

public class AppConfig {
    private static final String CONFIG_FILE = "config/app.properties";
    private static final String EXPORT_ORIENTATION_KEY = "export.orientation";
    private static final String EXPORT_FIELDS_KEY = "export.fields";
    private static final String CUSTOM_DB_PATH_KEY = "database.customPath";
    private static Properties properties;

    static {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            properties.load(fis);
            ensurePropertyDefaults();
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

        // Camera enabled by default
        properties.setProperty("camera.disabled", "false");

        properties.setProperty(EXPORT_ORIENTATION_KEY, "AUTO");
        properties.setProperty(EXPORT_FIELDS_KEY, String.join(",", ExportField.defaultKeys()));
    properties.setProperty(CUSTOM_DB_PATH_KEY, "");
    }

    private static void ensurePropertyDefaults() {
        boolean changed = false;

        if (!properties.containsKey("webcam.device")) {
            properties.setProperty("webcam.device", "0");
            changed = true;
        }

        if (!properties.containsKey("images.directory")) {
            String userHome = System.getProperty("user.home");
            String defaultDir = userHome + File.separator + "KeyBase" + File.separator + "images";
            properties.setProperty("images.directory", defaultDir);
            new File(defaultDir).mkdirs();
            changed = true;
        } else {
            new File(properties.getProperty("images.directory")).mkdirs();
        }

        if (!properties.containsKey("camera.disabled")) {
            properties.setProperty("camera.disabled", "false");
            changed = true;
        }

        if (!properties.containsKey(EXPORT_ORIENTATION_KEY)) {
            properties.setProperty(EXPORT_ORIENTATION_KEY, "AUTO");
            changed = true;
        }

        if (!properties.containsKey(EXPORT_FIELDS_KEY)) {
            properties.setProperty(EXPORT_FIELDS_KEY, String.join(",", ExportField.defaultKeys()));
            changed = true;
        }

        if (!properties.containsKey(CUSTOM_DB_PATH_KEY)) {
            properties.setProperty(CUSTOM_DB_PATH_KEY, "");
            changed = true;
        }

        if (changed) {
            saveProperties();
        }
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

    public static boolean isCameraDisabled() {
        return Boolean.parseBoolean(properties.getProperty("camera.disabled", "false"));
    }

    public static void setCameraDisabled(boolean disabled) {
        properties.setProperty("camera.disabled", Boolean.toString(disabled));
        saveProperties();
    }

    public static String getExportOrientation() {
        return properties.getProperty(EXPORT_ORIENTATION_KEY, "AUTO");
    }

    public static void setExportOrientation(String orientation) {
        String sanitized = orientation == null ? "AUTO" : orientation.trim().toUpperCase(Locale.ROOT);
        if (!"AUTO".equals(sanitized) && !"PORTRAIT".equals(sanitized) && !"LANDSCAPE".equals(sanitized)) {
            sanitized = "AUTO";
        }
        properties.setProperty(EXPORT_ORIENTATION_KEY, sanitized);
        saveProperties();
    }

    public static List<String> getExportFields() {
        String value = properties.getProperty(EXPORT_FIELDS_KEY, String.join(",", ExportField.defaultKeys()));
        List<String> result = new ArrayList<>();
        if (value != null) {
            String[] parts = value.split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    String normalized = trimmed.toUpperCase(Locale.ROOT);
                    if (ExportField.fromKey(normalized) != null && !result.contains(normalized)) {
                        result.add(normalized);
                    }
                }
            }
        }
        if (result.isEmpty()) {
            result.addAll(ExportField.defaultKeys());
        }
        return result;
    }

    public static void setExportFields(List<String> fields) {
        if (fields == null || fields.isEmpty()) {
            return;
        }
        List<String> sanitized = new ArrayList<>(fields.size());
        for (String field : fields) {
            if (field != null) {
                String trimmed = field.trim();
                if (!trimmed.isEmpty()) {
                    String normalized = trimmed.toUpperCase(Locale.ROOT);
                    if (ExportField.fromKey(normalized) != null && !sanitized.contains(normalized)) {
                        sanitized.add(normalized);
                    }
                }
            }
        }
        if (sanitized.isEmpty()) {
            sanitized.addAll(ExportField.defaultKeys());
        }
        properties.setProperty(EXPORT_FIELDS_KEY, String.join(",", sanitized));
        saveProperties();
    }

    public static String getCustomDatabasePath() {
        return properties.getProperty(CUSTOM_DB_PATH_KEY, "").trim();
    }

    public static void setCustomDatabasePath(String path) {
        String normalized = normalizeDatabasePath(path);
        properties.setProperty(CUSTOM_DB_PATH_KEY, normalized);
        saveProperties();
    }

    public static String normalizeDatabasePath(String input) {
        if (input == null) {
            return "";
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        if (trimmed.startsWith("jdbc:h2:")) {
            trimmed = trimmed.substring("jdbc:h2:".length());
        }
        if (trimmed.startsWith("file:")) {
            trimmed = trimmed.substring("file:".length());
        }
        int semicolonIndex = trimmed.indexOf(';');
        if (semicolonIndex >= 0) {
            trimmed = trimmed.substring(0, semicolonIndex);
        }
        File base = new File(trimmed);
        String absolute = base.getAbsolutePath();
        String lower = absolute.toLowerCase(Locale.ROOT);
        final String MV_SUFFIX = ".mv.db";
        final String TRACE_SUFFIX = ".trace.db";
        if (lower.endsWith(MV_SUFFIX)) {
            absolute = absolute.substring(0, absolute.length() - MV_SUFFIX.length());
        } else if (lower.endsWith(TRACE_SUFFIX)) {
            absolute = absolute.substring(0, absolute.length() - TRACE_SUFFIX.length());
        }
        return absolute;
    }

    private static void saveProperties() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "KeyBase Application Settings");
        } catch (IOException e) {
            System.err.println("Error saving application properties: " + e.getMessage());
        }
    }
}