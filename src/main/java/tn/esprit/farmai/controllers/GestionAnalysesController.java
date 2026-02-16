package tn.esprit.farmai.controllers;

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
import tn.esprit.farmai.models.Analyse;
import tn.esprit.farmai.services.AnalyseService;
import tn.esprit.farmai.utils.AnalyseDialog;
import tn.esprit.farmai.utils.NavigationUtil;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for managing analyses (US2 - Consultation).
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
    private Button backButton;
    @FXML
    private Label totalAnalysesLabel;

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label userRoleLabel;

    private final AnalyseService analyseService;
    private ObservableList<Analyse> analysesList;

    public GestionAnalysesController() {
        this.analyseService = new AnalyseService();
        this.analysesList = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableView();
        setupSearch();
        setupDoubleClick();
        loadAnalyses();
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
     * Configure the TableView columns
     */
    private void setupTableView() {
        // ID Column
        colId.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getIdAnalyse()));

        // Date Column
        colDate.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDateAnalyse().format(formatter));
        });

        // Resultat Column
        colResultat.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getResultatTechnique()));

        // Technicien Column
        colTechnicien.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getIdTechnicien()));

        // Ferme Column
        colFerme.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getIdFerme()));

        // Image Column - Display thumbnail
        colImage.setCellFactory(param -> new TableCell<Analyse, String>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(60);
                imageView.setFitHeight(40);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Analyse analyse = getTableRow().getItem();
                    String imageUrl = analyse.getImageUrl();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        try {
                            String path = imageUrl;
                            if (!imageUrl.startsWith("http") && !imageUrl.startsWith("file:")) {
                                File file = new File(imageUrl);
                                if (file.exists()) {
                                    path = file.toURI().toString();
                                }
                            }
                            imageView.setImage(new Image(path, true));
                            setGraphic(imageView);
                        } catch (Exception e) {
                            setGraphic(new Label("Pas d'image"));
                        }
                    } else {
                        setGraphic(new Label("Pas d'image"));
                    }
                }
            }
        });

        // Actions Column - Edit and Delete buttons
        colActions.setCellFactory(param -> new TableCell<Analyse, Void>() {
            private final Button editBtn = new Button("✎");
            private final Button deleteBtn = new Button("🗑");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);

            {
                pane.setAlignment(Pos.CENTER);
                editBtn.getStyleClass().add("action-btn");
                deleteBtn.getStyleClass().add("danger-btn");

                editBtn.setOnAction(event -> {
                    Analyse analyse = getTableView().getItems().get(getIndex());
                    handleEditAnalyse(analyse);
                });

                deleteBtn.setOnAction(event -> {
                    Analyse analyse = getTableView().getItems().get(getIndex());
                    handleDeleteAnalyse(analyse);
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
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAnalyses());
        }
    }

    /**
     * Load all analyses from database
     */
    @FXML
    private void loadAnalyses() {
        try {
            analysesList.clear();
            analysesList.addAll(analyseService.selectAll());
            analysesTableView.setItems(analysesList);
            updateTotalLabel();
        } catch (SQLException e) {
            NavigationUtil.showError("Erreur", "Impossible de charger les analyses: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Filter analyses based on search text
     */
    private void filterAnalyses() {
        String searchText = searchField != null ? searchField.getText().toLowerCase() : "";
        try {
            ObservableList<Analyse> filteredList = FXCollections.observableArrayList();
            for (Analyse analyse : analysesList) {
                boolean matchesSearch = searchText.isEmpty() ||
                    analyse.getResultatTechnique().toLowerCase().contains(searchText) ||
                    String.valueOf(analyse.getIdAnalyse()).contains(searchText) ||
                    String.valueOf(analyse.getIdTechnicien()).contains(searchText);
                if (matchesSearch) {
                    filteredList.add(analyse);
                }
            }
            analysesTableView.setItems(filteredList);
            updateTotalLabel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle refresh button
     */
    @FXML
    private void handleRefresh() {
        loadAnalyses();
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
     * Navigate to a view with smooth fade transition
     */
    private void navigateWithFade(String fxmlPath, String title) {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) backButton.getScene().getWindow();
            javafx.scene.Parent currentRoot = backButton.getScene().getRoot();

            // Fade out
            javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(200), currentRoot);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(event -> {
                try {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource(fxmlPath));
                    javafx.scene.Parent newRoot = loader.load();
                    javafx.scene.Scene scene = new javafx.scene.Scene(newRoot, 1200, 800);

                    String cssPath = getClass().getResource("/tn/esprit/farmai/styles/dashboard.css") != null
                            ? getClass().getResource("/tn/esprit/farmai/styles/dashboard.css").toExternalForm()
                            : null;
                    if (cssPath != null) {
                        scene.getStylesheets().add(cssPath);
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
     * Handle add analyse button - open dialog
     */
    @FXML
    private void handleAddAnalyse() {
        AnalyseDialog dialog = new AnalyseDialog();
        Optional<Analyse> result = dialog.showAndWait(analysesTableView.getScene().getWindow());

        result.ifPresent(analyse -> {
            try {
                analyseService.insertOne(analyse);
                analysesList.add(analyse);
                updateTotalLabel();
                NavigationUtil.showSuccess("Succès", "Analyse créée avec succès!\nID: " + analyse.getIdAnalyse());
            } catch (SQLException e) {
                NavigationUtil.showError("Erreur", "Impossible de créer l'analyse: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Handle edit analyse - open dialog with pre-filled data
     */
    private void handleEditAnalyse(Analyse analyse) {
        AnalyseDialog dialog = new AnalyseDialog(analyse);
        Optional<Analyse> result = dialog.showAndWait(analysesTableView.getScene().getWindow());

        result.ifPresent(updatedAnalyse -> {
            try {
                analyseService.updateOne(updatedAnalyse);
                // Refresh the list
                int index = analysesList.indexOf(analyse);
                if (index >= 0) {
                    analysesList.set(index, updatedAnalyse);
                }
                NavigationUtil.showSuccess("Succès", "Analyse modifiée avec succès!");
            } catch (SQLException e) {
                NavigationUtil.showError("Erreur", "Impossible de modifier l'analyse: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Handle delete analyse
     */
    private void handleDeleteAnalyse(Analyse analyse) {
        Optional<ButtonType> result = NavigationUtil.showConfirmation(
            "Confirmer la suppression",
            "Êtes-vous sûr de vouloir supprimer l'analyse ID " + analyse.getIdAnalyse() + " ?"
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                analyseService.deleteOne(analyse.getIdAnalyse());
                analysesList.remove(analyse);
                updateTotalLabel();
                NavigationUtil.showSuccess("Succès", "Analyse supprimée avec succès.");
            } catch (SQLException e) {
                NavigationUtil.showError("Erreur", "Impossible de supprimer l'analyse: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Update total analyses label
     */
    private void updateTotalLabel() {
        if (totalAnalysesLabel != null) {
            totalAnalysesLabel.setText("Total: " + analysesTableView.getItems().size() + " analyse(s)");
        }
    }
}
