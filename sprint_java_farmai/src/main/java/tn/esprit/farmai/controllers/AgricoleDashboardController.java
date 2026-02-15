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
     * Handle AI analysis
     */
    @FXML
    private void handleAIAnalysis() {
        NavigationUtil.showSuccess("Analyse IA", "Module d'analyse IA à venir.");
    }
}
