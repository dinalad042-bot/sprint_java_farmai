package tn.esprit.farmai.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.models.UserLog;
import tn.esprit.farmai.models.UserLogAction;
import tn.esprit.farmai.services.UserLogService;
import tn.esprit.farmai.utils.NavigationUtil;
import tn.esprit.farmai.utils.ProfileManager;
import tn.esprit.farmai.utils.SessionManager;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Audit Logs view using ListView.
 */
public class UserLogController implements Initializable {

    @FXML
    private ListView<UserLog> logListView;

    // Sidebar/Header elements
    @FXML
    private Label welcomeLabel;
    @FXML
    private ImageView profileImageView;
    @FXML
    private Label userRoleLabel;
    @FXML
    private ImageView headerAvatarImageView;

    private final UserLogService userLogService;
    private final ObservableList<UserLog> logList;

    public UserLogController() {
        this.userLogService = new UserLogService();
        this.logList = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            setupListView();
            updateUserSessionUI();
            loadLogs();
        } catch (Exception e) {
            System.err.println("Error initializing UserLogController: " + e.getMessage());
            e.printStackTrace();
            // Show error but don't crash the UI
            NavigationUtil.showError("Erreur d'initialisation", 
                "Erreur lors du chargement des logs: " + e.getMessage());
        }
    }

    private void setupListView() {
        logListView.setCellFactory(param -> new ListCell<UserLog>() {
            @Override
            protected void updateItem(UserLog log, boolean empty) {
                super.updateItem(log, empty);

                if (empty || log == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox card = new HBox(20);
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-padding: 15px; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

                    // 1. Action Badge
                    Label actionBadge = new Label(log.getActionType().name());
                    actionBadge.setPrefWidth(85);
                    actionBadge.setAlignment(Pos.CENTER);
                    actionBadge.setStyle("-fx-font-weight: bold; -fx-padding: 5px 10px; -fx-background-radius: 15px; "
                            + getActionStyle(log.getActionType()));

                    // 2. Info Section (Who and Whom)
                    VBox whoBox = new VBox(3);
                    whoBox.setPrefWidth(220);
                    Label performedByLabel = new Label("Par: " + log.getPerformedBy());
                    performedByLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #263238; -fx-font-size: 14px;");
                    Label targetLabel = new Label("ID Utilisateur concerné: ******");
                    targetLabel.setStyle("-fx-text-fill: #78909C; -fx-font-size: 12px;");
                    whoBox.getChildren().addAll(performedByLabel, targetLabel);

                    // 3. Timestamp
                    VBox timeBox = new VBox(3);
                    timeBox.setPrefWidth(150);
                    Label dateLabel = new Label(log.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    dateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #546E7A;");
                    Label hourLabel = new Label(log.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    hourLabel.setStyle("-fx-text-fill: #90A4AE;");
                    timeBox.getChildren().addAll(dateLabel, hourLabel);

                    // 4. Description
                    Label descLabel = new Label(log.getDescription());
                    descLabel.setStyle("-fx-text-fill: #455A64; -fx-italic: true;");
                    descLabel.setWrapText(true);
                    HBox.setHgrow(descLabel, Priority.ALWAYS);

                    card.getChildren().addAll(actionBadge, whoBox, timeBox, descLabel);
                    setGraphic(card);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5px 0;");
                }
            }
        });
    }

    private String getActionStyle(UserLogAction action) {
        switch (action) {
            case CREATE:
                return "-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32;";
            case UPDATE:
                return "-fx-background-color: #FFF3E0; -fx-text-fill: #EF6C00;";
            case DELETE:
                return "-fx-background-color: #FFEBEE; -fx-text-fill: #D32F2F;";
            default:
                return "";
        }
    }

    private void updateUserSessionUI() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText(currentUser.getFullName());
            userRoleLabel.setText(currentUser.getRole().getDisplayName());
            ProfileManager.loadUserImageIntoImageView(profileImageView, currentUser);
            ProfileManager.loadUserImageIntoImageView(headerAvatarImageView, currentUser);
        }
    }

    private void loadLogs() {
        try {
            List<UserLog> logs = userLogService.selectALL();
            logList.setAll(logs);
            logListView.setItems(logList);
        } catch (SQLException e) {
            e.printStackTrace();
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("user_log")) {
                NavigationUtil.showError("Erreur Base de Données", 
                    "La table des logs n'existe pas encore.\n" +
                    "Veuillez exécuter le script SQL pour créer la table user_log.\n\n" +
                    "Détails: " + errorMsg);
            } else {
                NavigationUtil.showError("Erreur", 
                    "Impossible de charger les logs d'audit.\n" +
                    "Détails: " + (errorMsg != null ? errorMsg : "Erreur inconnue"));
            }
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) logListView.getScene().getWindow();
        NavigationUtil.navigateToUserList(stage);
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) logListView.getScene().getWindow();
        NavigationUtil.logout(stage);
    }

    @FXML
    private void handleDashboard() {
        Stage stage = (Stage) logListView.getScene().getWindow();
        NavigationUtil.navigateToDashboard(stage);
    }

    @FXML
    private void handleRefresh() {
        loadLogs();
    }
}
