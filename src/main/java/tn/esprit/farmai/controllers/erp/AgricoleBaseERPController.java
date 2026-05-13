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
 * Base controller for all AGRICOLE ERP views.
 * Same as BaseERPController but navigation goes back to the agricole dashboard,
 * not the fournisseur dashboard.
 */
public abstract class AgricoleBaseERPController implements Initializable {

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
                userRoleLabel.setText("Production ERP");
        }
        onInit();
    }

    protected abstract void onInit();

    @FXML
    protected void handleBackToDashboard() {
        Stage stage = getStage();
        if (stage != null)
            NavigationUtil.navigateTo(stage, "views/agricole-dashboard.fxml", "Tableau de Bord Agricole");
    }

    @FXML
    protected void handleERPDashboard() {
        Stage stage = getStage();
        if (stage != null)
            NavigationUtil.navigateTo(stage, "views/erp-agricole-dashboard.fxml", "Production ERP");
    }

    @FXML
    protected void handleCatalogueMatiere() {
        Stage stage = getStage();
        if (stage != null)
            NavigationUtil.navigateTo(stage, "views/erp-catalogue-matieres.fxml", "Catalogue Matières");
    }

    @FXML
    protected void handleProduits() {
        Stage stage = getStage();
        if (stage != null)
            NavigationUtil.navigateTo(stage, "views/erp-agricole-produits.fxml", "Mes Produits Finis");
    }

    @FXML
    protected void handleAchats() {
        Stage stage = getStage();
        if (stage != null)
            NavigationUtil.navigateTo(stage, "views/erp-agricole-achats.fxml", "Mes Achats");
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
