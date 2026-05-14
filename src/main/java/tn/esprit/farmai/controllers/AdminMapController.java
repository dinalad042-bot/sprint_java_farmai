package tn.esprit.farmai.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import netscape.javascript.JSObject;
import tn.esprit.farmai.models.Ferme;
import tn.esprit.farmai.services.ServiceAnimaux;
import tn.esprit.farmai.services.ServiceFerme;
import tn.esprit.farmai.services.ServicePlantes;
import tn.esprit.farmai.utils.NavigationUtil;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class AdminMapController implements Initializable {

    @FXML private WebView mapWebView;
    @FXML private AnchorPane paneDetails;
    @FXML private Label lblNomFerme;
    @FXML private ListView<String> lvAnimaux;
    @FXML private ListView<String> lvPlantes;
    @FXML private ImageView ivQRCode;

    private WebEngine webEngine;
    private final ServiceFerme sf = new ServiceFerme();
    private final ServiceAnimaux sa = new ServiceAnimaux();
    private final ServicePlantes sp = new ServicePlantes();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webEngine = mapWebView.getEngine();

        String htmlContent = "<!DOCTYPE html><html><head>" +
        "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css' />" +
        "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
        "<style>#map { height: 100vh; width: 100%; margin: 0; } body { margin: 0; }</style>" +
        "</head><body><div id='map'></div><script>" +
        " var map = L.map('map').setView([33.8869, 9.5375], 7);" +
        " L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);" +
        " window.addFermeByAddress = function(address, nom, id) {" +
        " fetch('https://nominatim.openstreetmap.org/search?format=json&q=' + encodeURIComponent(address))" +
        " .then(res => res.json()).then(data => {" +
        " if (data.length > 0) {" +
        " var marker = L.marker([data[0].lat, data[0].lon]).addTo(map);" +
        " marker.bindTooltip('<b>' + nom + '</b>');" +
        " marker.on('click', () => javaApp.onMarkerClick(id, nom, address));" +
        " }" +
        " });" +
        " };" +
        "</script></body></html>";

        webEngine.loadContent(htmlContent);

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState.toString().equals("SUCCEEDED")) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaApp", this);
                Timeline waitJS = new Timeline(new KeyFrame(Duration.millis(600), e -> chargerFermesSurCarte()));
                waitJS.play();
            }
        });
    }

    private void chargerFermesSurCarte() {
        try {
            List<Ferme> fermes = sf.selectALL();
            for (Ferme f : fermes) {
                if (f.getLieu() != null && !f.getLieu().isEmpty()) {
                    String script = String.format(
                        "if(typeof window.addFermeByAddress === 'function') { window.addFermeByAddress('%s', '%s', %d); }",
                        f.getLieu().replace("'", "\\\\'"),
                        f.getNomFerme().replace("'", "\\\\'"),
                        f.getIdFerme()
                    );
                    webEngine.executeScript(script);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void onMarkerClick(int idFerme, String nomFerme, String lieu) {
        Platform.runLater(() -> {
            lblNomFerme.setText(nomFerme);
            paneDetails.setVisible(true);
            paneDetails.setManaged(true);

            String qrData = "Ferme:" + nomFerme + "|Lieu:" + lieu;
            String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" + qrData.replace(" ", "%20");
            ivQRCode.setImage(new Image(qrUrl, true));

            try {
                lvAnimaux.getItems().clear();
                sa.selectALL().stream()
                    .filter(a -> a.getIdFerme() == idFerme)
                    .forEach(a -> lvAnimaux.getItems().add("🐄 " + a.getEspece() + " (" + a.getEtatSante() + ")"));

                lvPlantes.getItems().clear();
                sp.selectALL().stream()
                    .filter(p -> p.getIdFerme() == idFerme)
                    .forEach(p -> lvPlantes.getItems().add("🌱 " + p.getNomEspece()));

                if(lvAnimaux.getItems().isEmpty()) lvAnimaux.getItems().add("Aucun animal.");
                if(lvPlantes.getItems().isEmpty()) lvPlantes.getItems().add("Aucune culture.");

            } catch (SQLException e) { e.printStackTrace(); }
        });
    }

    @FXML private void closeDetails() {
        paneDetails.setVisible(false);
        paneDetails.setManaged(false);
    }

    @FXML private void handleBack(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/tn/esprit/farmai/views/admin-dashboard.fxml", "Admin Dashboard");
    }
}
