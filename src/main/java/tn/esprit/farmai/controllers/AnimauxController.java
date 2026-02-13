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

public class AnimauxController implements Initializable {

    @FXML private TextField tfEspece;
    @FXML private TextField tfEtat;
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<Ferme> cbFerme;
    @FXML private TextField tfRecherche;

    @FXML private TableView<Animaux> tvAnimaux;
    @FXML private TableColumn<Animaux, String> colEspece;
    @FXML private TableColumn<Animaux, String> colEtat;
    @FXML private TableColumn<Animaux, Date> colDate;
    @FXML private TableColumn<Animaux, Integer> colIdFerme;

    private final ServiceAnimaux sa = new ServiceAnimaux();
    private final ServiceFerme sf = new ServiceFerme();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colEspece.setCellValueFactory(new PropertyValueFactory<>("espece"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat_sante"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_naissance"));
        colIdFerme.setCellValueFactory(new PropertyValueFactory<>("id_ferme"));

        chargerFermes();
        rafraichir();

        tvAnimaux.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                tfEspece.setText(newVal.getEspece());
                tfEtat.setText(newVal.getEtat_sante());
                dpDate.setValue(newVal.getDate_naissance().toLocalDate());
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
    private void handleRecherche() {
        String query = tfRecherche.getText().trim();
        try {
            if (query.isEmpty()) {
                rafraichir();
            } else {
                tvAnimaux.setItems(FXCollections.observableArrayList(sa.chercherParEspece(query)));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void ajouter() {
        if (!validerChamps()) return;
        try {
            Animaux a = new Animaux(0, tfEspece.getText(), tfEtat.getText(),
                    Date.valueOf(dpDate.getValue()), cbFerme.getValue().getId_ferme());
            sa.insertOne(a);
            rafraichir();
            viderChamps();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void modifier() {
        Animaux selected = tvAnimaux.getSelectionModel().getSelectedItem();
        if (selected != null && validerChamps()) {
            try {
                selected.setEspece(tfEspece.getText());
                selected.setEtat_sante(tfEtat.getText());
                selected.setDate_naissance(Date.valueOf(dpDate.getValue()));
                selected.setId_ferme(cbFerme.getValue().getId_ferme());

                sa.updateOne(selected);
                tvAnimaux.refresh();
                rafraichir();
                viderChamps();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void supprimer() {
        Animaux selected = tvAnimaux.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                sa.deleteOne(selected);
                rafraichir();
                viderChamps();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void rafraichir() {
        try { tvAnimaux.setItems(FXCollections.observableArrayList(sa.selectALL())); }
        catch (SQLException e) { e.printStackTrace(); }
    }

    private boolean validerChamps() {
        return !tfEspece.getText().isEmpty() && dpDate.getValue() != null && cbFerme.getValue() != null;
    }

    private void viderChamps() {
        tfEspece.clear(); tfEtat.clear(); dpDate.setValue(null);
        cbFerme.getSelectionModel().clearSelection();
        tvAnimaux.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleReturnToSelection(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/tn/esprit/farmai/views/selection-gestion.fxml", "Gestion");
    }
}