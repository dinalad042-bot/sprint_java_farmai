package tn.esprit.farmai.services;

import org.json.JSONObject;

public class IrrigationAI {

    public String getRecommendation(JSONObject weatherData, String typePlante) {
        try {
            // Sécurité : Vérifier si l'objet JSON est valide
            if (weatherData == null || !weatherData.has("main")) {
                return "❌ ERREUR : Données météo manquantes.";
            }

            // 1. Extraction des paramètres météorologiques
            double temp = weatherData.getJSONObject("main").getDouble("temp");
            int humidity = weatherData.getJSONObject("main").getInt("humidity");
            String mainWeather = weatherData.getJSONArray("weather").getJSONObject(0).getString("main");

            // Sécurité sur le vent car l'objet wind peut parfois être absent
            double windSpeed = 0;
            if (weatherData.has("wind")) {
                windSpeed = weatherData.getJSONObject("wind").getDouble("speed");
            }

            int aqi = weatherData.optInt("air_quality_index", 1);

            // 2. Logique décisionnelle (Arbre de décision)

            if (temp < 2.0) {
                return "❄️ ALERTE GEL ! \nAction : Risque de mort pour " + typePlante + ". Couvrez vos cultures.";
            }

            if (temp > 38.0) {
                return "🔥 CANICULE : \nAction : Arrosage de survie requis. Ombrage conseillé.";
            }

            if (aqi >= 4) {
                return "⚠️ AIR POLLUÉ (AQI: " + aqi + ") : \nAction : Brumisez le feuillage de vos " + typePlante + " pour nettoyer les stomates.";
            }

            if (mainWeather.equalsIgnoreCase("Rain") || mainWeather.equalsIgnoreCase("Drizzle") || mainWeather.equalsIgnoreCase("Thunderstorm")) {
                return "🌧️ PLUIE DÉTECTÉE : \nAction : Arrosage annulé. Apport naturel détecté.";
            }

            if (temp > 30 && humidity < 40) {
                if (windSpeed > 30) {
                    return "🌬️ VENT & CHALEUR : \nAction : Évapotranspiration critique. Arrosez au pied uniquement.";
                }
                return "⚠️ STRESS HYDRIQUE : \nAction : Arrosage intensif requis.";
            }

            if (windSpeed > 45) {
                return "🌬️ VENT VIOLENT : \nAction : Arrosage déconseillé (gaspillage par dérive).";
            }

            if (temp >= 18 && temp <= 26 && humidity > 50) {
                return "✅ CONDITIONS IDÉALES : \nAction : Arrosage standard. Zone de confort pour " + typePlante + ".";
            }

            return "ℹ️ ANALYSE STABLE : \nAction : Pas d'intervention urgente. Humidité suffisante.";

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ ERREUR IA : Format de données incompatible.";
        }
    }
}