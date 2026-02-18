package tn.esprit.farmai.services;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class VoiceRecorder {
    private AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
    private TargetDataLine line;

    /**
     * Démarre l'enregistrement dans un thread séparé
     */
    public void startRecording(String fileName) {
        new Thread(() -> {
            try {
                // Format Audio : 16kHz, 16bits, Mono (Standard pour la reconnaissance vocale)
                AudioFormat format = new AudioFormat(16000, 16, 1, true, true);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                if (!AudioSystem.isLineSupported(info)) {
                    System.err.println("Le micro n'est pas supporté.");
                    return;
                }

                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();

                AudioInputStream ais = new AudioInputStream(line);
                File wavFile = new File(fileName);
                AudioSystem.write(ais, fileType, wavFile);

            } catch (LineUnavailableException | IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    /**
     * Arrête la capture audio
     */
    public void stopRecording() {
        if (line != null) {
            line.stop();
            line.close();
            System.out.println("Enregistrement terminé.");
        }
    }
}