package tn.esprit.farmai.controllers.erp;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import tn.esprit.farmai.models.Achat;
import tn.esprit.farmai.models.LigneAchat;
import tn.esprit.farmai.models.Matiere;
import tn.esprit.farmai.services.AchatService;
import tn.esprit.farmai.services.ExchangeRateService;
import tn.esprit.farmai.services.MatiereService;
import tn.esprit.farmai.services.StripeService;
import tn.esprit.farmai.utils.AlertUtils;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Achats controller for AGRICOLE role.
 * Features:
 *  - Compact icon-only action buttons (no overflow)
 *  - Currency conversion (EUR → TND / other) via open.er-api.com
 *  - Stripe payment: opens browser + shows QR code of checkout URL
 *  - QR code generated with ZXing (mirrors Symfony PaymentService.generateQrCode)
 */
public class AgricoleAchatsController extends AgricoleBaseERPController {

    @FXML private TableView<Achat> achatsTable;
    @FXML private TableColumn<Achat, String> colId;
    @FXML private TableColumn<Achat, String> colDate;
    @FXML private TableColumn<Achat, String> colTotal;
    @FXML private TableColumn<Achat, String> colLignes;
    @FXML private TableColumn<Achat, String> colPaid;
    @FXML private TableColumn<Achat, Void>   colActions;
    @FXML private Label totalAchatsLabel;
    @FXML private Label totalMontantLabel;
    @FXML private Label totalPayesLabel;

    private final AchatService        achatService        = new AchatService();
    private final MatiereService      matiereService      = new MatiereService();
    private final ExchangeRateService exchangeRateService = new ExchangeRateService();
    private final StripeService       stripeService       = new StripeService();

    private ObservableList<Achat> allAchats = FXCollections.observableArrayList();

    @Override
    protected void onInit() {
        setupColumns();
        loadData();
    }

    // ── Table columns ────────────────────────────────────────────────────────

    private void setupColumns() {
        colId.setCellValueFactory(c -> new SimpleStringProperty("#" + c.getValue().getIdAchat()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDateAchat().toString()));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(
                String.format("%.2f TND", c.getValue().getTotal())));
        colLignes.setCellValueFactory(c -> {
            String mats = c.getValue().getLignes().stream()
                    .map(l -> l.getNomMatiere() + " ×" + String.format("%.1f", l.getQuantite()))
                    .collect(java.util.stream.Collectors.joining(", "));
            return new SimpleStringProperty(mats.isEmpty()
                    ? c.getValue().getLignes().size() + " ligne(s)" : mats);
        });

