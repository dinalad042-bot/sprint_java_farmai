package tn.esprit.farmai.services;

import tn.esprit.farmai.models.LigneVente;
import tn.esprit.farmai.models.Produit;
import tn.esprit.farmai.models.Vente;
import tn.esprit.farmai.utils.MyDBConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for Vente (sale orders) with stock management.
 * Maps to erp_vente and erp_ligne_vente tables.
 */
public class VenteService {

    private final Connection cnx;
    private final ProduitService produitService;

    public VenteService() {
        this.cnx = MyDBConnexion.getInstance().getCnx();
        this.produitService = new ProduitService();
    }

    /**
     * Create a sale order (FOURNISSEUR sells produits finis).
     * Validates produit.stock, then decreases it on save.
     * Mirrors Symfony VenteService.createVente().
     *
     * NOTE: Raw materials are NOT touched here.
     * They were consumed during Agricole's "Produire" step.
     */
    public void createVente(Vente vente) throws SQLException {
        vente.recalculateTotal();

        // Aggregate quantities per product to handle duplicate lines
        java.util.Map<Integer, Double> totalQteParProduit = new java.util.LinkedHashMap<>();
        for (LigneVente ligne : vente.getLignes()) {
            totalQteParProduit.merge(ligne.getIdProduit(), (double) ligne.getQuantite(), Double::sum);
        }

        // Validate produit.stock for all lines before touching the DB
        for (java.util.Map.Entry<Integer, Double> entry : totalQteParProduit.entrySet()) {
            Produit p = produitService.findById(entry.getKey());
            if (p == null) throw new RuntimeException("Produit introuvable: " + entry.getKey());
            if (p.getStock() < entry.getValue()) {
                throw new RuntimeException(
                    "Stock insuffisant pour \"" + p.getNom() + "\": disponible=" + (int) p.getStock()
                    + ", demandé=" + entry.getValue().intValue()
                    + ". Utilisez 'Produire' pour fabriquer ce produit d'abord.");
            }
        }

        boolean autoCommit = cnx.getAutoCommit();
        cnx.setAutoCommit(false);
        try {
            String sql = "INSERT INTO erp_vente (date_vente, total) VALUES (?,?)";
            try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDate(1, Date.valueOf(vente.getDateVente()));
                ps.setDouble(2, vente.getTotal());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) vente.setIdVente(keys.getInt(1));
                }
            }
            for (LigneVente ligne : vente.getLignes()) {
                ligne.setIdVente(vente.getIdVente());
                insertLigne(ligne);
                // Decrease produit fini stock
                String updateStock = "UPDATE erp_produit SET stock = stock - ? WHERE id_produit = ? AND stock >= ?";
                try (PreparedStatement ps = cnx.prepareStatement(updateStock)) {
                    ps.setInt(1, ligne.getQuantite());
                    ps.setInt(2, ligne.getIdProduit());
                    ps.setInt(3, ligne.getQuantite());
                    ps.executeUpdate();
                }
            }
            cnx.commit();
        } catch (SQLException | RuntimeException e) {
            cnx.rollback();
            throw (e instanceof SQLException) ? (SQLException) e : new SQLException(e.getMessage(), e);
        } finally {
            cnx.setAutoCommit(autoCommit);
        }
    }

    public void deleteVente(int id) throws SQLException {
        boolean autoCommit = cnx.getAutoCommit();
        cnx.setAutoCommit(false);
        try {
            // Restore finished product stock
            List<LigneVente> lignes = findLignesByVente(id);
            for (LigneVente ligne : lignes) {
                String sql = "UPDATE erp_produit SET stock = stock + ? WHERE id_produit = ?";
                try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                    ps.setInt(1, ligne.getQuantite());
                    ps.setInt(2, ligne.getIdProduit());
                    ps.executeUpdate();
                }
            }
            // Delete lines first (FK: erp_ligne_vente.id_vente → erp_vente.id_vente)
            String deleteLines = "DELETE FROM erp_ligne_vente WHERE id_vente = ?";
            try (PreparedStatement ps = cnx.prepareStatement(deleteLines)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            // Delete header
            String sql = "DELETE FROM erp_vente WHERE id_vente=?";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            cnx.commit();
        } catch (SQLException | RuntimeException e) {
            cnx.rollback();
            throw (e instanceof SQLException) ? (SQLException) e : new SQLException(e.getMessage(), e);
        } finally {
            cnx.setAutoCommit(autoCommit);
        }
    }

    public List<Vente> selectAll() throws SQLException {
        List<Vente> list = new ArrayList<>();
        String sql = "SELECT * FROM erp_vente ORDER BY date_vente DESC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Vente v = mapVente(rs);
                v.setLignes(findLignesByVente(v.getIdVente()));
                list.add(v);
            }
        }
        return list;
    }

    public Vente findById(int id) throws SQLException {
        String sql = "SELECT * FROM erp_vente WHERE id_vente=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Vente v = mapVente(rs);
                    v.setLignes(findLignesByVente(id));
                    return v;
                }
            }
        }
        return null;
    }

    public List<LigneVente> findLignesByVente(int idVente) throws SQLException {
        List<LigneVente> list = new ArrayList<>();
        String sql = "SELECT lv.id, lv.id_vente, lv.id_produit, lv.quantite, lv.prix_unitaire, " +
                     "p.nom AS nom_produit " +
                     "FROM erp_ligne_vente lv " +
                     "JOIN erp_produit p ON lv.id_produit = p.id_produit " +
                     "WHERE lv.id_vente = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idVente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LigneVente l = new LigneVente();
                    l.setId(rs.getInt("id"));
                    l.setIdVente(rs.getInt("id_vente"));
                    l.setIdProduit(rs.getInt("id_produit"));
                    l.setQuantite(rs.getInt("quantite"));
                    l.setPrixUnitaire(rs.getDouble("prix_unitaire"));
                    l.setNomProduit(rs.getString("nom_produit"));
                    list.add(l);
                }
            }
        }
        return list;
    }

    private void insertLigne(LigneVente l) throws SQLException {
        String sql = "INSERT INTO erp_ligne_vente (id_vente, id_produit, quantite, prix_unitaire) VALUES (?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, l.getIdVente());
            ps.setInt(2, l.getIdProduit());
            ps.setInt(3, l.getQuantite());
            ps.setDouble(4, l.getPrixUnitaire());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) l.setId(keys.getInt(1));
            }
        }
    }

    private Vente mapVente(ResultSet rs) throws SQLException {
        Vente v = new Vente();
        v.setIdVente(rs.getInt("id_vente"));
        v.setDateVente(rs.getDate("date_vente").toLocalDate());
        v.setTotal(rs.getDouble("total"));
        return v;
    }
}
