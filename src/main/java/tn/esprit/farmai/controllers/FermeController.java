package tn.esprit.farmai.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import tn.esprit.farmai.models.Ferme;
import tn.esprit.farmai.services.ServiceFerme;
import tn.esprit.farmai.utils.NavigationUtil;

import java.net.URL;
import java.util.ResourceBundle;

public class FermeController implements Initializable {

    @FXML private TextField tfNom;
    @FXML private TextField tfLieu;
    @FXML private TextField tfSurface;
    @FXML private TextField tfRecherche; // Nouveau champ pour la recherche

    @FXML private TableView<Ferme> tvFermes;
    @FXML private TableColumn<Ferme, String> colNom;
    @FXML private TableColumn<Ferme, String> colLieu;
    @FXML private TableColumn<Ferme, Float> colSurface;

    private final ServiceFerme sf = new ServiceFerme();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom_ferme"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        colSurface.setCellValueFactory(new PropertyValueFactory<>("surface"));

        tvFermes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                tfNom.setText(newSelection.getNom_ferme());
                tfLieu.setText(newSelection.getLieu());
                tfSurface.setText(String.valueOf(newSelection.getSurface()));
            }
        });

        rafraichir();
    }

    @FXML
    private void handleRecherche() {
        String query = tfRecherche.getText().trim();
        try {
            if (query.isEmpty()) {
                rafraichir();
            } else {
                // Appel de la méthode chercherParNom que vous avez ajoutée au Service
                tvFermes.setItems(FXCollections.observableArrayList(sf.chercherParNom(query)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ajouter() {
        try {
            if (validerChamps()) {
                Ferme f = new Ferme(0, tfNom.getText(), tfLieu.getText(), Float.parseFloat(tfSurface.getText()));
                sf.insertOne(f);
                rafraichir();
                viderChamps();
            }
        } catch (NumberFormatException e) {
            afficherAlerte("Erreur de format", "La surface doit être un nombre décimal.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void modifier() {
        Ferme selected = tvFermes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                if (validerChamps()) {
                    selected.setNom_ferme(tfNom.getText());
                    selected.setLieu(tfLieu.getText());
                    selected.setSurface(Float.parseFloat(tfSurface.getText()));

                    sf.updateOne(selected);
                    tvFermes.refresh(); // Mise à jour visuelle immédiate
                    rafraichir();       // Resynchronisation avec la DB
                    viderChamps();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            afficherAlerte("Sélection requise", "Veuillez sélectionner une ferme.");
        }
    }

    @FXML
    private void supprimer() {
        Ferme selected = tvFermes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette ferme ?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    try {
                        sf.deleteOne(selected);
                        rafraichir();
                        viderChamps();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void rafraichir() {
        try {
            tvFermes.setItems(FXCollections.observableArrayList(sf.selectALL()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validerChamps() {
        if (tfNom.getText().trim().isEmpty() || tfLieu.getText().trim().isEmpty() || tfSurface.getText().trim().isEmpty()) {
            afficherAlerte("Champs vides", "Veuillez remplir toutes les informations.");
            return false;
        }
        return true;
    }

    private void viderChamps() {
        tfNom.clear();
        tfLieu.clear();
        tfSurface.clear();
        tvFermes.getSelectionModel().clearSelection();
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleReturnToSelection(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationUtil.navigateTo(stage, "/tn/esprit/farmai/views/selection-gestion.fxml", "FarmAI - Gestion");
    }
}