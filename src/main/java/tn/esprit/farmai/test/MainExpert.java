package tn.esprit.farmai.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Point d'entrée principal pour le lot 3 - Expert.
 * Démarre directement sur l'interface GestionAnalyses.fxml.
 */
public class MainExpert extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        // Load GestionAnalyses view directly
        FXMLLoader fxmlLoader = new FXMLLoader(MainExpert.class.getResource("/tn/esprit/farmai/views/gestion-analyses.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, 1200, 800);

        // Load CSS
        String cssPath = MainExpert.class.getResource("/tn/esprit/farmai/styles/dashboard.css") != null
                ? MainExpert.class.getResource("/tn/esprit/farmai/styles/dashboard.css").toExternalForm()
                : null;
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        }

        stage.setTitle("FarmAI - Gestion des Analyses (Expert)");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.centerOnScreen();
        stage.show();
    }

    /**
     * Get the primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch();
    }
}
