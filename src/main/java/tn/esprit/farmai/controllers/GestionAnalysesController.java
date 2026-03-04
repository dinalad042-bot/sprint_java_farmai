package tn.esprit.farmai.controllers;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.farmai.models.Analyse;
import tn.esprit.farmai.models.DiagnosisResult;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.AnalyseService;
import tn.esprit.farmai.services.ExpertVisionService;
import tn.esprit.farmai.services.IntelligentReportService;
import tn.esprit.farmai.services.NotificationService;
import tn.esprit.farmai.utils.AlertUtils;
import tn.esprit.farmai.utils.AnalyseDialog;
import tn.esprit.farmai.utils.NavigationUtil;
import tn.esprit.farmai.utils.NotificationManager;
import tn.esprit.farmai.utils.ProfileManager;
import tn.esprit.farmai.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import javafx.stage.FileChooser;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for managing analyses with advanced features.
 * Implements US8: AI-Assisted Diagnostics, US9: PDF Technical Reporting
 * Uses TableView with ObservableList populated from AnalyseService.
 */
public class GestionAnalysesController implements Initializable {

    @FXML
    private TableView<Analyse> analysesTableView;
    @FXML
    private TableColumn<Analyse, Integer> colId;
    @FXML
    private TableColumn<Analyse, String> colDate;
    @FXML
    private TableColumn<Analyse, String> colResultat;
    @FXML
    private TableColumn<Analyse, Integer> colTechnicien;
    @FXML
    private TableColumn<Analyse, Integer> colFerme;
    @FXML
    private TableColumn<Analyse, String> colImage;
    @FXML
    private TableColumn<Analyse, Void> colActions;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> technicienFilterComboBox;
    @FXML
    private Button refreshButton;
    @FXML
    private Button addAnalyseButton;
    @FXML
    private Button aiDiagnosticButton;
    @FXML
    private Button visualDiagnoseButton;
    @FXML
    private Button exportPdfButton;
    @FXML
    private Button backButton;
    @FXML
    private Label totalAnalysesLabel;

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userRoleLabel;
    @FXML
    private Circle userAvatarCircle;
    @FXML
    private Label defaultAvatarText;
    @FXML
    private Circle headerAvatarCircle;

    private final AnalyseService analyseService;
    private ObservableList<Analyse> analysesList;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public GestionAnalysesController() {
        this.analyseService = new AnalyseService();
        this.analysesList = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableView();
        setupSearch();
        setupDoubleClick();
        setupAIAndPDFButtons();
        loadAnalyses();
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
     * US8: Setup AI Diagnostic button functionality
     */
    private void setupAIAndPDFButtons() {
        // AI Diagnostic button
        aiDiagnosticButton.setOnAction(event -> handleAIDiagnostic());

        // PDF Export button
        exportPdfButton.setOnAction(event -> handleExportPDF());
    }

    /**
     * US8: Handle AI Diagnostic generation
     */
    @FXML
    private void handleAIDiagnostic() {
        Analyse selectedAnalyse = analysesTableView.getSelectionModel().getSelectedItem();

        if (selectedAnalyse == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "Please select an analysis first to use AI diagnostic.");
            return;
        }

        // Create dialog for observation input
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("AI-Assisted Diagnostic");
        dialog.setHeaderText("Enter your observations for AI analysis");

        // Set the button types
        ButtonType generateButtonType = new ButtonType("Generate", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(generateButtonType, ButtonType.CANCEL);

        // Create the observation text area
        TextArea observationArea = new TextArea();
        observationArea.setPromptText("Enter sensor data, visual observations, or notes for AI analysis...");
        observationArea.setPrefRowCount(10);
        observationArea.setPrefColumnCount(40);

        dialog.getDialogPane().setContent(observationArea);

        // Request focus on the observation area by default
        dialog.setOnShown(dialogEvent -> observationArea.requestFocus());

        // Convert the result to a string when the generate button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == generateButtonType) {
                return observationArea.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(observation -> {
            if (observation.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Empty Input",
                        "Please enter some observations for AI analysis.");
                return;
            }

            Label statusLabel = new Label("Generating AI diagnostic...\nPlease wait...");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-text-alignment: center;");
            statusLabel.setPrefSize(300, 80);

            Dialog<Void> loadingDialog = new Dialog<>();
            loadingDialog.setTitle("AI Analysis in Progress");
            loadingDialog.setHeaderText("Connecting to AI service...");
            loadingDialog.getDialogPane().setContent(statusLabel);
            loadingDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            loadingDialog.getDialogPane().setPrefSize(350, 150);

            final Analyse analyseToEdit = selectedAnalyse;

            loadingDialog.setOnCloseRequest(e -> {
                // Handle cancellation
            });

            loadingDialog.show();

            new Thread(() -> {
                try {
                    String aiResult = analyseService.generateAIDiagnostic(observation);

                    Platform.runLater(() -> {
                        loadingDialog.close();

                        TextArea resultArea = new TextArea(aiResult);
                        resultArea.setPrefRowCount(15);
                        resultArea.setPrefColumnCount(60);
                        resultArea.setWrapText(true);
                        resultArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");

                        Dialog<String> resultDialog = new Dialog<>();
                        resultDialog.setTitle("AI Diagnostic Result");
                        resultDialog.setHeaderText("Review and edit the AI-generated diagnostic:");
                        resultDialog.getDialogPane().setContent(resultArea);
                        resultDialog.getDialogPane().setPrefSize(600, 400);

                        ButtonType applyButtonType = new ButtonType("Apply to Analysis", ButtonBar.ButtonData.OK_DONE);
                        resultDialog.getDialogPane().getButtonTypes().addAll(applyButtonType, ButtonType.CANCEL);

                        resultDialog.setResultConverter(dialogButton -> {
                            if (dialogButton == applyButtonType) {
                                return resultArea.getText();
                            }
                            return null;
                        });

                        Optional<String> finalResult = resultDialog.showAndWait();
                        finalResult.ifPresent(editedResult -> {
                            analyseToEdit.setResultatTechnique(editedResult);
                            try {
                                analyseService.updateOne(analyseToEdit);
                                loadAnalyses();
                                showAlert(Alert.AlertType.INFORMATION, "Success",
                                        "AI diagnostic applied to analysis #" + analyseToEdit.getIdAnalyse());
                            } catch (SQLException e) {
                                showAlert(Alert.AlertType.ERROR, "Database Error",
                                        "Failed to update: " + e.getMessage());
                            }
                        });
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        loadingDialog.close();
                        String errorMsg = e.getMessage();
                        if (errorMsg == null || errorMsg.isEmpty()) {
                            errorMsg = "Unknown error occurred during AI analysis.";
                        }
                        showAlert(Alert.AlertType.ERROR, "AI Diagnostic Error", errorMsg);
                    });
                }
            }).start();
        });
    }

