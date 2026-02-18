package tn.esprit.farmai.services;

import org.json.JSONObject;

/**
 * Système Expert d'aide à la décision pour l'irrigation
 */
public class IrrigationAI {

    /**
     * Analyse les données météo et retourne une recommandation textuelle
     * @param weatherData Objet JSON provenant de WeatherService
     * @param typePlante Nom de la culture (ex: Tomate, Blé)
     * @return Message de recommandation IA
     */
    public String getRecommendation(JSONObject weatherData, String typePlante) {
        try {
            // 1. Extraction des paramètres vitaux
            double temp = weatherData.getJSONObject("main").getDouble("temp");
            int humidity = weatherData.getJSONObject("main").getInt("humidity");
            String mainWeather = weatherData.getJSONArray("weather").getJSONObject(0).getString("main");
            double windSpeed = weatherData.getJSONObject("wind").getDouble("speed");

            // 2. Logique décisionnelle (Arbre de décision IA)

            // Cas de pluie : On annule l'irrigation
            if (mainWeather.equalsIgnoreCase("Rain") || mainWeather.equalsIgnoreCase("Drizzle")) {
                return "🌧️ Statut : Pluie détectée. \nAction : Arrosage annulé pour vos " + typePlante + ".";
            }

            // Cas de forte chaleur (Stress hydrique)
            if (temp > 32 && humidity < 35) {
                return "⚠️ Alerte Stress Hydrique ! \nAction : Arrosage intensif requis immédiatement. Température élevée (" + temp + "°C).";
            }

            // Cas de vent fort (Évaporation rapide)
            if (windSpeed > 40) {
                return "🌬️ Vent fort détecté. \nAction : Arrosage déconseillé (l'eau s'évaporera avant d'atteindre les racines).";
            }

            // Cas optimal
            if (temp >= 20 && temp <= 28 && humidity > 50) {
                return "✅ Conditions optimales. \nAction : Arrosage modéré programmé pour ce soir.";
            }

            return "ℹ️ Analyse IA : Sol stable. \nAction : Aucune intervention urgente requise pour le moment.";

        } catch (Exception e) {
            return "❌ Erreur : Impossible d'analyser les données pour l'irrigation.";
        }
    }
}