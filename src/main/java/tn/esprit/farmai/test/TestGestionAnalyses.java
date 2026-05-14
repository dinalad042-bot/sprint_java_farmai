package tn.esprit.farmai.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

/**
 * Test class for GestionAnalyses.fxml interface.
 * Run this to test the analysis management GUI.
 */
public class TestGestionAnalyses extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML file
        URL fxmlUrl = getClass().getResource("/tn/esprit/farmai/views/gestion-analyses.fxml");
        if (fxmlUrl == null) {
            throw new IllegalStateException("Cannot find FXML file: gestion-analyses.fxml");
        }
        Parent root = FXMLLoader.load(fxmlUrl);

        // Setup the stage
        primaryStage.setTitle("Gestion des Analyses - Test Interface");
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);

        // Show the window
        primaryStage.show();

        System.out.println("✓ Interface GestionAnalyses chargée avec succès!");
        System.out.println("✓ TableView doit afficher les analyses de la base de données.");
    }

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   TEST INTERFACE GESTION ANALYSES");
        System.out.println("========================================");
        System.out.println("Lancement de l'interface JavaFX...\n");

        launch(args);
    }
}
