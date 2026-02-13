package tn.esprit.farmai.utils;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Window;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.UserService;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Utility class to handle user profile editing.
 */
public class ProfileManager {

    private static final UserService userService = new UserService();

    /**
     * Shows the profile edit dialog and updates the user if saved.
     * 
     * @param owner The owner window (or null)
     * @return true if the profile was updated, false otherwise.
     */
    public static boolean showProfileEditDialog(Window owner) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null)
            return false;

        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Modifier mon profil");
        dialog.setHeaderText("Mettez à jour vos informations peronnelles");
        dialog.initOwner(owner);

        // Apply CSS if available
        try {
            dialog.getDialogPane().getStylesheets().add(
                    ProfileManager.class.getResource("/tn/esprit/farmai/styles/dashboard.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load dialog CSS: " + e.getMessage());
        }

        // Set button types
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create form fields
        TextField nomField = new TextField(currentUser.getNom());
        nomField.setPromptText("Nom");

        TextField prenomField = new TextField(currentUser.getPrenom());
        prenomField.setPromptText("Prénom");

        TextField emailField = new TextField(currentUser.getEmail());
        emailField.setPromptText("Email");

        TextField cinField = new TextField(currentUser.getCin());
        cinField.setPromptText("CIN");

        TextField telephoneField = new TextField(currentUser.getTelephone());
        telephoneField.setPromptText("Téléphone");

        TextArea adresseField = new TextArea(currentUser.getAdresse());
        adresseField.setPromptText("Adresse");
        adresseField.setPrefRowCount(2);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Nouveau mot de passe (laisser vide pour garder l'ancien)");

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Prénom:"), 0, 1);
        grid.add(prenomField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("CIN:"), 0, 3);
        grid.add(cinField, 1, 3);
        grid.add(new Label("Téléphone:"), 0, 4);
        grid.add(telephoneField, 1, 4);
        grid.add(new Label("Adresse:"), 0, 5);
        grid.add(adresseField, 1, 5);
        grid.add(new Label("Mot de passe:"), 0, 6);
        grid.add(passwordField, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // Convert result to user object (with updates)
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Validation could go here

                // Update local object
                currentUser.setNom(nomField.getText().trim());
                currentUser.setPrenom(prenomField.getText().trim());
                currentUser.setEmail(emailField.getText().trim());
                currentUser.setCin(cinField.getText().trim());
                currentUser.setTelephone(telephoneField.getText().trim());
                currentUser.setAdresse(adresseField.getText().trim());

                // Only return user if we plan to save
                return currentUser;
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();

        if (result.isPresent()) {
            User updatedUser = result.get();
            try {
                // 1. Update basic info
                userService.updateOne(updatedUser);

                // 2. Update password if provided
                if (!passwordField.getText().isEmpty()) {
                    userService.updatePassword(updatedUser.getIdUser(), passwordField.getText());
                }

                // 3. Update session
                SessionManager.getInstance().setCurrentUser(updatedUser);

                NavigationUtil.showSuccess("Succès", "Profil mis à jour avec succès.");
                return true;

            } catch (SQLException e) {
                NavigationUtil.showError("Erreur", "Erreur lors de la mise à jour: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Updates common dashboard UI elements after a profile change.
     */
    public static void updateProfileUI(User user, Label welcomeLabel, Label userNameLabel, Circle avatarCircle,
            Text avatarText) {
        if (user == null)
            return;

        if (welcomeLabel != null) {
            welcomeLabel.setText("Bienvenue, " + user.getFullName() + "!");
        }
        if (userNameLabel != null) {
            userNameLabel.setText(user.getFullName());
        }
        if (avatarCircle != null) {
            boolean success = loadUserImageIntoCircle(avatarCircle, user);
            if (avatarText != null) {
                avatarText.setVisible(!success);
            }
        }
    }

    /**
     * Loads a user's image (from URL, local path, or fallback) into a Circle.
     */
    public static boolean loadUserImageIntoCircle(Circle circle, User user) {
        if (circle == null || user == null)
            return false;

        boolean imageLoaded = false;
        String imgUrl = user.getImageUrl();

        // 1. Try User Image from Path/URL
        if (imgUrl != null && !imgUrl.isEmpty()) {
            try {
                String pathToLoad = imgUrl;
                if (!imgUrl.startsWith("http") && !imgUrl.startsWith("file:")) {
                    java.io.File file = new java.io.File(imgUrl);
                    if (file.exists()) {
                        pathToLoad = file.toURI().toString();
                    }
                }
                javafx.scene.image.Image img = new javafx.scene.image.Image(pathToLoad, true);
                if (!img.isError()) {
                    circle.setFill(new javafx.scene.paint.ImagePattern(img));
                    imageLoaded = true;
                }
            } catch (Exception e) {
                System.err.println("Failed to load user image: " + e.getMessage());
            }
        }

        // 2. Fallback: UI Avatars (Online)
        if (!imageLoaded) {
            try {
                String name = (user.getNom() != null ? user.getNom() : "U") + "+"
                        + (user.getPrenom() != null ? user.getPrenom() : "User");
                String fallbackUrl = "https://ui-avatars.com/api/?name=" + name
                        + "&background=random&color=fff&size=128";
                circle.setFill(new javafx.scene.paint.ImagePattern(new javafx.scene.image.Image(fallbackUrl, true)));
                imageLoaded = true;
            } catch (Exception e) {
                // 3. Final Fallback: Simple Color
                circle.setFill(javafx.scene.paint.Color.web("#90A4AE"));
                imageLoaded = false;
            }
        }
        return imageLoaded;
    }
}
