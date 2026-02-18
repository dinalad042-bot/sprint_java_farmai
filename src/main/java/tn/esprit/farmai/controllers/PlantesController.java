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
import tn.esprit.farmai.services.IrrigationAI;
import tn.esprit.farmai.services.ServiceFerme;
import tn.esprit.farmai.services.ServicePlantes;
import tn.esprit.farmai.services.WeatherService;
import tn.esprit.farmai.utils.NavigationUtil;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.function.Function;

public class PlantesController implements Initializable {

    // FXML Gestion
    @FXML private TextField tfNomEspece;
    @FXML private TextField tfCycleVie;
    @FXML private ComboBox<Ferme> cbFerme;
    @FXML private TextField tfRecherche;
    @FXML private TableView<Plantes> tvPlantes;
    @FXML private TableColumn<Plantes, String> colNom;
    @FXML private TableColumn<Plantes, String> colCycle;
    @FXML private TableColumn<Plantes, Integer> colFerme;

    // FXML IA Advisor
    @FXML private TextField tfVilleIA;
    @FXML private Label lblMeteo;
    @FXML private Label lblConseilIA;
    @FXML private VBox paneResultatIA;

    // Services
    private final ServicePlantes sp = new ServicePlantes();
    private final ServiceFerme sf = new ServiceFerme();
    private final WeatherService weatherService = new WeatherService();
    private final IrrigationAI irrigationAI = new IrrigationAI();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup TableView
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom_espece"));
        colCycle.setCellValueFactory(new PropertyValueFactory<>("cycle_vie"));
        colFerme.setCellValueFactory(new PropertyValueFactory<>("id_ferme"));

        chargerFermes();
        rafraichir();

        // Listener de sélection
        tvPlantes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                tfNomEspece.setText(newVal.getNom_espece());
                tfCycleVie.setText(newVal.getCycle_vie());
                cbFerme.getItems().stream()
                        .filter(f -> f.getId_ferme() == newVal.getId_ferme())
                        .findFirst().ifPresent(f -> {
                            cbFerme.setValue(f);
                            // Optionnel : remplir automatiquement la ville IA
                            tfVilleIA.setText(f.getLieu());
                        });
            }
        });
    }

    // --- LOGIQUE IA IRRIGATION ---

    @FXML
    private void analyserIrrigationIA() {
        String ville = tfVilleIA.getText().trim();
        String planteActuelle = tfNomEspece.getText().isEmpty() ? "votre culture" : tfNomEspece.getText();

        if (ville.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Veuillez entrer une ville pour l'IA.").show();
            return;
        }

        paneResultatIA.setVisible(true);
        lblConseilIA.setText("⏳ Analyse des données en cours...");

        new Thread(() -> {
            JSONObject data = weatherService.getWeatherData(ville);
            Platform.runLater(() -> {
                if (data != null && data.has("main")) {
                    double temp = data.getJSONObject("main").getDouble("temp");
                    int hum = data.getJSONObject("main").getInt("humidity");
                    lblMeteo.setText(String.format("🌡️ %.1f°C | 💧 %d%% Humidité", temp, hum));

                    String conseil = irrigationAI.getRecommendation(data, planteActuelle);
                    lblConseilIA.setText(conseil);
                } else {
                    lblConseilIA.setText("❌ Erreur : Impossible de récupérer la météo.");
                }
            });
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
            sp.insertOne(new Plantes(0, tfNomEspece.getText(), tfCycleVie.getText(), cbFerme.getValue().getId_ferme()));
            rafraichir();
            viderChamps();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void modifier() {
        Plantes selected = tvPlantes.getSelectionModel().getSelectedItem();
        if (selected != null && validerChamps()) {
            try {
                selected.setNom_espece(tfNomEspece.getText());
                selected.setCycle_vie(tfCycleVie.getText());
                selected.setId_ferme(cbFerme.getValue().getId_ferme());
                sp.updateOne(selected);
                rafraichir();
                viderChamps();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void supprimer() {
        Plantes selected = tvPlantes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                sp.deleteOne(selected);
                rafraichir();
                viderChamps();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void rafraichir() {
        try { tvPlantes.setItems(FXCollections.observableArrayList(sp.selectALL())); }
        catch (SQLException e) { e.printStackTrace(); }
    }

    private boolean validerChamps() {
        return !tfNomEspece.getText().isEmpty() && cbFerme.getValue() != null;
    }

    private void viderChamps() {
        tfNomEspece.clear(); tfCycleVie.clear();
        cbFerme.getSelectionModel().clearSelection();
        tvPlantes.getSelectionModel().clearSelection();
        paneResultatIA.setVisible(false);
    }

    @FXML
    private void handleReturnToSelection(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/tn/esprit/farmai/views/selection-gestion.fxml", "Gestion");
    }

    @FXML
    private void imprimerPdf() {
        String[] headers = {"Espèce", "Cycle de Vie", "ID Ferme"};
        Function<Plantes, String>[] extractors = new Function[] {
                (Function<Plantes, String>) p -> p.getNom_espece(),
                (Function<Plantes, String>) p -> p.getCycle_vie(),
                (Function<Plantes, String>) p -> String.valueOf(p.getId_ferme())
        };
        try {
            tn.esprit.farmai.services.PdfGenerator.generatePdf("Rapport_Plantes.pdf", "Suivi FarmAI", tvPlantes.getItems(), headers, extractors);
            new Alert(Alert.AlertType.INFORMATION, "PDF généré !").show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}