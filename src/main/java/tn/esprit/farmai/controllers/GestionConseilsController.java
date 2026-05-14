
package tn.esprit.farmai.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tn.esprit.farmai.models.Conseil;
import tn.esprit.farmai.models.Priorite;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.AnalyseService;
import tn.esprit.farmai.services.ConseilService;
import tn.esprit.farmai.services.IntelligentReportService;
import tn.esprit.farmai.utils.AlertUtils;
import tn.esprit.farmai.utils.NavigationUtil;
import tn.esprit.farmai.utils.ProfileManager;
import tn.esprit.farmai.utils.SessionManager;
import tn.esprit.farmai.utils.SpeechUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for managing Conseils (Expert recommendations).
 * Displays conseils in a TableView with CRUD operations.
 * US5 & US7: Manages 1:N relationship with Analyse.
 */
public class GestionConseilsController implements Initializable {

    @FXML
    private TableView<Conseil> conseilsTableView;
    @FXML
    private TableColumn<Conseil, Integer> colId;
    @FXML
    private TableColumn<Conseil, String> colDescription;
    @FXML
    private TableColumn<Conseil, Priorite> colPriorite;
    @FXML
    private TableColumn<Conseil, Integer> colAnalyseId;
    @FXML
    private TableColumn<Conseil, Void> colActions;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<Priorite> prioriteFilterComboBox;
    @FXML
    private Button refreshButton;
    @FXML
    private Button addConseilButton;
    @FXML
    private Button backButton;
    @FXML
    private Label totalConseilsLabel;

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userRoleLabel;
    @FXML
    private Circle userAvatarCircle;
    @FXML
    private Text defaultAvatarText;
    @FXML
    private Circle headerAvatarCircle;

    private final ConseilService conseilService;
    private final AnalyseService analyseService;
    private ObservableList<Conseil> conseilsList;

    public GestionConseilsController() {
        this.conseilService = new ConseilService();
        this.analyseService = new AnalyseService();
        this.conseilsList = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableView();
        setupSearch();
        setupFilters();
        loadConseils();
        setupDoubleClick();
        initializeUserData();
    }

    /**
     * Initialize user data from SessionManager - ensures consistent avatar
     * and user info display across all pages.
     */
    private void initializeUserData() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Update sidebar labels
            if (welcomeLabel != null) {
                welcomeLabel.setText(currentUser.getFullName());
            }
            if (userNameLabel != null) {
                userNameLabel.setText(currentUser.getFullName());
            }
            if (userRoleLabel != null) {
                userRoleLabel.setText(ProfileManager.getStandardizedRoleLabel(currentUser));
            }

            // Load avatar images using ProfileManager
            ProfileManager.loadUserImageIntoCircle(userAvatarCircle, currentUser);
            ProfileManager.loadUserImageIntoCircle(headerAvatarCircle, currentUser);

