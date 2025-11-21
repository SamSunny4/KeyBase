package src;

import java.io.*;
import java.util.*;

public class AppConfig {
    private static final String CONFIG_FILE = "config/app.properties";
    // Per-user writable config under %LOCALAPPDATA%\KeyBase\app.properties
    private static final String USER_CONFIG_FILE;
    private static final String EXPORT_ORIENTATION_KEY = "export.orientation";
    private static final String EXPORT_FIELDS_KEY = "export.fields";
    private static final String CUSTOM_DB_PATH_KEY = "database.customPath";
    private static final String REQUIRED_FIELDS_KEY = "form.requiredFields";
    
    // Key Catalog Keys
    private static final String KEY_CATEGORIES = "key.categories";
    private static final String KEY_CATEGORY_PREFIX = "key.category.";
    private static final String KEY_DEFAULT_PARENT = "key.default.parent";
    private static final String KEY_DEFAULT_CHILD_PREFIX = "key.default.child.";

    private static Properties properties;

    static {
        properties = new Properties();
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData == null || localAppData.trim().isEmpty()) {
            USER_CONFIG_FILE = CONFIG_FILE; // fallback to packaged config
        } else {
            USER_CONFIG_FILE = localAppData + File.separator + "KeyBase" + File.separator + "app.properties";
        }

