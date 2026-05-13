package tn.esprit.farmai.controllers.erp;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.farmai.models.Matiere;
import tn.esprit.farmai.models.Produit;
import tn.esprit.farmai.models.RecetteIngredient;
import tn.esprit.farmai.services.MatiereService;
import tn.esprit.farmai.services.ProduitService;
import tn.esprit.farmai.utils.AlertUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Produits Finis controller for AGRICOLE role.
 * Full CRUD + production (same as ProduitsController but with agricole navigation).
 */
public class AgricoleProduitsController extends AgricoleBaseERPController {

    @FXML private TextField searchField;
    @FXML private TableView<Produit> produitsTable;
    @FXML private TableColumn<Produit, String> colNom;
    @FXML private TableColumn<Produit, String> colPrix;
    @FXML private TableColumn<Produit, String> colStock;
    @FXML private TableColumn<Produit, String> colQteProduite;
    @FXML private TableColumn<Produit, String> colType;
    @FXML private TableColumn<Produit, String> colRecette;
    @FXML private TableColumn<Produit, Void>   colActions;
    @FXML private Label totalProduitsLabel;
    @FXML private Label enStockLabel;
    @FXML private Label valeurLabel;

    private final ProduitService produitService = new ProduitService();
    private final MatiereService matiereService = new MatiereService();
    private ObservableList<Produit> allProduits = FXCollections.observableArrayList();

    @Override
    protected void onInit() {
        setupColumns();
        loadData();
    }