        colPaid.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().isPaid() ? "✅ Payé" : "⏳ En attente"));
        colPaid.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle(item.contains("Payé")
                        ? "-fx-text-fill: #2E7D32; -fx-font-weight: bold;"
                        : "-fx-text-fill: #F57C00; -fx-font-weight: bold;");
            }
        });

        // ── Compact icon-only action buttons ──────────────────────────────
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button detailBtn = iconBtn("👁️", "Détails",    "#1565C0", "#E3F2FD");
            private final Button payBtn    = iconBtn("💳", "Payer",      "#4A148C", "#EDE7F6");
            private final Button deleteBtn = iconBtn("🗑️", "Supprimer",  "#B71C1C", "#FFEBEE");
            private final HBox   box       = new HBox(4, detailBtn, payBtn, deleteBtn);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                detailBtn.setOnAction(e -> handleDetails(getTableView().getItems().get(getIndex())));
                payBtn.setOnAction(e    -> handlePay(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Achat a = getTableView().getItems().get(getIndex());
                payBtn.setVisible(!a.isPaid());
                payBtn.setManaged(!a.isPaid());
                setGraphic(box);
            }
        });
    }

    /** Creates a compact square icon button with tooltip */
    private Button iconBtn(String emoji, String tip, String fg, String bg) {
        Button b = new Button(emoji);
        b.setTooltip(new Tooltip(tip));
        b.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: %s; " +
                "-fx-font-size: 13px; -fx-padding: 4 7; " +
                "-fx-background-radius: 6; -fx-cursor: hand;", bg, fg));
        b.setMinWidth(30); b.setMaxWidth(30);
        return b;
    }

    // ── Data ─────────────────────────────────────────────────────────────────

    private void loadData() {
        try {
            allAchats = FXCollections.observableArrayList(achatService.selectAll());
            achatsTable.setItems(allAchats);
            updateStats();
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les achats: " + e.getMessage());
        }
    }

    private void updateStats() {
        int total    = allAchats.size();
        double montant = allAchats.stream().mapToDouble(Achat::getTotal).sum();
        long payes   = allAchats.stream().filter(Achat::isPaid).count();
        if (totalAchatsLabel  != null) totalAchatsLabel.setText(String.valueOf(total));
        if (totalMontantLabel != null) totalMontantLabel.setText(String.format("%.2f TND", montant));
        if (totalPayesLabel   != null) totalPayesLabel.setText(String.valueOf(payes));
    }

    @FXML private void handleAdd() { showAchatDialog(); }

    // ── Detail dialog ─────────────────────────────────────────────────────────

    private void handleDetails(Achat achat) {
        StringBuilder sb = new StringBuilder();
        for (LigneAchat l : achat.getLignes())
            sb.append(String.format("  • %s: %.2f %s × %.2f TND = %.2f TND%n",
                    l.getNomMatiere(), l.getQuantite(), l.getUniteMatiere(),
                    l.getPrixUnitaire(), l.getSousTotal()));
        sb.append(String.format("%nTotal: %.2f TND%nStatut: %s",
                achat.getTotal(), achat.isPaid() ? "✅ Payé" : "⏳ En attente"));
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Achat #" + achat.getIdAchat());
        a.setHeaderText("Bon de commande du " + achat.getDateAchat());
        a.setContentText(sb.toString());
        a.showAndWait();
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    private void handleDelete(Achat achat) {
        boolean ok = AlertUtils.showConfirmation("Supprimer l'achat", "Confirmation",
                "Supprimer l'achat #" + achat.getIdAchat() + " ?" +
                (achat.isPaid() ? " Le stock sera restauré." : ""));
        if (ok) {
            try {
                achatService.deleteAchat(achat.getIdAchat());
                loadData();
                AlertUtils.showSuccess("Succès", "Achat supprimé." +
                        (achat.isPaid() ? " Stock matières restauré." : ""));
            } catch (Exception e) {
                AlertUtils.showError("Erreur", e.getMessage());
            }
        }
    }

    // ── Stripe payment dialog ─────────────────────────────────────────────────

    private void handlePay(Achat achat) {
        String successUrl = "https://farmia.app/payment/success?achat=" + achat.getIdAchat();
        String cancelUrl  = "https://farmia.app/payment/cancel?achat=" + achat.getIdAchat();

        Thread bg = new Thread(() -> {
            String stripeUrl;
            String errorMsg = null;
            try {
                stripeUrl = stripeService.createCheckoutSession(
                        achat.getTotal(), achat.getIdAchat(), successUrl, cancelUrl);
            } catch (Exception e) {
                stripeUrl = null;
                errorMsg = e.getMessage();
            }

            final String finalUrl = stripeUrl;
            final String finalErr = errorMsg;

            javafx.application.Platform.runLater(() -> {
                if (finalUrl == null) {
                    AlertUtils.showError("Stripe", "Impossible de créer la session:\n" + finalErr);
                    return;
                }
                Image qrImage = stripeService.generateQrCodeImage(finalUrl, 200);
                showStripeDialog(achat, finalUrl, qrImage);
            });
        });
        bg.setDaemon(true);
        bg.start();
    }

    private void showStripeDialog(Achat achat, String stripeUrl, Image qrImage) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("💳 Paiement — Achat #" + achat.getIdAchat());
        dialog.setHeaderText(null);
        dialog.getDialogPane().setPrefWidth(480);

        ButtonType payNowBtn = new ButtonType("✅ Marquer comme payé", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(payNowBtn, cancelBtn);

        VBox content = new VBox(14);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        // Header
        Label title = new Label("Paiement sécurisé Stripe");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#635bff"));

        // Amount box
        VBox amountBox = new VBox(4);
        amountBox.setAlignment(Pos.CENTER);
        amountBox.setStyle("-fx-background-color: #F3F0FF; -fx-background-radius: 12; -fx-padding: 14 24;");
        Label amtLabel = new Label("Montant à payer");
        amtLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px;");
        Label amtValue = new Label(String.format("%.2f TND", achat.getTotal()));
        amtValue.setFont(Font.font("System", FontWeight.BOLD, 26));
        amtValue.setTextFill(Color.web("#0F172A"));
        amountBox.getChildren().addAll(amtLabel, amtValue);

        // QR code
        VBox qrBox = new VBox(8);
        qrBox.setAlignment(Pos.CENTER);
        Label qrLabel = new Label("📱 Scannez pour ouvrir la page de paiement Stripe");
        qrLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px; -fx-font-weight: bold;");
        if (qrImage != null) {
            ImageView qrView = new ImageView(qrImage);
            qrView.setFitWidth(200); qrView.setFitHeight(200);
            qrView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);");
            Label testCard = new Label("🧪 Carte test: 4242 4242 4242 4242  |  12/34  |  CVC: 123");
            testCard.setStyle("-fx-text-fill: #635bff; -fx-font-size: 11px; -fx-font-weight: bold; " +
                    "-fx-background-color: #F3F0FF; -fx-background-radius: 8; -fx-padding: 6 12;");
            qrBox.getChildren().addAll(qrLabel, qrView, testCard);
        } else {
            qrBox.getChildren().add(new Label("⚠️ QR code indisponible"));
        }

        // Open browser button
        Button openBrowserBtn = new Button("🔗  Ouvrir Stripe dans le navigateur");
        openBrowserBtn.setStyle(
                "-fx-background-color: #635bff; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 13px; " +
                "-fx-background-radius: 10; -fx-padding: 10 20; -fx-cursor: hand;");
        openBrowserBtn.setMaxWidth(Double.MAX_VALUE);
        openBrowserBtn.setOnAction(e -> {
            boolean opened = stripeService.openInBrowser(stripeUrl);
            if (!opened)
                AlertUtils.showError("Navigateur",
                        "Impossible d'ouvrir le navigateur.\nURL: " + stripeUrl);
        });

        // Security note
        Label secNote = new Label("🔒 Paiement sécurisé. Vos données ne sont jamais stockées.");
        secNote.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 11px;");
        secNote.setWrapText(true);

        content.getChildren().addAll(title, amountBox, qrBox, openBrowserBtn, secNote);
        dialog.getDialogPane().setContent(content);

        // Style confirm button green
        javafx.application.Platform.runLater(() -> {
            javafx.scene.Node okNode = dialog.getDialogPane().lookupButton(payNowBtn);
            if (okNode != null)
                okNode.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-background-radius: 8;");
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == payNowBtn) {
            boolean confirmed = AlertUtils.showConfirmation(
                    "Confirmer le paiement",
                    "Achat #" + achat.getIdAchat(),
                    "Confirmer le paiement de " + String.format("%.2f TND", achat.getTotal()) +
                    " ?\nLe stock des matières sera mis à jour.");
            if (confirmed) {
                try {
                    achatService.markAsPaid(achat.getIdAchat());
                    loadData();
                    showPaymentSuccess(achat);
                } catch (Exception e) {
                    AlertUtils.showError("Erreur", e.getMessage());
                }
            }
        }
    }

    /** Payment success screen — mirrors Symfony payment_success.html.twig */
    private void showPaymentSuccess(Achat achat) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("🎉 Paiement confirmé !");
        alert.setHeaderText("✅ Achat #" + achat.getIdAchat() + " réglé avec succès");

        StringBuilder sb = new StringBuilder("📋 Récapitulatif de commande\n");
        sb.append("─".repeat(40)).append("\n");
        for (LigneAchat l : achat.getLignes())
            sb.append(String.format("  • %s × %.1f %s  →  %.2f TND%n",
                    l.getNomMatiere(), l.getQuantite(), l.getUniteMatiere(), l.getSousTotal()));
        sb.append("─".repeat(40)).append("\n");
        sb.append(String.format("  Total payé : %.2f TND%n", achat.getTotal()));
        sb.append("\n✅ Stock matières mis à jour.");
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    // ── New achat dialog ──────────────────────────────────────────────────────

    /**
     * Create new achat dialog with currency conversion widget.
     * Mirrors Symfony new.html.twig convert-box section.
     */
    private void showAchatDialog() {
        Dialog<Achat> dialog = new Dialog<>();
        dialog.setTitle("Nouvel Achat");
        dialog.setHeaderText("Commander des matières premières");
        dialog.getDialogPane().setPrefWidth(640);

        ButtonType saveBtn = new ButtonType("💾 Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));

        // Date
        DatePicker datePicker = new DatePicker(LocalDate.now());
        content.getChildren().addAll(boldLabel("Date de la commande:"), datePicker);

        // Lines
        content.getChildren().add(boldLabel("Matières à commander:"));
        List<Matiere> matieres;
        try { matieres = matiereService.selectAll(); }
        catch (SQLException e) { matieres = new ArrayList<>(); }
        final List<Matiere> matieresFinal = matieres;

        VBox lignesBox = new VBox(8);
        List<HBox> ligneRows = new ArrayList<>();

        // ── Total bar (declared early so updateTotal can reference it) ──
        Label totalBar = new Label("Total: 0.00 TND");
        totalBar.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #22C55E; " +
                "-fx-font-weight: bold; -fx-font-size: 16px; " +
                "-fx-background-radius: 10; -fx-padding: 10 20;");
        totalBar.setMaxWidth(Double.MAX_VALUE);

        // ── Convert result label (declared early so convert button can reference it) ──
        Label convertResult = new Label();
        convertResult.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");
        convertResult.setWrapText(true);

        // Live total updater — called whenever qty/price fields change
        Runnable updateTotal = () -> {
            double t = computeTotal(ligneRows);
            totalBar.setText(String.format("Total: %.2f TND", t));
            convertResult.setText("");
        };

        lignesBox.getChildren().add(buildLigneRow(matieresFinal, ligneRows, lignesBox, updateTotal));

        Button addBtn = new Button("➕ Ajouter une matière");
        addBtn.getStyleClass().add("secondary-btn");
        addBtn.setOnAction(e -> lignesBox.getChildren().add(
                buildLigneRow(matieresFinal, ligneRows, lignesBox, updateTotal)));
        content.getChildren().addAll(lignesBox, addBtn, totalBar);

        // ── Currency conversion box (mirrors Symfony convert-box) ──
        VBox convertBox = new VBox(8);
        convertBox.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 12; " +
                "-fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-padding: 14;");
        Label convertTitle = new Label("💱  Conversion de devise");
        convertTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #475569;");
        Label convertSub = new Label("Convertissez le total dans une autre devise");
        convertSub.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px;");

        ComboBox<String> currencyCombo = new ComboBox<>();
        currencyCombo.getItems().addAll("TND", "USD", "GBP", "JPY", "CHF", "CAD", "MAD", "DZD", "SAR", "AED");
        currencyCombo.setValue("TND");
        currencyCombo.setPrefWidth(100);

        Button convertBtn = new Button("🔄 Convertir");
        convertBtn.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 6 14;");

        HBox convertRow = new HBox(10, currencyCombo, convertBtn);
        convertRow.setAlignment(Pos.CENTER_LEFT);
        convertBox.getChildren().addAll(convertTitle, convertSub, convertRow, convertResult);
        content.getChildren().add(convertBox);

        // Wire up convert button — runs on background thread, updates UI on FX thread
        convertBtn.setOnAction(e -> {
            double total = computeTotal(ligneRows);
            String currency = currencyCombo.getValue();
            convertResult.setText("⏳ Conversion en cours…");
            Thread t = new Thread(() -> {
                Double converted = exchangeRateService.convert(total, "EUR", currency);
                Double rate      = exchangeRateService.getRate("EUR", currency);
                javafx.application.Platform.runLater(() -> {
                    if (converted == null)
                        convertResult.setText("⚠️ API indisponible. Vérifiez votre connexion.");
                    else
                        convertResult.setText(String.format(
                                "💱  %.2f EUR = %.2f %s  (taux : %.4f)", total, converted, currency, rate));
                });
            });
            t.setDaemon(true);
            t.start();
        });

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(520);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        dialog.getDialogPane().setContent(scroll);

        dialog.setResultConverter(btn -> {
            if (btn != saveBtn) return null;
            Achat achat = new Achat();
            achat.setDateAchat(datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now());
            List<LigneAchat> lignes = new ArrayList<>();
            for (HBox row : ligneRows) {
                ComboBox<Matiere> combo = (ComboBox<Matiere>) row.getChildren().get(0);
                TextField qteField  = (TextField) row.getChildren().get(1);
                TextField prixField = (TextField) row.getChildren().get(2);
                if (combo.getValue() == null || qteField.getText().trim().isEmpty()) continue;
                try {
                    double qte  = Double.parseDouble(qteField.getText().trim());
                    double prix = prixField.getText().trim().isEmpty() ? 0
                            : Double.parseDouble(prixField.getText().trim());
                    if (qte <= 0) {
                        AlertUtils.showError("Validation", "La quantité doit être > 0.");
                        return null;
                    }
                    LigneAchat l = new LigneAchat();
                    l.setIdMatiere(combo.getValue().getIdMatiere());
                    l.setQuantite(qte);
                    l.setPrixUnitaire(prix);
                    l.setNomMatiere(combo.getValue().getNom());
                    lignes.add(l);
                } catch (NumberFormatException ex) {
                    AlertUtils.showError("Validation", "Quantité et prix doivent être des nombres.");
                    return null;
                }
            }
            if (lignes.isEmpty()) {
                AlertUtils.showError("Validation", "Ajoutez au moins une matière.");
                return null;
            }
            achat.setLignes(lignes);
            return achat;
        });

        Optional<Achat> result = dialog.showAndWait();
        result.ifPresent(achat -> {
            try {
                achatService.createAchat(achat);
                loadData();
                AlertUtils.showSuccess("Commande enregistrée",
                        "Achat enregistré avec succès.\nTotal: " +
                        String.format("%.2f TND", achat.getTotal()) +
                        "\nCliquez sur '💳' pour payer et recevoir les matières.");
            } catch (Exception e) {
                AlertUtils.showError("Erreur", e.getMessage());
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private double computeTotal(List<HBox> rows) {
        double total = 0;
        for (HBox row : rows) {
            try {
                TextField qte  = (TextField) row.getChildren().get(1);
                TextField prix = (TextField) row.getChildren().get(2);
                double q = Double.parseDouble(qte.getText().trim().isEmpty() ? "0" : qte.getText().trim());
                double p = Double.parseDouble(prix.getText().trim().isEmpty() ? "0" : prix.getText().trim());
                total += q * p;
            } catch (NumberFormatException ignored) {}
        }
        return total;
    }

    private HBox buildLigneRow(List<Matiere> matieres, List<HBox> rows, VBox box, Runnable updateTotal) {
        ComboBox<Matiere> combo = new ComboBox<>(FXCollections.observableArrayList(matieres));
        combo.setPromptText("Matière..."); combo.setPrefWidth(200);

        TextField prixField = new TextField("0");
        prixField.setPromptText("Prix/unité"); prixField.setPrefWidth(90);

        TextField qteField = new TextField("1");
        qteField.setPromptText("Qté"); qteField.setPrefWidth(70);

        // Auto-fill price + trigger total update when matière selected
        combo.setOnAction(e -> {
            if (combo.getValue() != null)
                prixField.setText(String.valueOf(combo.getValue().getPrixUnitaire()));
            updateTotal.run();
        });
        // Live total update on typing
        qteField.textProperty().addListener((obs, o, n) -> updateTotal.run());
        prixField.textProperty().addListener((obs, o, n) -> updateTotal.run());

        Button removeBtn = iconBtn("✕", "Supprimer", "#B71C1C", "#FFEBEE");

        // Order: combo[0], qte[1], prix[2], remove[3]
        HBox row = new HBox(8, combo, qteField, prixField, removeBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        rows.add(row);
        removeBtn.setOnAction(e -> {
            box.getChildren().remove(row);
            rows.remove(row);
            updateTotal.run();
        });
        return row;
    }

    private Label boldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        return l;
    }
}
