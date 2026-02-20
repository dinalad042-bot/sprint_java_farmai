package tn.esprit.farmai.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.json.JSONObject;
import tn.esprit.farmai.models.Ferme;
import tn.esprit.farmai.models.Plantes;
import tn.esprit.farmai.services.*;
import tn.esprit.farmai.utils.NavigationUtil;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.function.Function;

public class PlantesController implements Initializable {

    // FXML Gestion CRUD
    @FXML private TextField tfNomEspece;
    @FXML private TextField tfCycleVie;
    @FXML private TextField tfQuantite;
    @FXML private ComboBox<Ferme> cbFerme;
    @FXML private TextField tfRecherche;
    @FXML private TableView<Plantes> tvPlantes;
    @FXML private TableColumn<Plantes, String> colNom;
    @FXML private TableColumn<Plantes, String> colCycle;
    @FXML private TableColumn<Plantes, Integer> colFerme;
    @FXML private TableColumn<Plantes, Double> colQuantite;

    // FXML IA Advisor
    @FXML private TextField tfVilleIA;
    @FXML private Label lblTemp;
    @FXML private Label lblHum;
    @FXML private Label lblAirQual;
    @FXML private Label lblConseilIA;
    @FXML private VBox paneResultatIA;

    // FXML Finance (Export)
    @FXML private TextField tfPrixLocal;
    @FXML private Label lblPrixExport;
    @FXML private Label lblPrixTotalExport;

    // Services
    private final ServicePlantes sp = new ServicePlantes();
    private final ServiceFerme sf = new ServiceFerme();
    private final WeatherService weatherService = new WeatherService();
    private final IrrigationAI irrigationAI = new IrrigationAI();
    private final MarketService marketService = new MarketService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom_espece"));
        colCycle.setCellValueFactory(new PropertyValueFactory<>("cycle_vie"));
        colFerme.setCellValueFactory(new PropertyValueFactory<>("id_ferme"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));

        chargerFermes();
        rafraichir();

        tvPlantes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                tfNomEspece.setText(newVal.getNom_espece());
                tfCycleVie.setText(newVal.getCycle_vie());
                tfQuantite.setText(String.valueOf(newVal.getQuantite()));

                cbFerme.getItems().stream()
                        .filter(f -> f.getId_ferme() == newVal.getId_ferme())
                        .findFirst().ifPresent(f -> {
                            cbFerme.setValue(f);
                            tfVilleIA.setText(f.getLieu());
                        });

