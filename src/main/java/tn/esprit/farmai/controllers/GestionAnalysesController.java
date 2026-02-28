package tn.esprit.farmai.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.farmai.models.Analyse;
import tn.esprit.farmai.services.AnalyseService;
import tn.esprit.farmai.utils.AnalyseDialog;
import tn.esprit.farmai.utils.NavigationUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
    private Button exportPdfButton;
    @FXML
    private Button backButton;
    @FXML
    private Label totalAnalysesLabel;

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label userRoleLabel;

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
            tipLabel
        );
        
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
                    Runtime.getRuntime().exec(new String[]{"open", "-R", filePath});
                } else {
                    Runtime.getRuntime().exec(new String[]{"xdg-open", parentDir.getAbsolutePath()});
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
        
        colId.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getIdAnalyse()));
        colId.setCellFactory(col -> new TableCell<Analyse, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(String.valueOf(item));
                    label.setStyle("-fx-text-fill: black; -fx-font-size: 13px; -fx-font-family: 'Segoe UI'; -fx-padding: 8px;");
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
                    label.setStyle("-fx-text-fill: black; -fx-font-size: 13px; -fx-font-family: 'Segoe UI'; -fx-padding: 8px;");
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
                    label.setStyle("-fx-text-fill: black; -fx-font-size: 13px; -fx-font-family: 'Segoe UI'; -fx-padding: 8px;");
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
                    label.setStyle("-fx-text-fill: black; -fx-font-size: 13px; -fx-font-family: 'Segoe UI'; -fx-padding: 8px;");
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
                    label.setStyle("-fx-text-fill: black; -fx-font-size: 13px; -fx-font-family: 'Segoe UI'; -fx-padding: 8px;");
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
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Del");
            private final Button viewButton = new Button("View");
            private final HBox pane = new HBox(5, editButton, viewButton, deleteButton);

            {
                pane.setAlignment(Pos.CENTER);
                pane.setStyle("-fx-padding: 5px;");
                
                String btnStyle = "-fx-font-size: 11px; -fx-padding: 4px 8px; -fx-background-radius: 4px;";
                editButton.setStyle("-fx-background-color: #E3F2FD; -fx-text-fill: #1976D2; " + btnStyle);
                viewButton.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; " + btnStyle);
                deleteButton.setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #D32F2F; " + btnStyle);
                
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

        technicienFilterComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
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
            Platform.runLater(() -> showError("Unexpected Error", "An error occurred while loading data: " + e.getMessage()));
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
        // Railway Track: View-only dialog for Analyse entity
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("View Analysis");
        alert.setHeaderText("Analysis ID: " + analyse.getIdAnalyse());
        
        StringBuilder content = new StringBuilder();
        content.append("Date: ").append(analyse.getDateAnalyse().format(DATE_FORMATTER)).append("\n\n");
        content.append("Technical Result:\n").append(analyse.getResultatTechnique()).append("\n\n");
        content.append("Technician ID: ").append(analyse.getIdTechnicien()).append("\n");
        content.append("Farm ID: ").append(analyse.getIdFerme()).append("\n");
        content.append("Image: ").append(analyse.getImageUrl() != null ? analyse.getImageUrl() : "No image");
        
        alert.setContentText(content.toString());
        alert.getDialogPane().setExpandableContent(new TextArea(analyse.getResultatTechnique()));
        alert.showAndWait();
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

    // Utility methods
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}