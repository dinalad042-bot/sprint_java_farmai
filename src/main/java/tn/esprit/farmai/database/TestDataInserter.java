package tn.esprit.farmai.database;

import tn.esprit.farmai.models.*;
import tn.esprit.farmai.services.*;
import tn.esprit.farmai.utils.MyDBConnexion;

import java.sql.Date;
import java.time.LocalDate;

public class TestDataInserter {
    
    public static void insertTestData() {
        System.out.println("🔄 Inserting test data...");
        
        try {
            // Test database connection
            MyDBConnexion dbConn = MyDBConnexion.getInstance();
            if (dbConn.getCnx() == null) {
                System.err.println("❌ Database connection failed!");
                return;
            }
            
            // Insert additional test farms
            insertTestFarms();
            
            // Insert test animals
            insertTestAnimals();
            
            // Insert test plants
            insertTestPlantes();
            
            // Insert test analyses
            insertTestAnalyses();
            
            // Insert test conseils
            insertTestConseils();
            
            System.out.println("✅ Test data insertion completed!");
            
            // Print summary
            printDataSummary();
            
        } catch (Exception e) {
            System.err.println("❌ Test data insertion failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void insertTestFarms() {
        try {
            FermeService fermeService = new FermeService();
            
            // Additional test farms if not enough exist
            if (fermeService.selectALL().size() < 5) {
                Ferme ferme4 = new Ferme(0, "Ferme Test Nord", "Nord, Tunisie", 120.0, 4);
                Ferme ferme5 = new Ferme(0, "Ferme Test Sud", "Gabès, Tunisie", 85.0, 3);
                Ferme ferme6 = new Ferme(0, "Ferme Test Est", "Monastir, Tunisie", 95.0, 2);
                
                fermeService.insertOne(ferme4);
                fermeService.insertOne(ferme5);
                fermeService.insertOne(ferme6);
                
                System.out.println("🏡 Added 3 new test farms");
            }
        } catch (Exception e) {
            System.err.println("⚠️  Error inserting test farms: " + e.getMessage());
        }
    }
    
    private static void insertTestAnimals() {
        try {
            ServiceAnimaux animauxService = new ServiceAnimaux();
            
            // Check if we have enough animals
            if (animauxService.selectALL().size() < 10) {
                // Sheep
                Animaux mouton1 = new Animaux(0, "Mouton", "Excellent", 
                    Date.valueOf(LocalDate.now().minusMonths(6)), 1);
                Animaux mouton2 = new Animaux(0, "Mouton", "Bon", 
                    Date.valueOf(LocalDate.now().minusMonths(8)), 2);
                
                // Goats
                Animaux chevre1 = new Animaux(0, "Chèvre", "Excellent", 
                    Date.valueOf(LocalDate.now().minusMonths(4)), 3);
                Animaux chevre2 = new Animaux(0, "Chèvre", "Moyen", 
                    Date.valueOf(LocalDate.now().minusMonths(7)), 1);
                
                // Cows
                Animaux vache1 = new Animaux(0, "Vache", "Excellent", 
                    Date.valueOf(LocalDate.now().minusMonths(12)), 2);
                Animaux vache2 = new Animaux(0, "Vache", "Bon", 
                    Date.valueOf(LocalDate.now().minusMonths(10)), 3);
                
                // Add animals
                animauxService.insertOne(mouton1);
                animauxService.insertOne(mouton2);
                animauxService.insertOne(chevre1);
                animauxService.insertOne(chevre2);
                animauxService.insertOne(vache1);
                animauxService.insertOne(vache2);
                
                System.out.println("🐑 Added 6 new test animals (sheep, goats, cows)");
            }
        } catch (Exception e) {
            System.err.println("⚠️  Error inserting test animals: " + e.getMessage());
        }
    }
    
    private static void insertTestPlantes() {
        try {
            ServicePlantes plantesService = new ServicePlantes();
            
            // Check if we have enough plants
            if (plantesService.selectALL().size() < 8) {
                // Vegetables
                Plantes tomate = new Plantes(0, "Tomate", "3 mois", 1, 150.0);
                Plantes carotte = new Plantes(0, "Carotte", "2 mois", 2, 200.0);
                Plantes laitue = new Plantes(0, "Laitue", "1 mois", 3, 100.0);
                
                // Fruits
                Plantes pomme = new Plantes(0, "Pommier", "6 mois", 1, 75.0);
                Plantes orange = new Plantes(0, "Oranger", "8 mois", 2, 60.0);
                Plantes olive = new Plantes(0, "Olivier", "12 mois", 3, 45.0);
                
                // Cereals
                Plantes ble = new Plantes(0, "Blé", "4 mois", 1, 500.0);
                Plantes orge = new Plantes(0, "Orge", "3 mois", 2, 300.0);
                
                // Add plants
                plantesService.insertOne(tomate);
                plantesService.insertOne(carotte);
                plantesService.insertOne(laitue);
                plantesService.insertOne(pomme);
                plantesService.insertOne(orange);
                plantesService.insertOne(olive);
                plantesService.insertOne(ble);
                plantesService.insertOne(orge);
                
                System.out.println("🌱 Added 8 new test plants (vegetables, fruits, cereals)");
            }
        } catch (Exception e) {
            System.err.println("⚠️  Error inserting test plants: " + e.getMessage());
        }
    }
    
    private static void insertTestAnalyses() {
        try {
            AnalyseService analyseService = new AnalyseService();
            
            // Check if we have enough analyses
            if (analyseService.selectALL().size() < 5) {
                // Soil analysis
                Analyse analyse1 = new Analyse(0, null, 
                    "Analyse du sol: pH 7.2, azote moyen, phosphore élevé, potassium moyen. Recommandation: Ajouter du compost.", 
                    2, 1, null);
                
                // Plant health analysis
                Analyse analyse2 = new Analyse(0, null, 
                    "Analyse des plantes: Présence de mildiou sur les tomates. Recommandation: Traitement fongique nécessaire.", 
                    2, 2, null);
                
                // Water quality analysis
                Analyse analyse3 = new Analyse(0, null, 
                    "Analyse de l'eau: Qualité acceptable, légère contamination par les nitrates. Recommandation: Filtration recommandée.", 
                    2, 3, null);
                
                // Animal health analysis
                Analyse analyse4 = new Analyse(0, null, 
                    "Analyse animale: État général bon, mais carence en minéraux. Recommandation: Compléments minéraux.", 
                    2, 1, null);
                
                // Crop yield analysis
                Analyse analyse5 = new Analyse(0, null, 
                    "Analyse de rendement: Rendement en dessous de la moyenne. Recommandation: Réviser les pratiques culturales.", 
                    2, 2, null);
                
                // Add analyses
                analyseService.insertOne(analyse1);
                analyseService.insertOne(analyse2);
                analyseService.insertOne(analyse3);
                analyseService.insertOne(analyse4);
                analyseService.insertOne(analyse5);
                
                System.out.println("🔬 Added 5 new test analyses");
            }
        } catch (Exception e) {
            System.err.println("⚠️  Error inserting test analyses: " + e.getMessage());
        }
    }
    
    private static void insertTestConseils() {
        try {
            ConseilService conseilService = new ConseilService();
            
            // Check if we have enough conseils
            if (conseilService.selectALL().size() < 5) {
                // High priority conseils
                Conseil conseil1 = new Conseil(0, 
                    "URGENT: Traiter immédiatement les plants atteints de mildiou pour éviter la propagation à l'ensemble de la culture.", 
                    Priorite.HAUTE, 2);
                
                Conseil conseil2 = new Conseil(0, 
                    "IMPORTANT: Fournir des compléments minéraux aux animaux pour améliorer leur santé générale.", 
                    Priorite.HAUTE, 4);
                
                // Medium priority conseils
                Conseil conseil3 = new Conseil(0, 
                    "Recommandé: Installer un système de filtration pour améliorer la qualité de l'eau d'irrigation.", 
                    Priorite.MOYENNE, 3);
                
                Conseil conseil4 = new Conseil(0, 
                    "Conseillé: Ajouter du compost au sol pour enrichir la matière organique.", 
                    Priorite.MOYENNE, 1);
                
                // Low priority conseils
                Conseil conseil5 = new Conseil(0, 
                    "À considérer: Réviser les pratiques culturales pour améliorer le rendement des cultures.", 
                    Priorite.BASSE, 5);
                
                // Add conseils
                conseilService.insertOne(conseil1);
                conseilService.insertOne(conseil2);
                conseilService.insertOne(conseil3);
                conseilService.insertOne(conseil4);
                conseilService.insertOne(conseil5);
                
                System.out.println("💡 Added 5 new test conseils");
            }
        } catch (Exception e) {
            System.err.println("⚠️  Error inserting test conseils: " + e.getMessage());
        }
    }
    
    private static void printDataSummary() {
        try {
            UserService userService = new UserService();
            FermeService fermeService = new FermeService();
            ServiceAnimaux animauxService = new ServiceAnimaux();
            ServicePlantes plantesService = new ServicePlantes();
            AnalyseService analyseService = new AnalyseService();
            ConseilService conseilService = new ConseilService();
            
            System.out.println("\n📊 DATABASE SUMMARY:");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("👥 Users: " + userService.selectALL().size());
            System.out.println("🏡 Farms: " + fermeService.selectALL().size());
            System.out.println("🐑 Animals: " + animauxService.selectALL().size());
            System.out.println("🌱 Plants: " + plantesService.selectALL().size());
            System.out.println("🔬 Analyses: " + analyseService.selectALL().size());
            System.out.println("💡 Conseils: " + conseilService.selectALL().size());
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            // Test login credentials
            System.out.println("\n🔑 TEST LOGIN CREDENTIALS:");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("Admin: admin@farmai.tn / password123");
            System.out.println("Expert: expert@farmai.tn / password123");
            System.out.println("Agricole: agricole@farmai.tn / password123");
            System.out.println("Fournisseur: fournisseur@farmai.tn / password123");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
        } catch (Exception e) {
            System.err.println("⚠️  Error generating summary: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        insertTestData();
    }
}