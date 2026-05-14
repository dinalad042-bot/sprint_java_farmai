package tn.esprit.farmai.test;

import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.UserService;
import tn.esprit.farmai.utils.MyDBConnexion;

/**
 * Simple database connection test
 * Run this to verify database is working before starting the main application
 */
public class DatabaseTest {
    
    public static void main(String[] args) {
        System.out.println("🧪 Testing FarmAI Database Connection");
        System.out.println("══════════════════════════════════════");
        
        try {
            // Test database connection
            System.out.println("🔌 Testing database connection...");
            MyDBConnexion dbConn = MyDBConnexion.getInstance();
            
            if (dbConn.getCnx() == null) {
                System.err.println("❌ Database connection failed!");
                System.err.println("Please ensure:");
                System.err.println("1. MySQL is running on localhost:3306");
                System.err.println("2. Database 'farmai' exists");
                System.err.println("3. User 'root' can connect without password");
                System.err.println("4. MySQL JDBC driver is available");
                return;
            }
            
            System.out.println("✅ Database connection successful!");
            
            // Test user service
            System.out.println("👥 Testing user service...");
            UserService userService = new UserService();
            var users = userService.selectALL();
            
            if (users.isEmpty()) {
                System.err.println("⚠️  No users found in database!");
                System.err.println("Please run the database setup first.");
                System.err.println("Run: DatabaseInitializer.main()");
            } else {
                System.out.println("✅ Found " + users.size() + " users in database");
                
                // Show first user
                if (!users.isEmpty()) {
                    User firstUser = users.get(0);
                    System.out.println("📋 Sample user: " + firstUser.getPrenom() + " " + firstUser.getNom() + 
                                     " (" + firstUser.getEmail() + ")");
                }
            }
            
            System.out.println("\n🎉 Database test completed successfully!");
            System.out.println("You can now start the FarmAI application.");
            
        } catch (Exception e) {
            System.err.println("❌ Database test failed: " + e.getMessage());
            e.printStackTrace();
            
            System.err.println("\n🔧 Troubleshooting:");
            System.err.println("1. Ensure MySQL is running");
            System.err.println("2. Check connection in MyDBConnexion.java");
            System.err.println("3. Verify database schema exists");
            System.err.println("4. Check MySQL JDBC driver in pom.xml");
        }
    }
}