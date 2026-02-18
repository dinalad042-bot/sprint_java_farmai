package tn.esprit.farmai.controllers;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class VoiceRecorder {
    private TargetDataLine line;

    public void startRecording(String fileName) {
        try {
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            Thread thread = new Thread(() -> {
                AudioInputStream ais = new AudioInputStream(line);
                try {
                    AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(fileName));
                } catch (IOException e) { e.printStackTrace(); }
            });
            thread.start();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void stopRecording() {
        line.stop();
        line.close();
    }
}