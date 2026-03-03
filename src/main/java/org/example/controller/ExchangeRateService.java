package org.example.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;
import java.util.TreeSet;

public class ExchangeRateService {

    private final HttpClient client = HttpClient.newHttpClient();

    public BigDecimal getRate(String base, String target) {
        try {
            String url = "https://open.er-api.com/v6/latest/" + base.toUpperCase();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) return null;

            JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();
            if (!root.has("rates")) return null;

            JsonObject rates = root.getAsJsonObject("rates");
            if (!rates.has(target.toUpperCase())) return null;

            return rates.get(target.toUpperCase()).getAsBigDecimal();
        } catch (Exception e) {
            return null;
        }
    }

    public BigDecimal convert(BigDecimal amount, String base, String target) {
        BigDecimal rate = getRate(base, target);
        if (rate == null) return null;
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    public Set<String> getCurrencyCodes(String base) {
        try {
            String url = "https://open.er-api.com/v6/latest/" + base.toUpperCase();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) return null;

            JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();
            if (!root.has("rates")) return null;

            JsonObject rates = root.getAsJsonObject("rates");

            Set<String> codes = new TreeSet<>();
            for (String key : rates.keySet()) codes.add(key);
            return codes;
        } catch (Exception e) {
            return null;
        }
    }
}