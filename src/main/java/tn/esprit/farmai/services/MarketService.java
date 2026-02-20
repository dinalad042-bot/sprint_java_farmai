package tn.esprit.farmai.services;

import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MarketService {
    // API gratuite pour les taux de change
    private static final String API_KEY = "89aca7935e0a7a9d52b34485";
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/" + "89aca7935e0a7a9d52b34485" + "/latest/TND";

    public double getExchangeRate(String targetCurrency) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject json = new JSONObject(response.body());
                return json.getJSONObject("conversion_rates").getDouble(targetCurrency);
            }
        } catch (Exception e) {
            System.err.println("Erreur Market API : " + e.getMessage());
        }
        return 0.30; // Valeur de secours (1 TND approx 0.30 USD)
    }
}