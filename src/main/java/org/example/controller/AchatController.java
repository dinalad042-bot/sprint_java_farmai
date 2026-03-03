package org.example.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.dao.AchatDAO;
import org.example.dao.ServiceDAO;
import org.example.entity.Achat;
import org.example.entity.ListeAchat;
import org.example.entity.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import java.util.ResourceBundle;

public class AchatController implements Initializable {

    @FXML private DatePicker dateAchat;
    @FXML private TextField fieldTotal;
    @FXML private ComboBox<Service> comboService;
    @FXML private TextField fieldQuantite;
    @FXML private TextField fieldPrixUnitaire;

    @FXML private TableView<ListeAchat> tableLignes;
    @FXML private TableColumn<ListeAchat, String> colLigneService;
    @FXML private TableColumn<ListeAchat, Integer> colLigneQuantite;
    @FXML private TableColumn<ListeAchat, Double> colLignePrix;
    @FXML private TableColumn<ListeAchat, Double> colLigneSousTotal;

    @FXML private TableView<Achat> tableAchat;
    @FXML private TableColumn<Achat, Integer> colAchatId;
    @FXML private TableColumn<Achat, LocalDate> colAchatDate;
    @FXML private TableColumn<Achat, Double> colAchatTotal;

    @FXML private ImageView qrCodeImageView;
    @FXML private Label labelConverted;
    @FXML private ComboBox<String> comboCurrency;

    private final AchatDAO achatDAO = new AchatDAO();
    private final ServiceDAO serviceDAO = new ServiceDAO();

    private final ObservableList<Achat> listAchats = FXCollections.observableArrayList();
    private final ObservableList<ListeAchat> listLignes = FXCollections.observableArrayList();

    private final ExchangeRateService fxService = new ExchangeRateService();

    // Base currency of your prices/totals in the UI
    private static final String BASE_CCY = "EUR";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dateAchat.setValue(LocalDate.now());

        comboService.setItems(FXCollections.observableArrayList(serviceDAO.findAll()));
        comboService.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Service s) {
                return s == null ? "" : s.getNom() + " (ID " + s.getIdService() + ")";
            }
            @Override public Service fromString(String string) { return null; }
        });

        // Lines table
        colLigneService.setCellValueFactory(new PropertyValueFactory<>("nomService"));
        colLigneQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colLignePrix.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        colLigneSousTotal.setCellValueFactory(new PropertyValueFactory<>("sousTotal"));
        tableLignes.setItems(listLignes);

        // Achat table
        colAchatId.setCellValueFactory(new PropertyValueFactory<>("idAchat"));
        colAchatDate.setCellValueFactory(new PropertyValueFactory<>("dateAchat"));
        colAchatTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        tableAchat.setItems(listAchats);

        comboService.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal) -> {
            if (newVal != null) fieldPrixUnitaire.setText(String.valueOf(newVal.getPrix()));
        });

        // Load currencies into comboCurrency
        loadCurrencies();

        chargerAchats();
        majTotal();
    }

    private void loadCurrencies() {
        var codes = fxService.getCurrencyCodes(BASE_CCY);
        if (codes == null || codes.isEmpty()) {
            // Don’t crash UI, just show alert and leave combo empty
            showAlert(Alert.AlertType.ERROR, "Currency", "Could not load currency list (check internet).");
            return;
        }
        comboCurrency.setItems(FXCollections.observableArrayList(codes));
        comboCurrency.getSelectionModel().select("TND"); // default
    }

    private void chargerAchats() {
        listAchats.clear();
        listAchats.addAll(achatDAO.findAll());
    }

    private double calculerTotal() {
        return listLignes.stream().mapToDouble(ListeAchat::getSousTotal).sum();
    }

    private void majTotal() {
        fieldTotal.setText(String.format("%.2f", calculerTotal()));
    }

    @FXML
    private void nouveau() {
        dateAchat.setValue(LocalDate.now());
        listLignes.clear();
        fieldTotal.setText("0.00");
        labelConverted.setText("Converted: -");
        qrCodeImageView.setVisible(false);

        comboService.getSelectionModel().clearSelection();
        fieldQuantite.clear();
        fieldPrixUnitaire.clear();
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

        ListeAchat ligne = new ListeAchat(0, s.getIdService(), qte, pu);
        ligne.setNomService(s.getNom());
        listLignes.add(ligne);

        majTotal();
        labelConverted.setText("Converted: -");

        fieldQuantite.clear();
    }

    @FXML
    private void retirerLigne() {
        ListeAchat sel = tableLignes.getSelectionModel().getSelectedItem();
        if (sel != null) {
            listLignes.remove(sel);
            majTotal();
            labelConverted.setText("Converted: -");
        }
    }

    @FXML
    private void enregistrer() {
        if (dateAchat.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Date", "Choisissez une date d'achat.");
            return;
        }
        if (listLignes.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Lignes", "Ajoutez au moins une ligne à l'achat.");
            return;
        }

        Achat achat = new Achat(dateAchat.getValue(), calculerTotal());
        achat.getLignes().addAll(listLignes);

        achatDAO.create(achat);
        showAlert(Alert.AlertType.INFORMATION, "Enregistrement", "Achat enregistré. Les stocks ont été mis à jour.");

        chargerAchats();
        nouveau();
    }

    @FXML
    private void supprimer() {
        Achat sel = tableAchat.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Sélectionnez un achat à supprimer.");
            return;
        }
        achatDAO.delete(sel.getIdAchat());
        showAlert(Alert.AlertType.INFORMATION, "Suppression", "Achat supprimé.");
        chargerAchats();
    }

    @FXML
    private void convertTotal(ActionEvent event) {
        if (listLignes.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Conversion", "Ajoutez au moins une ligne d'achat.");
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

    @FXML
    private void payerStripe(ActionEvent event) {
        if (listLignes.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Paiement", "Ajoutez au moins une ligne avant de payer.");
            return;
        }

        double total = calculerTotal();
        if (total <= 0) {
            showAlert(Alert.AlertType.WARNING, "Paiement", "Total invalide.");
            return;
        }

        long amountCents = BigDecimal.valueOf(total)
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        Payment paymentService = new Payment();
        String checkoutUrl = paymentService.createCheckoutSession(amountCents);

        if (checkoutUrl == null || checkoutUrl.isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Stripe", "Impossible de créer la session de paiement Stripe.");
            return;
        }

        try {
            Path outDir = Paths.get(System.getProperty("user.home"), "FarmIADesk");
            Files.createDirectories(outDir);

            Path qrPath = outDir.resolve("stripe_checkout_qr.png");
            generateQrPng(checkoutUrl, qrPath, 260, 260);

            qrCodeImageView.setImage(new Image(qrPath.toUri().toString()));
            qrCodeImageView.setVisible(true);

            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Paiement Stripe");
            a.setHeaderText("QR Code généré");
            a.setContentText("Scannez le QR Code pour ouvrir le paiement Stripe.\n\nFichier: " + qrPath);
            a.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "QR Code", "Erreur lors de la génération du QR Code.");
        }
    }

    private void generateQrPng(String data, Path path, int width, int height) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, width, height, hints);
        MatrixToImageWriter.writeToPath(matrix, "PNG", path);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}