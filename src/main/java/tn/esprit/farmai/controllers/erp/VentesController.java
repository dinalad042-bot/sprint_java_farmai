package tn.esprit.farmai.controllers.erp;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.farmai.models.LigneVente;
import tn.esprit.farmai.models.Produit;
import tn.esprit.farmai.models.Vente;
import tn.esprit.farmai.services.ProduitService;
import tn.esprit.farmai.services.ExchangeRateService;
import tn.esprit.farmai.services.VenteService;
import tn.esprit.farmai.utils.AlertUtils;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Ventes controller — FOURNISSEUR only.
 * Fournisseur sells produits finis created by agricole.
 * Mirrors Symfony VenteController with ROLE_FOURNISSEUR.
 */
public class VentesController extends BaseERPController {

    @FXML private TableView<Vente> ventesTable;
    @FXML private TableColumn<Vente, String> colId;
    @FXML private TableColumn<Vente, String> colDate;
    @FXML private TableColumn<Vente, String> colTotal;
    @FXML private TableColumn<Vente, String> colLignes;
    @FXML private TableColumn<Vente, Void> colActions;
    @FXML private Label totalVentesLabel;
    @FXML private Label totalMontantLabel;
    @FXML private Label moyenneLabel;

    private final VenteService venteService = new VenteService();
    private final ProduitService produitService = new ProduitService();
    private ObservableList<Vente> allVentes = FXCollections.observableArrayList();

    @Override
    protected void onInit() {
        setupColumns();
        loadData();
    }

    // Override navigation — fournisseur sidebar: Matières + Ventes only
    @Override
    @FXML
    protected void handleDashboard() {
        var stage = getStage();
        if (stage != null)
            tn.esprit.farmai.utils.NavigationUtil.navigateTo(stage, "views/fournisseur-dashboard.fxml", "Tableau de Bord Fournisseur");
    }

    @Override
    @FXML
    protected void handleMatieres() {
        var stage = getStage();
        if (stage != null)
            tn.esprit.farmai.utils.NavigationUtil.navigateTo(stage, "views/erp-matieres.fxml", "Mon Catalogue");
    }

