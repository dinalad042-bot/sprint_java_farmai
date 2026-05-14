package tn.esprit.farmai.utils;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.util.Optional;

/**
 * Utility class for consistent alert dialogs across the application.
 * Refactored from duplicate code in controllers - Clean Code principle.
 * 
 * SRP: Handles only alert dialog display.
 * DRY: Single source of truth for alert styling and behavior.
 * 
 * @author FarmAI Team
 * @since Sprint Java - Seance 6/7
 */
public class AlertUtils {

    // Common styles
    private static final String ERROR_STYLE = "-fx-accent: #D32F2F;";
    private static final String SUCCESS_STYLE = "-fx-accent: #2E7D32;";
    private static final String WARNING_STYLE = "-fx-accent: #FF9800;";
    private static final String INFO_STYLE = "-fx-accent: #1976D2;";

    /**
     * Show error alert with message.
     * 
     * @param title Alert title
     * @param message Alert message
     */
    public static void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, null, message);
    }

    /**
     * Show error alert with header and message.
     * 
     * @param title Alert title
     * @param header Alert header (can be null)
     * @param message Alert message
     */
    public static void showError(String title, String header, String message) {
        showAlert(Alert.AlertType.ERROR, title, header, message);
    }

    /**
     * Show success/information alert.
     * 
     * @param title Alert title
     * @param message Alert message
     */
    public static void showSuccess(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, null, message);
    }

    /**
     * Show information alert.
     * 
     * @param title Alert title
     * @param message Alert message
     */
    public static void showInfo(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, null, message);
    }

    /**
     * Show warning alert.
     * 
     * @param title Alert title
     * @param message Alert message
     */
    public static void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, null, message);
    }

    /**
     * Show warning alert with header.
     * 
     * @param title Alert title
     * @param header Alert header
     * @param message Alert message
     */
    public static void showWarning(String title, String header, String message) {
        showAlert(Alert.AlertType.WARNING, title, header, message);
    }

    /**
     * Show confirmation dialog.
     * 
     * @param title Dialog title
     * @param header Dialog header
     * @param message Dialog message
     * @return true if user clicked OK, false otherwise
     */
    public static boolean showConfirmation(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Show confirmation dialog with custom buttons.
     * 
     * @param title Dialog title
     * @param header Dialog header
     * @param message Dialog message
     * @param okButtonText Custom OK button text
     * @param cancelButtonText Custom Cancel button text
     * @return true if user clicked OK, false otherwise
     */
    public static boolean showConfirmation(String title, String header, String message, 
                                           String okButtonText, String cancelButtonText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        
        ButtonType okButton = new ButtonType(okButtonText, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType(cancelButtonText, ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(okButton, cancelButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == okButton;
    }

    /**
     * Show alert with expandable content (for long text like AI responses).
     * 
     * @param type Alert type
     * @param title Alert title
     * @param header Alert header
     * @param message Short message
     * @param expandableContent Long text to show in expandable area
     */
    public static void showExpandable(Alert.AlertType type, String title, String header, 
                                      String message, String expandableContent) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        
        TextArea textArea = new TextArea(expandableContent);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        
        alert.getDialogPane().setExpandableContent(textArea);
        alert.showAndWait();
    }

    /**
     * Show error alert on JavaFX Application Thread.
     * Safe to call from background threads.
     * 
     * @param title Alert title
     * @param message Alert message
     */
    public static void showErrorOnFxThread(String title, String message) {
        Platform.runLater(() -> showError(title, message));
    }

    /**
     * Show info alert on JavaFX Application Thread.
     * Safe to call from background threads.
     * 
     * @param title Alert title
     * @param message Alert message
     */
    public static void showInfoOnFxThread(String title, String message) {
        Platform.runLater(() -> showInfo(title, message));
    }

    /**
     * Show success alert on JavaFX Application Thread.
     * Safe to call from background threads.
     * 
     * @param title Alert title
     * @param message Alert message
     */
    public static void showSuccessOnFxThread(String title, String message) {
        Platform.runLater(() -> showSuccess(title, message));
    }

    /**
     * Show warning alert on JavaFX Application Thread.
     * Safe to call from background threads.
     * 
     * @param title Alert title
     * @param message Alert message
     */
    public static void showWarningOnFxThread(String title, String message) {
        Platform.runLater(() -> showWarning(title, message));
    }

    /**
     * Core alert display method.
     * 
     * @param type Alert type
     * @param title Alert title
     * @param header Alert header (can be null)
     * @param message Alert message
     */
    public static void showAlert(Alert.AlertType type, String title, String header, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show input validation errors in a formatted dialog.
     * 
     * @param errors List of validation error messages
     */
    public static void showValidationErrors(java.util.List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Requise");
        alert.setHeaderText("Veuillez corriger les erreurs suivantes:");
        
        VBox content = new VBox(5);
        content.setPadding(new Insets(10));
        
        for (String error : errors) {
            Label errorLabel = new Label("• " + error);
            errorLabel.setStyle("-fx-text-fill: #D32F2F;");
            content.getChildren().add(errorLabel);
        }
        
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    /**
     * Show a toast-like notification (non-blocking, auto-dismiss).
     * Note: Uses a custom stage for toast-like behavior.
     * 
     * @param message Toast message
     * @param durationMs Duration in milliseconds
     */
    public static void showToast(String message, int durationMs) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("");
            alert.setHeaderText(null);
            alert.setContentText(message);
            
            // Auto-close after duration
            new Thread(() -> {
                try {
                    Thread.sleep(durationMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                Platform.runLater(alert::close);
            }).start();
            
            alert.show();
        });
    }

    /**
     * Show toast with default duration (3 seconds).
     * 
     * @param message Toast message
     */
    public static void showToast(String message) {
        showToast(message, 3000);
    }

    /**
     * Private constructor - utility class should not be instantiated.
     */
    private AlertUtils() {}
}