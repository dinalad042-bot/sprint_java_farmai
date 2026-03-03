package org.example.dao;

import org.example.entity.Service;
import org.example.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {

    public List<Service> findAll() {
        String sql = "SELECT id_service, nom, description, prix, stock, seuil_critique FROM service ORDER BY id_service DESC";
        List<Service> list = new ArrayList<>();

        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Service s = new Service();
                s.setIdService(rs.getInt("id_service"));
                s.setNom(rs.getString("nom"));
                s.setDescription(rs.getString("description"));
                s.setPrix(rs.getDouble("prix"));
                s.setStock(rs.getInt("stock"));
                s.setSeuilCritique(rs.getInt("seuil_critique"));
                list.add(s);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error findAll services", e);
        }
        return list;
    }

    public Service findById(int idService) {
        String sql = "SELECT id_service, nom, description, prix, stock, seuil_critique FROM service WHERE id_service = ?";
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, idService);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Service s = new Service();
                    s.setIdService(rs.getInt("id_service"));
                    s.setNom(rs.getString("nom"));
                    s.setDescription(rs.getString("description"));
                    s.setPrix(rs.getDouble("prix"));
                    s.setStock(rs.getInt("stock"));
                    s.setSeuilCritique(rs.getInt("seuil_critique"));
                    return s;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error findById service " + idService, e);
        }
        return null;
    }

    public void create(Service s) {
        String sql = "INSERT INTO service(nom, description, prix, stock, seuil_critique) VALUES (?, ?, ?, ?, ?)";

        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, s.getNom());
            ps.setString(2, s.getDescription());
            ps.setDouble(3, s.getPrix());
            ps.setInt(4, s.getStock());
            ps.setInt(5, s.getSeuilCritique());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) s.setIdService(rs.getInt(1));
            }

        } catch (Exception e) {
            throw new RuntimeException("Error create service", e);
        }
    }

    public void update(Service s) {
        String sql = "UPDATE service SET nom = ?, description = ?, prix = ?, stock = ?, seuil_critique = ? WHERE id_service = ?";

        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, s.getNom());
            ps.setString(2, s.getDescription());
            ps.setDouble(3, s.getPrix());
            ps.setInt(4, s.getStock());
            ps.setInt(5, s.getSeuilCritique());
            ps.setInt(6, s.getIdService());

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Error update service " + s.getIdService(), e);
        }
    }

    public void delete(int idService) {
        String sql = "DELETE FROM service WHERE id_service = ?";

        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, idService);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Error delete service " + idService, e);
        }
    }

    // ----- Stock Helpers -----

    public int getStockById(int idService) {
        String sql = "SELECT stock FROM service WHERE id_service = ?";
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, idService);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("stock");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error getting stock for service " + idService, e);
        }
        return -1;
    }

    public void decreaseStock(int idService, int quantity) {
        if (quantity <= 0) return;

        String sql = "UPDATE service SET stock = stock - ? WHERE id_service = ? AND stock >= ?";
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setInt(2, idService);
            ps.setInt(3, quantity);

            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new RuntimeException("Stock insuffisant pour le service " + idService);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error decreasing stock for service " + idService, e);
        }
    }

    public void increaseStock(int idService, int quantity) {
        if (quantity <= 0) return;

        String sql = "UPDATE service SET stock = stock + ? WHERE id_service = ?";
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setInt(2, idService);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Error increasing stock for service " + idService, e);
        }
    }

    public List<Service> findStockCritique() {
        String sql = "SELECT id_service, nom, description, prix, stock, seuil_critique " +
                "FROM service WHERE stock <= seuil_critique ORDER BY stock ASC";

        List<Service> list = new ArrayList<>();
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Service s = new Service();
                s.setIdService(rs.getInt("id_service"));
                s.setNom(rs.getString("nom"));
                s.setDescription(rs.getString("description"));
                s.setPrix(rs.getDouble("prix"));
                s.setStock(rs.getInt("stock"));
                s.setSeuilCritique(rs.getInt("seuil_critique"));
                list.add(s);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error findStockCritique", e);
        }
        return list;
    }
}