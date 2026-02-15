package tn.esprit.farmai.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

/**
 * Test class for AjoutConseil.fxml interface.
 * Run this to test the advice creation GUI with 1:N relationship.
 */
public class TestAjoutConseil extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML file
        URL fxmlUrl = getClass().getResource("/tn/esprit/farmai/views/ajout-conseil.fxml");
        if (fxmlUrl == null) {
            throw new IllegalStateException("Cannot find FXML file: ajout-conseil.fxml");
        }
        Parent root = FXMLLoader.load(fxmlUrl);

        // Setup the stage
        primaryStage.setTitle("Ajouter un Conseil - Test Interface");
        primaryStage.setScene(new Scene(root, 650, 750));
        primaryStage.setResizable(false);

        // Show the window
        primaryStage.show();

        System.out.println("✓ Interface AjoutConseil chargée avec succès!");
        System.out.println("✓ Testez la relation 1:N avec le ComboBox Analyse.");
        System.out.println("✓ Vérifiez la validation des champs (US11).");
    }

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   TEST INTERFACE AJOUT CONSEIL");
        System.out.println("   US5 & US7 - Relation 1:N");
        System.out.println("========================================");
        System.out.println("Lancement de l'interface JavaFX...\n");

        launch(args);
    }
}
