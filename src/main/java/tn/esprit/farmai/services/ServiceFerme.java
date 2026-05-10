package tn.esprit.farmai.services;

import tn.esprit.farmai.interfaces.CRUD;
import tn.esprit.farmai.models.Ferme;
import tn.esprit.farmai.utils.MyDBConnexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceFerme implements CRUD<Ferme> {

    private Connection cnx;

    public ServiceFerme() {
        cnx = MyDBConnexion.getInstance().getCnx();
    }

    @Override
    public void insertOne(Ferme ferme) throws SQLException {
        String req = "INSERT INTO ferme (nom_ferme, lieu, surface, id_user) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, ferme.getNomFerme());
        ps.setString(2, ferme.getLieu());
        ps.setDouble(3, ferme.getSurface());
        ps.setInt(4, ferme.getIdFermier());
        ps.executeUpdate();
    }

    @Override
    public void updateOne(Ferme ferme) throws SQLException {
        String req = "UPDATE ferme SET nom_ferme=?, lieu=?, surface=?, id_user=? WHERE id_ferme=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, ferme.getNomFerme());
        ps.setString(2, ferme.getLieu());
        ps.setDouble(3, ferme.getSurface());
        ps.setInt(4, ferme.getIdFermier());
        ps.setInt(5, ferme.getIdFerme());
        ps.executeUpdate();
    }

    @Override
    public void deleteOne(Ferme ferme) throws SQLException {
        String req = "DELETE FROM ferme WHERE id_ferme=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, ferme.getIdFerme());
        ps.executeUpdate();
    }

    @Override
    public List<Ferme> selectALL() throws SQLException {
        List<Ferme> list = new ArrayList<>();
        String req = "SELECT * FROM ferme ORDER BY id_ferme DESC";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Ferme f = new Ferme(
                rs.getInt("id_ferme"),
                rs.getString("nom_ferme"),
                rs.getString("lieu"),
                rs.getDouble("surface"),
                rs.getInt("id_user")
            );
            list.add(f);
        }
        return list;
    }

    // Méthode de recherche spécifique
    public List<Ferme> chercherParNom(String nom) throws SQLException {
        List<Ferme> list = new ArrayList<>();
        String req = "SELECT * FROM ferme WHERE nom_ferme LIKE ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, "%" + nom + "%");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(new Ferme(
                rs.getInt("id_ferme"),
                rs.getString("nom_ferme"),
                rs.getString("lieu"),
                rs.getDouble("surface"),
                rs.getInt("id_user")
            ));
        }
        return list;
    }

    public List<Ferme> findByFermier(int idFermier) throws SQLException {
        List<Ferme> list = new ArrayList<>();
        String req = "SELECT * FROM ferme WHERE id_user = ? ORDER BY id_ferme DESC";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, idFermier);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new Ferme(
                rs.getInt("id_ferme"),
                rs.getString("nom_ferme"),
                rs.getString("lieu"),
                rs.getDouble("surface"),
                rs.getInt("id_user")
            ));
        }
        return list;
    }

    public List<Integer> getFermeIdsByFermier(int idFermier) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String req = "SELECT id_ferme FROM ferme WHERE id_user = ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, idFermier);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            ids.add(rs.getInt("id_ferme"));
        }
        return ids;
    }
}
