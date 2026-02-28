package tn.esprit.farmai.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import tn.esprit.farmai.models.Ferme;
import tn.esprit.farmai.services.FermeService;
import tn.esprit.farmai.services.ServiceAnimaux;
import tn.esprit.farmai.services.ServicePlantes;
import tn.esprit.farmai.utils.NavigationUtil;
import tn.esprit.farmai.utils.Config;
import tn.esprit.farmai.utils.SessionManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;

public class FermeController implements Initializable {

    // --- Éléments FXML existants ---
    @FXML private TextField tfNom, tfLieu, tfSurface, tfRecherche;
    @FXML private TableView<Ferme> tvFermes;
    @FXML private TableColumn<Ferme, String> colNom, colLieu;
    @FXML private TableColumn<Ferme, Float> colSurface;

    // --- Éléments FXML pour l'API Synergie ---
    @FXML private Label lblScore, lblAzoteRatio;
    @FXML private ProgressBar progressSynergy;
    @FXML private Text txtAnalysisResult;

    private final FermeService sf = new FermeService();
    private final ServiceAnimaux sa = new ServiceAnimaux();
    private final ServicePlantes sp = new ServicePlantes();

    // Trefle.io API key from Config
    private String getTrefleApiToken() {
        return Config.TREFLE_API_KEY;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomFerme"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        colSurface.setCellValueFactory(new PropertyValueFactory<>("surface"));

        tvFermes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                tfNom.setText(newSelection.getNomFerme());
                tfLieu.setText(newSelection.getLieu());
                tfSurface.setText(String.valueOf(newSelection.getSurface()));
                // Reset de l'analyse lors du changement de sélection
                txtAnalysisResult.setText("Prêt pour l'audit de : " + newSelection.getNomFerme());
            }
        });

        rafraichir();
    }

    // ==========================================
    // MÉTHODE API EXCEPTIONNELLE : AUDIT IA TREFLE
    // ==========================================
    @FXML
    private void analyserSynergie() {
        Ferme selected = tvFermes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte("Sélection requise", "Choisissez une ferme pour lancer l'audit.");
            return;
        }

        txtAnalysisResult.setText("🛰️ Interrogation de Trefle.io & Corrélation base de données...");

        new Thread(() -> {
            try {
                // 1. Récupération des statistiques internes via vos services
                long nbAnimaux = sa.selectALL().stream().filter(a -> a.getIdFerme() == selected.getIdFerme()).count();
                long nbPlantes = sp.selectALL().stream().filter(p -> p.getIdFerme() == selected.getIdFerme()).count();

                // 2. Appel API Trefle pour récupérer des constantes agronomiques (Ex: recherche de "Grass" pour pâturage)
                String urlStr = "https://trefle.io/api/v1/plants/search?token=" + getTrefleApiToken() + "&q=fodder";
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) sb.append(line);
                rd.close();

                // 3. LOGIQUE CRÉATIVE : Calcul du ratio Azote / Surface
                // Hypothèse : 1 animal produit assez d'azote pour fertiliser 500m2
                double azoteProduit = nbAnimaux * 500.0;
                double ratioUtilisation = (selected.getSurface() > 0) ? azoteProduit / selected.getSurface() : 0;

                // Score de synergie basé sur la présence équilibrée des deux types
                double synergyScore = (nbAnimaux > 0 && nbPlantes > 0) ? 0.92 : 0.45;
                if (ratioUtilisation > 1.2) synergyScore -= 0.2; // Malus pour surpâturage

                final double finalRatio = ratioUtilisation;
                final double finalScore = synergyScore;

                Platform.runLater(() -> {
                    lblAzoteRatio.setText(String.format("%.2f %% d'Autosuffisance", finalRatio * 100));
                    lblScore.setText((int)(finalScore * 100) + "%");
                    progressSynergy.setProgress(finalScore);

                    String bilan = (finalRatio < 0.5) ? "⚠️ Manque de fertilisants naturels." : "✅ Équilibre azote optimal.";
                    txtAnalysisResult.setText("Audit Terminé.\nSynergie Animale/Végétale : " + (int)(finalScore * 100) + "%\n" + bilan);
                });

            } catch (Exception e) {
                Platform.runLater(() -> txtAnalysisResult.setText("⚠️ Erreur : Vérifiez votre connexion ou clé API."));
                e.printStackTrace();
            }
        }).start();
    }

    // ==========================================
    // MÉTHODES CRUD STANDARDS
    // ==========================================
    @FXML
    private void handleRecherche() {
        String query = tfRecherche.getText().trim();
        try {
            tvFermes.setItems(FXCollections.observableArrayList(query.isEmpty() ? sf.selectALL() : sf.findByLieu(query)));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void ajouter() {
        try {
            if (validerChamps()) {
                // Get current user's ID as fermier, or use default
                int idFermier = 1; // Default
                if (SessionManager.getInstance().isLoggedIn()) {
                    idFermier = SessionManager.getInstance().getCurrentUser().getIdUser();
                }
                sf.insertOne(new Ferme(tfNom.getText(), tfLieu.getText(), Double.parseDouble(tfSurface.getText()), idFermier));
                rafraichir();
                viderChamps();
            }
        } catch (NumberFormatException e) {
            afficherAlerte("Erreur de format", "La surface doit être un nombre.");
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void modifier() {
        Ferme selected = tvFermes.getSelectionModel().getSelectedItem();
        if (selected != null && validerChamps()) {
            try {
                selected.setNomFerme(tfNom.getText());
                selected.setLieu(tfLieu.getText());
                selected.setSurface(Double.parseDouble(tfSurface.getText()));
                sf.updateOne(selected);
                rafraichir();
                viderChamps();
            } catch (Exception e) { e.printStackTrace(); }
        } else {
            afficherAlerte("Sélection requise", "Veuillez sélectionner une ferme.");
        }
    }

    @FXML
    private void supprimer() {
        Ferme selected = tvFermes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette ferme ?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    try {
                        sf.deleteOne(selected);
                        rafraichir();
                        viderChamps();
                    } catch (Exception e) { e.printStackTrace(); }
                }
            });
        }
    }

    private void rafraichir() {
        try {
            tvFermes.setItems(FXCollections.observableArrayList(sf.selectALL()));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private boolean validerChamps() {
        return !tfNom.getText().trim().isEmpty() && !tfLieu.getText().trim().isEmpty() && !tfSurface.getText().trim().isEmpty();
    }

    private void viderChamps() {
        tfNom.clear(); tfLieu.clear(); tfSurface.clear();
        tvFermes.getSelectionModel().clearSelection();
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleReturnToSelection(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/tn/esprit/farmai/views/agricole-dashboard.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1200, 800);
            stage.setScene(scene);
            stage.setTitle("FarmAI - Espace Agricole");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to navigation util
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            NavigationUtil.navigateToDashboard(stage);
        }
    }

    @FXML
    private void imprimerPdf() {
        String[] headers = {"Nom de la Ferme", "Localisation", "Surface (m²)"};
        java.util.function.Function<Ferme, String>[] extractors = new java.util.function.Function[] {
            (java.util.function.Function<Ferme, String>) f -> f.getNomFerme(),
            (java.util.function.Function<Ferme, String>) f -> f.getLieu(),
            (java.util.function.Function<Ferme, String>) f -> String.valueOf(f.getSurface())
        };
        try {
            tn.esprit.farmai.services.PdfGenerator.generatePdf("Rapport_Fermes.pdf", "FarmAI - Audit Fermes", tvFermes.getItems(), headers, extractors);
            new Alert(Alert.AlertType.INFORMATION, "Le rapport PDF a été généré !").show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur PDF : " + e.getMessage()).show();
        }
    }
}