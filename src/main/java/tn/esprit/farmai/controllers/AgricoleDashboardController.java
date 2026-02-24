package tn.esprit.farmai.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.utils.NavigationUtil;
import tn.esprit.farmai.utils.ProfileManager;
import tn.esprit.farmai.utils.SessionManager;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Agricole Dashboard.
 * Provides navigation to analysis consultation for fermier users.
 */
public class AgricoleDashboardController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(AgricoleDashboardController.class.getName());

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userRoleLabel;

    @FXML
    private Circle sidebarAvatar;

    @FXML
    private Text sidebarAvatarText;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            ProfileManager.updateProfileUI(currentUser, welcomeLabel, userNameLabel, sidebarAvatar, sidebarAvatarText);
            if (userRoleLabel != null) {
                userRoleLabel.setText(currentUser.getRole().getDisplayName());
            }
        }
    }

    /**
     * Handle logout
     */
    @FXML
    private void handleLogout() {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        NavigationUtil.logout(stage);
    }

    /**
     * Handle profile click
     */
    @FXML
    private void handleProfile() {
        boolean updated = ProfileManager.showProfileEditDialog(welcomeLabel.getScene().getWindow());
        if (updated) {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            ProfileManager.updateProfileUI(currentUser, welcomeLabel, userNameLabel, sidebarAvatar, sidebarAvatarText);
        }
    }

    /**
     * Handle my farms
     */
    @FXML
    private void handleMyFarms() {
        NavigationUtil.showSuccess("Mes Exploitations", "Module des exploitations à venir.");
    }

    /**
     * Handle my crops
     */
    @FXML
    private void handleMyCrops() {
        NavigationUtil.showSuccess("Mes Cultures", "Module des cultures à venir.");
    }

    /**
     * Handle orders
     */
    @FXML
    private void handleOrders() {
        NavigationUtil.showSuccess("Commandes", "Module des commandes à venir.");
    }

    /**
     * Handle AI analysis (Consultation Fermier)
     * Navigates to the fermier analyses view where the user can see expert analyses.
     */
    @FXML
    private void handleAIAnalysis() {
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                LOGGER.log(Level.WARNING, "User session not found when trying to access analyses");
                NavigationUtil.showError("Session expirée", "Veuillez vous reconnecter pour accéder aux analyses.");
                return;
            }

            LOGGER.log(Level.INFO, "User {0} navigating to analyses view", currentUser.getFullName());

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            javafx.scene.Parent currentRoot = welcomeLabel.getScene().getRoot();

            // Fade out animation
            javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(200), currentRoot);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(event -> {
                try {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                            getClass().getResource("/tn/esprit/farmai/views/fermier-analyses.fxml"));
                    javafx.scene.Parent newRoot = loader.load();
                    javafx.scene.Scene scene = new javafx.scene.Scene(newRoot, 1200, 800);

                    newRoot.setOpacity(0.0);
                    stage.setScene(scene);

                    // Fade in animation
                    javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                            javafx.util.Duration.millis(250), newRoot);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();

                    stage.show();
                    LOGGER.log(Level.INFO, "Successfully navigated to analyses view");
                } catch (java.io.IOException e) {
                    LOGGER.log(Level.SEVERE, "Failed to load fermier-analyses.fxml", e);
                    NavigationUtil.showError("Erreur de chargement", 
                            "Impossible de charger la vue des analyses.\n" +
                            "Veuillez réessayer ou contacter le support.");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Unexpected error loading analyses view", e);
                    NavigationUtil.showError("Erreur", 
                            "Une erreur inattendue s'est produite: " + e.getMessage());
                }
            });

            fadeOut.play();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during navigation to analyses", e);
            NavigationUtil.showError("Erreur de navigation", 
                    "Impossible de naviguer vers les analyses: " + e.getMessage());
        }
    }
}
