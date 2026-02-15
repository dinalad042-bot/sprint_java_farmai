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
     * Handle consultations
     */
    @FXML
    private void handleConsultations() {
        NavigationUtil.showSuccess("Consultations", "Module de consultations à venir.");
    }

    /**
     * Handle recommendations
     */
    @FXML
    private void handleRecommendations() {
        NavigationUtil.showSuccess("Recommandations", "Module de recommandations à venir.");
    }
}
