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
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tn.esprit.farmai.models.Analyse;
import tn.esprit.farmai.models.Ferme;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.AnalyseService;
import tn.esprit.farmai.services.FermeService;
import tn.esprit.farmai.utils.NavigationUtil;
import tn.esprit.farmai.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FermierAnalysesController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(FermierAnalysesController.class.getName());

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
    private TableColumn<Analyse, String> colImage;
    @FXML
    private TableColumn<Analyse, Void> colActions;

    @FXML
    private Button backButton;
    @FXML
    private TextField searchField;
    
    // Sidebar elements
    @FXML
    private Circle sidebarAvatar;
    @FXML
    private Text sidebarAvatarText;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userRoleLabel;

    private final AnalyseService analyseService;
    private final FermeService fermeService;
    private ObservableList<Analyse> analysesList;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public FermierAnalysesController() {
        this.analyseService = new AnalyseService();
        this.fermeService = new FermeService();
        this.analysesList = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set user info in sidebar
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (userNameLabel != null) userNameLabel.setText(currentUser.getFullName());
            if (userRoleLabel != null) userRoleLabel.setText(currentUser.getRole().getDisplayName());
        }
        
        setupTableView();
        setupSearch();
        loadAnalysesForFermier();
    }
    
    /**
     * Setup search functionality
     */
    private void setupSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filterAnalyses(newVal);
            });
        }
    }
    
    /**
     * Filter analyses based on search text
     */
    private void filterAnalyses(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            analysesTableView.setItems(analysesList);
            return;
        }
        
        String search = searchText.toLowerCase().trim();
        ObservableList<Analyse> filteredList = FXCollections.observableArrayList();
        
        for (Analyse analyse : analysesList) {
            String resultat = analyse.getResultatTechnique();
            if (resultat != null && resultat.toLowerCase().contains(search)) {
                filteredList.add(analyse);
            } else if (String.valueOf(analyse.getIdAnalyse()).contains(search)) {
                filteredList.add(analyse);
            }
        }
        
        analysesTableView.setItems(filteredList);
    }

    private void setupTableView() {
        analysesTableView.setFixedCellSize(-1);

        colId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getIdAnalyse()));

        colDate.setCellValueFactory(cellData -> {
            try {
                LocalDateTime date = cellData.getValue().getDateAnalyse();
                return date == null ? new SimpleStringProperty("No date")
                        : new SimpleStringProperty(date.format(DATE_FORMATTER));
            } catch (Exception e) {
                return new SimpleStringProperty("Invalid date");
            }
        });

        colResultat.setCellValueFactory(cellData -> {
            String resultat = cellData.getValue().getResultatTechnique();
            return new SimpleStringProperty(resultat == null || resultat.trim().isEmpty() ? "No result" : resultat);
        });

        colTechnicien
                .setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getIdTechnicien()));

        colImage.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getImageUrl()));
        colImage.setCellFactory(col -> new TableCell<Analyse, String>() {
            @Override
            protected void updateItem(String imageUrl, boolean empty) {
                super.updateItem(imageUrl, empty);
                if (empty || imageUrl == null || imageUrl.trim().isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        ImageView imageView = new ImageView();
                        imageView.setFitWidth(40);
                        imageView.setFitHeight(30);
                        imageView.setPreserveRatio(true);

                        File imageFile = new File(imageUrl);
                        if (imageFile.exists()) {
                            imageView.setImage(new Image(imageFile.toURI().toString()));
                        } else {
                            imageView.setImage(new Image("file:" + imageUrl, true));
                        }
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(new Label("[IMG]"));
                    }
                }
            }
        });

        // Actions: View PDF
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button exportPdfButton = new Button("Export PDF");
            private final HBox pane = new HBox(5, exportPdfButton);

            {
                pane.setAlignment(Pos.CENTER);
                pane.setStyle("-fx-padding: 5px;");
                exportPdfButton.setStyle(
                        "-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-font-size: 11px; -fx-padding: 4px 8px; -fx-background-radius: 4px;");

                exportPdfButton.setOnAction(event -> {
                    Analyse analyse = getTableRow().getItem();
                    if (analyse != null) {
                        handleExportPDF(analyse);
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
     * Load analyses for the current fermier user.
     * Handles edge cases: user not logged in, no farm associated, no analyses found.
     */
    private void loadAnalysesForFermier() {
        try {
            analysesList.clear();

            // Step 1: Check if user is logged in
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                LOGGER.log(Level.WARNING, "No user session found");
                analysesTableView.setPlaceholder(new Label("Session expirée. Veuillez vous reconnecter."));
                NavigationUtil.showError("Session expirée", "Aucune session active. Veuillez vous reconnecter.");
                return;
            }

            LOGGER.log(Level.INFO, "Loading analyses for user: {0} (ID: {1})", 
                    new Object[]{currentUser.getFullName(), currentUser.getIdUser()});

            // Step 2: Find the farm associated with this fermier
            Ferme ferme = fermeService.findByFermier(currentUser.getIdUser());
            if (ferme == null) {
                LOGGER.log(Level.WARNING, "No farm found for user ID: {0}", currentUser.getIdUser());
                analysesTableView.setPlaceholder(new Label(
                    "Aucune ferme associée à votre compte.\n" +
                    "Veuillez contacter l'administrateur pour associer une ferme à votre profil."));
                return;
            }

            int farmId = ferme.getIdFerme();
            LOGGER.log(Level.INFO, "Farm found: {0} (ID: {1})", new Object[]{ferme.getNomFerme(), farmId});

            // Step 3: Load analyses for this farm
            List<Analyse> analyses = analyseService.findByFerme(farmId);
            LOGGER.log(Level.INFO, "Found {0} analyses for farm ID: {1}", new Object[]{analyses.size(), farmId});

            if (analyses.isEmpty()) {
                analysesTableView.setPlaceholder(new Label(
                    "Aucune analyse disponible pour votre ferme.\n" +
                    "Les analyses effectuées par l'expert apparaîtront ici."));
            } else {
                analysesList.addAll(analyses);
                analysesTableView.setItems(analysesList);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while loading analyses", e);
            analysesTableView.setPlaceholder(new Label(
                "Erreur de chargement des analyses.\n" +
                "Détails: " + (e.getMessage() != null ? e.getMessage() : "Erreur inconnue")));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error while loading analyses", e);
            analysesTableView.setPlaceholder(new Label(
                "Erreur inattendue.\n" +
                "Veuillez réessayer ou contacter le support."));
        }
    }

    private void handleExportPDF(Analyse analyse) {
        // Using the exact logic from GestionAnalysesController (US9)
        try {
            String pdfPath = analyseService.exportAnalysisToPDF(analyse.getIdAnalyse());
            NavigationUtil.showSuccess("PDF généré", "Fichier sauvegardé dans : " + pdfPath);
        } catch (Exception e) {
            NavigationUtil.showError("Erreur PDF", "Erreur lors de la génération : " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        NavigationUtil.navigateToAgricoleDashboard((Stage) backButton.getScene().getWindow());
    }

    /**
     * Handle logout
     */
    @FXML
    private void handleLogout() {
        Stage stage = (Stage) analysesTableView.getScene().getWindow();
        NavigationUtil.logout(stage);
    }
}
