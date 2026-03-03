package tn.esprit.farmai.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.UserService;

import java.io.File;
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
        dialog.setHeaderText("Mettez à jour vos informations personnelles");
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

        // ===== IMAGE UPLOAD SECTION =====
        // Image preview
        ImageView imagePreview = new ImageView();
        imagePreview.setFitWidth(100);
        imagePreview.setFitHeight(100);
        imagePreview.setPreserveRatio(true);
        
        // Load current user image if exists
        StringBuilder selectedImagePath = new StringBuilder(
                currentUser.getImageUrl() != null ? currentUser.getImageUrl() : "");
        if (!selectedImagePath.toString().isEmpty()) {
            try {
                String path = selectedImagePath.toString();
                if (!path.startsWith("http") && !path.startsWith("file:")) {
                    File file = new File(path);
                    if (file.exists()) {
                        path = file.toURI().toString();
                    }
                }
                imagePreview.setImage(new Image(path, true));
            } catch (Exception e) {
                // Image failed to load, use fallback
            }
        }
        
        // Image upload button
        Button uploadImageBtn = new Button("📷 Choisir Photo");
        uploadImageBtn.getStyleClass().add("secondary-btn");
        uploadImageBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir une photo de profil");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File selectedFile = fileChooser.showOpenDialog(dialog.getOwner());
            if (selectedFile != null) {
                selectedImagePath.setLength(0);
                selectedImagePath.append(selectedFile.getAbsolutePath());
                imagePreview.setImage(new Image(selectedFile.toURI().toString()));
            }
        });
        
        VBox imageBox = new VBox(10, imagePreview, uploadImageBtn);
        imageBox.setAlignment(Pos.CENTER);
        imageBox.setPadding(new Insets(0, 0, 10, 0));
        // ===== END IMAGE UPLOAD SECTION =====

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
        grid.setPadding(new Insets(20, 20, 10, 20));

        // Add image upload section spanning 2 columns at top
        grid.add(imageBox, 0, 0, 2, 1);
        
        grid.add(new Label("Nom:"), 0, 1);
        grid.add(nomField, 1, 1);
        grid.add(new Label("Prénom:"), 0, 2);
        grid.add(prenomField, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("CIN:"), 0, 4);
        grid.add(cinField, 1, 4);
        grid.add(new Label("Téléphone:"), 0, 5);
        grid.add(telephoneField, 1, 5);
        grid.add(new Label("Adresse:"), 0, 6);
        grid.add(adresseField, 1, 6);
        grid.add(new Label("Mot de passe:"), 0, 7);
        grid.add(passwordField, 1, 7);

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
                
                // Update image URL if changed
                if (selectedImagePath.length() > 0) {
                    currentUser.setImageUrl(selectedImagePath.toString());
                }

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
     * Delegates to AvatarUtil for consistent avatar handling.
     */
    public static boolean loadUserImageIntoCircle(Circle circle, User user) {
        return AvatarUtil.loadUserImageIntoCircle(circle, user);
    }

    /**
     * Loads a user's image (from URL, local path, or fallback) into an ImageView.
     * Delegates to AvatarUtil for consistent avatar handling.
     */
    public static boolean loadUserImageIntoImageView(ImageView imageView, User user) {
        return AvatarUtil.loadUserImageIntoImageView(imageView, user, 100);
    }
}
