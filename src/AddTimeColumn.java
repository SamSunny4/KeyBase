package src;

import java.sql.Connection;
import java.sql.Statement;

public class AddTimeColumn {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Connected to database...");
            
            // Add time_added column
            try {
                stmt.execute("ALTER TABLE duplicator ADD COLUMN IF NOT EXISTS time_added TIME");
                System.out.println("✓ Successfully added time_added column to duplicator table");
            } catch (Exception e) {
                System.out.println("✗ Could not add time_added column: " + e.getMessage());
            }
            
            // Verify column exists
            var rs = stmt.executeQuery(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'DUPLICATOR' AND COLUMN_NAME = 'TIME_ADDED'"
            );
            
            if (rs.next()) {
                System.out.println("✓ time_added column verified in database");
            } else {
                System.out.println("✗ time_added column NOT found in database");
            }
            
            System.out.println("\nDone!");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
