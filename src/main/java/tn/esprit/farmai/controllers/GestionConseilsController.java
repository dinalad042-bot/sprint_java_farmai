
package tn.esprit.farmai.controllers;

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
import javafx.stage.Stage;
import tn.esprit.farmai.models.Conseil;
import tn.esprit.farmai.models.Priorite;
import tn.esprit.farmai.services.AnalyseService;
import tn.esprit.farmai.services.ConseilService;
import tn.esprit.farmai.utils.NavigationUtil;

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
    }

    /**
     * Setup TableView columns with CellValueFactory bindings
     */
    private void setupTableView() {
        // ID Column
        colId.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getIdConseil()));

        // Description Column with text wrapping
        colDescription.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
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
        colPriorite.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getPriorite()));
        colPriorite.setCellFactory(column -> new TableCell<Conseil, Priorite>() {
            @Override
            protected void updateItem(Priorite priorite, boolean empty) {
                super.updateItem(priorite, empty);
                if (empty || priorite == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String emoji = switch (priorite) {
                        case BASSE -> "🟢";
                        case MOYENNE -> "🟡";
                        case HAUTE -> "🔴";
                    };
                    setText(emoji + " " + priorite.getLabel());
                }
            }
        });

        // Analyse ID Column (FK) - Shows the related analysis
        colAnalyseId.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getIdAnalyse()));

        // Actions Column (Edit/Delete)
        colActions.setCellFactory(param -> new TableCell<Conseil, Void>() {
            private final Button editBtn = new Button("✎");
            private final Button deleteBtn = new Button("🗑");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);

            {
                pane.setAlignment(Pos.CENTER);
                editBtn.getStyleClass().add("action-btn");
                deleteBtn.getStyleClass().add("danger-btn");
                editBtn.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-padding: 5px 10px;");
                deleteBtn.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-padding: 5px 10px;");

                editBtn.setOnAction(event -> {
                    Conseil conseil = getTableView().getItems().get(getIndex());
                    handleEditConseil(conseil);
                });

                deleteBtn.setOnAction(event -> {
                    Conseil conseil = getTableView().getItems().get(getIndex());
                    handleDeleteConseil(conseil);
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
            conseilsList.addAll(conseilService.selectAll());
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
            "Êtes-vous sûr de vouloir supprimer le conseil ID " + conseil.getIdConseil() + " ?"
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                conseilService.deleteOne(conseil.getIdConseil());
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

                    java.net.URL cssUrl = getClass().getResource("/tn/esprit/farmai/styles/dashboard.css");
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
}