    private void setupColumns() {
        colId.setCellValueFactory(c -> new SimpleStringProperty("#" + c.getValue().getIdVente()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDateVente().toString()));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.2f TND", c.getValue().getTotal())));
        colLignes.setCellValueFactory(c -> {
            String produits = c.getValue().getLignes().stream()
                .map(l -> l.getNomProduit() + " x" + l.getQuantite())
                .collect(Collectors.joining(", "));
            return new SimpleStringProperty(produits.isEmpty() ? "—" : produits);
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button detailBtn = new Button("👁️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox box = new HBox(6, detailBtn, deleteBtn);
            {
                detailBtn.setTooltip(new Tooltip("Détails"));
                deleteBtn.setTooltip(new Tooltip("Supprimer"));
                detailBtn.setStyle("-fx-background-color: #E3F2FD; -fx-background-radius: 6; -fx-padding: 4 8; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #FFEBEE; -fx-background-radius: 6; -fx-padding: 4 8; -fx-cursor: hand;");
                box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                detailBtn.setOnAction(e -> handleDetails(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadData() {
        try {
            allVentes = FXCollections.observableArrayList(venteService.selectAll());
            ventesTable.setItems(allVentes);
            updateStats();
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les ventes: " + e.getMessage());
        }
    }

    private void updateStats() {
        int total = allVentes.size();
        double montant = allVentes.stream().mapToDouble(Vente::getTotal).sum();
        double moyenne = total > 0 ? montant / total : 0;

        if (totalVentesLabel != null) totalVentesLabel.setText(String.valueOf(total));
        if (totalMontantLabel != null) totalMontantLabel.setText(String.format("%.2f TND", montant));
        if (moyenneLabel != null) moyenneLabel.setText(String.format("%.2f TND", moyenne));
    }

    @FXML
    private void handleAdd() {
        showVenteDialog();
    }

    private void handleDetails(Vente vente) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la vente #" + vente.getIdVente());
        alert.setHeaderText("Bon de vente du " + vente.getDateVente());

        StringBuilder sb = new StringBuilder();
        sb.append("Produits vendus:\n");
        for (LigneVente l : vente.getLignes()) {
            sb.append(String.format("  • %s: %d × %.2f TND = %.2f TND%n",
                l.getNomProduit(), l.getQuantite(), l.getPrixUnitaire(), l.getSousTotal()));
        }
        sb.append(String.format("%nTotal: %.2f TND", vente.getTotal()));

        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    private void handleDelete(Vente vente) {
        boolean confirmed = AlertUtils.showConfirmation(
            "Supprimer la vente",
            "Confirmation",
            "Supprimer la vente #" + vente.getIdVente() + " ? Le stock des produits sera restauré."
        );
        if (confirmed) {
            try {
                venteService.deleteVente(vente.getIdVente());
                loadData();
                AlertUtils.showSuccess("Succès", "Vente supprimée. Stock produits restauré.");
            } catch (Exception e) {
                AlertUtils.showError("Erreur", "Impossible de supprimer: " + e.getMessage());
            }
        }
    }

    private void showVenteDialog() {
        Dialog<Vente> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Vente");
        dialog.setHeaderText("Créer un bon de vente");
        dialog.getDialogPane().setPrefWidth(600);

        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));

        DatePicker datePicker = new DatePicker(LocalDate.now());
        content.getChildren().addAll(new Label("Date de la vente:"), datePicker);

        Label lignesLabel = new Label("Produits vendus:");
        lignesLabel.setStyle("-fx-font-weight: bold;");
        content.getChildren().add(lignesLabel);

        List<Produit> produits;
        try {
            produits = produitService.selectAll();
        } catch (SQLException e) {
            produits = new ArrayList<>();
        }
        final List<Produit> produitsFinal = produits;

        VBox lignesBox = new VBox(8);
        List<HBox> ligneRows = new ArrayList<>();
        lignesBox.getChildren().add(buildLigneRow(produitsFinal, ligneRows, lignesBox));

        Button addLigneBtn = new Button("➕ Ajouter un produit");
        addLigneBtn.getStyleClass().add("secondary-btn");
        addLigneBtn.setOnAction(e -> lignesBox.getChildren().add(buildLigneRow(produitsFinal, ligneRows, lignesBox)));

        content.getChildren().addAll(lignesBox, addLigneBtn);

        // ── Total bar + Currency conversion (mirrors Symfony VenteController::convert) ──
        Label totalBar = new Label("Total: 0.00 TND");
        totalBar.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #22C55E; " +
                "-fx-font-weight: bold; -fx-font-size: 15px; " +
                "-fx-background-radius: 10; -fx-padding: 10 20;");
        totalBar.setMaxWidth(Double.MAX_VALUE);

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
            totalBar.setText(String.format("Total: %.2f TND", total));
            Thread t = new Thread(() -> {
                Double converted = exchangeRateService.convert(total, "EUR", currency);
                Double rate      = exchangeRateService.getRate("EUR", currency);
                javafx.application.Platform.runLater(() -> {
                    if (converted == null) convertResult.setText("⚠️ API indisponible.");
                    else convertResult.setText(String.format("💱  %.2f EUR = %.2f %s  (taux : %.4f)", total, converted, currency, rate));
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
            if (btn == saveBtn) {
                Vente vente = new Vente();
                vente.setDateVente(datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now());

                List<LigneVente> lignes = new ArrayList<>();
                for (HBox row : ligneRows) {
                    ComboBox<Produit> combo = (ComboBox<Produit>) row.getChildren().get(0);
                    TextField qteField = (TextField) row.getChildren().get(1);
                    TextField prixField = (TextField) row.getChildren().get(2);
                    if (combo.getValue() != null && !qteField.getText().trim().isEmpty()) {
                        try {
                            LigneVente l = new LigneVente();
                            l.setIdProduit(combo.getValue().getIdProduit());
                            l.setQuantite(Integer.parseInt(qteField.getText().trim()));
                            l.setPrixUnitaire(Double.parseDouble(prixField.getText().trim()));
                            l.setNomProduit(combo.getValue().getNom());
                            lignes.add(l);
                        } catch (NumberFormatException ignored) {}
                    }
                }

                if (lignes.isEmpty()) {
                    AlertUtils.showError("Validation", "Ajoutez au moins un produit.");
                    return null;
                }
                vente.setLignes(lignes);
                return vente;
            }
            return null;
        });

        Optional<Vente> result = dialog.showAndWait();
        result.ifPresent(vente -> {
            try {
                venteService.createVente(vente);
                loadData();
                AlertUtils.showSuccess("Succès",
                    "Vente enregistrée. Stock produits mis à jour.\nTotal: " + String.format("%.2f TND", vente.getTotal()));
            } catch (RuntimeException | SQLException e) {
                AlertUtils.showError("Erreur", "Opération échouée: " + e.getMessage());
            }
        });
    }

    private HBox buildLigneRow(List<Produit> produits, List<HBox> ligneRows, VBox lignesBox) {
        ComboBox<Produit> combo = new ComboBox<>(FXCollections.observableArrayList(produits));
        combo.setPromptText("Produit...");
        combo.setPrefWidth(200);

        TextField prixField = new TextField("0");
        prixField.setPromptText("Prix/unité");
        prixField.setPrefWidth(90);
        combo.setOnAction(e -> {
            if (combo.getValue() != null)
                prixField.setText(String.valueOf(combo.getValue().getPrixVente()));
        });

        TextField qteField = new TextField("1");
        qteField.setPromptText("Qté");
        qteField.setPrefWidth(70);

        Button removeBtn = new Button("✕");
        removeBtn.getStyleClass().add("danger-btn");

        HBox row = new HBox(8, combo, qteField, prixField, removeBtn);
        ligneRows.add(row);
        removeBtn.setOnAction(e -> {
            lignesBox.getChildren().remove(row);
            ligneRows.remove(row);
        });
        return row;
    }
}
