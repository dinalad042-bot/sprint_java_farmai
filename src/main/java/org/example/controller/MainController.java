package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import org.example.dao.ServiceDAO;
import org.example.entity.Service;

import java.io.IOException;
import java.util.List;

/**
 * Contrôleur principal : affiche les vues Services / Achats et déclenche l'alerte stock critique.
 */
public class MainController {

    @FXML private StackPane contentArea;

    @FXML
    private void afficherServices() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/service-view.fxml"));
            Parent root = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
            ServiceController ctrl = loader.getController();
            if (ctrl != null) {
                ctrl.chargerEtAlerterStockCritique();
            }
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger la vue Services", e);
        }
    }
    @FXML
    private void afficherVentes() {
        loadView("/org/example/view/vente-view.fxml");
    }
    @FXML
    private void afficherAchats() {
        loadView("/org/example/view/achat-view.fxml");
    }

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger la vue: " + fxml, e);
        }
    }

    /**
     * Affiche une alerte si des services sont en dessous du seuil critique.
     * Exemple d'utilisation au démarrage ou à l'ouverture du module.
     */
    public static void alerterStockCritiqueSiNecessaire() {
        List<Service> critiques = new ServiceDAO().findStockCritique();
        if (critiques.isEmpty()) return;
        StringBuilder sb = new StringBuilder("Les services suivants sont en stock critique :\n\n");
        for (Service s : critiques) {
            sb.append("• ").append(s.getNom())
                    .append(" — Stock actuel: ").append(s.getStock())
                    .append(", Seuil critique: ").append(s.getSeuilCritique()).append("\n");
        }
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Stock critique");
        alert.setHeaderText("Attention : stock insuffisant");
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }
}
