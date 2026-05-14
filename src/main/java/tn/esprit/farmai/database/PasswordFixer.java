package tn.esprit.farmai.database;

import tn.esprit.farmai.utils.MyDBConnexion;
import tn.esprit.farmai.utils.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility to fix password hashes in the database.
 * Generates real SHA-256 hashes for test users.
 */
public class PasswordFixer {

    // Test users with their default password
    private static final Map<String, String> TEST_USERS = new HashMap<>();
    
    static {
        TEST_USERS.put("admin@farmai.tn", "password123");
        TEST_USERS.put("expert@farmai.tn", "password123");
        TEST_USERS.put("agricole@farmai.tn", "password123");
        TEST_USERS.put("fournisseur@farmai.tn", "password123");
        TEST_USERS.put("expert1@farmai.tn", "password123");
        TEST_USERS.put("ahmed@farmai.tn", "password123");
        TEST_USERS.put("fatma@farmai.tn", "password123");
        TEST_USERS.put("jeune@farmai.tn", "password123");
        TEST_USERS.put("principal@farmai.tn", "password123");
    }

    /**
     * Fix all test user passwords with proper SHA-256 hashes
     */
    public static void fixPasswords() {
        System.out.println("\n🔧 FIXING PASSWORD HASHES...");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        Connection cnx = null;
        try {
            cnx = MyDBConnexion.getInstance().getCnx();
            if (cnx == null) {
                System.err.println("❌ Cannot connect to database!");
                return;
            }

            // Check current password format
            System.out.println("\n📋 Checking current password format...");
            checkCurrentPasswords(cnx);

            // Update passwords with proper hashes
            System.out.println("\n🔄 Updating passwords...");
            int updated = updatePasswords(cnx);
            
            System.out.println("\n✅ Updated " + updated + " user passwords");

            // Verify the fix
            System.out.println("\n🔍 Verifying password fix...");
            verifyPasswordFix(cnx);

        } catch (Exception e) {
            System.err.println("❌ Error fixing passwords: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    /**
     * Check current password format in database
     */
    private static void checkCurrentPasswords(Connection cnx) throws SQLException {
        String query = "SELECT email, password FROM user LIMIT 5";
        try (PreparedStatement ps = cnx.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String email = rs.getString("email");
                String password = rs.getString("password");
                String format = password.contains("$") ? "salt$hash" : "unknown";
                boolean isPlaceholder = password.contains("qxZX") || password.contains("AAAA");
                
                System.out.println("  " + email + ": " + format + 
                    (isPlaceholder ? " ⚠️ PLACEHOLDER!" : " ✓"));
            }
        }
    }

    /**
     * Update all test user passwords with proper hashes
     */
    private static int updatePasswords(Connection cnx) throws SQLException {
        String query = "UPDATE user SET password = ? WHERE email = ?";
        int totalUpdated = 0;

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            for (Map.Entry<String, String> entry : TEST_USERS.entrySet()) {
                String email = entry.getKey();
                String plainPassword = entry.getValue();
                String hashedPassword = PasswordUtil.hashPassword(plainPassword);

                ps.setString(1, hashedPassword);
                ps.setString(2, email);
                
                int updated = ps.executeUpdate();
                if (updated > 0) {
                    System.out.println("  ✓ Updated: " + email);
                    totalUpdated++;
                } else {
                    System.out.println("  ⚠ Not found: " + email);
                }
            }
        }

        return totalUpdated;
    }

    /**
     * Verify that passwords now work correctly
     */
    private static void verifyPasswordFix(Connection cnx) throws SQLException {
        String query = "SELECT email, password FROM user WHERE email = ?";
        
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            for (Map.Entry<String, String> entry : TEST_USERS.entrySet()) {
                String email = entry.getKey();
                String plainPassword = entry.getValue();

                ps.setString(1, email);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    boolean valid = PasswordUtil.verifyPassword(plainPassword, storedHash);
                    
                    if (valid) {
                        System.out.println("  ✅ " + email + " - Password verified!");
                    } else {
                        System.out.println("  ❌ " + email + " - Password verification FAILED!");
                    }
                }
            }
        }
    }

    /**
     * Generate a password hash for testing
     */
    public static String generateHash(String password) {
        return PasswordUtil.hashPassword(password);
    }

    /**
     * Main entry point for standalone execution
     */
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║       FarmAI Password Fixer Utility        ║");
        System.out.println("╚════════════════════════════════════════════╝");
        
        fixPasswords();
        
        System.out.println("\n🎉 Password fix complete!");
        System.out.println("\n🔑 You can now login with:");
        System.out.println("   Email: agricole@farmai.tn");
        System.out.println("   Password: password123");
    }
}