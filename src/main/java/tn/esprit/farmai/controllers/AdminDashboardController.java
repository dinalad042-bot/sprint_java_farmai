package tn.esprit.farmai.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.farmai.models.Role;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.UserService;
import tn.esprit.farmai.utils.NavigationUtil;
import tn.esprit.farmai.utils.ProfileManager;
import tn.esprit.farmai.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Controller for Admin Dashboard.
 */
public class AdminDashboardController implements Initializable {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label totalUsersLabel;

    @FXML
    private Label totalAdminsLabel;

    @FXML
    private Label totalExpertsLabel;

    @FXML
    private Label totalAgricolesLabel;

    @FXML
    private Label totalFournisseursLabel;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userRoleLabel;

    @FXML
    private VBox statsContainer;

    private final UserService userService;

    public AdminDashboardController() {
        this.userService = new UserService();
    }

    @FXML
    private javafx.scene.image.ImageView profileImageView;

    @FXML
    private javafx.scene.image.ImageView headerProfileImageView;

    @FXML
    private void handleViewStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/tn/esprit/farmai/views/user-statistics.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("FarmAi — Statistiques Utilisateurs");
            stage.setScene(new Scene(root));
            stage.initOwner(welcomeLabel.getScene().getWindow());
            stage.show();
        } catch (IOException e) {
            NavigationUtil.showError("Erreur", "Impossible d'ouvrir les statistiques.");
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set user info
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
            if (userRoleLabel != null) {
                userRoleLabel.setText(currentUser.getRole().getDisplayName());
            }
        }

        // Load statistics
        loadStatistics();
    }

    private void updateUI(User user) {
        ProfileManager.updateProfileUI(user, welcomeLabel, userNameLabel, null, null, profileImageView);
        ProfileManager.loadUserImageIntoImageView(headerProfileImageView, user);
    }

    /**
     * Load dashboard statistics
     */
    private void loadStatistics() {
        try {
            int totalUsers = userService.countAll();
            int admins = userService.countByRole(Role.ADMIN);
            int experts = userService.countByRole(Role.EXPERT);
            int agricoles = userService.countByRole(Role.AGRICOLE);
            int fournisseurs = userService.countByRole(Role.FOURNISSEUR);

            if (totalUsersLabel != null)
                totalUsersLabel.setText(String.valueOf(totalUsers));
            if (totalAdminsLabel != null)
                totalAdminsLabel.setText(String.valueOf(admins));
            if (totalExpertsLabel != null)
                totalExpertsLabel.setText(String.valueOf(experts));
            if (totalAgricolesLabel != null)
                totalAgricolesLabel.setText(String.valueOf(agricoles));
            if (totalFournisseursLabel != null)
                totalFournisseursLabel.setText(String.valueOf(fournisseurs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Navigate to user management
     */
    @FXML
    private void handleManageUsers() {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        NavigationUtil.navigateToUserList(stage);
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
     * Refresh statistics
     */
    @FXML
    private void handleRefresh() {
        loadStatistics();
    }

    /**
     * Handle profile click/edit
     */
    @FXML
    private void handleProfileEdit() {
        boolean updated = ProfileManager.showProfileEditDialog(welcomeLabel.getScene().getWindow());

        if (updated) {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            updateUI(currentUser);
        }
    }

    /**
     * Handle profile click (placeholder)
     */
    @FXML
    private void handleProfile() {
        handleProfileEdit();
    }

    /**
     * Opens the Face Recognition (camera + detection) window.
     * Users can enrol their face here.
     */
    @FXML
    private void handleFaceRecognition() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/tn/esprit/farmai/views/face-recognition-view.fxml"));
            Parent root = loader.load();
            FaceRecognitionController ctrl = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("FarmAi — Reconnaissance Faciale");
            stage.setScene(new Scene(root));
            stage.setOnCloseRequest(e -> ctrl.cleanup());
            stage.show();
        } catch (IOException e) {
            NavigationUtil.showError("Erreur", "Impossible d'ouvrir la reconnaissance faciale.");
            e.printStackTrace();
        }
    }
}