            // Show default avatar text if no image loaded
            if (defaultAvatarText != null) {
                boolean hasImage = currentUser.getImageUrl() != null && !currentUser.getImageUrl().isEmpty();
                defaultAvatarText.setVisible(!hasImage);
            }
        }

        // Listen for profile changes (avatar updates, etc.)
        SessionManager.getInstance().currentUserProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser != null) {
                Platform.runLater(() -> {
                    if (welcomeLabel != null) {
                        welcomeLabel.setText(newUser.getFullName());
                    }
                    if (userNameLabel != null) {
                        userNameLabel.setText(newUser.getFullName());
                    }
                    if (userRoleLabel != null) {
                        userRoleLabel.setText(ProfileManager.getStandardizedRoleLabel(newUser));
                    }
                    ProfileManager.loadUserImageIntoCircle(userAvatarCircle, newUser);
                    ProfileManager.loadUserImageIntoCircle(headerAvatarCircle, newUser);
                    if (defaultAvatarText != null) {
                        boolean hasImage = newUser.getImageUrl() != null && !newUser.getImageUrl().isEmpty();
                        defaultAvatarText.setVisible(!hasImage);
                    }
                });
            }
        });
    }

    /**
     * Setup TableView columns with CellValueFactory bindings
     */
    private void setupTableView() {
        // ID Column
        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getIdConseil()));
        colId.setCellFactory(tc -> new TableCell<Conseil, Integer>() {
            private final javafx.scene.text.Text text = new javafx.scene.text.Text();
            {
                text.setStyle("-fx-fill: #000000;");
                setGraphic(text);
            }

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    text.setText("");
                } else {
                    text.setText(String.valueOf(item));
                }
            }
        });

        // Description Column with text wrapping
        colDescription.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDescriptionConseil()));
        colDescription.setCellFactory(tc -> {
            TableCell<Conseil, String> cell = new TableCell<>();
            javafx.scene.text.Text text = new javafx.scene.text.Text();
            cell.setGraphic(text);
            cell.setPrefHeight(javafx.scene.control.Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(colDescription.widthProperty().subtract(10));
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });

        // Priorite Column with color indicator
        colPriorite.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getPriorite()));
        colPriorite.setCellFactory(column -> new TableCell<Conseil, Priorite>() {
            private final javafx.scene.text.Text text = new javafx.scene.text.Text();
            {
                setGraphic(text);
            }

            @Override
            protected void updateItem(Priorite priorite, boolean empty) {
                super.updateItem(priorite, empty);
                if (empty || priorite == null) {
                    text.setText("");
                    setStyle("");
                } else {
                    String emoji = switch (priorite) {
                        case BASSE -> "🟢";
                        case MOYENNE -> "🟡";
                        case HAUTE -> "🔴";
                    };
                    text.setText(emoji + " " + priorite.getLabel());
                }
            }
        });

        // Analyse ID Column (FK) - Shows the related analysis
        colAnalyseId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getIdAnalyse()));
        colAnalyseId.setCellFactory(tc -> new TableCell<Conseil, Integer>() {
            private final javafx.scene.text.Text text = new javafx.scene.text.Text();
            {
                text.setStyle("-fx-fill: #000000;");
                setGraphic(text);
            }

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    text.setText("");
                } else {
                    text.setText(String.valueOf(item));
                }
            }
        });

        // Actions Column (Edit/Delete/Read Aloud - TTS)
        colActions.setCellFactory(param -> new TableCell<Conseil, Void>() {
            private final Button editBtn = new Button("✎");
            private final Button deleteBtn = new Button("🗑");
            private final Button ttsBtn = new Button("🔊");
            private final HBox pane = new HBox(5, ttsBtn, editBtn, deleteBtn);

            {
                pane.setAlignment(Pos.CENTER);
                editBtn.getStyleClass().add("action-btn");
                deleteBtn.getStyleClass().add("danger-btn");
                editBtn.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-padding: 5px 10px;");
                deleteBtn.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-padding: 5px 10px;");
                ttsBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 5px 10px;");
                
                // Add tooltips
                ttsBtn.setTooltip(new Tooltip("Lire à haute voix"));
                editBtn.setTooltip(new Tooltip("Modifier le conseil"));
                deleteBtn.setTooltip(new Tooltip("Supprimer"));

                editBtn.setOnAction(event -> {
                    Conseil conseil = getTableView().getItems().get(getIndex());
                    handleEditConseil(conseil);
                });

                deleteBtn.setOnAction(event -> {
                    Conseil conseil = getTableView().getItems().get(getIndex());
                    handleDeleteConseil(conseil);
                });

                // TTS: Read conseil aloud
                ttsBtn.setOnAction(event -> {
                    Conseil conseil = getTableView().getItems().get(getIndex());
                    handleReadAloud(conseil, ttsBtn);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    /**
     * Setup double-click on table row to edit
     */
    private void setupDoubleClick() {
        conseilsTableView.setRowFactory(tv -> {
            TableRow<Conseil> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Conseil conseil = row.getItem();
                    handleEditConseil(conseil);
                }
            });
            return row;
        });
    }

    /**
     * Setup search functionality
     */
    private void setupSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterConseils());
        }
    }

    /**
     * Setup priority filter ComboBox
     */
    private void setupFilters() {
        if (prioriteFilterComboBox != null) {
            prioriteFilterComboBox.setItems(FXCollections.observableArrayList(Priorite.values()));
            prioriteFilterComboBox.setPromptText("Toutes les priorités");
            prioriteFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> filterConseils());
        }
    }

    /**
     * Load all conseils from database into TableView
     */
    @FXML
    private void loadConseils() {
        try {
            conseilsList.clear();
            conseilsList.addAll(conseilService.selectALL());
            conseilsTableView.setItems(conseilsList);
            updateTotalLabel();
        } catch (SQLException e) {
            NavigationUtil.showError("Erreur",
                    "Impossible de charger les conseils: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Filter conseils based on search text and priority filter
     */
    private void filterConseils() {
        String searchText = searchField != null ? searchField.getText().toLowerCase() : "";
        Priorite selectedPriorite = prioriteFilterComboBox != null ? prioriteFilterComboBox.getValue() : null;

        ObservableList<Conseil> filteredList = FXCollections.observableArrayList();

        for (Conseil conseil : conseilsList) {
            boolean matchesSearch = searchText.isEmpty() ||
                    conseil.getDescriptionConseil().toLowerCase().contains(searchText) ||
                    String.valueOf(conseil.getIdConseil()).contains(searchText) ||
                    String.valueOf(conseil.getIdAnalyse()).contains(searchText);

            boolean matchesPriorite = selectedPriorite == null ||
                    conseil.getPriorite() == selectedPriorite;

            if (matchesSearch && matchesPriorite) {
                filteredList.add(conseil);
            }
        }

        conseilsTableView.setItems(filteredList);
        updateTotalLabel();
    }

    /**
     * Handle edit conseil
     */
    private void handleEditConseil(Conseil conseil) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/tn/esprit/farmai/views/ajout-conseil.fxml"));
            Parent root = loader.load();

            // Get controller and set edit mode
            AjoutConseilController controller = loader.getController();
            controller.setEditMode(conseil);

            Stage stage = new Stage();
            stage.setTitle("FarmAI - Modifier un Conseil");
            stage.setScene(new Scene(root, 650, 750));
            stage.setResizable(false);

            // Refresh after close
            stage.setOnHiding(event -> loadConseils());
            stage.show();

        } catch (Exception e) {
            NavigationUtil.showError("Erreur",
                    "Impossible d'ouvrir l'édition: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle delete conseil
     */
    private void handleDeleteConseil(Conseil conseil) {
        Optional<ButtonType> result = NavigationUtil.showConfirmation(
                "Confirmer la suppression",
                "Êtes-vous sûr de vouloir supprimer le conseil ID " + conseil.getIdConseil() + " ?");

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                conseilService.deleteOne(conseil);
                conseilsList.remove(conseil);
                updateTotalLabel();
                NavigationUtil.showSuccess("Succès", "Conseil supprimé avec succès.");
            } catch (SQLException e) {
                NavigationUtil.showError("Erreur",
                        "Impossible de supprimer le conseil: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle refresh button
     */
    @FXML
    private void handleRefresh() {
        loadConseils();
        if (prioriteFilterComboBox != null) {
            prioriteFilterComboBox.setValue(null);
        }
        if (searchField != null) {
            searchField.clear();
        }
    }

    /**
     * Handle back button - return to Expert Dashboard with fade transition
     */
    @FXML
    private void handleBack() {
        navigateWithFade("/tn/esprit/farmai/views/expert-dashboard.fxml",
                "FarmAI - Tableau de Bord Expert");
    }

    /**
     * Handle dashboard navigation from sidebar
     */
    @FXML
    private void handleDashboard() {
        navigateWithFade("/tn/esprit/farmai/views/expert-dashboard.fxml",
                "FarmAI - Tableau de Bord Expert");
    }

    /**
     * Handle analyses navigation from sidebar
     */
    @FXML
    private void handleAnalyses() {
        navigateWithFade("/tn/esprit/farmai/views/gestion-analyses.fxml",
                "FarmAI - Gestion des Analyses");
    }

    /**
     * Handle statistics navigation from sidebar
     */
    @FXML
    private void handleStatistics() {
        navigateWithFade("/tn/esprit/farmai/views/statistics.fxml",
                "FarmAI - Statistiques");
    }

    /**
     * Handle add face navigation from sidebar
     */
    @FXML
    private void handleAddFace() {
        try {
            Stage stage = (Stage) conseilsTableView.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/tn/esprit/farmai/views/face-recognition-view.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.scene.Scene scene = new javafx.scene.Scene(root, 800, 600);
            String cssPath = getClass().getResource("/tn/esprit/farmai/styles/main.css") != null
                    ? getClass().getResource("/tn/esprit/farmai/styles/main.css").toExternalForm()
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

    /**
     * NEW: Handle Generate Report - Generate intelligent analysis report with AI summary
     */
    @FXML
    private void handleGenerateReport() {
        // Show simple dialog to select farm ID
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog("1");
        dialog.setTitle("Generer Rapport");
        dialog.setHeaderText("Rapport Intelligent d'Analyse");
        dialog.setContentText("ID de la ferme:");

        dialog.showAndWait().ifPresent(farmIdStr -> {
            try {
                int farmId = Integer.parseInt(farmIdStr.trim());
                generateReportForFarm(farmId);
            } catch (NumberFormatException e) {
                NavigationUtil.showError("Erreur", "ID de ferme invalide.");
            }
        });
    }

    /**
     * Generate report for specified farm with progress updates
     */
    private void generateReportForFarm(int farmId) {
        // Show progress dialog with progress bar
        javafx.scene.control.Dialog<Void> progressDialog = new javafx.scene.control.Dialog<>();
        progressDialog.setTitle("Generation du Rapport");
        progressDialog.setHeaderText("Creation du rapport intelligent en cours...");

        // Progress bar
        javafx.scene.control.ProgressBar progressBar = new javafx.scene.control.ProgressBar(0);
        progressBar.setPrefWidth(300);
        progressBar.setStyle("-fx-accent: #4CAF50;");

        // Status label that shows current step
        javafx.scene.control.Label statusLabel = new javafx.scene.control.Label("Initialisation...");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

        // Percentage label
        javafx.scene.control.Label percentLabel = new javafx.scene.control.Label("0%");
        percentLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        javafx.scene.layout.VBox dialogContent = new javafx.scene.layout.VBox(15,
                statusLabel,
                progressBar,
                percentLabel);
        dialogContent.setAlignment(javafx.geometry.Pos.CENTER);
        dialogContent.setPadding(new javafx.geometry.Insets(25));

        progressDialog.getDialogPane().setContent(dialogContent);
        progressDialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CANCEL);

        // Progress callback to update UI
        IntelligentReportService.ProgressCallback progressCallback = (step, percent) -> {
            javafx.application.Platform.runLater(() -> {
                statusLabel.setText(step);
                progressBar.setProgress(percent / 100.0);
                percentLabel.setText(percent + "%");
            });
        };

        // Store result path for use after thread completes
        final String[] resultPath = new String[1];
        final IntelligentReportService.ReportException[] error = new IntelligentReportService.ReportException[1];

        // Run report generation in background
        Thread reportThread = new Thread(() -> {
            try {
                IntelligentReportService reportService = new IntelligentReportService();
                resultPath[0] = reportService.generateFarmReport(farmId, null, null, progressCallback);

                javafx.application.Platform.runLater(() -> {
                    progressDialog.close();
                    showReportSuccessDialog(resultPath[0]);
                });

            } catch (IntelligentReportService.ReportException e) {
                error[0] = e;
                javafx.application.Platform.runLater(() -> {
                    progressDialog.close();
                    NavigationUtil.showError("Erreur de rapport", e.getMessage());
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    progressDialog.close();
                    NavigationUtil.showError("Erreur", "Une erreur s'est produite: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });

        reportThread.setDaemon(true);
        reportThread.start();

        progressDialog.showAndWait();
    }

    /**
     * Show report generation success dialog with clear file location and access buttons
     */
    private void showReportSuccessDialog(String pdfPath) {
        javafx.scene.control.Dialog<javafx.scene.control.ButtonType> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("✅ Rapport Genere avec Succes!");
        dialog.setHeaderText(null);
        dialog.setResizable(true);

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(15);
        content.setPadding(new javafx.geometry.Insets(25));
        content.setPrefWidth(550);
        content.setMinHeight(300);
        content.setStyle("-fx-background-color: #E8F5E9;");

        // Success message
        javafx.scene.control.Label successLabel = new javafx.scene.control.Label("📄 Votre rapport a ete genere!");
        successLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");

        // File location section
        javafx.scene.control.Label locationTitle = new javafx.scene.control.Label("📁 Emplacement du fichier:");
        locationTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        File pdfFile = new File(pdfPath);
        javafx.scene.control.TextField pathField = new javafx.scene.control.TextField(pdfPath);
        pathField.setEditable(false);
        pathField.setPrefWidth(480);
        pathField.setMinWidth(400);

        javafx.scene.control.Label filenameLabel = new javafx.scene.control.Label(
                "Nom: " + pdfFile.getName());
        filenameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

        // Action buttons
        javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(15);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        buttonBox.setPadding(new javafx.geometry.Insets(20, 0, 0, 0));

        // Open PDF button (primary)
        javafx.scene.control.Button openPdfBtn = new javafx.scene.control.Button("📖 Ouvrir le PDF");
        openPdfBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        openPdfBtn.setPrefSize(150, 40);
        openPdfBtn.setOnAction(e -> {
            try {
                java.awt.Desktop.getDesktop().open(pdfFile);
                dialog.close();
            } catch (IOException ex) {
                NavigationUtil.showError("Erreur", "Impossible d'ouvrir le PDF: " + ex.getMessage());
            }
        });

        // Open folder button
        javafx.scene.control.Button openFolderBtn = new javafx.scene.control.Button("📂 Ouvrir Dossier");
        openFolderBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        openFolderBtn.setPrefSize(130, 40);
        openFolderBtn.setOnAction(e -> {
            try {
                java.awt.Desktop.getDesktop().open(pdfFile.getParentFile());
            } catch (IOException ex) {
                NavigationUtil.showError("Erreur", "Impossible d'ouvrir le dossier: " + ex.getMessage());
            }
        });

        buttonBox.getChildren().addAll(openPdfBtn, openFolderBtn);

        content.getChildren().addAll(
                successLabel,
                new javafx.scene.control.Separator(),
                locationTitle,
                pathField,
                filenameLabel,
                new javafx.scene.control.Separator(),
                buttonBox);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(600, 400);
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.OK);

        dialog.showAndWait();
    }

    /**
     * Handle profile navigation from sidebar
     */
    @FXML
    private void handleProfile() {
        boolean updated = ProfileManager.showProfileEditDialog(conseilsTableView.getScene().getWindow());
        if (updated) {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (welcomeLabel != null) {
                welcomeLabel.setText(currentUser.getFullName());
            }
            if (userNameLabel != null) {
                userNameLabel.setText(currentUser.getFullName());
            }
            if (userRoleLabel != null) {
                userRoleLabel.setText(ProfileManager.getStandardizedRoleLabel(currentUser));
            }
            ProfileManager.loadUserImageIntoCircle(userAvatarCircle, currentUser);
            ProfileManager.loadUserImageIntoCircle(headerAvatarCircle, currentUser);
        }
    }

    /**
     * Handle logout from sidebar
     */
    @FXML
    private void handleLogout() {
        Stage stage = (Stage) conseilsTableView.getScene().getWindow();
        NavigationUtil.logout(stage);
    }

    /**
     * Navigate to a view with smooth fade transition
     */
    private void navigateWithFade(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) backButton.getScene().getWindow();
            Parent currentRoot = backButton.getScene().getRoot();

            // Fade out
            javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(200), currentRoot);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(event -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                    Parent newRoot = loader.load();
                    Scene scene = new Scene(newRoot, 1200, 800);

                    java.net.URL cssUrl = getClass().getResource("/tn/esprit/farmai/styles/main.css");
                    if (cssUrl != null) {
                        scene.getStylesheets().add(cssUrl.toExternalForm());
                    }

                    newRoot.setOpacity(0.0);
                    stage.setScene(scene);
                    stage.setTitle(title);

                    javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                            javafx.util.Duration.millis(250), newRoot);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();

                    stage.show();
                } catch (Exception e) {
                    NavigationUtil.showError("Erreur", "Impossible de charger la vue: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            fadeOut.play();
        } catch (Exception e) {
            NavigationUtil.showError("Erreur", "Erreur de navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle add conseil button
     */
    @FXML
    private void handleAddConseil() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/tn/esprit/farmai/views/ajout-conseil.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("FarmAI - Ajouter un Conseil");
            stage.setScene(new Scene(root, 650, 750));
            stage.setResizable(false);

            // Refresh after close
            stage.setOnHiding(event -> loadConseils());
            stage.show();

        } catch (Exception e) {
            NavigationUtil.showError("Erreur",
                    "Impossible d'ouvrir l'ajout de conseil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update total conseils label
     */
    private void updateTotalLabel() {
        if (totalConseilsLabel != null) {
            totalConseilsLabel.setText(
                    "Total: " + conseilsTableView.getItems().size() + " conseil(s)");
        }
    }

    /**
     * Handle TTS: Read conseil description aloud.
     * Uses SpeechUtils for async text-to-speech.
     * SRP: SpeechUtils handles TTS, this method handles UI state.
     * 
     * @param conseil The conseil to read aloud
     * @param ttsBtn  The button to update during playback
     */
    private void handleReadAloud(Conseil conseil, Button ttsBtn) {
        if (conseil == null || conseil.getDescriptionConseil() == null) {
            AlertUtils.showWarning("Aucun contenu", "Ce conseil n'a pas de description à lire.");
            return;
        }

        String description = conseil.getDescriptionConseil();

        // Check if TTS is already playing
        if (SpeechUtils.isPlaying()) {
            SpeechUtils.stop();
            ttsBtn.setText("🔊");
            ttsBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 5px 10px;");
            return;
        }

        // Update button state
        ttsBtn.setText("⏹");
        ttsBtn.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-padding: 5px 10px;");

        // Run TTS asynchronously to avoid UI freeze
        SpeechUtils.speakAsync(description)
                .thenRun(() -> {
                    // Reset button state when done
                    javafx.application.Platform.runLater(() -> {
                        ttsBtn.setText("🔊");
                        ttsBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 5px 10px;");
                    });
                })
                .exceptionally(ex -> {
                    // Handle TTS errors gracefully
                    javafx.application.Platform.runLater(() -> {
                        ttsBtn.setText("🔊");
                        ttsBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 5px 10px;");
                        AlertUtils.showError("Erreur TTS", "Impossible de lire le conseil: " + ex.getMessage());
                    });
                    return null;
                });

        // Show feedback to user
        AlertUtils.showToast("Lecture en cours...", 2000);
    }
}
