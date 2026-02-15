package tn.esprit.farmai.services;

import tn.esprit.farmai.interfaces.CRUD;
import tn.esprit.farmai.models.Analyse;
import tn.esprit.farmai.utils.MyDBConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Analyse CRUD operations.
 * Uses PreparedStatement for secure data handling (US1).
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