    private void setupColumns() {
        colNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom()));
        colPrix.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.2f TND", c.getValue().getPrixVente())));
        colStock.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.1f", c.getValue().getStock())));
        colQteProduite.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.1f", c.getValue().getQuantiteProduite())));
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isSimple() ? "Simple" : "Recette"));
        colRecette.setCellValueFactory(c -> {
            Produit p = c.getValue();
            if (p.isSimple()) return new SimpleStringProperty("—");
            String ing = p.getRecette().stream()
                .map(ri -> ri.getNomMatiere() + " x" + ri.getQuantite())
                .collect(Collectors.joining(", "));
            return new SimpleStringProperty(ing.isEmpty() ? "Aucune" : ing);
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn     = new Button("✏️");
            private final Button deleteBtn   = new Button("🗑️");
            private final Button produireBtn = new Button("⚙️");
            private final HBox box = new HBox(5, editBtn, deleteBtn, produireBtn);
            {
                editBtn.setTooltip(new Tooltip("Modifier"));
                deleteBtn.setTooltip(new Tooltip("Supprimer"));
                produireBtn.setTooltip(new Tooltip("Produire"));
                editBtn.setStyle("-fx-background-color: #E3F2FD; -fx-background-radius: 6; -fx-padding: 4 8; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #FFEBEE; -fx-background-radius: 6; -fx-padding: 4 8; -fx-cursor: hand;");
                produireBtn.setStyle("-fx-background-color: #E8F5E9; -fx-background-radius: 6; -fx-padding: 4 8; -fx-cursor: hand;");
                box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                produireBtn.setOnAction(e -> handleProduire(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Produit p = getTableView().getItems().get(getIndex());
                produireBtn.setVisible(!p.isSimple());
                setGraphic(box);
            }
        });
    }

    private void loadData() {
        try {
            allProduits = FXCollections.observableArrayList(produitService.selectAll());
            produitsTable.setItems(allProduits);
            updateStats();
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les produits: " + e.getMessage());
        }
    }

    private void updateStats() {
        int total = allProduits.size();
        long enStock = allProduits.stream().filter(p -> p.getStock() > 0).count();
        double valeur = allProduits.stream().mapToDouble(p -> p.getStock() * p.getPrixVente()).sum();
        if (totalProduitsLabel != null) totalProduitsLabel.setText(String.valueOf(total));
        if (enStockLabel != null) enStockLabel.setText(String.valueOf(enStock));
        if (valeurLabel != null) valeurLabel.setText(String.format("%.0f TND", valeur));
    }

    @FXML private void handleSearch() {
        String q = searchField.getText().toLowerCase().trim();
        produitsTable.setItems(q.isEmpty() ? allProduits :
            allProduits.filtered(p -> p.getNom().toLowerCase().contains(q)));
    }

    @FXML private void handleAdd() { showProduitDialog(null); }

    private void handleEdit(Produit p) { showProduitDialog(p); }

    private void handleDelete(Produit p) {
        boolean ok = AlertUtils.showConfirmation("Supprimer", "Confirmation",
            "Supprimer \"" + p.getNom() + "\" ?");
        if (ok) {
            try {
                produitService.deleteOne(p.getIdProduit());
                loadData();
                AlertUtils.showSuccess("Succès", "Produit supprimé.");
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", e.getMessage());
            }
        }
    }

    private void handleProduire(Produit produit) {
        TextInputDialog dlg = new TextInputDialog("1");
        dlg.setTitle("Lancer la production");
        dlg.setHeaderText("Produire \"" + produit.getNom() + "\"");
        dlg.setContentText("Nombre de lots:");
        dlg.showAndWait().ifPresent(val -> {
            try {
                int batches = Integer.parseInt(val.trim());
                if (batches <= 0) throw new NumberFormatException();
                double qteParLot = produit.getQuantiteProduite();
                produitService.produire(produit.getIdProduit(), batches);
                loadData();
                AlertUtils.showSuccess("Production enregistrée",
                    batches + " lot(s) → +" + (qteParLot * batches) + " unité(s) de \"" + produit.getNom() + "\".");
            } catch (NumberFormatException ex) {
                AlertUtils.showError("Validation", "Entrez un nombre entier positif.");
            } catch (RuntimeException | SQLException ex) {
                AlertUtils.showError("Erreur de production", ex.getMessage());
            }
        });
    }

    private void showProduitDialog(Produit existing) {
        boolean isEdit = existing != null;
        Dialog<Produit> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier le produit" : "Nouveau produit");
        dialog.setHeaderText(isEdit ? "Modifier \"" + existing.getNom() + "\"" : "Créer un produit fini");
        dialog.getDialogPane().setPrefWidth(500);

        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField nomField   = new TextField(isEdit ? existing.getNom() : "");
        nomField.setPromptText("Nom du produit");
        TextField descField  = new TextField(isEdit && existing.getDescription() != null ? existing.getDescription() : "");
        descField.setPromptText("Description (optionnel)");
        TextField prixField  = new TextField(isEdit ? String.valueOf(existing.getPrixVente()) : "0");
        TextField qteField   = new TextField(isEdit ? String.valueOf(existing.getQuantiteProduite()) : "1");
        TextField stockField = new TextField(isEdit ? String.valueOf(existing.getStock()) : "0");
        CheckBox simpleCheck = new CheckBox("Produit simple (pas de recette)");
        simpleCheck.setSelected(isEdit && existing.isSimple());

        grid.add(new Label("Nom *"), 0, 0);              grid.add(nomField, 1, 0);
        grid.add(new Label("Description"), 0, 1);        grid.add(descField, 1, 1);
        grid.add(new Label("Prix de vente (TND)"), 0, 2); grid.add(prixField, 1, 2);
        grid.add(new Label("Qté produite/lot"), 0, 3);   grid.add(qteField, 1, 3);
        grid.add(new Label("Stock initial"), 0, 4);      grid.add(stockField, 1, 4);
        grid.add(simpleCheck, 0, 5, 2, 1);

        Label recetteLabel = new Label("Recette (ingrédients):");
        recetteLabel.setStyle("-fx-font-weight: bold;");
        VBox recetteBox = new VBox(8);
        List<HBox> recetteRows = new ArrayList<>();

        List<Matiere> matieres;
        try { matieres = matiereService.selectAll(); }
        catch (SQLException e) { matieres = new ArrayList<>(); }
        final List<Matiere> matieresFinal = matieres;

        if (isEdit && !existing.isSimple()) {
            for (RecetteIngredient ri : existing.getRecette())
                recetteBox.getChildren().add(buildRecetteRow(matieresFinal, ri, recetteRows, recetteBox));
        }

        Button addIngBtn = new Button("➕ Ajouter ingrédient");
        addIngBtn.getStyleClass().add("secondary-btn");
        addIngBtn.setOnAction(e -> recetteBox.getChildren().add(buildRecetteRow(matieresFinal, null, recetteRows, recetteBox)));

        boolean showRecette = !simpleCheck.isSelected();
        recetteLabel.setVisible(showRecette); recetteLabel.setManaged(showRecette);
        recetteBox.setVisible(showRecette);   recetteBox.setManaged(showRecette);
        addIngBtn.setVisible(showRecette);    addIngBtn.setManaged(showRecette);

        simpleCheck.selectedProperty().addListener((obs, old, val) -> {
            recetteLabel.setVisible(!val); recetteLabel.setManaged(!val);
            recetteBox.setVisible(!val);   recetteBox.setManaged(!val);
            addIngBtn.setVisible(!val);    addIngBtn.setManaged(!val);
        });

        grid.add(recetteLabel, 0, 6, 2, 1);
        grid.add(recetteBox, 0, 7, 2, 1);
        grid.add(addIngBtn, 0, 8, 2, 1);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != saveBtn) return null;
            if (nomField.getText().trim().isEmpty()) {
                AlertUtils.showError("Validation", "Le nom est obligatoire.");
                return null;
            }
            Produit p = isEdit ? existing : new Produit();
            p.setNom(nomField.getText().trim());
            p.setDescription(descField.getText().trim().isEmpty() ? null : descField.getText().trim());
            p.setSimple(simpleCheck.isSelected());
            try {
                p.setPrixVente(Double.parseDouble(prixField.getText().trim()));
                p.setQuantiteProduite(Double.parseDouble(qteField.getText().trim()));
                p.setStock(Double.parseDouble(stockField.getText().trim()));
            } catch (NumberFormatException ex) {
                AlertUtils.showError("Validation", "Prix, quantité et stock doivent être des nombres.");
                return null;
            }
            List<RecetteIngredient> recette = new ArrayList<>();
            if (!simpleCheck.isSelected()) {
                for (HBox row : recetteRows) {
                    ComboBox<Matiere> combo = (ComboBox<Matiere>) row.getChildren().get(0);
                    TextField qte = (TextField) row.getChildren().get(1);
                    if (combo.getValue() != null && !qte.getText().trim().isEmpty()) {
                        try {
                            RecetteIngredient ri = new RecetteIngredient();
                            ri.setIdMatiere(combo.getValue().getIdMatiere());
                            ri.setQuantite(Double.parseDouble(qte.getText().trim()));
                            recette.add(ri);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
            p.setRecette(recette);
            return p;
        });

        Optional<Produit> result = dialog.showAndWait();
        result.ifPresent(p -> {
            try {
                if (isEdit) { produitService.updateOne(p); AlertUtils.showSuccess("Succès", "Produit mis à jour."); }
                else        { produitService.insertOne(p); AlertUtils.showSuccess("Succès", "Produit créé."); }
                loadData();
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", e.getMessage());
            }
        });
    }

    private HBox buildRecetteRow(List<Matiere> matieres, RecetteIngredient existing,
                                  List<HBox> rows, VBox box) {
        ComboBox<Matiere> combo = new ComboBox<>(FXCollections.observableArrayList(matieres));
        combo.setPromptText("Matière..."); combo.setPrefWidth(200);
        if (existing != null)
            matieres.stream().filter(m -> m.getIdMatiere() == existing.getIdMatiere())
                .findFirst().ifPresent(combo::setValue);

        TextField qteField = new TextField(existing != null ? String.valueOf(existing.getQuantite()) : "1");
        qteField.setPromptText("Quantité"); qteField.setPrefWidth(80);

        Button removeBtn = new Button("✕");
        removeBtn.getStyleClass().add("danger-btn");

        HBox row = new HBox(8, combo, qteField, removeBtn);
        rows.add(row);
        removeBtn.setOnAction(e -> { box.getChildren().remove(row); rows.remove(row); });
        return row;
    }
}
