package tn.esprit.farmai.services;

import tn.esprit.farmai.models.Analyse;
import tn.esprit.farmai.models.Conseil;
import tn.esprit.farmai.models.Ferme;
import tn.esprit.farmai.models.Priorite;
import tn.esprit.farmai.utils.Config;
import tn.esprit.farmai.utils.SimpleHttpClient;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for generating intelligent analysis reports.
 * Combines analysis data, weather conditions, and AI-generated executive summaries.
 */
public class IntelligentReportService {
    
    /**
     * Progress callback interface for reporting generation status.
     */
    public interface ProgressCallback {
        void onProgress(String step, int percent);
    }
    
    private final AnalyseService analyseService;
    private final ConseilService conseilService;
    private final FermeService fermeService;
    private final WeatherService weatherService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public IntelligentReportService() {
        this.analyseService = new AnalyseService();
        this.conseilService = new ConseilService();
        this.fermeService = new FermeService();
        this.weatherService = new WeatherService();
    }
    
    /**
     * Generate a comprehensive farm analysis report with AI summary.
     * 
     * @param fermeId Farm ID
     * @param startDate Optional start date filter (null for all time)
     * @param endDate Optional end date filter (null for all time)
     * @return Path to generated PDF file
     * @throws ReportException if report generation fails
     */
    public String generateFarmReport(int fermeId, LocalDateTime startDate, LocalDateTime endDate) throws ReportException {
        try {
            // 1. Fetch farm data
            Ferme ferme = fermeService.findById(fermeId);
            if (ferme == null) {
                throw new ReportException("Farm not found with ID: " + fermeId);
            }
            
            // 2. Fetch analyses for farm
            List<Analyse> allAnalyses = analyseService.findByFerme(fermeId);
            
            // 3. Filter by date if provided
            final LocalDateTime finalStartDate = startDate;
            final LocalDateTime finalEndDate = endDate;
            List<Analyse> analyses;
            if (startDate != null || endDate != null) {
                analyses = allAnalyses.stream()
                    .filter(a -> {
                        LocalDateTime date = a.getDateAnalyse();
                        if (finalStartDate != null && date.isBefore(finalStartDate)) return false;
                        if (finalEndDate != null && date.isAfter(finalEndDate)) return false;
                        return true;
                    })
                    .toList();
            } else {
                analyses = allAnalyses;
            }
            
            if (analyses.isEmpty()) {
                throw new ReportException("No analyses found for this farm in the selected date range.");
            }
            
            // 4. Fetch related conseils
            List<Conseil> allConseils = conseilService.selectALL();
            List<Conseil> farmConseils = allConseils.stream()
                .filter(c -> analyses.stream().anyMatch(a -> a.getIdAnalyse() == c.getIdAnalyse()))
                .toList();
            
            // 5. Generate AI executive summary
            String executiveSummary = generateExecutiveSummary(analyses, farmConseils, ferme);
            
            // 6. Build PDF
            return buildPdfReport(ferme, analyses, farmConseils, executiveSummary);
            
        } catch (SQLException e) {
            throw new ReportException("Database error: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new ReportException("PDF generation error: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate report with progress updates.
     */
    public String generateFarmReport(int fermeId, LocalDateTime startDate, LocalDateTime endDate, ProgressCallback callback) throws ReportException {
        try {
            reportProgress(callback, "Recuperation des donnees de la ferme...", 10);
            
            // 1. Fetch farm data
            Ferme ferme = fermeService.findById(fermeId);
            if (ferme == null) {
                throw new ReportException("Farm not found with ID: " + fermeId);
            }
            
            reportProgress(callback, "Chargement des analyses...", 25);
            
            // 2. Fetch analyses for farm
            List<Analyse> allAnalyses = analyseService.findByFerme(fermeId);
            
            // 3. Filter by date if provided
            final LocalDateTime finalStartDate = startDate;
            final LocalDateTime finalEndDate = endDate;
            List<Analyse> analyses;
            if (startDate != null || endDate != null) {
                analyses = allAnalyses.stream()
                    .filter(a -> {
                        LocalDateTime date = a.getDateAnalyse();
                        if (finalStartDate != null && date.isBefore(finalStartDate)) return false;
                        if (finalEndDate != null && date.isAfter(finalEndDate)) return false;
                        return true;
                    })
                    .toList();
            } else {
                analyses = allAnalyses;
            }
            
            if (analyses.isEmpty()) {
                throw new ReportException("No analyses found for this farm in the selected date range.");
            }
            
            reportProgress(callback, "Chargement des recommandations...", 40);
            
            // 4. Fetch related conseils
            List<Conseil> allConseils = conseilService.selectALL();
            List<Conseil> farmConseils = allConseils.stream()
                .filter(c -> analyses.stream().anyMatch(a -> a.getIdAnalyse() == c.getIdAnalyse()))
                .toList();
            
            reportProgress(callback, "Generation du resume IA...", 60);
            
            // 5. Generate AI executive summary
            String executiveSummary = generateExecutiveSummary(analyses, farmConseils, ferme);
            
            reportProgress(callback, "Construction du PDF...", 80);
            
            // 6. Build PDF
            String result = buildPdfReport(ferme, analyses, farmConseils, executiveSummary);
            
            reportProgress(callback, "Termine!", 100);
            
            return result;
            
        } catch (SQLException e) {
            throw new ReportException("Database error: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new ReportException("PDF generation error: " + e.getMessage(), e);
        }
    }
    
    private void reportProgress(ProgressCallback callback, String step, int percent) {
        if (callback != null) {
            callback.onProgress(step, percent);
        }
    }

    /**
     * Generate AI executive summary using Groq API.
     */
    private String generateExecutiveSummary(List<Analyse> analyses, List<Conseil> conseils, Ferme ferme) {
        try {
            String apiKey = Config.GROQ_API_KEY;
            if (apiKey == null || apiKey.trim().isEmpty()) {
                return "Resume indisponible - API key non configuree.";
            }
            
            // Build analysis summary
            StringBuilder prompt = new StringBuilder();
            prompt.append("Analyse ces donnees agricoles et genere un resume executif:\n\n");
            prompt.append("Ferme: ").append(ferme.getNomFerme()).append("\n");
            prompt.append("Lieu: ").append(ferme.getLieu()).append("\n");
            prompt.append("Nombre d'analyses: ").append(analyses.size()).append("\n");
            prompt.append("Nombre de recommandations: ").append(conseils.size()).append("\n\n");
            
            // Add summary of analyses
            prompt.append("Apercu des analyses:\n");
            for (int i = 0; i < Math.min(analyses.size(), 5); i++) {
                Analyse a = analyses.get(i);
                prompt.append("- Analyse #").append(a.getIdAnalyse())
                      .append(" (").append(a.getDateAnalyse().format(DATE_FORMATTER)).append(")\n");
            }
            if (analyses.size() > 5) {
                prompt.append("- ... et ").append(analyses.size() - 5).append(" autres\n");
            }
            
            prompt.append("\nGenere un resume executif professionnel en francais.");
            prompt.append(" Format: 3-4 phrases maximum. Sois concis et informatif.");
            
            // Build request
            JSONObject request = new JSONObject();
            request.put("model", Config.GROQ_MODEL);
            request.put("max_tokens", 300);
            request.put("temperature", 0.5);
            
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject()
                .put("role", "system")
                .put("content", "Tu es un redacteur de rapports agricoles professionnels. Sois concis."));
            messages.put(new JSONObject()
                .put("role", "user")
                .put("content", prompt.toString()));
            request.put("messages", messages);
            
            // Call API
            String response = SimpleHttpClient.postJson(
                Config.GROQ_API_URL,
                request.toString(),
                "Bearer " + apiKey
            );
            
            // Parse response
            JSONObject jsonResponse = new JSONObject(response);
            if (jsonResponse.has("choices")) {
                String content = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
                return content.trim();
            }
            
            return "Resume indisponible.";
            
        } catch (Exception e) {
            System.err.println("Failed to generate executive summary: " + e.getMessage());
            return "Resume indisponible - erreur de generation.";
        }
    }
    
    /**
     * Build the PDF report with enhanced styling and statistics.
     */
    private String buildPdfReport(Ferme ferme, List<Analyse> analyses, 
                                   List<Conseil> conseils, String executiveSummary) throws IOException {
        
        File outputDir = new File(Config.PDF_OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        String fileName = "rapport_ferme_" + ferme.getIdFerme() + "_" + System.currentTimeMillis() + ".pdf";
        String filePath = Config.PDF_OUTPUT_DIR + fileName;
        
        // Calculate statistics
        long highPriorityCount = conseils.stream().filter(c -> c.getPriorite() == Priorite.HAUTE).count();
        long mediumPriorityCount = conseils.stream().filter(c -> c.getPriorite() == Priorite.MOYENNE).count();
        long lowPriorityCount = conseils.stream().filter(c -> c.getPriorite() == Priorite.BASSE).count();
        
        try (PDDocument document = new PDDocument()) {
            // Page 1: Professional Cover Page
            PDPage page1 = new PDPage(PDRectangle.A4);
            document.addPage(page1);
            
            float margin = 50;
            float yPosition = page1.getMediaBox().getHeight() - margin;
            float pageWidth = page1.getMediaBox().getWidth() - 2 * margin;
            
            try (PDPageContentStream cs = new PDPageContentStream(document, page1)) {
                // Header background
                cs.setNonStrokingColor(0.2f, 0.6f, 0.3f); // Green color
                cs.addRect(0, page1.getMediaBox().getHeight() - 100, page1.getMediaBox().getWidth(), 100);
                cs.fill();
                
                // Title in header
                cs.setNonStrokingColor(1, 1, 1); // White text
                cs.setFont(PDType1Font.HELVETICA_BOLD, 24);
                cs.beginText();
                cs.newLineAtOffset(margin, page1.getMediaBox().getHeight() - 60);
                cs.showText("RAPPORT AGRICOLE INTELLIGENT");
                cs.endText();
                
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.beginText();
                cs.newLineAtOffset(margin, page1.getMediaBox().getHeight() - 80);
                cs.showText("Analyse Complete avec Intelligence Artificielle");
                cs.endText();
                
                // Reset color
                cs.setNonStrokingColor(0, 0, 0);
                yPosition = page1.getMediaBox().getHeight() - 130;
                
                // Farm info box
                drawColoredBox(cs, margin, yPosition - 80, pageWidth, 70, 0.95f, 0.95f, 0.95f);
                
                cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
                cs.setNonStrokingColor(0.2f, 0.6f, 0.3f);
                cs.beginText();
                cs.newLineAtOffset(margin + 10, yPosition - 25);
                cs.showText(ferme.getNomFerme());
                cs.endText();
                
                cs.setNonStrokingColor(0, 0, 0);
                cs.setFont(PDType1Font.HELVETICA, 11);
                cs.beginText();
                cs.newLineAtOffset(margin + 10, yPosition - 45);
                cs.showText("Lieu: " + ferme.getLieu());
                cs.endText();
                
                cs.beginText();
                cs.newLineAtOffset(margin + 10, yPosition - 65);
                cs.showText("Genere le: " + LocalDateTime.now().format(DATE_FORMATTER));
                cs.endText();
                
                yPosition -= 100;
                
                // Executive Summary Section with styled header
                yPosition = drawStyledSectionHeader(cs, "RESUME EXECUTIF (IA)", margin, yPosition, 0.2f, 0.6f, 0.3f);
                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.setNonStrokingColor(0.2f, 0.2f, 0.2f);
                yPosition = drawWrappedText(cs, executiveSummary, margin + 10, yPosition, pageWidth - 20, 10);
                yPosition -= 25;
                
                // Statistics Section with visual boxes
                yPosition = drawStyledSectionHeader(cs, "STATISTIQUES", margin, yPosition, 0.2f, 0.6f, 0.3f);
                
                // Draw stat boxes
                float boxWidth = (pageWidth - 20) / 3;
                drawStatBox(cs, margin + 5, yPosition - 50, boxWidth - 10, 40, 
                           "Analyses", String.valueOf(analyses.size()), 0.2f, 0.5f, 0.8f);
                drawStatBox(cs, margin + boxWidth + 5, yPosition - 50, boxWidth - 10, 40, 
                           "Conseils", String.valueOf(conseils.size()), 1.0f, 0.6f, 0.2f);
                drawStatBox(cs, margin + 2 * boxWidth + 5, yPosition - 50, boxWidth - 10, 40, 
                           "Priorite Haute", String.valueOf(highPriorityCount), 0.9f, 0.3f, 0.3f);
                
                yPosition -= 70;
                
                // Additional stats
                drawColoredBox(cs, margin, yPosition - 30, pageWidth, 25, 0.98f, 0.95f, 0.9f);
                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.setNonStrokingColor(0, 0, 0);
                cs.beginText();
                cs.newLineAtOffset(margin + 10, yPosition - 20);
                cs.showText("Priorites: Haute=" + highPriorityCount + " | Moyenne=" + mediumPriorityCount + " | Basse=" + lowPriorityCount);
                cs.endText();
            }
            
            // Page 2: Analysis Details with better formatting
            if (!analyses.isEmpty()) {
                PDPage page2 = new PDPage(PDRectangle.A4);
                document.addPage(page2);
                yPosition = page2.getMediaBox().getHeight() - margin;
                
                try (PDPageContentStream cs = new PDPageContentStream(document, page2)) {
                    yPosition = drawStyledSectionHeader(cs, "DETAIL DES ANALYSES", margin, yPosition, 0.2f, 0.6f, 0.3f);
                    
                    for (int i = 0; i < analyses.size() && yPosition > 150; i++) {
                        Analyse a = analyses.get(i);
                        
                        // Analysis box
                        drawColoredBox(cs, margin, yPosition - 60, pageWidth, 55, 0.97f, 0.97f, 1.0f);
                        
                        cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
                        cs.setNonStrokingColor(0.1f, 0.4f, 0.7f);
                        cs.beginText();
                        cs.newLineAtOffset(margin + 10, yPosition - 20);
                        cs.showText("Analyse #" + a.getIdAnalyse());
                        cs.endText();
                        
                        cs.setFont(PDType1Font.HELVETICA, 10);
                        cs.setNonStrokingColor(0.5f, 0.5f, 0.5f);
                        cs.beginText();
                        cs.newLineAtOffset(margin + 150, yPosition - 20);
                        cs.showText(a.getDateAnalyse().format(DATE_FORMATTER));
                        cs.endText();
                        
                        // Result preview
                        String result = a.getResultatTechnique();
                        if (result != null && !result.isEmpty()) {
                            cs.setNonStrokingColor(0.2f, 0.2f, 0.2f);
                            cs.setFont(PDType1Font.HELVETICA, 9);
                            String preview = result.length() > 150 ? result.substring(0, 150) + "..." : result;
                            yPosition = drawWrappedText(cs, preview, margin + 10, yPosition - 40, pageWidth - 20, 9);
                        }
                        yPosition -= 70;
                    }
                }
            }
            
            // Page 3: Conseils with priority colors
            if (!conseils.isEmpty()) {
                PDPage page3 = new PDPage(PDRectangle.A4);
                document.addPage(page3);
                yPosition = page3.getMediaBox().getHeight() - margin;
                
                try (PDPageContentStream cs = new PDPageContentStream(document, page3)) {
                    yPosition = drawStyledSectionHeader(cs, "RECOMMANDATIONS", margin, yPosition, 0.2f, 0.6f, 0.3f);
                    
                    for (int i = 0; i < conseils.size() && yPosition > 150; i++) {
                        Conseil c = conseils.get(i);
                        
                        // Priority color
                        float[] priorityColor = getPriorityColor(c.getPriorite());
                        drawColoredBox(cs, margin, yPosition - 50, pageWidth, 45, 
                                      priorityColor[0], priorityColor[1], priorityColor[2]);
                        
                        cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
                        cs.setNonStrokingColor(0, 0, 0);
                        cs.beginText();
                        cs.newLineAtOffset(margin + 10, yPosition - 20);
                        String priorityLabel = getPriorityLabel(c.getPriorite());
                        cs.showText((i + 1) + ". [" + priorityLabel + "]");
                        cs.endText();
                        
                        cs.setFont(PDType1Font.HELVETICA, 9);
                        yPosition = drawWrappedText(cs, c.getDescriptionConseil(), 
                                                   margin + 15, yPosition - 38, pageWidth - 25, 9);
                        yPosition -= 60;
                    }
                }
            }
            
            // Footer with page numbers
            addPageNumbers(document);
            
            document.save(filePath);
        }
        
        return filePath;
    }
    
    /**
     * Draw a colored box.
     */
    private void drawColoredBox(PDPageContentStream cs, float x, float y, float width, float height, 
                                 float r, float g, float b) throws IOException {
        cs.setNonStrokingColor(r, g, b);
        cs.addRect(x, y, width, height);
        cs.fill();
        cs.setNonStrokingColor(0, 0, 0); // Reset to black
    }
    
    /**
     * Draw a stat box with label and value.
     */
    private void drawStatBox(PDPageContentStream cs, float x, float y, float width, float height,
                             String label, String value, float r, float g, float b) throws IOException {
        // Box background
        cs.setNonStrokingColor(r, g, b);
        cs.addRect(x, y + height - 20, width, 20);
        cs.fill();
        
        // Value background
        cs.setNonStrokingColor(0.95f, 0.95f, 0.95f);
        cs.addRect(x, y, width, height - 20);
        cs.fill();
        
        // Label
        cs.setNonStrokingColor(1, 1, 1);
        cs.setFont(PDType1Font.HELVETICA_BOLD, 9);
        cs.beginText();
        cs.newLineAtOffset(x + 5, y + height - 15);
        cs.showText(label);
        cs.endText();
        
        // Value
        cs.setNonStrokingColor(0, 0, 0);
        cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
        cs.beginText();
        cs.newLineAtOffset(x + width/2 - 10, y + 10);
        cs.showText(value);
        cs.endText();
    }
    
    /**
     * Draw styled section header with color.
     */
    private float drawStyledSectionHeader(PDPageContentStream cs, String header, float x, float y, 
                                           float r, float g, float b) throws IOException {
        // Header line
        cs.setStrokingColor(r, g, b);
        cs.setLineWidth(2);
        cs.moveTo(x, y);
        cs.lineTo(x + 200, y);
        cs.stroke();
        
        // Header text
        cs.setNonStrokingColor(r, g, b);
        cs.setFont(PDType1Font.HELVETICA_BOLD, 13);
        cs.beginText();
        cs.newLineAtOffset(x, y - 18);
        cs.showText(header);
        cs.endText();
        
        cs.setStrokingColor(0, 0, 0); // Reset
        return y - 30;
    }
    
    /**
     * Get color for priority level.
     */
    private float[] getPriorityColor(Priorite priorite) {
        return switch (priorite) {
            case HAUTE -> new float[]{1.0f, 0.9f, 0.9f}; // Light red
            case MOYENNE -> new float[]{1.0f, 0.95f, 0.9f}; // Light orange
            case BASSE -> new float[]{0.9f, 1.0f, 0.9f}; // Light green
        };
    }
    
    /**
     * Get label for priority level.
     */
    private String getPriorityLabel(Priorite priorite) {
        return switch (priorite) {
            case HAUTE -> "HAUTE";
            case MOYENNE -> "MOYENNE";
            case BASSE -> "BASSE";
        };
    }
    
    /**
     * Add page numbers to all pages.
     */
    private void addPageNumbers(PDDocument document) throws IOException {
        int pageCount = document.getNumberOfPages();
        for (int i = 0; i < pageCount; i++) {
            PDPage page = document.getPage(i);
            try (PDPageContentStream cs = new PDPageContentStream(document, page, 
                    PDPageContentStream.AppendMode.APPEND, true, true)) {
                cs.setFont(PDType1Font.HELVETICA, 8);
                cs.setNonStrokingColor(0.5f, 0.5f, 0.5f);
                cs.beginText();
                cs.newLineAtOffset(page.getMediaBox().getWidth() / 2 - 20, 30);
                cs.showText("Page " + (i + 1) + " / " + pageCount);
                cs.endText();
            }
        }
    }
    
    /**
     * Draw section header in PDF.
     */
    private float drawSectionHeader(PDPageContentStream cs, String header, float x, float y) throws IOException {
        cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
        cs.beginText();
        cs.newLineAtOffset(x, y);
        cs.showText(header);
        cs.endText();
        return y - 20;
    }
    
    /**
     * Draw wrapped text in PDF.
     */
    private float drawWrappedText(PDPageContentStream cs, String text, float x, float y, 
                                   float maxWidth, int fontSize) throws IOException {
        cs.setFont(PDType1Font.HELVETICA, fontSize);
        
        String cleanText = text.replace("\r", " ").replace("\n", " ").replaceAll("\\s+", " ").trim();
        
        if (cleanText.isEmpty()) {
            return y;
        }
        
        float charWidth = fontSize * 0.5f;
        int charsPerLine = (int) (maxWidth / charWidth);
        
        String[] words = cleanText.split(" ");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > charsPerLine) {
                cs.beginText();
                cs.newLineAtOffset(x, y);
                cs.showText(currentLine.toString().trim());
                cs.endText();
                y -= fontSize + 2;
                currentLine = new StringBuilder(word + " ");
            } else {
                currentLine.append(word).append(" ");
            }
        }
        
        if (currentLine.length() > 0) {
            cs.beginText();
            cs.newLineAtOffset(x, y);
            cs.showText(currentLine.toString().trim());
            cs.endText();
            y -= fontSize + 2;
        }
        
        return y;
    }
    
    /**
     * Custom exception for report generation errors.
     */
    public static class ReportException extends Exception {
        public ReportException(String message) {
            super(message);
        }
        
        public ReportException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
