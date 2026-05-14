package tn.esprit.farmai.models;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Notification entity representing a notification for a user.
 * Used to notify fermiers when experts add analyses or conseils for their farms.
 */
public class Notification {

    private int idNotification;
    private int idUser; // Recipient (fermier)
    private String titre;
    private String message;
    private String type; // ANALYSE, CONSEIL, SYSTEM
    private Integer idReference; // ID of related analysis or conseil (can be null)
    private boolean isRead;
    private LocalDateTime dateCreation;

    // Default constructor
    public Notification() {
        this.dateCreation = LocalDateTime.now();
        this.isRead = false;
    }

    // Constructor without ID (for new notifications)
    public Notification(int idUser, String titre, String message, String type, Integer idReference) {
        this.idUser = idUser;
        this.titre = titre;
        this.message = message;
        this.type = type;
        this.idReference = idReference;
        this.isRead = false;
        this.dateCreation = LocalDateTime.now();
    }

    // Full constructor
    public Notification(int idNotification, int idUser, String titre, String message, 
                        String type, Integer idReference, boolean isRead, LocalDateTime dateCreation) {
        this.idNotification = idNotification;
        this.idUser = idUser;
        this.titre = titre;
        this.message = message;
        this.type = type;
        this.idReference = idReference;
        this.isRead = isRead;
        this.dateCreation = dateCreation;
    }

    // Getters and Setters
    public int getIdNotification() {
        return idNotification;
    }

    public void setIdNotification(int idNotification) {
        this.idNotification = idNotification;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getIdReference() {
        return idReference;
    }

    public void setIdReference(Integer idReference) {
        this.idReference = idReference;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    /**
     * Get icon based on notification type
     */
    public String getIcon() {
        return switch (type.toUpperCase()) {
            case "ANALYSE" -> "📊";
            case "CONSEIL" -> "💡";
            case "SYSTEM" -> "ℹ️";
            default -> "📌";
        };
    }

    /**
     * Get CSS style class based on notification type
     */
    public String getStyleClass() {
        return switch (type.toUpperCase()) {
            case "ANALYSE" -> "notification-analyse";
            case "CONSEIL" -> "notification-conseil";
            case "SYSTEM" -> "notification-system";
            default -> "notification-default";
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return idNotification == that.idNotification;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idNotification);
    }

    @Override
    public String toString() {
        return "Notification{" +
                "idNotification=" + idNotification +
                ", idUser=" + idUser +
                ", titre='" + titre + '\'' +
                ", type='" + type + '\'' +
                ", isRead=" + isRead +
                ", dateCreation=" + dateCreation +
                '}';
    }
}