package org.reconan.database;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Singleton manager to handle database initialization and schema validation.
 */
public class DatabaseManager {
    private static DatabaseManager instance;

    private DatabaseManager() {
        // Private constructor for Singleton
    }

    /**
     * Retrieves the thread-safe singleton instance of DatabaseManager.
     * @return The DatabaseManager instance
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Initializes the database schema by creating required tables if they do not exist.
     * 
     * @throws SQLException if a database access error occurs
     */
    public void initialize() throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.createStatement();
            
            System.out.println("SQL Server: Initializing database schema...");

            // Read SQL script from resources
            String sqlScript = readSqlResource("/sql/init.sql");
            
            if (sqlScript != null && !sqlScript.isEmpty()) {
                // Execute the full script. SQL Server driver usually handles multiple batches if needed.
                // Note: For complex scripts with GO commands, we would need to split by GO.
                stmt.execute(sqlScript);
                System.out.println("SQL Server: Database schema initialized successfully.");
            } else {
                System.err.println("SQL Server: Failed to read schema initialization script.");
            }
        } finally {
            DatabaseConnection.close(stmt, conn);
        }
    }

    /**
     * Reads a SQL file from the resources directory.
     * 
     * @param path The path to the SQL file relative to resources
     * @return The content of the SQL file as a String
     */
    private String readSqlResource(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("SQL Server: Resource not found: " + path);
                return null;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            System.err.println("SQL Server: Error reading SQL resource " + path + ": " + e.getMessage());
            return null;
        }
    }
}
