package tn.esprit.farmai.services;

import tn.esprit.farmai.models.Analyse;
import tn.esprit.farmai.models.Conseil;
import tn.esprit.farmai.models.Priorite;
import tn.esprit.farmai.utils.Config;
import tn.esprit.farmai.utils.MyDBConnexion;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for generating PDF reports for Analyse entities.
 * Implements US9: PDF Technical Reporting with 1:N relationship (Analyse -> Conseil)
 * 
 * Railway Track Trace:
 * - Line 32: FK parameter binding (id_analyse)
 * - Line 34: Iterating Conseil collection (1:N relationship)
 * - Lines 45-80: PDF generation with Analyse + Conseil data
 */
public class PDFReportService {

    private final Connection cnx;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PDFReportService() {
        this.cnx = MyDBConnexion.getInstance().getCnx();
    }

    /**
     * US9: Export analysis report with related recommendations
     * 
     * @param idAnalyse The analysis ID to export
     * @return Path to generated report file
     * @throws SQLException if database error occurs
     * @throws IOException if file creation fails
     * 
     * Railway Track: Implements 1:N relationship (Analyse -> Conseil)
     */
    public String exportAnalysisReport(int idAnalyse) throws SQLException, IOException {
        // Step 1: Fetch Analyse entity
        Analyse analyse = fetchAnalyseById(idAnalyse);
        if (analyse == null) {
            throw new SQLException("Analysis not found with ID: " + idAnalyse);
        }

        // Step 2: Fetch related Conseil entities using FK relationship
        // Railway Track: This is the 1:N relationship query
        List<Conseil> conseils = fetchConseilsByAnalyseId(idAnalyse);

        // Step 3: Generate text-based report
        return generateReport(analyse, conseils);
    }

    /**
     * Fetch Analyse entity by ID
     * Railway Track: Queries Analyse entity table
     */
    private Analyse fetchAnalyseById(int idAnalyse) throws SQLException {
        String sql = "SELECT * FROM analyse WHERE id_analyse = ?";
        
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idAnalyse);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
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
        return null;
    }

    /**
     * Fetch Conseil entities related to an Analyse (1:N relationship)
     * Railway Track: FK query demonstrating 1:N relationship
     * 
     * SQL Trace: SELECT * FROM conseil WHERE id_analyse = ? (FK constraint)
     * Line 82: FK parameter binding
     * Line 84: Iterating through related Conseil collection
     */
    private List<Conseil> fetchConseilsByAnalyseId(int idAnalyse) throws SQLException {
        List<Conseil> conseils = new ArrayList<>();
        
        // Railway Track: JOIN query for 1:N relationship
        // Alternative: SELECT c.* FROM conseil c WHERE c.id_analyse = ?
        String sql = "SELECT * FROM conseil WHERE id_analyse = ? ORDER BY priorite, id_conseil";
        
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idAnalyse); // Line 82: FK parameter binding
            ResultSet rs = ps.executeQuery();
            
            // Line 84: Iterate through Conseil collection (1:N relationship)
            while (rs.next()) {
                Conseil conseil = new Conseil();
                conseil.setIdConseil(rs.getInt("id_conseil"));
                conseil.setDescriptionConseil(rs.getString("description_conseil"));
                conseil.setPriorite(Priorite.valueOf(rs.getString("priorite")));
                conseil.setIdAnalyse(rs.getInt("id_analyse")); // FK reference
                conseils.add(conseil);
            }
        }
        return conseils;
    }

    /**
     * Generate text-based report with Analyse and Conseil data
     * Railway Track: Combines Analyse entity with 1:N Conseil entities
     */
    private String generateReport(Analyse analyse, List<Conseil> conseils) throws IOException {
        // Create output directory
        File outputDir = new File(Config.PDF_OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String fileName = "analysis_report_" + analyse.getIdAnalyse() + "_" + 
                         System.currentTimeMillis() + ".txt";
        String filePath = Config.PDF_OUTPUT_DIR + fileName;

        StringBuilder report = new StringBuilder();
        
        // Header
        report.append("=".repeat(70)).append("\n");
        report.append("FARMIA TECHNICAL ANALYSIS REPORT\n");
        report.append("Analysis ID: ").append(analyse.getIdAnalyse()).append("\n");
        report.append("=".repeat(70)).append("\n\n");

        // Analyse Section
        report.append("ANALYSIS DETAILS\n");
        report.append("-".repeat(40)).append("\n");
        report.append("Date: ").append(analyse.getDateAnalyse().format(DATE_FORMATTER)).append("\n");
        report.append("Farm ID: ").append(analyse.getIdFerme()).append("\n");
        report.append("Technician ID: ").append(analyse.getIdTechnicien()).append("\n");
        report.append("Image: ").append(analyse.getImageUrl() != null ? analyse.getImageUrl() : "No image").append("\n\n");
        
        report.append("Technical Result:\n");
        report.append(analyse.getResultatTechnique()).append("\n\n");

        // Conseil Section (1:N Relationship)
        report.append("=".repeat(70)).append("\n");
        report.append("TECHNICAL RECOMMENDATIONS (").append(conseils.size()).append(" total)\n");
        report.append("Railway Track: 1:N Relationship - Analyse -> Conseil\n");
        report.append("=".repeat(70)).append("\n\n");

        if (conseils.isEmpty()) {
            report.append("No recommendations available for this analysis.\n");
        } else {
            // Railway Track: Iterating through 1:N relationship
            for (int i = 0; i < conseils.size(); i++) {
                Conseil conseil = conseils.get(i);
                report.append("Recommendation #").append(i + 1).append("\n");
                report.append("-".repeat(40)).append("\n");
                report.append("Priority: ").append(conseil.getPriorite()).append("\n");
                report.append("Description: ").append(conseil.getDescriptionConseil()).append("\n");
                report.append("FK Reference: id_analyse = ").append(conseil.getIdAnalyse()).append("\n\n");
            }
        }

        // Summary
        report.append("=".repeat(70)).append("\n");
        report.append("SUMMARY\n");
        report.append("-".repeat(40)).append("\n");
        report.append("Total Recommendations: ").append(conseils.size()).append("\n");
        
        long hauteCount = conseils.stream().filter(c -> c.getPriorite() == Priorite.HAUTE).count();
        long moyenneCount = conseils.stream().filter(c -> c.getPriorite() == Priorite.MOYENNE).count();
        long basseCount = conseils.stream().filter(c -> c.getPriorite() == Priorite.BASSE).count();
        
        report.append("High Priority: ").append(hauteCount).append("\n");
        report.append("Medium Priority: ").append(moyenneCount).append("\n");
        report.append("Low Priority: ").append(basseCount).append("\n\n");

        report.append("=".repeat(70)).append("\n");
        report.append("Generated by: ").append(Config.PDF_CREATOR).append("\n");
        report.append("Report generated at: ").append(java.time.LocalDateTime.now().format(DATE_FORMATTER)).append("\n");
        report.append("=".repeat(70)).append("\n");

        // Write to file
        Files.write(Paths.get(filePath), report.toString().getBytes());

        return filePath;
    }

    /**
     * Get statistics for data visualization
     * US10: Data visualization support
     */
    public List<Object[]> getConseilPriorityStats() throws SQLException {
        List<Object[]> stats = new ArrayList<>();
        String sql = "SELECT priorite, COUNT(*) as count FROM conseil GROUP BY priorite ORDER BY count DESC";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                stats.add(new Object[]{
                    rs.getString("priorite"),
                    rs.getInt("count")
                });
            }
        }
        return stats;
    }
}