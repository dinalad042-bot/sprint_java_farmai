package tn.esprit.farmai.services;

import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Currency conversion using open.er-api.com (free, no API key).
 * Mirrors Symfony ExchangeRateService — same API endpoint.
 *
 * Base currency: EUR (same as Symfony).
 * Default target: TND (Tunisian Dinar).
 */
public class ExchangeRateService {

    private static final Logger LOGGER = Logger.getLogger(ExchangeRateService.class.getName());
    private static final String API_BASE = "https://open.er-api.com/v6/latest/";
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * Get exchange rate from base to target currency.
     * @return rate or null if unavailable
     */
    public Double getRate(String base, String target) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE + base.toUpperCase()))
                    .timeout(Duration.ofSeconds(5))
                    .GET().build();
            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) return null;
            JSONObject json = new JSONObject(resp.body());
            if (!json.has("rates")) return null;
            JSONObject rates = json.getJSONObject("rates");
            if (!rates.has(target.toUpperCase())) return null;
            return rates.getDouble(target.toUpperCase());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "ExchangeRate API error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Convert amount from base to target currency.
     * @return converted amount or null if API unavailable
     */
    public Double convert(double amount, String base, String target) {
        Double rate = getRate(base, target);
        if (rate == null) return null;
        return Math.round(amount * rate * 100.0) / 100.0;
    }

    /**
     * Get all available currency codes (for the dropdown).
     * Same as Symfony getCurrencyCodes().
     */
    public List<String> getCurrencyCodes(String base) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE + base.toUpperCase()))
                    .timeout(Duration.ofSeconds(5))
                    .GET().build();
            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) return defaultCurrencies();
            JSONObject json = new JSONObject(resp.body());
            if (!json.has("rates")) return defaultCurrencies();
            List<String> codes = new ArrayList<>(json.getJSONObject("rates").keySet());
            Collections.sort(codes);
            return codes;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "ExchangeRate getCurrencies error: " + e.getMessage());
            return defaultCurrencies();
        }
    }

    /** Fallback list if API is down */
    private List<String> defaultCurrencies() {
        return List.of("TND", "USD", "GBP", "JPY", "CHF", "CAD", "AUD", "MAD", "DZD", "SAR", "AED");
    }
}
