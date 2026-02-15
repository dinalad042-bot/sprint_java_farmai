package tn.esprit.farmai.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tn.esprit.farmai.utils.NotificationManager;

import java.net.URL;
import java.util.ResourceBundle;

public class NotificationsController implements Initializable {

    @FXML
    private ListView<String> notificationListView;

    @FXML
    private Label countLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Bind list
        notificationListView.setItems(NotificationManager.getNotifications());

        // Update count label
        updateCountLabel();

        // Listen for changes
        NotificationManager.getNotifications()
                .addListener((javafx.collections.ListChangeListener<String>) c -> updateCountLabel());

        // Custom Cell Factory
        notificationListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox card = new HBox(12);
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.setStyle(
                            "-fx-background-color: white; -fx-background-radius: 8px; -fx-padding: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 3, 0, 0, 1);");

                    Text icon = new Text("ℹ️"); // Could differ based on message content
                    if (item.toLowerCase().contains("supprimé"))
                        icon.setText("🗑️");
                    else if (item.toLowerCase().contains("ajouté"))
                        icon.setText("✅");
                    else if (item.toLowerCase().contains("modifié"))
                        icon.setText("✏️");

                    Label msgLabel = new Label(item);
                    msgLabel.setWrapText(true);
                    msgLabel.setMaxWidth(350);
                    msgLabel.setStyle("-fx-text-fill: #455A64; -fx-font-size: 13px;");

                    card.getChildren().addAll(icon, msgLabel);
                    setGraphic(card);
                    setStyle("-fx-background-color: transparent; -fx-padding: 4px 0;");
                }
            }
        });

        // Mark as read immediately when viewed (simplification)
        NotificationManager.markAllAsRead();
    }

    private void updateCountLabel() {
        if (countLabel != null) {
            countLabel.setText(NotificationManager.getNotifications().size() + " total");
        }
    }

    @FXML
    private void handleClearAll() {
        NotificationManager.clearAll();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) notificationListView.getScene().getWindow();
        stage.close();
    }
}
