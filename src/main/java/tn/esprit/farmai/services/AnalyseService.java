package tn.esprit.farmai.services;

import tn.esprit.farmai.models.Analyse;
import tn.esprit.farmai.utils.MyDBConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnalyseService {
    
    private Connection connection;
    
    public AnalyseService() {
        connection = MyDBConnexion.getInstance().getCnx();
    }
    
    // Create - Add new analyse
    public void ajouterAnalyse(Analyse analyse) throws SQLException {
        String query = "INSERT INTO analyse (date_analyse, resultat_technique, id_technicien, id_ferme, image_url, ai_diagnosis_result, ai_confidence_score, statut, id_demandeur, description_demande, id_animal_cible, id_plante_cible, diagnosis_mode) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, analyse.getDateAnalyse() != null ? Timestamp.valueOf(analyse.getDateAnalyse()) : new Timestamp(System.currentTimeMillis()));
            ps.setString(2, analyse.getResultatTechnique());
            ps.setObject(3, analyse.getIdTechnicien() != 0 ? analyse.getIdTechnicien() : null);
            ps.setInt(4, analyse.getIdFerme());
            ps.setString(5, analyse.getImageUrl());
            ps.setString(6, analyse.getAiDiagnosisResult());
            ps.setString(7, analyse.getAiConfidenceScore());
            ps.setString(8, analyse.getStatut() != null ? analyse.getStatut() : "en_attente");
            ps.setObject(9, analyse.getIdDemandeur() != 0 ? analyse.getIdDemandeur() : null);
            ps.setString(10, analyse.getDescriptionDemande());
            ps.setObject(11, analyse.getIdAnimalCible() != 0 ? analyse.getIdAnimalCible() : null);
            ps.setObject(12, analyse.getIdPlanteCible() != 0 ? analyse.getIdPlanteCible() : null);
            ps.setString(13, analyse.getDiagnosisMode());
            
            ps.executeUpdate();
            
            // Get generated ID
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    analyse.setIdAnalyse(generatedKeys.getInt(1));
                }
            }
        }
    }

    // Alias method for compatibility
    public void insertOne(Analyse analyse) throws SQLException {
        ajouterAnalyse(analyse);
    }
    
    // Read - Get all analyses
    public List<Analyse> afficherAnalyses() throws SQLException {
        List<Analyse> analyses = new ArrayList<>();
        String query = "SELECT * FROM analyse";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Analyse analyse = mapResultSetToAnalyse(rs);
                analyses.add(analyse);
            }
        }
        
        return analyses;
    }

    // Helper method to map ResultSet to Analyse object
    private Analyse mapResultSetToAnalyse(ResultSet rs) throws SQLException {
        Analyse analyse = new Analyse();
        analyse.setIdAnalyse(rs.getInt("id_analyse"));
        
        Timestamp dateTs = rs.getTimestamp("date_analyse");
        if (dateTs != null) {
            analyse.setDateAnalyse(dateTs.toLocalDateTime());
        }
        
        analyse.setResultatTechnique(rs.getString("resultat_technique"));
        analyse.setIdTechnicien(rs.getInt("id_technicien"));
        analyse.setIdFerme(rs.getInt("id_ferme"));
        analyse.setImageUrl(rs.getString("image_url"));
        analyse.setAiDiagnosisResult(rs.getString("ai_diagnosis_result"));
        analyse.setAiConfidenceScore(rs.getString("ai_confidence_score"));
        analyse.setStatut(rs.getString("statut"));
        analyse.setIdDemandeur(rs.getInt("id_demandeur"));
        analyse.setDescriptionDemande(rs.getString("description_demande"));
        analyse.setIdAnimalCible(rs.getInt("id_animal_cible"));
        analyse.setIdPlanteCible(rs.getInt("id_plante_cible"));
        analyse.setDiagnosisMode(rs.getString("diagnosis_mode"));
        
        Timestamp aiDateTs = rs.getTimestamp("ai_diagnosis_date");
        if (aiDateTs != null) {
            analyse.setAiDiagnosisDate(aiDateTs.toLocalDateTime());
        }
        
        return analyse;
    }
    
    // Read - Get analyse by ID
    public Analyse getAnalyseById(int id) throws SQLException {
        String query = "SELECT * FROM analyse WHERE id_analyse = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAnalyse(rs);
                }
            }
        }
        
        return null;
    }

    // Alias method for compatibility
    public Analyse findById(int id) throws SQLException {
        return getAnalyseById(id);
    }
    
    // Update - Modify existing analyse
    public void modifierAnalyse(Analyse analyse) throws SQLException {
        String query = "UPDATE analyse SET date_analyse = ?, resultat_technique = ?, id_technicien = ?, id_ferme = ?, image_url = ?, ai_diagnosis_result = ?, ai_confidence_score = ?, statut = ?, id_demandeur = ?, description_demande = ?, id_animal_cible = ?, id_plante_cible = ?, diagnosis_mode = ? WHERE id_analyse = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setTimestamp(1, analyse.getDateAnalyse() != null ? Timestamp.valueOf(analyse.getDateAnalyse()) : null);
            ps.setString(2, analyse.getResultatTechnique());
            ps.setObject(3, analyse.getIdTechnicien() != 0 ? analyse.getIdTechnicien() : null);
            ps.setInt(4, analyse.getIdFerme());
            ps.setString(5, analyse.getImageUrl());
            ps.setString(6, analyse.getAiDiagnosisResult());
            ps.setString(7, analyse.getAiConfidenceScore());
            ps.setString(8, analyse.getStatut());
            ps.setObject(9, analyse.getIdDemandeur() != 0 ? analyse.getIdDemandeur() : null);
            ps.setString(10, analyse.getDescriptionDemande());
            ps.setObject(11, analyse.getIdAnimalCible() != 0 ? analyse.getIdAnimalCible() : null);
            ps.setObject(12, analyse.getIdPlanteCible() != 0 ? analyse.getIdPlanteCible() : null);
            ps.setString(13, analyse.getDiagnosisMode());
            ps.setInt(14, analyse.getIdAnalyse());
            
            ps.executeUpdate();
        }
    }

    // Alias method for compatibility
    public void updateOne(Analyse analyse) throws SQLException {
        modifierAnalyse(analyse);
    }
    
    // Delete - Remove analyse
    public void supprimerAnalyse(int id) throws SQLException {
        String query = "DELETE FROM analyse WHERE id_analyse = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Alias method for compatibility
    public void deleteOne(Analyse analyse) throws SQLException {
        supprimerAnalyse(analyse.getIdAnalyse());
    }
    
    // Get analyses by ferme
    public List<Analyse> getAnalysesByFerme(int idFerme) throws SQLException {
        List<Analyse> analyses = new ArrayList<>();
        String query = "SELECT * FROM analyse WHERE id_ferme = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idFerme);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    analyses.add(mapResultSetToAnalyse(rs));
                }
            }
        }
        
        return analyses;
    }

    // Alias method for compatibility
    public List<Analyse> findByFerme(int idFerme) throws SQLException {
        return getAnalysesByFerme(idFerme);
    }
    
    // Get analyses by technicien
    public List<Analyse> getAnalysesByTechnicien(int idTechnicien) throws SQLException {
        List<Analyse> analyses = new ArrayList<>();
        String query = "SELECT * FROM analyse WHERE id_technicien = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idTechnicien);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    analyses.add(mapResultSetToAnalyse(rs));
                }
            }
        }
        
        return analyses;
    }

    // Alias method for compatibility
    public List<Analyse> findByTechnicien(int idTechnicien) throws SQLException {
        return getAnalysesByTechnicien(idTechnicien);
    }

    // Get analyses by demandeur (farmer)
    public List<Analyse> findByDemandeur(int idDemandeur) throws SQLException {
        List<Analyse> analyses = new ArrayList<>();
        String query = "SELECT * FROM analyse WHERE id_demandeur = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idDemandeur);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    analyses.add(mapResultSetToAnalyse(rs));
                }
            }
        }
        
        return analyses;
    }

    // Missing method: selectALL - alias for afficherAnalyses
    public List<Analyse> selectALL() throws SQLException {
        return afficherAnalyses();
    }

    // Missing method: findByFermes - get analyses for multiple farms
    public List<Analyse> findByFermes(List<Integer> fermeIds) throws SQLException {
        if (fermeIds == null || fermeIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Analyse> analyses = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT * FROM analyse WHERE id_ferme IN (");
        
        for (int i = 0; i < fermeIds.size(); i++) {
            query.append("?");
            if (i < fermeIds.size() - 1) {
                query.append(",");
            }
        }
        query.append(")");
        
        try (PreparedStatement ps = connection.prepareStatement(query.toString())) {
            for (int i = 0; i < fermeIds.size(); i++) {
                ps.setInt(i + 1, fermeIds.get(i));
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    analyses.add(mapResultSetToAnalyse(rs));
                }
            }
        }
        
        return analyses;
    }

    // Missing method: findPendingRequests - get analyses with pending status
    public List<Analyse> findPendingRequests() throws SQLException {
        List<Analyse> analyses = new ArrayList<>();
        String query = "SELECT * FROM analyse WHERE statut = 'en_attente' OR statut IS NULL";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                analyses.add(mapResultSetToAnalyse(rs));
            }
        }
        
        return analyses;
    }

    // Missing method: findInProgressByTechnicien - get in-progress analyses for a technician
    public List<Analyse> findInProgressByTechnicien(int technicienId) throws SQLException {
        List<Analyse> analyses = new ArrayList<>();
        String query = "SELECT * FROM analyse WHERE id_technicien = ? AND statut = 'en_cours'";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, technicienId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    analyses.add(mapResultSetToAnalyse(rs));
                }
            }
        }
        
        return analyses;
    }

    // Helper method to convert Timestamp to LocalDateTime safely
    private void setDateAnalyseFromTimestamp(Analyse analyse, Timestamp timestamp) {
        if (timestamp != null) {
            analyse.setDateAnalyse(timestamp.toLocalDateTime());
        }
    }

    // Missing method: takeRequest - assign a technician to an analysis
    public void takeRequest(int analyseId, int technicienId) throws SQLException {
        String query = "UPDATE analyse SET id_technicien = ?, statut = 'en_cours' WHERE id_analyse = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, technicienId);
            ps.setInt(2, analyseId);
            ps.executeUpdate();
        }
    }

    // Missing method: completeAnalyse - complete an analysis with results
    public void completeAnalyse(int analyseId, String result) throws SQLException {
        String query = "UPDATE analyse SET resultat_technique = ?, statut = 'terminee' WHERE id_analyse = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, result);
            ps.setInt(2, analyseId);
            ps.executeUpdate();
        }
    }

    // Missing method: updateAIDiagnosis - update AI diagnosis results
    public void updateAIDiagnosis(int analyseId, String result, String confidence, String mode) throws SQLException {
        String query = "UPDATE analyse SET ai_diagnosis_result = ?, ai_confidence_score = ?, diagnosis_mode = ? WHERE id_analyse = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, result);
            ps.setString(2, confidence);
            ps.setString(3, mode);
            ps.setInt(4, analyseId);
            ps.executeUpdate();
        }
    }

    // Missing method: getConseilPriorityStats - get priority statistics for charts
    public List<Object[]> getConseilPriorityStats() throws SQLException {
        List<Object[]> stats = new ArrayList<>();
        String query = "SELECT c.priorite, COUNT(*) as count FROM conseil c " +
                      "JOIN analyse a ON c.id_analyse_id = a.id_analyse " +
                      "GROUP BY c.priorite";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] stat = new Object[2];
                stat[0] = rs.getString("priorite");
                stat[1] = rs.getInt("count");
                stats.add(stat);
            }
        }
        
        return stats;
    }

    // Missing method: getAnalysisPerFarmStats - get analysis count per farm
    public List<Object[]> getAnalysisPerFarmStats(List<Integer> fermeIds) throws SQLException {
        if (fermeIds == null || fermeIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Object[]> stats = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT id_ferme, COUNT(*) as count FROM analyse WHERE id_ferme IN (");
        
        for (int i = 0; i < fermeIds.size(); i++) {
            query.append("?");
            if (i < fermeIds.size() - 1) {
                query.append(",");
            }
        }
        query.append(") GROUP BY id_ferme");
        
        try (PreparedStatement ps = connection.prepareStatement(query.toString())) {
            for (int i = 0; i < fermeIds.size(); i++) {
                ps.setInt(i + 1, fermeIds.get(i));
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] stat = new Object[2];
                    stat[0] = rs.getInt("id_ferme");
                    stat[1] = rs.getInt("count");
                    stats.add(stat);
                }
            }
        }
        
        return stats;
    }

    // Overloaded method: getAnalysisPerFarmStats - get analysis count for all farms
    public List<Object[]> getAnalysisPerFarmStats() throws SQLException {
        List<Object[]> stats = new ArrayList<>();
        String query = "SELECT id_ferme, COUNT(*) as count FROM analyse GROUP BY id_ferme";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] stat = new Object[2];
                stat[0] = rs.getInt("id_ferme");
                stat[1] = rs.getInt("count");
                stats.add(stat);
            }
        }
        
        return stats;
    }

    // Missing method: createFarmerRequest - create a farmer request
    public void createFarmerRequest(Analyse analyse) throws SQLException {
        ajouterAnalyse(analyse);
    }

    // Missing method: generateAIDiagnostic - generate AI diagnosis
    public String generateAIDiagnostic(String imageUrl) throws SQLException {
        // Placeholder implementation - in real scenario this would call AI service
        return "AI diagnosis result for image: " + imageUrl;
    }

    // Missing method: exportAnalysisToPDF - export analysis to PDF
    public String exportAnalysisToPDF(int analyseId) throws SQLException {
        // Placeholder implementation - in real scenario this would generate PDF
        System.out.println("Exporting analysis " + analyseId + " to PDF");
        return null;
    }
}