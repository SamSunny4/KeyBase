package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Database initializer for KeyBase application
 * This class reads the SQL script and initializes the H2 database
 */
public class DatabaseInitializer {

    public static void main(String[] args) {
        System.out.println("Starting database initialization...");

        try {
            // Ensure database driver is available
            Class.forName("org.h2.Driver");

            // Initialize the database (best-effort)
            boolean ok = initializeDatabase();
            if (ok) {
                System.out.println("Database initialization completed successfully.");
            } else {
                System.err.println("Database initialization did not run (no init script found or errors).\n");
            }

        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Initialize the database using the SQL script
     */
    /**
     * Initialize the database using the SQL script. Returns true if initialization ran.
     */
    public static boolean initializeDatabase() {
        // Try multiple possible locations for the SQL file
        String[] possiblePaths = {
            "config/init_h2_database.sql",
            "../config/init_h2_database.sql",
            "../../config/init_h2_database.sql"
        };
        
        File sqlFile = null;
        for (String path : possiblePaths) {
            File testFile = new File(path);
            if (testFile.exists()) {
                sqlFile = testFile;
                System.out.println("Found SQL file at: " + testFile.getAbsolutePath());
                break;
            }
        }
        
        if (sqlFile == null) {
            System.err.println("SQL initialization file not found. Skipping DB initialization. Tried paths:");
            for (String path : possiblePaths) {
                System.err.println("  - " + new File(path).getAbsolutePath());
            }
            return false;
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             BufferedReader reader = new BufferedReader(new FileReader(sqlFile))) {
            
            StringBuilder sb = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                // Skip comments and empty lines
                if (line.trim().isEmpty() || line.trim().startsWith("--")) {
                    continue;
                }
                
                sb.append(line);
                
                // If line ends with semicolon, execute the statement
                if (line.trim().endsWith(";")) {
                    String sql = sb.toString();
                    try {
                        stmt.execute(sql);
                    } catch (Exception e) {
                        System.err.println("Error executing SQL statement: " + sql);
                        System.err.println("Error message: " + e.getMessage());
                    }
                    sb.setLength(0); // Clear the string builder
                }
            }
            
            System.out.println("Database tables created successfully.");

            // Try to relax vehicle_no constraint on existing DBs (make nullable)
            try {
                stmt.execute("ALTER TABLE duplicator ALTER COLUMN vehicle_no DROP NOT NULL");
                System.out.println("Altered column vehicle_no to allow NULLs (if it existed).");
            } catch (Exception e) {
                // ignore if the DB doesn't support this syntax or column not present
                // try an alternative ALTER that some H2 versions accept
                try {
                    stmt.execute("ALTER TABLE duplicator ALTER COLUMN vehicle_no VARCHAR(20)");
                    System.out.println("Altered column vehicle_no type to VARCHAR(20) (no-op for nullability).");
                } catch (Exception ex) {
                    System.out.println("Could not alter vehicle_no nullability: " + ex.getMessage());
                }
            }
            
            // Try to add purpose column if it doesn't exist (for existing databases)
            try {
                stmt.execute("ALTER TABLE duplicator ADD COLUMN IF NOT EXISTS purpose VARCHAR(50)");
                System.out.println("Added purpose column to duplicator table (if it didn't exist).");
            } catch (Exception e) {
                System.out.println("Could not add purpose column: " + e.getMessage());
            }
            
            // Try to add time_added column if it doesn't exist (for existing databases)
            try {
                stmt.execute("ALTER TABLE duplicator ADD COLUMN IF NOT EXISTS time_added TIME");
                System.out.println("Added time_added column to duplicator table (if it didn't exist).");
            } catch (Exception e) {
                System.out.println("Could not add time_added column: " + e.getMessage());
            }
            
            // After creating tables, optionally look for sample data script and execute it
            // Try multiple possible locations for the sample data file
            String[] samplePaths = {
                "config/sample_data.sql",
                "../config/sample_data.sql",
                "../../config/sample_data.sql"
            };
            File sampleFile = null;
            for (String path : samplePaths) {
                File testFile = new File(path);
                if (testFile.exists()) {
                    sampleFile = testFile;
                    System.out.println("Found sample data file at: " + testFile.getAbsolutePath());
                    break;
                }
            }
            if (sampleFile != null) {
                try (BufferedReader sreader = new BufferedReader(new FileReader(sampleFile))) {
                    StringBuilder ssb = new StringBuilder();
                    String sline;
                    while ((sline = sreader.readLine()) != null) {
                        if (sline.trim().isEmpty() || sline.trim().startsWith("--")) {
                            continue;
                        }
                        ssb.append(sline);
                        if (sline.trim().endsWith(";")) {
                            String sql = ssb.toString();
                            try {
                                stmt.execute(sql);
                            } catch (Exception e) {
                                System.err.println("Error executing sample SQL statement: " + sql);
                                System.err.println("Error message: " + e.getMessage());
                            }
                            ssb.setLength(0);
                        }
                    }
                    System.out.println("Sample data (if any) executed.");
                } catch (Exception e) {
                    System.err.println("Error executing sample data file: " + e.getMessage());
                }
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error initializing database from SQL file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}