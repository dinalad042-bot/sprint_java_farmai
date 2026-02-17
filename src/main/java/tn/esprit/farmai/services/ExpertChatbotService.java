package tn.esprit.farmai.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

public class ExpertChatbotService {

    private static final String API_KEY = "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";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

    public String genererReponseAI(String questionTexte) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            // Création de l'objet JSON racine
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "llama-3.3-70b-versatile");

            // Création du tableau de messages
            JSONArray messages = new JSONArray();

            // Message système (le rôle de l'IA)
            messages.put(new JSONObject()
                    .put("role", "system")
                    .put("content", "Tu es un expert agronome tunisien. Réponds de manière concise."));

            // Message utilisateur (ta question)
            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", questionTexte));

            jsonBody.put("messages", messages);
            jsonBody.put("temperature", 0.7);

            // Construction de la requête POST
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                    .build();

            // Envoi et réception
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject responseJson = new JSONObject(response.body());
                return responseJson.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
            } else {
                System.err.println("Détail Erreur Groq : " + response.body());
                return "Erreur serveur (Code " + response.statusCode() + ")";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Connexion à l'expert impossible.";
        }
    }
}