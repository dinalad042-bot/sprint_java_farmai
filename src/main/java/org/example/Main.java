package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.controller.MainController;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/main-view.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1600, 800);
        stage.setTitle("FarmIA Desk - Gestion Achat et Stocks");
        stage.setScene(scene);
        stage.show();

        // Alerte stock critique au démarrage (exemple)
        MainController.alerterStockCritiqueSiNecessaire();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
