package tn.esprit.farmai.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.utils.NavigationUtil;
import tn.esprit.farmai.utils.ProfileManager;
import tn.esprit.farmai.utils.SessionManager;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for Agricole Dashboard.
 */
public class AgricoleDashboardController implements Initializable {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userRoleLabel;

    @FXML
    private javafx.scene.image.ImageView profileImageView;

    @FXML
    private javafx.scene.image.ImageView headerProfileImageView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
            if (userRoleLabel != null) {
                userRoleLabel.setText(currentUser.getRole().getDisplayName());
            }
        }
    }

    private void updateUI(User user) {
        ProfileManager.updateProfileUI(user, welcomeLabel, userNameLabel, null, null, profileImageView);
        ProfileManager.loadUserImageIntoImageView(headerProfileImageView, user);
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
            updateUI(currentUser);
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
     * Handle AI analysis
     */
    @FXML
    private void handleAIAnalysis() {
        NavigationUtil.showSuccess("Analyse IA", "Module d'analyse IA à venir.");
    }

    @FXML
    private void handleFaceRecognition() {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "views/face-recognition-view.fxml", "Reconnaissance Faciale");
    }
}
