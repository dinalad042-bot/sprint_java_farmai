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
import tn.esprit.farmai.models.Ferme;
import tn.esprit.farmai.models.Plantes;
import tn.esprit.farmai.services.ServiceFerme;
import tn.esprit.farmai.services.ServicePlantes;
import tn.esprit.farmai.utils.NavigationUtil;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class PlantesController implements Initializable {

    @FXML private TextField tfNomEspece;
    @FXML private TextField tfCycleVie;
    @FXML private ComboBox<Ferme> cbFerme;

    @FXML private TableView<Plantes> tvPlantes;
    @FXML private TableColumn<Plantes, String> colNom;
    @FXML private TableColumn<Plantes, String> colCycle;
    @FXML private TableColumn<Plantes, Integer> colFerme;

    private final ServicePlantes sp = new ServicePlantes();
    private final ServiceFerme sf = new ServiceFerme();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuration des colonnes selon ton modèle
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom_espece"));
        colCycle.setCellValueFactory(new PropertyValueFactory<>("cycle_vie"));
        colFerme.setCellValueFactory(new PropertyValueFactory<>("id_ferme"));

        chargerFermes();
        rafraichir();

        // Listener pour remplir le formulaire lors d'un clic sur le tableau
        tvPlantes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                tfNomEspece.setText(newVal.getNom_espece());
                tfCycleVie.setText(newVal.getCycle_vie());
                // Sélection de la ferme correspondante
                cbFerme.getItems().stream()
                        .filter(f -> f.getId_ferme() == newVal.getId_ferme())
                        .findFirst().ifPresent(f -> cbFerme.setValue(f));
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
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void ajouter() {
        if (!validerChamps()) return;
        try {
            Plantes p = new Plantes(0, tfNomEspece.getText(), tfCycleVie.getText(), cbFerme.getValue().getId_ferme());
            sp.insertOne(p);
            rafraichir();
            viderChamps();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void modifier() {
        Plantes selected = tvPlantes.getSelectionModel().getSelectedItem();
        if (selected != null && validerChamps()) {
            try {
                selected.setNom_espece(tfNomEspece.getText());
                selected.setCycle_vie(tfCycleVie.getText());
                selected.setId_ferme(cbFerme.getValue().getId_ferme());
                sp.updateOne(selected);
                tvPlantes.refresh();
                viderChamps();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void supprimer() {
        Plantes selected = tvPlantes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                sp.deleteOne(selected);
                rafraichir();
                viderChamps();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void rafraichir() {
        try { tvPlantes.setItems(FXCollections.observableArrayList(sp.selectALL())); }
        catch (SQLException e) { e.printStackTrace(); }
    }

    private boolean validerChamps() {
        return !tfNomEspece.getText().isEmpty() && cbFerme.getValue() != null;
    }

    private void viderChamps() {
        tfNomEspece.clear(); tfCycleVie.clear();
        cbFerme.getSelectionModel().clearSelection();
        tvPlantes.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleReturnToSelection(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/tn/esprit/farmai/views/selection-gestion.fxml", "Gestion");
    }
}