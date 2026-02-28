package tn.esprit.farmai.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import javax.sound.sampled.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Text-to-Speech utility for reading Conseil descriptions aloud.
 * Uses Voice RSS API (free tier) or fallback to system TTS.
 * 
 * SRP: Handles only text-to-speech conversion.
 * Async: Uses CompletableFuture to prevent UI freezing.
 * 
 * @author FarmAI Team
 * @since Sprint Java - Seance 6/7
 */
public class SpeechUtils {

    // Voice RSS API (free tier: 350 requests/day without API key)
    // Using free TTS service that doesn't require API key
    private static final String TTS_API_URL = "https://translate.google.com/translate_tts?ie=UTF-8&tl=fr&client=tw-ob&q=%s";
    
    // Alternative: Voice RSS (requires free API key from voicerss.org)
    // private static final String VOICE_RSS_URL = "https://api.voicerss.org/?key=%s&hl=fr-fr&src=%s";
    
    private static final int CONNECT_TIMEOUT = 10000;  // 10 seconds
    private static final int READ_TIMEOUT = 30000;     // 30 seconds
    
    // Singleton pattern for audio clip management
    private static final AtomicBoolean isPlaying = new AtomicBoolean(false);
    private static Clip currentClip = null;

    /**
     * Speak text asynchronously using TTS API.
     * Non-blocking UI - uses CompletableFuture.
     * 
     * @param text The text to speak (Conseil description)
     * @return CompletableFuture<Void> for chaining/cancellation
     * 
     * Edge Cases Handled:
     * - Null or empty text
     * - Network timeout
     * - Audio playback failure
     * - Concurrent playback prevention
     */
    public static CompletableFuture<Void> speakAsync(String text) {
        return CompletableFuture.runAsync(() -> {
            try {
                speak(text);
            } catch (Exception e) {
                System.err.println("TTS Error: " + e.getMessage());
            }
        });
    }

    /**
     * Speak text synchronously.
     * Blocks until audio playback completes.
     * 
     * @param text The text to speak
     * @throws IOException if TTS fails
     */
    public static void speak(String text) throws IOException {
        // Edge case: null or empty text
        if (text == null || text.trim().isEmpty()) {
            throw new IOException("Text cannot be empty");
        }
        
        // Prevent concurrent playback
        if (isPlaying.get()) {
            stop();
        }
        
        // Truncate very long text (API limit)
        String truncatedText = truncateText(text, 500);
        
        try {
            // Use system TTS as primary method (no API required)
            speakWithSystemTTS(truncatedText);
        } catch (Exception e) {
            // Fallback: Try online TTS API
            System.err.println("System TTS failed, trying API fallback: " + e.getMessage());
            speakWithApi(truncatedText);
        }
    }

    /**
     * Stop current playback.
     * Thread-safe method to cancel ongoing TTS.
     */
    public static void stop() {
        isPlaying.set(false);
        if (currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
            currentClip.close();
            currentClip = null;
        }
    }

    /**
     * Check if TTS is currently playing.
     */
    public static boolean isPlaying() {
        return isPlaying.get();
    }

    /**
     * System TTS using native OS speech synthesis.
     * Works on Windows (SAPI), macOS (say), Linux (espeak).
     * No external API required - KISS principle.
     */
    private static void speakWithSystemTTS(String text) throws IOException, InterruptedException {
        isPlaying.set(true);
        
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            
            if (os.contains("win")) {
                // Windows: Use PowerShell with SAPI
                String escapedText = escapeForPowerShell(text);
                pb = new ProcessBuilder(
                    "powershell", "-Command",
                    "Add-Type -AssemblyName System.Speech; " +
                    "$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                    "$synth.Speak('" + escapedText + "');"
                );
            } else if (os.contains("mac")) {
                // macOS: Use 'say' command
                pb = new ProcessBuilder("say", "-v", "Thomas", text);
            } else {
                // Linux: Use 'espeak' if available
                pb = new ProcessBuilder("espeak", "-v", "fr", text);
            }
            
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Wait for completion with timeout
            boolean completed = process.waitFor(60, java.util.concurrent.TimeUnit.SECONDS);
            
            if (!completed) {
                process.destroyForcibly();
                throw new IOException("TTS timeout");
            }
            
            if (process.exitValue() != 0) {
                throw new IOException("TTS process failed with code: " + process.exitValue());
            }
            
        } finally {
            isPlaying.set(false);
        }
    }

    /**
     * Fallback: Online TTS API.
     * Downloads audio and plays it.
     */
    private static void speakWithApi(String text) throws IOException {
        isPlaying.set(true);
        
        try {
            // Encode text for URL
            String encodedText = URLEncoder.encode(text, "UTF-8");
            String urlString = String.format(TTS_API_URL, encodedText);
            
            // Download audio
            byte[] audioData = downloadAudio(urlString);
            
            if (audioData == null || audioData.length == 0) {
                throw new IOException("Failed to download TTS audio");
            }
            
            // Play audio
            playAudio(audioData);
            
        } finally {
            isPlaying.set(false);
        }
    }

    /**
     * Download audio from URL.
     */
    private static byte[] downloadAudio(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream is = conn.getInputStream();
                     ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    return baos.toByteArray();
                }
            } else {
                throw new IOException("HTTP error: " + responseCode);
            }
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Play audio data using Java Sound API.
     */
    private static void playAudio(byte[] audioData) throws IOException {
        try {
            // Create audio input stream from MP3
            ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bais);
            
            // Get audio format
            AudioFormat format = audioStream.getFormat();
            
            // Create clip
            currentClip = AudioSystem.getClip();
            currentClip.open(audioStream);
            
            // Add listener to detect playback completion
            currentClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    isPlaying.set(false);
                }
            });
            
            // Start playback
            currentClip.start();
            
            // Wait for playback to complete
            while (currentClip.isRunning()) {
                Thread.sleep(100);
            }
            
            currentClip.close();
            currentClip = null;
            
        } catch (UnsupportedAudioFileException | LineUnavailableException e) {
            throw new IOException("Audio playback failed: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Playback interrupted");
        }
    }

    /**
     * Escape text for PowerShell command.
     * Handles single quotes and special characters.
     */
    private static String escapeForPowerShell(String text) {
        return text
            .replace("'", "''")  // Escape single quotes
            .replace("\n", " ")   // Replace newlines
            .replace("\r", "")    // Remove carriage returns
            .replace("\"", "'")   // Replace double quotes
            .trim();
    }

    /**
     * Truncate text to maximum length for TTS API.
     * Preserves word boundaries.
     */
    private static String truncateText(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        
        // Find last space before maxLength
        int lastSpace = text.lastIndexOf(' ', maxLength);
        if (lastSpace > 0) {
            return text.substring(0, lastSpace) + "...";
        }
        
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Private constructor - utility class should not be instantiated.
     */
    private SpeechUtils() {}
}