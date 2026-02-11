package tn.esprit.farmai.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.farmai.models.Role;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.UserService;
import tn.esprit.farmai.utils.NavigationUtil;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Controller for the signup view.
 */
public class SignupController implements Initializable {

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField cinField;

    @FXML
    private TextField telephoneField;

    @FXML
    private TextArea adresseField;

    @FXML
    private ComboBox<Role> roleComboBox;

    @FXML
    private Button signupButton;

    @FXML
    private Hyperlink loginLink;

    @FXML
    private Label errorLabel;

    @FXML
    private VBox signupContainer;

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private CheckBox termsCheckbox;

    private final UserService userService;

    public SignupController() {
        this.userService = new UserService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize role combo box
        if (roleComboBox != null) {
            roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));
            roleComboBox.setValue(Role.AGRICOLE); // Default role

            // Custom cell factory for display names
            roleComboBox.setCellFactory(param -> new ListCell<Role>() {
                @Override
                protected void updateItem(Role item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getDisplayName());
                    }
                }
            });

            roleComboBox.setButtonCell(new ListCell<Role>() {
                @Override
                protected void updateItem(Role item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getDisplayName());
                    }
                }
            });
        }

        // Hide error label initially
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }

        // Hide loading indicator
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }

        // Add entrance animation
        if (signupContainer != null) {
            playEntranceAnimation();
        }

        // Setup real-time validation
        setupValidation();
    }

    /**
     * Handle signup button click
     */
    @FXML
    private void handleSignup() {
        hideError();

        // Validate all fields
        if (!validateFields()) {
            return;
        }

        // Check terms acceptance
        if (termsCheckbox != null && !termsCheckbox.isSelected()) {
            showError("Veuillez accepter les conditions d'utilisation.");
            return;
        }

        setLoading(true);

        try {
            // Check if email already exists
            if (userService.emailExists(emailField.getText().trim())) {
                showError("Cet email est déjà utilisé.");
                setLoading(false);
                return;
            }

            // Check if CIN already exists
            if (userService.cinExists(cinField.getText().trim())) {
                showError("Ce CIN est déjà enregistré.");
                setLoading(false);
                return;
            }

            // Create new user
            User newUser = new User();
            newUser.setNom(nomField.getText().trim());
            newUser.setPrenom(prenomField.getText().trim());
            newUser.setEmail(emailField.getText().trim());
            newUser.setPassword(passwordField.getText());
            newUser.setCin(cinField.getText().trim());
            newUser.setTelephone(telephoneField.getText().trim());
            newUser.setAdresse(adresseField != null ? adresseField.getText().trim() : "");
            newUser.setRole(roleComboBox.getValue());

            // Insert user
            userService.insertOne(newUser);

            // Show success and navigate to login
            NavigationUtil.showSuccess("Inscription réussie",
                    "Votre compte a été créé avec succès. Vous pouvez maintenant vous connecter.");

            Stage stage = (Stage) signupButton.getScene().getWindow();
            NavigationUtil.navigateToLogin(stage);

        } catch (SQLException e) {
            showError("Erreur lors de l'inscription. Veuillez réessayer.");
            e.printStackTrace();
        } finally {
            setLoading(false);
        }
    }

    /**
     * Handle login link click
     */
    @FXML
    private void handleLoginLink() {
        Stage stage = (Stage) loginLink.getScene().getWindow();
        NavigationUtil.navigateToLogin(stage);
    }

    /**
     * Validate all form fields
     */
    private boolean validateFields() {
        // Required fields
        if (nomField.getText().trim().isEmpty()) {
            showError("Le nom est requis.");
            nomField.requestFocus();
            return false;
        }

        if (prenomField.getText().trim().isEmpty()) {
            showError("Le prénom est requis.");
            prenomField.requestFocus();
            return false;
        }

        if (emailField.getText().trim().isEmpty()) {
            showError("L'email est requis.");
            emailField.requestFocus();
            return false;
        }

        if (!isValidEmail(emailField.getText().trim())) {
            showError("Veuillez entrer un email valide.");
            emailField.requestFocus();
            return false;
        }

        if (cinField.getText().trim().isEmpty()) {
            showError("Le CIN est requis.");
            cinField.requestFocus();
            return false;
        }

        if (!isValidCin(cinField.getText().trim())) {
            showError("Le CIN doit contenir 8 chiffres.");
            cinField.requestFocus();
            return false;
        }

        if (passwordField.getText().isEmpty()) {
            showError("Le mot de passe est requis.");
            passwordField.requestFocus();
            return false;
        }

        if (passwordField.getText().length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères.");
            passwordField.requestFocus();
            return false;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Les mots de passe ne correspondent pas.");
            confirmPasswordField.requestFocus();
            return false;
        }

        if (telephoneField.getText().trim().length() > 0 &&
                !isValidPhone(telephoneField.getText().trim())) {
            showError("Numéro de téléphone invalide.");
            telephoneField.requestFocus();
            return false;
        }

        if (roleComboBox.getValue() == null) {
            showError("Veuillez sélectionner un rôle.");
            return false;
        }

        return true;
    }

    /**
     * Setup real-time validation listeners
     */
    private void setupValidation() {
        // Email validation
        if (emailField != null) {
            emailField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal && !emailField.getText().isEmpty()) {
                    if (!isValidEmail(emailField.getText())) {
                        emailField.setStyle("-fx-border-color: #e74c3c;");
                    } else {
                        emailField.setStyle("-fx-border-color: #27ae60;");
                    }
                }
            });
        }

        // Password match validation
        if (confirmPasswordField != null) {
            confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.isEmpty()) {
                    if (newVal.equals(passwordField.getText())) {
                        confirmPasswordField.setStyle("-fx-border-color: #27ae60;");
                    } else {
                        confirmPasswordField.setStyle("-fx-border-color: #e74c3c;");
                    }
                }
            });
        }

        // CIN validation (8 digits)
        if (cinField != null) {
            cinField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d*")) {
                    cinField.setText(oldVal);
                }
                if (newVal.length() > 8) {
                    cinField.setText(oldVal);
                }
            });
        }

        // Phone validation
        if (telephoneField != null) {
            telephoneField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("[\\d+]*")) {
                    telephoneField.setText(oldVal);
                }
            });
        }
    }

    /**
     * Show error message with animation
     */
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);

            TranslateTransition shake = new TranslateTransition(Duration.millis(50), errorLabel);
            shake.setFromX(0);
            shake.setByX(10);
            shake.setCycleCount(6);
            shake.setAutoReverse(true);
            shake.play();
        }
    }

    /**
     * Hide error message
     */
    private void hideError() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
    }

    /**
     * Set loading state
     */
    private void setLoading(boolean loading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(loading);
        }
        if (signupButton != null) {
            signupButton.setDisable(loading);
        }
    }

    /**
     * Play entrance animation
     */
    private void playEntranceAnimation() {
        signupContainer.setOpacity(0);
        signupContainer.setTranslateY(30);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), signupContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideUp = new TranslateTransition(Duration.millis(600), signupContainer);
        slideUp.setFromY(30);
        slideUp.setToY(0);

        fadeIn.play();
        slideUp.play();
    }

    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    /**
     * Validate CIN format (8 digits)
     */
    private boolean isValidCin(String cin) {
        return cin.matches("\\d{8}");
    }

    /**
     * Validate phone format
     */
    private boolean isValidPhone(String phone) {
        return phone.matches("[+]?\\d{8,15}");
    }
}
