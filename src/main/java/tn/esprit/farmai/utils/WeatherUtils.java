package tn.esprit.farmai.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Weather API utility for enriching Analyse diagnostics.
 * Uses Open-Meteo API (free, no API key required) - KISS principle.
 * 
 * SRP: Handles only weather data retrieval.
 * Railway Track: Uses Ferme.lieu to fetch weather, enriches Analyse.resultat_technique.
 * 
 * @author FarmAI Team
 * @since Sprint Java - Seance 6/7
 */
public class WeatherUtils {

    // Open-Meteo Geocoding API (no API key required)
    private static final String GEOCODING_API_URL = "https://geocoding-api.open-meteo.com/v1/search?name=%s&count=1&language=fr";
    
    // Open-Meteo Weather API (no API key required)
    private static final String WEATHER_API_URL = "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current=temperature_2m,relative_humidity_2m,weather_code";
    
    private static final int CONNECT_TIMEOUT = 5000;  // 5 seconds
    private static final int READ_TIMEOUT = 10000;    // 10 seconds

    /**
     * Weather data record for encapsulating weather information.
     * Immutable data carrier - Clean Code principle.
     */
    public record WeatherData(
        double temperature,
        int humidity,
        int weatherCode,
        String location,
        boolean success,
        String errorMessage
    ) {
        /**
         * Factory method for successful weather data
         */
        public static WeatherData success(double temperature, int humidity, int weatherCode, String location) {
            return new WeatherData(temperature, humidity, weatherCode, location, true, null);
        }
        
        /**
         * Factory method for failed weather data
         */
        public static WeatherData failure(String errorMessage) {
            return new WeatherData(0, 0, 0, null, false, errorMessage);
        }
        
        /**
         * Get weather description from WMO weather code
         * @return Human-readable weather description in French
         */
        public String getWeatherDescription() {
            return WeatherUtils.getWeatherDescription(weatherCode);
        }
        
        /**
         * Format weather data for Analyse.resultat_technique concatenation
         * @return Formatted string like "Météo: 25°C, Humidité: 65%, Ensoleillé"
         */
        public String formatForDiagnostic() {
            if (!success) return "";
            return String.format("Météo: %.1f°C, Humidité: %d%%, %s", 
                temperature, humidity, getWeatherDescription());
        }
    }

    /**
     * Fetch weather data for a given location.
     * 
     * @param lieu The farm location (e.g., "Tunis, Tunisie")
     * @return WeatherData containing temperature, humidity, and conditions
     * 
     * Edge Cases Handled:
     * - Null or empty lieu
     * - Network timeout
     * - Location not found
     * - Malformed response
     */
    public static WeatherData fetchWeather(String lieu) {
        // Edge case: null or empty location
        if (lieu == null || lieu.trim().isEmpty()) {
            return WeatherData.failure("Location is required");
        }
        
        try {
            // Parse city name from "City, Country" format
            String cityName = parseCityName(lieu);
            
            // Step 1: Get coordinates from city name
            Coordinates coords = getCoordinates(cityName);
            if (coords == null) {
                return WeatherData.failure("Location not found: " + cityName);
            }
            
            // Step 2: Get weather data from coordinates
            return getWeatherData(coords, cityName);
            
        } catch (IOException e) {
            // Network error - graceful degradation
            return WeatherData.failure("Weather service unavailable: " + e.getMessage());
        } catch (Exception e) {
            // Unexpected error - log and fail gracefully
            System.err.println("Weather API error: " + e.getMessage());
            return WeatherData.failure("Failed to fetch weather data");
        }
    }

    /**
     * Parse city name from full location string.
     * Handles formats like "Tunis, Tunisie" or "Sfax, Tunisie"
     */
    private static String parseCityName(String lieu) {
        if (lieu == null) return "";
        
        // Split by comma and take first part (city name)
        String[] parts = lieu.split(",");
        String city = parts[0].trim();
        
        // Map Tunisian city names to English/ASCII for API
        return normalizeCityName(city);
    }

    /**
     * Normalize city name for API compatibility.
     * Maps common variations to standard names.
     */
    private static String normalizeCityName(String city) {
        // Common Tunisian city mappings
        return switch (city.toLowerCase()) {
            case "tunis" -> "Tunis";
            case "sfax" -> "Sfax";
            case "sousse" -> "Sousse";
            case "bizerte" -> "Bizerte";
            case "nabeul" -> "Nabeul";
            case "gabès", "gabes" -> "Gabes";
            case "ariana" -> "Ariana";
            case "ben arous" -> "Ben Arous";
            case "monastir" -> "Monastir";
            case "kairouan" -> "Kairouan";
            default -> city; // Return as-is for other cities
        };
    }

