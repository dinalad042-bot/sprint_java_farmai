package tn.esprit.farmai.services;

import tn.esprit.farmai.interfaces.CRUD;
import tn.esprit.farmai.models.Plantes;
import tn.esprit.farmai.utils.MyDBConnexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePlantes implements CRUD<Plantes> {

    private Connection cnx;

    public ServicePlantes() {
        cnx = MyDBConnexion.getInstance().getCnx();
    }

    @Override
    public void insertOne(Plantes plante) throws SQLException {
        String req = "INSERT INTO `plantes`(`nom_espece`, `cycle_vie`, `id_ferme`) VALUES " +
                "( '" + plante.getNom_espece() + "' ,  '" + plante.getCycle_vie() + "' , " + plante.getId_ferme() + ")";
        Statement st = cnx.createStatement();
        st.executeUpdate(req);
    }

    @Override
    public void updateOne(Plantes plante) throws SQLException {
        String req = "UPDATE `plantes` SET `nom_espece`=?, `cycle_vie`=?, `id_ferme`=? WHERE `id_plante`=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, plante.getNom_espece());
        ps.setString(2, plante.getCycle_vie());
        ps.setInt(3, plante.getId_ferme());
        ps.setInt(4, plante.getId_plante());
        ps.executeUpdate();
    }

    @Override
    public void deleteOne(Plantes plante) throws SQLException {
        String req = "DELETE FROM `plantes` WHERE `id_plante`=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, plante.getId_plante());
        ps.executeUpdate();
    }

    @Override
    public List<Plantes> selectALL() throws SQLException {
        List<Plantes> list = new ArrayList<>();
        String req = "SELECT * FROM `plantes`";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Plantes p = new Plantes(
                    rs.getInt("id_plante"),
                    rs.getString("nom_espece"),
                    rs.getString("cycle_vie"),
                    rs.getInt("id_ferme")
            );
            list.add(p);
        }
        return list;
    }

    // Méthode de recherche spécifique
    public List<Plantes> chercherParNom(String nom) throws SQLException {
        List<Plantes> list = new ArrayList<>();
        String req = "SELECT * FROM `plantes` WHERE `nom_espece` LIKE ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, "%" + nom + "%");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(new Plantes(
                    rs.getInt("id_plante"),
                    rs.getString("nom_espece"),
                    rs.getString("cycle_vie"),
                    rs.getInt("id_ferme")
            ));
        }
        return list;
    }
}