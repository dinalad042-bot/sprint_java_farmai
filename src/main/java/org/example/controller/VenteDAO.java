package org.example.controller;

import org.example.entity.ListeVente;
import org.example.entity.Vente;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VenteDAO {

    public void create(Vente vente) {
        String sqlVente = "INSERT INTO vente(date_vente, total) VALUES (?, ?)";
        String sqlLigne = "INSERT INTO liste_vente(id_vente, id_service, quantite, prix_unitaire) VALUES (?, ?, ?, ?)";

        Connection cnx = null;
        try {
            cnx = DatabaseConnection.getConnection();
            cnx.setAutoCommit(false);

            int idVente;

            try (PreparedStatement ps = cnx.prepareStatement(sqlVente, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDate(1, Date.valueOf(vente.getDateVente()));
                ps.setDouble(2, vente.getTotal());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) throw new SQLException("No generated key for vente.");
                    idVente = rs.getInt(1);
                }
            }

            try (PreparedStatement psL = cnx.prepareStatement(sqlLigne)) {
                for (ListeVente l : vente.getLignes()) {
                    psL.setInt(1, idVente);
                    psL.setInt(2, l.getIdService());
                    psL.setInt(3, l.getQuantite());
                    psL.setDouble(4, l.getPrixUnitaire());
                    psL.addBatch();
                }
                psL.executeBatch();
            }

            cnx.commit();

        } catch (Exception e) {
            try { if (cnx != null) cnx.rollback(); } catch (Exception ignored) {}
            throw new RuntimeException("Error creating vente", e);
        } finally {
            try { if (cnx != null) cnx.setAutoCommit(true); } catch (Exception ignored) {}
            DatabaseConnection.closeQuietly(cnx);
        }
    }

    public List<Vente> findAll() {
        String sql = "SELECT id_vente, date_vente, total FROM vente ORDER BY id_vente DESC";
        List<Vente> list = new ArrayList<>();

        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Vente v = new Vente();
                v.setIdVente(rs.getInt("id_vente"));
                Date d = rs.getDate("date_vente");
                v.setDateVente(d != null ? d.toLocalDate() : null);
                v.setTotal(rs.getDouble("total"));
                list.add(v);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading ventes", e);
        }

        return list;
    }

    public void delete(int idVente) {
        String sql1 = "DELETE FROM liste_vente WHERE id_vente = ?";
        String sql2 = "DELETE FROM vente WHERE id_vente = ?";

        Connection cnx = null;
        try {
            cnx = DatabaseConnection.getConnection();
            cnx.setAutoCommit(false);

            try (PreparedStatement ps = cnx.prepareStatement(sql1)) {
                ps.setInt(1, idVente);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = cnx.prepareStatement(sql2)) {
                ps.setInt(1, idVente);
                ps.executeUpdate();
            }

            cnx.commit();

        } catch (Exception e) {
            try { if (cnx != null) cnx.rollback(); } catch (Exception ignored) {}
            throw new RuntimeException("Error deleting vente", e);
        } finally {
            try { if (cnx != null) cnx.setAutoCommit(true); } catch (Exception ignored) {}
            DatabaseConnection.closeQuietly(cnx);
        }
    }
}