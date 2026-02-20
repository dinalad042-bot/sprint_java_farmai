package tn.esprit.farmai.services;

import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Service météo avancé intégrant les données climatiques et la qualité de l'air.
 */
public class WeatherService {

    private static final String API_KEY = "5ef1420f42bc6ddf45015231736cc42b";
    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Récupère les données météo complètes pour une ville donnée.
     * Inclut désormais l'indice de qualité de l'air (AQI).
     */
    public JSONObject getWeatherData(String city) {
        try {
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String url = "https://api.openweathermap.org/data/2.5/weather?q="
                    + encodedCity + "&units=metric&appid=" + API_KEY;

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject weatherJson = new JSONObject(response.body());

                // Extraction des coordonnées pour l'API de pollution
                double lat = weatherJson.getJSONObject("coord").getDouble("lat");
                double lon = weatherJson.getJSONObject("coord").getDouble("lon");

                // Enrichissement de l'objet avec la qualité de l'air
                int aqi = getAirQualityIndex(lat, lon);
                weatherJson.put("air_quality_index", aqi);

                return weatherJson;
            } else {
                System.err.println("Erreur API Météo (Code " + response.statusCode() + ")");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Exception WeatherService : " + e.getMessage());
            return null;
        }
    }

    /**
     * Appelle l'API Air Pollution pour obtenir l'indice de qualité de l'air (1 à 5).
     */
    private int getAirQualityIndex(double lat, double lon) {
        try {
            String url = String.format("https://api.openweathermap.org/data/2.5/air_pollution?lat=%f&lon=%f&appid=%s",
                    lat, lon, API_KEY);

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject pollutionJson = new JSONObject(response.body());
                // L'AQI se trouve dans list[0].main.aqi
                return pollutionJson.getJSONArray("list")
                        .getJSONObject(0)
                        .getJSONObject("main")
                        .getInt("aqi");
            }
        } catch (Exception e) {
            System.err.println("Erreur Qualité Air : " + e.getMessage());
        }
        return 1; // Retourne 1 (Excellent) par défaut en cas d'échec
    }
}