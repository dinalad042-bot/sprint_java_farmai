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
import javafx.stage.Stage;
import tn.esprit.farmai.models.Analyse;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.AnalyseService;
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

public class FermierAnalysesController implements Initializable {

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

    private final AnalyseService analyseService;
    private ObservableList<Analyse> analysesList;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public FermierAnalysesController() {
        this.analyseService = new AnalyseService();
        this.analysesList = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableView();
        loadAnalysesForFermier();
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

    private void loadAnalysesForFermier() {
        try {
            analysesList.clear();

            // On récupère le current user pour avoir l'id du fermier
            User currentUser = SessionManager.getInstance().getCurrentUser();
            int farmId = 1; // Fallback pour dev
            if (currentUser != null) {
                // Gérer la liaison User <-> Ferme si elle existe, sinon utiliser son ID
                // temporairement
                farmId = currentUser.getIdUser();
            }

            List<Analyse> analyses = analyseService.findByFerme(farmId);

            if (analyses.isEmpty()) {
                // Silent fail on load or log it
            }
            analysesList.addAll(analyses);
            analysesTableView.setItems(analysesList);

        } catch (SQLException e) {
            e.printStackTrace();
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

}
