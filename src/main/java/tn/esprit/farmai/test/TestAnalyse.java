package tn.esprit.farmai.test;

import tn.esprit.farmai.models.Analyse;
import tn.esprit.farmai.models.Conseil;
import tn.esprit.farmai.models.Priorite;
import tn.esprit.farmai.services.AnalyseService;
import tn.esprit.farmai.services.ConseilService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Test class for Analyse and Conseil services.
 * Validates Session 5 - JDBC CRUD operations with Singleton connection.
 */

public class TestAnalyse {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   TEST ANALYSE & CONSEIL SERVICES");
        System.out.println("   Session 5 - JDBC CRUD Validation");
        System.out.println("========================================\n");

        // Test Analyse Service
        testAnalyseService();

        System.out.println("\n----------------------------------------\n");

        // Test Conseil Service
        testConseilService();

        System.out.println("\n========================================");
        System.out.println("   TESTS COMPLETED SUCCESSFULLY!");
        System.out.println("========================================");
    }

    private static void testAnalyseService() {
        System.out.println(">>> TESTING ANALYSE SERVICE <<<\n");
        AnalyseService analyseService = new AnalyseService();

        try {
            // 1. Test insertOne
            System.out.println("1. Testing insertOne()...");
            Analyse analyse = new Analyse();
            analyse.setDateAnalyse(LocalDateTime.now());
            analyse.setResultatTechnique("Diagnostic technique du sol - pH neutre, azote suffisant");
            analyse.setIdTechnicien(1); // ID du technicien (à adapter selon vos données)
            analyse.setIdFerme(1); // ID de la ferme (à adapter selon vos données)
            analyse.setImageUrl("/images/analyses/analyse_001.jpg");

            analyseService.insertOne(analyse);
            System.out.println("   ✓ Analyse inserted successfully with ID: " + analyse.getIdAnalyse());

            // 2. Test selectAll
            System.out.println("\n2. Testing selectAll()...");
            List<Analyse> analyses = analyseService.selectALL();
            System.out.println("   ✓ Retrieved " + analyses.size() + " analyses from database");
            if (!analyses.isEmpty()) {
                System.out.println("   ✓ First analyse: " + analyses.get(0).getResultatTechnique().substring(0,
                        Math.min(50, analyses.get(0).getResultatTechnique().length())) + "...");
            }

            // 3. Test findById
            if (analyse.getIdAnalyse() > 0) {
                System.out.println("\n3. Testing findById()...");
                Analyse foundAnalyse = analyseService.findById(analyse.getIdAnalyse());
                if (foundAnalyse != null) {
                    System.out.println("   ✓ Found analyse by ID: " + foundAnalyse.getIdAnalyse());
                }
            }

            // 4. Test updateOne
            if (analyse.getIdAnalyse() > 0) {
                System.out.println("\n4. Testing updateOne()...");
                analyse.setResultatTechnique("Diagnostic mis à jour - correction apportée");
                analyseService.updateOne(analyse);
                System.out.println("   ✓ Analyse updated successfully");
            }

            // 5. Test findByTechnicien
            System.out.println("\n5. Testing findByTechnicien()...");
            List<Analyse> analysesByTech = analyseService.findByTechnicien(1);
            System.out.println("   ✓ Found " + analysesByTech.size() + " analyses for technician ID 1");

            // 6. Test deleteOne (décommenter pour tester la suppression)
            // System.out.println("\n6. Testing deleteOne()...");
            // if (analyse.getIdAnalyse() > 0) {
            //     analyseService.deleteOne(analyse.getIdAnalyse());
            //     System.out.println("   ✓ Analyse deleted successfully");
            // }

        } catch (SQLException e) {
            System.err.println("   ✗ ERROR in AnalyseService: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testConseilService() {
        System.out.println(">>> TESTING CONSEIL SERVICE <<<\n");
        ConseilService conseilService = new ConseilService();

        try {
            // 1. Test insertOne
            System.out.println("1. Testing insertOne()...");
            Conseil conseil = new Conseil();
            conseil.setDescriptionConseil("Appliquer un engrais azoté de type NPK 20-10-10 pour stimuler la croissance");
            conseil.setPriorite(Priorite.HAUTE);
            conseil.setIdAnalyse(1); // ID de l'analyse liée (à adapter selon vos données)

            conseilService.insertOne(conseil);
            System.out.println("   ✓ Conseil inserted successfully with ID: " + conseil.getIdConseil());

            // 2. Test selectAll
            System.out.println("\n2. Testing selectAll()...");
            List<Conseil> conseils = conseilService.selectALL();
            System.out.println("   ✓ Retrieved " + conseils.size() + " conseils from database");
            if (!conseils.isEmpty()) {
                System.out.println("   ✓ First conseil priority: " + conseils.get(0).getPriorite());
            }

            // 3. Test findById
            if (conseil.getIdConseil() > 0) {
                System.out.println("\n3. Testing findById()...");
                Optional<Conseil> foundConseilOpt = conseilService.findById(conseil.getIdConseil());
                if (foundConseilOpt.isPresent()) {
                    Conseil foundConseil = foundConseilOpt.get();
                    System.out.println("   ✓ Found conseil by ID: " + foundConseil.getIdConseil());
                }
            }

            // 4. Test findByAnalyse (1:N relationship)
            System.out.println("\n4. Testing findByAnalyse() (1:N relationship)...");
            List<Conseil> conseilsByAnalyse = conseilService.findByAnalyse(1);
            System.out.println("   ✓ Found " + conseilsByAnalyse.size() + " conseils for analyse ID 1");

            // 5. Test findByPriorite
            System.out.println("\n5. Testing findByPriorite()...");
            List<Conseil> highPriorityConseils = conseilService.findByPriorite(Priorite.HAUTE);
            System.out.println("   ✓ Found " + highPriorityConseils.size() + " HIGH priority conseils");

            // 6. Test updateOne
            if (conseil.getIdConseil() > 0) {
                System.out.println("\n6. Testing updateOne()...");
                conseil.setDescriptionConseil("Conseil mis à jour avec nouvelles recommandations");
                conseil.setPriorite(Priorite.MOYENNE);
                conseilService.updateOne(conseil);
                System.out.println("   ✓ Conseil updated successfully");
            }

            // 7. Test deleteOne (décommenter pour tester la suppression)
            // System.out.println("\n7. Testing deleteOne()...");
            // if (conseil.getIdConseil() > 0) {
            //     conseilService.deleteOne(conseil.getIdConseil());
            //     System.out.println("   ✓ Conseil deleted successfully");
            // }

        } catch (SQLException e) {
            System.err.println("   ✗ ERROR in ConseilService: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
