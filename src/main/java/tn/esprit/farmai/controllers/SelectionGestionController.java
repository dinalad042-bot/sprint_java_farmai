package tn.esprit.farmai.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import tn.esprit.farmai.utils.NavigationUtil;

/**
 * Contrôleur pour la page de sélection des modules de gestion (Fermes, Animaux, Plantes).
 */
public class SelectionGestionController {

    /**
     * Redirige l'utilisateur vers la gestion des fermes.
     * Déclenché par un clic sur la carte VBox correspondante.
     */
    @FXML
    private void goToFermes(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/tn/esprit/farmai/views/gestion-fermes.fxml", "FarmAI - Gestion des Fermes");
    }

    /**
     * Redirige l'utilisateur vers la gestion du bétail (Animaux).
     * Déclenché par un clic sur la carte VBox correspondante.
     */
    @FXML
    private void goToAnimaux(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/tn/esprit/farmai/views/gestion-animaux.fxml", "FarmAI - Gestion des Animaux");
    }

    /**
     * Redirige l'utilisateur vers la gestion des cultures (Plantes).
     * Déclenché par un clic sur la carte VBox correspondante.
     */
    @FXML
    private void goToPlantes(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/tn/esprit/farmai/views/gestion-plantes.fxml", "FarmAI - Gestion des Plantes");
    }

    /**
     * Retourne au tableau de bord principal.
     * Utilisé par les boutons "Tableau de bord" et "Retour" dans la barre latérale.
     */
    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        NavigationUtil.navigateTo(stage, "/tn/esprit/farmai/views/agricole-dashboard.fxml", "FarmAI - Tableau de bord");
    }
}