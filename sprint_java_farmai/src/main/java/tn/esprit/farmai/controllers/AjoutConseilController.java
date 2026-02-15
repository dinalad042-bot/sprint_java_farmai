package tn.esprit.farmai.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.farmai.models.Analyse;
import tn.esprit.farmai.models.Conseil;
import tn.esprit.farmai.models.Priorite;
import tn.esprit.farmai.services.AnalyseService;
import tn.esprit.farmai.services.ConseilService;
import tn.esprit.farmai.utils.NavigationUtil;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller for adding a new Conseil (US5 & US7).
 * Manages 1:N relationship with Analyse via ComboBox.
 * US11: Input validation before calling services.
 */
public class AjoutConseilController implements Initializable {

    @FXML
    private ComboBox<Analyse> analyseComboBox;
    @FXML
    private ComboBox<Priorite> prioriteComboBox;
    @FXML
    private TextArea descriptionConseilField;

    @FXML
    private VBox imagePreviewContainer;
    @FXML
    private ImageView imagePreview;
    @FXML
    private Label imageUrlLabel;

    @FXML
    private Label analyseErrorLabel;
    @FXML
    private Label prioriteErrorLabel;
    @FXML
    private Label descriptionErrorLabel;
    @FXML
    private Label charCountLabel;

    @FXML
    private Button enregistrerButton;
    @FXML
    private Button annulerButton;

    private final AnalyseService analyseService;
    private final ConseilService conseilService;
    private ObservableList<Analyse> analysesList;

    public AjoutConseilController() {
        this.analyseService = new AnalyseService();
        this.conseilService = new ConseilService();
        this.analysesList = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadAnalysesComboBox();
        setupPrioriteComboBox();
        setupValidationListeners();
    }

