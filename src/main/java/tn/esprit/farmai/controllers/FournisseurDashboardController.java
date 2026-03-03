package tn.esprit.farmai.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.AnalyseService;
import tn.esprit.farmai.services.ConseilService;
import tn.esprit.farmai.services.FermeService;
import tn.esprit.farmai.utils.NavigationUtil;
import tn.esprit.farmai.utils.ProfileManager;
import tn.esprit.farmai.utils.SessionManager;
import javafx.scene.image.ImageView;
import tn.esprit.farmai.utils.AvatarUtil;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Fournisseur Dashboard.
 * Loads dynamic statistics from Analyse, Conseil and Ferme services.
 */
public class FournisseurDashboardController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(FournisseurDashboardController.class.getName());

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

    // Dynamic statistics labels
    @FXML
    private Label totalAnalysesLabel;

    @FXML
    private Label totalFermesLabel;

    @FXML
    private Label totalConseilsLabel;

    private final AnalyseService analyseService;
    private final ConseilService conseilService;
    private final FermeService fermeService;

    public FournisseurDashboardController() {
        this.analyseService = new AnalyseService();
        this.conseilService = new ConseilService();
        this.fermeService = new FermeService();
    }

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
                javafx.application.Platform
                        .runLater(() -> ProfileManager.updateProfileUI(newUser, welcomeLabel, userNameLabel,
                                sidebarAvatar, sidebarAvatarText));
            }
        });

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

            // Total fermes
            int totalFermes = fermeService.selectALL().size();
            if (totalFermesLabel != null) {
                totalFermesLabel.setText(String.valueOf(totalFermes));
            }

            // Total conseils
            int totalConseils = conseilService.selectALL().size();
            if (totalConseilsLabel != null) {
                totalConseilsLabel.setText(String.valueOf(totalConseils));
            }

            LOGGER.log(Level.INFO, "Fournisseur Statistics loaded: {0} analyses, {1} fermes, {2} conseils",
                    new Object[] { totalAnalyses, totalFermes, totalConseils });

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading fournisseur statistics", e);
            // Set default values on error
            if (totalAnalysesLabel != null)
                totalAnalysesLabel.setText("-");
            if (totalFermesLabel != null)
                totalFermesLabel.setText("-");
            if (totalConseilsLabel != null)
                totalConseilsLabel.setText("-");
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
            // Load header profile image
            if (headerProfileImage != null) {
                AvatarUtil.loadUserImageIntoImageView(headerProfileImage, currentUser, 36);
            }
        }
    }

    /**
     * Handle my products
     */
    @FXML
    private void handleMyProducts() {
        NavigationUtil.showSuccess("Mes Produits", "Module des produits à venir.");
    }

    /**
     * Handle orders
     */
    @FXML
    private void handleOrders() {
        NavigationUtil.showSuccess("Commandes", "Module des commandes à venir.");
    }

    /**
     * Handle deliveries
     */
    @FXML
    private void handleDeliveries() {
        NavigationUtil.showSuccess("Livraisons", "Module des livraisons à venir.");
    }

    /**
     * Handle inventory
     */
    @FXML
    private void handleInventory() {
        NavigationUtil.showSuccess("Stock", "Module de stock à venir.");
    }

    /**
     * Handle statistics
     */
    @FXML
    private void handleStatistics() {
        NavigationUtil.showSuccess("Statistiques", "Module des statistiques à venir.");
    }
}
