package tn.esprit.farmai.database;

import tn.esprit.farmai.utils.MyDBConnexion;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseSetup {
    
    public static void setupDatabase() {
        try {
            System.out.println("Setting up FarmAI database...");
            
            // Get database connection
            MyDBConnexion dbConn = MyDBConnexion.getInstance();
            Connection conn = dbConn.getCnx();
            
            if (conn == null) {
                System.err.println("❌ Failed to establish database connection!");
                System.err.println("Please ensure MySQL is running and accessible.");
                return;
            }
            
            Statement stmt = conn.createStatement();
            
            // Read and execute the SQL script
            String sqlFile = "../../database/farmai_complete.sql";
            InputStream is = DatabaseSetup.class.getResourceAsStream(sqlFile);
            if (is == null) {
                // Try alternative path
                sqlFile = "/database/farmai_complete.sql";
                is = DatabaseSetup.class.getResourceAsStream(sqlFile);
            }
            if (is == null) {
                System.err.println("⚠️  Could not find SQL file: " + sqlFile);
                System.err.println("Database setup skipped - using existing database");
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            
            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sql.append(line).append("\n");
            }
            reader.close();
            
            // Split and execute statements
            String[] statements = sql.toString().split(";");
            int executedCount = 0;
            
            for (String statement : statements) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("--") && !trimmed.startsWith("/*!")) {
                    try {
                        stmt.execute(trimmed);
                        executedCount++;
                    } catch (Exception e) {
                        System.err.println("⚠️  Warning executing statement: " + e.getMessage());
                    }
                }
            }
            
            System.out.println("✅ Database setup completed successfully!");
            System.out.println("📊 Executed " + executedCount + " SQL statements");
            
            // Test the setup
            testDatabaseConnection();
            
        } catch (Exception e) {
            System.err.println("❌ Database setup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void testDatabaseConnection() {
        try {
            MyDBConnexion dbConn = MyDBConnexion.getInstance();
            Connection conn = dbConn.getCnx();
            
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Database connection test: PASSED");
                
                // Quick test query
                Statement stmt = conn.createStatement();
                java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as user_count FROM user");
                if (rs.next()) {
                    System.out.println("👥 Found " + rs.getInt("user_count") + " users in database");
                }
                rs.close();
                stmt.close();
            } else {
                System.err.println("❌ Database connection test: FAILED");
            }
        } catch (Exception e) {
            System.err.println("❌ Database connection test failed: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        setupDatabase();
    }
}