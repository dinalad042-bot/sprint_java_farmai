package tn.esprit.farmai.controllers.erp;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.farmai.models.Achat;
import tn.esprit.farmai.models.LigneAchat;
import tn.esprit.farmai.models.Matiere;
import tn.esprit.farmai.services.AchatService;
import tn.esprit.farmai.services.ExchangeRateService;
import tn.esprit.farmai.services.MatiereService;
import tn.esprit.farmai.utils.AlertUtils;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Read-only catalogue of raw materials for the AGRICOLE role.
 * Mirrors Symfony erp/matiere/catalogue.html.twig
 *
 * Agriculteur can:
 *  - Browse all matières and their stock/price
 *  - Click "Passer une Commande" (header button) → opens achat dialog with all matières
 *  - Click "Commander" on a specific row → opens achat dialog pre-filled with that matière
 *
 * Cannot add/edit/delete matières (fournisseur-only).
 */
public class CatalogueMatieresController extends AgricoleBaseERPController {

    @FXML private TextField searchField;
    @FXML private TableView<Matiere> matieresTable;
    @FXML private TableColumn<Matiere, String> colNom;
    @FXML private TableColumn<Matiere, String> colUnite;
    @FXML private TableColumn<Matiere, String> colStock;
    @FXML private TableColumn<Matiere, String> colPrix;
    @FXML private TableColumn<Matiere, String> colSeuil;
    @FXML private TableColumn<Matiere, String> colStatut;
    @FXML private TableColumn<Matiere, Void>   colCommander;

    private final MatiereService matiereService = new MatiereService();
    private final AchatService achatService = new AchatService();
    private ObservableList<Matiere> allMatieres = FXCollections.observableArrayList();

    @Override
    protected void onInit() {
        setupColumns();
        loadData();
    }

