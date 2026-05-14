package tn.esprit.farmai;

import tn.esprit.farmai.database.DatabaseSetup;
import tn.esprit.farmai.database.PasswordFixer;
import tn.esprit.farmai.database.TestDataInserter;

/**
 * Database initialization utility for FarmAI
 * 
 * Usage: Run this before starting the main application to ensure
 * the database is set up with schema and test data.
 * 
 * IMPORTANT: This now includes password fixing to ensure test users
 * can login with proper SHA-256 hashed passwords.
 */
public class DatabaseInitializer {
    
    public static void main(String[] args) {
        System.out.println("🚀 FarmAI Database Initializer");
        System.out.println("══════════════════════════════════════");
        
        try {
            // Step 1: Setup database schema
            System.out.println("\n📋 Step 1: Setting up database schema...");
            DatabaseSetup.setupDatabase();
            
            // Step 2: Insert test data
            System.out.println("\n🧪 Step 2: Inserting test data...");
            TestDataInserter.insertTestData();
            
            // Step 3: Fix passwords (CRITICAL for login to work!)
            System.out.println("\n🔐 Step 3: Fixing password hashes...");
            PasswordFixer.fixPasswords();
            
            System.out.println("\n✅ Database initialization completed successfully!");
            System.out.println("\n🎮 You can now start the FarmAI application.");
            System.out.println("   The database is ready for manual testing.");
            
            // Print login credentials
            System.out.println("\n🔑 TEST LOGIN CREDENTIALS:");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("  Admin:      admin@farmai.tn / password123");
            System.out.println("  Expert:     expert@farmai.tn / password123");
            System.out.println("  Agricole:   agricole@farmai.tn / password123");
            System.out.println("  Fournisseur: fournisseur@farmai.tn / password123");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
        } catch (Exception e) {
            System.err.println("\n❌ Database initialization failed!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            System.err.println("\n🔧 Troubleshooting tips:");
            System.err.println("1. Ensure MySQL is running on localhost:3306");
            System.err.println("2. Verify MySQL credentials (root with empty password)");
            System.err.println("3. Check if the 'farmai' database exists");
            System.err.println("4. Verify MySQL JDBC driver is in classpath");
        }
    }
    
    /**
     * Quick check if database is already initialized
     */
    public static boolean isDatabaseInitialized() {
        try {
            TestDataInserter.insertTestData();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}