package tn.esprit.farmai.controllers.erp;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.farmai.models.Matiere;
import tn.esprit.farmai.services.MatiereService;
import tn.esprit.farmai.utils.AlertUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Matières Premières controller — FOURNISSEUR only.
 * Full CRUD: list, add, edit, delete.
 * Mirrors Symfony erp/matiere/index.html.twig with ROLE_FOURNISSEUR.
 */
public class MatieresController extends BaseERPController {

    @FXML private TextField searchField;
    @FXML private TableView<Matiere> matieresTable;
    @FXML private TableColumn<Matiere, String> colNom;
    @FXML private TableColumn<Matiere, String> colUnite;
    @FXML private TableColumn<Matiere, String> colStock;
    @FXML private TableColumn<Matiere, String> colPrix;
    @FXML private TableColumn<Matiere, String> colSeuil;
    @FXML private TableColumn<Matiere, String> colStatut;
    @FXML private TableColumn<Matiere, Void>   colActions;
    @FXML private HBox  alertBox;
    @FXML private Label alertLabel;
    @FXML private Label totalMatieresLabel;
    @FXML private Label critiquesLabel;
    @FXML private Label valeurLabel;

    private final MatiereService matiereService = new MatiereService();
    private ObservableList<Matiere> allMatieres = FXCollections.observableArrayList();

    @Override
    protected void onInit() {
        setupColumns();
        loadData();
    }

    // ── Override sidebar navigation for fournisseur ──────────────────────────
    // Fournisseur only has: Matières + Ventes (no Produits, Services, Achats)

    @Override
    @FXML
    protected void handleDashboard() {
        var stage = getStage();
        if (stage != null)
            tn.esprit.farmai.utils.NavigationUtil.navigateTo(stage, "views/fournisseur-dashboard.fxml", "Tableau de Bord Fournisseur");
    }

    @Override
    @FXML
    protected void handleVentes() {
        var stage = getStage();
        if (stage != null)
            tn.esprit.farmai.utils.NavigationUtil.navigateTo(stage, "views/erp-ventes.fxml", "Mes Ventes");
    }

    // ── Columns ──────────────────────────────────────────────────────────────

