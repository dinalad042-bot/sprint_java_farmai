package tn.esprit.farmai.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import tn.esprit.farmai.models.Ferme;
import tn.esprit.farmai.models.Animaux;
import tn.esprit.farmai.models.Plantes;
import tn.esprit.farmai.services.ServiceAnimaux;
import tn.esprit.farmai.services.ServiceFerme;
import tn.esprit.farmai.services.ServicePlantes;
import tn.esprit.farmai.utils.NavigationUtil;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminMapController implements Initializable {

    @FXML private WebView mapWebView;
    @FXML private AnchorPane paneDetails;
    @FXML private Label lblNomFerme;
    @FXML private ListView<String> lvAnimaux;
    @FXML private ListView<String> lvPlantes;

    private WebEngine webEngine;

    // Services
    private final ServiceFerme sf = new ServiceFerme();
    private final ServiceAnimaux sa = new ServiceAnimaux();
    private final ServicePlantes sp = new ServicePlantes();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webEngine = mapWebView.getEngine();

        // Injection Leaflet.js
        String htmlContent = "<!DOCTYPE html>" +
                "<html><head>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css' />" +
                "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                "<style>" +
                "  #map { height: 100vh; width: 100%; margin: 0; padding: 0; }" +
                "  body { margin: 0; overflow: hidden; }" +
                "</style>" +
                "</head><body>" +
                "<div id='map'></div>" +
                "<script>" +
                "  var map = L.map('map').setView([33.8869, 9.5375], 7);" +
                "  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);" +
                "  function addFermeByAddress(address, nom, id) {" +
                "    fetch('https://nominatim.openstreetmap.org/search?format=json&q=' + encodeURIComponent(address))" +
                "      .then(res => res.json()).then(data => {" +
                "        if (data.length > 0) {" +
                "          var marker = L.marker([data[0].lat, data[0].lon]).addTo(map);" +
                "          marker.bindTooltip('<b>' + nom + '</b>');" +
                "          marker.on('click', () => javaApp.onMarkerClick(id, nom));" +
                "        }" +
                "      }).catch(e => console.error('Erreur Geocoding:', e));" +
                "  }" +
                "</script></body></html>";

        webEngine.loadContent(htmlContent);

        // Passerelle Java -> JavaScript
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState.toString().equals("SUCCEEDED")) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaApp", this);
                chargerFermesSurCarte();
            }
        });
    }

    private void chargerFermesSurCarte() {
        try {
            List<Ferme> fermes = sf.selectALL();
            for (Ferme f : fermes) {
                if (f.getLieu() != null && !f.getLieu().isEmpty()) {
                    webEngine.executeScript(String.format("addFermeByAddress('%s', '%s', %d)",
                            f.getLieu().replace("'", "\\'"),
                            f.getNom_ferme().replace("'", "\\'"),
                            f.getId_ferme()));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Méthode appelée par JavaScript lors du clic sur un marqueur
     */
    public void onMarkerClick(int idFerme, String nomFerme) {
        Platform.runLater(() -> {
            lblNomFerme.setText(nomFerme);
            paneDetails.setVisible(true);
            paneDetails.setManaged(true);

            try {
                // Filtrage Animaux
                lvAnimaux.getItems().clear();
                List<Animaux> animauxFerme = sa.selectALL().stream()
                        .filter(a -> a.getId_ferme() == idFerme)
                        .collect(Collectors.toList());

                if (animauxFerme.isEmpty()) {
                    lvAnimaux.getItems().add("Aucun animal");
                } else {
                    for (Animaux a : animauxFerme) {
                        lvAnimaux.getItems().add("🐄 " + a.getEspece() + " (" + a.getEtat_sante() + ")");
                    }
                }

                // Filtrage Plantes
                lvPlantes.getItems().clear();
                List<Plantes> plantesFerme = sp.selectALL().stream()
                        .filter(p -> p.getId_ferme() == idFerme)
                        .collect(Collectors.toList());

                if (plantesFerme.isEmpty()) {
                    lvPlantes.getItems().add("Aucune plante");
                } else {
                    for (Plantes p : plantesFerme) {
                        lvPlantes.getItems().add("🌱 " + p.getNom_espece() + " [" + p.getCycle_vie() + "]");
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void closeDetails() {
        paneDetails.setVisible(false);
        paneDetails.setManaged(false);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        // S'assure que le chemin correspond à votre admin-dashboard.fxml
        NavigationUtil.navigateTo(stage, "/tn/esprit/farmai/views/admin-dashboard.fxml", "Admin Dashboard");
    }
}