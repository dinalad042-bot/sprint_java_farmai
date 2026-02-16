package tn.esprit.farmai.controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.utils.NavigationUtil;
import tn.esprit.farmai.utils.ProfileManager;
import tn.esprit.farmai.utils.SessionManager;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for Expert Dashboard.
 */
public class ExpertDashboardController implements Initializable {

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
     * Handle consultations - Open GestionAnalyses with fade transition
     */
    @FXML
    private void handleConsultations() {
        navigateWithFade("/tn/esprit/farmai/views/gestion-analyses.fxml", 
                        "FarmAI - Gestion des Analyses");
    }

    /**
     * Handle recommendations - Open GestionConseils with fade transition
     */
    @FXML
    private void handleRecommendations() {
        navigateWithFade("/tn/esprit/farmai/views/gestion-conseils.fxml", 
                        "FarmAI - Gestion des Conseils");
    }

    /**
     * Navigate to a new view with smooth fade transition
     */
    private void navigateWithFade(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            javafx.scene.Parent currentRoot = welcomeLabel.getScene().getRoot();
            
            // Fade out current scene
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentRoot);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            
            fadeOut.setOnFinished(event -> {
                try {
                    // Load new view
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource(fxmlPath));
                    javafx.scene.Parent newRoot = loader.load();
                    
                    // Create scene with new root
                    Scene scene = new Scene(newRoot, 1200, 800);
                    
                    // Apply CSS
                    String cssPath = getClass().getResource("/tn/esprit/farmai/styles/dashboard.css") != null
                            ? getClass().getResource("/tn/esprit/farmai/styles/dashboard.css").toExternalForm()
                            : null;
                    if (cssPath != null) {
                        scene.getStylesheets().add(cssPath);
                    }
                    
                    // Set initial opacity for fade in
                    newRoot.setOpacity(0.0);
                    stage.setScene(scene);
                    stage.setTitle(title);
                    
                    // Fade in new scene
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(250), newRoot);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                    
                    stage.show();
                } catch (Exception e) {
                    NavigationUtil.showError("Erreur", "Impossible de charger la vue: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
            fadeOut.play();
            
        } catch (Exception e) {
            NavigationUtil.showError("Erreur", "Erreur de navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Play entrance animation for dashboard elements
     */
    private void playEntranceAnimation() {
        if (welcomeLabel != null) {
            welcomeLabel.setOpacity(0.0);
            welcomeLabel.setTranslateY(20);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), welcomeLabel);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            
            javafx.animation.TranslateTransition slideUp = new javafx.animation.TranslateTransition(
                Duration.millis(400), welcomeLabel);
            slideUp.setFromY(20);
            slideUp.setToY(0);
            
            fadeIn.play();
            slideUp.play();
        }
    }
}
