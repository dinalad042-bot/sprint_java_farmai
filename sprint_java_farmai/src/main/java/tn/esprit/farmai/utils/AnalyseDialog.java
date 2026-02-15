package tn.esprit.farmai.utils;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import tn.esprit.farmai.models.Analyse;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Dialog for adding and editing Analyse.
 * Implements US11 validation and uses PreparedStatement.
 */
public class AnalyseDialog {

    private Analyse analyse;
    private boolean isEditMode;
    private String imageUrl = "";

    public AnalyseDialog() {
        this.analyse = new Analyse();
        this.isEditMode = false;
    }

    public AnalyseDialog(Analyse analyse) {
        this.analyse = analyse;
        this.isEditMode = true;
        this.imageUrl = analyse.getImageUrl() != null ? analyse.getImageUrl() : "";
    }

    /**
     * Show the dialog and return the result
     */
    public Optional<Analyse> showAndWait(Window owner) {
        Dialog<Analyse> dialog = new Dialog<>();
        dialog.setTitle(isEditMode ? "Modifier l'Analyse" : "Nouvelle Analyse");
        dialog.setHeaderText(isEditMode 
            ? "Modifier l'analyse ID: " + analyse.getIdAnalyse()
            : "Créer une nouvelle analyse");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form fields
        GridPane grid = createFormGrid();

        // Create form fields
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(isEditMode ? analyse.getDateAnalyse().toLocalDate() : LocalDate.now());
        datePicker.setPromptText("Date de l'analyse");

        TextField resultatField = new TextField();
        resultatField.setText(isEditMode ? analyse.getResultatTechnique() : "");
        resultatField.setPromptText("Résultat technique");

        Spinner<Integer> idTechnicienSpinner = new Spinner<>(1, 9999, 
            isEditMode ? analyse.getIdTechnicien() : 1);
        idTechnicienSpinner.setEditable(true);

        Spinner<Integer> idFermeSpinner = new Spinner<>(1, 9999, 
            isEditMode ? analyse.getIdFerme() : 1);
        idFermeSpinner.setEditable(true);

        // Image URL field with preview
        TextField imageUrlField = new TextField();
        imageUrlField.setText(imageUrl);
        imageUrlField.setPromptText("URL de l'image ou chemin local");

        ImageView imagePreview = new ImageView();
        imagePreview.setFitWidth(150);
        imagePreview.setFitHeight(100);
        imagePreview.setPreserveRatio(true);

        // Update preview when URL changes
        imageUrlField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateImagePreview(newVal, imagePreview);
        });

        // File chooser button
        Button browseButton = new Button("Parcourir...");
        browseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner une image");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (selectedFile != null) {
                imageUrlField.setText(selectedFile.getAbsolutePath());
            }
        });

        HBox imageBox = new HBox(10, imageUrlField, browseButton);
        imageBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Error labels
        Label dateErrorLabel = createErrorLabel("Veuillez sélectionner une date");
        Label resultatErrorLabel = createErrorLabel("Le résultat est requis (min 5 caractères)");
        Label technicienErrorLabel = createErrorLabel("ID technicien invalide");
        Label fermeErrorLabel = createErrorLabel("ID ferme invalide");

        // Add fields to grid
        int row = 0;
        grid.add(new Label("Date *"), 0, row);
        grid.add(datePicker, 1, row);
        grid.add(dateErrorLabel, 1, row + 1);
        row += 2;

        grid.add(new Label("Résultat technique *"), 0, row);
        grid.add(resultatField, 1, row);
        grid.add(resultatErrorLabel, 1, row + 1);
        row += 2;

        grid.add(new Label("ID Technicien *"), 0, row);
        grid.add(idTechnicienSpinner, 1, row);
        grid.add(technicienErrorLabel, 1, row + 1);
        row += 2;

        grid.add(new Label("ID Ferme *"), 0, row);
        grid.add(idFermeSpinner, 1, row);
        grid.add(fermeErrorLabel, 1, row + 1);
        row += 2;

        grid.add(new Label("Image"), 0, row);
        grid.add(imageBox, 1, row);
        row++;

        grid.add(imagePreview, 1, row);
        GridPane.setMargin(imagePreview, new Insets(10, 0, 0, 0));

        // Set initial preview
        if (!imageUrl.isEmpty()) {
            updateImagePreview(imageUrl, imagePreview);
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500);

        // Enable/Disable save button based on validation
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(false);

        // Convert the result to Analyse when save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // US11: Validation before saving
                if (!validateFields(datePicker, resultatField, idTechnicienSpinner, idFermeSpinner,
                        dateErrorLabel, resultatErrorLabel, technicienErrorLabel, fermeErrorLabel)) {
                    return null;
                }

                analyse.setDateAnalyse(LocalDateTime.of(datePicker.getValue(), LocalTime.now()));
                analyse.setResultatTechnique(resultatField.getText().trim());
                analyse.setIdTechnicien(idTechnicienSpinner.getValue());
                analyse.setIdFerme(idFermeSpinner.getValue());
                analyse.setImageUrl(imageUrlField.getText().trim());

                return analyse;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    /**
     * Validate all fields (US11)
     */
    private boolean validateFields(DatePicker datePicker, TextField resultatField,
                                   Spinner<Integer> idTechnicienSpinner, Spinner<Integer> idFermeSpinner,
                                   Label dateErrorLabel, Label resultatErrorLabel,
                                   Label technicienErrorLabel, Label fermeErrorLabel) {
        boolean isValid = true;

        // Validate date
        if (datePicker.getValue() == null) {
            showError(dateErrorLabel);
            isValid = false;
        } else if (datePicker.getValue().isAfter(LocalDate.now())) {
            dateErrorLabel.setText("La date ne peut pas être dans le futur");
            showError(dateErrorLabel);
            isValid = false;
        } else {
            hideError(dateErrorLabel);
        }

        // Validate resultat (min 5 characters)
        String resultat = resultatField.getText().trim();
        if (resultat.isEmpty() || resultat.length() < 5) {
            resultatErrorLabel.setText("Le résultat doit contenir au moins 5 caractères");
            showError(resultatErrorLabel);
            resultatField.setStyle("-fx-border-color: #D32F2F; -fx-border-width: 2px;");
            isValid = false;
        } else {
            hideError(resultatErrorLabel);
            resultatField.setStyle("-fx-border-color: transparent;");
        }

        // Validate technicien ID
        try {
            int techId = idTechnicienSpinner.getValue();
            if (techId <= 0) {
                throw new IllegalArgumentException();
            }
            hideError(technicienErrorLabel);
        } catch (Exception e) {
            showError(technicienErrorLabel);
            isValid = false;
        }

        // Validate ferme ID
        try {
            int fermeId = idFermeSpinner.getValue();
            if (fermeId <= 0) {
                throw new IllegalArgumentException();
            }
            hideError(fermeErrorLabel);
        } catch (Exception e) {
            showError(fermeErrorLabel);
            isValid = false;
        }

        if (!isValid) {
            // Show global error alert
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez corriger les erreurs avant d'enregistrer.");
            alert.showAndWait();
        }

        return isValid;
    }

    private void showError(Label errorLabel) {
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private Label createErrorLabel(String message) {
        Label label = new Label(message);
        label.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 11px;");
        label.setVisible(false);
        label.setManaged(false);
        return label;
    }

    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setPadding(new Insets(20, 20, 10, 20));
        grid.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(120);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        return grid;
    }

    private void updateImagePreview(String url, ImageView imageView) {
        if (url == null || url.trim().isEmpty()) {
            imageView.setImage(null);
            return;
        }

        try {
            String path = url;
            if (!url.startsWith("http") && !url.startsWith("file:")) {
                File file = new File(url);
                if (file.exists()) {
                    path = file.toURI().toString();
                }
            }
            imageView.setImage(new Image(path, true));
        } catch (Exception e) {
            imageView.setImage(null);
        }
    }
}