    private void setupColumns() {
        colNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom()));
        colUnite.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUnite()));
        colStock.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.2f", c.getValue().getStock())));
        colPrix.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.2f TND", c.getValue().getPrixUnitaire())));
        colSeuil.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.2f", c.getValue().getSeuilCritique())));

        colStatut.setCellValueFactory(c -> {
            Matiere m = c.getValue();
            if (m.getStock() <= 0)        return new SimpleStringProperty("❌ Épuisé");
            if (m.isStockCritique())       return new SimpleStringProperty("⚠️ Stock faible");
            return new SimpleStringProperty("✅ Disponible");
        });
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if      (item.contains("Épuisé")) setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");
                else if (item.contains("faible")) setStyle("-fx-text-fill: #E65100; -fx-font-weight: bold;");
                else                              setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
            }
        });

        // "Commander" button per row — opens achat dialog pre-filled with this matière
        colCommander.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("🛒 Commander");
            {
                btn.getStyleClass().add("primary-btn");
                btn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10;");
                btn.setOnAction(e -> {
                    Matiere m = getTableView().getItems().get(getIndex());
                    showAchatDialog(m);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                // Disable if stock is 0
                Matiere m = getTableView().getItems().get(getIndex());
                btn.setDisable(m.getStock() <= 0);
                btn.setText(m.getStock() <= 0 ? "❌ Épuisé" : "🛒 Commander");
                setGraphic(btn);
            }
        });
    }

    private void loadData() {
        try {
            allMatieres = FXCollections.observableArrayList(matiereService.selectAll());
            matieresTable.setItems(allMatieres);
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger le catalogue: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase().trim();
        matieresTable.setItems(query.isEmpty() ? allMatieres :
            allMatieres.filtered(m ->
                m.getNom().toLowerCase().contains(query) ||
                m.getUnite().toLowerCase().contains(query)));
    }

    /**
     * "Passer une Commande" header button — opens achat dialog with all matières available.
     */
    @FXML
    private void handleNewAchat() {
        showAchatDialog(null);
    }

    /**
     * Opens the achat creation dialog.
     * If preSelected is not null, the first row is pre-filled with that matière.
     */
    private void showAchatDialog(Matiere preSelected) {
        Dialog<Achat> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Commande d'Achat");
        dialog.setHeaderText("Commander des matières premières");
        dialog.getDialogPane().setPrefWidth(620);

        ButtonType saveBtn = new ButtonType("✅  Confirmer la commande", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        VBox content = new VBox(14);
        content.setPadding(new Insets(20));

        // Date picker
        Label dateLabel = new Label("Date de la commande:");
        dateLabel.setStyle("-fx-font-weight: bold;");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        content.getChildren().addAll(dateLabel, datePicker);

        // Lines header
        Label lignesLabel = new Label("Matières à commander:");
        lignesLabel.setStyle("-fx-font-weight: bold;");
        content.getChildren().add(lignesLabel);

        // Load all matières for the combo boxes
        List<Matiere> matieres;
        try { matieres = matiereService.selectAll(); }
        catch (SQLException e) { matieres = new ArrayList<>(); }
        final List<Matiere> matieresFinal = matieres;

        VBox lignesBox = new VBox(8);
        List<HBox> ligneRows = new ArrayList<>();

        // Add first row — pre-fill if a matière was selected from the table
        lignesBox.getChildren().add(buildLigneRow(matieresFinal, preSelected, ligneRows, lignesBox));

        Button addBtn = new Button("➕  Ajouter une matière");
        addBtn.getStyleClass().add("secondary-btn");
        addBtn.setOnAction(e -> lignesBox.getChildren().add(
            buildLigneRow(matieresFinal, null, ligneRows, lignesBox)));

        content.getChildren().addAll(lignesBox, addBtn);

        // ── Total bar ──
        Label totalBar = new Label("Total: 0.00 TND");
        totalBar.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #22C55E; " +
                "-fx-font-weight: bold; -fx-font-size: 15px; " +
                "-fx-background-radius: 10; -fx-padding: 10 20;");
        totalBar.setMaxWidth(Double.MAX_VALUE);

        // ── Currency conversion box ──
        Label convertResult = new Label();
        convertResult.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");
        convertResult.setWrapText(true);

        ComboBox<String> currencyCombo = new ComboBox<>();
        currencyCombo.getItems().addAll("TND","USD","GBP","JPY","CHF","CAD","MAD","DZD","SAR","AED");
        currencyCombo.setValue("TND");
        currencyCombo.setPrefWidth(100);

        Button convertBtn = new Button("🔄 Convertir");
        convertBtn.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 6 14;");

        ExchangeRateService exchangeRateService = new ExchangeRateService();
        convertBtn.setOnAction(ev -> {
            double total = ligneRows.stream().mapToDouble(row -> {
                try {
                    double q = Double.parseDouble(((TextField) row.getChildren().get(1)).getText().trim().isEmpty() ? "0" : ((TextField) row.getChildren().get(1)).getText().trim());
                    double p = Double.parseDouble(((TextField) row.getChildren().get(2)).getText().trim().isEmpty() ? "0" : ((TextField) row.getChildren().get(2)).getText().trim());
                    return q * p;
                } catch (NumberFormatException e) { return 0; }
            }).sum();
            String currency = currencyCombo.getValue();
            convertResult.setText("⏳ Conversion en cours…");
            Thread t = new Thread(() -> {
                Double converted = exchangeRateService.convert(total, "EUR", currency);
                Double rate      = exchangeRateService.getRate("EUR", currency);
                javafx.application.Platform.runLater(() -> {
                    if (converted == null) convertResult.setText("⚠️ API indisponible.");
                    else convertResult.setText(String.format("💱  %.2f EUR = %.2f %s  (taux : %.4f)", total, converted, currency, rate));
                    totalBar.setText(String.format("Total: %.2f TND", total));
                });
            });
            t.setDaemon(true); t.start();
        });

        VBox convertBox = new VBox(8);
        convertBox.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 12; " +
                "-fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-padding: 12;");
        Label convertTitle = new Label("💱  Conversion de devise");
        convertTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #475569;");
        HBox convertRow = new HBox(10, currencyCombo, convertBtn);
        convertRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        convertBox.getChildren().addAll(convertTitle, convertRow, convertResult);

        content.getChildren().addAll(totalBar, convertBox);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn != saveBtn) return null;

            Achat achat = new Achat();
            achat.setDateAchat(datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now());

            List<LigneAchat> lignes = new ArrayList<>();
            for (HBox row : ligneRows) {
                ComboBox<Matiere> combo = (ComboBox<Matiere>) row.getChildren().get(0);
                TextField qteField    = (TextField) row.getChildren().get(1);
                TextField prixField   = (TextField) row.getChildren().get(2);

                if (combo.getValue() == null) continue;
                String qteText  = qteField.getText().trim();
                String prixText = prixField.getText().trim();
                if (qteText.isEmpty()) continue;

                try {
                    double qte  = Double.parseDouble(qteText);
                    double prix = prixText.isEmpty() ? 0 : Double.parseDouble(prixText);
                    if (qte <= 0) {
                        AlertUtils.showError("Validation", "La quantité doit être supérieure à 0.");
                        return null;
                    }
                    LigneAchat l = new LigneAchat();
                    l.setIdMatiere(combo.getValue().getIdMatiere());
                    l.setQuantite(qte);
                    l.setPrixUnitaire(prix);
                    l.setNomMatiere(combo.getValue().getNom());
                    lignes.add(l);
                } catch (NumberFormatException ex) {
                    AlertUtils.showError("Validation", "Quantité et prix doivent être des nombres valides.");
                    return null;
                }
            }

            if (lignes.isEmpty()) {
                AlertUtils.showError("Validation", "Ajoutez au moins une matière à commander.");
                return null;
            }
            achat.setLignes(lignes);
            return achat;
        });

        Optional<Achat> result = dialog.showAndWait();
        result.ifPresent(achat -> {
            try {
                achatService.createAchat(achat);
                AlertUtils.showSuccess("Commande enregistrée",
                    "Votre commande a été enregistrée avec succès.\n" +
                    "Total: " + String.format("%.2f TND", achat.getTotal()) + "\n" +
                    "Rendez-vous dans 'Mes Achats' pour payer et recevoir les matières.");
                // Refresh the catalogue table
                loadData();
            } catch (Exception e) {
                AlertUtils.showError("Erreur", "Impossible d'enregistrer la commande: " + e.getMessage());
            }
        });
    }

    /**
     * Builds one row in the achat dialog.
     * If preSelected is not null, the combo is pre-filled and the price auto-filled.
     */
    private HBox buildLigneRow(List<Matiere> matieres, Matiere preSelected,
                                List<HBox> rows, VBox box) {
        ComboBox<Matiere> combo = new ComboBox<>(FXCollections.observableArrayList(matieres));
        combo.setPromptText("Choisir une matière...");
        combo.setPrefWidth(210);

        TextField prixField = new TextField();
        prixField.setPromptText("Prix/unité (TND)");
        prixField.setPrefWidth(110);

        TextField qteField = new TextField("1");
        qteField.setPromptText("Quantité");
        qteField.setPrefWidth(80);

        // Auto-fill price when matière is selected
        combo.setOnAction(e -> {
            if (combo.getValue() != null)
                prixField.setText(String.valueOf(combo.getValue().getPrixUnitaire()));
        });

        // Pre-fill if a specific matière was passed
        if (preSelected != null) {
            combo.setValue(preSelected);
            prixField.setText(String.valueOf(preSelected.getPrixUnitaire()));
        }

        Button removeBtn = new Button("✕");
        removeBtn.getStyleClass().add("danger-btn");

        // Label showing unit — kept for display but placed AFTER the fields
        // so index-based reads in resultConverter stay: 0=combo, 1=qte, 2=prix
        Label uniteLabel = new Label();
        uniteLabel.setStyle("-fx-text-fill: #78909C; -fx-font-size: 12px;");
        combo.setOnAction(e -> {
            if (combo.getValue() != null) {
                prixField.setText(String.valueOf(combo.getValue().getPrixUnitaire()));
                uniteLabel.setText(combo.getValue().getUnite());
            }
        });
        if (preSelected != null) uniteLabel.setText(preSelected.getUnite());

        // Order: combo[0], qteField[1], prixField[2], uniteLabel[3], removeBtn[4]
        HBox row = new HBox(8, combo, qteField, prixField, uniteLabel, removeBtn);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        rows.add(row);
        removeBtn.setOnAction(e -> {
            box.getChildren().remove(row);
            rows.remove(row);
        });
        return row;
    }
}