    /**
     * NEW: Handle Visual Diagnosis - AI image analysis for plant diseases
     */
    @FXML
    private void handleVisualDiagnosis() {
        // Check API key first
        if (tn.esprit.farmai.utils.Config.GROQ_API_KEY == null ||
                tn.esprit.farmai.utils.Config.GROQ_API_KEY.trim().isEmpty()) {
            showError("Configuration manquante",
                    "Cle API Groq non configuree.\n\n" +
                            "Veuillez creer un fichier config.properties dans le dossier du projet avec:\n" +
                            "GROQ_API_KEY=votre_cle_api\n\n" +
                            "Obtenez une cle gratuite sur: https://console.groq.com/keys");
            return;
        }

        // File chooser for image selection
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selectionner une image de plante");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png"));

        File selectedFile = fileChooser.showOpenDialog(getCurrentStage());
        if (selectedFile == null) {
            return; // User cancelled
        }

        // Validate file
        if (!selectedFile.exists()) {
            showError("Erreur fichier", "Le fichier selectionne n'existe pas.");
            return;
        }

        long fileSizeMB = selectedFile.length() / (1024 * 1024);
        if (fileSizeMB > 5) {
            showError("Fichier trop grand",
                    "L'image est trop grande (" + fileSizeMB + " MB).\nMaximum: 5 MB");
            return;
        }

        // Show progress dialog
        Dialog<Void> progressDialog = new Dialog<>();
        progressDialog.setTitle("Analyse Visuelle");
        progressDialog.setHeaderText("Analyse de l'image en cours...");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(60, 60);

        Label statusLabel = new Label("Preparation de l'image...");
        statusLabel.setStyle("-fx-font-size: 13px;");

        VBox dialogContent = new VBox(15, progressIndicator, statusLabel);
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setPadding(new javafx.geometry.Insets(20));

        progressDialog.getDialogPane().setContent(dialogContent);
        progressDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        // Store file path for later use
        final String selectedFilePath = selectedFile.getAbsolutePath();
        final String selectedFileName = selectedFile.getName();

        // Run analysis in background thread
        Thread analysisThread = new Thread(() -> {
            try {
                Platform.runLater(() -> statusLabel.setText("Envoi a l'IA pour analyse..."));

                ExpertVisionService visionService = new ExpertVisionService();
                DiagnosisResult result = visionService.analyzePlantImage(selectedFilePath);

                Platform.runLater(() -> {
                    progressDialog.close();
                    showDiagnosisResultDialog(result, selectedFileName, selectedFilePath);
                });

            } catch (ExpertVisionService.VisionException e) {
                Platform.runLater(() -> {
                    progressDialog.close();
                    String errorMsg = e.getMessage();
                    System.err.println("VisionException: " + errorMsg);
                    e.printStackTrace();

                    // Show full error details
                    javafx.scene.control.TextArea errorText = new javafx.scene.control.TextArea(errorMsg);
                    errorText.setEditable(false);
                    errorText.setWrapText(true);
                    errorText.setPrefRowCount(10);
                    errorText.setPrefWidth(500);

                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Erreur de connexion API");
                    alert.setHeaderText("L'appel a l'API Groq a echoue");
                    alert.getDialogPane().setContent(errorText);
                    alert.setResizable(true);
                    alert.showAndWait();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressDialog.close();
                    String errorDetail = e.getMessage();
                    if (errorDetail == null || errorDetail.isEmpty()) {
                        errorDetail = e.getClass().getName();
                    }
                    showError("Erreur inattendue",
                            "Une erreur s'est produite:\n" + errorDetail);
                    e.printStackTrace();
                });
            }
        });

        analysisThread.setDaemon(true);
        analysisThread.start();

        progressDialog.showAndWait();
    }

    /**
     * Show diagnosis result dialog with option to save to analysis
     */
    private void showDiagnosisResultDialog(DiagnosisResult result, String imageName, String imagePath) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Resultat du Diagnostic Visuel");
        dialog.setHeaderText("Analyse de: " + imageName);
        dialog.setResizable(true);

        // Create content with minimum size
        VBox content = new VBox(15);
        content.setPadding(new javafx.geometry.Insets(20));
        content.setPrefWidth(600);
        content.setMinWidth(500);
        content.setMinHeight(400);
        content.setStyle("-fx-background-color: white;");

        // Condition with confidence
        HBox conditionBox = new HBox(10);
        conditionBox.setAlignment(Pos.CENTER_LEFT);
        Label conditionLabel = new Label("Condition:");
        conditionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label conditionValue = new Label(result.getCondition());
        conditionValue.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        conditionBox.getChildren().addAll(conditionLabel, conditionValue);

        // Confidence badge
        String confidenceColor = switch (result.getConfidence()) {
            case HIGH -> "#4CAF50";
            case MEDIUM -> "#FF9800";
            case LOW -> "#F44336";
        };
        Label confidenceBadge = new Label("Confiance: " + result.getConfidence().getLabel());
        confidenceBadge.setStyle("-fx-background-color: " + confidenceColor + "; -fx-text-fill: white; " +
                "-fx-padding: 8px 20px; -fx-background-radius: 15px; -fx-font-weight: bold; -fx-font-size: 13px;");

        // Expert consult warning
        VBox warningBox = new VBox();
        if (result.isNeedsExpertConsult()) {
            Label warningLabel = new Label("⚠️ Consultation d'expert recommandee");
            warningLabel.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold; -fx-font-size: 14px;");
            warningBox.getChildren().add(warningLabel);
        }

        // Details sections - using VBox with expandable panes (all can be open at once)
        VBox detailsBox = new VBox(10);
        detailsBox.setFillWidth(true);
        VBox.setVgrow(detailsBox, javafx.scene.layout.Priority.ALWAYS);

        // Create expandable sections
        TitledPane symptomsPane = createDetailPane("Symptomes", result.getSymptoms());
        TitledPane treatmentPane = createDetailPane("Traitement", result.getTreatment());
        TitledPane preventionPane = createDetailPane("Prevention", result.getPrevention());

        // Expand all sections by default
        symptomsPane.setExpanded(true);
        treatmentPane.setExpanded(true);
        preventionPane.setExpanded(true);

        detailsBox.getChildren().addAll(symptomsPane, treatmentPane, preventionPane);

        content.getChildren().addAll(conditionBox, confidenceBadge, warningBox, new Separator(), detailsBox);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(650, 500);

        // Add buttons
        ButtonType saveBtn = new ButtonType("Sauvegarder dans Analyse", ButtonBar.ButtonData.OK_DONE);
        ButtonType closeBtn = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, closeBtn);

        Optional<ButtonType> dialogResult = dialog.showAndWait();

        if (dialogResult.isPresent() && dialogResult.get() == saveBtn) {
            saveDiagnosisToNewAnalysis(result, imagePath);
        }
    }

    private TitledPane createDetailPane(String title, String content) {
        TextArea textArea = new TextArea(content);
        textArea.setWrapText(true);
        textArea.setEditable(false);
        textArea.setPrefRowCount(5);
        textArea.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 12px;");

        TitledPane pane = new TitledPane(title, textArea);
        pane.setStyle("-fx-font-weight: bold;");
        return pane;
    }

    private void saveDiagnosisToNewAnalysis(DiagnosisResult result, String imagePath) {
        try {
            Analyse newAnalyse = new Analyse();
            newAnalyse.setDateAnalyse(LocalDateTime.now());
            newAnalyse.setResultatTechnique(result.toAnalysisText());
            newAnalyse.setIdTechnicien(1); // Current user ID - should get from session
            newAnalyse.setIdFerme(1); // Default farm - should prompt user
            newAnalyse.setImageUrl(imagePath);

            analyseService.insertOne(newAnalyse);
            loadAnalyses();

            // Create notification for the fermier (same as handleAddAnalyse)
            try {
                NotificationService notificationService = new NotificationService();
                boolean notificationCreated = notificationService.createForFermier(
                        newAnalyse.getIdFerme(),
                        "📊 Nouvelle analyse disponible",
                        "Une nouvelle analyse a été ajoutée pour votre ferme. Cliquez pour la consulter.",
                        "ANALYSE",
                        newAnalyse.getIdAnalyse());

                if (notificationCreated) {
                    System.out.println(
                            "✅ Notification créée avec succès pour le fermier de la ferme #" + newAnalyse.getIdFerme());
                } else {
                    System.err.println("⚠️ Impossible de créer la notification pour la ferme #"
                            + newAnalyse.getIdFerme() + " (fermier non trouvé ou erreur DB)");
                }
            } catch (Exception ex) {
                System.err.println("❌ ERREUR lors de la création de la notification: " + ex.getMessage());
                ex.printStackTrace();
            }

            showInfo("Succes", "Diagnostic sauvegarde dans l'analyse #" + newAnalyse.getIdAnalyse());

        } catch (SQLException e) {
            showError("Erreur de base de donnees", "Impossible de sauvegarder: " + e.getMessage());
        }
    }

    /**
     * US9: Handle PDF export - Non-blocking implementation
     */
    @FXML
    private void handleExportPDF() {
        Analyse selectedAnalyse = analysesTableView.getSelectionModel().getSelectedItem();

        if (selectedAnalyse == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "Please select an analysis to export as PDF.");
            return;
        }

        // Disable the export button during generation
        exportPdfButton.setDisable(true);

        // Create progress dialog (non-blocking)
        Stage progressStage = new Stage();
        progressStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        progressStage.setTitle("PDF Generation");
        progressStage.setResizable(false);

        VBox progressBox = new VBox(15);
        progressBox.setAlignment(Pos.CENTER);
        progressBox.setPadding(new javafx.geometry.Insets(20));
        progressBox.setPrefWidth(300);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(50, 50);

        Label statusLabel = new Label("Generating PDF report...");
        statusLabel.setStyle("-fx-font-size: 13px;");

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

        progressBox.getChildren().addAll(progressIndicator, statusLabel, cancelButton);

        Scene progressScene = new Scene(progressBox);
        progressStage.setScene(progressScene);

        // Thread reference for cancellation
        final Thread[] generationThread = new Thread[1];

        cancelButton.setOnAction(e -> {
            if (generationThread[0] != null) {
                generationThread[0].interrupt();
            }
            progressStage.close();
            exportPdfButton.setDisable(false);
        });

        // Run PDF generation in background thread
        generationThread[0] = new Thread(() -> {
            try {
                String pdfPath = analyseService.exportAnalysisToPDF(selectedAnalyse.getIdAnalyse());

                Platform.runLater(() -> {
                    if (progressStage.isShowing()) {
                        progressStage.close();
                    }
                    exportPdfButton.setDisable(false);
                    showPDFSuccessDialog(pdfPath);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (progressStage.isShowing()) {
                        progressStage.close();
                    }
                    exportPdfButton.setDisable(false);
                    showAlert(Alert.AlertType.ERROR, "PDF Error",
                            "Failed to generate PDF: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });

        generationThread[0].setDaemon(true);
        generationThread[0].start();

        // Show progress stage
        progressStage.show();
    }

    /**
     * Show PDF generation success dialog with file path
     */
    private void showPDFSuccessDialog(String pdfPath) {
        // Create custom dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("PDF Generated Successfully");
        dialog.setHeaderText(null);

        // Create content
        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(20));
        content.setPrefWidth(500);
        content.setStyle("-fx-background-color: #f8f9fa;");

        // Success icon and message
        Label successLabel = new Label("✓ Report saved successfully!");
        successLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #28a745;");

        // File path section
        Label locationLabel = new Label("File Location:");
        locationLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #495057;");

        TextField pathField = new TextField(pdfPath);
        pathField.setEditable(false);
        pathField.setPrefWidth(460);
        pathField.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; " +
                "-fx-background-color: white; -fx-border-color: #dee2e6; " +
                "-fx-border-radius: 4px; -fx-padding: 8px;");
        pathField.setTooltip(new Tooltip("Full path to the generated PDF file"));

        // File name display
        File pdfFile = new File(pdfPath);
        Label filenameLabel = new Label("Filename: " + pdfFile.getName());
        filenameLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

        // Tips section
        Label tipLabel = new Label("💡 The PDF has been saved to your temp folder. " +
                "You can copy the path above to open it.");
        tipLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d; -fx-font-style: italic;");
        tipLabel.setWrapText(true);

        content.getChildren().addAll(
                successLabel,
                new Separator(),
                locationLabel,
                pathField,
                filenameLabel,
                new Separator(),
                tipLabel);

        dialog.getDialogPane().setContent(content);

        // Add buttons
        ButtonType openFolderBtn = new ButtonType("Open Folder", ButtonBar.ButtonData.LEFT);
        ButtonType copyPathBtn = new ButtonType("Copy Path", ButtonBar.ButtonData.LEFT);
        ButtonType okBtn = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(openFolderBtn, copyPathBtn, okBtn);

        // Style the buttons
        Button openFolderButton = (Button) dialog.getDialogPane().lookupButton(openFolderBtn);
        openFolderButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;");

        Button copyPathButton = (Button) dialog.getDialogPane().lookupButton(copyPathBtn);
        copyPathButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");

        // Handle button actions
        dialog.setResultConverter(buttonType -> {
            if (buttonType == openFolderBtn) {
                openFolderContainingFile(pdfPath);
            } else if (buttonType == copyPathBtn) {
                copyToClipboard(pdfPath);
                showInfo("Copied", "File path copied to clipboard!");
            }
            return buttonType;
        });

        dialog.showAndWait();
    }

    /**
     * Open the folder containing the generated PDF
     */
    private void openFolderContainingFile(String filePath) {
        try {
            File file = new File(filePath);
            File parentDir = file.getParentFile();

            if (parentDir != null && parentDir.exists()) {
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    Runtime.getRuntime().exec("explorer.exe /select,\"" + filePath + "\"");
                } else if (os.contains("mac")) {
                    Runtime.getRuntime().exec(new String[] { "open", "-R", filePath });
                } else {
                    Runtime.getRuntime().exec(new String[] { "xdg-open", parentDir.getAbsolutePath() });
                }
            }
        } catch (IOException e) {
            showError("Error", "Could not open folder: " + e.getMessage());
        }
    }

    /**
     * Copy text to system clipboard
     */
    private void copyToClipboard(String text) {
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }

    /**
     * Setup double-click on table row to edit
     */
    private void setupDoubleClick() {
        analysesTableView.setRowFactory(tv -> {
            TableRow<Analyse> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Analyse analyse = row.getItem();
                    handleEditAnalyse(analyse);
                }
            });
            return row;
        });
    }

    /**
     * Setup table view columns and cell factories
     */
    private void setupTableView() {
        analysesTableView.setFixedCellSize(-1);

        colId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getIdAnalyse()));
        colId.setCellFactory(col -> new TableCell<Analyse, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(String.valueOf(item));
                    label.getStyleClass().add("table-cell-content");
                    label.setWrapText(true);
                    setGraphic(label);
                }
            }
        });

        colDate.setCellValueFactory(cellData -> {
            try {
                LocalDateTime date = cellData.getValue().getDateAnalyse();
                if (date == null) {
                    return new SimpleStringProperty("No date");
                }
                return new SimpleStringProperty(date.format(DATE_FORMATTER));
            } catch (Exception e) {
                return new SimpleStringProperty("Invalid date");
            }
        });
        colDate.setCellFactory(col -> new TableCell<Analyse, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item);
                    label.getStyleClass().add("table-cell-content");
                    label.setWrapText(true);
                    setGraphic(label);
                }
            }
        });

        colResultat.setCellValueFactory(cellData -> {
            try {
                String resultat = cellData.getValue().getResultatTechnique();
                if (resultat == null || resultat.trim().isEmpty()) {
                    return new SimpleStringProperty("No result");
                }
                return new SimpleStringProperty(resultat);
            } catch (Exception e) {
                return new SimpleStringProperty("Error");
            }
        });
        colResultat.setCellFactory(col -> new TableCell<Analyse, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item);
                    label.getStyleClass().add("table-cell-content");
                    label.setWrapText(true);
                    label.setMaxWidth(240);
                    label.setPrefWidth(240);
                    setGraphic(label);
                }
            }
        });

        colTechnicien.setCellValueFactory(cellData -> {
            try {
                int techId = cellData.getValue().getIdTechnicien();
                return new SimpleObjectProperty<>(techId);
            } catch (Exception e) {
                return new SimpleObjectProperty<>(0);
            }
        });
        colTechnicien.setCellFactory(col -> new TableCell<Analyse, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label("Tech #" + item);
                    label.getStyleClass().add("table-cell-content");
                    label.setWrapText(true);
                    setGraphic(label);
                }
            }
        });

        colFerme.setCellValueFactory(cellData -> {
            try {
                int farmId = cellData.getValue().getIdFerme();
                return new SimpleObjectProperty<>(farmId);
            } catch (Exception e) {
                return new SimpleObjectProperty<>(0);
            }
        });
        colFerme.setCellFactory(col -> new TableCell<Analyse, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label("Farm #" + item);
                    label.getStyleClass().add("table-cell-content");
                    label.setWrapText(true);
                    setGraphic(label);
                }
            }
        });

        colImage.setCellValueFactory(cellData -> {
            String imageUrl = cellData.getValue().getImageUrl();
            return new SimpleStringProperty(imageUrl);
        });
        colImage.setCellFactory(col -> new TableCell<Analyse, String>() {
            @Override
            protected void updateItem(String imageUrl, boolean empty) {
                super.updateItem(imageUrl, empty);
                if (empty) {
                    setGraphic(null);
                } else if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    try {
                        ImageView imageView = new ImageView();
                        imageView.setFitWidth(40);
                        imageView.setFitHeight(30);
                        imageView.setPreserveRatio(true);

                        File imageFile = new File(imageUrl);
                        if (imageFile.exists()) {
                            Image image = new Image(imageFile.toURI().toString());
                            imageView.setImage(image);
                        } else {
                            imageView.setImage(new Image("file:" + imageUrl, true));
                        }

                        setGraphic(imageView);
                    } catch (Exception e) {
                        Label label = new Label("[IMG]");
                        label.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 11px; -fx-font-weight: bold;");
                        setGraphic(label);
                    }
                } else {
                    Label label = new Label("[NO IMG]");
                    label.setStyle("-fx-text-fill: #9E9E9E; -fx-font-size: 11px;");
                    setGraphic(label);
                }
            }
        });

        setupActionsColumn();
    }

    /**
     * Setup actions column with edit, delete, and view buttons
     */
    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("✎");
            private final Button deleteButton = new Button("🗑");
            private final Button viewButton = new Button("👁");
            private final HBox pane = new HBox(5, viewButton, editButton, deleteButton);

            {
                pane.setAlignment(Pos.CENTER);
                pane.setStyle("-fx-padding: 5px;");

                // Use CSS classes for consistent button styling
                editButton.getStyleClass().add("btn-action");
                viewButton.getStyleClass().add("btn-success");
                deleteButton.getStyleClass().add("btn-danger");
                
                // Add tooltips
                editButton.setTooltip(new Tooltip("Modifier l'analyse"));
                viewButton.setTooltip(new Tooltip("Voir les détails"));
                deleteButton.setTooltip(new Tooltip("Supprimer"));

                editButton.setOnAction(event -> {
                    Analyse analyse = getTableRow().getItem();
                    if (analyse != null) {
                        handleEditAnalyse(analyse);
                    }
                });

                viewButton.setOnAction(event -> {
                    Analyse analyse = getTableRow().getItem();
                    if (analyse != null) {
                        handleViewAnalyse(analyse);
                    }
                });

                deleteButton.setOnAction(event -> {
                    Analyse analyse = getTableRow().getItem();
                    if (analyse != null) {
                        handleDeleteAnalyse(analyse);
                    }
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
     * Setup search functionality
     */
    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAnalyses();
        });

        technicienFilterComboBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    filterAnalyses();
                });
    }

    /**
     * Filter analyses based on search criteria
     */
    private void filterAnalyses() {
        String searchText = searchField.getText().toLowerCase();
        String selectedTechnicien = technicienFilterComboBox.getValue();

        ObservableList<Analyse> filteredList = FXCollections.observableArrayList();

        for (Analyse analyse : analysesList) {
            // Fix: Add null check for getResultatTechnique()
            String resultat = analyse.getResultatTechnique();
            boolean matchesSearch = searchText.isEmpty() ||
                    (resultat != null && resultat.toLowerCase().contains(searchText)) ||
                    String.valueOf(analyse.getIdAnalyse()).contains(searchText);

            boolean matchesTechnicien = selectedTechnicien == null ||
                    selectedTechnicien.equals("Tous les techniciens") ||
                    selectedTechnicien.contains(String.valueOf(analyse.getIdTechnicien()));

            if (matchesSearch && matchesTechnicien) {
                filteredList.add(analyse);
            }
        }

        analysesTableView.setItems(filteredList);
        totalAnalysesLabel.setText("Total: " + filteredList.size() + " analyses");
    }

    /**
     * Load analyses from database
     */
    private void loadAnalyses() {
        try {
            analysesList.clear();
            List<Analyse> analyses = analyseService.selectALL();

            if (analyses.isEmpty()) {
                // Defer dialog to avoid showing during animation
                Platform.runLater(() -> showInfo("No Data", "No analyses found in the database."));
            }
            analysesList.addAll(analyses);

            analysesTableView.setItems(analysesList);
            analysesTableView.refresh();

            totalAnalysesLabel.setText("Total: " + analysesList.size() + " analyses");

            populateTechnicienFilter();

        } catch (SQLException e) {
            // Defer dialog to avoid showing during animation
            Platform.runLater(() -> showError("Database Error", "Failed to load analyses: " + e.getMessage()));
            e.printStackTrace();

            // Keep UI functional even with database errors
            analysesList.clear();
            analysesTableView.setItems(analysesList);
            totalAnalysesLabel.setText("Total: 0 analyses (Database error)");
        } catch (Exception e) {
            // Defer dialog to avoid showing during animation
            Platform.runLater(
                    () -> showError("Unexpected Error", "An error occurred while loading data: " + e.getMessage()));
            e.printStackTrace();

            // Keep UI functional even with unexpected errors
            analysesList.clear();
            analysesTableView.setItems(analysesList);
            totalAnalysesLabel.setText("Total: 0 analyses (Error)");
        }
    }

    /**
     * Populate technician filter combo box
     */
    private void populateTechnicienFilter() {
        ObservableList<String> techniciens = FXCollections.observableArrayList();
        techniciens.add("Tous les techniciens");

        for (Analyse analyse : analysesList) {
            String technicien = "Technicien " + analyse.getIdTechnicien();
            if (!techniciens.contains(technicien)) {
                techniciens.add(technicien);
            }
        }

        technicienFilterComboBox.setItems(techniciens);
        technicienFilterComboBox.setValue("Tous les techniciens");
    }

    // Event handlers
    @FXML
    private void handleBack() {
        NavigationUtil.navigateToDashboard(getCurrentStage());
    }

    /**
     * Handle dashboard navigation from sidebar
     */
    @FXML
    private void handleDashboard() {
        navigateWithFade("/tn/esprit/farmai/views/expert-dashboard.fxml", "FarmAI - Tableau de Bord Expert");
    }

    /**
     * Handle conseils navigation from sidebar
     */
    @FXML
    private void handleConseils() {
        navigateWithFade("/tn/esprit/farmai/views/gestion-conseils.fxml", "FarmAI - Gestion des Conseils");
    }

    /**
     * Handle statistics navigation from sidebar
     */
    @FXML
    private void handleStatistics() {
        navigateWithFade("/tn/esprit/farmai/views/statistics.fxml", "FarmAI - Statistiques");
    }

    /**
     * Handle add face navigation from sidebar
     */
    @FXML
    private void handleAddFace() {
        try {
            Stage stage = getCurrentStage();
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
        boolean updated = ProfileManager.showProfileEditDialog(getCurrentStage());
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
        NavigationUtil.logout(getCurrentStage());
    }

    /**
     * Navigate to a view with smooth fade transition
     */
    private void navigateWithFade(String fxmlPath, String title) {
        try {
            Stage stage = getCurrentStage();
            Parent currentRoot = analysesTableView.getScene().getRoot();

            // Fade out
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentRoot);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(event -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                    Parent newRoot = loader.load();
                    Scene scene = new Scene(newRoot, 1200, 800);

                    // Apply unified main.css
                    java.net.URL cssUrl = getClass().getResource("/tn/esprit/farmai/styles/main.css");
                    if (cssUrl != null) {
                        scene.getStylesheets().add(cssUrl.toExternalForm());
                    }

                    newRoot.setOpacity(0.0);
                    stage.setScene(scene);
                    stage.setTitle(title);

                    // Fade in
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(250), newRoot);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();

                    stage.show();
                } catch (Exception e) {
                    showError("Erreur", "Impossible de charger la vue: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            fadeOut.play();
        } catch (Exception e) {
            showError("Erreur", "Erreur de navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Stage getCurrentStage() {
        return (Stage) analysesTableView.getScene().getWindow();
    }

    @FXML
    private void handleRefresh() {
        loadAnalyses();
    }

    @FXML
    private void handleAddAnalyse() {
        // Railway Track: Creates new Analyse entity via CRUD interface
        AnalyseDialog dialog = new AnalyseDialog();
        Optional<Analyse> result = dialog.showAndWait(analysesTableView.getScene().getWindow());

        result.ifPresent(newAnalyse -> {
            try {
                analyseService.insertOne(newAnalyse); // CRUD interface call
                loadAnalyses(); // Refresh table

                // Notify agricole users about new analysis (in-memory for current session)
                NotificationManager.addNotification("📊 Nouvelle analyse ajoutée - Ferme #" + newAnalyse.getIdFerme());

                // Create persistent notification for the fermier who owns this farm
                // This ensures the fermier sees the notification even if they're not currently
                // logged in
                try {
                    NotificationService notificationService = new NotificationService();
                    boolean notificationCreated = notificationService.createForFermier(
                            newAnalyse.getIdFerme(),
                            "📊 Nouvelle analyse disponible",
                            "Une nouvelle analyse a été ajoutée pour votre ferme. Cliquez pour la consulter.",
                            "ANALYSE",
                            newAnalyse.getIdAnalyse());

                    if (notificationCreated) {
                        System.out.println("✅ Notification créée avec succès pour le fermier de la ferme #"
                                + newAnalyse.getIdFerme());
                    } else {
                        System.err.println("⚠️ Impossible de créer la notification pour la ferme #"
                                + newAnalyse.getIdFerme() + " (fermier non trouvé ou erreur DB)");
                    }
                } catch (Exception ex) {
                    // Catch ALL exceptions from notification creation
                    System.err.println("❌ ERREUR lors de la création de la notification: " + ex.getMessage());
                    ex.printStackTrace();
                }

                showInfo("Success", "Analysis created successfully");
            } catch (SQLException e) {
                showError("Database Error", "Failed to create analysis: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void handleEditAnalyse(Analyse analyse) {
        // Railway Track: Updates Analyse entity via CRUD interface
        AnalyseDialog dialog = new AnalyseDialog(analyse);
        Optional<Analyse> result = dialog.showAndWait(analysesTableView.getScene().getWindow());

        result.ifPresent(updatedAnalyse -> {
            try {
                analyseService.updateOne(updatedAnalyse); // CRUD interface call
                loadAnalyses(); // Refresh table
                showInfo("Success", "Analysis updated successfully");
            } catch (SQLException e) {
                showError("Database Error", "Failed to update analysis: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void handleViewAnalyse(Analyse analyse) {
        // Railway Track: View-only dialog for Analyse entity with full details
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de l'Analyse");
        dialog.setHeaderText("Analyse ID: " + analyse.getIdAnalyse());
        dialog.setResizable(true);
        
        VBox content = new VBox(15);
        content.setPadding(new javafx.geometry.Insets(20));
        content.setPrefWidth(600);
        content.setMinHeight(400);
        
        // Date
        Label dateLabel = new Label("Date: " + analyse.getDateAnalyse().format(DATE_FORMATTER));
        dateLabel.setStyle("-fx-font-size: 14px;");
        
        // Farm and Technician info
        HBox infoBox = new HBox(20);
        Label farmLabel = new Label("Ferme: #" + analyse.getIdFerme());
        Label techLabel = new Label("Technicien: #" + analyse.getIdTechnicien());
        infoBox.getChildren().addAll(farmLabel, techLabel);
        
        // Image info
        Label imageLabel = new Label("Image: " + (analyse.getImageUrl() != null ? analyse.getImageUrl() : "Aucune image"));
        imageLabel.setStyle("-fx-text-fill: #78909C;");
        
        // Technical Result section
        Label resultTitle = new Label("Résultat technique:");
        resultTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        TextArea resultArea = new TextArea(analyse.getResultatTechnique());
        resultArea.setWrapText(true);
        resultArea.setEditable(false);
        resultArea.setPrefRowCount(15);
        resultArea.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");
        VBox.setVgrow(resultArea, javafx.scene.layout.Priority.ALWAYS);
        
        content.getChildren().addAll(dateLabel, infoBox, imageLabel, new Separator(), resultTitle, resultArea);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(650, 500);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void handleDeleteAnalyse(Analyse analyse) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Analysis");
        confirmAlert.setContentText("Are you sure you want to delete analysis #" + analyse.getIdAnalyse() + "?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                analyseService.deleteOne(analyse);
                loadAnalyses(); // Refresh table
                showAlert(Alert.AlertType.INFORMATION, "Success", "Analysis deleted successfully");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error",
                        "Failed to delete analysis: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Utility methods - delegated to AlertUtils for consistent styling
    private void showInfo(String title, String message) {
        AlertUtils.showInfo(title, message);
    }

    private void showError(String title, String message) {
        AlertUtils.showError(title, message);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        switch (type) {
            case INFORMATION -> AlertUtils.showInfo(title, message);
            case WARNING -> AlertUtils.showWarning(title, message);
            case ERROR -> AlertUtils.showError(title, message);
            case CONFIRMATION -> AlertUtils.showConfirmation(title, null, message);
            default -> AlertUtils.showInfo(title, message);
        }
    }
}
