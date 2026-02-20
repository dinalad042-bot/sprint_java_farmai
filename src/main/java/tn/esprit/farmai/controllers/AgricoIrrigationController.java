package tn.esprit.farmai.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.json.JSONObject;
import tn.esprit.farmai.services.WeatherService;
import tn.esprit.farmai.services.IrrigationAI;

public class AgricoIrrigationController {

    @FXML private TextField tfVille;
    @FXML private Label lblTitreVille, lblDetailsMeteo, lblConseilIA;
    @FXML private VBox cardResultat;

    private final WeatherService weatherService = new WeatherService();
    private final IrrigationAI irrigationAI = new IrrigationAI();

    @FXML
    private void analyserBesoins() {
        String ville = tfVille.getText().trim();
        if (ville.isEmpty()) return;

        // Appel asynchrone pour ne pas geler l'application
        new Thread(() -> {
            JSONObject data = weatherService.getWeatherData(ville);

            javafx.application.Platform.runLater(() -> {
                if (data != null) {
                    double temp = data.getJSONObject("main").getDouble("temp");
                    int hum = data.getJSONObject("main").getInt("humidity");

                    lblTitreVille.setText("📍 " + ville.toUpperCase());
                    lblDetailsMeteo.setText("Température : " + temp + "°C | Humidité : " + hum + "%");

                    // Utilisation de l'IA créée à l'étape précédente
                    String recommendation = irrigationAI.getRecommendation(data, "vos cultures");
                    lblConseilIA.setText(recommendation);

                    cardResultat.setVisible(true);
                } else {
                    lblConseilIA.setText("❌ Impossible de joindre le satellite météo.");
                    cardResultat.setVisible(true);
                }
            });
        }).start();
    }
}