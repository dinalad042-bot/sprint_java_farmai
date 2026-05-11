package tn.esprit.farmai.services;

import tn.esprit.farmai.interfaces.CRUD;
import tn.esprit.farmai.models.Analyse;
import tn.esprit.farmai.models.Conseil;
import tn.esprit.farmai.models.Ferme;
import tn.esprit.farmai.utils.Config;
import tn.esprit.farmai.utils.MyDBConnexion;
import tn.esprit.farmai.utils.SimpleHttpClient;
import tn.esprit.farmai.utils.WeatherUtils;

import org.json.JSONObject;
import org.json.JSONArray;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.sql.Types;

/**
 * Service class for Analyse CRUD operations with advanced features.
 * Implements US8: AI-Assisted Diagnostics, US9: PDF Technical Reporting, US10: Data Visualization
 * Uses PreparedStatement for secure data handling (US1).
 * 
 * Railway Track Trace:
 * - Entity: Analyse (this service)
 * - Relation: Analyse -> Conseil (1:N via id_analyse FK)
 */
public class AnalyseService implements CRUD<Analyse> {

    private final Connection cnx;

    public AnalyseService() {
        this.cnx = MyDBConnexion.getInstance().getCnx();
    }

    @Override
    public void insertOne(Analyse analyse) throws SQLException {
        String query = "INSERT INTO analyse (date_analyse, resultat_technique, id_technicien_id, id_ferme_id, " +
                      "statut, id_demandeur, description_demande, image_url, id_animal_cible, id_plante_cible, " +
                      "ai_diagnosis_result, ai_diagnosis_date, ai_confidence_score, diagnosis_mode) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, Timestamp.valueOf(analyse.getDateAnalyse()));
            ps.setString(2, analyse.getResultatTechnique());
            ps.setInt(3, analyse.getIdTechnicien());
            ps.setInt(4, analyse.getIdFerme());
            ps.setString(5, analyse.getStatut() != null ? analyse.getStatut() : "en_attente");
            ps.setInt(6, analyse.getIdDemandeur());
            ps.setString(7, analyse.getDescriptionDemande());
            ps.setString(8, analyse.getImageUrl());
            ps.setInt(9, analyse.getIdAnimalCible());
            ps.setInt(10, analyse.getIdPlanteCible());
            ps.setString(11, analyse.getAiDiagnosisResult());
            ps.setTimestamp(12, analyse.getAiDiagnosisDate() != null ? Timestamp.valueOf(analyse.getAiDiagnosisDate()) : null);
            ps.setString(13, analyse.getAiConfidenceScore());
            ps.setString(14, analyse.getDiagnosisMode());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                analyse.setIdAnalyse(rs.getInt(1));
            }
        }
    }

    /**
     * Create a new farmer request (simplified insert for agricultural users)
     */
    public void createFarmerRequest(Analyse analyse) throws SQLException {
        String query = "INSERT INTO analyse (date_analyse, statut, id_demandeur, id_ferme_id, " +
                      "description_demande, image_url, id_animal_cible, id_plante_cible) " +
                      "VALUES (NOW(), 'en_attente', ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, analyse.getIdDemandeur());
            ps.setInt(2, analyse.getIdFerme());
            ps.setString(3, analyse.getDescriptionDemande());
            ps.setString(4, analyse.getImageUrl());
            if (analyse.getIdAnimalCible() > 0) {
                ps.setInt(5, analyse.getIdAnimalCible());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            if (analyse.getIdPlanteCible() > 0) {
                ps.setInt(6, analyse.getIdPlanteCible());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                analyse.setIdAnalyse(rs.getInt(1));
                analyse.setStatut("en_attente");
            }
        }
    }

    @Override
    public void updateOne(Analyse analyse) throws SQLException {
        String query = "UPDATE analyse SET date_analyse = ?, resultat_technique = ?, " +
                      "id_technicien_id = ?, id_ferme_id = ?, image_url = ?, " +
                      "statut = ?, id_demandeur = ?, description_demande = ?, " +
                      "id_animal_cible = ?, id_plante_cible = ?, " +
                      "ai_diagnosis_result = ?, ai_diagnosis_date = ?, ai_confidence_score = ?, diagnosis_mode = ? " +
                      "WHERE id_analyse = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setTimestamp(1, Timestamp.valueOf(analyse.getDateAnalyse()));
            ps.setString(2, analyse.getResultatTechnique());
            ps.setInt(3, analyse.getIdTechnicien());
            ps.setInt(4, analyse.getIdFerme());
            ps.setString(5, analyse.getImageUrl());
            ps.setString(6, analyse.getStatut());
            ps.setInt(7, analyse.getIdDemandeur());
            ps.setString(8, analyse.getDescriptionDemande());
            ps.setInt(9, analyse.getIdAnimalCible());
            ps.setInt(10, analyse.getIdPlanteCible());
            ps.setString(11, analyse.getAiDiagnosisResult());
            ps.setTimestamp(12, analyse.getAiDiagnosisDate() != null ? Timestamp.valueOf(analyse.getAiDiagnosisDate()) : null);
            ps.setString(13, analyse.getAiConfidenceScore());
            ps.setString(14, analyse.getDiagnosisMode());
            ps.setInt(15, analyse.getIdAnalyse());

            ps.executeUpdate();
        }
    }

    @Override
    public void deleteOne(Analyse analyse) throws SQLException {
        String query = "DELETE FROM analyse WHERE id_analyse = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, analyse.getIdAnalyse());
            ps.executeUpdate();
        }
    }

