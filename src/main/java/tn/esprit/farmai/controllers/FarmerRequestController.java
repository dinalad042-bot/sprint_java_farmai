package tn.esprit.farmai.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.farmai.models.Analyse;
import tn.esprit.farmai.models.Animaux;
import tn.esprit.farmai.models.Ferme;
import tn.esprit.farmai.models.Plantes;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.AnalyseService;
import tn.esprit.farmai.services.FermeService;
import tn.esprit.farmai.services.ServiceAnimaux;
import tn.esprit.farmai.services.ServicePlantes;
import tn.esprit.farmai.utils.ProfileManager;
import tn.esprit.farmai.utils.SessionManager;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Farmer request submission.
 * Allows agricultural users to create analysis requests.
 */
public class FarmerRequestController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(FarmerRequestController.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Circle sidebarAvatar;
    @FXML private Text sidebarAvatarText;

    // Form fields
    @FXML private ComboBox<Ferme> fermeComboBox;
    @FXML private ComboBox<String> typeComboBox; // Animal or Plante
    @FXML private ComboBox<String> cibleComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private TextField imagePathField;
    @FXML private Label selectedFileLabel;

    // My requests table
    @FXML private TableView<Analyse> myRequestsTableView;
    @FXML private TableColumn<Analyse, String> colId;
    @FXML private TableColumn<Analyse, String> colDate;
    @FXML private TableColumn<Analyse, String> colFerme;
    @FXML private TableColumn<Analyse, String> colStatut;
    @FXML private TableColumn<Analyse, String> colDescription;
    @FXML private TableColumn<Analyse, Void> colActions;

    private final AnalyseService analyseService;
    private final FermeService fermeService;
    private final ServiceAnimaux animalService;
    private final ServicePlantes planteService;
    private ObservableList<Analyse> myRequestsList;
    private ObservableList<String> animalOptions = FXCollections.observableArrayList();
    private ObservableList<String> planteOptions = FXCollections.observableArrayList();
    private String selectedImagePath;

    public FarmerRequestController() {
        this.analyseService = new AnalyseService();
        this.fermeService = new FermeService();
        this.animalService = new ServiceAnimaux();
        this.planteService = new ServicePlantes();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                ProfileManager.updateProfileUI(currentUser, null, userNameLabel, sidebarAvatar, sidebarAvatarText);
                if (userRoleLabel != null) {
                    userRoleLabel.setText(currentUser.getRole().getDisplayName());
                }
            }

            setupForm();
            setupMyRequestsTable();
            loadMyRequests();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing FarmerRequestController: " + e.getMessage(), e);
            tn.esprit.farmai.utils.NavigationUtil.showError("Erreur d'initialisation", "Erreur: " + e.getMessage());
        }
    }

    private void setupForm() {
        // Load user's fermes
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            try {
                List<Ferme> fermes = fermeService.findByFermier(currentUser.getIdUser());
                fermeComboBox.setItems(FXCollections.observableArrayList(fermes));
                fermeComboBox.getSelectionModel().selectFirst();

                // Load animals and plants for the selected farm
                if (!fermes.isEmpty()) {
                    loadAnimauxForFerme(fermes.get(0).getIdFerme());
                    loadPlantesForFerme(fermes.get(0).getIdFerme());
                }

                // When farm changes, reload animals/plants
                fermeComboBox.setOnAction(e -> {
                    Ferme selected = fermeComboBox.getValue();
                    if (selected != null) {
                        loadAnimauxForFerme(selected.getIdFerme());
                        loadPlantesForFerme(selected.getIdFerme());
                        loadCibles(); // Refresh cible dropdown
                    }
                });
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to load fermes", e);
            }
        }

        // Type combo
        typeComboBox.getItems().addAll("Animal", "Plante", "Autre");
        typeComboBox.getSelectionModel().selectFirst();
        typeComboBox.setOnAction(e -> loadCibles());
    }

    private void loadAnimauxForFerme(int idFerme) {
        try {
            List<Animaux> animaux = animalService.findByFerme(idFerme);
            animalOptions.clear();
            animalOptions.add("Aucun spécifique");
            for (Animaux a : animaux) {
                animalOptions.add(a.getIdAnimal() + " - " + a.getEspece());
            }
            LOGGER.log(Level.INFO, "Loaded {0} animals for ferme {1}", new Object[]{animaux.size(), idFerme});
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load animals for ferme " + idFerme, e);
        }
    }

    private void loadPlantesForFerme(int idFerme) {
        try {
            List<Plantes> plantes = planteService.findByFerme(idFerme);
            planteOptions.clear();
            planteOptions.add("Aucune spécifique");
            for (Plantes p : plantes) {
                planteOptions.add(p.getIdPlante() + " - " + p.getNomEspece());
            }
            LOGGER.log(Level.INFO, "Loaded {0} plants for ferme {1}", new Object[]{plantes.size(), idFerme});
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load plants for ferme " + idFerme, e);
        }
    }

    private void loadCibles() {
        String type = typeComboBox.getValue();
        cibleComboBox.getItems().clear();

        if ("Animal".equals(type)) {
            cibleComboBox.setItems(animalOptions);
        } else if ("Plante".equals(type)) {
            cibleComboBox.setItems(planteOptions);
        } else {
            cibleComboBox.getItems().add("Aucune cible");
        }
        cibleComboBox.getSelectionModel().selectFirst();
    }

    private void setupMyRequestsTable() {
        colId.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getIdAnalyse())));
        colDate.setCellValueFactory(cell -> {
            if (cell.getValue().getDateAnalyse() != null) {
                return new SimpleStringProperty(cell.getValue().getDateAnalyse().format(DATE_FORMATTER));
            }
            return new SimpleStringProperty("-");
        });
        colFerme.setCellValueFactory(cell -> new SimpleStringProperty("Ferme #" + cell.getValue().getIdFerme()));
        colStatut.setCellValueFactory(cell -> new SimpleStringProperty(formatStatut(cell.getValue().getStatut())));
        colDescription.setCellValueFactory(cell -> {
            String desc = cell.getValue().getDescriptionDemande();
            if (desc != null && desc.length() > 50) {
                desc = desc.substring(0, 50) + "...";
            }
            return new SimpleStringProperty(desc != null ? desc : "-");
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("Voir");
            {
                viewBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 11px;");
                viewBtn.setOnAction(e -> showRequestDetails(getTableRow().getItem()));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || getTableRow() == null || getTableRow().getItem() == null ? null : viewBtn);
            }
        });
    }

    private String formatStatut(String statut) {
        return switch (statut) {
            case "en_attente" -> "En attente ⏳";
            case "en_cours" -> "En cours 🔄";
            case "terminee" -> "Terminée ✅";
            case "annulee" -> "Annulée ❌";
            default -> statut != null ? statut : "-";
        };
    }

    private void loadMyRequests() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        try {
            List<Analyse> requests = analyseService.findByDemandeur(currentUser.getIdUser());
            myRequestsList = FXCollections.observableArrayList(requests);
            myRequestsTableView.setItems(myRequestsList);
            LOGGER.log(Level.INFO, "Loaded {0} requests for farmer", new Object[]{requests.size()});
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Error loading requests: " + e.getMessage() + " | State: " + e.getSQLState() + " | ErrorCode: " + e.getErrorCode(), e);
            tn.esprit.farmai.utils.NavigationUtil.showError("Erreur", "Impossible de charger vos demandes.\n\n Détails: " + e.getMessage());
        }
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(imagePathField.getScene().getWindow());
        if (file != null) {
            selectedImagePath = file.getAbsolutePath();
            imagePathField.setText(file.getName());
            selectedFileLabel.setText("📷 " + file.getName());
        }
    }

    @FXML
    private void handleSubmitRequest() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            tn.esprit.farmai.utils.NavigationUtil.showError("Erreur", "Vous devez être connecté.");
            return;
        }

        Ferme selectedFerme = fermeComboBox.getValue();
        if (selectedFerme == null) {
            tn.esprit.farmai.utils.NavigationUtil.showError("Erreur", "Veuillez sélectionner une ferme.");
            return;
        }

        String description = descriptionArea.getText();
        if (description == null || description.trim().isEmpty()) {
            tn.esprit.farmai.utils.NavigationUtil.showError("Erreur", "Veuillez décrire votre demande.");
            return;
        }

        try {
            Analyse analyse = new Analyse();
            analyse.setDateAnalyse(LocalDateTime.now());
            analyse.setStatut("en_attente");
            analyse.setIdDemandeur(currentUser.getIdUser());
            analyse.setIdFerme(selectedFerme.getIdFerme());
            analyse.setDescriptionDemande(description);
            analyse.setImageUrl(selectedImagePath);
            analyse.setIdAnimalCible(0);
            analyse.setIdPlanteCible(0);

            analyseService.createFarmerRequest(analyse);
            tn.esprit.farmai.utils.NavigationUtil.showSuccess("Succès", "Votre demande a été soumise! Un expert la prendra en charge bientôt.");

            // Clear form
            descriptionArea.clear();
            imagePathField.clear();
            selectedFileLabel.setText("");
            selectedImagePath = null;

            // Reload table
            loadMyRequests();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to submit request", e);
            tn.esprit.farmai.utils.NavigationUtil.showError("Erreur", "Impossible de soumettre la demande: " + e.getMessage());
        }
    }

    private void showRequestDetails(Analyse analyse) {
        if (analyse == null) return;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la Demande #" + analyse.getIdAnalyse());
        alert.setHeaderText("Demande #" + analyse.getIdAnalyse());

        StringBuilder content = new StringBuilder();
        content.append("Statut: ").append(formatStatut(analyse.getStatut())).append("\n\n");
        content.append("Ferme: #").append(analyse.getIdFerme()).append("\n\n");
        content.append("Date: ").append(analyse.getDateAnalyse() != null ? analyse.getDateAnalyse().format(DATE_FORMATTER) : "-").append("\n\n");
        content.append("Description:\n").append(analyse.getDescriptionDemande() != null ? analyse.getDescriptionDemande() : "Aucune").append("\n\n");

        if (analyse.getResultatTechnique() != null && !analyse.getResultatTechnique().isEmpty()) {
            content.append("Résultat de l'expert:\n").append(analyse.getResultatTechnique()).append("\n");
        }

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        navigateToDashboard();
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        tn.esprit.farmai.utils.NavigationUtil.logout(stage);
    }

    private void navigateToDashboard() {
        try {
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/farmai/views/agricole-dashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 800);

            java.net.URL cssUrl = getClass().getResource("/tn/esprit/farmai/styles/dashboard.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("FarmAI - Tableau de Bord Agricole");
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to dashboard", e);
        }
    }
}