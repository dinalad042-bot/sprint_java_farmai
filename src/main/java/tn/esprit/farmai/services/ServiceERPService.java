package tn.esprit.farmai.services;

import tn.esprit.farmai.models.ServiceERP;
import tn.esprit.farmai.utils.MyDBConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD service for ServiceERP.
 * Maps to erp_service table.
 */
public class ServiceERPService {

    private final Connection cnx;

    public ServiceERPService() {
        this.cnx = MyDBConnexion.getInstance().getCnx();
    }

    public void insertOne(ServiceERP s) throws SQLException {
        String sql = "INSERT INTO erp_service (nom, description, prix, stock, seuil_critique) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getNom());
            if (s.getDescription() != null) ps.setString(2, s.getDescription());
            else ps.setNull(2, java.sql.Types.VARCHAR);
            ps.setDouble(3, s.getPrix());
            ps.setInt(4, s.getStock());
            ps.setInt(5, s.getSeuilCritique());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) s.setIdService(keys.getInt(1));
            }
        }
    }

    public void updateOne(ServiceERP s) throws SQLException {
        String sql = "UPDATE erp_service SET nom=?, description=?, prix=?, stock=?, seuil_critique=? WHERE id_service=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, s.getNom());
            if (s.getDescription() != null) ps.setString(2, s.getDescription());
            else ps.setNull(2, java.sql.Types.VARCHAR);
            ps.setDouble(3, s.getPrix());
            ps.setInt(4, s.getStock());
            ps.setInt(5, s.getSeuilCritique());
            ps.setInt(6, s.getIdService());
            ps.executeUpdate();
        }
    }

    public void deleteOne(int id) throws SQLException {
        String sql = "DELETE FROM erp_service WHERE id_service=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<ServiceERP> selectAll() throws SQLException {
        List<ServiceERP> list = new ArrayList<>();
        String sql = "SELECT * FROM erp_service ORDER BY id_service ASC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    /** Efficient count — used for dashboard stats. */
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM erp_service";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public ServiceERP findById(int id) throws SQLException {
        String sql = "SELECT * FROM erp_service WHERE id_service=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<ServiceERP> findStockCritique() throws SQLException {
        List<ServiceERP> list = new ArrayList<>();
        // Only flag as critical when seuil_critique > 0 (mirrors Matiere logic)
        String sql = "SELECT * FROM erp_service WHERE seuil_critique > 0 AND stock <= seuil_critique ORDER BY nom ASC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private ServiceERP map(ResultSet rs) throws SQLException {
        ServiceERP s = new ServiceERP();
        s.setIdService(rs.getInt("id_service"));
        s.setNom(rs.getString("nom"));
        s.setDescription(rs.getString("description"));
        s.setPrix(rs.getDouble("prix"));
        s.setStock(rs.getInt("stock"));
        s.setSeuilCritique(rs.getInt("seuil_critique"));
        return s;
    }
}
