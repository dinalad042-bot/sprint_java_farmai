package tn.esprit.farmai.utils;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import tn.esprit.farmai.models.Analyse;
import tn.esprit.farmai.models.Ferme;
import tn.esprit.farmai.services.AnalyseService;
import tn.esprit.farmai.services.FermeService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * Dialog for adding and editing Analyse.
 * Implements US11 validation and uses PreparedStatement.
 * Updated: ComboBox for Ferme selection instead of manual ID input.
 * 
 * Advanced Features:
 * - US8: AI-Assisted Diagnostic Suggestions
 * - Weather API Integration for diagnostic enrichment
 */
public class AnalyseDialog {

    private Analyse analyse;
    private boolean isEditMode;
    private String imageUrl = "";
    private FermeService fermeService;
    private AnalyseService analyseService;
    private ObservableList<Ferme> fermesList;
    
    // Weather enrichment state
    private boolean includeWeather = true;

    public AnalyseDialog() {
        this.analyse = new Analyse();
        this.isEditMode = false;
        this.fermeService = new FermeService();
        this.analyseService = new AnalyseService();
        this.fermesList = FXCollections.observableArrayList();
    }

    public AnalyseDialog(Analyse analyse) {
        this.analyse = analyse;
        this.isEditMode = true;
        this.imageUrl = analyse.getImageUrl() != null ? analyse.getImageUrl() : "";
        this.fermeService = new FermeService();
        this.analyseService = new AnalyseService();
        this.fermesList = FXCollections.observableArrayList();
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

        // ComboBox for Ferme selection (replaces Spinner)
        ComboBox<Ferme> fermeComboBox = new ComboBox<>();
        fermeComboBox.setPromptText("Sélectionner une ferme");
        fermeComboBox.setPrefWidth(250);
        loadFermes(fermeComboBox);

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
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File selectedFile = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (selectedFile != null) {
                imageUrlField.setText(selectedFile.getAbsolutePath());
            }
        });

        HBox imageBox = new HBox(10, imageUrlField, browseButton);
        imageBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // ===== US8: AI Suggestion Button =====
        Button aiSuggestButton = new Button("🤖 Suggérer via IA");
        aiSuggestButton.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; -fx-font-weight: bold;");
        aiSuggestButton.setTooltip(new Tooltip("Générer une suggestion de diagnostic via IA (US8)"));
        
        TextArea observationArea = new TextArea();
        observationArea.setPromptText("Entrez vos observations pour l'IA (données capteurs, notes visuelles...)");
        observationArea.setPrefRowCount(3);
        observationArea.setPrefWidth(300);
        observationArea.setWrapText(true);
        
        // AI suggestion handler
        aiSuggestButton.setOnAction(e -> {
            String observation = observationArea.getText().trim();
            if (observation.isEmpty()) {
                AlertUtils.showWarning("Observation Requise", 
                    "Veuillez entrer vos observations pour générer une suggestion IA.");
                return;
            }
            
            // Disable button during processing
            aiSuggestButton.setDisable(true);
            aiSuggestButton.setText("⏳ Génération...");
            
            // Run AI generation in background thread
            new Thread(() -> {
                try {
                    String aiResult = analyseService.generateAIDiagnostic(observation);
                    
                    Platform.runLater(() -> {
                        resultatField.setText(aiResult);
                        aiSuggestButton.setDisable(false);
                        aiSuggestButton.setText("🤖 Suggérer via IA");
                        AlertUtils.showSuccess("Suggestion IA", 
                            "Le diagnostic a été généré avec succès. Vous pouvez le modifier si nécessaire.");
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        aiSuggestButton.setDisable(false);
                        aiSuggestButton.setText("🤖 Suggérer via IA");
                        AlertUtils.showError("Erreur IA", ex.getMessage());
                    });
                }
            }).start();
        });
        
        HBox aiBox = new HBox(10, observationArea, aiSuggestButton);
        aiBox.setAlignment(Pos.CENTER_LEFT);
        aiBox.setStyle("-fx-padding: 5; -fx-background-color: #F3E5F5; -fx-background-radius: 5;");
        
        // ===== Weather Integration with Preview =====
        CheckBox weatherCheckBox = new CheckBox("Inclure données météo");
        weatherCheckBox.setSelected(includeWeather);
        weatherCheckBox.setTooltip(new Tooltip("Enrichir le diagnostic avec les conditions météo de la ferme"));
        weatherCheckBox.setStyle("-fx-text-fill: #1565C0;");
        
        // Weather status label with detailed info
        Label weatherStatusLabel = new Label("🌍 Météo: Sélectionnez une ferme");
        weatherStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #1565C0; -fx-font-weight: bold;");
        weatherStatusLabel.setWrapText(true);
        
        // Weather details area (shows temperature, humidity, conditions)
        TextArea weatherDetailsArea = new TextArea();
        weatherDetailsArea.setPrefRowCount(3);
        weatherDetailsArea.setEditable(false);
        weatherDetailsArea.setWrapText(true);
        weatherDetailsArea.setStyle("-fx-background-color: #E3F2FD; -fx-border-color: #90CAF9;");
        weatherDetailsArea.setVisible(false);
        weatherDetailsArea.setManaged(false);
        
        // "Voir météo" button to fetch and display weather
        Button fetchWeatherBtn = new Button("🌍 Voir météo");
        fetchWeatherBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 11px;");
        fetchWeatherBtn.setTooltip(new Tooltip("Cliquez pour voir les conditions météo actuelles de la ferme"));
        
        // Store fetched weather data for later use
        final WeatherUtils.WeatherData[] fetchedWeather = new WeatherUtils.WeatherData[1];
        
        // Update weather flag when checkbox changes
        weatherCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            includeWeather = newVal;
        });
        
        // Fetch weather button handler
        fetchWeatherBtn.setOnAction(e -> {
            Ferme selectedFerme = fermeComboBox.getValue();
            if (selectedFerme == null) {
                AlertUtils.showWarning("Sélection requise", "Veuillez sélectionner une ferme d'abord.");
                return;
            }
            
            String lieu = selectedFerme.getLieu();
            fetchWeatherBtn.setDisable(true);
            fetchWeatherBtn.setText("⏳ Chargement...");
            weatherStatusLabel.setText("🌍 Récupération météo pour: " + lieu);
            
            // Fetch weather asynchronously
            new Thread(() -> {
                try {
                    WeatherUtils.WeatherData weather = WeatherUtils.fetchWeather(lieu);
                    fetchedWeather[0] = weather;
                    
                    Platform.runLater(() -> {
                        fetchWeatherBtn.setDisable(false);
                        fetchWeatherBtn.setText("🔄 Actualiser météo");
                        
                        if (weather.success()) {
                            // Show detailed weather info
                            weatherDetailsArea.setVisible(true);
                            weatherDetailsArea.setManaged(true);
                            weatherDetailsArea.setText(
                                "📍 Lieu: " + weather.location() + "\n" +
                                "🌡️ Température: " + String.format("%.1f", weather.temperature()) + "°C\n" +
                                "💧 Humidité: " + weather.humidity() + "%\n" +
                                "☁️ Conditions: " + weather.getWeatherDescription()
                            );
                            weatherStatusLabel.setText("✅ Météo récupérée: " + weather.formatForDiagnostic());
                            weatherStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                            weatherCheckBox.setSelected(true);
                        } else {
                            weatherDetailsArea.setVisible(false);
                            weatherDetailsArea.setManaged(false);
                            weatherStatusLabel.setText("❌ Erreur: " + weather.errorMessage());
                            weatherStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #D32F2F; -fx-font-weight: bold;");
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        fetchWeatherBtn.setDisable(false);
                        fetchWeatherBtn.setText("🌍 Voir météo");
                        weatherStatusLabel.setText("❌ Erreur: " + ex.getMessage());
                        weatherStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #D32F2F;");
                    });
                }
            }).start();
        });
        
        // Update weather preview when farm is selected
        fermeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                weatherStatusLabel.setText("🌍 Météo: " + newVal.getLieu() + " (cliquez 'Voir météo')");
                weatherStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #1565C0;");
                // Reset weather details
                weatherDetailsArea.setVisible(false);
                weatherDetailsArea.setManaged(false);
                fetchWeatherBtn.setText("🌍 Voir météo");
                fetchedWeather[0] = null;
            } else {
                weatherStatusLabel.setText("🌍 Météo: Sélectionnez une ferme");
                weatherDetailsArea.setVisible(false);
                weatherDetailsArea.setManaged(false);
            }
        });

        // Error labels
        Label dateErrorLabel = createErrorLabel("Veuillez sélectionner une date");
        Label resultatErrorLabel = createErrorLabel("Le résultat est requis (min 5 caractères)");
        Label technicienErrorLabel = createErrorLabel("ID technicien invalide");
        Label fermeErrorLabel = createErrorLabel("Veuillez sélectionner une ferme");
        Label imageErrorLabel = createErrorLabel("L'URL de l'image est requise (US11/US6)");

        // Add fields to grid
        int row = 0;
        grid.add(new Label("Date *"), 0, row);
        grid.add(datePicker, 1, row);
        grid.add(dateErrorLabel, 1, row + 1);
        row += 2;

        // AI Suggestion section (US8)
        grid.add(new Label("Suggestions IA (US8)"), 0, row);
        grid.add(aiBox, 1, row);
        row += 2;

        grid.add(new Label("Résultat technique *"), 0, row);
        grid.add(resultatField, 1, row);
        grid.add(resultatErrorLabel, 1, row + 1);
        row += 2;

        grid.add(new Label("ID Technicien *"), 0, row);
        grid.add(idTechnicienSpinner, 1, row);
        grid.add(technicienErrorLabel, 1, row + 1);
        row += 2;

        grid.add(new Label("Ferme de destination *"), 0, row);
        VBox fermeBox = new VBox(5, fermeComboBox, fetchWeatherBtn, weatherDetailsArea, weatherStatusLabel, weatherCheckBox);
        grid.add(fermeBox, 1, row);
        grid.add(fermeErrorLabel, 1, row + 1);
        row += 2;

        grid.add(new Label("Image *"), 0, row);
        grid.add(imageBox, 1, row);
        grid.add(imageErrorLabel, 1, row + 1);
        row += 2;

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

        // Prevent dialog from closing when validation fails
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validateFields(datePicker, resultatField, idTechnicienSpinner, fermeComboBox, imageUrlField,
                    dateErrorLabel, resultatErrorLabel, technicienErrorLabel, fermeErrorLabel, imageErrorLabel)) {
                event.consume(); // Prevent dialog from closing
            }
        });

        // Convert the result to Analyse when save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Validation already done in event filter, just create the analyse
                analyse.setDateAnalyse(LocalDateTime.of(datePicker.getValue(), LocalTime.now()));
                analyse.setResultatTechnique(resultatField.getText().trim());
                analyse.setIdTechnicien(idTechnicienSpinner.getValue());
                // Get the selected ferme's ID
                Ferme selectedFerme = fermeComboBox.getValue();
                if (selectedFerme != null) {
                    analyse.setIdFerme(selectedFerme.getIdFerme());
                }
                analyse.setImageUrl(imageUrlField.getText().trim());

                return analyse;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    /**
     * Load fermes from database into ComboBox
     */
    private void loadFermes(ComboBox<Ferme> fermeComboBox) {
        try {
            List<Ferme> fermes = fermeService.selectAll();
            fermesList.addAll(fermes);
            fermeComboBox.setItems(fermesList);

            // In edit mode, select the current ferme
            if (isEditMode) {
                for (Ferme f : fermes) {
                    if (f.getIdFerme() == analyse.getIdFerme()) {
                        fermeComboBox.setValue(f);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des fermes: " + e.getMessage());
            // Show error in ComboBox
            fermeComboBox.setPromptText("Erreur de chargement des fermes");
        }
    }

    /**
     * Date format constants for validation (Séance 7 compliance)
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String DATE_REGEX = "^([0-2][0-9]|3[0-1])/(0[1-9]|1[0-2])/([0-9]{4})$";

    /**
     * Validate date with multiple checks (US11 - Séance 7)
     * - Null check
     * - Future date check
     * - Format validation (if manual input)
     * - Reasonable past date check (not before 2000)
     */
    private boolean validateDate(DatePicker datePicker, Label dateErrorLabel) {
        // Check null
        if (datePicker.getValue() == null) {
            dateErrorLabel.setText("Veuillez sélectionner une date");
            showError(dateErrorLabel);
            return false;
        }
        
        LocalDate selectedDate = datePicker.getValue();
        LocalDate today = LocalDate.now();
        
        // Check future date
        if (selectedDate.isAfter(today)) {
            dateErrorLabel.setText("La date ne peut pas être dans le futur");
            showError(dateErrorLabel);
            return false;
        }
        
        // Check reasonable past (not before year 2000)
        if (selectedDate.getYear() < 2000) {
            dateErrorLabel.setText("La date doit être après le 01/01/2000");
            showError(dateErrorLabel);
            return false;
        }
        
        // Validate date is parseable (additional safety check)
        try {
            String formattedDate = selectedDate.format(DATE_FORMATTER);
            if (!formattedDate.matches(DATE_REGEX)) {
                dateErrorLabel.setText("Format de date invalide (dd/MM/yyyy)");
                showError(dateErrorLabel);
                return false;
            }
        } catch (DateTimeParseException e) {
            dateErrorLabel.setText("Erreur de format de date");
            showError(dateErrorLabel);
            return false;
        }
        
        hideError(dateErrorLabel);
        return true;
    }

    /**
     * Validate all fields (US11)
     */
    private boolean validateFields(DatePicker datePicker, TextField resultatField,
            Spinner<Integer> idTechnicienSpinner, ComboBox<Ferme> fermeComboBox,
            TextField imageUrlField,
            Label dateErrorLabel, Label resultatErrorLabel,
            Label technicienErrorLabel, Label fermeErrorLabel,
            Label imageErrorLabel) {
        boolean isValid = true;

        // Validate date (enhanced with DateTimeFormatter and regex - Séance 7)
        if (!validateDate(datePicker, dateErrorLabel)) {
            isValid = false;
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

        // Validate ferme selection
        Ferme selectedFerme = fermeComboBox.getValue();
        if (selectedFerme == null) {
            fermeErrorLabel.setText("Veuillez sélectionner une ferme de destination");
            showError(fermeErrorLabel);
            fermeComboBox.setStyle("-fx-border-color: #D32F2F; -fx-border-width: 2px;");
            isValid = false;
        } else {
            hideError(fermeErrorLabel);
            fermeComboBox.setStyle("-fx-border-color: transparent;");
        }

        // Validate image URL (US6 and US11)
        if (imageUrlField != null && (imageUrlField.getText() == null || imageUrlField.getText().trim().isEmpty())) {
            showError(imageErrorLabel);
            imageUrlField.setStyle("-fx-border-color: #D32F2F; -fx-border-width: 2px;");
            isValid = false;
        } else if (imageUrlField != null) {
            hideError(imageErrorLabel);
            imageUrlField.setStyle("-fx-border-color: transparent;");
        }

        if (!isValid) {
            // Show global error alert
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Requise");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez corriger les erreurs en rouge avant d'enregistrer.");
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