        // Load packaged defaults first (if present)
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            properties.load(fis);
        } catch (IOException ignored) {
            // no packaged defaults; we'll populate defaults below
        }

        // Then load/override with user-specific config if present
        File userCfg = new File(USER_CONFIG_FILE);
        if (userCfg.exists()) {
            try (FileInputStream uis = new FileInputStream(userCfg)) {
                properties.load(uis);
            } catch (IOException ignored) {
                // ignore and proceed with whatever we have
            }
        }

        // Ensure all required properties exist and persist per-user config if needed
        ensurePropertyDefaults();
        // If user config didn't exist, ensure it's created so subsequent saves succeed
        if (!userCfg.exists()) {
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
        properties.setProperty(REQUIRED_FIELDS_KEY, "NAME,PHONE,ID_NO");

        // Default Key Catalog
        if (!properties.containsKey(KEY_CATEGORIES)) {
            properties.setProperty(KEY_CATEGORIES, "Vehicles,Padlock,Locker,Other");
            properties.setProperty(KEY_CATEGORY_PREFIX + "Vehicles", "Bike,Scooter,Auto,Car,Bus,Truck,Traveller,JCB,Hitachi,Machines,Door key,Other");
            properties.setProperty(KEY_CATEGORY_PREFIX + "Padlock", "7 Lever,6 Lever,Other");
            properties.setProperty(KEY_CATEGORY_PREFIX + "Locker", "Office Locker,Home Locker,Digital Locker,Other");
            properties.setProperty(KEY_CATEGORY_PREFIX + "Other", "Other");
            
            properties.setProperty(KEY_DEFAULT_PARENT, "Vehicles");
            properties.setProperty(KEY_DEFAULT_CHILD_PREFIX + "Vehicles", "Bike");
        }
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

        if (!properties.containsKey(REQUIRED_FIELDS_KEY)) {
            properties.setProperty(REQUIRED_FIELDS_KEY, "NAME,PHONE,ID_NO");
            changed = true;
        }

        // Ensure Key Catalog defaults
        if (!properties.containsKey(KEY_CATEGORIES)) {
            properties.setProperty(KEY_CATEGORIES, "Vehicles,Padlock,Locker,Other");
            properties.setProperty(KEY_CATEGORY_PREFIX + "Vehicles", "Bike,Scooter,Auto,Car,Bus,Truck,Traveller,JCB,Hitachi,Machines,Door key,Other");
            properties.setProperty(KEY_CATEGORY_PREFIX + "Padlock", "7 Lever,6 Lever,Other");
            properties.setProperty(KEY_CATEGORY_PREFIX + "Locker", "Office Locker,Home Locker,Digital Locker,Other");
            properties.setProperty(KEY_CATEGORY_PREFIX + "Other", "Other");
            
            properties.setProperty(KEY_DEFAULT_PARENT, "Vehicles");
            properties.setProperty(KEY_DEFAULT_CHILD_PREFIX + "Vehicles", "Bike");
            changed = true;
        }

        if (changed) {
            saveProperties();
        }
    }

    // Key Catalog Methods

    public static List<String> getParentCategories() {
        String val = properties.getProperty(KEY_CATEGORIES, "");
        return splitList(val);
    }

    public static void setParentCategories(List<String> categories) {
        properties.setProperty(KEY_CATEGORIES, String.join(",", categories));
        saveProperties();
    }

    public static List<String> getChildCategories(String parent) {
        String val = properties.getProperty(KEY_CATEGORY_PREFIX + parent, "");
        return splitList(val);
    }

    public static void setChildCategories(String parent, List<String> children) {
        properties.setProperty(KEY_CATEGORY_PREFIX + parent, String.join(",", children));
        saveProperties();
    }

    public static void renameParentCategory(String oldName, String newName) {
        // Get children of old parent
        String children = properties.getProperty(KEY_CATEGORY_PREFIX + oldName);
        if (children != null) {
            properties.setProperty(KEY_CATEGORY_PREFIX + newName, children);
            properties.remove(KEY_CATEGORY_PREFIX + oldName);
        }
        
        // Update default parent if needed
        String defaultParent = getDefaultParentCategory();
        if (oldName.equals(defaultParent)) {
            setDefaultParentCategory(newName);
        }
        
        // Update default child key
        String defaultChild = properties.getProperty(KEY_DEFAULT_CHILD_PREFIX + oldName);
        if (defaultChild != null) {
            properties.setProperty(KEY_DEFAULT_CHILD_PREFIX + newName, defaultChild);
            properties.remove(KEY_DEFAULT_CHILD_PREFIX + oldName);
        }

        saveProperties();
    }

    public static void deleteParentCategory(String name) {
        properties.remove(KEY_CATEGORY_PREFIX + name);
        properties.remove(KEY_DEFAULT_CHILD_PREFIX + name);
        if (name.equals(getDefaultParentCategory())) {
            properties.remove(KEY_DEFAULT_PARENT);
        }
        saveProperties();
    }

    public static String getDefaultParentCategory() {
        return properties.getProperty(KEY_DEFAULT_PARENT, "");
    }

    public static void setDefaultParentCategory(String parent) {
        properties.setProperty(KEY_DEFAULT_PARENT, parent);
        saveProperties();
    }

    public static String getDefaultChildCategory(String parent) {
        return properties.getProperty(KEY_DEFAULT_CHILD_PREFIX + parent, "");
    }

    public static void setDefaultChildCategory(String parent, String child) {
        properties.setProperty(KEY_DEFAULT_CHILD_PREFIX + parent, child);
        saveProperties();
    }

    public static String findParentForChild(String child) {
        if (child == null || child.trim().isEmpty()) return null;
        for (String parent : getParentCategories()) {
            List<String> children = getChildCategories(parent);
            for (String c : children) {
                if (c.equalsIgnoreCase(child)) {
                    return parent;
                }
            }
        }
        return null;
    }

    private static List<String> splitList(String val) {
        List<String> list = new ArrayList<>();
        if (val != null && !val.trim().isEmpty()) {
            for (String s : val.split(",")) {
                if (!s.trim().isEmpty()) {
                    list.add(s.trim());
                }
            }
        }
        return list;
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

    public static Set<String> getRequiredFields() {
        String value = properties.getProperty(REQUIRED_FIELDS_KEY, "NAME,PHONE,ID_NO");
        Set<String> fields = new HashSet<>();
        if (value != null && !value.trim().isEmpty()) {
            for (String field : value.split(",")) {
                String normalized = field.trim().toUpperCase(Locale.ROOT);
                if (!normalized.isEmpty()) {
                    fields.add(normalized);
                }
            }
        }
        return fields;
    }

    public static void setRequiredFields(Set<String> fields) {
        if (fields == null || fields.isEmpty()) {
            properties.setProperty(REQUIRED_FIELDS_KEY, "");
        } else {
            Set<String> normalized = new HashSet<>();
            for (String field : fields) {
                if (field != null && !field.trim().isEmpty()) {
                    normalized.add(field.trim().toUpperCase(Locale.ROOT));
                }
            }
            properties.setProperty(REQUIRED_FIELDS_KEY, String.join(",", normalized));
        }
        saveProperties();
    }

    public static boolean isFieldRequired(String fieldName) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            return false;
        }
        return getRequiredFields().contains(fieldName.trim().toUpperCase(Locale.ROOT));
    }

    private static void saveProperties() {
        File out = new File(USER_CONFIG_FILE != null ? USER_CONFIG_FILE : CONFIG_FILE);
        try {
            File parent = out.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(out)) {
                properties.store(fos, "KeyBase Application Settings");
            }
        } catch (IOException e) {
            System.err.println("Error saving application properties: " + e.getMessage());
        }
    }
}