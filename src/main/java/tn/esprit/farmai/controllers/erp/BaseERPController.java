package tn.esprit.farmai.controllers.erp;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.utils.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Base controller shared by all ERP views.
 * Handles sidebar profile, navigation, and logout.
 */
public abstract class BaseERPController implements Initializable {

    @FXML protected Label welcomeLabel;
    @FXML protected Label userNameLabel;
    @FXML protected Label userRoleLabel;
    @FXML protected Circle sidebarAvatar;
    @FXML protected Text sidebarAvatarText;
    @FXML protected ImageView headerProfileImage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (welcomeLabel != null)
                ProfileManager.updateProfileUI(currentUser, welcomeLabel, userNameLabel, sidebarAvatar, sidebarAvatarText);
            else
                ProfileManager.updateProfileUI(currentUser, null, userNameLabel, sidebarAvatar, sidebarAvatarText);

            if (headerProfileImage != null)
                AvatarUtil.loadUserImageIntoImageView(headerProfileImage, currentUser, 36);
            if (userRoleLabel != null)
                userRoleLabel.setText("ERP");
        }
        onInit();
    }

    /** Subclasses implement their own initialization here */
    protected abstract void onInit();

    @FXML
    protected void handleDashboard() {
        Stage stage = getStage();
        if (stage != null) NavigationUtil.navigateTo(stage, "views/fournisseur-dashboard.fxml", "Tableau de Bord Fournisseur");
    }

    @FXML
    protected void handleMatieres() {
        Stage stage = getStage();
        if (stage != null) NavigationUtil.navigateTo(stage, "views/erp-matieres.fxml", "Matières Premières");
    }

    @FXML
    protected void handleProduits() {
        Stage stage = getStage();
        if (stage != null) NavigationUtil.navigateTo(stage, "views/erp-produits.fxml", "Produits");
    }

    @FXML
    protected void handleServices() {
        Stage stage = getStage();
        if (stage != null) NavigationUtil.navigateTo(stage, "views/erp-services.fxml", "Services");
    }

    @FXML
    protected void handleAchats() {
        Stage stage = getStage();
        if (stage != null) NavigationUtil.navigateTo(stage, "views/erp-achats.fxml", "Achats");
    }

    @FXML
    protected void handleVentes() {
        Stage stage = getStage();
        if (stage != null) NavigationUtil.navigateTo(stage, "views/erp-ventes.fxml", "Ventes");
    }

    @FXML
    protected void handleLogout() {
        Stage stage = getStage();
        if (stage != null) NavigationUtil.logout(stage);
    }

    @FXML
    protected void handleProfile() {
        boolean updated = ProfileManager.showProfileEditDialog(getStage());
        if (updated) {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (welcomeLabel != null)
                ProfileManager.updateProfileUI(currentUser, welcomeLabel, userNameLabel, sidebarAvatar, sidebarAvatarText);
            else
                ProfileManager.updateProfileUI(currentUser, null, userNameLabel, sidebarAvatar, sidebarAvatarText);
            if (headerProfileImage != null)
                AvatarUtil.loadUserImageIntoImageView(headerProfileImage, currentUser, 36);
        }
    }

    protected Stage getStage() {
        if (userNameLabel != null && userNameLabel.getScene() != null)
            return (Stage) userNameLabel.getScene().getWindow();
        if (headerProfileImage != null && headerProfileImage.getScene() != null)
            return (Stage) headerProfileImage.getScene().getWindow();
        return null;
    }
}
