package tn.esprit.farmai.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.OTPService;
import tn.esprit.farmai.services.UserService;
import tn.esprit.farmai.utils.MailingService;
import tn.esprit.farmai.utils.NavigationUtil;

import java.sql.SQLException;
import java.util.Optional;

public class VerificationController {

    public enum VerificationMode {
        SIGNUP, FORGOT_PASSWORD
    }

    @FXML
    private Label emailLabel;
    @FXML
    private Label errorLabel;
    @FXML
    private TextField otpField;
    @FXML
    private Button verifyButton;

    private User pendingUser;
    private String userEmail;
    private VerificationMode mode = VerificationMode.FORGOT_PASSWORD;
    private UserService userService = new UserService();

    public void setPendingUser(User user) {
        this.pendingUser = user;
        this.userEmail = user.getEmail();
        this.mode = VerificationMode.SIGNUP;
        emailLabel.setText(userEmail);
    }

    public void setUserEmail(String email) {
        this.userEmail = email;
        this.mode = VerificationMode.FORGOT_PASSWORD;
        emailLabel.setText(email);
    }

    @FXML
    private void handleVerify() {
        String enteredOtp = otpField.getText().trim();
        if (enteredOtp.isEmpty()) {
            showError("Veuillez entrer le code OTP.");
            return;
        }

        if (OTPService.verifyOTP(userEmail, enteredOtp)) {
            if (mode == VerificationMode.SIGNUP) {
                handleSignupVerification();
            } else {
                handleForgotPasswordVerification();
            }
        } else {
            showError("Code OTP incorrect ou expiré.");
        }
    }

    private void handleSignupVerification() {
        try {
            userService.insertOne(pendingUser);
            NavigationUtil.showSuccess("Succès", "Votre compte a été créé avec succès !");
            NavigationUtil.navigateToLogin((Stage) verifyButton.getScene().getWindow());
        } catch (SQLException e) {
            showError("Erreur lors de la création du compte : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleForgotPasswordVerification() {
        // Show password reset dialog
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Réinitialisation");
        dialog.setHeaderText("Nouveau mot de passe");
        dialog.setContentText("Entrez votre nouveau mot de passe:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPassword -> {
            if (newPassword.length() < 6) {
                showError("Le mot de passe doit contenir au moins 6 caractères.");
                return;
            }
            try {
                User user = userService.findByEmail(userEmail).orElseThrow();
                userService.updatePassword(user.getIdUser(), newPassword);
                NavigationUtil.showSuccess("Succès", "Votre mot de passe a été réinitialisé.");
                NavigationUtil.navigateToLogin((Stage) verifyButton.getScene().getWindow());
            } catch (Exception e) {
                showError("Erreur lors de la réinitialisation : " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleResend() {
        String otp = OTPService.generateOTP(userEmail);
        MailingService.sendMail(userEmail, "Votre nouveau code OTP",
                "Votre nouveau code de vérification est : " + otp);
        NavigationUtil.showSuccess("Code envoyé", "Un nouveau code a été envoyé à " + userEmail);
        errorLabel.setVisible(false);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
