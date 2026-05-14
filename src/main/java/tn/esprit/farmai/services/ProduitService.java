package tn.esprit.farmai.services;

import tn.esprit.farmai.models.Produit;
import tn.esprit.farmai.models.RecetteIngredient;
import tn.esprit.farmai.utils.MyDBConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD service for Produit (finished products) and their recipes.
 * Maps to erp_produit and erp_recette_ingredient tables.
 */
public class ProduitService {

    private final Connection cnx;
    private final MatiereService matiereService;

    public ProduitService() {
        this.cnx = MyDBConnexion.getInstance().getCnx();
        this.matiereService = new MatiereService();
    }

    public void insertOne(Produit p) throws SQLException {
        String sql = "INSERT INTO erp_produit (nom, description, prix_vente, quantite_produite, stock, is_simple) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNom());
            if (p.getDescription() != null) ps.setString(2, p.getDescription());
            else ps.setNull(2, java.sql.Types.VARCHAR);
            ps.setDouble(3, p.getPrixVente());
            ps.setDouble(4, p.getQuantiteProduite());
            ps.setDouble(5, p.getStock());
            ps.setBoolean(6, p.isSimple());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setIdProduit(keys.getInt(1));
            }
        }
        // Save recipe
        for (RecetteIngredient ri : p.getRecette()) {
            ri.setIdProduit(p.getIdProduit());
            insertRecetteIngredient(ri);
        }
    }

    public void updateOne(Produit p) throws SQLException {
        String sql = "UPDATE erp_produit SET nom=?, description=?, prix_vente=?, quantite_produite=?, stock=?, is_simple=? WHERE id_produit=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            if (p.getDescription() != null) ps.setString(2, p.getDescription());
            else ps.setNull(2, java.sql.Types.VARCHAR);
            ps.setDouble(3, p.getPrixVente());
            ps.setDouble(4, p.getQuantiteProduite());
            ps.setDouble(5, p.getStock());
            ps.setBoolean(6, p.isSimple());
            ps.setInt(7, p.getIdProduit());
            ps.executeUpdate();
        }
        // Replace recipe
        deleteRecetteByProduit(p.getIdProduit());
        for (RecetteIngredient ri : p.getRecette()) {
            ri.setIdProduit(p.getIdProduit());
            insertRecetteIngredient(ri);
        }
    }

    public void deleteOne(int id) throws SQLException {
        deleteRecetteByProduit(id);
        String sql = "DELETE FROM erp_produit WHERE id_produit=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Produit> selectAll() throws SQLException {
        List<Produit> list = new ArrayList<>();
        String sql = "SELECT * FROM erp_produit ORDER BY nom ASC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Produit p = map(rs);
                p.setRecette(findRecetteByProduit(p.getIdProduit()));
                list.add(p);
            }
        }
        return list;
    }

    /** Efficient count without loading recipes — used for dashboard stats. */
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM erp_produit";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public Produit findById(int id) throws SQLException {
        String sql = "SELECT * FROM erp_produit WHERE id_produit=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Produit p = map(rs);
                    p.setRecette(findRecetteByProduit(id));
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * Produce batches of a product: consume raw materials from recipe, increase product stock.
     * Wrapped in a transaction — all stock changes succeed or nothing is committed.
     */
    public void produire(int idProduit, int batches) throws SQLException {
        Produit p = findById(idProduit);
        if (p == null) throw new RuntimeException("Produit introuvable.");
        if (p.isSimple()) throw new RuntimeException("Ce produit est simple, pas de production par recette.");
        if (p.getRecette().isEmpty()) throw new RuntimeException("Ce produit n'a pas de recette définie.");

        // Validate all stocks before touching the DB
        for (RecetteIngredient ri : p.getRecette()) {
            double needed = ri.getQuantite() * batches;
            tn.esprit.farmai.models.Matiere m = matiereService.findById(ri.getIdMatiere());
            if (m == null) throw new RuntimeException("Matière introuvable: " + ri.getIdMatiere());
            if (m.getStock() < needed) {
                throw new RuntimeException("Stock insuffisant pour \"" + m.getNom()
                    + "\": disponible=" + m.getStock() + ", requis=" + needed);
            }
        }

        boolean autoCommit = cnx.getAutoCommit();
        cnx.setAutoCommit(false);
        try {
            // Consume materials
            for (RecetteIngredient ri : p.getRecette()) {
                matiereService.decreaseStock(ri.getIdMatiere(), ri.getQuantite() * batches);
            }
            // Increase product stock
            double produced = p.getQuantiteProduite() * batches;
            String sql = "UPDATE erp_produit SET stock = stock + ? WHERE id_produit = ?";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setDouble(1, produced);
                ps.setInt(2, idProduit);
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

    // --- Recipe helpers ---

    private void insertRecetteIngredient(RecetteIngredient ri) throws SQLException {
        String sql = "INSERT INTO erp_recette_ingredient (id_produit, id_matiere, quantite) VALUES (?,?,?) ON DUPLICATE KEY UPDATE quantite=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, ri.getIdProduit());
            ps.setInt(2, ri.getIdMatiere());
            ps.setDouble(3, ri.getQuantite());
            ps.setDouble(4, ri.getQuantite());
            ps.executeUpdate();
        }
    }

    private void deleteRecetteByProduit(int idProduit) throws SQLException {
        String sql = "DELETE FROM erp_recette_ingredient WHERE id_produit=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idProduit);
            ps.executeUpdate();
        }
    }

    public List<RecetteIngredient> findRecetteByProduit(int idProduit) throws SQLException {
        List<RecetteIngredient> list = new ArrayList<>();
        String sql = "SELECT ri.id, ri.id_produit, ri.id_matiere, ri.quantite, " +
                     "m.nom AS nom_matiere, m.unite AS unite_matiere " +
                     "FROM erp_recette_ingredient ri " +
                     "JOIN erp_matiere m ON ri.id_matiere = m.id_matiere " +
                     "WHERE ri.id_produit = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idProduit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RecetteIngredient ri = new RecetteIngredient();
                    ri.setId(rs.getInt("id"));
                    ri.setIdProduit(rs.getInt("id_produit"));
                    ri.setIdMatiere(rs.getInt("id_matiere"));
                    ri.setQuantite(rs.getDouble("quantite"));
                    ri.setNomMatiere(rs.getString("nom_matiere"));
                    ri.setUniteMatiere(rs.getString("unite_matiere"));
                    list.add(ri);
                }
            }
        }
        return list;
    }

    private Produit map(ResultSet rs) throws SQLException {
        Produit p = new Produit();
        p.setIdProduit(rs.getInt("id_produit"));
        p.setNom(rs.getString("nom"));
        p.setDescription(rs.getString("description"));
        p.setPrixVente(rs.getDouble("prix_vente"));
        p.setQuantiteProduite(rs.getDouble("quantite_produite"));
        p.setStock(rs.getDouble("stock"));
        p.setSimple(rs.getBoolean("is_simple"));
        return p;
    }
}
