package tn.esprit.farmai.test;

import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.UserService;
import tn.esprit.farmai.services.FermeService;
import tn.esprit.farmai.utils.MyDBConnexion;
import tn.esprit.farmai.utils.SessionManager;

/**
 * Test class to debug agricole integration issue
 * Checks if agricole users can see their farms correctly
 */
public class AgricoleIntegrationTest {
    
    public static void main(String[] args) {
        System.out.println("Testing Agricole Integration Issue");
        System.out.println("==================================");
        
        try {
            // Test database connection
            MyDBConnexion dbConn = MyDBConnexion.getInstance();
            if (dbConn.getCnx() == null) {
                System.err.println("Database connection failed!");
                return;
            }
            
            // Test user service
            UserService userService = new UserService();
            FermeService fermeService = new FermeService();
            
            System.out.println("Looking for agricole user...");
            
            // Find agricole user
            User agricoleUser = null;
            for (User user : userService.selectALL()) {
                if ("agricole@farmai.tn".equals(user.getEmail())) {
                    agricoleUser = user;
                    break;
                }
            }
            
            if (agricoleUser == null) {
                System.err.println("Agricole user not found in database!");
                return;
            }
            
            System.out.println("Found agricole user: " + agricoleUser.getPrenom() + " " + agricoleUser.getNom());
            System.out.println("User ID: " + agricoleUser.getIdUser());
            System.out.println("Email: " + agricoleUser.getEmail());
            System.out.println("Role: " + agricoleUser.getRole());
            
            // Set current user in session
            SessionManager.getInstance().setCurrentUser(agricoleUser);
            System.out.println("Set agricole user in session");
            
            // Test farm lookup
            System.out.println("");
            System.out.println("Looking for farms assigned to agricole user...");
            int userId = agricoleUser.getIdUser();
            
            int totalFarms = 0;
            int matchingFarms = 0;
            
            for (var ferme : fermeService.selectALL()) {
                totalFarms++;
                if (ferme.getIdFermier() == userId) {
                    matchingFarms++;
                    System.out.println("Farm: " + ferme.getNomFerme() + " (ID: " + ferme.getIdFerme() + ")");
                }
            }
            
            System.out.println("");
            System.out.println("Results:");
            System.out.println("Total farms in database: " + totalFarms);
            System.out.println("Farms assigned to agricole user: " + matchingFarms);
            
            if (matchingFarms == 0) {
                System.err.println("PROBLEM: No farms found for agricole user!");
                System.out.println("");
                System.out.println("Checking all farm assignments:");
                for (var ferme : fermeService.selectALL()) {
                    System.out.println("Farm: " + ferme.getNomFerme() + " - Owner ID: " + ferme.getIdFermier());
                }
            } else {
                System.out.println("SUCCESS: Found " + matchingFarms + " farms for agricole user!");
            }
            
            // Test the actual filtering logic from the controller
            System.out.println("");
            System.out.println("Testing controller filtering logic...");
            try {
                int filteredCount = fermeService.selectALL().stream()
                        .filter(f -> f.getIdFermier() == userId)
                        .toList()
                        .size();
                System.out.println("Stream filter result: " + filteredCount);
            } catch (Exception e) {
                System.err.println("Stream filter failed: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("");
        System.out.println("Test completed!");
    }
}