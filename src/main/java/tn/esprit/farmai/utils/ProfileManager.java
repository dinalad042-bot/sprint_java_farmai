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

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;

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
        passwordField.setPromptText("Nouveau mot de passe (facultatif)");

        // Image Selection Elements
        ImageView imagePreview = new ImageView();
        imagePreview.setFitWidth(100);
        imagePreview.setFitHeight(100);
        imagePreview.setPreserveRatio(true);

        // Initial image loading
        loadUserImageIntoImageView(imagePreview, currentUser);

        Button uploadBtn = new Button("Changer Photo");
        uploadBtn.getStyleClass().add("secondary-btn");
        final StringBuilder selectedImagePath = new StringBuilder(
                currentUser.getImageUrl() != null ? currentUser.getImageUrl() : "");

        uploadBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir une photo de profil");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File file = fileChooser.showOpenDialog(owner);
            if (file != null) {
                selectedImagePath.setLength(0);
                selectedImagePath.append(file.getAbsolutePath());
                imagePreview.setImage(new Image(file.toURI().toString()));
                // Re-apply clip in case ImageView properties reset
                double radius = 50;
                imagePreview.setClip(new Circle(radius, radius, radius));
            }
        });

        javafx.scene.layout.VBox imageBox = new javafx.scene.layout.VBox(10, imagePreview, uploadBtn);
        imageBox.setAlignment(javafx.geometry.Pos.CENTER);
        imageBox.setPadding(new Insets(0, 20, 0, 0));

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 20, 20));

        grid.add(imageBox, 0, 0, 1, 7);
        grid.add(new Label("Nom:"), 1, 0);
        grid.add(nomField, 2, 0);
        grid.add(new Label("Prénom:"), 1, 1);
        grid.add(prenomField, 2, 1);
        grid.add(new Label("Email:"), 1, 2);
        grid.add(emailField, 2, 2);
        grid.add(new Label("CIN:"), 1, 3);
        grid.add(cinField, 2, 3);
        grid.add(new Label("Téléphone:"), 1, 4);
        grid.add(telephoneField, 2, 4);
        grid.add(new Label("Adresse:"), 1, 5);
        grid.add(adresseField, 2, 5);
        grid.add(new Label("Mot de passe:"), 1, 6);
        grid.add(passwordField, 2, 6);

        dialog.getDialogPane().setContent(grid);

        // Convert result to user object (with updates)
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                currentUser.setNom(nomField.getText().trim());
                currentUser.setPrenom(prenomField.getText().trim());
                currentUser.setEmail(emailField.getText().trim());
                currentUser.setCin(cinField.getText().trim());
                currentUser.setTelephone(telephoneField.getText().trim());
                currentUser.setAdresse(adresseField.getText().trim());
                currentUser.setImageUrl(selectedImagePath.toString());
                return currentUser;
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();

        if (result.isPresent()) {
            User updatedUser = result.get();
            try {
                userService.updateOne(updatedUser);
                if (!passwordField.getText().isEmpty()) {
                    userService.updatePassword(updatedUser.getIdUser(), passwordField.getText());
                }
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
            Text avatarText, javafx.scene.image.ImageView avatarImageView) {
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
        if (avatarImageView != null) {
            loadUserImageIntoImageView(avatarImageView, user);
        }
    }

    /**
     * Overloaded version for backward compatibility.
     */
    public static void updateProfileUI(User user, Label welcomeLabel, Label userNameLabel, Circle avatarCircle,
            Text avatarText) {
        updateProfileUI(user, welcomeLabel, userNameLabel, avatarCircle, avatarText, null);
    }

    /**
     * Loads a user's image into an ImageView with circular clipping.
     * Handles null/invalid cases by showing a default avatar.
     * 
     * @param imageView The ImageView to update
     * @param user      The user whose image to load
     */
    public static void loadUserImageIntoImageView(javafx.scene.image.ImageView imageView, User user) {
        if (imageView == null)
            return;

        // Apply circular clip
        double width = imageView.getFitWidth();
        double height = imageView.getFitHeight();
        if (width <= 0)
            width = 40;
        if (height <= 0)
            height = 40;

        double radius = Math.min(width, height) / 2;
        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(radius, radius, radius);
        imageView.setClip(clip);

        String imgUrl = (user != null) ? user.getImageUrl() : null;
        boolean imageLoaded = false;

        // 1. Try User Image
        if (imgUrl != null && !imgUrl.isEmpty()) {
            try {
                String pathToLoad = null;
                if (imgUrl.startsWith("http") || imgUrl.startsWith("file:") || imgUrl.startsWith("jar:")) {
                    pathToLoad = imgUrl;
                } else {
                    java.io.File file = new java.io.File(imgUrl);
                    if (file.exists()) {
                        pathToLoad = file.toURI().toString();
                    }
                }

                if (pathToLoad != null) {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(pathToLoad, true);
                    // Check for error immediately if possible, or wait if background loading
                    if (!img.isError()) {
                        imageView.setImage(img);
                        imageLoaded = true;
                    }
                }
            } catch (Exception e) {
                System.err.println("Skipping invalid image path [" + imgUrl + "]: " + e.getMessage());
            }
        }

        // 2. Fallback: UI Avatars
        if (!imageLoaded) {
            try {
                String name = (user != null)
                        ? ((user.getNom() != null ? user.getNom() : "U") + "+"
                                + (user.getPrenom() != null ? user.getPrenom() : "User"))
                        : "Default+User";

                String fallbackUrl = "https://ui-avatars.com/api/?name=" + name
                        + "&background=random&color=fff&size=256";
                javafx.scene.image.Image fallbackImg = new javafx.scene.image.Image(fallbackUrl, true);
                imageView.setImage(fallbackImg);
            } catch (Exception e) {
                // Final placeholder if all fails
                System.err.println("Failed to load fallback image: " + e.getMessage());
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
                String pathToLoad = null;
                if (imgUrl.startsWith("http") || imgUrl.startsWith("file:") || imgUrl.startsWith("jar:")) {
                    pathToLoad = imgUrl;
                } else {
                    java.io.File file = new java.io.File(imgUrl);
                    if (file.exists()) {
                        pathToLoad = file.toURI().toString();
                    }
                }

                if (pathToLoad != null) {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(pathToLoad, true);
                    if (!img.isError()) {
                        circle.setFill(new javafx.scene.paint.ImagePattern(img));
                        imageLoaded = true;
                    }
                }
            } catch (Exception e) {
                System.err.println("Skipping invalid image path [" + imgUrl + "]: " + e.getMessage());
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
