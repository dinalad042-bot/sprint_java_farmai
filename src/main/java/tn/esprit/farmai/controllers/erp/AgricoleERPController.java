package tn.esprit.farmai.controllers.erp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tn.esprit.farmai.models.Matiere;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.AchatService;
import tn.esprit.farmai.services.MatiereService;
import tn.esprit.farmai.services.ProduitService;
import tn.esprit.farmai.utils.*;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * ERP Production hub controller for the AGRICOLE role.
 * Mirrors the Symfony erp/dashboard/agricole.html.twig view.
 *
 * Agriculteur can:
 *  - Browse the matière catalogue (read-only)
 *  - Create/view purchase orders (Achats) — pay to receive stock
 *  - Create/manage finished products and launch production (Produire)
 */
public class AgricoleERPController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(AgricoleERPController.class.getName());

    // Sidebar profile
    @FXML private Label welcomeLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Circle sidebarAvatar;
    @FXML private Text sidebarAvatarText;
    @FXML private ImageView headerProfileImage;

    // Stats labels
    @FXML private Label totalMatieresLabel;
    @FXML private Label totalProduitsLabel;
    @FXML private Label totalAchatsLabel;

    // Critical stock alert
    @FXML private HBox alertBox;
    @FXML private Label alertLabel;

    private final MatiereService matiereService = new MatiereService();
    private final ProduitService produitService = new ProduitService();
    private final AchatService achatService = new AchatService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            ProfileManager.updateProfileUI(currentUser, welcomeLabel, userNameLabel, sidebarAvatar, sidebarAvatarText);
            if (headerProfileImage != null)
                AvatarUtil.loadUserImageIntoImageView(headerProfileImage, currentUser, 36);
            if (userRoleLabel != null)
                userRoleLabel.setText("Production ERP");
        }

        // Load stats on background thread to avoid UI freeze
        Thread bg = new Thread(() -> {
            try {
                int matieres = matiereService.countAll();
                int produits = produitService.countAll();
                int achats   = achatService.selectAll().size();

                // Check critical stock
                List<Matiere> critiques = matiereService.findStockCritique();

                Platform.runLater(() -> {
                    if (totalMatieresLabel != null) totalMatieresLabel.setText(String.valueOf(matieres));
                    if (totalProduitsLabel  != null) totalProduitsLabel.setText(String.valueOf(produits));
                    if (totalAchatsLabel    != null) totalAchatsLabel.setText(String.valueOf(achats));

                    if (!critiques.isEmpty() && alertBox != null) {
                        String names = critiques.stream().map(Matiere::getNom).collect(Collectors.joining(", "));
                        alertLabel.setText("⚠️ Stock critique pour: " + names + " — Réapprovisionnez via Achats.");
                        alertBox.setVisible(true);
                        alertBox.setManaged(true);
                    } else if (alertBox != null) {
                        alertBox.setVisible(false);
                        alertBox.setManaged(false);
                    }
                });
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error loading agricole ERP stats", e);
            }
        });
        bg.setDaemon(true);
        bg.start();
    }

    // ── Navigation ──────────────────────────────────────────────────────────

    /** Back to the main agricole dashboard */
    @FXML
    private void handleBackToDashboard() {
        Stage stage = getStage();
        if (stage != null)
            NavigationUtil.navigateTo(stage, "views/agricole-dashboard.fxml", "Tableau de Bord Agricole");
    }

    /**
     * Catalogue matières — read-only view of all raw materials.
     * Reuses the existing erp-matieres view but the agriculteur cannot
     * add/edit/delete (those buttons are fournisseur-only in Symfony).
     * In JavaFX we navigate to the same view — the table is read-only by
     * default since the edit/delete buttons are inside the table cells and
     * the agriculteur can still see the stock levels.
     */
    @FXML
    private void handleCatalogueMatiere() {
        Stage stage = getStage();
        if (stage != null)
            NavigationUtil.navigateTo(stage, "views/erp-catalogue-matieres.fxml", "Catalogue Matières");
    }

    /** Produits finis — full CRUD + production for agriculteur */
    @FXML
    private void handleProduits() {
        Stage stage = getStage();
        if (stage != null)
            NavigationUtil.navigateTo(stage, "views/erp-agricole-produits.fxml", "Mes Produits Finis");
    }

    /** Achats — purchase orders for agriculteur */
    @FXML
    private void handleAchats() {
        Stage stage = getStage();
        if (stage != null)
            NavigationUtil.navigateTo(stage, "views/erp-agricole-achats.fxml", "Mes Achats");
    }

    @FXML
    private void handleLogout() {
        Stage stage = getStage();
        if (stage != null) NavigationUtil.logout(stage);
    }

    @FXML
    private void handleProfile() {
        boolean updated = ProfileManager.showProfileEditDialog(getStage());
        if (updated) {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            ProfileManager.updateProfileUI(currentUser, welcomeLabel, userNameLabel, sidebarAvatar, sidebarAvatarText);
            if (headerProfileImage != null)
                AvatarUtil.loadUserImageIntoImageView(headerProfileImage, currentUser, 36);
        }
    }

    private Stage getStage() {
        if (userNameLabel != null && userNameLabel.getScene() != null)
            return (Stage) userNameLabel.getScene().getWindow();
        if (headerProfileImage != null && headerProfileImage.getScene() != null)
            return (Stage) headerProfileImage.getScene().getWindow();
        return null;
    }
}
