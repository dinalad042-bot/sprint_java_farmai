package tn.esprit.farmai.controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.AnalyseService;
import tn.esprit.farmai.services.ConseilService;
import tn.esprit.farmai.services.FermeService;
import tn.esprit.farmai.services.IntelligentReportService;
import tn.esprit.farmai.utils.NavigationUtil;
import tn.esprit.farmai.utils.ProfileManager;
import tn.esprit.farmai.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Expert Dashboard.
 * Loads dynamic statistics from Analyse and Conseil services.
 */
public class ExpertDashboardController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ExpertDashboardController.class.getName());

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

    // Dynamic statistics labels
    @FXML
    private Label totalAnalysesLabel;

    @FXML
    private Label totalConseilsLabel;

    @FXML
    private Label totalFermesLabel;

    private final AnalyseService analyseService;
    private final ConseilService conseilService;
    private final FermeService fermeService;

    public ExpertDashboardController() {
        this.analyseService = new AnalyseService();
        this.conseilService = new ConseilService();
        this.fermeService = new FermeService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            ProfileManager.updateProfileUI(currentUser, welcomeLabel, userNameLabel, sidebarAvatar, sidebarAvatarText);
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

            // Total conseils
            int totalConseils = conseilService.selectALL().size();
            if (totalConseilsLabel != null) {
                totalConseilsLabel.setText(String.valueOf(totalConseils));
            }

            // Total fermes
            int totalFermes = fermeService.selectALL().size();
            if (totalFermesLabel != null) {
                totalFermesLabel.setText(String.valueOf(totalFermes));
            }

            LOGGER.log(Level.INFO, "Statistics loaded: {0} analyses, {1} conseils, {2} fermes",
                    new Object[] { totalAnalyses, totalConseils, totalFermes });

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading statistics", e);
            // Set default values on error
            if (totalAnalysesLabel != null)
                totalAnalysesLabel.setText("-");
            if (totalConseilsLabel != null)
                totalConseilsLabel.setText("-");
            if (totalFermesLabel != null)
                totalFermesLabel.setText("-");
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
        }
    }

    /**
     * Handle consultations - Open GestionAnalyses with fade transition
     */
    @FXML
    private void handleConsultations() {
        navigateWithFade("/tn/esprit/farmai/views/gestion-analyses.fxml",
                "FarmAI - Gestion des Analyses");
    }

    /**
     * Handle recommendations - Open GestionConseils with fade transition
     */
    @FXML
    private void handleRecommendations() {
        navigateWithFade("/tn/esprit/farmai/views/gestion-conseils.fxml",
                "FarmAI - Gestion des Conseils");
    }

    /**
     * Handle statistics - Open Statistics Dashboard (US10)
     */
    @FXML
    private void handleStatistics() {
        navigateWithFade("/tn/esprit/farmai/views/statistics.fxml",
                "FarmAI - Statistiques");
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
            NavigationUtil.showError("Erreur", "Impossible d'ouvrir la reconnaissance faciale: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * NEW: Handle Generate Report - Generate intelligent analysis report with AI
     * summary
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
     * Show report generation success dialog with clear file location and access
     * buttons
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
     * Navigate to a new view with smooth fade transition
     */
    private void navigateWithFade(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            javafx.scene.Parent currentRoot = welcomeLabel.getScene().getRoot();

            // Fade out current scene
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentRoot);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(event -> {
                try {
                    // Load new view
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                            getClass().getResource(fxmlPath));
                    javafx.scene.Parent newRoot = loader.load();

                    // Create scene with new root
                    Scene scene = new Scene(newRoot, 1200, 800);

                    // Apply CSS
                    String cssPath = getClass().getResource("/tn/esprit/farmai/styles/dashboard.css") != null
                            ? getClass().getResource("/tn/esprit/farmai/styles/dashboard.css").toExternalForm()
                            : null;
                    if (cssPath != null) {
                        scene.getStylesheets().add(cssPath);
                    }

                    // Set initial opacity for fade in
                    newRoot.setOpacity(0.0);
                    stage.setScene(scene);
                    stage.setTitle(title);

                    // Fade in new scene
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(250), newRoot);
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
     * Play entrance animation for dashboard elements
     */
    private void playEntranceAnimation() {
        if (welcomeLabel != null) {
            welcomeLabel.setOpacity(0.0);
            welcomeLabel.setTranslateY(20);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), welcomeLabel);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            javafx.animation.TranslateTransition slideUp = new javafx.animation.TranslateTransition(
                    Duration.millis(400), welcomeLabel);
            slideUp.setFromY(20);
            slideUp.setToY(0);

            fadeIn.play();
            slideUp.play();
        }
    }
}
