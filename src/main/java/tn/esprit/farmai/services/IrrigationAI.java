package tn.esprit.farmai.services;

import org.json.JSONObject;

/**
 * Système Expert d'aide à la décision avancé pour l'irrigation et la santé végétale.
 */
public class IrrigationAI {

    /**
     * Analyse les données météo et de pollution pour retourner une recommandation intelligente.
     * @param weatherData Objet JSON enrichi provenant de WeatherService
     * @param typePlante Nom de la culture (ex: Tomate, Maïs)
     * @return Message de recommandation IA formaté
     */
    public String getRecommendation(JSONObject weatherData, String typePlante) {
        try {
            // 1. Extraction des paramètres météorologiques
            double temp = weatherData.getJSONObject("main").getDouble("temp");
            int humidity = weatherData.getJSONObject("main").getInt("humidity");
            String mainWeather = weatherData.getJSONArray("weather").getJSONObject(0).getString("main");
            double windSpeed = weatherData.getJSONObject("wind").getDouble("speed");

            // 2. Extraction du nouvel indice de qualité de l'air (AQI)
            // L'indice va de 1 (Excellent) à 5 (Très pauvre)
            int aqi = weatherData.optInt("air_quality_index", 1);

            // 3. Logique décisionnelle augmentée (Arbre de décision IA)

            // PRIORITÉ 1 : Alertes critiques (Gel ou Canicule extrême)
            if (temp < 2.0) {
                return "❄️ ALERTE GEL ! \nAction : Risque de mort cellulaire pour " + typePlante + ". Couvrez vos cultures ou activez le chauffage de serre.";
            }

            if (temp > 38.0) {
                return "🔥 CANICULE EXTRÊME : \nAction : Arrosage de survie requis. Ombrage fortement conseillé pour protéger les feuilles.";
            }

            // PRIORITÉ 2 : Conditions de l'air (Pollution)
            if (aqi >= 4) {
                return "⚠️ AIR POLLUÉ (AQI: " + aqi + ") : \nAction : Les particules fines bouchent les stomates. Une brumisation légère est conseillée pour nettoyer le feuillage.";
            }

            // PRIORITÉ 3 : Météo actuelle (Pluie)
            if (mainWeather.equalsIgnoreCase("Rain") || mainWeather.equalsIgnoreCase("Drizzle") || mainWeather.equalsIgnoreCase("Thunderstorm")) {
                return "🌧️ PLUIE DÉTECTÉE : \nAction : Arrosage annulé. Profitez de l'apport naturel en eau pour vos " + typePlante + ".";
            }

            // PRIORITÉ 4 : Stress hydrique et évaporation
            if (temp > 30 && humidity < 40) {
                if (windSpeed > 30) {
                    return "🌬️ VENT & CHALEUR : \nAction : Évapotranspiration critique. Arrosez au pied (goutte-à-goutte) uniquement pour éviter la perte par dérive.";
                }
                return "⚠️ STRESS HYDRIQUE : \nAction : Arrosage intensif requis. Les conditions favorisent le dessèchement rapide.";
            }

            // PRIORITÉ 5 : Vent fort
            if (windSpeed > 45) {
                return "🌬️ VENT VIOLENT : \nAction : Arrosage déconseillé. Risque de casse mécanique et gaspillage d'eau par pulvérisation.";
            }

            // PRIORITÉ 6 : Conditions Optimales
            if (temp >= 18 && temp <= 26 && humidity > 50) {
                return "✅ CONDITIONS IDÉALES : \nAction : Arrosage standard programmé. Vos " + typePlante + " sont dans leur zone de confort thermique.";
            }

            return "ℹ️ ANALYSE STABLE : \nAction : Pas d'intervention urgente. Le sol conserve une humidité suffisante selon les relevés.";

        } catch (Exception e) {
            return "❌ ERREUR IA : Les données reçues sont incomplètes pour une analyse précise.";
        }
    }
}