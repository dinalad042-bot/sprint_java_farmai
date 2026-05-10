package tn.esprit.farmai.services;

import tn.esprit.farmai.interfaces.CRUD;
import tn.esprit.farmai.models.Ferme;
import tn.esprit.farmai.utils.MyDBConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for Ferme CRUD operations.
 * Provides methods to manage farms and their relationship with users (fermiers).
 * 
 * Railway Track Trace:
 * - Entity: Ferme (this service)
 * - Relation: Ferme -> User (N:1 via id_user FK)
 * - Relation: Analyse -> Ferme (N:1 via id_ferme FK)
 */
public class FermeService implements CRUD<Ferme> {

    private final Connection cnx;

    public FermeService() {
        this.cnx = MyDBConnexion.getInstance().getCnx();
    }

    @Override
    public void insertOne(Ferme ferme) throws SQLException {
        String query = "INSERT INTO ferme (nom_ferme, lieu, surface, id_user) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ferme.getNomFerme());
            ps.setString(2, ferme.getLieu());
            ps.setDouble(3, ferme.getSurface());
            ps.setInt(4, ferme.getIdFermier());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating ferme failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ferme.setIdFerme(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating ferme failed, no ID obtained.");
                }
            }
        }
    }

    @Override
    public void updateOne(Ferme ferme) throws SQLException {
        String query = "UPDATE ferme SET nom_ferme = ?, lieu = ?, surface = ?, id_user = ? WHERE id_ferme = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, ferme.getNomFerme());
            ps.setString(2, ferme.getLieu());
            ps.setDouble(3, ferme.getSurface());
            ps.setInt(4, ferme.getIdFermier());
            ps.setInt(5, ferme.getIdFerme());

            ps.executeUpdate();
        }
    }

    @Override
    public void deleteOne(Ferme ferme) throws SQLException {
        String query = "DELETE FROM ferme WHERE id_ferme = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, ferme.getIdFerme());
            ps.executeUpdate();
        }
    }

@Override
    public List<Ferme> selectALL() throws SQLException {
        List<Ferme> fermes = new ArrayList<>();
        String query = "SELECT * FROM ferme ORDER BY id_ferme DESC";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                fermes.add(mapResultSetToFerme(rs));
            }
        }
        return fermes;
    }

    /**
     * Find a ferme by its ID
     */
    public Ferme findById(int idFerme) throws SQLException {
        String query = "SELECT * FROM ferme WHERE id_ferme = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, idFerme);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToFerme(rs);
            }
        }
        return null;
    }

    /**
     * Find fermes by lieu (location)
     */
    public List<Ferme> findByLieu(String lieu) throws SQLException {
        List<Ferme> fermes = new ArrayList<>();
        String query = "SELECT * FROM ferme WHERE lieu LIKE ? ORDER BY nom_ferme ASC";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, "%" + lieu + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                fermes.add(mapResultSetToFerme(rs));
            }
        }
        return fermes;
    }

    /**
     * Find all fermes owned by a specific fermier (user)
     */
    public List<Ferme> findByFermier(int idFermier) throws SQLException {
        List<Ferme> fermes = new ArrayList<>();
        String query = "SELECT * FROM ferme WHERE id_user = ? ORDER BY id_ferme DESC";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, idFermier);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                fermes.add(mapResultSetToFerme(rs));
            }
        }
        return fermes;
    }

    /**
     * Get list of ferme IDs owned by a fermier
     */
    public List<Integer> getFermeIdsByFermier(int idFermier) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String query = "SELECT id_ferme FROM ferme WHERE id_user = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, idFermier);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("id_ferme"));
            }
        }
        return ids;
    }

    /**
     * Map ResultSet to Ferme object
     */
    private Ferme mapResultSetToFerme(ResultSet rs) throws SQLException {
        Ferme ferme = new Ferme();
        ferme.setIdFerme(rs.getInt("id_ferme"));
        ferme.setNomFerme(rs.getString("nom_ferme"));
        ferme.setLieu(rs.getString("lieu"));
        ferme.setSurface(rs.getDouble("surface"));
        ferme.setIdFermier(rs.getInt("id_user"));
        return ferme;
    }
}
