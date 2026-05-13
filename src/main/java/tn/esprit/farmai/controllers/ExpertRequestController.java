package tn.esprit.farmai.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.farmai.services.ExpertVisionService;
import tn.esprit.farmai.models.Analyse;
import tn.esprit.farmai.models.DiagnosisResult;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.AnalyseService;
import tn.esprit.farmai.services.ExpertVisionService;
import tn.esprit.farmai.services.FermeService;
import tn.esprit.farmai.utils.ProfileManager;
import tn.esprit.farmai.utils.SessionManager;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Expert handling farmer requests.
 * Allows experts to view pending requests and take them.
 */
public class ExpertRequestController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ExpertRequestController.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private TableView<Analyse> pendingTableView;
    @FXML private TableColumn<Analyse, String> colId;
    @FXML private TableColumn<Analyse, String> colDate;
    @FXML private TableColumn<Analyse, String> colFerme;
    @FXML private TableColumn<Analyse, String> colDescription;
    @FXML private TableColumn<Analyse, String> colDemandeur;
    @FXML private TableColumn<Analyse, Void> colActions;

    @FXML private TableView<Analyse> myRequestsTableView;
    @FXML private TableColumn<Analyse, String> colMyId;
    @FXML private TableColumn<Analyse, String> colMyDate;
    @FXML private TableColumn<Analyse, String> colMyFerme;
    @FXML private TableColumn<Analyse, String> colMyStatut;
    @FXML private TableColumn<Analyse, String> colMyResultat;
    @FXML private TableColumn<Analyse, Void> colMyActions;

    private final AnalyseService analyseService;
    private final FermeService fermeService;
    private ObservableList<Analyse> pendingList;
    private ObservableList<Analyse> myRequestsList;

    public ExpertRequestController() {
        this.analyseService = new AnalyseService();
        this.fermeService = new FermeService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (userNameLabel != null) {
                userNameLabel.setText(currentUser.getFullName());
            }
            if (userRoleLabel != null) {
                userRoleLabel.setText(currentUser.getRole().getDisplayName());
            }
        }

        setupPendingTable();
        setupMyRequestsTable();
        loadData();
    }

    private void setupPendingTable() {
        colId.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getIdAnalyse())));
        colDate.setCellValueFactory(cell -> {
            if (cell.getValue().getDateAnalyse() != null) {
                return new SimpleStringProperty(cell.getValue().getDateAnalyse().format(DATE_FORMATTER));
            }
            return new SimpleStringProperty("-");
        });
        colFerme.setCellValueFactory(cell -> new SimpleStringProperty("Ferme #" + cell.getValue().getIdFerme()));
        colDescription.setCellValueFactory(cell -> {
            String desc = cell.getValue().getDescriptionDemande();
            if (desc != null && desc.length() > 50) {
                desc = desc.substring(0, 50) + "...";
            }
            return new SimpleStringProperty(desc != null ? desc : "-");
        });
        colDemandeur.setCellValueFactory(cell -> new SimpleStringProperty("Demandeur #" + cell.getValue().getIdDemandeur()));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button takeBtn = new Button("Prendre");
            {
                takeBtn.setStyle("-fx-background-color: #1e2a38; -fx-text-fill: white; -fx-font-size: 12px; -fx-cursor: hand;");
                takeBtn.setOnAction(e -> takeRequest(getTableRow().getItem()));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || getTableRow() == null || getTableRow().getItem() == null ? null : takeBtn);
            }
        });
    }

    private void setupMyRequestsTable() {
        colMyId.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getIdAnalyse())));
        colMyDate.setCellValueFactory(cell -> {
            if (cell.getValue().getDateAnalyse() != null) {
                return new SimpleStringProperty(cell.getValue().getDateAnalyse().format(DATE_FORMATTER));
            }
            return new SimpleStringProperty("-");
        });
        colMyFerme.setCellValueFactory(cell -> new SimpleStringProperty("Ferme #" + cell.getValue().getIdFerme()));
        colMyStatut.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatut()));
        colMyResultat.setCellValueFactory(cell -> {
            String result = cell.getValue().getResultatTechnique();
            if (result != null && result.length() > 50) {
                result = result.substring(0, 50) + "...";
            }
            return new SimpleStringProperty(result != null ? result : "-");
        });

        colMyActions.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("Voir");
            private final Button aiBtn = new Button("IA");
            private final Button completeBtn = new Button("Terminer");
            {
                viewBtn.setStyle("-fx-background-color: #455A64; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
                viewBtn.setOnAction(e -> showRequestDetails(getTableRow().getItem()));

                aiBtn.setStyle("-fx-background-color: #1e2a38; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
                aiBtn.setTooltip(new Tooltip("Diagnostic IA visuel"));
                aiBtn.setOnAction(e -> handleAIDiagnosis(getTableRow().getItem()));

                completeBtn.setStyle("-fx-background-color: #1e2a38; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
                completeBtn.setOnAction(e -> showCompleteDialog(getTableRow().getItem()));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Analyse a = getTableRow().getItem();
                    HBox hbox = new HBox(5);
                    hbox.getChildren().add(viewBtn);
                    // Always show AI button (removed image URL check for testing)
                    hbox.getChildren().add(aiBtn);
                    if ("en_cours".equals(a.getStatut())) {
                        hbox.getChildren().add(completeBtn);
                    }
                    setGraphic(hbox);
                }
            }
        });
    }

    private void loadData() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        try {
            // Load pending requests
            List<Analyse> pending = analyseService.findPendingRequests();
            pendingList = FXCollections.observableArrayList(pending);
            pendingTableView.setItems(pendingList);

            // Load expert's requests
            List<Analyse> myReqs = analyseService.findInProgressByTechnicien(currentUser.getIdUser());
            myRequestsList = FXCollections.observableArrayList(myReqs);
            myRequestsTableView.setItems(myRequestsList);

            LOGGER.log(Level.INFO, "Loaded {0} pending, {1} my requests", new Object[]{pending.size(), myReqs.size()});
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load requests", e);
            tn.esprit.farmai.utils.NavigationUtil.showError("Erreur", "Impossible de charger les demandes: " + e.getMessage());
        }
    }

    private void takeRequest(Analyse analyse) {
        if (analyse == null) return;

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        try {
            analyseService.takeRequest(analyse.getIdAnalyse(), currentUser.getIdUser());
            tn.esprit.farmai.utils.NavigationUtil.showSuccess("Succès", "Demande prise en charge!");
            loadData();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to take request", e);
            tn.esprit.farmai.utils.NavigationUtil.showError("Erreur", "Impossible de prendre la demande: " + e.getMessage());
        }
    }

    private void showRequestDetails(Analyse analyse) {
        if (analyse == null) return;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la Demande #" + analyse.getIdAnalyse());
        alert.setHeaderText("Demande #" + analyse.getIdAnalyse());

        StringBuilder content = new StringBuilder();
        content.append("Statut: ").append(analyse.getStatut()).append("\n\n");
        content.append("Ferme: #").append(analyse.getIdFerme()).append("\n\n");
        content.append("Date: ").append(analyse.getDateAnalyse() != null ? analyse.getDateAnalyse().format(DATE_FORMATTER) : "-").append("\n\n");
        content.append("Demandeur: #").append(analyse.getIdDemandeur()).append("\n\n");
        content.append("Description:\n").append(analyse.getDescriptionDemande() != null ? analyse.getDescriptionDemande() : "Aucune").append("\n\n");

        if (analyse.getIdAnimalCible() > 0) {
            content.append("Animal cible: #").append(analyse.getIdAnimalCible()).append("\n");
        }
        if (analyse.getIdPlanteCible() > 0) {
            content.append("Plante cible: #").append(analyse.getIdPlanteCible()).append("\n");
        }

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    private void showCompleteDialog(Analyse analyse) {
        if (analyse == null) return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Terminer l'Analyse");
        dialog.setHeaderText("Analyse #" + analyse.getIdAnalyse());
        dialog.setContentText("Entrez le résultat technique:");

        dialog.showAndWait().ifPresent(result -> {
            if (result != null && !result.trim().isEmpty()) {
                try {
                    analyseService.completeAnalyse(analyse.getIdAnalyse(), result);
                    tn.esprit.farmai.utils.NavigationUtil.showSuccess("Succès", "Analyse terminée!");
                    loadData();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Failed to complete analyse", e);
                    tn.esprit.farmai.utils.NavigationUtil.showError("Erreur", "Impossible de terminer l'analyse: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Handle AI visual diagnosis for an analysis with image.
     */
    private void handleAIDiagnosis(Analyse analyse) {
        if (analyse == null) return;

        String imageUrl = analyse.getImageUrl();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            tn.esprit.farmai.utils.NavigationUtil.showError("Erreur", "Pas d'image disponible pour cette analyse.");
            return;
        }

        // Check API key
        String apiKey = tn.esprit.farmai.utils.Config.GROQ_API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            tn.esprit.farmai.utils.NavigationUtil.showError("Configuration manquante",
                    "Cle API Groq non configuree.\nVeuillez configurer GROQ_API_KEY dans config.properties.");
            return;
        }

        // Resolve image path - handle Symfony relative paths
        String resolvedPath = resolveImagePath(imageUrl);
        System.out.println("DEBUG RESOLVED PATH (ExpertRequest): " + resolvedPath);

        // Check if it's a URL (http/https) or a local file
        if (resolvedPath.startsWith("http")) {
            // For remote URLs, we need to download it first
            // For now, show error that local files are required
            tn.esprit.farmai.utils.NavigationUtil.showError("Erreur",
                    "Les images distantes ne sont pas supportées. L'image doit être un fichier local.");
            return;
        }

        // Verify local image file exists
        File imageFile = new File(resolvedPath);
        if (!imageFile.exists()) {
            tn.esprit.farmai.utils.NavigationUtil.showError("Erreur", "Fichier image introuvable: " + resolvedPath);
            return;
        }

        // Show progress dialog
        Dialog<Void> progressDialog = new Dialog<>();
        progressDialog.setTitle("Diagnostic IA");
        progressDialog.setHeaderText("Analyse visuelle en cours...");
        progressDialog.setResizable(false);

        Label statusLabel = new Label("Envoi de l'image pour analyse IA...");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-padding: 10;");

        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(250);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

        VBox content = new VBox(15, progressBar, statusLabel);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.setPadding(new javafx.geometry.Insets(20));

        progressDialog.getDialogPane().setContent(content);
        progressDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        final String imagePath = resolvedPath;
        final int analyseId = analyse.getIdAnalyse();

        Thread analysisThread = new Thread(() -> {
            try {
                Platform.runLater(() -> statusLabel.setText("Analyse en cours..."));

                ExpertVisionService visionService = new ExpertVisionService();
                DiagnosisResult result = visionService.analyzePlantImage(imagePath);

                Platform.runLater(() -> {
                    progressDialog.close();
                    showDiagnosisResultDialog(result, analyseId);
                });

            } catch (ExpertVisionService.VisionException e) {
                Platform.runLater(() -> {
                    progressDialog.close();
                    tn.esprit.farmai.utils.NavigationUtil.showError("Erreur de diagnostic",
                            "Impossible d'analyser l'image: " + e.getMessage());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressDialog.close();
                    tn.esprit.farmai.utils.NavigationUtil.showError("Erreur", "Une erreur s'est produite: " + e.getMessage());
                });
            }
        });

        analysisThread.setDaemon(true);
        analysisThread.start();
        progressDialog.showAndWait();
    }

    /**
     * Show diagnosis result and offer to save to analysis.
     * Fix: Force layout pass when dialog window is shown to prevent blank content.
     */
    private void showDiagnosisResultDialog(DiagnosisResult result, int analyseId) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Resultat du Diagnostic IA");
        dialog.setHeaderText("Analyse #" + analyseId);
        dialog.setResizable(true);

        VBox content = new VBox(15);
        content.setPadding(new javafx.geometry.Insets(20));
        content.setPrefWidth(550);
        content.setMinWidth(450);
        content.setMinHeight(350);

        // Condition with color badge
        Label conditionLabel = new Label("Condition: " + result.getCondition());
        conditionLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        String conditionLower = result.getCondition().toLowerCase();
        if (conditionLower.contains("saine") || conditionLower.contains("normale") || conditionLower.contains("normal")) {
            conditionLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 18px; -fx-font-weight: bold;");
        } else if (conditionLower.contains("pas") && (conditionLower.contains("plante") || conditionLower.contains("animal"))) {
            conditionLabel.setStyle("-fx-text-fill: #9E9E9E; -fx-font-size: 18px; -fx-font-weight: bold;");
        }

        // Confidence
        String confColor = switch (result.getConfidence()) {
            case HIGH -> "#4CAF50";
            case MEDIUM -> "#FF9800";
            case LOW -> "#F44336";
        };
        Label confLabel = new Label("Confiance: " + result.getConfidence().getLabel());
        confLabel.setStyle("-fx-background-color: " + confColor + "; -fx-text-fill: white; " +
                "-fx-padding: 5px 15px; -fx-background-radius: 10px; -fx-font-weight: bold;");

        // Details
        TextArea symptomsArea = new TextArea("Symptomes:\n" + result.getSymptoms());
        symptomsArea.setEditable(false);
        symptomsArea.setWrapText(true);
        symptomsArea.setPrefRowCount(3);
        symptomsArea.setStyle("-fx-font-family: monospace;");

        TextArea treatmentArea = new TextArea("Traitement:\n" + result.getTreatment());
        treatmentArea.setEditable(false);
        treatmentArea.setWrapText(true);
        treatmentArea.setPrefRowCount(3);
        treatmentArea.setStyle("-fx-font-family: monospace;");

        TextArea preventionArea = new TextArea("Prevention:\n" + result.getPrevention());
        preventionArea.setEditable(false);
        preventionArea.setWrapText(true);
        preventionArea.setPrefRowCount(2);
        preventionArea.setStyle("-fx-font-family: monospace;");

        // Warning if expert consult needed
        if (result.isNeedsExpertConsult()) {
            Label warningLabel = new Label("⚠️ Consultation d'expert recommandee");
            warningLabel.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
            content.getChildren().add(warningLabel);
        }

        content.getChildren().addAll(conditionLabel, confLabel,
                new Separator(), symptomsArea, treatmentArea, preventionArea);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(600, 500);

        // THE ONLY REAL FIX: force layout pass when window is shown
        dialog.setOnShown(e -> {
            dialog.getDialogPane().requestLayout();
            dialog.getDialogPane().layout();
        });

        // Save to analysis button
        ButtonType saveBtn = new ButtonType("Sauvegarder Resultat", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CLOSE);

        Optional<ButtonType> resultOpt = dialog.showAndWait();
        if (resultOpt.isPresent() && resultOpt.get() == saveBtn) {
            try {
                // Update the analysis with AI diagnosis
                String aiResult = "=== Diagnostic IA ===\n" +
                        "Condition: " + result.getCondition() + "\n" +
                        "Confiance: " + result.getConfidence().getLabel() + "\n\n" +
                        "Symptomes: " + result.getSymptoms() + "\n\n" +
                        "Traitement: " + result.getTreatment() + "\n\n" +
                        "Prevention: " + result.getPrevention() + "\n\n" +
                        "Urgence: " + result.getUrgency();

                // Save AI diagnosis result
                analyseService.updateAIDiagnosis(analyseId, aiResult,
                        result.getConfidence().getLabel(), "vision");

                tn.esprit.farmai.utils.NavigationUtil.showSuccess("Succes",
                        "Resultat IA sauvegarde dans l'analyse #" + analyseId);
                loadData();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to save AI diagnosis", e);
                tn.esprit.farmai.utils.NavigationUtil.showError("Erreur",
                        "Impossible de sauvegarder le diagnostic: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        tn.esprit.farmai.utils.NavigationUtil.logout(stage);
    }

    @FXML
    private void navigateToDashboard() {
        try {
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/farmai/views/expert-dashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 800);

            java.net.URL cssUrl = getClass().getResource("/tn/esprit/farmai/styles/dashboard.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("FarmAI - Tableau de Bord Expert");
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to dashboard", e);
        }
    }

    /**
     * Resolve image path from database (Symfony format) to local file path.
     * Handles:
     * - Absolute paths (C:\...)
     * - Relative paths (/uploads/...)
     * - URLs (http://...)
     */
    private String resolveImagePath(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return imageUrl;
        }

        // Already a URL or absolute path
        if (imageUrl.startsWith("http") || (imageUrl.length() >= 3 && imageUrl.charAt(1) == ':')) {
            return imageUrl;
        }

        // Symfony relative path like /uploads/analyses/image.jpg
        if (imageUrl.startsWith("/")) {
            // The Symfony project public folder is at the parent of sprint_java_farmai
            // Path: [INTEG]/public/uploads/...
            String basePath = System.getProperty("user.dir");
            int lastIndex = basePath.lastIndexOf(File.separator);
            if (lastIndex > 0) {
                String parentPath = basePath.substring(0, lastIndex);
                // Add 'public' folder between parent path and image path
                String publicPath = parentPath + File.separator + "public";
                return publicPath + imageUrl.replace("/", File.separator);
            }
            // Fallback
            return System.getProperty("user.dir") + File.separator + "public" + imageUrl.replace("/", File.separator);
        }

        // Plain filename - try current directory first
        return imageUrl;
    }
}