    /**
     * Load analyses into ComboBox for 1:N relationship
     */
    private void loadAnalysesComboBox() {
        try {
            analysesList.clear();
            analysesList.addAll(analyseService.selectAll());
            analyseComboBox.setItems(analysesList);

            // Custom display for ComboBox items
            analyseComboBox.setCellFactory(param -> new ListCell<Analyse>() {
                @Override
                protected void updateItem(Analyse analyse, boolean empty) {
                    super.updateItem(analyse, empty);
                    if (empty || analyse == null) {
                        setText(null);
                    } else {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        setText(String.format("ID: %d - %s (Ferme: %d)",
                            analyse.getIdAnalyse(),
                            analyse.getDateAnalyse().format(formatter),
                            analyse.getIdFerme()));
                    }
                }
            });

            analyseComboBox.setButtonCell(new ListCell<Analyse>() {
                @Override
                protected void updateItem(Analyse analyse, boolean empty) {
                    super.updateItem(analyse, empty);
                    if (empty || analyse == null) {
                        setText("Sélectionner une analyse...");
                    } else {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        setText(String.format("ID: %d - %s (Ferme: %d)",
                            analyse.getIdAnalyse(),
                            analyse.getDateAnalyse().format(formatter),
                            analyse.getIdFerme()));
                    }
                }
            });

            // Listen for selection changes to update image preview
            analyseComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                handleAnalyseSelection(newVal);
                clearError(analyseErrorLabel);
            });

        } catch (SQLException e) {
            NavigationUtil.showError("Erreur", "Impossible de charger les analyses: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Setup Priorite ComboBox with enum values
     */
    private void setupPrioriteComboBox() {
        prioriteComboBox.setItems(FXCollections.observableArrayList(Priorite.values()));
        prioriteComboBox.setCellFactory(param -> new ListCell<Priorite>() {
            @Override
            protected void updateItem(Priorite priorite, boolean empty) {
                super.updateItem(priorite, empty);
                if (empty || priorite == null) {
                    setText(null);
                } else {
                    // Display with color indicator
                    String emoji = switch (priorite) {
                        case BASSE -> "🟢";
                        case MOYENNE -> "🟡";
                        case HAUTE -> "🔴";
                    };
                    setText(emoji + " " + priorite.getLabel());
                }
            }
        });

        prioriteComboBox.setButtonCell(new ListCell<Priorite>() {
            @Override
            protected void updateItem(Priorite priorite, boolean empty) {
                super.updateItem(priorite, empty);
                if (empty || priorite == null) {
                    setText("Choisir la priorité...");
                } else {
                    String emoji = switch (priorite) {
                        case BASSE -> "🟢";
                        case MOYENNE -> "🟡";
                        case HAUTE -> "🔴";
                    };
                    setText(emoji + " " + priorite.getLabel());
                }
            }
        });

        // Clear error on selection
        prioriteComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            clearError(prioriteErrorLabel);
        });
    }

    /**
     * Setup validation listeners (US11)
     */
    private void setupValidationListeners() {
        // Description field validation
        descriptionConseilField.textProperty().addListener((obs, oldVal, newVal) -> {
            int length = newVal != null ? newVal.length() : 0;
            charCountLabel.setText(length + " caractères");

            if (length >= 10) {
                clearError(descriptionErrorLabel);
                descriptionConseilField.setStyle("-fx-border-color: transparent;");
            }
        });
    }

    /**
     * Handle analyse selection - update image preview (US6)
     */
    private void handleAnalyseSelection(Analyse analyse) {
        if (analyse != null && analyse.getImageUrl() != null && !analyse.getImageUrl().isEmpty()) {
            try {
                String imageUrl = analyse.getImageUrl();
                String path = imageUrl;

                if (!imageUrl.startsWith("http") && !imageUrl.startsWith("file:")) {
                    File file = new File(imageUrl);
                    if (file.exists()) {
                        path = file.toURI().toString();
                    }
                }

                imagePreview.setImage(new Image(path, true));
                imageUrlLabel.setText("URL: " + imageUrl);
                imagePreviewContainer.setVisible(true);
            } catch (Exception e) {
                imagePreviewContainer.setVisible(false);
            }
        } else {
            imagePreviewContainer.setVisible(false);
        }
    }

    /**
     * Validate all fields (US11 - Contrôles de Saisie)
     */
    private boolean validateFields() {
        boolean isValid = true;

        // Validate Analyse selection
        if (analyseComboBox.getValue() == null) {
            showError(analyseErrorLabel);
            isValid = false;
        } else {
            clearError(analyseErrorLabel);
        }

        // Validate Priorite selection
        if (prioriteComboBox.getValue() == null) {
            showError(prioriteErrorLabel);
            isValid = false;
        } else {
            clearError(prioriteErrorLabel);
        }

        // Validate Description (min 10 characters)
        String description = descriptionConseilField.getText();
        if (description == null || description.trim().length() < 10) {
            showError(descriptionErrorLabel);
            descriptionConseilField.setStyle("-fx-border-color: #D32F2F; -fx-border-width: 2px;");
            isValid = false;
        } else {
            clearError(descriptionErrorLabel);
            descriptionConseilField.setStyle("-fx-border-color: transparent;");
        }

        return isValid;
    }

    private void showError(Label errorLabel) {
        errorLabel.setVisible(true);
    }

    private void clearError(Label errorLabel) {
        errorLabel.setVisible(false);
    }

    /**
     * Handle save button
     */
    @FXML
    private void handleEnregistrer() {
        // US11: Validation before calling service
        if (!validateFields()) {
            NavigationUtil.showWarning("Validation", "Veuillez corriger les erreurs avant d'enregistrer.");
            return;
        }

        try {
            Conseil conseil = new Conseil();
            conseil.setDescriptionConseil(descriptionConseilField.getText().trim());
            conseil.setPriorite(prioriteComboBox.getValue());
            conseil.setIdAnalyse(analyseComboBox.getValue().getIdAnalyse());

            conseilService.insertOne(conseil);

            NavigationUtil.showSuccess("Succès",
                "Conseil enregistré avec succès!\nID: " + conseil.getIdConseil());

            // Close the window
            closeWindow();

        } catch (SQLException e) {
            NavigationUtil.showError("Erreur",
                "Impossible d'enregistrer le conseil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle cancel button
     */
    @FXML
    private void handleAnnuler() {
        closeWindow();
    }

    /**
     * Close the current window
     */
    private void closeWindow() {
        Stage stage = (Stage) annulerButton.getScene().getWindow();
        stage.close();
    }
}