                lblPrixExport.setText("Valeur Export de " + newVal.getNom_espece() + " : -- $");
                lblPrixExport.setStyle("-fx-text-fill: #546E7A; -fx-font-weight: normal;");
                lblPrixTotalExport.setText("Prêt pour le calcul du revenu total.");
            }
        });
    }

    // --- LOGIQUE IA IRRIGATION (CORRIGÉE) ---

    @FXML
    private void analyserIrrigationIA() {
        String ville = tfVilleIA.getText().trim();
        String planteActuelle = tfNomEspece.getText().isEmpty() ? "vos cultures" : tfNomEspece.getText();

        if (ville.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Veuillez entrer une ville.").show();
            return;
        }

        paneResultatIA.setVisible(true);
        lblConseilIA.setText("⏳ Analyse en cours...");

        new Thread(() -> {
            try {
                // Appel au service météo (opération bloquante dans le thread)
                JSONObject data = weatherService.getWeatherData(ville);

                Platform.runLater(() -> {
                    if (data != null && data.has("main")) {
                        double temp = data.getJSONObject("main").getDouble("temp");
                        int hum = data.getJSONObject("main").getInt("humidity");

                        lblTemp.setText(String.format("%.1f°C", temp));
                        lblHum.setText(hum + "%");

                        // Récupération du conseil via le service IA
                        String conseil = irrigationAI.getRecommendation(data, planteActuelle);
                        lblConseilIA.setText(conseil);

                        // Gestion de la qualité de l'air
                        int aqi = data.optInt("air_quality_index", 1);
                        updateAirQualityUI(aqi);

                        // Mise à jour du style selon l'urgence
                        if (temp < 2 || temp > 38 || aqi >= 4) {
                            lblConseilIA.setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #B71C1C; -fx-padding: 12; -fx-background-radius: 10; -fx-border-color: #EF9A9A;");
                        } else {
                            lblConseilIA.setStyle("-fx-background-color: white; -fx-text-fill: #2E7D32; -fx-padding: 12; -fx-background-radius: 10; -fx-border-color: #C8E6C9;");
                        }

                    } else {
                        lblConseilIA.setText("❌ Erreur : Ville introuvable ou problème API.");
                    }
                });
            } catch (Exception e) {
                // Débloque l'UI en cas d'exception (Time-out, erreur JSON, etc.)
                Platform.runLater(() -> lblConseilIA.setText("❌ Erreur lors de la récupération des données."));
                e.printStackTrace();
            }
        }).start();
    }

    private void updateAirQualityUI(int aqi) {
        String status; String color;
        switch (aqi) {
            case 1 -> { status = "Excellente"; color = "#2E7D32"; }
            case 2 -> { status = "Bonne"; color = "#827717"; }
            case 3 -> { status = "Modérée"; color = "#F57F17"; }
            case 4 -> { status = "Médiocre"; color = "#E64A19"; }
            case 5 -> { status = "Critique"; color = "#B71C1C"; }
            default -> { status = "Inconnue"; color = "#607D8B"; }
        }
        lblAirQual.setText(status);
        lblAirQual.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }

    // --- LOGIQUE FINANCE & EXPORT ---

    @FXML
    private void calculerExport() {
        Plantes planteSelectionnee = tvPlantes.getSelectionModel().getSelectedItem();
        if (planteSelectionnee == null) {
            new Alert(Alert.AlertType.WARNING, "Sélectionnez une plante !").show();
            return;
        }

        String prixTxt = tfPrixLocal.getText();
        if (prixTxt.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Entrez un prix en TND.").show();
            return;
        }

        new Thread(() -> {
            try {
                double taux = marketService.getExchangeRate("USD");
                double prixUnitaireTND = Double.parseDouble(prixTxt);
                double prixUnitaireUSD = prixUnitaireTND * taux;
                double totalGlobalUSD = prixUnitaireUSD * planteSelectionnee.getQuantite();

                Platform.runLater(() -> {
                    lblPrixExport.setText(String.format("Valeur : %.2f $ / Kg", prixUnitaireUSD));
                    lblPrixTotalExport.setText(String.format("Revenu Total (%s) : %.2f $",
                            planteSelectionnee.getNom_espece(), totalGlobalUSD));

                    if (taux > 0.32) {
                        lblPrixTotalExport.setStyle("-fx-text-fill: #1B5E20; -fx-font-weight: bold;");
                        lblPrixTotalExport.setText(lblPrixTotalExport.getText() + " 🚀");
                    } else {
                        lblPrixTotalExport.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblPrixTotalExport.setText("❌ Erreur calcul"));
            }
        }).start();
    }

    // --- LOGIQUE GESTION CRUD ---

    private void chargerFermes() {
        try {
            cbFerme.setItems(FXCollections.observableArrayList(sf.selectALL()));
            cbFerme.setConverter(new StringConverter<Ferme>() {
                @Override public String toString(Ferme f) { return (f != null) ? f.getNom_ferme() : ""; }
                @Override public Ferme fromString(String s) { return null; }
            });
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleRecherche() {
        String query = tfRecherche.getText().trim();
        try {
            if (query.isEmpty()) rafraichir();
            else tvPlantes.setItems(FXCollections.observableArrayList(sp.chercherParNom(query)));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void ajouter() {
        if (!validerChamps()) return;
        try {
            double qte = Double.parseDouble(tfQuantite.getText());
            sp.insertOne(new Plantes(0, tfNomEspece.getText(), tfCycleVie.getText(), cbFerme.getValue().getId_ferme(), qte));
            rafraichir(); viderChamps();
        } catch (SQLException | NumberFormatException e) { e.printStackTrace(); }
    }

    @FXML
    private void modifier() {
        Plantes selected = tvPlantes.getSelectionModel().getSelectedItem();
        if (selected != null && validerChamps()) {
            try {
                selected.setNom_espece(tfNomEspece.getText());
                selected.setCycle_vie(tfCycleVie.getText());
                selected.setId_ferme(cbFerme.getValue().getId_ferme());
                selected.setQuantite(Double.parseDouble(tfQuantite.getText()));
                sp.updateOne(selected);
                rafraichir(); viderChamps();
                tvPlantes.refresh();
            } catch (SQLException | NumberFormatException e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void supprimer() {
        Plantes selected = tvPlantes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                sp.deleteOne(selected);
                rafraichir(); viderChamps();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void rafraichir() {
        try { tvPlantes.setItems(FXCollections.observableArrayList(sp.selectALL())); }
        catch (SQLException e) { e.printStackTrace(); }
    }

    private boolean validerChamps() {
        if (tfNomEspece.getText().isEmpty() || tfQuantite.getText().isEmpty() || cbFerme.getValue() == null) {
            new Alert(Alert.AlertType.ERROR, "Champs obligatoires manquants.").show();
            return false;
        }
        return true;
    }

    private void viderChamps() {
        tfNomEspece.clear(); tfCycleVie.clear(); tfQuantite.clear();
        cbFerme.getSelectionModel().clearSelection();
        tvPlantes.getSelectionModel().clearSelection();
        paneResultatIA.setVisible(false);
        tfPrixLocal.clear();
        lblPrixExport.setText("Valeur : -- $");
        lblPrixTotalExport.setText("Revenu : -- $");
    }

    @FXML
    private void handleReturnToSelection(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/tn/esprit/farmai/views/selection-gestion.fxml", "Gestion");
    }

    @FXML
    private void imprimerPdf() {
        String[] headers = {"Espèce", "Cycle", "Quantité (Kg)", "Ferme"};
        Function<Plantes, String>[] extractors = new Function[] {
                (Function<Plantes, String>) p -> p.getNom_espece(),
                (Function<Plantes, String>) p -> p.getCycle_vie(),
                (Function<Plantes, String>) p -> String.valueOf(p.getQuantite()),
                (Function<Plantes, String>) p -> String.valueOf(p.getId_ferme())
        };
        try {
            tn.esprit.farmai.services.PdfGenerator.generatePdf("Rapport_Plantes.pdf", "Suivi FarmAI", tvPlantes.getItems(), headers, extractors);
            new Alert(Alert.AlertType.INFORMATION, "PDF généré !").show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}