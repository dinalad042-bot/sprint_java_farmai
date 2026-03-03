package tn.esprit.farmai.utils;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import tn.esprit.farmai.HelloApplication;
import tn.esprit.farmai.models.Role;

import java.io.IOException;
import java.util.Optional;

/**
 * Utility class for navigation between different views.
 */
public class NavigationUtil {

    private static final int DEFAULT_WIDTH = 1200;
    private static final int DEFAULT_HEIGHT = 800;
    private static final int LOGIN_WIDTH = 900;
    private static final int LOGIN_HEIGHT = 600;

    public static void navigateTo(Stage stage, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);

            String cssPath = HelloApplication.class.getResource("styles/main.css") != null
                    ? HelloApplication.class.getResource("styles/main.css").toExternalForm()
                    : null;
            if (cssPath != null) {
                scene.getStylesheets().add(cssPath);
            }

            stage.setScene(scene);
            stage.setTitle("FarmAI - " + title);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            showError("Navigation Error", "Could not load the requested page: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static void navigateToLogin(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("views/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, LOGIN_WIDTH, LOGIN_HEIGHT);

            String cssPath = HelloApplication.class.getResource("styles/auth.css") != null
                    ? HelloApplication.class.getResource("styles/auth.css").toExternalForm()
                    : null;
            if (cssPath != null) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(cssPath);
            }

            stage.setScene(scene);
            stage.setTitle("FarmAI - Connexion");
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            showError("Navigation Error", "Could not load login page.");
            e.printStackTrace();
        }
    }

    public static void navigateToSignup(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("views/signup.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, LOGIN_WIDTH, LOGIN_HEIGHT);

            String cssPath = HelloApplication.class.getResource("styles/auth.css") != null
                    ? HelloApplication.class.getResource("styles/auth.css").toExternalForm()
                    : null;
            if (cssPath != null) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(cssPath);
            }

            stage.setScene(scene);
            stage.setTitle("FarmAI - Inscription");
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            showError("Navigation Error", "Could not load signup page.");
            e.printStackTrace();
        }
    }

    public static void navigateToDashboard(Stage stage) {
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn()) {
            navigateToLogin(stage);
            return;
        }

        Role role = session.getCurrentUser().getRole();
        String fxmlPath;
        String title;

        switch (role) {
            case ADMIN:
                fxmlPath = "views/admin-dashboard.fxml";
                title = "Tableau de Bord Admin";
                break;
            case EXPERT:
                fxmlPath = "views/expert-dashboard.fxml";
                title = "Tableau de Bord Expert";
                break;
            case AGRICOLE:
                fxmlPath = "views/agricole-dashboard.fxml";
                title = "Tableau de Bord Agricole";
                break;
            case FOURNISSEUR:
                fxmlPath = "views/fournisseur-dashboard.fxml";
                title = "Tableau de Bord Fournisseur";
                break;
            default:
                fxmlPath = "views/login.fxml";
                title = "Connexion";
        }

        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);

            String cssPath = HelloApplication.class.getResource("styles/dashboard.css") != null
                    ? HelloApplication.class.getResource("styles/dashboard.css").toExternalForm()
                    : null;
            if (cssPath != null)
                scene.getStylesheets().add(cssPath);

            stage.setScene(scene);
            stage.setTitle("FarmAI - " + title);
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            showError("Navigation Error", "Could not load dashboard: " + msg);
            e.printStackTrace();
        }
    }

    public static void navigateToAgricoleDashboard(Stage stage) {

        navigateTo(stage, "views/agricole-dashboard.fxml", "Tableau de Bord Agricole");

    }

    

    public static void navigateToUserList(Stage stage) {
        navigateTo(stage, "views/user-list.fxml", "Gestion des Utilisateurs");
    }

    public static void navigateToGestionAnalyses(Stage stage) {
        navigateTo(stage, "views/gestion-analyses.fxml", "Gestion des Analyses");
    }

    public static void navigateToGestionConseils(Stage stage) {
        navigateTo(stage, "views/gestion-conseils.fxml", "Gestion des Conseils");
    }

    public static void navigateToStatistics(Stage stage) {
        navigateTo(stage, "views/statistics.fxml", "Statistiques");
    }

    public static void logout(Stage stage) {
        Optional<ButtonType> result = showConfirmation("Déconnexion", "Êtes-vous sûr de vouloir vous déconnecter?");
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SessionManager.getInstance().logout();
            navigateToLogin(stage);
        }
    }

    public static void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void showSuccess(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static Optional<ButtonType> showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }

    public static void showWarning(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void showInfo(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
