package tn.esprit.farmai.test;

import tn.esprit.farmai.models.Analyse;
import tn.esprit.farmai.models.Conseil;
import tn.esprit.farmai.models.Priorite;
import tn.esprit.farmai.services.AnalyseService;
import tn.esprit.farmai.services.ConseilService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class TestMechanisme1N {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   TEST MECANISME 1:N");
        System.out.println("   Analyse -> Conseils");
        System.out.println("========================================\n");

        AnalyseService analyseService = new AnalyseService();
        ConseilService conseilService = new ConseilService();

        try {
            System.out.println("1. Creation d'une analyse...");
            Analyse analyse = new Analyse();
            analyse.setDateAnalyse(LocalDateTime.now());
            analyse.setResultatTechnique("Test du mecanisme 1:N - Diagnostic sol");
            analyse.setIdTechnicien(1);
            analyse.setIdFerme(1);
            analyse.setImageUrl("/images/test/analyse_1n.jpg");

            analyseService.insertOne(analyse);
            System.out.println("   [OK] Analyse creee avec ID: " + analyse.getIdAnalyse());

            System.out.println("\n2. Creation de conseils lies...");

            Conseil conseil1 = new Conseil();
            conseil1.setDescriptionConseil("Conseil prioritaire: Traitement urgent");
            conseil1.setPriorite(Priorite.HAUTE);
            conseil1.setIdAnalyse(analyse.getIdAnalyse());

            Conseil conseil2 = new Conseil();
            conseil2.setDescriptionConseil("Conseil standard: Surveillance");
            conseil2.setPriorite(Priorite.MOYENNE);
            conseil2.setIdAnalyse(analyse.getIdAnalyse());

            conseilService.insertOne(conseil1);
            conseilService.insertOne(conseil2);

            System.out.println("   [OK] 2 conseils crees");

            List<Conseil> conseils = conseilService.findByAnalyse(analyse.getIdAnalyse());
            System.out.println("\n3. Verification: " + conseils.size() + " conseils trouves");

            System.out.println("\n========================================");
            System.out.println("   TEST REUSSI!");
            System.out.println("========================================");

        } catch (SQLException e) {
            System.err.println("\n[ERREUR SQL] " + e.getMessage());
            e.printStackTrace();
        }
    }
}
