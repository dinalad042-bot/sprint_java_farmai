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
import tn.esprit.farmai.services.MatiereService;
import tn.esprit.farmai.utils.AlertUtils;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for ERP Achats (purchase orders) view.
 * Create, view details, and delete purchase orders with automatic stock update.
 */
public class AchatsController extends BaseERPController {

    @FXML private TableView<Achat> achatsTable;
    @FXML private TableColumn<Achat, String> colId;
    @FXML private TableColumn<Achat, String> colDate;
    @FXML private TableColumn<Achat, String> colTotal;
    @FXML private TableColumn<Achat, String> colLignes;
    @FXML private TableColumn<Achat, String> colPaid;
    @FXML private TableColumn<Achat, Void> colActions;
    @FXML private Label totalAchatsLabel;
    @FXML private Label totalMontantLabel;
    @FXML private Label totalPayesLabel;

    private final AchatService achatService = new AchatService();
    private final MatiereService matiereService = new MatiereService();
    private ObservableList<Achat> allAchats = FXCollections.observableArrayList();

    @Override
    protected void onInit() {
        setupColumns();
        loadData();
    }

    private void setupColumns() {
        colId.setCellValueFactory(c -> new SimpleStringProperty("#" + c.getValue().getIdAchat()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDateAchat().toString()));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.2f TND", c.getValue().getTotal())));
        colLignes.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLignes().size() + " ligne(s)"));
        colPaid.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isPaid() ? "✅ Payé" : "⏳ En attente"));
        colPaid.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if (item.contains("Payé")) setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                else setStyle("-fx-text-fill: #F57C00; -fx-font-weight: bold;");
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button detailBtn = new Button("👁️");
            private final Button payBtn    = new Button("💳");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox box = new HBox(5, detailBtn, payBtn, deleteBtn);

            {
                detailBtn.setTooltip(new Tooltip("Détails"));
                payBtn.setTooltip(new Tooltip("Payer"));
                deleteBtn.setTooltip(new Tooltip("Supprimer"));
                detailBtn.setStyle("-fx-background-color: #E3F2FD; -fx-background-radius: 6; -fx-padding: 4 8; -fx-cursor: hand;");
                payBtn.setStyle("-fx-background-color: #EDE7F6; -fx-background-radius: 6; -fx-padding: 4 8; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #FFEBEE; -fx-background-radius: 6; -fx-padding: 4 8; -fx-cursor: hand;");
                box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                detailBtn.setOnAction(e -> handleDetails(getTableView().getItems().get(getIndex())));
                payBtn.setOnAction(e -> handlePay(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Achat a = getTableView().getItems().get(getIndex());
                payBtn.setVisible(!a.isPaid());
                payBtn.setManaged(!a.isPaid());
                setGraphic(box);
            }
        });
    }

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
        int total = allAchats.size();
        double montant = allAchats.stream().mapToDouble(Achat::getTotal).sum();
        long payes = allAchats.stream().filter(Achat::isPaid).count();

        if (totalAchatsLabel != null) totalAchatsLabel.setText(String.valueOf(total));
        if (totalMontantLabel != null) totalMontantLabel.setText(String.format("%.2f TND", montant));
        if (totalPayesLabel != null) totalPayesLabel.setText(String.valueOf(payes));
    }

    @FXML
    private void handleAdd() {
        showAchatDialog();
    }

    private void handleDetails(Achat achat) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de l'achat #" + achat.getIdAchat());
        alert.setHeaderText("Bon de commande du " + achat.getDateAchat());

        StringBuilder sb = new StringBuilder();
        sb.append("Lignes:\n");
        for (LigneAchat l : achat.getLignes()) {
            sb.append(String.format("  • %s: %.2f %s × %.2f TND = %.2f TND%n",
                l.getNomMatiere(), l.getQuantite(), l.getUniteMatiere(),
                l.getPrixUnitaire(), l.getSousTotal()));
        }
        sb.append(String.format("%nTotal: %.2f TND", achat.getTotal()));
        sb.append("\nStatut: ").append(achat.isPaid() ? "Payé" : "En attente");

        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    private void handleDelete(Achat achat) {
        boolean confirmed = AlertUtils.showConfirmation(
            "Supprimer l'achat",
            "Confirmation",
            "Supprimer l'achat #" + achat.getIdAchat() + " ?" +
            (achat.isPaid() ? " Le stock des matières sera restauré." : "")
        );
        if (confirmed) {
            try {
                achatService.deleteAchat(achat.getIdAchat());
                loadData();
                AlertUtils.showSuccess("Succès", "Achat supprimé." +
                    (achat.isPaid() ? " Stock matières restauré." : ""));
            } catch (Exception e) {
                AlertUtils.showError("Erreur", "Impossible de supprimer: " + e.getMessage());
            }
        }
    }

    private void handlePay(Achat achat) {
        boolean confirmed = AlertUtils.showConfirmation(
            "Confirmer le paiement",
            "Paiement de l'achat #" + achat.getIdAchat(),
            "Confirmer le paiement de " + String.format("%.2f TND", achat.getTotal()) +
            " ?\nLe stock des matières sera mis à jour."
        );
        if (confirmed) {
            try {
                achatService.markAsPaid(achat.getIdAchat());
                loadData();
                AlertUtils.showSuccess("Paiement confirmé",
                    "Achat #" + achat.getIdAchat() + " marqué comme payé.\nStock matières mis à jour.");
            } catch (Exception e) {
                AlertUtils.showError("Erreur", "Impossible de confirmer le paiement: " + e.getMessage());
            }
        }
    }

    private void showAchatDialog() {
        Dialog<Achat> dialog = new Dialog<>();
        dialog.setTitle("Nouvel Achat");
        dialog.setHeaderText("Créer un bon de commande");
        dialog.getDialogPane().setPrefWidth(600);

        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));

        DatePicker datePicker = new DatePicker(LocalDate.now());
        content.getChildren().addAll(new Label("Date de l'achat:"), datePicker);

        Label lignesLabel = new Label("Lignes de commande:");
        lignesLabel.setStyle("-fx-font-weight: bold;");
        content.getChildren().add(lignesLabel);

        List<Matiere> matieres;
        try {
            matieres = matiereService.selectAll();
        } catch (SQLException e) {
            matieres = new ArrayList<>();
        }
        final List<Matiere> matieresFinal = matieres;

        VBox lignesBox = new VBox(8);
        List<HBox> ligneRows = new ArrayList<>();

        // Add first row by default
        lignesBox.getChildren().add(buildLigneRow(matieresFinal, ligneRows, lignesBox));

        Button addLigneBtn = new Button("➕ Ajouter une ligne");
        addLigneBtn.getStyleClass().add("secondary-btn");
        addLigneBtn.setOnAction(e -> lignesBox.getChildren().add(buildLigneRow(matieresFinal, ligneRows, lignesBox)));

        content.getChildren().addAll(lignesBox, addLigneBtn);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                Achat achat = new Achat();
                achat.setDateAchat(datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now());

                List<LigneAchat> lignes = new ArrayList<>();
                for (HBox row : ligneRows) {
                    ComboBox<Matiere> combo = (ComboBox<Matiere>) row.getChildren().get(0);
                    TextField qteField = (TextField) row.getChildren().get(1);
                    TextField prixField = (TextField) row.getChildren().get(2);
                    if (combo.getValue() != null && !qteField.getText().trim().isEmpty()) {
                        try {
                            LigneAchat l = new LigneAchat();
                            l.setIdMatiere(combo.getValue().getIdMatiere());
                            l.setQuantite(Double.parseDouble(qteField.getText().trim()));
                            l.setPrixUnitaire(Double.parseDouble(prixField.getText().trim()));
                            l.setNomMatiere(combo.getValue().getNom());
                            lignes.add(l);
                        } catch (NumberFormatException ignored) {}
                    }
                }

                if (lignes.isEmpty()) {
                    AlertUtils.showError("Validation", "Ajoutez au moins une ligne de commande.");
                    return null;
                }
                achat.setLignes(lignes);
                return achat;
            }
            return null;
        });

        Optional<Achat> result = dialog.showAndWait();
        result.ifPresent(achat -> {
            try {
                achatService.createAchat(achat);
                loadData();
                AlertUtils.showSuccess("Commande enregistrée",
                    "Achat enregistré avec succès.\nTotal: " + String.format("%.2f TND", achat.getTotal()) +
                    "\nCliquez sur '💳 Payer' pour confirmer le paiement et recevoir les matières.");
            } catch (Exception e) {
                AlertUtils.showError("Erreur", "Opération échouée: " + e.getMessage());
            }
        });
    }

    private HBox buildLigneRow(List<Matiere> matieres, List<HBox> ligneRows, VBox lignesBox) {
        ComboBox<Matiere> combo = new ComboBox<>(FXCollections.observableArrayList(matieres));
        combo.setPromptText("Matière...");
        combo.setPrefWidth(200);

        // Auto-fill price when matiere selected
        TextField prixField = new TextField("0");
        prixField.setPromptText("Prix/unité");
        prixField.setPrefWidth(90);
        combo.setOnAction(e -> {
            if (combo.getValue() != null)
                prixField.setText(String.valueOf(combo.getValue().getPrixUnitaire()));
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
