package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Properties;

public class DatabaseConnection {
    private static Connection connection = null;
    private static String url;
    private static String username;
    private static String password;
    private static String driverClassName;
    private static final String INIT_SCRIPT = "config/init_h2_database.sql";
    private static String lastAppliedCustomPath = "";
    private static boolean schemaChecked = false;

    static {
        try {
            loadDatabaseProperties();
            // Don't automatically initialize database on every connection
            // Database should be initialized only once using DatabaseInitializer
        } catch (IOException e) {
            System.err.println("Error loading database properties: " + e.getMessage());
        }
    }

    private static void loadDatabaseProperties() throws IOException {
        Properties props = new Properties();
        
        // Try multiple potential locations for the properties file
        String[] possiblePaths = {
            "config/database.properties",
            "../config/database.properties"
        };
        
        boolean loaded = false;
        for (String path : possiblePaths) {
            File propsFile = new File(path);
            if (propsFile.exists()) {
                try (FileInputStream fis = new FileInputStream(propsFile)) {
                    props.load(fis);
                    System.out.println("Loaded database properties from: " + propsFile.getAbsolutePath());
                    loaded = true;
                    break;
                } catch (IOException e) {
                    // Try next path
                }
            }
        }
        
        if (!loaded) {
            System.err.println("Could not find database.properties file. Using default H2 settings.");
            url = null;
            username = "sa";
            password = "";
            driverClassName = "org.h2.Driver";
        } else {
            url = props.getProperty("db.url");
            username = props.getProperty("db.username");
            password = props.getProperty("db.password");
            driverClassName = props.getProperty("db.driverClassName");
        }

        url = resolveJdbcUrl(url);
        ensureDataDirectory(url);

        String customDbPath = AppConfig.getCustomDatabasePath();
        if (customDbPath != null && !customDbPath.isEmpty()) {
            String normalized = AppConfig.normalizeDatabasePath(customDbPath).replace('\\', '/');
            String candidateUrl = normalized.startsWith("jdbc:h2:") ? normalized : "jdbc:h2:" + normalized;
            url = candidateUrl;
            if (username == null || username.trim().isEmpty()) {
                username = "sa";
            }
            if (password == null) {
                password = "";
            }
            if (driverClassName == null || driverClassName.trim().isEmpty()) {
                driverClassName = "org.h2.Driver";
            }
            System.out.println("Using custom database path: " + normalized);
            ensureDataDirectory(url);
        }
        lastAppliedCustomPath = AppConfig.getCustomDatabasePath();
        schemaChecked = false;

        System.out.println("Effective database URL: " + url);
        
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            System.err.println("Database driver not found: " + e.getMessage());
        }
    }

    private static String resolveJdbcUrl(String rawUrl) {
        String candidate = rawUrl == null ? "" : rawUrl.trim();
        candidate = expandDatabaseTokens(candidate);

        if (candidate.isEmpty() || candidate.equalsIgnoreCase("./data/keybase") || candidate.equalsIgnoreCase(".\\data\\keybase")) {
            candidate = defaultDatabaseLocation();
        }

        if (!candidate.startsWith("jdbc:h2:")) {
            candidate = "jdbc:h2:" + candidate;
        }

        return candidate;
    }

    private static String expandDatabaseTokens(String value) {
        if (value == null) {
            return "";
        }

        String localApp = normalizePath(System.getenv("LOCALAPPDATA"));
        String userHome = normalizePath(System.getProperty("user.home", "."));

        String expanded = value;
        if (!localApp.isEmpty()) {
            expanded = expanded.replace("${LOCALAPPDATA}", localApp);
        }
        expanded = expanded.replace("${USER_HOME}", userHome);
        return expanded;
    }

    private static String normalizePath(String path) {
        if (path == null) {
            return "";
        }
        return path.replace('\\', '/');
    }

    private static String defaultDatabaseLocation() {
        Path dbPath;
        String localApp = System.getenv("LOCALAPPDATA");
        if (localApp != null && !localApp.trim().isEmpty()) {
            dbPath = Paths.get(localApp, "KeyBase", "data", "keybase");
        } else {
            String userHome = System.getProperty("user.home", ".");
            dbPath = Paths.get(userHome, "KeyBase", "data", "keybase");
        }

        try {
            Path parent = dbPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            System.err.println("Unable to prepare default database directory: " + e.getMessage());
        }

        return dbPath.toString().replace('\\', '/');
    }

    private static void ensureDataDirectory(String jdbcUrl) {
        if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:h2:")) {
            return;
        }

        String remainder = jdbcUrl.substring("jdbc:h2:".length());
        int paramIndex = remainder.indexOf(';');
        if (paramIndex >= 0) {
            remainder = remainder.substring(0, paramIndex);
        }

        if (remainder.startsWith("mem:")) {
            return; // nothing to create for in-memory database
        }

        if (remainder.startsWith("file:")) {
            remainder = remainder.substring("file:".length());
        }

        if (remainder.trim().isEmpty()) {
            remainder = defaultDatabaseLocation();
        }

        try {
            Path dbPath = Paths.get(remainder);
            Path directory = dbPath.toAbsolutePath().getParent();
            if (directory != null) {
                Files.createDirectories(directory);
            }
        } catch (Exception e) {
            System.err.println("Unable to ensure database directory for URL '" + jdbcUrl + "': " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unused")
    private static void initDatabase() throws SQLException {
        // Execute the initialization script
        try (Connection conn = getConnection();
             BufferedReader reader = new BufferedReader(new FileReader(INIT_SCRIPT));
             Statement stmt = conn.createStatement()) {
            
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip comments and empty lines
                if (line.trim().startsWith("--") || line.trim().isEmpty()) {
                    continue;
                }
                sb.append(line);
                
                // Execute the statement when a semicolon is found
                if (line.trim().endsWith(";")) {
                    stmt.execute(sb.toString());
                    sb.setLength(0);
                }
            }
            
            System.out.println("Database initialized successfully.");
        } catch (IOException e) {
            System.err.println("Error reading initialization script: " + e.getMessage());
        }
    }

    public static synchronized Connection getConnection() throws SQLException {
        String currentCustomPath = AppConfig.getCustomDatabasePath();
        if (!Objects.equals(currentCustomPath, lastAppliedCustomPath)) {
            closeConnection();
            try {
                loadDatabaseProperties();
            } catch (IOException e) {
                System.err.println("Error reloading database properties: " + e.getMessage());
            }
        }

        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, username, password);
            ensureSchemaUpgrades(connection);
        }
        return connection;
    }

    private static synchronized void ensureSchemaUpgrades(Connection conn) {
        if (schemaChecked || conn == null) {
            return;
        }

        try {
            java.sql.DatabaseMetaData meta = conn.getMetaData();
            try (java.sql.ResultSet tables = meta.getTables(null, null, "DUPLICATOR", null)) {
                if (tables == null || !tables.next()) {
                    // Table not present yet: attempt to initialize schema using bundled SQL
                    System.out.println("DUPLICATOR table not found; attempting DB initialization...");
                    try {
                        boolean ok = DatabaseInitializer.initializeDatabase();
                        if (ok) {
                            // attempt to re-check existence
                            try (java.sql.ResultSet after = meta.getTables(null, null, "DUPLICATOR", null)) {
                                if (after != null && after.next()) {
                                    System.out.println("DUPLICATOR table created by initializer.");
                                } else {
                                    System.out.println("DUPLICATOR table still missing after initializer.");
                                }
                            } catch (SQLException ex) {
                                // ignore
                            }
                        }
                    } catch (Throwable t) {
                        System.out.println("Database initialization attempt failed: " + t.getMessage());
                    }
                    return;
                }
            }
        } catch (SQLException e) {
            return; // Unable to check metadata, try again on next call
        }

        final String[] alterations = {
            "ALTER TABLE duplicator ADD COLUMN IF NOT EXISTS key_type VARCHAR(50)",
            "ALTER TABLE duplicator ADD COLUMN IF NOT EXISTS id_no VARCHAR(50)",
            "ALTER TABLE duplicator ADD COLUMN IF NOT EXISTS remarks VARCHAR(500)",
            "ALTER TABLE duplicator ADD COLUMN IF NOT EXISTS quantity INT DEFAULT 1",
            "ALTER TABLE duplicator ADD COLUMN IF NOT EXISTS amount DECIMAL(10,2) DEFAULT 0.00",
            "ALTER TABLE duplicator ADD COLUMN IF NOT EXISTS image_path VARCHAR(255)",
            "ALTER TABLE duplicator ADD COLUMN IF NOT EXISTS purpose VARCHAR(50)",
            "ALTER TABLE duplicator ADD COLUMN IF NOT EXISTS date_added DATE"
        };

        for (String sql : alterations) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            } catch (SQLException ignore) {
                // Column already exists or other benign issue; move on to the next
            }
        }

        schemaChecked = true;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
}