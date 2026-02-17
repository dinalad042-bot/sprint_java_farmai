package tn.esprit.farmai.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration class for external service integrations.
 * This class manages API keys and service endpoints.
 */
public class Config {
    
    private static final Properties props = new Properties();
    
    static {
        // Try to load from config.properties file
        try (InputStream input = new FileInputStream("config.properties")) {
            props.load(input);
        } catch (IOException e) {
            System.err.println("Warning: Could not load config.properties: " + e.getMessage());
        }
    }
    
    // Groq API Configuration - US8 Implementation
    public static final String GROQ_API_KEY = props.getProperty("GROQ_API_KEY", System.getenv("GROQ_API_KEY"));
    public static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    public static final String GROQ_MODEL = "llama-3.3-70b-versatile";
    
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