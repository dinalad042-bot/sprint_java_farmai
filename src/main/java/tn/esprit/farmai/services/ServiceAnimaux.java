package tn.esprit.farmai.services;

import tn.esprit.farmai.interfaces.CRUD;
import tn.esprit.farmai.models.Animaux;
import tn.esprit.farmai.utils.MyDBConnexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for Animaux CRUD operations.
 * Fixed: Updated to use camelCase getters/setters.
 */
public class ServiceAnimaux implements CRUD<Animaux> {

    private Connection cnx;

    public ServiceAnimaux() {
        cnx = MyDBConnexion.getInstance().getCnx();
    }

    @Override
    public void insertOne(Animaux animal) throws SQLException {
        String req = "INSERT INTO `animal`(`espece`, `etat_sante`, `date_naissance`, `id_ferme`) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, animal.getEspece());
        ps.setString(2, animal.getEtatSante());
        ps.setDate(3, animal.getDateNaissance());
        ps.setInt(4, animal.getIdFerme());
        ps.executeUpdate();
    }

    @Override
    public void updateOne(Animaux animal) throws SQLException {
        String req = "UPDATE `animal` SET `espece`=?, `etat_sante`=?, `date_naissance`=?, `id_ferme`=? WHERE `id_animal`=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, animal.getEspece());
        ps.setString(2, animal.getEtatSante());
        ps.setDate(3, animal.getDateNaissance());
        ps.setInt(4, animal.getIdFerme());
        ps.setInt(5, animal.getIdAnimal());
        ps.executeUpdate();
    }

    @Override
    public void deleteOne(Animaux animal) throws SQLException {
        String req = "DELETE FROM `animal` WHERE `id_animal`=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, animal.getIdAnimal());
        ps.executeUpdate();
    }

    @Override
    public List<Animaux> selectALL() throws SQLException {
        List<Animaux> list = new ArrayList<>();
        String req = "SELECT * FROM `animal`";
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
        String req = "SELECT * FROM `animal` WHERE `espece` LIKE ?";
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

    public List<Animaux> findByFermes(List<Integer> fermeIds) throws SQLException {
        if (fermeIds == null || fermeIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Animaux> list = new ArrayList<>();
        String placeholders = String.join(",", fermeIds.stream().map(id -> "?").toList());
        String req = "SELECT * FROM `animal` WHERE `id_ferme` IN (" + placeholders + ")";
        PreparedStatement ps = cnx.prepareStatement(req);
        for (int i = 0; i < fermeIds.size(); i++) {
            ps.setInt(i + 1, fermeIds.get(i));
        }
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

    public List<Animaux> findByFerme(int idFerme) throws SQLException {
        List<Animaux> list = new ArrayList<>();
        String req = "SELECT * FROM `animal` WHERE `id_ferme` = ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, idFerme);
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
