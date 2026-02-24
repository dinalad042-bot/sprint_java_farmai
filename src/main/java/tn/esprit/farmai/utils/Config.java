package tn.esprit.farmai.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration class for external service integrations.
 * This class manages API keys and service endpoints.
 * 
 * Configuration Priority:
 * 1. config.properties file in working directory
 * 2. Environment variables
 * 
 * To setup Groq API:
 * 1. Get API key from https://console.groq.com/keys
 * 2. Add to config.properties: GROQ_API_KEY=your_key_here
 * 
 * Available models (as of 2024):
 * - llama-3.1-70b-versatile (recommended)
 * - llama-3.1-8b-instant
 * - mixtral-8x7b-32768
 */
public class Config {
    
    private static final Properties props = new Properties();
    private static boolean configLoaded = false;
    private static String configLoadError = null;
    
    static {
        // Try to load from config.properties file
        try (InputStream input = new FileInputStream("config.properties")) {
            props.load(input);
            configLoaded = true;
            System.out.println("Configuration loaded from config.properties");
        } catch (IOException e) {
            configLoadError = e.getMessage();
            System.err.println("Warning: Could not load config.properties: " + e.getMessage());
            System.err.println("Will try environment variables for API keys.");
        }
    }
    
    /**
     * Check if configuration file was loaded successfully
     */
    public static boolean isConfigLoaded() {
        return configLoaded;
    }
    
    /**
     * Get configuration load error message if any
     */
    public static String getConfigLoadError() {
        return configLoadError;
    }
    
    // Groq API Configuration - US8 Implementation
    private static String loadApiKey() {
        String key = props.getProperty("GROQ_API_KEY");
        if (key == null || key.trim().isEmpty()) {
            key = System.getenv("GROQ_API_KEY");
        }
        return key;
    }
    
    public static final String GROQ_API_KEY = loadApiKey();
    public static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    public static final String GROQ_MODEL = "openai/gpt-oss-120b";  // Updated to available model
    
    // PDF Generation Configuration - US9 Implementation
    public static final String PDF_AUTHOR = "FarmAI System";
    public static final String PDF_CREATOR = "FarmAI Technical Reporting Module";
    public static final String PDF_TITLE_PREFIX = "Technical Analysis Report - ";
    
    // File Paths
    public static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    public static final String PDF_OUTPUT_DIR = TEMP_DIR + "farmai_reports/";
    
    // Chart Configuration - US10 Implementation
    public static final String CHART_TITLE = "FarmAI Analytics Dashboard";
    public static final int CHART_WIDTH = 800;
    public static final int CHART_HEIGHT = 600;
    
    private Config() {
        // Private constructor to prevent instantiation
    }
}