package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.dao.ServiceDAO;
import org.example.entity.Service;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ServiceController implements Initializable {

    @FXML private TextField fieldNom;
    @FXML private TextArea fieldDescription;
    @FXML private TextField fieldPrix;
    @FXML private TextField fieldStock;
    @FXML private TextField fieldSeuilCritique;
    @FXML private TableView<Service> tableService;
    @FXML private TableColumn<Service, Integer> colId;
    @FXML private TableColumn<Service, String> colNom;
    @FXML private TableColumn<Service, Double> colPrix;
    @FXML private TableColumn<Service, Integer> colStock;
    @FXML private TableColumn<Service, Integer> colSeuil;

    private final ServiceDAO serviceDAO = new ServiceDAO();
    private final ObservableList<Service> list = FXCollections.observableArrayList();
    private Service selection = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("idService"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colSeuil.setCellValueFactory(new PropertyValueFactory<>("seuilCritique"));
        tableService.setItems(list);
        tableService.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal) -> {
            selection = newVal;
            if (newVal != null) {
                fieldNom.setText(newVal.getNom());
                fieldDescription.setText(newVal.getDescription());
                fieldPrix.setText(String.valueOf(newVal.getPrix()));
                fieldStock.setText(String.valueOf(newVal.getStock()));
                fieldSeuilCritique.setText(String.valueOf(newVal.getSeuilCritique()));
            }
        });
        chargerListe();
    }

    private void chargerListe() {
        list.clear();
        list.addAll(serviceDAO.findAll());
    }

    private void viderFormulaire() {
        fieldNom.clear();
        fieldDescription.clear();
        fieldPrix.clear();
        fieldStock.clear();
        fieldSeuilCritique.clear();
        selection = null;
        tableService.getSelectionModel().clearSelection();
    }

    @FXML
    private void nouveau() {
        viderFormulaire();
    }

    @FXML
    private void enregistrer() {
        String nom = fieldNom.getText();
        if (nom == null || nom.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Nom obligatoire", "Saisissez un nom pour le service.");
            return;
        }
        double prix = 0;
        int stock = 0, seuil = 0;
        try {
            prix = Double.parseDouble(fieldPrix.getText());
            stock = Integer.parseInt(fieldStock.getText());
            seuil = Integer.parseInt(fieldSeuilCritique.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Valeurs invalides", "Prix, stock et seuil critique doivent être des nombres.");
            return;
        }
        if (selection == null) {
            Service s = new Service(nom, fieldDescription.getText(), prix, stock, seuil);
            serviceDAO.create(s);
            showAlert(Alert.AlertType.INFORMATION, "Création", "Service créé.");
        } else {
            selection.setNom(nom);
            selection.setDescription(fieldDescription.getText());
            selection.setPrix(prix);
            selection.setStock(stock);
            selection.setSeuilCritique(seuil);
            serviceDAO.update(selection);
            showAlert(Alert.AlertType.INFORMATION, "Modification", "Service mis à jour.");
        }
        chargerListe();
        viderFormulaire();
    }

    @FXML
    private void modifier() {
        if (selection == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Sélectionnez un service à modifier.");
            return;
        }
        fieldNom.setText(selection.getNom());
        fieldDescription.setText(selection.getDescription());
        fieldPrix.setText(String.valueOf(selection.getPrix()));
        fieldStock.setText(String.valueOf(selection.getStock()));
        fieldSeuilCritique.setText(String.valueOf(selection.getSeuilCritique()));
    }

    @FXML
    private void supprimer() {
        if (selection == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Sélectionnez un service à supprimer.");
            return;
        }
        serviceDAO.delete(selection.getIdService());
        showAlert(Alert.AlertType.INFORMATION, "Suppression", "Service supprimé.");
        chargerListe();
        viderFormulaire();
    }

    public void chargerEtAlerterStockCritique() {
        chargerListe();
        List<Service> critiques = serviceDAO.findStockCritique();
        if (!critiques.isEmpty()) {
            StringBuilder sb = new StringBuilder("Services en stock critique :\n");
            for (Service s : critiques) {
                sb.append("• ").append(s.getNom()).append(" — Stock: ").append(s.getStock()).append(" (seuil: ").append(s.getSeuilCritique()).append(")\n");
            }
            showAlert(Alert.AlertType.WARNING, "Stock critique", sb.toString());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
