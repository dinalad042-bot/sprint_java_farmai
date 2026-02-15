package tn.esprit.farmai;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application class for FarmAI.
 * Starts with the login screen.
 */
public class HelloApplication extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        // Load login view
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("views/login.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, 900, 600);

        // Load CSS
        String cssPath = HelloApplication.class.getResource("styles/auth.css") != null
                ? HelloApplication.class.getResource("styles/auth.css").toExternalForm()
                : null;
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        }

        stage.setTitle("FarmAI - Connexion");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(500);
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