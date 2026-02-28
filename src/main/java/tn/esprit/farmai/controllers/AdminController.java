package tn.esprit.farmai.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.farmai.services.ServiceAnimaux;
import tn.esprit.farmai.services.ServiceFerme;
import tn.esprit.farmai.services.UserService; // Assume que tu as un service User
import tn.esprit.farmai.utils.NavigationUtil;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    // Labels des statistiques (doivent correspondre aux fx:id dans ton FXML)
    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalFermes;
    @FXML private Label lblTotalAnimaux;
    @FXML private Label lblAdminName;

    // Services
    private final ServiceFerme sf = new ServiceFerme();
    private final ServiceAnimaux sa = new ServiceAnimaux();
    private final UserService su = new UserService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Chargement automatique des données au lancement du dashboard
        chargerStatistiques();

        // Optionnel : Afficher le nom de l'admin connecté (si stocké dans une session)
        // lblAdminName.setText("Bienvenue, Admin");
    }

    /**
     * Récupère les chiffres depuis la base de données
     */
    private void chargerStatistiques() {
        try {
            // On compte les entrées dans chaque table
            int nbFermes = sf.selectALL().size();
            int nbAnimaux = sa.selectALL().size();
            // int nbUsers = su.selectALL().size();

            // Mise à jour de l'interface
            lblTotalFermes.setText(String.valueOf(nbFermes));
            lblTotalAnimaux.setText(String.valueOf(nbAnimaux));
            // lblTotalUsers.setText(String.valueOf(nbUsers));

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des stats : " + e.getMessage());
            // Valeurs par défaut en cas d'erreur
            lblTotalFermes.setText("0");
            lblTotalAnimaux.setText("0");
        }
    }

    /**
     * Action déclenchée par le bouton "Cultures"
     * Dirige vers la carte interactive des fermes
     */
    @FXML
    private void handleCulturesClick(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/tn/esprit/farmai/views/admin_map.fxml", "Carte des Exploitations");
    }

    /**
     * Redirection vers la gestion des utilisateurs (si applicable)
     */
    @FXML
    private void handleUsersClick(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/tn/esprit/farmai/views/gestion_users.fxml", "Gestion Utilisateurs");
    }

    /**
     * Déconnexion et retour à la page de Login
     */
    @FXML
    private void handleDeconnexion(ActionEvent event) {
        // Ici, tu pourrais vider la session utilisateur si tu en as une
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/tn/esprit/farmai/views/login.fxml", "Connexion - FarmAI");
    }

    /**
     * Rafraîchir manuellement les données
     */
    @FXML
    private void handleRefresh() {
        chargerStatistiques();
        System.out.println("Données du dashboard admin rafraîchies.");
    }
}