package tn.esprit.farmai.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.farmai.models.Role;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.UserService;
import tn.esprit.farmai.utils.NavigationUtil;
import tn.esprit.farmai.utils.NotificationManager;
import tn.esprit.farmai.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for user list/management view.
 */
public class UserListController implements Initializable {

    @FXML
    private ListView<User> userListView;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> roleFilterComboBox;
    @FXML
    private Button addUserButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Button auditButton;
    @FXML
    private Label totalUsersLabel;

    // Sidebar Elements
    @FXML
    private Label welcomeLabel;
    @FXML
    private ImageView profileImageView;
    @FXML
    private Label userRoleLabel;

    // Header Elements
    @FXML
    private ImageView headerAvatarImageView;
    @FXML
    private Label notificationBadge;

    private final UserService userService;
    private ObservableList<User> userList;

    public UserListController() {
        this.userService = new UserService();
        this.userList = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupListView();
        setupRoleFilter();
        setupSearch();
        updateUserSessionUI();

        // Notification Badge Setup
        updateNotificationBadge();
        NotificationManager.getNotifications().addListener((ListChangeListener<String>) c -> updateNotificationBadge());

        loadUsers();
    }

    private void updateUserSessionUI() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (welcomeLabel != null)
                welcomeLabel.setText(currentUser.getFullName());
            if (userRoleLabel != null)
                userRoleLabel.setText(currentUser.getRole().getDisplayName()); // Assuming Role has logic or plain text

