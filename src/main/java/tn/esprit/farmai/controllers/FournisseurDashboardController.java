package tn.esprit.farmai.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.MatiereService;
import tn.esprit.farmai.services.ProduitService;
import tn.esprit.farmai.services.VenteService;
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

    private final MatiereService matiereService;
    private final ProduitService produitService;
    private final VenteService venteService;

    public FournisseurDashboardController() {
        this.matiereService = new MatiereService();
        this.produitService = new ProduitService();
        this.venteService = new VenteService();
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
     * Load real ERP statistics from database using COUNT queries on a background thread.
     */
    private void loadStatistics() {
        // Run DB calls off the JavaFX Application Thread to avoid UI freeze
        Thread bgThread = new Thread(() -> {
            try {
                int totalMatieres = matiereService.countAll();
                int totalProduits = produitService.countAll();
                int totalVentes   = venteService.selectAll().size();

                LOGGER.log(Level.INFO, "Fournisseur ERP Statistics loaded: {0} matières, {1} produits, {2} ventes",
                        new Object[] { totalMatieres, totalProduits, totalVentes });

                javafx.application.Platform.runLater(() -> {
                    if (totalAnalysesLabel != null) totalAnalysesLabel.setText(String.valueOf(totalMatieres));
                    if (totalFermesLabel != null)   totalFermesLabel.setText(String.valueOf(totalProduits));
                    if (totalConseilsLabel != null)  totalConseilsLabel.setText(String.valueOf(totalVentes));
                });
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error loading fournisseur ERP statistics", e);
                javafx.application.Platform.runLater(() -> {
                    if (totalAnalysesLabel != null) totalAnalysesLabel.setText("-");
                    if (totalFermesLabel != null)   totalFermesLabel.setText("-");
                    if (totalConseilsLabel != null)  totalConseilsLabel.setText("-");
                });
            }
        });
        bgThread.setDaemon(true);
        bgThread.start();
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
     * Handle my products → navigate to ERP Matières (fournisseur catalogue)
     */
    @FXML
    private void handleMyProducts() {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "views/erp-matieres.fxml", "Mon Catalogue");
    }

    /**
     * Handle deliveries → navigate to ERP Ventes (fournisseur sells produits finis)
     */
    @FXML
    private void handleDeliveries() {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        NavigationUtil.navigateTo(stage, "views/erp-ventes.fxml", "Mes Ventes");
    }
}
