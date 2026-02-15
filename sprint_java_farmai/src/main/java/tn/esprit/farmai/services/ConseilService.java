package tn.esprit.farmai.services;

import tn.esprit.farmai.interfaces.CRUD;
import tn.esprit.farmai.models.Conseil;
import tn.esprit.farmai.models.Priorite;
import tn.esprit.farmai.utils.MyDBConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Conseil CRUD operations.
 * Manages 1:N relationship between Conseil and Analyse.
 * US6: Image is handled as URL (String) in Analyse, not BLOB.
 */
public class ConseilService implements CRUD<Conseil> {

    private final Connection cnx;

    public ConseilService() {
        this.cnx = MyDBConnexion.getInstance().getCnx();
    }

    @Override
    public void insertOne(Conseil conseil) throws SQLException {
        String query = "INSERT INTO conseil (description_conseil, priorite, id_analyse) " +
                      "VALUES (?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, conseil.getDescriptionConseil());
            ps.setString(2, conseil.getPriorite().name());
            ps.setInt(3, conseil.getIdAnalyse());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                conseil.setIdConseil(rs.getInt(1));
            }
        }
    }

    @Override
    public void updateOne(Conseil conseil) throws SQLException {
        String query = "UPDATE conseil SET description_conseil = ?, priorite = ?, " +
                      "id_analyse = ? WHERE id_conseil = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, conseil.getDescriptionConseil());
            ps.setString(2, conseil.getPriorite().name());
            ps.setInt(3, conseil.getIdAnalyse());
            ps.setInt(4, conseil.getIdConseil());

            ps.executeUpdate();
        }
    }

    @Override
    public void deleteOne(int id) throws SQLException {
        String query = "DELETE FROM conseil WHERE id_conseil = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Conseil> selectAll() throws SQLException {
        List<Conseil> conseils = new ArrayList<>();
        String query = "SELECT * FROM conseil ORDER BY id_conseil DESC";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                conseils.add(mapResultSetToConseil(rs));
            }
        }
        return conseils;
    }

    /**
     * Find conseil by ID
     */
    public Optional<Conseil> findById(int id) throws SQLException {
        String query = "SELECT * FROM conseil WHERE id_conseil = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToConseil(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Find conseils by analysis ID (1:N relationship)
     */
    public List<Conseil> findByAnalyse(int idAnalyse) throws SQLException {
        List<Conseil> conseils = new ArrayList<>();
        String query = "SELECT * FROM conseil WHERE id_analyse = ? ORDER BY id_conseil DESC";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, idAnalyse);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                conseils.add(mapResultSetToConseil(rs));
            }
        }
        return conseils;
    }

    /**
     * Find conseils by priority level
     */
    public List<Conseil> findByPriorite(Priorite priorite) throws SQLException {
        List<Conseil> conseils = new ArrayList<>();
        String query = "SELECT * FROM conseil WHERE priorite = ? ORDER BY id_conseil DESC";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, priorite.name());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                conseils.add(mapResultSetToConseil(rs));
            }
        }
        return conseils;
    }

    /**
     * Map ResultSet to Conseil object
     */
    private Conseil mapResultSetToConseil(ResultSet rs) throws SQLException {
        Conseil conseil = new Conseil();
        conseil.setIdConseil(rs.getInt("id_conseil"));
        conseil.setDescriptionConseil(rs.getString("description_conseil"));

        // Handle Priorite enum
        String prioriteStr = rs.getString("priorite");
        if (prioriteStr != null && !prioriteStr.isEmpty()) {
            try {
                conseil.setPriorite(Priorite.valueOf(prioriteStr));
            } catch (IllegalArgumentException e) {
                conseil.setPriorite(Priorite.MOYENNE); // Default priority
            }
        } else {
            conseil.setPriorite(Priorite.MOYENNE);
        }

        conseil.setIdAnalyse(rs.getInt("id_analyse"));
        return conseil;
    }
}