    private void setupColumns() {
        colNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom()));
        colUnite.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUnite()));
        colStock.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.2f", c.getValue().getStock())));
        colPrix.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.2f TND", c.getValue().getPrixUnitaire())));
        colSeuil.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.2f", c.getValue().getSeuilCritique())));

        colStatut.setCellValueFactory(c -> {
            Matiere m = c.getValue();
            if (m.getStock() <= 0)        return new SimpleStringProperty("❌ Rupture");
            if (m.isStockCritique())       return new SimpleStringProperty("⚠️ Critique");
            return new SimpleStringProperty("✅ OK");
        });
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if      (item.contains("Rupture"))  setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");
                else if (item.contains("Critique")) setStyle("-fx-text-fill: #E65100; -fx-font-weight: bold;");
                else                                setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn   = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox box = new HBox(6, editBtn, deleteBtn);
            {
                editBtn.setTooltip(new Tooltip("Modifier"));
                deleteBtn.setTooltip(new Tooltip("Supprimer"));
                editBtn.setStyle("-fx-background-color: #E3F2FD; -fx-background-radius: 6; -fx-padding: 4 8; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #FFEBEE; -fx-background-radius: 6; -fx-padding: 4 8; -fx-cursor: hand;");
                box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // ── Data ─────────────────────────────────────────────────────────────────

    private void loadData() {
        try {
            allMatieres = FXCollections.observableArrayList(matiereService.selectAll());
            matieresTable.setItems(allMatieres);
            updateStats();
            checkCriticalStock();
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les matières: " + e.getMessage());
        }
    }

    private void updateStats() {
        int total = allMatieres.size();
        long critiques = allMatieres.stream().filter(Matiere::isStockCritique).count();
        double valeur = allMatieres.stream()
            .mapToDouble(m -> m.getStock() * m.getPrixUnitaire()).sum();

        if (totalMatieresLabel != null) totalMatieresLabel.setText(String.valueOf(total));
        if (critiquesLabel != null)     critiquesLabel.setText(String.valueOf(critiques));
        if (valeurLabel != null)        valeurLabel.setText(String.format("%.0f TND", valeur));
    }

    private void checkCriticalStock() {
        try {
            List<Matiere> critiques = matiereService.findStockCritique();
            if (!critiques.isEmpty()) {
                String names = critiques.stream().map(Matiere::getNom).collect(Collectors.joining(", "));
                alertLabel.setText("Stock critique pour: " + names);
                alertBox.setVisible(true);
                alertBox.setManaged(true);
            } else {
                alertBox.setVisible(false);
                alertBox.setManaged(false);
            }
        } catch (SQLException ignored) {}
    }

    @FXML
    private void handleSearch() {
        String q = searchField.getText().toLowerCase().trim();
        matieresTable.setItems(q.isEmpty() ? allMatieres :
            allMatieres.filtered(m ->
                m.getNom().toLowerCase().contains(q) ||
                (m.getDescription() != null && m.getDescription().toLowerCase().contains(q))));
    }

    @FXML
    private void handleAdd() {
        showMatiereForm(null);
    }

    private void handleEdit(Matiere matiere) {
        showMatiereForm(matiere);
    }

    private void handleDelete(Matiere matiere) {
        boolean confirmed = AlertUtils.showConfirmation(
            "Supprimer la matière", "Confirmation",
            "Supprimer \"" + matiere.getNom() + "\" ?\n" +
            "Impossible si elle est utilisée dans une recette ou un achat.");
        if (confirmed) {
            try {
                matiereService.deleteOne(matiere.getIdMatiere());
                loadData();
                AlertUtils.showSuccess("Succès", "Matière supprimée.");
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Impossible de supprimer: " + e.getMessage());
            }
        }
    }

    // ── Form dialog — mirrors Symfony erp/matiere/form.html.twig ─────────────

    private void showMatiereForm(Matiere existing) {
        boolean isEdit = existing != null;

        Dialog<Matiere> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier la matière" : "Nouvelle matière");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setPrefWidth(520);
        dialog.getDialogPane().setPrefHeight(500);

        ButtonType saveBtn = new ButtonType(
            isEdit ? "💾  Enregistrer les modifications" : "✅  Créer la matière",
            ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        // ── Form layout ──
        VBox form = new VBox(16);
        form.setPadding(new Insets(24));

        // Title inside dialog
        Label title = new Label(isEdit ? "✏️  Modifier \"" + existing.getNom() + "\"" : "🧪  Nouvelle Matière Première");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #263238;");
        form.getChildren().add(title);

        Separator sep = new Separator();
        form.getChildren().add(sep);

        // Nom
        form.getChildren().add(makeLabel("Nom *"));
        TextField nomField = new TextField(isEdit ? existing.getNom() : "");
        nomField.setPromptText("Ex: Engrais NPK, Semences Blé...");
        nomField.setStyle("-fx-pref-height: 36px;");
        form.getChildren().add(nomField);

        // Description
        form.getChildren().add(makeLabel("Description"));
        TextArea descField = new TextArea(isEdit && existing.getDescription() != null ? existing.getDescription() : "");
        descField.setPromptText("Description optionnelle de la matière...");
        descField.setPrefRowCount(2);
        descField.setWrapText(true);
        form.getChildren().add(descField);

        // Unité + Stock (side by side)
        HBox row1 = new HBox(16);
        VBox uniteBox = new VBox(6);
        uniteBox.getChildren().add(makeLabel("Unité *"));
        TextField uniteField = new TextField(isEdit ? existing.getUnite() : "kg");
        uniteField.setPromptText("kg, L, unité, sac...");
        uniteField.setStyle("-fx-pref-height: 36px;");
        uniteBox.getChildren().add(uniteField);
        HBox.setHgrow(uniteBox, Priority.ALWAYS);

        VBox stockBox = new VBox(6);
        stockBox.getChildren().add(makeLabel("Stock initial"));
        TextField stockField = new TextField(isEdit ? String.valueOf(existing.getStock()) : "0");
        stockField.setPromptText("0");
        stockField.setStyle("-fx-pref-height: 36px;");
        stockBox.getChildren().add(stockField);
        HBox.setHgrow(stockBox, Priority.ALWAYS);

        row1.getChildren().addAll(uniteBox, stockBox);
        form.getChildren().add(row1);

        // Prix + Seuil (side by side)
        HBox row2 = new HBox(16);
        VBox prixBox = new VBox(6);
        prixBox.getChildren().add(makeLabel("Prix unitaire (TND) *"));
        TextField prixField = new TextField(isEdit ? String.valueOf(existing.getPrixUnitaire()) : "0.00");
        prixField.setPromptText("0.00");
        prixField.setStyle("-fx-pref-height: 36px;");
        prixBox.getChildren().add(prixField);
        HBox.setHgrow(prixBox, Priority.ALWAYS);

        VBox seuilBox = new VBox(6);
        seuilBox.getChildren().add(makeLabel("Seuil critique"));
        TextField seuilField = new TextField(isEdit ? String.valueOf(existing.getSeuilCritique()) : "0");
        seuilField.setPromptText("0 = désactivé");
        seuilField.setStyle("-fx-pref-height: 36px;");
        seuilBox.getChildren().add(seuilField);
        HBox.setHgrow(seuilBox, Priority.ALWAYS);

        row2.getChildren().addAll(prixBox, seuilBox);
        form.getChildren().add(row2);

        // Help text
        Label help = new Label("💡 Le seuil critique déclenche une alerte quand le stock descend en dessous de cette valeur.");
        help.setStyle("-fx-text-fill: #78909C; -fx-font-size: 11px;");
        help.setWrapText(true);
        form.getChildren().add(help);

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        dialog.getDialogPane().setContent(scroll);

        // Focus nom field
        javafx.application.Platform.runLater(nomField::requestFocus);

        dialog.setResultConverter(btn -> {
            if (btn != saveBtn) return null;

            // Validation
            if (nomField.getText().trim().isEmpty()) {
                AlertUtils.showError("Validation", "Le nom de la matière est obligatoire.");
                return null;
            }
            if (uniteField.getText().trim().isEmpty()) {
                AlertUtils.showError("Validation", "L'unité est obligatoire (ex: kg, L, unité).");
                return null;
            }

            double stock, prix, seuil;
            try {
                stock = Double.parseDouble(stockField.getText().trim().replace(",", "."));
                prix  = Double.parseDouble(prixField.getText().trim().replace(",", "."));
                seuil = Double.parseDouble(seuilField.getText().trim().replace(",", "."));
            } catch (NumberFormatException ex) {
                AlertUtils.showError("Validation", "Stock, prix et seuil doivent être des nombres valides.\nEx: 100, 2.50, 10");
                return null;
            }
            if (prix < 0) {
                AlertUtils.showError("Validation", "Le prix ne peut pas être négatif.");
                return null;
            }

            Matiere m = isEdit ? existing : new Matiere();
            m.setNom(nomField.getText().trim());
            m.setDescription(descField.getText().trim().isEmpty() ? null : descField.getText().trim());
            m.setUnite(uniteField.getText().trim());
            m.setStock(stock);
            m.setPrixUnitaire(prix);
            m.setSeuilCritique(seuil);
            return m;
        });

        Optional<Matiere> result = dialog.showAndWait();
        result.ifPresent(m -> {
            try {
                if (isEdit) {
                    matiereService.updateOne(m);
                    AlertUtils.showSuccess("Succès", "Matière \"" + m.getNom() + "\" mise à jour.");
                } else {
                    matiereService.insertOne(m);
                    AlertUtils.showSuccess("Succès", "Matière \"" + m.getNom() + "\" créée avec succès.");
                }
                loadData();
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Opération échouée: " + e.getMessage());
            }
        });
    }

    private Label makeLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #455A64;");
        return l;
    }
}