@Override
    public List<Analyse> selectALL() throws SQLException {
        List<Analyse> analyses = new ArrayList<>();
        String query = "SELECT * FROM analyse ORDER BY date_analyse DESC";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                analyses.add(mapResultSetToAnalyse(rs));
            }
        }
        return analyses;
    }

    /**
     * US8: AI-Assisted Diagnostics using Groq API
     * 
     * @param observation Raw observation data from technician
     * @return AI-generated technical analysis
     * @throws IOException if API call fails
     * 
     * Railway Track Trace: Targets Analyse.resultat_technique attribute
     */
    public String generateAIDiagnostic(String observation) throws IOException, InterruptedException {
        if (observation == null || observation.trim().isEmpty()) {
            return "No observation provided. Please enter your observations.";
        }

        // Validate API key configuration
        String apiKey = Config.GROQ_API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            String configHint = Config.isConfigLoaded() 
                ? "GROQ_API_KEY is missing in config.properties"
                : "config.properties not found: " + Config.getConfigLoadError();
            throw new IOException(
                "AI service not configured.\n\n" +
                "Setup Instructions:\n" +
                "1. Get a free API key from https://console.groq.com/keys\n" +
                "2. Add to config.properties: GROQ_API_KEY=your_key_here\n\n" +
                "Current status: " + configHint
            );
        }

        // Validate API key format (Groq keys start with 'gsk_')
        if (!apiKey.startsWith("gsk_")) {
            throw new IOException(
                "Invalid API key format. Groq API keys should start with 'gsk_'.\n" +
                "Please get a valid key from https://console.groq.com/keys"
            );
        }

        String systemContent = "You are an expert agricultural technician providing technical analysis for farm diagnostics. Provide concise, professional technical reports.";
        String userContent = "Analyze this agricultural observation and provide a technical diagnostic summary: " + observation;

        String jsonBody = buildGroqRequest(systemContent, userContent);

        try {
            String response = SimpleHttpClient.postJson(Config.GROQ_API_URL, jsonBody, "Bearer " + apiKey);
            return extractContent(response);
        } catch (IOException e) {
            String msg = e.getMessage();
            if (msg == null) {
                throw new IOException("AI service connection failed. Please check your internet connection.");
            }
            
            // Parse specific error conditions
            if (msg.contains("invalid_api_key") || msg.contains("Invalid API Key")) {
                throw new IOException(
                    "Invalid Groq API key. The key may have been revoked or expired.\n" +
                    "Please get a new key from https://console.groq.com/keys"
                );
            } else if (msg.contains("insufficient_quota") || msg.contains("quota")) {
                throw new IOException(
                    "Groq API quota exceeded. Please check your usage at https://console.groq.com/usage"
                );
            } else if (msg.contains("model_not_found") || msg.contains("does not exist")) {
                throw new IOException(
                    "AI model '" + Config.GROQ_MODEL + "' not available.\n" +
                    "Please check available models at https://console.groq.com/docs/models"
                );
            } else if (msg.contains("image") || msg.contains("does not support")) {
                throw new IOException("This AI model does not support image input. Please provide text-based observations only.");
            } else if (msg.contains("401") || msg.contains("Unauthorized")) {
                throw new IOException(
                    "AI service authentication failed. Your API key may be invalid.\n" +
                    "Please verify at https://console.groq.com/keys"
                );
            } else if (msg.contains("429") || msg.contains("rate limit")) {
                throw new IOException("AI service rate limit reached. Please wait a moment and try again.");
            } else if (msg.contains("500") || msg.contains("502") || msg.contains("503")) {
                throw new IOException("AI service is temporarily unavailable. Please try again later.");
            }
            throw new IOException("Failed to generate AI diagnostic: " + msg);
        }
    }

    private String buildGroqRequest(String systemContent, String userContent) {
        JSONObject request = new JSONObject();
        request.put("model", Config.GROQ_MODEL);
        request.put("temperature", 0.7);
        request.put("max_tokens", 500);
        
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
            .put("role", "system")
            .put("content", systemContent));
        messages.put(new JSONObject()
            .put("role", "user")
            .put("content", userContent));
        request.put("messages", messages);
        
        return request.toString();
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    private String extractContent(String response) throws IOException {
        if (response == null || response.isEmpty()) {
            throw new IOException("Empty response from AI service");
        }
        
        // Debug: Log raw response for troubleshooting
        System.out.println("=== Groq API Raw Response ===");
        System.out.println(response.length() > 500 ? response.substring(0, 500) + "..." : response);
        System.out.println("=============================");
        
        // Check for error response
        if (response.contains("\"error\"")) {
            String errorMsg = "AI service error";
            
            if (response.contains("\"message\":")) {
                int msgIdx = response.indexOf("\"message\":");
                int start = response.indexOf("\"", msgIdx + 10);
                if (start != -1) {
                    int end = response.indexOf("\"", start + 1);
                    while (end > start && end > 0 && response.charAt(end - 1) == '\\') {
                        end = response.indexOf("\"", end + 1);
                    }
                    if (end > start) {
                        errorMsg = response.substring(start + 1, end)
                            .replace("\\n", " ")
                            .replace("\\\"", "\"")
                            .replace("\\\\", "\\");
                    }
                }
            }
            
            if (errorMsg.contains("image") || errorMsg.contains("Image")) {
                throw new IOException("The AI cannot process image references. Please remove any image paths or file names from your observation text and try again.");
            }
            throw new IOException("AI Error: " + errorMsg);
        }
        
        try {
            // Parse OpenAI/Groq response format: choices[0].message.content
            // The response structure is: {"choices":[{"message":{"role":"assistant","content":"..."}}]}
            
            // Find the choices array
            int choicesIndex = response.indexOf("\"choices\"");
            if (choicesIndex == -1) {
                // Fallback: Try legacy format (direct content field)
                return extractContentFallback(response);
            }
            
            // Find the first message object within choices
            int messageIndex = response.indexOf("\"message\"", choicesIndex);
            if (messageIndex == -1) {
                // Some responses may have content directly in choice
                int contentIndex = response.indexOf("\"content\"", choicesIndex);
                if (contentIndex != -1) {
                    return extractContentValue(response, contentIndex);
                }
                throw new IOException("No message found in API response");
            }
            
            // Find content within the message object
            int contentIndex = response.indexOf("\"content\"", messageIndex);
            if (contentIndex == -1) {
                throw new IOException("No content field found in message");
            }
            
            return extractContentValue(response, contentIndex);
            
        } catch (IOException e) {
            throw e; // Re-throw IOException as-is
        } catch (Exception e) {
            System.err.println("JSON parsing error: " + e.getMessage());
            throw new IOException("Failed to parse AI response: " + e.getMessage());
        }
    }
    
    /**
     * Extract content value from a JSON string starting from the content field index
     */
    private String extractContentValue(String response, int contentIndex) throws IOException {
        // Find the colon after "content"
        int colonIndex = response.indexOf(":", contentIndex);
        if (colonIndex == -1) {
            throw new IOException("Invalid content field format");
        }
        
        // Find the opening quote of the content value
        int startQuote = response.indexOf("\"", colonIndex);
        if (startQuote == -1) {
            // Content might be null or a different type
            String afterColon = response.substring(colonIndex + 1, Math.min(colonIndex + 20, response.length())).trim();
            if (afterColon.startsWith("null")) {
                return "AI analysis returned empty result. Please try with different observations.";
            }
            throw new IOException("Content value is not a string: " + afterColon);
        }
        
        // Find the closing quote (handle escaped quotes)
        int endQuote = startQuote + 1;
        while (endQuote < response.length()) {
            char c = response.charAt(endQuote);
            if (c == '"') {
                // Check if this quote is escaped
                int backslashCount = 0;
                int checkIndex = endQuote - 1;
                while (checkIndex > startQuote && response.charAt(checkIndex) == '\\') {
                    backslashCount++;
                    checkIndex--;
                }
                // If even number of backslashes, the quote is not escaped
                if (backslashCount % 2 == 0) {
                    break;
                }
            }
            endQuote++;
        }
        
        if (endQuote >= response.length()) {
            throw new IOException("Could not find end of content string");
        }
        
        // Extract and unescape the content
        String content = response.substring(startQuote + 1, endQuote);
        String result = content
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\");
        
        if (result.trim().isEmpty()) {
            return "AI analysis completed. The result was empty - please try with different observations.";
        }
        
        System.out.println("=== Extracted Content ===");
        System.out.println(result.length() > 200 ? result.substring(0, 200) + "..." : result);
        System.out.println("=========================");
        
        return result;
    }
    
    /**
     * Fallback method for legacy/different response formats
     */
    private String extractContentFallback(String response) throws IOException {
        // Try to find content field anywhere in the response
        int contentIndex = response.indexOf("\"content\":");
        if (contentIndex != -1) {
            return extractContentValue(response, contentIndex);
        }
        
        // If no content field found, the response format is unexpected
        throw new IOException("Unexpected AI response format. Response: " + 
            (response.length() > 200 ? response.substring(0, 200) + "..." : response));
    }

    /**
     * US9: PDF Technical Report - generates real binary PDF
     * 
     * @param idAnalyse The analysis ID to export
     * @return Path to generated PDF report file
     * @throws SQLException if database error occurs
     * @throws IOException if file creation fails
     * 
     * Railway Track: Implements 1:N relationship (Analyse -> Conseil)
     * Uses Apache PDFBox for binary PDF generation
     */
    public String exportAnalysisToPDF(int idAnalyse) throws SQLException, IOException {
        Analyse analyse = findById(idAnalyse).orElseThrow(() -> 
            new SQLException("Analysis not found with ID: " + idAnalyse));
        
        List<Conseil> conseils = getConseilsByAnalyse(idAnalyse);

        File outputDir = new File(Config.PDF_OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String fileName = "analysis_" + idAnalyse + "_" + System.currentTimeMillis() + ".pdf";
        String filePath = Config.PDF_OUTPUT_DIR + fileName;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String dateFormatted = analyse.getDateAnalyse().format(formatter);

        // Create PDF document
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            float margin = 50;
            float yPosition = page.getMediaBox().getHeight() - margin;
            float pageWidth = page.getMediaBox().getWidth() - 2 * margin;
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Title
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("FARMIA TECHNICAL ANALYSIS REPORT");
                contentStream.endText();
                yPosition -= 30;
                
                // Underline
                contentStream.setLineWidth(1f);
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
                contentStream.stroke();
                yPosition -= 25;
                
                // Metadata section
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Analysis ID: #" + idAnalyse);
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.newLineAtOffset(margin + 200, yPosition);
                contentStream.showText("Date: " + dateFormatted);
                contentStream.endText();
                yPosition -= 20;
                
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Farm ID: " + analyse.getIdFerme());
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.newLineAtOffset(margin + 200, yPosition);
                contentStream.showText("Technician ID: " + analyse.getIdTechnicien());
                contentStream.endText();
                yPosition -= 30;
                
                // Technical Result Section
                yPosition = drawSectionHeader(contentStream, "TECHNICAL RESULT", margin, yPosition);
                String resultText = analyse.getResultatTechnique() != null ? analyse.getResultatTechnique() : "N/A";
                yPosition = drawWrappedText(contentStream, resultText, margin, yPosition, pageWidth, 10);
                yPosition -= 20;
                
                // Image section if URL exists
                if (analyse.getImageUrl() != null && !analyse.getImageUrl().trim().isEmpty()) {
                    yPosition = drawSectionHeader(contentStream, "ANALYSIS IMAGE", margin, yPosition);
                    yPosition = drawImageFromUrl(document, contentStream, analyse.getImageUrl(), margin, yPosition, pageWidth);
                    yPosition -= 20;
                }
                
                // Check if we need a new page for recommendations
                if (yPosition < 150 && !conseils.isEmpty()) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    yPosition = page.getMediaBox().getHeight() - margin;
                }
                
                // Recommendations Section
                if (!conseils.isEmpty()) {
                    yPosition = drawSectionHeader(contentStream, "RECOMMENDATIONS (" + conseils.size() + ")", margin, yPosition);
                    
                    for (int i = 0; i < conseils.size(); i++) {
                        Conseil conseil = conseils.get(i);
                        
                        // Check if we need a new page
                        if (yPosition < 100) {
                            contentStream.close();
                            page = new PDPage(PDRectangle.A4);
                            document.addPage(page);
                            yPosition = page.getMediaBox().getHeight() - margin;
                        }
                        
                        // Recommendation number and priority
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                        contentStream.beginText();
                        contentStream.newLineAtOffset(margin, yPosition);
                        contentStream.showText((i + 1) + ". [" + conseil.getPriorite().getLabel().toUpperCase() + " PRIORITY]");
                        contentStream.endText();
                        yPosition -= 15;
                        
                        // Recommendation description
                        yPosition = drawWrappedText(contentStream, conseil.getDescriptionConseil(), margin + 15, yPosition, pageWidth - 15, 10);
                        yPosition -= 10;
                    }
                }
                
                // Footer
                yPosition = margin + 20;
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 8);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Generated by: " + Config.PDF_CREATOR);
                contentStream.endText();
            }
            
            // Save the document
            document.save(filePath);
        }

        return filePath;
    }
    
    /**
     * Helper method to draw section headers in PDF
     */
    private float drawSectionHeader(PDPageContentStream contentStream, String header, float x, float y) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(header);
        contentStream.endText();
        return y - 20;
    }
    
    /**
     * Helper method to draw wrapped text in PDF
     */
    private float drawWrappedText(PDPageContentStream contentStream, String text, float x, float y, float maxWidth, int fontSize) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA, fontSize);
        
        // Clean text - remove newlines and extra spaces
        String cleanText = text.replace("\r", " ").replace("\n", " ").replaceAll("\\s+", " ").trim();
        
        if (cleanText.isEmpty()) {
            return y;
        }
        
        float charWidth = fontSize * 0.5f; // Approximate char width
        int charsPerLine = (int) (maxWidth / charWidth);
        
        String[] words = cleanText.split(" ");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > charsPerLine) {
                // Draw current line
                contentStream.beginText();
                contentStream.newLineAtOffset(x, y);
                contentStream.showText(currentLine.toString().trim());
                contentStream.endText();
                y -= fontSize + 2;
                currentLine = new StringBuilder(word + " ");
            } else {
                currentLine.append(word).append(" ");
            }
        }
        
        // Draw last line
        if (currentLine.length() > 0) {
            contentStream.beginText();
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(currentLine.toString().trim());
            contentStream.endText();
            y -= fontSize + 2;
        }
        
        return y;
    }
    
    /**
     * Helper method to draw image from URL in PDF
     * 
     * Supports multiple image sources:
     * 1. Local file paths (absolute or relative)
     * 2. HTTP/HTTPS URLs
     * 
     * Supported formats: JPEG, PNG, GIF, BMP
     * 
     * Railway Track: Images are loaded from URLs (String) stored in database
     * Uses timeout to prevent hanging on network issues
     */
    private float drawImageFromUrl(PDDocument document, PDPageContentStream contentStream, String imageUrl, float x, float y, float maxWidth) throws IOException {
        BufferedImage bufferedImage = null;
        String imageSource = "unknown";
        
        try {
            // Step 1: Try to load image from various sources
            File imageFile = new File(imageUrl);
            
            if (imageFile.exists() && imageFile.isFile()) {
                // Case 1: Local file path
                imageSource = "local file";
                bufferedImage = ImageIO.read(imageFile);
                
                if (bufferedImage == null) {
                    // ImageIO couldn't read the file format
                    throw new IOException("Unsupported image format for file: " + imageFile.getName());
                }
            } else if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                // Case 2: HTTP/HTTPS URL
                imageSource = "URL";
                URL url = new URL(imageUrl);
                java.net.URLConnection connection = url.openConnection();
                connection.setConnectTimeout(5000);  // 5 seconds timeout
                connection.setReadTimeout(10000);    // 10 seconds read timeout
                
                // Set user agent to avoid 403 errors from some servers
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                
                try (InputStream is = connection.getInputStream()) {
                    bufferedImage = ImageIO.read(is);
                    
                    if (bufferedImage == null) {
                        throw new IOException("Unsupported image format from URL");
                    }
                }
            } else {
                // Case 3: Invalid path - try to give helpful error message
                throw new IOException("Image path is not a valid file or URL: " + truncateString(imageUrl, 50));
            }
            
            // Step 2: Ensure image was loaded
            if (bufferedImage == null) {
                throw new IOException("Failed to load image from " + imageSource);
            }
            
            // Step 3: Convert to compatible format for PDF (JPEG/PNG)
            // PDFBox works best with RGB images
            BufferedImage rgbImage = bufferedImage;
            if (bufferedImage.getType() != BufferedImage.TYPE_INT_RGB && 
                bufferedImage.getType() != BufferedImage.TYPE_INT_ARGB) {
                // Convert to RGB
                rgbImage = new BufferedImage(
                    bufferedImage.getWidth(), 
                    bufferedImage.getHeight(), 
                    BufferedImage.TYPE_INT_RGB
                );
                rgbImage.createGraphics().drawImage(bufferedImage, 0, 0, null);
            }
            
            // Step 4: Create temporary file with proper extension
            String format = "JPEG";  // Use JPEG for smaller PDF size
            File tempFile = File.createTempFile("pdf_img_", "." + format.toLowerCase());
            tempFile.deleteOnExit();
            
            boolean written = ImageIO.write(rgbImage, format, tempFile);
            if (!written) {
                // Try PNG if JPEG fails
                format = "PNG";
                tempFile = File.createTempFile("pdf_img_", "." + format.toLowerCase());
                tempFile.deleteOnExit();
                written = ImageIO.write(rgbImage, format, tempFile);
            }
            
            if (!written) {
                throw new IOException("Could not write image in any supported format");
            }
            
            // Step 5: Load into PDF
            PDImageXObject pdImage = PDImageXObject.createFromFileByContent(tempFile, document);
            
            // Step 6: Calculate scaling (max 200px height, fit to page width)
            float imageWidth = pdImage.getWidth();
            float imageHeight = pdImage.getHeight();
            float maxHeight = 200;  // Max height in points
            float scale = Math.min(maxWidth / imageWidth, maxHeight / imageHeight);
            
            float scaledWidth = imageWidth * scale;
            float scaledHeight = imageHeight * scale;
            
            // Step 7: Check if we need a new page
            if (y - scaledHeight < 100) {
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
                contentStream.beginText();
                contentStream.newLineAtOffset(x, y);
                contentStream.showText("[Image requires more space - " + (int)imageWidth + "x" + (int)imageHeight + " pixels]");
                contentStream.endText();
                return y - 15;
            }
            
            // Step 8: Draw the image
            contentStream.drawImage(pdImage, x, y - scaledHeight, scaledWidth, scaledHeight);
            
            // Log success for debugging
            System.out.println("PDF Image loaded successfully: " + imageSource + " (" + (int)imageWidth + "x" + (int)imageHeight + " -> " + (int)scaledWidth + "x" + (int)scaledHeight + ")");
            
            return y - scaledHeight - 15;
            
        } catch (java.net.SocketTimeoutException e) {
            contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
            contentStream.beginText();
            contentStream.newLineAtOffset(x, y);
            contentStream.showText("[Image timeout - network too slow]");
            contentStream.endText();
            return y - 15;
        } catch (javax.imageio.IIOException e) {
            // ImageIO specific errors (corrupt image, wrong format, etc.)
            contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
            contentStream.beginText();
            contentStream.newLineAtOffset(x, y);
            contentStream.showText("[Invalid image format: " + truncateString(e.getMessage(), 30) + "]");
            contentStream.endText();
            System.err.println("PDF Image Error (ImageIO): " + e.getMessage());
            return y - 15;
        } catch (Exception e) {
            contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
            contentStream.beginText();
            contentStream.newLineAtOffset(x, y);
            contentStream.showText("[Image error: " + truncateString(e.getMessage(), 35) + "]");
            contentStream.endText();
            System.err.println("PDF Image Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return y - 15;
        }
    }
    
    /**
     * Helper to truncate strings for display
     */
    private String truncateString(String str, int maxLen) {
        if (str == null) return "";
        return str.length() > maxLen ? str.substring(0, maxLen) + "..." : str;
    }
    
    /**
     * US9: HTML Technical Report - opens in browser (Secondary option)
     * 
     * @param idAnalyse The analysis ID to export
     * @return Path to generated HTML report file
     * @throws SQLException if database error occurs
     * @throws IOException if file creation fails
     */
    public String exportAnalysisToHTML(int idAnalyse) throws SQLException, IOException {
        Analyse analyse = findById(idAnalyse).orElseThrow(() -> 
            new SQLException("Analysis not found with ID: " + idAnalyse));
        
        List<Conseil> conseils = getConseilsByAnalyse(idAnalyse);

        File outputDir = new File(Config.PDF_OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String fileName = "analysis_" + idAnalyse + "_" + System.currentTimeMillis() + ".html";
        String filePath = Config.PDF_OUTPUT_DIR + fileName;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String dateFormatted = analyse.getDateAnalyse().format(formatter);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang='en'>\n<head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        html.append("<title>FarmIA Technical Report - Analysis #").append(idAnalyse).append("</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: 'Segoe UI', Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 40px; background: #f5f5f5; }\n");
        html.append(".container { background: white; padding: 40px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n");
        html.append("h1 { color: #2E7D32; text-align: center; border-bottom: 3px solid #2E7D32; padding-bottom: 20px; }\n");
        html.append("h2 { color: #1565C0; margin-top: 30px; }\n");
        html.append(".meta { color: #666; font-size: 14px; }\n");
        html.append(".meta span { display: inline-block; margin-right: 30px; }\n");
        html.append(".result { background: #E8F5E9; padding: 20px; border-left: 4px solid #2E7D32; margin: 20px 0; }\n");
        html.append(".recommendations { margin-top: 30px; }\n");
        html.append(".recommendation { background: #f5f5f5; padding: 15px; margin-bottom: 15px; border-radius: 5px; }\n");
        html.append(".priority { display: inline-block; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: bold; }\n");
        html.append(".priority-HAUTE { background: #FFEBEE; color: #C62828; }\n");
        html.append(".priority-MOYENNE { background: #FFF3E0; color: #EF6C00; }\n");
        html.append(".priority-BASSE { background: #E8F5E9; color: #2E7D32; }\n");
        html.append(".footer { text-align: center; color: #999; font-size: 12px; margin-top: 40px; padding-top: 20px; border-top: 1px solid #eee; }\n");
        html.append("</style>\n</head>\n<body>\n");
        
        html.append("<div class='container'>\n");
        html.append("<h1>FARMIA Technical Analysis Report</h1>\n");
        
        html.append("<div class='meta'>\n");
        html.append("<span><strong>Analysis ID:</strong> #").append(idAnalyse).append("</span>\n");
        html.append("<span><strong>Date:</strong> ").append(dateFormatted).append("</span>\n");
        html.append("</div>\n");
        
        html.append("<div class='meta' style='margin-top: 10px;'>\n");
        html.append("<span><strong>Farm ID:</strong> ").append(analyse.getIdFerme()).append("</span>\n");
        html.append("<span><strong>Technician ID:</strong> ").append(analyse.getIdTechnicien()).append("</span>\n");
        html.append("</div>\n");
        
        html.append("<h2>Technical Result</h2>\n");
        html.append("<div class='result'>\n");
        html.append("<p>").append(analyse.getResultatTechnique() != null ? analyse.getResultatTechnique() : "N/A").append("</p>\n");
        html.append("</div>\n");
        
        // Add image to HTML if exists
        if (analyse.getImageUrl() != null && !analyse.getImageUrl().trim().isEmpty()) {
            html.append("<h2>Analysis Image</h2>\n");
            html.append("<img src='").append(analyse.getImageUrl()).append("' style='max-width: 100%; border-radius: 5px;' alt='Analysis Image'/>\n");
        }
        
        html.append("<h2>Recommendations (").append(conseils.size()).append(")</h2>\n");
        
        if (conseils.isEmpty()) {
            html.append("<p><i>No recommendations available for this analysis.</i></p>\n");
        } else {
            html.append("<div class='recommendations'>\n");
            for (Conseil conseil : conseils) {
                html.append("<div class='recommendation'>\n");
                html.append("<p>").append(conseil.getDescriptionConseil()).append("</p>\n");
                html.append("<span class='priority priority-").append(conseil.getPriorite().name()).append("'>");
                html.append(conseil.getPriorite().getLabel()).append(" Priority</span>\n");
                html.append("</div>\n");
            }
            html.append("</div>\n");
        }
        
        html.append("<div class='footer'>\n");
        html.append("<p>Generated by: ").append(Config.PDF_CREATOR).append("</p>\n");
        html.append("</div>\n");
        
        html.append("</div>\n</body>\n</html>");

        Files.write(new File(filePath).toPath(), html.toString().getBytes());

        return filePath;
    }

    /**
     * Railway Track: Fetch Conseil entities by Analyse FK (1:N relationship)
     */
    private List<Conseil> getConseilsByAnalyse(int idAnalyse) throws SQLException {
        List<Conseil> conseils = new ArrayList<>();
        String query = "SELECT c.* FROM conseil c WHERE c.id_analyse = ? ORDER BY c.id_conseil";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, idAnalyse);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Conseil conseil = new Conseil();
                conseil.setIdConseil(rs.getInt("id_conseil"));
                conseil.setDescriptionConseil(rs.getString("description_conseil"));
                conseil.setPriorite(tn.esprit.farmai.models.Priorite.valueOf(rs.getString("priorite")));
                conseil.setIdAnalyse(rs.getInt("id_analyse"));
                conseils.add(conseil);
            }
        }
        return conseils;
    }

    /**
     * US10: Get priority distribution for visualization
     */
    public List<Object[]> getConseilPriorityStats() throws SQLException {
        List<Object[]> stats = new ArrayList<>();
        String query = "SELECT priorite, COUNT(*) as count FROM conseil GROUP BY priorite ORDER BY count DESC";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                stats.add(new Object[]{
                    rs.getString("priorite"),
                    rs.getInt("count")
                });
            }
        }
        return stats;
    }

    /**
     * US10: Get analysis count per farm for visualization
     */
    public List<Object[]> getAnalysisPerFarmStats() throws SQLException {
        List<Object[]> stats = new ArrayList<>();
        String query = "SELECT id_ferme_id, COUNT(*) as count FROM analyse GROUP BY id_ferme_id ORDER BY count DESC";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                stats.add(new Object[]{
                    rs.getInt("id_ferme_id"),
                    rs.getInt("count")
                });
            }
        }
        return stats;
    }

    /**
     * Find analysis by ID
     */
    public Optional<Analyse> findById(int id) throws SQLException {
        String query = "SELECT * FROM analyse WHERE id_analyse = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToAnalyse(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Find analyses by technician ID
     */
    public List<Analyse> findByTechnicien(int idTechnicien) throws SQLException {
        List<Analyse> analyses = new ArrayList<>();
        String query = "SELECT * FROM analyse WHERE id_technicien_id = ? ORDER BY date_analyse DESC";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, idTechnicien);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                analyses.add(mapResultSetToAnalyse(rs));
            }
        }
        return analyses;
    }

    /**
     * Find all pending requests (for expert to take)
     */
    public List<Analyse> findPendingRequests() throws SQLException {
        List<Analyse> analyses = new ArrayList<>();
        String query = "SELECT * FROM analyse WHERE statut = 'en_attente' ORDER BY date_analyse ASC";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                analyses.add(mapResultSetToAnalyse(rs));
            }
        }
        return analyses;
    }

    /**
     * Expert takes a request (assigns to themselves)
     */
    public void takeRequest(int idAnalyse, int idTechnicien) throws SQLException {
        String query = "UPDATE analyse SET statut = 'en_cours', id_technicien_id = ? WHERE id_analyse = ? AND statut = 'en_attente'";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, idTechnicien);
            ps.setInt(2, idAnalyse);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new SQLException("Request not found or already taken");
            }
        }
    }

    /**
     * Find analyses by demandeur (farmer who made the request)
     */
    public List<Analyse> findByDemandeur(int idDemandeur) throws SQLException {
        List<Analyse> analyses = new ArrayList<>();
        String query = "SELECT * FROM analyse WHERE id_demandeur = ? ORDER BY date_analyse DESC";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, idDemandeur);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                analyses.add(mapResultSetToAnalyse(rs));
            }
        }
        return analyses;
    }

    /**
     * Find analyses in progress for a technician
     */
    public List<Analyse> findInProgressByTechnicien(int idTechnicien) throws SQLException {
        List<Analyse> analyses = new ArrayList<>();
        String query = "SELECT * FROM analyse WHERE id_technicien_id = ? AND statut = 'en_cours' ORDER BY date_analyse DESC";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, idTechnicien);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                analyses.add(mapResultSetToAnalyse(rs));
            }
        }
        return analyses;
    }

    /**
     * Complete an analysis with result
     */
    public void completeAnalyse(int idAnalyse, String resultatTechnique) throws SQLException {
        String query = "UPDATE analyse SET statut = 'terminee', resultat_technique = ? WHERE id_analyse = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, resultatTechnique);
            ps.setInt(2, idAnalyse);
            ps.executeUpdate();
        }
    }

    /**
     * Update AI diagnosis result - saves to both resultat_technique (visible in UI)
     * and ai_diagnosis_result (for tracking)
     */
    public void updateAIDiagnosis(int idAnalyse, String aiResult, String confidence, String mode) throws SQLException {
        String query = "UPDATE analyse SET resultat_technique = ?, ai_diagnosis_result = ?, " +
                      "ai_confidence_score = ?, diagnosis_mode = ?, ai_diagnosis_date = NOW() WHERE id_analyse = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, aiResult);
            ps.setString(2, aiResult);
            ps.setString(3, confidence);
            ps.setString(4, mode);
            ps.setInt(5, idAnalyse);
            ps.executeUpdate();
        }
    }

    /**
     * Find analyses by farm IDs (for user-scoped filtering)
     */
    public List<Analyse> findByFermes(List<Integer> fermeIds) throws SQLException {
        if (fermeIds == null || fermeIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Analyse> analyses = new ArrayList<>();
        String placeholders = String.join(",", fermeIds.stream().map(id -> "?").toList());
        String query = "SELECT * FROM analyse WHERE id_ferme_id IN (" + placeholders + ") ORDER BY date_analyse DESC";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            for (int i = 0; i < fermeIds.size(); i++) {
                ps.setInt(i + 1, fermeIds.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                analyses.add(mapResultSetToAnalyse(rs));
            }
        }
        return analyses;
    }

    /**
     * US10: Get analysis count per farm for visualization - filtered by user's farms
     */
    public List<Object[]> getAnalysisPerFarmStats(List<Integer> fermeIds) throws SQLException {
        if (fermeIds == null || fermeIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Object[]> stats = new ArrayList<>();
        String placeholders = String.join(",", fermeIds.stream().map(id -> "?").toList());
        String query = "SELECT id_ferme_id, COUNT(*) as count FROM analyse WHERE id_ferme_id IN (" + placeholders + ") GROUP BY id_ferme_id ORDER BY count DESC";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            for (int i = 0; i < fermeIds.size(); i++) {
                ps.setInt(i + 1, fermeIds.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                stats.add(new Object[]{
                    rs.getInt("id_ferme_id"),
                    rs.getInt("count")
                });
            }
        }
        return stats;
    }

    /**
     * Find analyses by farm ID
     */
    public List<Analyse> findByFerme(int idFerme) throws SQLException {
        List<Analyse> analyses = new ArrayList<>();
        String query = "SELECT * FROM analyse WHERE id_ferme_id = ? ORDER BY date_analyse DESC";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, idFerme);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                analyses.add(mapResultSetToAnalyse(rs));
            }
        }
        return analyses;
    }

    /**
     * Weather API Integration - Enrich diagnostic with weather data.
     * Uses Ferme.lieu to fetch current weather conditions.
     * KISS principle: Concatenates to resultat_technique, no schema changes.
     * 
     * @param analyse The analysis to enrich
     * @param fermeLieu The farm location (e.g., "Tunis, Tunisie")
     * @return The enriched technical result, or original if weather fetch fails
     * 
     * Railway Track: Ferme.lieu -> WeatherUtils -> Analyse.resultat_technique
     */
    public String enrichWithWeather(Analyse analyse, String fermeLieu) {
        if (fermeLieu == null || fermeLieu.trim().isEmpty()) {
            System.out.println("Weather enrichment skipped: No farm location provided");
            return analyse.getResultatTechnique();
        }
        
        try {
            // Fetch weather data asynchronously-safe
            WeatherUtils.WeatherData weather = WeatherUtils.fetchWeather(fermeLieu);
            
            if (weather.success()) {
                String currentResult = analyse.getResultatTechnique();
                if (currentResult == null) {
                    currentResult = "";
                }
                
                // Append weather data to diagnostic (KISS - no schema change)
                String weatherInfo = weather.formatForDiagnostic();
                String enrichedResult;
                
                if (currentResult.isEmpty()) {
                    enrichedResult = weatherInfo;
                } else {
                    enrichedResult = currentResult + " | " + weatherInfo;
                }
                
                System.out.println("Weather enrichment successful: " + weatherInfo);
                return enrichedResult;
            } else {
                // Graceful degradation - log warning but don't fail
                System.out.println("Weather enrichment skipped: " + weather.errorMessage());
                return analyse.getResultatTechnique();
            }
        } catch (Exception e) {
            // Edge case: Weather API failure shouldn't break analysis creation
            System.err.println("Weather enrichment error: " + e.getMessage());
            return analyse.getResultatTechnique();
        }
    }

    /**
     * Insert analysis with weather enrichment.
     * Convenience method that combines insert with weather data.
     * 
     * @param analyse The analysis to insert
     * @param fermeLieu The farm location for weather lookup
     * @throws SQLException if database operation fails
     */
    public void insertOneWithWeather(Analyse analyse, String fermeLieu) throws SQLException {
        // Enrich with weather before insert
        String enrichedResult = enrichWithWeather(analyse, fermeLieu);
        analyse.setResultatTechnique(enrichedResult);
        
        // Perform standard insert
        insertOne(analyse);
    }

    /**
     * Get farm location (lieu) by farm ID.
     * Helper method for weather enrichment.
     * 
     * @param idFerme The farm ID
     * @return The farm location string, or null if not found
     */
    public String getFermeLieu(int idFerme) {
        String query = "SELECT lieu FROM ferme WHERE id_ferme_id = ?";
        
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, idFerme);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getString("lieu");
            }
        } catch (SQLException e) {
            System.err.println("Failed to get farm location: " + e.getMessage());
        }
        return null;
    }

    /**
     * Map ResultSet to Analyse object
     */
    private Analyse mapResultSetToAnalyse(ResultSet rs) throws SQLException {
        Analyse analyse = new Analyse();
        analyse.setIdAnalyse(rs.getInt("id_analyse"));

        Timestamp ts = rs.getTimestamp("date_analyse");
        if (ts != null) {
            analyse.setDateAnalyse(rs.getTimestamp("date_analyse").toLocalDateTime());
        }

        analyse.setResultatTechnique(rs.getString("resultat_technique"));
        analyse.setIdTechnicien(rs.getInt("id_technicien_id"));
        analyse.setIdFerme(rs.getInt("id_ferme_id"));
        analyse.setImageUrl(rs.getString("image_url"));

        // Farmer request fields
        String statut = rs.getString("statut");
        analyse.setStatut(statut != null ? statut : "en_attente");

        analyse.setIdDemandeur(rs.getInt("id_demandeur"));
        analyse.setDescriptionDemande(rs.getString("description_demande"));

        int animalCible = rs.getInt("id_animal_cible");
        if (!rs.wasNull()) {
            analyse.setIdAnimalCible(animalCible);
        }

        int planteCible = rs.getInt("id_plante_cible");
        if (!rs.wasNull()) {
            analyse.setIdPlanteCible(planteCible);
        }

        // AI diagnosis fields
        analyse.setAiDiagnosisResult(rs.getString("ai_diagnosis_result"));

        Timestamp aiDate = rs.getTimestamp("ai_diagnosis_date");
        if (aiDate != null) {
            analyse.setAiDiagnosisDate(aiDate.toLocalDateTime());
        }

        analyse.setAiConfidenceScore(rs.getString("ai_confidence_score"));
        analyse.setDiagnosisMode(rs.getString("diagnosis_mode"));

        return analyse;
    }
}
