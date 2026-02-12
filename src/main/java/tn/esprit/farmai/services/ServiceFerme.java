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
        // Note: id_fermier a été retiré selon votre demande précédente
        String req = "INSERT INTO `ferme`(`nom_ferme`, `lieu`, `surface`) VALUES " +
                "( '" + ferme.getNom_ferme() + "' ,  '" + ferme.getLieu() + "' , " + ferme.getSurface() + ")";
        Statement st = cnx.createStatement();
        st.executeUpdate(req);
    }

    public void insertOneUpdated(Ferme ferme) throws SQLException {
        String req = "INSERT INTO `ferme`(`nom_ferme`, `lieu`, `surface`) VALUES (?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, ferme.getNom_ferme());
        ps.setString(2, ferme.getLieu());
        ps.setFloat(3, ferme.getSurface());
        ps.executeUpdate();
    }

    @Override
    public void updateOne(Ferme ferme) throws SQLException {
        String req = "UPDATE `ferme` SET `nom_ferme`=?, `lieu`=?, `surface`=? WHERE `id_ferme`=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, ferme.getNom_ferme());
        ps.setString(2, ferme.getLieu());
        ps.setFloat(3, ferme.getSurface());
        ps.setInt(4, ferme.getId_ferme());
        ps.executeUpdate();
    }

    @Override
    public void deleteOne(Ferme ferme) throws SQLException {
        String req = "DELETE FROM `ferme` WHERE `id_ferme`=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, ferme.getId_ferme());
        ps.executeUpdate();
    }

    @Override
    public List<Ferme> selectALL() throws SQLException {
        List<Ferme> list = new ArrayList<>();
        String req = "SELECT * FROM `ferme`";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Ferme f = new Ferme(
                    rs.getInt("id_ferme"),
                    rs.getString("nom_ferme"),
                    rs.getString("lieu"),
                    rs.getFloat("surface")
            );
            list.add(f);
        }
        return list;
    }

    // Méthode de recherche spécifique
    public List<Ferme> chercherParNom(String nom) throws SQLException {
        List<Ferme> list = new ArrayList<>();
        String req = "SELECT * FROM `ferme` WHERE `nom_ferme` LIKE ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, "%" + nom + "%");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(new Ferme(
                    rs.getInt("id_ferme"),
                    rs.getString("nom_ferme"),
                    rs.getString("lieu"),
                    rs.getFloat("surface")
            ));
        }
        return list;
    }
}