            // Update both sidebar and header avatars using ProfileManager
            tn.esprit.farmai.utils.ProfileManager.loadUserImageIntoImageView(profileImageView, currentUser);
            tn.esprit.farmai.utils.ProfileManager.loadUserImageIntoImageView(headerAvatarImageView, currentUser);
        }
    }

    // Unified profile image loading via ProfileManager

    private void setupListView() {
        userListView.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);

                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox card = new HBox(15);
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.getStyleClass().add("content-card"); // Reuse card styling
                    card.setStyle("-fx-padding: 15px; -fx-background-radius: 12px;");

                    ImageView avatar = new ImageView();
                    avatar.setFitWidth(50);
                    avatar.setFitHeight(50);

                    // Use ProfileManager for consistent image loading
                    tn.esprit.farmai.utils.ProfileManager.loadUserImageIntoImageView(avatar, user);

                    VBox infoBox = new VBox(5);
                    Label nameLabel = new Label(user.getFullName());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #263238;");
                    Label emailLabel = new Label(user.getEmail());
                    emailLabel.setStyle("-fx-text-fill: #78909C; -fx-font-size: 13px;");
                    infoBox.getChildren().addAll(nameLabel, emailLabel);
                    HBox.setHgrow(infoBox, Priority.ALWAYS);

                    VBox detailsBox = new VBox(5);
                    detailsBox.setAlignment(Pos.CENTER_RIGHT);
                    detailsBox.setPrefWidth(120);
                    Label roleLabel = new Label(user.getRole().getDisplayName());
                    roleLabel.setStyle("-fx-font-weight: bold; -fx-padding: 4px 10px; -fx-background-radius: 15px; "
                            + getRoleStyle(user.getRole()));
                    Label phoneLabel = new Label(user.getTelephone());
                    phoneLabel.setStyle("-fx-text-fill: #546E7A; -fx-font-size: 12px;");
                    detailsBox.getChildren().addAll(roleLabel, phoneLabel);

                    HBox actionsBox = new HBox(8);
                    actionsBox.setAlignment(Pos.CENTER_RIGHT);

                    // Improved icons using Unicode symbols that are widely supported
                    Button editBtn = new Button("\u270E"); // ✎ Lower Right Pencil
                    editBtn.getStyleClass().add("action-btn");
                    editBtn.setTooltip(new Tooltip("Modifier l'utilisateur"));
                    editBtn.setOnAction(e -> handleEditUser(user));

                    Button deleteBtn = new Button("\uD83D\uDDD1"); // 🗑 Wastebasket
                    deleteBtn.getStyleClass().add("danger-btn");
                    deleteBtn.setTooltip(new Tooltip("Supprimer l'utilisateur"));
                    deleteBtn.setOnAction(e -> handleDeleteUser(user));

                    actionsBox.getChildren().addAll(editBtn, deleteBtn);

                    card.getChildren().addAll(avatar, infoBox, detailsBox, actionsBox);
                    setGraphic(card);
                }
            }
        });
    }

    private String getRoleStyle(Role role) {
        if (role == null)
            return "";
        switch (role) {
            case ADMIN:
                return "-fx-background-color: #FFEBEE; -fx-text-fill: #D32F2F;";
            case EXPERT:
                return "-fx-background-color: #E3F2FD; -fx-text-fill: #1976D2;";
            case AGRICOLE:
                return "-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32;";
            case FOURNISSEUR:
                return "-fx-background-color: #FFF3E0; -fx-text-fill: #EF6C00;";
            default:
                return "";
        }
    }

    private void setupRoleFilter() {
        if (roleFilterComboBox != null) {
            ObservableList<String> roles = FXCollections.observableArrayList();
            roles.add("Tous les rôles");
            for (Role role : Role.values())
                roles.add(role.getDisplayName());
            roleFilterComboBox.setItems(roles);
            roleFilterComboBox.setValue("Tous les rôles");
            roleFilterComboBox.setOnAction(event -> filterUsers());
        }
    }

    private void setupSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterUsers());
        }
    }

    @FXML
    private void loadUsers() {
        try {
            List<User> users = userService.selectALL();
            userList.setAll(users);
            userListView.setItems(userList);
            updateTotalLabel();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterUsers() {
        String searchText = searchField != null ? searchField.getText().toLowerCase() : "";
        String selectedRole = roleFilterComboBox != null ? roleFilterComboBox.getValue() : "Tous les rôles";
        try {
            List<User> allUsers = userService.selectALL();
            ObservableList<User> filteredList = FXCollections.observableArrayList();
            for (User user : allUsers) {
                boolean matchesSearch = searchText.isEmpty() ||
                        user.getNom().toLowerCase().contains(searchText) ||
                        user.getPrenom().toLowerCase().contains(searchText) ||
                        user.getEmail().toLowerCase().contains(searchText) ||
                        user.getCin().toLowerCase().contains(searchText);
                boolean matchesRole = selectedRole.equals("Tous les rôles") ||
                        (user.getRole() != null && user.getRole().getDisplayName().equals(selectedRole));
                if (matchesSearch && matchesRole)
                    filteredList.add(user);
            }
            userList.setAll(filteredList);
            updateTotalLabel();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddUser() {
        showUserFormDialog(null);
    }

    @FXML
    private void handleProfile() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null)
            showUserFormDialog(currentUser);
    }

    private void handleEditUser(User user) {
        showUserFormDialog(user);
    }

    private void handleDeleteUser(User user) {
        if (user.getIdUser() == SessionManager.getInstance().getCurrentUser().getIdUser()) {
            NavigationUtil.showWarning("Action interdite", "Vous ne pouvez pas supprimer votre propre compte.");
            return;
        }
        Optional<ButtonType> result = NavigationUtil.showConfirmation("Confirmer la suppression",
                "Êtes-vous sûr de vouloir supprimer l'utilisateur " + user.getFullName() + "?");
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userService.deleteOne(user);
                NotificationManager.addNotification("Utilisateur supprimé: " + user.getFullName());
                loadUsers();
                NavigationUtil.showSuccess("Succès", "Utilisateur supprimé avec succès.");
            } catch (SQLException e) {
                NavigationUtil.showError("Erreur", "Impossible de supprimer l'utilisateur.");
                e.printStackTrace();
            }
        }
    }

    private void showUserFormDialog(User user) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(user == null ? "Ajouter un utilisateur" : "Modifier l'utilisateur");
        dialog.setHeaderText("Informations de l'utilisateur");
        try {
            dialog.getDialogPane().getStylesheets()
                    .add(getClass().getResource("/tn/esprit/farmai/styles/dashboard.css").toExternalForm());
        } catch (Exception e) {
        }
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField nomField = new TextField(user != null ? user.getNom() : "");
        nomField.setPromptText("Nom");
        TextField prenomField = new TextField(user != null ? user.getPrenom() : "");
        prenomField.setPromptText("Prénom");
        TextField emailField = new TextField(user != null ? user.getEmail() : "");
        emailField.setPromptText("Email");
        TextField cinField = new TextField(user != null ? user.getCin() : "");
        cinField.setPromptText("CIN");
        TextField telephoneField = new TextField(user != null ? user.getTelephone() : "");
        telephoneField.setPromptText("Téléphone");
        TextArea adresseField = new TextArea(user != null ? user.getAdresse() : "");
        adresseField.setPromptText("Adresse");
        adresseField.setPrefRowCount(2);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        ComboBox<Role> roleComboBox = new ComboBox<>(FXCollections.observableArrayList(Role.values()));
        roleComboBox.setValue(user != null ? user.getRole() : Role.AGRICOLE);

        ImageView imagePreview = new ImageView();
        imagePreview.setFitWidth(80);
        imagePreview.setFitHeight(80);
        imagePreview.setPreserveRatio(true);
        if (user != null) {
            tn.esprit.farmai.utils.ProfileManager.loadUserImageIntoImageView(imagePreview, user);
        }

        Button uploadImageBtn = new Button("Choisir Photo");
        uploadImageBtn.getStyleClass().add("secondary-btn");
        final StringBuilder selectedImagePath = new StringBuilder(
                user != null ? (user.getImageUrl() != null ? user.getImageUrl() : "") : "");

        uploadImageBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir une image");
            fileChooser.getExtensionFilters()
                    .add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
            File selectedFile = fileChooser.showOpenDialog(dialog.getOwner());
            if (selectedFile != null) {
                selectedImagePath.setLength(0);
                selectedImagePath.append(selectedFile.getAbsolutePath());
                imagePreview.setImage(new Image(selectedFile.toURI().toString()));
            }
        });

        VBox imageBox = new VBox(5, imagePreview, uploadImageBtn);
        imageBox.setAlignment(Pos.CENTER);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(imageBox, 0, 0, 1, 3);
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
        grid.add(new Label("Rôle:"), 1, 7);
        grid.add(roleComboBox, 2, 7);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                User resultUser = user != null ? user : new User();
                resultUser.setNom(nomField.getText().trim());
                resultUser.setPrenom(prenomField.getText().trim());
                resultUser.setEmail(emailField.getText().trim());
                resultUser.setCin(cinField.getText().trim());
                resultUser.setTelephone(telephoneField.getText().trim());
                resultUser.setAdresse(adresseField.getText().trim());
                resultUser.setRole(roleComboBox.getValue());
                resultUser.setImageUrl(selectedImagePath.toString());
                if (!passwordField.getText().isEmpty())
                    resultUser.setPassword(passwordField.getText());
                return resultUser;
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(resultUser -> {
            try {
                if (user == null) {
                    if (resultUser.getPassword() == null || resultUser.getPassword().isEmpty()) {
                        NavigationUtil.showError("Erreur", "Mot de passe requis.");
                        return;
                    }
                    userService.insertOne(resultUser);
                    NotificationManager.addNotification("Nouvel utilisateur ajouté: " + resultUser.getFullName());
                    NavigationUtil.showSuccess("Succès", "Utilisateur ajouté.");
                } else {
                    userService.updateOne(resultUser);
                    if (resultUser.getPassword() != null && !resultUser.getPassword().isEmpty()) {
                        userService.updatePassword(resultUser.getIdUser(), resultUser.getPassword());
                    }
                    NotificationManager.addNotification("Utilisateur modifié: " + resultUser.getFullName());
                    NavigationUtil.showSuccess("Succès", "Utilisateur modifié.");
                    if (SessionManager.getInstance().getCurrentUser().getIdUser() == resultUser.getIdUser()) {
                        SessionManager.getInstance().setCurrentUser(resultUser);
                        updateUserSessionUI();
                    }
                }
                loadUsers();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void updateTotalLabel() {
        if (totalUsersLabel != null)
            totalUsersLabel.setText("Total: " + userList.size() + " utilisateur(s)");
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) userListView.getScene().getWindow();
        NavigationUtil.logout(stage);
    }

    @FXML
    private void handleBackToDashboard() {
        Stage stage = (Stage) userListView.getScene().getWindow();
        NavigationUtil.navigateToDashboard(stage);
    }

    @FXML
    private void handleAudit() {
        Stage stage = (Stage) userListView.getScene().getWindow();
        NavigationUtil.navigateToAudit(stage);
    }

    // --- Notification Logic ---
    private void updateNotificationBadge() {
        int count = NotificationManager.getUnreadCount();
        if (notificationBadge != null) {
            if (count > 0) {
                notificationBadge.setText(String.valueOf(count));
                notificationBadge.setVisible(true);
            } else {
                notificationBadge.setVisible(false);
            }
        }
    }

    @FXML
    private void handleNotifications() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/farmai/views/notifications.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initOwner(userListView.getScene().getWindow());
            stage.setTitle("Notifications");
            stage.setScene(new Scene(root));

            // Re-apply common stylesheet if needed (usually view has it)

            stage.showAndWait();

            // After close, update badge
            updateNotificationBadge();
        } catch (IOException e) {
            e.printStackTrace();
            NavigationUtil.showError("Erreur", "Impossible d'ouvrir les notifications.");
        }
    }
}
