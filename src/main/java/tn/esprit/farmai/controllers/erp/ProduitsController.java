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
 * Controller for ERP Produits view.
 * Full CRUD + production (consume raw materials via recipe).
 */
public class ProduitsController extends BaseERPController {

    @FXML private TextField searchField;
    @FXML private TableView<Produit> produitsTable;
    @FXML private TableColumn<Produit, String> colNom;
    @FXML private TableColumn<Produit, String> colPrix;
    @FXML private TableColumn<Produit, String> colStock;
    @FXML private TableColumn<Produit, String> colQteProduite;
    @FXML private TableColumn<Produit, String> colType;
    @FXML private TableColumn<Produit, String> colRecette;
    @FXML private TableColumn<Produit, Void> colActions;

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
            String ingredients = p.getRecette().stream()
                .map(ri -> ri.getNomMatiere() + " x" + ri.getQuantite())
                .collect(Collectors.joining(", "));
            return new SimpleStringProperty(ingredients.isEmpty() ? "Aucune" : ingredients);
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final Button produireBtn = new Button("⚙️ Produire");
            private final HBox box = new HBox(6, editBtn, deleteBtn, produireBtn);

            {
                editBtn.getStyleClass().add("action-btn");
                deleteBtn.getStyleClass().add("danger-btn");
                produireBtn.getStyleClass().add("secondary-btn");
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
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les produits: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            produitsTable.setItems(allProduits);
        } else {
            produitsTable.setItems(allProduits.filtered(
                p -> p.getNom().toLowerCase().contains(query)
            ));
        }
    }

    @FXML
    private void handleAdd() {
        showProduitDialog(null);
    }

    private void handleEdit(Produit produit) {
        showProduitDialog(produit);
    }

    private void handleDelete(Produit produit) {
        boolean confirmed = AlertUtils.showConfirmation(
            "Supprimer le produit",
            "Confirmation",
            "Supprimer \"" + produit.getNom() + "\" ? Cette action est irréversible."
        );
        if (confirmed) {
            try {
                produitService.deleteOne(produit.getIdProduit());
                loadData();
                AlertUtils.showSuccess("Succès", "Produit supprimé.");
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Impossible de supprimer: " + e.getMessage());
            }
        }
    }

    private void handleProduire(Produit produit) {
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Lancer la production");
        dialog.setHeaderText("Produire \"" + produit.getNom() + "\"");
        dialog.setContentText("Nombre de lots à produire:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(val -> {
            try {
                int batches = Integer.parseInt(val.trim());
                if (batches <= 0) throw new NumberFormatException();
                // Capture values before service call (service reloads from DB internally)
                double qteParLot = produit.getQuantiteProduite();
                produitService.produire(produit.getIdProduit(), batches);
                loadData(); // refresh table with updated stock
                double produced = qteParLot * batches;
                AlertUtils.showSuccess("Production enregistrée",
                    batches + " lot(s) → +" + produced + " unité(s) de \"" + produit.getNom() + "\".\n"
                    + "Matières premières consommées selon la recette.");
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

        TextField nomField = new TextField(isEdit ? existing.getNom() : "");
        nomField.setPromptText("Nom du produit");
        TextField descField = new TextField(isEdit && existing.getDescription() != null ? existing.getDescription() : "");
        descField.setPromptText("Description (optionnel)");
        TextField prixField = new TextField(isEdit ? String.valueOf(existing.getPrixVente()) : "0");
        TextField qteField = new TextField(isEdit ? String.valueOf(existing.getQuantiteProduite()) : "1");
        TextField stockField = new TextField(isEdit ? String.valueOf(existing.getStock()) : "0");
        CheckBox simpleCheck = new CheckBox("Produit simple (pas de recette)");
        simpleCheck.setSelected(isEdit && existing.isSimple());

        grid.add(new Label("Nom *"), 0, 0); grid.add(nomField, 1, 0);
        grid.add(new Label("Description"), 0, 1); grid.add(descField, 1, 1);
        grid.add(new Label("Prix de vente (TND)"), 0, 2); grid.add(prixField, 1, 2);
        grid.add(new Label("Qté produite/lot"), 0, 3); grid.add(qteField, 1, 3);
        grid.add(new Label("Stock initial"), 0, 4); grid.add(stockField, 1, 4);
        grid.add(simpleCheck, 0, 5, 2, 1);

        // Recipe section
        Label recetteLabel = new Label("Recette (ingrédients):");
        recetteLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 5 0;");
        VBox recetteBox = new VBox(8);
        List<HBox> recetteRows = new ArrayList<>();

        // Load existing recipe
        List<Matiere> matieres;
        try {
            matieres = matiereService.selectAll();
        } catch (SQLException e) {
            matieres = new ArrayList<>();
        }
        final List<Matiere> matieresFinal = matieres;

        if (isEdit && !existing.isSimple()) {
            for (RecetteIngredient ri : existing.getRecette()) {
                recetteBox.getChildren().add(buildRecetteRow(matieresFinal, ri, recetteRows, recetteBox));
            }
        }

        Button addIngredientBtn = new Button("➕ Ajouter ingrédient");
        addIngredientBtn.getStyleClass().add("secondary-btn");
        addIngredientBtn.setOnAction(e -> recetteBox.getChildren().add(
            buildRecetteRow(matieresFinal, null, recetteRows, recetteBox)
        ));

        // Show/hide recipe based on simple checkbox
        recetteLabel.setVisible(!simpleCheck.isSelected());
        recetteLabel.setManaged(!simpleCheck.isSelected());
        recetteBox.setVisible(!simpleCheck.isSelected());
        recetteBox.setManaged(!simpleCheck.isSelected());
        addIngredientBtn.setVisible(!simpleCheck.isSelected());
        addIngredientBtn.setManaged(!simpleCheck.isSelected());

        simpleCheck.selectedProperty().addListener((obs, old, val) -> {
            recetteLabel.setVisible(!val); recetteLabel.setManaged(!val);
            recetteBox.setVisible(!val); recetteBox.setManaged(!val);
            addIngredientBtn.setVisible(!val); addIngredientBtn.setManaged(!val);
        });

        grid.add(recetteLabel, 0, 6, 2, 1);
        grid.add(recetteBox, 0, 7, 2, 1);
        grid.add(addIngredientBtn, 0, 8, 2, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
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
                // Collect recipe
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
            }
            return null;
        });

        Optional<Produit> result = dialog.showAndWait();
        result.ifPresent(p -> {
            try {
                if (isEdit) {
                    produitService.updateOne(p);
                    AlertUtils.showSuccess("Succès", "Produit mis à jour.");
                } else {
                    produitService.insertOne(p);
                    AlertUtils.showSuccess("Succès", "Produit créé.");
                }
                loadData();
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Opération échouée: " + e.getMessage());
            }
        });
    }

    private HBox buildRecetteRow(List<Matiere> matieres, RecetteIngredient existing,
                                  List<HBox> recetteRows, VBox recetteBox) {
        ComboBox<Matiere> combo = new ComboBox<>(FXCollections.observableArrayList(matieres));
        combo.setPromptText("Matière...");
        combo.setPrefWidth(200);
        if (existing != null) {
            matieres.stream().filter(m -> m.getIdMatiere() == existing.getIdMatiere())
                .findFirst().ifPresent(combo::setValue);
        }

        TextField qteField = new TextField(existing != null ? String.valueOf(existing.getQuantite()) : "1");
        qteField.setPromptText("Quantité");
        qteField.setPrefWidth(80);

        Button removeBtn = new Button("✕");
        removeBtn.getStyleClass().add("danger-btn");

        HBox row = new HBox(8, combo, qteField, removeBtn);
        recetteRows.add(row);
        removeBtn.setOnAction(e -> {
            recetteBox.getChildren().remove(row);
            recetteRows.remove(row);
        });
        return row;
    }
}
