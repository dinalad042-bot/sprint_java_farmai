package tn.esprit.farmai.services;

import tn.esprit.farmai.interfaces.CRUD;
import tn.esprit.farmai.models.Analyse;
import tn.esprit.farmai.models.Conseil;
import tn.esprit.farmai.utils.Config;
import tn.esprit.farmai.utils.MyDBConnexion;
import tn.esprit.farmai.utils.SimpleHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.List;
import com.itextpdf.layout.element.ListItem;
import com.itextpdf.layout.properties.TextAlignment;

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
        String query = "INSERT INTO analyse (date_analyse, resultat_technique, id_technicien, id_ferme, image_url) " +
                      "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, Timestamp.valueOf(analyse.getDateAnalyse()));
            ps.setString(2, analyse.getResultatTechnique());
            ps.setInt(3, analyse.getIdTechnicien());
            ps.setInt(4, analyse.getIdFerme());
            ps.setString(5, analyse.getImageUrl());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                analyse.setIdAnalyse(rs.getInt(1));
            }
        }
    }

    @Override
    public void updateOne(Analyse analyse) throws SQLException {
        String query = "UPDATE analyse SET date_analyse = ?, resultat_technique = ?, " +
                      "id_technicien = ?, id_ferme = ?, image_url = ? WHERE id_analyse = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setTimestamp(1, Timestamp.valueOf(analyse.getDateAnalyse()));
            ps.setString(2, analyse.getResultatTechnique());
            ps.setInt(3, analyse.getIdTechnicien());
            ps.setInt(4, analyse.getIdFerme());
            ps.setString(5, analyse.getImageUrl());
            ps.setInt(6, analyse.getIdAnalyse());

            ps.executeUpdate();
        }
    }

    @Override
    public void deleteOne(int id) throws SQLException {
        String query = "DELETE FROM analyse WHERE id_analyse = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Analyse> selectAll() throws SQLException {
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

        String systemContent = "You are an expert agricultural technician providing technical analysis for farm diagnostics. Provide concise, professional technical reports.";
        String userContent = "Analyze this agricultural observation and provide a technical diagnostic summary: " + observation;

        String jsonBody = buildGroqRequest(systemContent, userContent);

        try {
            String response = SimpleHttpClient.postJson(Config.GROQ_API_URL, jsonBody, "Bearer " + Config.GROQ_API_KEY);
            return extractContent(response);
        } catch (IOException e) {
            String msg = e.getMessage();
            if (msg == null) {
                throw new IOException("AI service connection failed. Please check your internet connection.");
            } else if (msg.contains("image") || msg.contains("does not support")) {
                throw new IOException("This AI model does not support image input. Please provide text-based observations only.");
            } else if (msg.contains("401") || msg.contains("Unauthorized")) {
                throw new IOException("AI service authentication failed. Please contact administrator.");
            } else if (msg.contains("429") || msg.contains("rate limit")) {
                throw new IOException("AI service rate limit reached. Please wait a moment and try again.");
            } else if (msg.contains("500") || msg.contains("502") || msg.contains("503")) {
                throw new IOException("AI service is temporarily unavailable. Please try again later.");
            }
            throw new IOException("Failed to generate AI diagnostic: " + msg);
        }
    }

    private String buildGroqRequest(String systemContent, String userContent) {
        String escapedSystem = escapeJson(systemContent);
        String escapedUser = escapeJson(userContent);
        
        return "{" +
            "\"model\":\"" + Config.GROQ_MODEL + "\"," +
            "\"messages\":[" +
                "{\"role\":\"system\",\"content\":\"" + escapedSystem + "\"}," +
                "{\"role\":\"user\",\"content\":\"" + escapedUser + "\"}" +
            "]," +
            "\"temperature\":0.7," +
            "\"max_tokens\":500" +
        "}";
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
        
        if (response.contains("\"error\"")) {
            String errorMsg = "AI service error";
            
            if (response.contains("\"message\":")) {
                int msgIdx = response.indexOf("\"message\":");
                int start = response.indexOf("\"", msgIdx + 10);
                if (start != -1) {
                    int end = response.indexOf("\"", start + 1);
                    while (end > start && response.charAt(end - 1) == '\\') {
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
            int contentIndex = response.indexOf("\"content\":");
            if (contentIndex != -1) {
                int startQuote = response.indexOf("\"", contentIndex + 11);
                if (startQuote != -1) {
                    int endQuote = startQuote + 1;
                    while (endQuote < response.length()) {
                        char c = response.charAt(endQuote);
                        if (c == '"' && response.charAt(endQuote - 1) != '\\') {
                            break;
                        }
                        endQuote++;
                    }
                    if (endQuote < response.length()) {
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
                        return result;
                    }
                }
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Failed to parse AI response: " + e.getMessage());
        }
        
        throw new IOException("Unexpected AI response format. Please try again.");
    }

    /**
     * US9: PDF Technical Reporting
     * 
     * @param idAnalyse The analysis ID to export
     * @return Path to generated report file
     * @throws SQLException if database error occurs
     * @throws IOException if file creation fails
     * 
     * Railway Track: Implements 1:N relationship (Analyse -> Conseil)
     */
    public String exportAnalysisToPDF(int idAnalyse) throws SQLException, IOException {
        Analyse analyse = findById(idAnalyse).orElseThrow(() -> 
            new SQLException("Analysis not found with ID: " + idAnalyse));
        
        List<Conseil> conseils = getConseilsByAnalyse(idAnalyse);

        File outputDir = new File(Config.PDF_OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String fileName = "analysis_" + idAnalyse + "_" + 
                         System.currentTimeMillis() + ".pdf";
        String filePath = Config.PDF_OUTPUT_DIR + fileName;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String dateFormatted = analyse.getDateAnalyse().format(formatter);

        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        document.add(new Paragraph("FARMIA TECHNICAL ANALYSIS REPORT")
            .setBold()
            .setFontSize(18)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(20));

        document.add(new Paragraph("Analysis ID: " + idAnalyse)
            .setFontSize(12)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(30));

        document.add(new Paragraph("ANALYSIS DETAILS")
            .setBold()
            .setFontSize(14)
            .setMarginBottom(10));

        document.add(new Paragraph("Date: " + dateFormatted).setMarginBottom(5));
        document.add(new Paragraph("Farm ID: " + analyse.getIdFerme()).setMarginBottom(5));
        document.add(new Paragraph("Technician ID: " + analyse.getIdTechnicien()).setMarginBottom(5));
        document.add(new Paragraph("Technical Result:").setBold().setMarginTop(15).setMarginBottom(5));
        document.add(new Paragraph(analyse.getResultatTechnique()).setMarginBottom(20));

        document.add(new Paragraph("TECHNICAL RECOMMENDATIONS (" + conseils.size() + ")")
            .setBold()
            .setFontSize(14)
            .setMarginTop(20)
            .setMarginBottom(10));

        if (conseils.isEmpty()) {
            document.add(new Paragraph("No recommendations available for this analysis.")
                .setItalic());
        } else {
            List pdfList = new List();
            for (Conseil conseil : conseils) {
                ListItem item = new ListItem();
                item.add(new Paragraph(conseil.getDescriptionConseil()));
                item.add(new Paragraph("Priority: " + conseil.getPriorite().name())
                    .setFontSize(10)
                    .setItalic());
                pdfList.add(item);
            }
            document.add(pdfList);
        }

        document.add(new Paragraph("Generated by: " + Config.PDF_CREATOR)
            .setFontSize(9)
            .setItalic()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(30));

        document.close();

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
        String query = "SELECT id_ferme, COUNT(*) as count FROM analyse GROUP BY id_ferme ORDER BY count DESC";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                stats.add(new Object[]{
                    rs.getInt("id_ferme"),
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
        String query = "SELECT * FROM analyse WHERE id_technicien = ? ORDER BY date_analyse DESC";

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
     * Find analyses by farm ID
     */
    public List<Analyse> findByFerme(int idFerme) throws SQLException {
        List<Analyse> analyses = new ArrayList<>();
        String query = "SELECT * FROM analyse WHERE id_ferme = ? ORDER BY date_analyse DESC";

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
     * Map ResultSet to Analyse object
     */
    private Analyse mapResultSetToAnalyse(ResultSet rs) throws SQLException {
        Analyse analyse = new Analyse();
        analyse.setIdAnalyse(rs.getInt("id_analyse"));
        analyse.setDateAnalyse(rs.getTimestamp("date_analyse").toLocalDateTime());
        analyse.setResultatTechnique(rs.getString("resultat_technique"));
        analyse.setIdTechnicien(rs.getInt("id_technicien"));
        analyse.setIdFerme(rs.getInt("id_ferme"));
        analyse.setImageUrl(rs.getString("image_url"));
        return analyse;
    }
}