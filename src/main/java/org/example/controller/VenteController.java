package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.dao.ServiceDAO;
import org.example.entity.ListeVente;
import org.example.entity.Service;
import org.example.entity.Vente;
import org.example.dao.ServiceDAO;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class VenteController implements Initializable {

    @FXML private DatePicker dateVente;
    @FXML private TextField fieldTotal;
    @FXML private ComboBox<Service> comboService;
    @FXML private TextField fieldQuantite;
    @FXML private TextField fieldPrixUnitaire;

    @FXML private TableView<ListeVente> tableLignes;
    @FXML private TableColumn<ListeVente, String> colLigneService;
    @FXML private TableColumn<ListeVente, Integer> colLigneQuantite;
    @FXML private TableColumn<ListeVente, Double> colLignePrix;
    @FXML private TableColumn<ListeVente, Double> colLigneSousTotal;

    @FXML private TableView<Vente> tableVentes;
    @FXML private TableColumn<Vente, Integer> colVenteId;
    @FXML private TableColumn<Vente, LocalDate> colVenteDate;
    @FXML private TableColumn<Vente, Double> colVenteTotal;

    @FXML private Label labelConverted;
    @FXML private ComboBox<String> comboCurrency;

    private final VenteDAO venteDAO = new VenteDAO();
    private final ServiceDAO serviceDAO = new ServiceDAO();

    private final ObservableList<Vente> listVentes = FXCollections.observableArrayList();
    private final ObservableList<ListeVente> listLignes = FXCollections.observableArrayList();

    private final ExchangeRateService fxService = new ExchangeRateService();
    private static final String BASE_CCY = "EUR";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dateVente.setValue(LocalDate.now());

        comboService.setItems(FXCollections.observableArrayList(serviceDAO.findAll()));
        comboService.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Service s) {
                return s == null ? "" : s.getNom() + " (Stock " + s.getStock() + ")";
            }
            @Override public Service fromString(String string) { return null; }
        });

        comboService.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal) -> {
            if (newVal != null) fieldPrixUnitaire.setText(String.valueOf(newVal.getPrix()));
        });

        // lignes table
        colLigneService.setCellValueFactory(new PropertyValueFactory<>("nomService"));
        colLigneQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colLignePrix.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        colLigneSousTotal.setCellValueFactory(new PropertyValueFactory<>("sousTotal"));
        tableLignes.setItems(listLignes);

        // ventes table
        colVenteId.setCellValueFactory(new PropertyValueFactory<>("idVente"));
        colVenteDate.setCellValueFactory(new PropertyValueFactory<>("dateVente"));
        colVenteTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        tableVentes.setItems(listVentes);

        loadCurrencies();
        chargerVentes();
        majTotal();
    }

    private void loadCurrencies() {
        var codes = fxService.getCurrencyCodes(BASE_CCY);
        if (codes == null || codes.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Currency", "Could not load currency list (check internet).");
            return;
        }
        comboCurrency.setItems(FXCollections.observableArrayList(codes));
        comboCurrency.getSelectionModel().select("TND");
    }

    private void chargerVentes() {
        listVentes.clear();
        listVentes.addAll(venteDAO.findAll());
    }

    private double calculerTotal() {
        return listLignes.stream().mapToDouble(ListeVente::getSousTotal).sum();
    }

    private void majTotal() {
        fieldTotal.setText(String.format("%.2f", calculerTotal()));
    }

    @FXML
    private void nouveau() {
        dateVente.setValue(LocalDate.now());
        listLignes.clear();
        fieldTotal.setText("0.00");
        labelConverted.setText("Converted: -");
        comboService.getSelectionModel().clearSelection();
        fieldQuantite.clear();
        fieldPrixUnitaire.clear();

        // refresh service list to show latest stocks
        comboService.setItems(FXCollections.observableArrayList(serviceDAO.findAll()));
    }

    @FXML
    private void ajouterLigne() {
        Service s = comboService.getSelectionModel().getSelectedItem();
        if (s == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Choisissez un service.");
            return;
        }

        int qte;
        double pu;
        try {
            qte = Integer.parseInt(fieldQuantite.getText());
            pu = Double.parseDouble(fieldPrixUnitaire.getText());
        } catch (Exception e) {
            showAlert(Alert.AlertType.WARNING, "Saisie", "Quantité et prix unitaire doivent être des nombres.");
            return;
        }

        if (qte <= 0) {
            showAlert(Alert.AlertType.WARNING, "Saisie", "La quantité doit être positive.");
            return;
        }

        // ✅ Stock check: cannot sell more than available
        if (s.getStock() <= 0) {
            showAlert(Alert.AlertType.WARNING, "Stock", "Stock épuisé. Impossible de vendre ce service.");
            return;
        }

        int alreadyInCart = listLignes.stream()
                .filter(l -> l.getIdService() == s.getIdService())
                .mapToInt(ListeVente::getQuantite)
                .sum();

        int available = s.getStock() - alreadyInCart;
        if (qte > available) {
            showAlert(Alert.AlertType.WARNING, "Stock insuffisant",
                    "Stock disponible: " + available + "\nVous ne pouvez pas vendre " + qte + ".");
            return;
        }

        // Merge same service lines
        for (ListeVente l : listLignes) {
            if (l.getIdService() == s.getIdService()) {
                l.setQuantite(l.getQuantite() + qte);
                l.setPrixUnitaire(pu);
                tableLignes.refresh();
                majTotal();
                labelConverted.setText("Converted: -");
                fieldQuantite.clear();
                return;
            }
        }

        ListeVente ligne = new ListeVente(0, s.getIdService(), qte, pu);
        ligne.setNomService(s.getNom());
        listLignes.add(ligne);

        majTotal();
        labelConverted.setText("Converted: -");
        fieldQuantite.clear();
    }

    @FXML
    private void retirerLigne() {
        ListeVente sel = tableLignes.getSelectionModel().getSelectedItem();
        if (sel != null) {
            listLignes.remove(sel);
            majTotal();
            labelConverted.setText("Converted: -");
        }
    }

    @FXML
    private void enregistrer() {
        if (dateVente.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Date", "Choisissez une date de vente.");
            return;
        }
        if (listLignes.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Lignes", "Ajoutez au moins une ligne à la vente.");
            return;
        }

        // ✅ Final stock validation (safe)
        for (ListeVente l : listLignes) {
            Service s = serviceDAO.findById(l.getIdService());
            if (s == null) {
                showAlert(Alert.AlertType.ERROR, "Service", "Service introuvable (ID " + l.getIdService() + ").");
                return;
            }
            if (s.getStock() < l.getQuantite()) {
                showAlert(Alert.AlertType.WARNING, "Stock insuffisant",
                        "Service: " + s.getNom() + "\nStock: " + s.getStock() + "\nDemandé: " + l.getQuantite());
                return;
            }
        }

        Vente vente = new Vente(dateVente.getValue(), calculerTotal());
        vente.getLignes().addAll(listLignes);

        // ✅ Save sale + decrease stock (do it in DAO transaction ideally)
        venteDAO.create(vente);

        // decrease stock for each service
        for (ListeVente l : listLignes) {
            serviceDAO.decreaseStock(l.getIdService(), l.getQuantite());

            int newStock = serviceDAO.getStockById(l.getIdService());
            if (newStock == 0) {
                Service s = serviceDAO.findById(l.getIdService());

                // IMPORTANT: put your Gmail + app password here (better from env vars)
                org.example.service.EmailService mail = new org.example.service.EmailService(
                        "leila.bellakhdhar@aiesec.net",
                        "wcdn mijt ntqr cnob"
                );

                String subject = "🚨 Stock épuisé — " + s.getNom();
                String body = org.example.service.EmailService.buildStockZeroEmail(s.getNom(), s.getIdService());
                mail.send("bellakhdharleila@gmail.com", subject, body);
            }
        }
        showAlert(Alert.AlertType.INFORMATION, "Enregistrement", "Vente enregistrée. Le stock a été mis à jour.");

        chargerVentes();
        nouveau();
    }

    @FXML
    private void supprimer() {
        Vente sel = tableVentes.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Sélectionnez une vente à supprimer.");
            return;
        }
        venteDAO.delete(sel.getIdVente());
        showAlert(Alert.AlertType.INFORMATION, "Suppression", "Vente supprimée.");
        chargerVentes();
    }

    @FXML
    private void convertTotal(ActionEvent event) {
        if (listLignes.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Conversion", "Ajoutez au moins une ligne de vente.");
            return;
        }

        String target = comboCurrency.getSelectionModel().getSelectedItem();
        if (target == null || target.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Conversion", "Choisissez une devise.");
            return;
        }

        BigDecimal total = BigDecimal.valueOf(calculerTotal());
        BigDecimal converted = fxService.convert(total, BASE_CCY, target);
        if (converted == null) {
            showAlert(Alert.AlertType.ERROR, "Conversion", "Impossible de récupérer le taux (API indisponible).");
            return;
        }

        BigDecimal rate = fxService.getRate(BASE_CCY, target);
        labelConverted.setText("Converted: " + converted + " " + target +
                " (1 " + BASE_CCY + " = " + rate + " " + target + ")");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}