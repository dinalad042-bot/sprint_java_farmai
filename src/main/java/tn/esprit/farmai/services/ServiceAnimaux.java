package tn.esprit.farmai.services;

import tn.esprit.farmai.interfaces.CRUD;
import tn.esprit.farmai.models.Animaux;
import tn.esprit.farmai.utils.MyDBConnexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceAnimaux implements CRUD<Animaux> {

    private Connection cnx;

    public ServiceAnimaux() {
        cnx = MyDBConnexion.getInstance().getCnx();
    }

    @Override
    public void insertOne(Animaux animal) throws SQLException {
        String req = "INSERT INTO `animaux`(`espece`, `etat_sante`, `date_naissance`, `id_ferme`) VALUES " +
                "( '" + animal.getEspece() + "' ,  '" + animal.getEtat_sante() + "' , '" + animal.getDate_naissance() + "', " + animal.getId_ferme() + ")";
        Statement st = cnx.createStatement();
        st.executeUpdate(req);
    }

    @Override
    public void updateOne(Animaux animal) throws SQLException {
        String req = "UPDATE `animaux` SET `espece`=?, `etat_sante`=?, `date_naissance`=?, `id_ferme`=? WHERE `id_animal`=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, animal.getEspece());
        ps.setString(2, animal.getEtat_sante());
        ps.setDate(3, animal.getDate_naissance());
        ps.setInt(4, animal.getId_ferme());
        ps.setInt(5, animal.getId_animal());
        ps.executeUpdate();
    }

    @Override
    public void deleteOne(Animaux animal) throws SQLException {
        String req = "DELETE FROM `animaux` WHERE `id_animal`=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, animal.getId_animal());
        ps.executeUpdate();
    }

    @Override
    public List<Animaux> selectALL() throws SQLException {
        List<Animaux> list = new ArrayList<>();
        String req = "SELECT * FROM `animaux`";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Animaux a = new Animaux(
                    rs.getInt("id_animal"),
                    rs.getString("espece"),
                    rs.getString("etat_sante"),
                    rs.getDate("date_naissance"),
                    rs.getInt("id_ferme")
            );
            list.add(a);
        }
        return list;
    }

    // Méthode de recherche spécifique
    public List<Animaux> chercherParEspece(String espece) throws SQLException {
        List<Animaux> list = new ArrayList<>();
        String req = "SELECT * FROM `animaux` WHERE `espece` LIKE ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, "%" + espece + "%");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(new Animaux(
                    rs.getInt("id_animal"),
                    rs.getString("espece"),
                    rs.getString("etat_sante"),
                    rs.getDate("date_naissance"),
                    rs.getInt("id_ferme")
            ));
        }
        return list;
    }
}