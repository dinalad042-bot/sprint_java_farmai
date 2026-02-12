package tn.esprit.farmai.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import tn.esprit.farmai.models.Animaux;
import tn.esprit.farmai.models.Ferme;
import tn.esprit.farmai.services.ServiceAnimaux;
import tn.esprit.farmai.services.ServiceFerme;
import tn.esprit.farmai.utils.NavigationUtil;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la gestion du CRUD des Animaux (Bétail).
 */
public class AnimauxController implements Initializable {

    @FXML private TextField tfEspece;
    @FXML private TextField tfEtat;
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<Ferme> cbFerme;

    @FXML private TableView<Animaux> tvAnimaux;
    @FXML private TableColumn<Animaux, String> colEspece;
    @FXML private TableColumn<Animaux, String> colEtat;
    @FXML private TableColumn<Animaux, Date> colDate;
    @FXML private TableColumn<Animaux, Integer> colIdFerme;

    private final ServiceAnimaux sa = new ServiceAnimaux();
    private final ServiceFerme sf = new ServiceFerme();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Liaison des colonnes de la TableView avec les attributs du modèle Animaux
        colEspece.setCellValueFactory(new PropertyValueFactory<>("espece"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat_sante"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_naissance"));
        colIdFerme.setCellValueFactory(new PropertyValueFactory<>("id_ferme"));

        // 2. Initialisation des composants
        chargerFermes();
        rafraichir();

        // 3. Listener de sélection : remplit le formulaire quand on clique sur une ligne
        tvAnimaux.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                remplirFormulaire(newVal);
            }
        });
    }

    private void chargerFermes() {
        try {
            cbFerme.setItems(FXCollections.observableArrayList(sf.selectALL()));
            cbFerme.setConverter(new StringConverter<Ferme>() {
                @Override public String toString(Ferme f) { return (f != null) ? f.getNom_ferme() : ""; }
                @Override public Ferme fromString(String s) { return null; }
            });
        } catch (SQLException e) {
            System.err.println("Erreur chargement fermes: " + e.getMessage());
        }
    }

    @FXML
    private void ajouter() {
        if (!validerChamps()) return;

        try {
            Animaux a = new Animaux(
                    0,
                    tfEspece.getText().trim(),
                    tfEtat.getText().trim(),
                    Date.valueOf(dpDate.getValue()),
                    cbFerme.getValue().getId_ferme()
            );
            sa.insertOne(a);
            rafraichir();
            viderChamps();
        } catch (SQLException e) {
            afficherAlerte("Erreur SQL", "Impossible d'ajouter l'animal : " + e.getMessage());
        }
    }

    @FXML
    private void modifier() {
        Animaux selected = tvAnimaux.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte("Attention", "Veuillez sélectionner un animal dans le tableau.");
            return;
        }

        if (!validerChamps()) return;

        try {
            selected.setEspece(tfEspece.getText().trim());
            selected.setEtat_sante(tfEtat.getText().trim());
            selected.setDate_naissance(Date.valueOf(dpDate.getValue()));
            selected.setId_ferme(cbFerme.getValue().getId_ferme());

            sa.updateOne(selected);
            rafraichir();
            viderChamps();
        } catch (SQLException e) {
            afficherAlerte("Erreur SQL", "Modification échouée : " + e.getMessage());
        }
    }

    @FXML
    private void supprimer() {
        Animaux selected = tvAnimaux.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte("Attention", "Veuillez sélectionner un animal à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cet animal ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    sa.deleteOne(selected);
                    rafraichir();
                    viderChamps();
                } catch (SQLException e) {
                    afficherAlerte("Erreur SQL", "Suppression impossible.");
                }
            }
        });
    }

    @FXML
    private void handleReturnToSelection(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/tn/esprit/farmai/views/selection-gestion.fxml", "Gestion");
    }

    private void rafraichir() {
        try {
            tvAnimaux.setItems(FXCollections.observableArrayList(sa.selectALL()));
        } catch (SQLException e) {
            System.err.println("Erreur rafraîchissement: " + e.getMessage());
        }
    }

    private void remplirFormulaire(Animaux a) {
        tfEspece.setText(a.getEspece());
        tfEtat.setText(a.getEtat_sante());
        dpDate.setValue(a.getDate_naissance().toLocalDate());
        // Trouve et sélectionne la ferme correspondante dans le ComboBox
        for (Ferme f : cbFerme.getItems()) {
            if (f.getId_ferme() == a.getId_ferme()) {
                cbFerme.setValue(f);
                break;
            }
        }
    }

    private boolean validerChamps() {
        if (tfEspece.getText().trim().isEmpty() || cbFerme.getValue() == null || dpDate.getValue() == null) {
            afficherAlerte("Données manquantes", "L'espèce, la date et la ferme sont obligatoires.");
            return false;
        }
        return true;
    }

    private void viderChamps() {
        tfEspece.clear();
        tfEtat.clear();
        dpDate.setValue(null);
        cbFerme.getSelectionModel().clearSelection();
        tvAnimaux.getSelectionModel().clearSelection();
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}