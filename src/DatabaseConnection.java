package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConnection {
    private static Connection connection = null;
    private static String url;
    private static String username;
    private static String password;
    private static String driverClassName;
    private static final String INIT_SCRIPT = "config/init_h2_database.sql";

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
            // Use default settings for H2 database
            url = "jdbc:h2:./data/keybase;AUTO_SERVER=TRUE";
            username = "sa";
            password = "";
            driverClassName = "org.h2.Driver";
        } else {
            url = props.getProperty("db.url");
            username = props.getProperty("db.username");
            password = props.getProperty("db.password");
            driverClassName = props.getProperty("db.driverClassName");
        }
        
        // Create data directory for H2 database file (handle both relative paths)
        if (url.contains("jdbc:h2:")) {
            new java.io.File("data").mkdirs();
            new java.io.File("./data").mkdirs();
        }
        
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            System.err.println("Database driver not found: " + e.getMessage());
        }
    }
    
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

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, username, password);
        }
        return connection;
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