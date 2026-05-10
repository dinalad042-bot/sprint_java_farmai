package tn.esprit.farmai.services;

import tn.esprit.farmai.interfaces.CRUD;
import tn.esprit.farmai.models.Plantes;
import tn.esprit.farmai.utils.MyDBConnexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for Plantes CRUD operations.
 * Fixed: Updated to use camelCase getters/setters.
 */
public class ServicePlantes implements CRUD<Plantes> {

    private Connection cnx;

    public ServicePlantes() {
        cnx = MyDBConnexion.getInstance().getCnx();
    }

    @Override
    public void insertOne(Plantes plante) throws SQLException {
        String req = "INSERT INTO `plantes`(`nom_espece`, `cycle_vie`, `id_ferme`, `quantite`) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, plante.getNomEspece());
        ps.setString(2, plante.getCycleVie());
        ps.setInt(3, plante.getIdFerme());
        ps.setDouble(4, plante.getQuantite());
        ps.executeUpdate();
    }

    @Override
    public void updateOne(Plantes plante) throws SQLException {
        String req = "UPDATE `plantes` SET `nom_espece`=?, `cycle_vie`=?, `id_ferme`=?, `quantite`=? WHERE `id_plante`=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, plante.getNomEspece());
        ps.setString(2, plante.getCycleVie());
        ps.setInt(3, plante.getIdFerme());
        ps.setDouble(4, plante.getQuantite());
        ps.setInt(5, plante.getIdPlante());
        ps.executeUpdate();
    }

    @Override
    public void deleteOne(Plantes plante) throws SQLException {
        String req = "DELETE FROM `plantes` WHERE `id_plante`=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, plante.getIdPlante());
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
                rs.getInt("id_ferme"),
                rs.getDouble("quantite")
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
            list.add(new Plantes(
                rs.getInt("id_plante"),
                rs.getString("nom_espece"),
                rs.getString("cycle_vie"),
                rs.getInt("id_ferme"),
                rs.getDouble("quantite")
            ));
        }
        return list;
    }

    public List<Plantes> findByFermes(List<Integer> fermeIds) throws SQLException {
        if (fermeIds == null || fermeIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Plantes> list = new ArrayList<>();
        String placeholders = String.join(",", fermeIds.stream().map(id -> "?").toList());
        String req = "SELECT * FROM `plantes` WHERE `id_ferme` IN (" + placeholders + ")";
        PreparedStatement ps = cnx.prepareStatement(req);
        for (int i = 0; i < fermeIds.size(); i++) {
            ps.setInt(i + 1, fermeIds.get(i));
        }
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new Plantes(
                rs.getInt("id_plante"),
                rs.getString("nom_espece"),
                rs.getString("cycle_vie"),
                rs.getInt("id_ferme"),
                rs.getDouble("quantite")
            ));
        }
        return list;
    }
}
