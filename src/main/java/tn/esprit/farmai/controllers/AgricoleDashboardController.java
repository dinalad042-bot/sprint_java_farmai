package tn.esprit.farmai.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.AnalyseService;
import tn.esprit.farmai.services.ConseilService;
import tn.esprit.farmai.services.FermeService;
import tn.esprit.farmai.services.NotificationService;
import tn.esprit.farmai.utils.NavigationUtil;
import tn.esprit.farmai.utils.ProfileManager;
import tn.esprit.farmai.utils.SessionManager;
import javafx.scene.image.ImageView;
import tn.esprit.farmai.utils.AvatarUtil;
import javafx.application.Platform;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Agricole Dashboard.
 * Provides navigation to analysis consultation for fermier users.
 * Loads dynamic statistics from Ferme, Analyse and Conseil services.
 */
public class AgricoleDashboardController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(AgricoleDashboardController.class.getName());

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
    private Label totalFermesLabel;

    @FXML
    private Label totalAnalysesLabel;

    @FXML
    private Label totalConseilsLabel;

    // Notification badge elements
    @FXML
    private Circle notificationBadge;
    @FXML
    private Label notificationCountLabel;

    private final FermeService fermeService;
    private final AnalyseService analyseService;
    private final ConseilService conseilService;

    public AgricoleDashboardController() {
        this.fermeService = new FermeService();
        this.analyseService = new AnalyseService();
        this.conseilService = new ConseilService();
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
                // Update header avatar on profile change
                if (headerProfileImage != null) {
                    AvatarUtil.loadUserImageIntoImageView(headerProfileImage, newUser, 36);
                }
            }
        });

        // Load dynamic statistics
        loadStatistics();

        // Check for unread notifications
        checkNotifications();
    }

    /**
     * Check for unread notifications and update badge + show alert
     */
    private void checkNotifications() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        try {
            NotificationService notificationService = new NotificationService();
            int unreadCount = notificationService.countUnreadByUser(currentUser.getIdUser());

            // Update notification badge
            final int count = unreadCount;
            Platform.runLater(() -> {
                if (count > 0) {
                    notificationBadge.setVisible(true);
                    notificationCountLabel.setVisible(true);
                    notificationCountLabel.setText(String.valueOf(count));
                } else {
                    notificationBadge.setVisible(false);
                    notificationCountLabel.setVisible(false);
                }
            });

            if (unreadCount > 0) {
                LOGGER.log(Level.INFO, "User {0} has {1} unread notifications",
                        new Object[] { currentUser.getFullName(), unreadCount });
            }

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to check notifications for user " + currentUser.getIdUser(), e);
        }
    }

    /**
     * Handle notification bell click - show notifications popup
     */
    @FXML
    private void handleNotifications() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        try {
            NotificationService notificationService = new NotificationService();
            int unreadCount = notificationService.countUnreadByUser(currentUser.getIdUser());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Notifications");
            alert.setHeaderText("📬 Vos Notifications");

            if (unreadCount == 0) {
                alert.setContentText("Aucune nouvelle notification.");
            } else if (unreadCount == 1) {
                alert.setContentText("Vous avez 1 notification non lue.\n\n" +
                        "Rendez-vous dans 'Analyse IA' pour consulter vos nouvelles analyses.");
            } else {
                alert.setContentText("Vous avez " + unreadCount + " notifications non lues.\n\n" +
                        "Rendez-vous dans 'Analyse IA' pour consulter vos nouvelles analyses.");
            }

            alert.showAndWait();

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to load notifications", e);
        }
    }

    /**
     * Load real statistics from database for the agricole user
     */
    private void loadStatistics() {
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                setDefaultStatistics();
                return;
            }
            int userId = currentUser.getIdUser();

            // Get user's own ferme IDs
            List<Integer> userFermeIds = fermeService.getFermeIdsByFermier(userId);

            // Total fermes for this user
            int totalFermes = userFermeIds.size();
            if (totalFermesLabel != null) {
                totalFermesLabel.setText(String.valueOf(totalFermes));
            }

            // Total analyses for user's farms only
            int totalAnalyses = 0;
            if (!userFermeIds.isEmpty()) {
                totalAnalyses = analyseService.findByFermes(userFermeIds).size();
            }
            if (totalAnalysesLabel != null) {
                totalAnalysesLabel.setText(String.valueOf(totalAnalyses));
            }

            // Total conseils (still global - would need more complex filtering)
            int totalConseils = conseilService.selectALL().size();
            if (totalConseilsLabel != null) {
                totalConseilsLabel.setText(String.valueOf(totalConseils));
            }

            LOGGER.log(Level.INFO, "Agricole Statistics loaded: {0} fermes, {1} analyses, {2} conseils",
                    new Object[] { totalFermes, totalAnalyses, totalConseils });

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading agricole statistics", e);
            setDefaultStatistics();
        }
    }

    private void setDefaultStatistics() {
        if (totalFermesLabel != null)
            totalFermesLabel.setText("-");
        if (totalAnalysesLabel != null)
            totalAnalysesLabel.setText("-");
        if (totalConseilsLabel != null)
            totalConseilsLabel.setText("-");
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
     * Handle my farms - Navigate to farm management
     */
    @FXML
    private void handleMyFarms() {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/tn/esprit/farmai/views/gestion-fermes.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1200, 800);
            stage.setScene(scene);
            stage.setTitle("FarmAI - Mes Exploitations");
            stage.show();
            LOGGER.log(Level.INFO, "Navigated to farm management");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to farm management", e);
            NavigationUtil.showError("Erreur", "Impossible d'ouvrir la gestion des exploitations.");
        }
    }

    /**
     * Handle my crops - Navigate to Mes Cultures hub with 3 buttons
     */
    @FXML
    private void handleMyCrops() {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/tn/esprit/farmai/views/mes-cultures.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1200, 800);

            // Apply CSS
            java.net.URL cssUrl = getClass().getResource("/tn/esprit/farmai/styles/dashboard.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("FarmAI - Mes Cultures");
            stage.show();
            LOGGER.log(Level.INFO, "Navigated to mes cultures hub");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to mes cultures hub", e);
            NavigationUtil.showError("Erreur", "Impossible d'ouvrir la gestion des cultures.");
        }
    }

    /**
     * Handle my animals - Navigate to animal management
     */
    @FXML
    private void handleMyAnimals() {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/tn/esprit/farmai/views/gestion-animaux.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1200, 800);
            stage.setScene(scene);
            stage.setTitle("FarmAI - Mes Animaux");
            stage.show();
            LOGGER.log(Level.INFO, "Navigated to animal management");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to animal management", e);
            NavigationUtil.showError("Erreur", "Impossible d'ouvrir la gestion des animaux.");
        }
    }

    /**
     * Handle orders - Future feature
     */
    @FXML
    private void handleOrders() {
        NavigationUtil.showSuccess("Commandes", "Module des commandes à venir.");
    }

    /**
     * Handle AI analysis (Consultation Fermier)
     * Navigates to the fermier analyses view where the user can see expert
     * analyses.
     */
    @FXML
    private void handleAIAnalysis() {
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                LOGGER.log(Level.WARNING, "User session not found when trying to access analyses");
                NavigationUtil.showError("Session expirée", "Veuillez vous reconnecter pour accéder aux analyses.");
                return;
            }

            LOGGER.log(Level.INFO, "User {0} navigating to analyses view", currentUser.getFullName());

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            javafx.scene.Parent currentRoot = welcomeLabel.getScene().getRoot();

            // Fade out animation
            javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(200), currentRoot);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(event -> {
                try {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                            getClass().getResource("/tn/esprit/farmai/views/fermier-analyses.fxml"));
                    javafx.scene.Parent newRoot = loader.load();
                    javafx.scene.Scene scene = new javafx.scene.Scene(newRoot, 1200, 800);

                    newRoot.setOpacity(0.0);
                    stage.setScene(scene);

                    // Fade in animation
                    javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                            javafx.util.Duration.millis(250), newRoot);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();

                    stage.show();
                    LOGGER.log(Level.INFO, "Successfully navigated to analyses view");
                } catch (java.io.IOException e) {
                    LOGGER.log(Level.SEVERE, "Failed to load fermier-analyses.fxml", e);
                    NavigationUtil.showError("Erreur de chargement",
                            "Impossible de charger la vue des analyses.\n" +
                                    "Veuillez réessayer ou contacter le support.");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Unexpected error loading analyses view", e);
                    NavigationUtil.showError("Erreur",
                            "Une erreur inattendue s'est produite: " + e.getMessage());
                }
            });

            fadeOut.play();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during navigation to analyses", e);
            NavigationUtil.showError("Erreur de navigation",
                    "Impossible de naviguer vers les analyses: " + e.getMessage());
        }
    }

    /**
     * Handle expert analyses - Navigate to agricole statistics view (read-only)
     * This shows statistics without expert editing capabilities
     */
    @FXML
    private void handleExpertAnalyses() {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/tn/esprit/farmai/views/agricole-statistics.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1200, 800);

            // Apply CSS
            java.net.URL cssUrl = getClass().getResource("/tn/esprit/farmai/styles/dashboard.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("FarmAI - Statistiques");
            stage.show();
            LOGGER.log(Level.INFO, "Navigated to agricole statistics view");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to statistics", e);
            NavigationUtil.showError("Erreur", "Impossible d'ouvrir les statistiques.");
        }
    }

    /**
     * Handle add face - Open Face Recognition view for face enrollment
     */
    @FXML
    private void handleAddFace() {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/tn/esprit/farmai/views/face-recognition-view.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.scene.Scene scene = new javafx.scene.Scene(root, 800, 600);
            String cssPath = getClass().getResource("/tn/esprit/farmai/styles/dashboard.css") != null
                    ? getClass().getResource("/tn/esprit/farmai/styles/dashboard.css").toExternalForm()
                    : null;
            if (cssPath != null) {
                scene.getStylesheets().add(cssPath);
            }

            Stage faceStage = new Stage();
            faceStage.initOwner(stage);
            faceStage.setTitle("FarmAI - Enregistrement Visage");
            faceStage.setScene(scene);

            // Cleanup camera when window closes
            FaceRecognitionController controller = loader.getController();
            faceStage.setOnCloseRequest(e -> controller.cleanup());

            faceStage.show();
        } catch (Exception e) {
            NavigationUtil.showError("Erreur", "Impossible d'ouvrir la reconnaissance faciale: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
