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
import tn.esprit.farmai.models.ServiceERP;
import tn.esprit.farmai.services.ServiceERPService;
import tn.esprit.farmai.utils.AlertUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for ERP Services view.
 * Full CRUD with critical stock alerts.
 */
public class ServicesController extends BaseERPController {

    @FXML private TextField searchField;
    @FXML private TableView<ServiceERP> servicesTable;
    @FXML private TableColumn<ServiceERP, String> colNom;
    @FXML private TableColumn<ServiceERP, String> colPrix;
    @FXML private TableColumn<ServiceERP, String> colStock;
    @FXML private TableColumn<ServiceERP, String> colSeuil;
    @FXML private TableColumn<ServiceERP, String> colStatut;
    @FXML private TableColumn<ServiceERP, Void> colActions;
    @FXML private HBox alertBox;
    @FXML private Label alertLabel;

    private final ServiceERPService serviceERPService = new ServiceERPService();
    private ObservableList<ServiceERP> allServices = FXCollections.observableArrayList();

    @Override
    protected void onInit() {
        setupColumns();
        loadData();
    }

    private void setupColumns() {
        colNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom()));
        colPrix.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.2f TND", c.getValue().getPrix())));
        colStock.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getStock())));
        colSeuil.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getSeuilCritique())));
        colStatut.setCellValueFactory(c -> {
            ServiceERP s = c.getValue();
            return new SimpleStringProperty(s.isStockCritique() ? "⚠️ Critique" : "✅ OK");
        });
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if (item.contains("Critique")) setStyle("-fx-text-fill: #E65100; -fx-font-weight: bold;");
                else setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("✏️ Modifier");
            private final Button deleteBtn = new Button("🗑️ Supprimer");
            private final HBox box = new HBox(8, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("action-btn");
                deleteBtn.getStyleClass().add("danger-btn");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
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
            allServices = FXCollections.observableArrayList(serviceERPService.selectAll());
            servicesTable.setItems(allServices);
            checkCriticalStock();
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les services: " + e.getMessage());
        }
    }

    private void checkCriticalStock() {
        try {
            List<ServiceERP> critiques = serviceERPService.findStockCritique();
            if (!critiques.isEmpty()) {
                String names = critiques.stream().map(ServiceERP::getNom).collect(Collectors.joining(", "));
                alertLabel.setText("Stock critique pour: " + names);
                alertBox.setVisible(true);
                alertBox.setManaged(true);
            } else {
                alertBox.setVisible(false);
                alertBox.setManaged(false);
            }
        } catch (SQLException e) {
            // ignore
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            servicesTable.setItems(allServices);
        } else {
            servicesTable.setItems(allServices.filtered(
                s -> s.getNom().toLowerCase().contains(query)
            ));
        }
    }

    @FXML
    private void handleAdd() {
        showServiceDialog(null);
    }

    private void handleEdit(ServiceERP service) {
        showServiceDialog(service);
    }

    private void handleDelete(ServiceERP service) {
        boolean confirmed = AlertUtils.showConfirmation(
            "Supprimer le service",
            "Confirmation",
            "Supprimer \"" + service.getNom() + "\" ? Cette action est irréversible."
        );
        if (confirmed) {
            try {
                serviceERPService.deleteOne(service.getIdService());
                loadData();
                AlertUtils.showSuccess("Succès", "Service supprimé.");
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Impossible de supprimer: " + e.getMessage());
            }
        }
    }

    private void showServiceDialog(ServiceERP existing) {
        boolean isEdit = existing != null;
        Dialog<ServiceERP> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Modifier le service" : "Nouveau service");
        dialog.setHeaderText(isEdit ? "Modifier \"" + existing.getNom() + "\"" : "Créer un service");

        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField nomField = new TextField(isEdit ? existing.getNom() : "");
        nomField.setPromptText("Nom du service");
        TextField descField = new TextField(isEdit && existing.getDescription() != null ? existing.getDescription() : "");
        descField.setPromptText("Description (optionnel)");
        TextField prixField = new TextField(isEdit ? String.valueOf(existing.getPrix()) : "0");
        TextField stockField = new TextField(isEdit ? String.valueOf(existing.getStock()) : "0");
        TextField seuilField = new TextField(isEdit ? String.valueOf(existing.getSeuilCritique()) : "0");

        grid.add(new Label("Nom *"), 0, 0); grid.add(nomField, 1, 0);
        grid.add(new Label("Description"), 0, 1); grid.add(descField, 1, 1);
        grid.add(new Label("Prix (TND)"), 0, 2); grid.add(prixField, 1, 2);
        grid.add(new Label("Stock"), 0, 3); grid.add(stockField, 1, 3);
        grid.add(new Label("Seuil critique"), 0, 4); grid.add(seuilField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                if (nomField.getText().trim().isEmpty()) {
                    AlertUtils.showError("Validation", "Le nom est obligatoire.");
                    return null;
                }
                ServiceERP s = isEdit ? existing : new ServiceERP();
                s.setNom(nomField.getText().trim());
                s.setDescription(descField.getText().trim().isEmpty() ? null : descField.getText().trim());
                try {
                    s.setPrix(Double.parseDouble(prixField.getText().trim()));
                    s.setStock(Integer.parseInt(stockField.getText().trim()));
                    s.setSeuilCritique(Integer.parseInt(seuilField.getText().trim()));
                } catch (NumberFormatException ex) {
                    AlertUtils.showError("Validation", "Prix, stock et seuil doivent être des nombres.");
                    return null;
                }
                return s;
            }
            return null;
        });

        Optional<ServiceERP> result = dialog.showAndWait();
        result.ifPresent(s -> {
            try {
                if (isEdit) {
                    serviceERPService.updateOne(s);
                    AlertUtils.showSuccess("Succès", "Service mis à jour.");
                } else {
                    serviceERPService.insertOne(s);
                    AlertUtils.showSuccess("Succès", "Service créé.");
                }
                loadData();
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Opération échouée: " + e.getMessage());
            }
        });
    }
}
