package tn.esprit.farmai.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotificationManager {

    // Using simple String for now, but could be an object
    private static final ObservableList<String> notifications = FXCollections.observableArrayList();
    private static int unreadCount = 0;

    public static ObservableList<String> getNotifications() {
        return notifications;
    }

    public static void addNotification(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        notifications.add(0, "[" + timestamp + "] " + message);
        unreadCount++;
    }

    public static int getUnreadCount() {
        return unreadCount;
    }

    public static void markAllAsRead() {
        unreadCount = 0;
    }

    public static void clearAll() {
        notifications.clear();
        unreadCount = 0;
    }
}
