package tn.esprit.farmai.services;

import tn.esprit.farmai.models.Matiere;
import tn.esprit.farmai.utils.MyDBConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD service for Matiere (raw materials).
 * Maps to erp_matiere table.
 */
public class MatiereService {

    private final Connection cnx;

    public MatiereService() {
        this.cnx = MyDBConnexion.getInstance().getCnx();
    }

    public void insertOne(Matiere m) throws SQLException {
        String sql = "INSERT INTO erp_matiere (nom, description, unite, stock, prix_unitaire, seuil_critique) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getNom());
            if (m.getDescription() != null) ps.setString(2, m.getDescription());
            else ps.setNull(2, java.sql.Types.VARCHAR);
            ps.setString(3, m.getUnite());
            ps.setDouble(4, m.getStock());
            ps.setDouble(5, m.getPrixUnitaire());
            ps.setDouble(6, m.getSeuilCritique());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) m.setIdMatiere(keys.getInt(1));
            }
        }
    }

    public void updateOne(Matiere m) throws SQLException {
        String sql = "UPDATE erp_matiere SET nom=?, description=?, unite=?, stock=?, prix_unitaire=?, seuil_critique=? WHERE id_matiere=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, m.getNom());
            if (m.getDescription() != null) ps.setString(2, m.getDescription());
            else ps.setNull(2, java.sql.Types.VARCHAR);
            ps.setString(3, m.getUnite());
            ps.setDouble(4, m.getStock());
            ps.setDouble(5, m.getPrixUnitaire());
            ps.setDouble(6, m.getSeuilCritique());
            ps.setInt(7, m.getIdMatiere());
            ps.executeUpdate();
        }
    }

    public void deleteOne(int id) throws SQLException {
        String sql = "DELETE FROM erp_matiere WHERE id_matiere=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Matiere> selectAll() throws SQLException {
        List<Matiere> list = new ArrayList<>();
        String sql = "SELECT * FROM erp_matiere ORDER BY nom ASC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    /** Efficient count — used for dashboard stats. */
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM erp_matiere";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public Matiere findById(int id) throws SQLException {
        String sql = "SELECT * FROM erp_matiere WHERE id_matiere=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Matiere> findStockCritique() throws SQLException {
        List<Matiere> list = new ArrayList<>();
        String sql = "SELECT * FROM erp_matiere WHERE seuil_critique > 0 AND stock <= seuil_critique ORDER BY nom ASC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void increaseStock(int id, double qty) throws SQLException {
        String sql = "UPDATE erp_matiere SET stock = stock + ? WHERE id_matiere = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDouble(1, qty);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void decreaseStock(int id, double qty) throws SQLException {
        // Check stock first
        Matiere m = findById(id);
        if (m == null) throw new SQLException("Matière introuvable: " + id);
        if (m.getStock() < qty) {
            throw new RuntimeException("Stock insuffisant pour \"" + m.getNom() + "\": disponible=" + m.getStock() + ", requis=" + qty);
        }
        String sql = "UPDATE erp_matiere SET stock = stock - ? WHERE id_matiere = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDouble(1, qty);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private Matiere map(ResultSet rs) throws SQLException {
        Matiere m = new Matiere();
        m.setIdMatiere(rs.getInt("id_matiere"));
        m.setNom(rs.getString("nom"));
        m.setDescription(rs.getString("description"));
        m.setUnite(rs.getString("unite"));
        m.setStock(rs.getDouble("stock"));
        m.setPrixUnitaire(rs.getDouble("prix_unitaire"));
        m.setSeuilCritique(rs.getDouble("seuil_critique"));
        return m;
    }
}
