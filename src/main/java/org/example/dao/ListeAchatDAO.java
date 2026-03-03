package org.example.dao;

import org.example.entity.ListeAchat;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO CRUD pour la table associative ListeAchat.
 */
public class ListeAchatDAO {

    private static final String SELECT_BY_ACHAT = "SELECT la.id, la.id_achat, la.id_service, la.quantite, la.prix_unitaire, s.nom AS nom_service " +
            "FROM liste_achat la JOIN service s ON la.id_service = s.id_service WHERE la.id_achat = ?";
    private static final String INSERT = "INSERT INTO liste_achat (id_achat, id_service, quantite, prix_unitaire) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE liste_achat SET id_service = ?, quantite = ?, prix_unitaire = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM liste_achat WHERE id = ?";
    private static final String DELETE_BY_ACHAT = "DELETE FROM liste_achat WHERE id_achat = ?";
    private static final String SELECT_BY_ID = "SELECT id, id_achat, id_service, quantite, prix_unitaire FROM liste_achat WHERE id = ?";

    public List<ListeAchat> findByAchat(int idAchat) {
        List<ListeAchat> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ACHAT)) {
            ps.setInt(1, idAchat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des lignes d'achat", e);
        }
        return list;
    }

    public ListeAchat findById(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetSimple(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de la ligne", e);
        }
        return null;
    }

    public int create(ListeAchat la) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, la.getIdAchat());
            ps.setInt(2, la.getIdService());
            ps.setInt(3, la.getQuantite());
            ps.setDouble(4, la.getPrixUnitaire());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création de la ligne d'achat", e);
        }
        return -1;
    }

    public boolean update(ListeAchat la) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setInt(1, la.getIdService());
            ps.setInt(2, la.getQuantite());
            ps.setDouble(3, la.getPrixUnitaire());
            ps.setInt(4, la.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de la ligne d'achat", e);
        }
    }

    public boolean delete(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la ligne d'achat", e);
        }
    }

    public void deleteByAchat(int idAchat) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_BY_ACHAT)) {
            ps.setInt(1, idAchat);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression des lignes d'achat", e);
        }
    }

    private ListeAchat mapResultSet(ResultSet rs) throws SQLException {
        ListeAchat la = mapResultSetSimple(rs);
        la.setNomService(rs.getString("nom_service"));
        return la;
    }

    private ListeAchat mapResultSetSimple(ResultSet rs) throws SQLException {
        ListeAchat la = new ListeAchat();
        la.setId(rs.getInt("id"));
        la.setIdAchat(rs.getInt("id_achat"));
        la.setIdService(rs.getInt("id_service"));
        la.setQuantite(rs.getInt("quantite"));
        la.setPrixUnitaire(rs.getDouble("prix_unitaire"));
        return la;
    }
}
