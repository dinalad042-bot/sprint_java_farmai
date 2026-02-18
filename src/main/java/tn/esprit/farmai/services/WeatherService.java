package tn.esprit.farmai.services;

import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WeatherService {
    // Ta clé API intégrée
    private static final String API_KEY = "5ef1420f42bc6ddf45015231736cc42b";

    public JSONObject getWeatherData(String city) {
        try {
            // Encodage pour gérer les espaces ou caractères spéciaux dans les noms de villes
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String url = "https://api.openweathermap.org/data/2.5/weather?q="
                    + encodedCity + "&units=metric&appid=" + "5ef1420f42bc6ddf45015231736cc42b";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return new JSONObject(response.body());
            } else {
                // Affiche l'erreur précise dans la console pour le debug
                System.err.println("Erreur API OpenWeather (Code: " + response.statusCode() + "): " + response.body());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Exception lors de l'appel météo : " + e.getMessage());
            return null;
        }
    }
}