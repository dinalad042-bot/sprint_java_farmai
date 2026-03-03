package tn.esprit.farmai.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tn.esprit.farmai.models.Analyse;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.AnalyseService;
import tn.esprit.farmai.utils.NavigationUtil;
import tn.esprit.farmai.utils.ProfileManager;
import tn.esprit.farmai.utils.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Fermier Analyses view.
 * Displays analyses performed by experts on the farmer's farms.
 */
public class FermierAnalysesController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(FermierAnalysesController.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userRoleLabel;

    @FXML
    private Circle sidebarAvatar;

    @FXML
    private Text sidebarAvatarText;

    @FXML
    private Circle headerAvatar;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Analyse> analysesTableView;

    @FXML
    private TableColumn<Analyse, String> colId;

    @FXML
    private TableColumn<Analyse, String> colDate;

    @FXML
    private TableColumn<Analyse, String> colResultat;

    @FXML
    private TableColumn<Analyse, String> colTechnicien;

    @FXML
    private TableColumn<Analyse, String> colImage;

    @FXML
    private TableColumn<Analyse, Void> colActions;

    private final AnalyseService analyseService;
    private ObservableList<Analyse> analysesList;

    public FermierAnalysesController() {
        this.analyseService = new AnalyseService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize user profile
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            ProfileManager.updateProfileUI(currentUser, null, userNameLabel, sidebarAvatar, sidebarAvatarText);
            if (userRoleLabel != null) {
                userRoleLabel.setText(currentUser.getRole().getDisplayName());
            }
        }

        // Auto-refresh sidebar when user profile changes
        SessionManager.getInstance().currentUserProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser != null) {
                javafx.application.Platform.runLater(() -> 
                    ProfileManager.updateProfileUI(newUser, null, userNameLabel, sidebarAvatar, sidebarAvatarText));
            }
        });

        // Setup table columns
        setupTableColumns();

        // Load analyses data
        loadAnalyses();

        // Setup search functionality
        setupSearch();
    }

    /**
     * Configure table columns
     */
    private void setupTableColumns() {
        colId.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getIdAnalyse())));

        colDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateAnalyse() != null) {
                return new SimpleStringProperty(
                    cellData.getValue().getDateAnalyse().format(DATE_FORMATTER));
            }
            return new SimpleStringProperty("-");
        });

        colResultat.setCellValueFactory(cellData -> {
            String resultat = cellData.getValue().getResultatTechnique();
            if (resultat != null && resultat.length() > 50) {
                resultat = resultat.substring(0, 50) + "...";
            }
            return new SimpleStringProperty(resultat != null ? resultat : "-");
        });

        // Use idTechnicien (int) instead of technicien name
        colTechnicien.setCellValueFactory(cellData -> 
            new SimpleStringProperty("Tech #" + cellData.getValue().getIdTechnicien()));

        // Image column with thumbnail
        colImage.setCellFactory(column -> new TableCell<Analyse, String>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText("-");
                } else {
                    Analyse analyse = getTableRow().getItem();
                    String imageUrl = analyse.getImageUrl();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        try {
                            Image image = new Image(imageUrl, 50, 50, true, true, true);
                            imageView.setImage(image);
                            setGraphic(imageView);
                            setText(null);
                        } catch (Exception e) {
                            setText("🖼️");
                            setGraphic(null);
                        }
                    } else {
                        setText("-");
                        setGraphic(null);
                    }
                }
            }
        });

        // Actions column with view button
        colActions.setCellFactory(column -> new TableCell<Analyse, Void>() {
            private final Button viewButton = new Button("👁️ Voir");
            {
                viewButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 11px;");
                viewButton.setOnAction(event -> {
                    Analyse analyse = getTableRow().getItem();
                    if (analyse != null) {
                        showAnalyseDetails(analyse);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });
    }

    /**
     * Load analyses from database
     */
    private void loadAnalyses() {
        try {
            analysesList = FXCollections.observableArrayList(analyseService.selectALL());
            analysesTableView.setItems(analysesList);
            LOGGER.log(Level.INFO, "Loaded {0} analyses", analysesList.size());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load analyses", e);
            NavigationUtil.showError("Erreur", "Impossible de charger les analyses: " + e.getMessage());
        }
    }

    /**
     * Setup search functionality
     */
    private void setupSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterAnalyses(newValue);
            });
        }
    }

    /**
     * Filter analyses based on search text
     */
    private void filterAnalyses(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            analysesTableView.setItems(analysesList);
        } else {
            String lowerCaseFilter = searchText.toLowerCase();
            ObservableList<Analyse> filteredList = analysesList.filtered(analyse ->
                String.valueOf(analyse.getIdAnalyse()).contains(lowerCaseFilter) ||
                (analyse.getResultatTechnique() != null && 
                 analyse.getResultatTechnique().toLowerCase().contains(lowerCaseFilter)) ||
                String.valueOf(analyse.getIdTechnicien()).contains(lowerCaseFilter)
            );
            analysesTableView.setItems(filteredList);
        }
    }

    /**
     * Show analysis details in an alert dialog
     */
    private void showAnalyseDetails(Analyse analyse) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de l'Analyse");
        alert.setHeaderText("Analyse #" + analyse.getIdAnalyse());
        
        StringBuilder content = new StringBuilder();
        content.append("Date: ").append(
            analyse.getDateAnalyse() != null ? 
            analyse.getDateAnalyse().format(DATE_FORMATTER) : "-"
        ).append("\n\n");
        content.append("Technicien ID: ").append(analyse.getIdTechnicien()).append("\n\n");
        content.append("Ferme ID: ").append(analyse.getIdFerme()).append("\n\n");
        content.append("Résultat Technique:\n");
        content.append(analyse.getResultatTechnique() != null ? 
            analyse.getResultatTechnique() : "Aucun résultat disponible");

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    /**
     * Handle back button - return to Agricole Dashboard
     */
    @FXML
    private void handleBack() {
        navigateToDashboard();
    }

    /**
     * Navigate to Mes Cultures
     */
    @FXML
    private void handleMesCultures() {
        try {
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/tn/esprit/farmai/views/mes-cultures.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 800);

            // Apply CSS
            java.net.URL cssUrl = getClass().getResource("/tn/esprit/farmai/styles/dashboard.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("FarmAI - Mes Cultures");
            stage.show();
            LOGGER.log(Level.INFO, "Navigated to mes cultures");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to mes cultures", e);
            NavigationUtil.showError("Erreur", "Impossible d'ouvrir la gestion des cultures.");
        }
    }

    /**
     * Handle AI Analysis - Refresh current view
     */
    @FXML
    private void handleAIAnalysis() {
        loadAnalyses();
        NavigationUtil.showSuccess("Actualisation", "La liste des analyses a été actualisée.");
    }

    /**
     * Handle Add Face - Open Face Recognition view
     */
    @FXML
    private void handleAddFace() {
        try {
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/tn/esprit/farmai/views/face-recognition-view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 800, 600);
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
            LOGGER.log(Level.SEVERE, "Failed to open face recognition", e);
            NavigationUtil.showError("Erreur", "Impossible d'ouvrir la reconnaissance faciale: " + e.getMessage());
        }
    }

    /**
     * Handle profile click
     */
    @FXML
    private void handleProfile() {
        boolean updated = ProfileManager.showProfileEditDialog(userNameLabel.getScene().getWindow());
        if (updated) {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            ProfileManager.updateProfileUI(currentUser, null, userNameLabel, sidebarAvatar, sidebarAvatarText);
        }
    }

    /**
     * Handle logout
     */
    @FXML
    private void handleLogout() {
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        NavigationUtil.logout(stage);
    }

    /**
     * Navigate to dashboard helper method
     */
    private void navigateToDashboard() {
        try {
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/tn/esprit/farmai/views/agricole-dashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 800);

            // Apply CSS
            java.net.URL cssUrl = getClass().getResource("/tn/esprit/farmai/styles/dashboard.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("FarmAI - Tableau de Bord");
            stage.show();
            LOGGER.log(Level.INFO, "Navigated to dashboard");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to dashboard", e);
            NavigationUtil.showError("Erreur", "Impossible de retourner au tableau de bord.");
        }
    }
}
