package tn.esprit.farmai.test;

import tn.esprit.farmai.models.Analyse;
import tn.esprit.farmai.models.Conseil;
import tn.esprit.farmai.models.Priorite;
import tn.esprit.farmai.services.AnalyseService;
import tn.esprit.farmai.services.ConseilService;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class TestBD {
    public static void main(String[] args) {
        try {
            System.out.println("Début du test CRUD...");
            AnalyseService as = new AnalyseService();
            ConseilService cs = new ConseilService();

            // 1. Create Analyse
            Analyse testA = new Analyse(LocalDateTime.now(), "Test tech result", 1, 1, "test.png");
            as.insertOne(testA);
            System.out.println("Analyse insérée avec ID : " + testA.getIdAnalyse());

            // 2. Create Conseil
            Conseil testC = new Conseil("Acheter Engrais XYZ 123", Priorite.HAUTE, testA.getIdAnalyse());
            cs.insertOne(testC);
            System.out.println("Conseil inséré avec ID : " + testC.getIdConseil());

            System.out.println("Check DB relations: Conseil créé pour l'Analyse.");

            // 3. Update
            testA.setResultatTechnique("Updated test tech result");
            as.updateOne(testA);
            System.out
                    .println("Analyse mise à jour : " + as.findById(testA.getIdAnalyse()).get().getResultatTechnique());

// 4. Delete
        cs.deleteOne(testC);
        as.deleteOne(testA);
        System.out.println("Données de test supprimées.");
            System.out.println("Test CRUD terminé avec succès.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("ERREUR LORS DU TEST CRUD !");
        }
    }
}
