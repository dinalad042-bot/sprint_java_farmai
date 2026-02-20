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
        // Mise à jour de la requête pour inclure quantite
        String req = "INSERT INTO `plantes`(`nom_espece`, `cycle_vie`, `id_ferme`, `quantite`) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, plante.getNom_espece());
        ps.setString(2, plante.getCycle_vie());
        ps.setInt(3, plante.getId_ferme());
        ps.setDouble(4, plante.getQuantite()); // Ajout de la quantité
        ps.executeUpdate();
    }

    @Override
    public void updateOne(Plantes plante) throws SQLException {
        // Mise à jour de la requête pour inclure quantite
        String req = "UPDATE `plantes` SET `nom_espece`=?, `cycle_vie`=?, `id_ferme`=?, `quantite`=? WHERE `id_plante`=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, plante.getNom_espece());
        ps.setString(2, plante.getCycle_vie());
        ps.setInt(3, plante.getId_ferme());
        ps.setDouble(4, plante.getQuantite()); // Mise à jour de la quantité
        ps.setInt(5, plante.getId_plante());
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
            // Utilisation du constructeur à 5 paramètres
            Plantes p = new Plantes(
                    rs.getInt("id_plante"),
                    rs.getString("nom_espece"),
                    rs.getString("cycle_vie"),
                    rs.getInt("id_ferme"),
                    rs.getDouble("quantite") // Récupération de la quantité
            );
            list.add(p);
        }
        return list;
    }

    public List<Plantes> chercherParNom(String nom) throws SQLException {
        List<Plantes> list = new ArrayList<>();
        String req = "SELECT * FROM `plantes` WHERE `nom_espece` LIKE ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, "%" + nom + "%");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            // Utilisation du constructeur à 5 paramètres
            list.add(new Plantes(
                    rs.getInt("id_plante"),
                    rs.getString("nom_espece"),
                    rs.getString("cycle_vie"),
                    rs.getInt("id_ferme"),
                    rs.getDouble("quantite") // Récupération de la quantité
            ));
        }
        return list;
    }
}