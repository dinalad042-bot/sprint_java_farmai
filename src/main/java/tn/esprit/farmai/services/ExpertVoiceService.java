package tn.esprit.farmai.services;

import java.io.IOException;

public class ExpertVoiceService {

    // On garde une référence au processus en cours pour pouvoir l'arrêter
    private Process currentProcess;

    public void repondre(String texte) {
        // Si une voix parle déjà, on l'arrête avant d'en lancer une nouvelle
        arreterLecture();

        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                // Nettoyage : on enlève les apostrophes et les retours à la ligne
                String cleanTexte = texte.replace("'", " ").replace("\n", " ");

                // Commande PowerShell
                String command = "PowerShell -Command \"Add-Type -AssemblyName System.Speech; " +
                        "$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                        "$speak.Speak('" + cleanTexte + "')\"";

                currentProcess = Runtime.getRuntime().exec(command);

            } else if (os.contains("mac")) {
                currentProcess = Runtime.getRuntime().exec(new String[]{"say", texte});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Méthode pour stopper immédiatement la lecture vocale
     */
    public void arreterLecture() {
        if (currentProcess != null && currentProcess.isAlive()) {
            currentProcess.destroy(); // Arrête le processus immédiatement

            // Sur Windows, PowerShell peut laisser un sous-processus de synthèse,
            // cette commande force l'arrêt propre des voix système.
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                try {
                    Runtime.getRuntime().exec("taskkill /F /IM sapissvr.exe /T");
                } catch (IOException e) {
                    // Ignoré si le processus n'existe pas
                }
            }
        }
    }
}