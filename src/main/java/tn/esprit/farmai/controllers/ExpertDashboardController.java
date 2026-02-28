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
import tn.esprit.farmai.services.AnalyseService;
import tn.esprit.farmai.services.ConseilService;
import tn.esprit.farmai.services.FermeService;
import tn.esprit.farmai.utils.NavigationUtil;
import tn.esprit.farmai.utils.ProfileManager;
import tn.esprit.farmai.utils.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Expert Dashboard.
 * Loads dynamic statistics from Analyse and Conseil services.
 */
public class ExpertDashboardController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ExpertDashboardController.class.getName());

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

    // Dynamic statistics labels
    @FXML
    private Label totalAnalysesLabel;

    @FXML
    private Label totalConseilsLabel;

    @FXML
    private Label totalFermesLabel;

    private final AnalyseService analyseService;
    private final ConseilService conseilService;
    private final FermeService fermeService;

    public ExpertDashboardController() {
        this.analyseService = new AnalyseService();
        this.conseilService = new ConseilService();
        this.fermeService = new FermeService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            ProfileManager.updateProfileUI(currentUser, welcomeLabel, userNameLabel, sidebarAvatar, sidebarAvatarText);
            if (userRoleLabel != null) {
                userRoleLabel.setText(currentUser.getRole().getDisplayName());
            }
        }
        
        // Load dynamic statistics
        loadStatistics();
    }

    /**
     * Load real statistics from database
     */
    private void loadStatistics() {
        try {
            // Total analyses
            int totalAnalyses = analyseService.selectALL().size();
            if (totalAnalysesLabel != null) {
                totalAnalysesLabel.setText(String.valueOf(totalAnalyses));
            }

            // Total conseils
            int totalConseils = conseilService.selectALL().size();
            if (totalConseilsLabel != null) {
                totalConseilsLabel.setText(String.valueOf(totalConseils));
            }

            // Total fermes
            int totalFermes = fermeService.selectALL().size();
            if (totalFermesLabel != null) {
                totalFermesLabel.setText(String.valueOf(totalFermes));
            }

            LOGGER.log(Level.INFO, "Statistics loaded: {0} analyses, {1} conseils, {2} fermes", 
                    new Object[]{totalAnalyses, totalConseils, totalFermes});

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading statistics", e);
            // Set default values on error
            if (totalAnalysesLabel != null) totalAnalysesLabel.setText("-");
            if (totalConseilsLabel != null) totalConseilsLabel.setText("-");
            if (totalFermesLabel != null) totalFermesLabel.setText("-");
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
     * Handle statistics - Open Statistics Dashboard (US10)
     */
    @FXML
    private void handleStatistics() {
        navigateWithFade("/tn/esprit/farmai/views/statistics.fxml", 
                        "FarmAI - Statistiques");
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
