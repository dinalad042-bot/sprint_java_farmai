package org.example.dao;

import org.example.entity.Achat;
import org.example.entity.ListeAchat;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;

/**
 * DAO CRUD pour l'entité Achat.
 * Lors de la création d'un achat avec lignes, met à jour automatiquement le stock des services.
 */
public class AchatDAO {

    private final ListeAchatDAO listeAchatDAO = new ListeAchatDAO();

    private static final String SELECT_ALL = "SELECT id_achat, date_achat, total FROM achat ORDER BY date_achat DESC";
    private static final String SELECT_BY_ID = "SELECT id_achat, date_achat, total FROM achat WHERE id_achat = ?";
    private static final String INSERT = "INSERT INTO achat (date_achat, total) VALUES (?, ?)";
    private static final String UPDATE = "UPDATE achat SET date_achat = ?, total = ? WHERE id_achat = ?";
    private static final String DELETE = "DELETE FROM achat WHERE id_achat = ?";

    public java.util.List<Achat> findAll() {
        java.util.List<Achat> list = new java.util.ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Achat a = mapResultSet(rs);
                a.setLignes(listeAchatDAO.findByAchat(a.getIdAchat()));
                list.add(a);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des achats", e);
        }
        return list;
    }

    public Achat findById(int idAchat) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, idAchat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Achat a = mapResultSet(rs);
                    a.setLignes(listeAchatDAO.findByAchat(idAchat));
                    return a;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de l'achat " + idAchat, e);
        }
        return null;
    }

    /**
     * Crée un achat, ses lignes, et met à jour le stock de chaque service (une seule transaction).
     */
    public int create(Achat achat) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int idAchat = insertAchat(conn, achat);
                achat.setIdAchat(idAchat);
                if (achat.getLignes() != null) {
                    for (ListeAchat ligne : achat.getLignes()) {
                        ligne.setIdAchat(idAchat);
                        insertLigne(conn, ligne);
                        updateStock(conn, ligne.getIdService(), ligne.getQuantite());
                    }
                }
                conn.commit();
                return idAchat;
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Erreur lors de la création de l'achat", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création de l'achat", e);
        }
    }

    private int insertAchat(Connection conn, Achat achat) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(achat.getDateAchat()));
            ps.setDouble(2, achat.getTotal());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Impossible d'obtenir l'id de l'achat.");
    }

    private void insertLigne(Connection conn, ListeAchat ligne) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO liste_achat (id_achat, id_service, quantite, prix_unitaire) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, ligne.getIdAchat());
            ps.setInt(2, ligne.getIdService());
            ps.setInt(3, ligne.getQuantite());
            ps.setDouble(4, ligne.getPrixUnitaire());
            ps.executeUpdate();
        }
    }

    private void updateStock(Connection conn, int idService, int quantite) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE service SET stock = stock + ? WHERE id_service = ?")) {
            ps.setInt(1, quantite);
            ps.setInt(2, idService);
            ps.executeUpdate();
        }
    }

    public boolean update(Achat achat) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setDate(1, Date.valueOf(achat.getDateAchat()));
            ps.setDouble(2, achat.getTotal());
            ps.setInt(3, achat.getIdAchat());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'achat", e);
        }
    }

    public boolean delete(int idAchat) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            listeAchatDAO.deleteByAchat(idAchat);
            try (PreparedStatement ps = conn.prepareStatement(DELETE)) {
                ps.setInt(1, idAchat);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'achat", e);
        }
    }

    private Achat mapResultSet(ResultSet rs) throws SQLException {
        Achat a = new Achat();
        a.setIdAchat(rs.getInt("id_achat"));
        Date d = rs.getDate("date_achat");
        a.setDateAchat(d != null ? d.toLocalDate() : null);
        a.setTotal(rs.getDouble("total"));
        return a;
    }
}