    /**
     * Get coordinates (lat, lon) for a city name using Open-Meteo Geocoding API.
     */
    private static Coordinates getCoordinates(String cityName) throws IOException {
        String encodedCity = URLEncoder.encode(cityName, "UTF-8");
        String url = String.format(GEOCODING_API_URL, encodedCity);
        
        String response = makeGetRequest(url);
        
        // Parse JSON response manually (avoid external JSON library dependency)
        // Response format: {"results":[{"latitude":36.8,"longitude":10.18,...}]}
        
        if (response == null || !response.contains("\"results\"")) {
            return null;
        }
        
        // Extract first result
        int resultsStart = response.indexOf("\"results\":[");
        if (resultsStart == -1) {
            return null;
        }
        
        // Check for empty results
        String resultsSection = response.substring(resultsStart);
        if (resultsSection.contains("[]")) {
            return null;
        }
        
        // Extract latitude and longitude
        double lat = extractDoubleValue(response, "latitude");
        double lon = extractDoubleValue(response, "longitude");
        
        if (Double.isNaN(lat) || Double.isNaN(lon)) {
            return null;
        }
        
        return new Coordinates(lat, lon);
    }

    /**
     * Get weather data from coordinates using Open-Meteo Weather API.
     */
    private static WeatherData getWeatherData(Coordinates coords, String cityName) throws IOException {
        String url = String.format(WEATHER_API_URL, coords.latitude, coords.longitude);
        
        String response = makeGetRequest(url);
        
        if (response == null) {
            return WeatherData.failure("No response from weather service");
        }
        
        // Parse current weather data
        // Response format: {"current":{"temperature_2m":25.0,"relative_humidity_2m":65,"weather_code":0}}
        
        double temperature = extractDoubleValue(response, "temperature_2m");
        int humidity = extractIntValue(response, "relative_humidity_2m");
        int weatherCode = extractIntValue(response, "weather_code");
        
        if (Double.isNaN(temperature)) {
            return WeatherData.failure("Invalid temperature data");
        }
        
        return WeatherData.success(temperature, humidity, weatherCode, cityName);
    }

    /**
     * Make HTTP GET request and return response body.
     */
    private static String makeGetRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    return response.toString();
                }
            } else {
                throw new IOException("HTTP error: " + responseCode);
            }
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Extract double value from JSON string by key.
     * Simple parser to avoid external dependencies.
     */
    private static double extractDoubleValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":";
            int startIndex = json.indexOf(searchKey);
            if (startIndex == -1) return Double.NaN;
            
            startIndex += searchKey.length();
            
            // Find end of number (comma, bracket, or whitespace)
            int endIndex = startIndex;
            while (endIndex < json.length()) {
                char c = json.charAt(endIndex);
                if (c == ',' || c == '}' || c == ']' || Character.isWhitespace(c)) {
                    break;
                }
                endIndex++;
            }
            
            String valueStr = json.substring(startIndex, endIndex).trim();
            return Double.parseDouble(valueStr);
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    /**
     * Extract integer value from JSON string by key.
     */
    private static int extractIntValue(String json, String key) {
        double value = extractDoubleValue(json, key);
        return Double.isNaN(value) ? 0 : (int) value;
    }

    /**
     * Get weather description from WMO weather code.
     * WMO Code definitions: https://open-meteo.com/en/docs
     */
    public static String getWeatherDescription(int code) {
        return switch (code) {
            case 0 -> "Ciel dégagé";
            case 1, 2, 3 -> "Partiellement nuageux";
            case 45, 48 -> "Brouillard";
            case 51, 53, 55 -> "Bruine";
            case 56, 57 -> "Bruine verglaçante";
            case 61, 63, 65 -> "Pluie";
            case 66, 67 -> "Pluie verglaçante";
            case 71, 73, 75 -> "Neige";
            case 77 -> "Grains de neige";
            case 80, 81, 82 -> "Averses de pluie";
            case 85, 86 -> "Averses de neige";
            case 95 -> "Orage";
            case 96, 99 -> "Orage avec grêle";
            default -> "Conditions inconnues";
        };
    }

    /**
     * Internal record for coordinates.
     */
    private record Coordinates(double latitude, double longitude) {}

    /**
     * Private constructor - utility class should not be instantiated.
     */
    private WeatherUtils() {}
}