package tn.esprit.farmai.services;

import tn.esprit.farmai.models.DiagnosisResult;
import tn.esprit.farmai.utils.Config;
import tn.esprit.farmai.utils.SimpleHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for AI visual plant disease diagnosis using Groq vision API.
 * Provides image analysis capabilities for expert module.
 */
public class ExpertVisionService {
    
    // Current supported Groq vision model
    // See: https://console.groq.com/docs/vision
    private static final String VISION_MODEL = "meta-llama/llama-4-scout-17b-16e-instruct";
    private static final int MAX_IMAGE_SIZE_MB = 5;
    private static final int MAX_IMAGE_WIDTH = 1024;
    private static final int MAX_IMAGE_HEIGHT = 1024;
    
    /**
     * Analyze a plant image for disease diagnosis.
     * 
     * @param imagePath Path to the local image file
     * @return DiagnosisResult with structured diagnosis information
     * @throws VisionException if analysis fails
     */
    public DiagnosisResult analyzePlantImage(String imagePath) throws VisionException {
        // Validate API key
        String apiKey = Config.GROQ_API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new VisionException(
                "Groq API key not configured. Please add GROQ_API_KEY to config.properties"
            );
        }
        
        try {
            // 1. Validate and process image
            File imageFile = validateAndResizeImage(imagePath);
            
            // 2. Encode to base64
            String base64Image = encodeImageToBase64(imageFile);
            String dataUri = "data:image/jpeg;base64," + base64Image;
            
            // 3. Build request
            JSONObject request = buildVisionRequest(dataUri);
            
            // 4. Call API
            String response = SimpleHttpClient.postJson(
                Config.GROQ_API_URL,
                request.toString(),
                "Bearer " + apiKey
            );
            
            // 5. Parse response
            return parseDiagnosisResponse(response);
            
        } catch (IOException e) {
            throw new VisionException("Failed to process image: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validate image file and resize if necessary.
     */
    private File validateAndResizeImage(String imagePath) throws VisionException, IOException {
        File file = new File(imagePath);
        
        if (!file.exists()) {
            throw new VisionException("Image file not found: " + imagePath);
        }
        
        // Check file size
        long fileSizeMB = file.length() / (1024 * 1024);
        if (fileSizeMB > MAX_IMAGE_SIZE_MB) {
            throw new VisionException(
                "Image too large (" + fileSizeMB + "MB). Max size is " + MAX_IMAGE_SIZE_MB + "MB."
            );
        }
        
        // Check format - allow more formats since extension might be wrong
        String fileName = file.getName().toLowerCase();

        // Try to read the image - attempt multiple formats
        BufferedImage image = tryReadImage(file);

        if (image == null) {
            // Provide diagnostic info for debugging - detect actual format
            long fileSize = file.length();
            String actualFormat = detectActualFormat(file);
            String extension = getFileExtension(file.getName());

            String diagnosticMsg = String.format(
                "Cannot read image file.\n" +
                "  File: %s\n" +
                "  Size: %d bytes\n" +
                "  Extension: %s\n" +
                "  Actual Format: %s\n\n" +
                "The file extension (.%s) doesn't match the actual format (%s).\n" +
                "Please convert the image to a standard JPEG or PNG format.",
                file.getName(), fileSize, extension, actualFormat, extension, actualFormat
            );
            throw new VisionException(diagnosticMsg);
        }
        
        // Resize if too large
        if (image.getWidth() > MAX_IMAGE_WIDTH || image.getHeight() > MAX_IMAGE_HEIGHT) {
            return resizeImage(image, file);
        }
        
        return file;
    }

    /**
     * Extract file extension from filename.
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "none";
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1).toLowerCase();
        }
        return "none";
    }

    /**
     * Try to read image using multiple format readers.
     * This helps when the file extension doesn't match the actual format.
     */
    private BufferedImage tryReadImage(File file) throws IOException {
        // First try standard ImageIO read
        BufferedImage image = ImageIO.read(file);
        if (image != null) {
            return image;
        }

        // Try common formats that might have wrong extension
        String[] formatsToTry = {"png", "jpeg", "gif", "bmp", "webp", "wbmp"};
        for (String format : formatsToTry) {
            try {
                java.util.Iterator<javax.imageio.ImageReader> readers = ImageIO.getImageReadersByFormatName(format);
                while (readers.hasNext()) {
                    javax.imageio.ImageReader reader = readers.next();
                    try {
                        reader.setInput(ImageIO.createImageInputStream(file), true);
                        image = reader.read(0);
                        reader.dispose();
                        if (image != null) {
                            System.out.println("Successfully read image as " + format.toUpperCase());
                            return image;
                        }
                    } catch (Exception e) {
                        // Try next reader
                    }
                }
            } catch (Exception e) {
                // Format not available, skip
            }
        }

        return null;
    }

    /**
     * Detect actual image format by reading file magic bytes.
     * Common formats:
     * - JPEG: FF D8 FF
     * - PNG: 89 50 4E 47
     * - GIF: 47 49 46 38
     * - BMP: 42 4D
     * - WebP: 52 49 46 46 ... 57 45 42 50
     */
    private String detectActualFormat(File file) {
        try {
            byte[] header = new byte[12];
            try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                int bytesRead = fis.read(header);
                if (bytesRead < 4) {
                    return "unknown (file too small)";
                }
            }

            // Check magic bytes
            if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
                return "JPEG";
            }
            if (header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
                return "PNG";
            }
            if (header[0] == 0x47 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x38) {
                return "GIF";
            }
            if (header[0] == 0x42 && header[1] == 0x4D) {
                return "BMP";
            }
            // WebP: RIFF....WEBP
            if (header[0] == 0x52 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x46 &&
                header[8] == 0x57 && header[9] == 0x45 && header[10] == 0x42 && header[11] == 0x50) {
                return "WebP";
            }

            // Print hex for debugging
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < Math.min(8, header.length); i++) {
                hex.append(String.format("%02X ", header[i]));
            }
            return "unknown (header: " + hex.toString().trim() + ")";

        } catch (Exception e) {
            return "error reading: " + e.getMessage();
        }
    }

    /**
     * Resize image to fit within max dimensions while maintaining aspect ratio.
     */
    private File resizeImage(BufferedImage originalImage, File originalFile) throws IOException {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // Calculate new dimensions
        double scale = Math.min(
            (double) MAX_IMAGE_WIDTH / originalWidth,
            (double) MAX_IMAGE_HEIGHT / originalHeight
        );
        
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);
        
        // Create resized image
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        resizedImage.createGraphics().drawImage(
            originalImage.getScaledInstance(newWidth, newHeight, java.awt.Image.SCALE_SMOOTH),
            0, 0, null
        );
        
        // Save to temp file
        File tempFile = File.createTempFile("resized_", ".jpg");
        tempFile.deleteOnExit();
        ImageIO.write(resizedImage, "JPEG", tempFile);
        
        System.out.println("Image resized from " + originalWidth + "x" + originalHeight + 
                          " to " + newWidth + "x" + newHeight);
        
        return tempFile;
    }
    
    /**
     * Encode image file to base64 string.
     */
    private String encodeImageToBase64(File imageFile) throws IOException {
        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
        return Base64.getEncoder().encodeToString(imageBytes);
    }
    
    /**
     * Build the JSON request for Groq vision API.
     * Using simplified format that works with Groq's API.
     */
    private JSONObject buildVisionRequest(String dataUri) {
        JSONObject request = new JSONObject();
        request.put("model", VISION_MODEL);
        request.put("max_tokens", 800);
        request.put("temperature", 0.3);
        
        JSONArray messages = new JSONArray();
        
        // First message with system prompt
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are an expert agricultural pathologist. Analyze plant images and provide diagnoses in the exact format requested.");
        messages.put(systemMessage);
        
        // Second message with image
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        
        JSONArray content = new JSONArray();
        
        // Add the prompt text
        content.put(new JSONObject()
            .put("type", "text")
            .put("text", getSystemPrompt()));
        
        // Add the image - format for Groq API
        JSONObject imageContent = new JSONObject();
        imageContent.put("type", "image_url");
        JSONObject imageUrl = new JSONObject();
        imageUrl.put("url", dataUri);
        imageContent.put("image_url", imageUrl);
        content.put(imageContent);
        
        userMessage.put("content", content);
        messages.put(userMessage);
        
        request.put("messages", messages);
        
        System.out.println("Request payload: " + request.toString().substring(0, Math.min(200, request.toString().length())) + "...");
        
        return request;
    }
    
    /**
     * Get the system prompt for universal agricultural diagnosis (plants AND animals).
     * Matches the Symfony GroqService approach.
     */
    private String getSystemPrompt() {
        return """
            Tu es un expert agronome et veterinary agricultural. Analyse cette image et identifie ce qui est represente.

            Etape 1: DETECTE CE QUI EST DANS L'IMAGE
            - Plante/culture (legume, fruits, cereales, etc.)
            - Animal (betail, volaille, etc.)

            Etape 2: ANALYSE ADAPTEE

            Si c'est une PLANTES/COLLECTION:
            - Analyse les maladies foliaires, carences nutritionnelles, problemes de racines
            - Evalue les symptomes sur les feuilles, tiged, fruits
            - Recommande traitements phytosanitaires, ameliorations culturelles

            Si c'est un ANIMAL:
            - Evalue l'etat de sante general, apparence physique
            - Identifie les problemes de peau, de la fleece, de la salive, etc.
            - Recommande soins veterinaires, traitements, pronostics
            - Precise si consultation veterinaire urgent necessaire

            IMPORTANT: Sois honnete et precis. Si tu n'es pas certain, indique un niveau de confiance bas.

            Provide your analysis in this EXACT format:

            SUBJECT_TYPE: [plant|animal]
            CONDITION: [condition detectee ou "Sante normale"]
            CONFIDENCE: [MUST be Low if any uncertainty, Medium if fairly sure, High ONLY if 100% certain]
            SYMPTOMS: [symptomes observes detailles en francais]
            TREATMENT: [traitement recommande]
            PREVENTION: [mesures preventives]
            URGENCY: [Immediat|Dans la semaine|Surveiller]
            EXPERT_CONSULT: [Yes/No - Yes if uncertain or serious issue]

            Be precise, honest, and practical for Tunisian agricultural conditions.
            """;
    }
    
    /**
     * Parse the API response into a DiagnosisResult.
     */
    private DiagnosisResult parseDiagnosisResponse(String response) throws VisionException {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            
            // Check for errors
            if (jsonResponse.has("error")) {
                JSONObject error = jsonResponse.getJSONObject("error");
                throw new VisionException("API Error: " + error.optString("message", "Unknown error"));
            }
            
            // Extract content from choices
            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (choices.length() == 0) {
                throw new VisionException("No response from AI");
            }
            
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            String content = message.getString("content");
            
            // Parse structured response
            return parseStructuredResponse(content);
            
        } catch (Exception e) {
            if (e instanceof VisionException) {
                throw e;
            }
            throw new VisionException("Failed to parse response: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parse the structured text response from the AI.
     */
    private DiagnosisResult parseStructuredResponse(String content) {
        DiagnosisResult result = new DiagnosisResult();
        result.setRawResponse(content);
        
        // Parse each field using regex
        result.setCondition(extractField(content, "CONDITION"));
        result.setConfidence(parseConfidence(extractField(content, "CONFIDENCE")));
        result.setSymptoms(extractField(content, "SYMPTOMS"));
        result.setTreatment(extractField(content, "TREATMENT"));
        result.setPrevention(extractField(content, "PREVENTION"));
        result.setUrgency(extractField(content, "URGENCY"));
        result.setNeedsExpertConsult(parseYesNo(extractField(content, "EXPERT_CONSULT")));
        
        return result;
    }
    
    /**
     * Extract a field value from the structured response.
     */
    private String extractField(String content, String fieldName) {
        Pattern pattern = Pattern.compile(fieldName + ":\\s*(.+?)(?=\\n[A-Z_]+:|\\z)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "Non specifie";
    }
    
    /**
     * Parse confidence level string to enum.
     */
    private DiagnosisResult.ConfidenceLevel parseConfidence(String value) {
        if (value == null) return DiagnosisResult.ConfidenceLevel.LOW;
        
        String normalized = value.toLowerCase().trim();
        if (normalized.contains("high") || normalized.contains("eleve")) {
            return DiagnosisResult.ConfidenceLevel.HIGH;
        } else if (normalized.contains("medium") || normalized.contains("moyen")) {
            return DiagnosisResult.ConfidenceLevel.MEDIUM;
        }
        return DiagnosisResult.ConfidenceLevel.LOW;
    }
    
    /**
     * Parse yes/no string to boolean.
     */
    private boolean parseYesNo(String value) {
        if (value == null) return false;
        String normalized = value.toLowerCase().trim();
        return normalized.contains("yes") || normalized.contains("oui") || normalized.contains("true");
    }
    
    /**
     * Custom exception for vision service errors.
     */
    public static class VisionException extends Exception {
        public VisionException(String message) {
            super(message);
        }
        
        public VisionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
