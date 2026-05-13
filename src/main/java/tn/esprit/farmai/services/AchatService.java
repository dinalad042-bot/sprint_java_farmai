package tn.esprit.farmai.services;

import tn.esprit.farmai.models.Achat;
import tn.esprit.farmai.models.LigneAchat;
import tn.esprit.farmai.utils.MyDBConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for Achat (purchase orders) with stock management.
 * Maps to erp_achat and erp_ligne_achat tables.
 */
public class AchatService {

    private final Connection cnx;
    private final MatiereService matiereService;

    public AchatService() {
        this.cnx = MyDBConnexion.getInstance().getCnx();
        this.matiereService = new MatiereService();
    }

    /**
     * Create a purchase order.
     * Stock is NOT increased here — only after payment is confirmed (markAsPaid).
     * This mirrors the Symfony AchatService.createAchat() behavior.
     */
    public void createAchat(Achat achat) throws SQLException {
        achat.recalculateTotal();

        // Deduplicate lines with the same matière (merge quantities)
        java.util.Map<Integer, LigneAchat> merged = new java.util.LinkedHashMap<>();
        for (LigneAchat ligne : achat.getLignes()) {
            int idM = ligne.getIdMatiere();
            if (merged.containsKey(idM)) {
                merged.get(idM).setQuantite(merged.get(idM).getQuantite() + ligne.getQuantite());
            } else {
                merged.put(idM, ligne);
            }
        }
        achat.setLignes(new java.util.ArrayList<>(merged.values()));
        achat.recalculateTotal();

        boolean autoCommit = cnx.getAutoCommit();
        cnx.setAutoCommit(false);
        try {
            String sql = "INSERT INTO erp_achat (date_achat, total, paid) VALUES (?,?,?)";
            try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDate(1, Date.valueOf(achat.getDateAchat()));
                ps.setDouble(2, achat.getTotal());
                ps.setBoolean(3, false); // always unpaid on creation
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) achat.setIdAchat(keys.getInt(1));
                }
            }
            for (LigneAchat ligne : achat.getLignes()) {
                ligne.setIdAchat(achat.getIdAchat());
                insertLigne(ligne);
                // DO NOT increase stock here — only after payment
            }
            cnx.commit();
        } catch (SQLException | RuntimeException e) {
            cnx.rollback();
            throw (e instanceof SQLException) ? (SQLException) e : new SQLException(e.getMessage(), e);
        } finally {
            cnx.setAutoCommit(autoCommit);
        }
    }

    /**
     * Mark an achat as paid and increase matière stock.
     * Mirrors Symfony AchatService.confirmPayment().
     * In the desktop app we skip Stripe — payment is confirmed by the user clicking "Marquer comme payé".
     */
    public void markAsPaid(int id) throws SQLException {
        Achat achat = findById(id);
        if (achat == null) throw new RuntimeException("Achat introuvable: " + id);
        if (achat.isPaid()) throw new RuntimeException("Cet achat est déjà payé.");

        boolean autoCommit = cnx.getAutoCommit();
        cnx.setAutoCommit(false);
        try {
            // Mark as paid
            String sql = "UPDATE erp_achat SET paid = 1 WHERE id_achat = ?";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            // Increase matière stock (agricole received the goods)
            for (LigneAchat ligne : achat.getLignes()) {
                matiereService.increaseStock(ligne.getIdMatiere(), ligne.getQuantite());
            }
            cnx.commit();
        } catch (SQLException | RuntimeException e) {
            cnx.rollback();
            throw (e instanceof SQLException) ? (SQLException) e : new SQLException(e.getMessage(), e);
        } finally {
            cnx.setAutoCommit(autoCommit);
        }
    }

    public void deleteAchat(int id) throws SQLException {
        Achat achat = findById(id);
        if (achat == null) throw new RuntimeException("Achat introuvable: " + id);

        boolean autoCommit = cnx.getAutoCommit();
        cnx.setAutoCommit(false);
        try {
            // Reverse stock ONLY if the achat was paid (stock was actually updated)
            if (achat.isPaid()) {
                for (LigneAchat ligne : achat.getLignes()) {
                    String undoStock = "UPDATE erp_matiere SET stock = stock - ? WHERE id_matiere = ?";
                    try (PreparedStatement ps = cnx.prepareStatement(undoStock)) {
                        ps.setDouble(1, ligne.getQuantite());
                        ps.setInt(2, ligne.getIdMatiere());
                        ps.executeUpdate();
                    }
                }
            }
            // Delete lines first (FK constraint)
            String deleteLines = "DELETE FROM erp_ligne_achat WHERE id_achat = ?";
            try (PreparedStatement ps = cnx.prepareStatement(deleteLines)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            // Delete header
            String sql = "DELETE FROM erp_achat WHERE id_achat=?";
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

    public List<Achat> selectAll() throws SQLException {
        List<Achat> list = new ArrayList<>();
        String sql = "SELECT * FROM erp_achat ORDER BY date_achat DESC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Achat a = mapAchat(rs);
                a.setLignes(findLignesByAchat(a.getIdAchat()));
                list.add(a);
            }
        }
        return list;
    }

    public Achat findById(int id) throws SQLException {
        String sql = "SELECT * FROM erp_achat WHERE id_achat=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Achat a = mapAchat(rs);
                    a.setLignes(findLignesByAchat(id));
                    return a;
                }
            }
        }
        return null;
    }

    public List<LigneAchat> findLignesByAchat(int idAchat) throws SQLException {
        List<LigneAchat> list = new ArrayList<>();
        String sql = "SELECT la.id, la.id_achat, la.id_matiere, la.quantite, la.prix_unitaire, " +
                     "m.nom AS nom_matiere, m.unite AS unite_matiere " +
                     "FROM erp_ligne_achat la " +
                     "JOIN erp_matiere m ON la.id_matiere = m.id_matiere " +
                     "WHERE la.id_achat = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idAchat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LigneAchat l = new LigneAchat();
                    l.setId(rs.getInt("id"));
                    l.setIdAchat(rs.getInt("id_achat"));
                    l.setIdMatiere(rs.getInt("id_matiere"));
                    l.setQuantite(rs.getDouble("quantite"));
                    l.setPrixUnitaire(rs.getDouble("prix_unitaire"));
                    l.setNomMatiere(rs.getString("nom_matiere"));
                    l.setUniteMatiere(rs.getString("unite_matiere"));
                    list.add(l);
                }
            }
        }
        return list;
    }

    private void insertLigne(LigneAchat l) throws SQLException {
        String sql = "INSERT INTO erp_ligne_achat (id_achat, id_matiere, quantite, prix_unitaire) VALUES (?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, l.getIdAchat());
            ps.setInt(2, l.getIdMatiere());
            ps.setDouble(3, l.getQuantite());
            ps.setDouble(4, l.getPrixUnitaire());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) l.setId(keys.getInt(1));
            }
        }
    }

    private Achat mapAchat(ResultSet rs) throws SQLException {
        Achat a = new Achat();
        a.setIdAchat(rs.getInt("id_achat"));
        a.setDateAchat(rs.getDate("date_achat").toLocalDate());
        a.setTotal(rs.getDouble("total"));
        a.setPaid(rs.getBoolean("paid"));
        return a;
    }
}
