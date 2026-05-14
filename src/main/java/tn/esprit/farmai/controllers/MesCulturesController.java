package tn.esprit.farmai.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.scene.image.ImageView;
import tn.esprit.farmai.utils.AvatarUtil;
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
 * Controller for Mes Cultures hub view.
 * Provides navigation to Animaux, Plantes, and Fermes management.
 */
public class MesCulturesController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(MesCulturesController.class.getName());

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

    @FXML
    private ImageView headerProfileImage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            ProfileManager.updateProfileUI(currentUser, welcomeLabel, userNameLabel, sidebarAvatar, sidebarAvatarText);

            // Load header profile image
            if (headerProfileImage != null) {
                AvatarUtil.loadUserImageIntoImageView(headerProfileImage, currentUser, 36);
            }
            if (userRoleLabel != null) {
                userRoleLabel.setText(currentUser.getRole().getDisplayName());
            }
        }

        // Auto-refresh sidebar when user profile changes (avatar, name, etc.)
        SessionManager.getInstance().currentUserProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser != null) {
                javafx.application.Platform.runLater(() -> ProfileManager.updateProfileUI(newUser, welcomeLabel,
                        userNameLabel, sidebarAvatar, sidebarAvatarText));
            }
        });
    }

    /**
     * Navigate to Animaux (Animals) management
     */
    @FXML
    private void handleAnimaux() {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/tn/esprit/farmai/views/gestion-animaux.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 800);
            stage.setScene(scene);
            stage.setTitle("FarmAI - Mes Animaux");
            stage.show();
            LOGGER.log(Level.INFO, "Navigated to animal management");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to animal management", e);
            NavigationUtil.showError("Erreur", "Impossible d'ouvrir la gestion des animaux.");
        }
    }

    /**
     * Navigate to Plantes (Plants) management
     */
    @FXML
    private void handlePlantes() {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/tn/esprit/farmai/views/gestion-plantes.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 800);
            stage.setScene(scene);
            stage.setTitle("FarmAI - Mes Plantes");
            stage.show();
            LOGGER.log(Level.INFO, "Navigated to plant management");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to plant management", e);
            NavigationUtil.showError("Erreur", "Impossible d'ouvrir la gestion des plantes.");
        }
    }

    /**
     * Navigate to Fermes (Farms) management
     */
    @FXML
    private void handleFermes() {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/tn/esprit/farmai/views/gestion-fermes.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 800);
            stage.setScene(scene);
            stage.setTitle("FarmAI - Mes Fermes");
            stage.show();
            LOGGER.log(Level.INFO, "Navigated to farm management");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to farm management", e);
            NavigationUtil.showError("Erreur", "Impossible d'ouvrir la gestion des fermes.");
        }
    }

    /**
     * Handle back button - return to Agricole Dashboard
     */
    @FXML
    private void handleBack() {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/tn/esprit/farmai/views/agricole-dashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 800);

            // Apply CSS
            java.net.URL cssUrl = getClass().getResource("/tn/esprit/farmai/styles/dashboard.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("FarmAI - Tableau de Bord");
            stage.show();
            LOGGER.log(Level.INFO, "Navigated back to dashboard");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to dashboard", e);
            NavigationUtil.showError("Erreur", "Impossible de retourner au tableau de bord.");
        }
    }

    /**
     * Handle AI Analysis - Navigate to fermier analyses view
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
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/tn/esprit/farmai/views/fermier-analyses.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 800);
            stage.setScene(scene);
            stage.setTitle("FarmAI - Mes Analyses");
            stage.show();
            LOGGER.log(Level.INFO, "Successfully navigated to analyses view");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to analyses view", e);
            NavigationUtil.showError("Erreur", "Impossible d'ouvrir la gestion des analyses.");
        }
    }

    /**
     * Handle Expert Analyses - Navigate to agricole statistics view (read-only)
     */
    @FXML
    private void handleExpertAnalyses() {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/tn/esprit/farmai/views/agricole-statistics.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 800);

            // Apply CSS
            java.net.URL cssUrl = getClass().getResource("/tn/esprit/farmai/styles/dashboard.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("FarmAI - Statistiques");
            stage.show();
            LOGGER.log(Level.INFO, "Navigated to statistics view");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to statistics", e);
            NavigationUtil.showError("Erreur", "Impossible d'ouvrir les statistiques.");
        }
    }

    /**
     * Handle Add Face - Open Face Recognition view for face enrollment
     */
    @FXML
    private void handleAddFace() {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/tn/esprit/farmai/views/face-recognition-view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 800, 600);
            String cssPath = getClass().getResource("/tn/esprit/farmai/styles/dashboard.css") != null
                    ? getClass().getResource("/tn/esprit/farmai/styles/dashboard.css").toExternalForm()
                    : null;
            if (cssPath != null) {
                scene.getStylesheets().add(cssPath);
            }

            Stage faceStage = new Stage();
            faceStage.initOwner(stage);
            faceStage.setTitle("FarmAI - Enregistrement Visage");
            faceStage.setScene(scene);

            // Cleanup camera when window closes
            FaceRecognitionController controller = loader.getController();
            faceStage.setOnCloseRequest(e -> controller.cleanup());

            faceStage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to open face recognition", e);
            NavigationUtil.showError("Erreur", "Impossible d'ouvrir la reconnaissance faciale: " + e.getMessage());
        }
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
     * Handle logout
     */
    @FXML
    private void handleLogout() {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        NavigationUtil.logout(stage);
    }
}
