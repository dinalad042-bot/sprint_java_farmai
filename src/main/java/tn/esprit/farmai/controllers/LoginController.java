package tn.esprit.farmai.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.UserService;
import tn.esprit.farmai.utils.NavigationUtil;
import tn.esprit.farmai.utils.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the login view.
 */
public class LoginController implements Initializable {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink signupLink;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private Label errorLabel;

    @FXML
    private CheckBox rememberMeCheckbox;

    @FXML
    private VBox loginContainer;

    @FXML
    private ProgressIndicator loadingIndicator;

    private final UserService userService;

    public LoginController() {
        this.userService = new UserService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Hide error label initially
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }

        // Hide loading indicator
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }

        // Add entrance animation
        if (loginContainer != null) {
            playEntranceAnimation();
        }

        // Add enter key handler for password field
        if (passwordField != null) {
            passwordField.setOnAction(event -> handleLogin());
        }

        // Setup button hover effects
        if (loginButton != null) {
            setupButtonEffects();
        }
    }

    /**
     * Handle login button click
     */
    @FXML
    private void handleLogin() {
        hideError();

        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Veuillez entrer un email valide.");
            return;
        }

        // Show loading
        setLoading(true);

        try {
            Optional<User> userOpt = userService.authenticate(email, password);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // Set session
                SessionManager.getInstance().setCurrentUser(user);

                // Navigate to appropriate dashboard
                Stage stage = (Stage) loginButton.getScene().getWindow();
                NavigationUtil.navigateToDashboard(stage);
            } else {
                showError("Email ou mot de passe incorrect.");
            }
        } catch (SQLException e) {
            showError("Erreur de connexion à la base de données.");
            e.printStackTrace();
        } finally {
            setLoading(false);
        }
    }

    /**
     * Handle signup link click
     */
    @FXML
    private void handleSignupLink() {
        Stage stage = (Stage) signupLink.getScene().getWindow();
        NavigationUtil.navigateToSignup(stage);
    }

    /**
     * Handle forgot password link click
     */
    @FXML
    private void handleForgotPassword() {
        // Show dialog to enter email
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Mot de passe oublié");
        dialog.setHeaderText("Réinitialisation du mot de passe");
        dialog.setContentText("Entrez votre adresse email:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(email -> {
            try {
                if (userService.emailExists(email)) {
                    NavigationUtil.showSuccess("Email envoyé",
                            "Un email de réinitialisation a été envoyé à " + email);
                } else {
                    NavigationUtil.showWarning("Email non trouvé",
                            "Aucun compte associé à cet email.");
                }
            } catch (SQLException e) {
                NavigationUtil.showError("Erreur", "Erreur lors de la vérification de l'email.");
            }
        });
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);

            // Shake animation for error
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
        if (loginButton != null) {
            loginButton.setDisable(loading);
        }
    }

    /**
     * Play entrance animation
     */
    private void playEntranceAnimation() {
        loginContainer.setOpacity(0);
        loginContainer.setTranslateY(30);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), loginContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideUp = new TranslateTransition(Duration.millis(600), loginContainer);
        slideUp.setFromY(30);
        slideUp.setToY(0);

        fadeIn.play();
        slideUp.play();
    }

    /**
     * Setup button hover effects
     */
    private void setupButtonEffects() {
        loginButton.setOnMouseEntered(e -> {
            loginButton.setStyle("-fx-scale-x: 1.02; -fx-scale-y: 1.02;");
        });
        loginButton.setOnMouseExited(e -> {
            loginButton.setStyle("-fx-scale-x: 1.0; -fx-scale-y: 1.0;");
        });
    }

    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
}
