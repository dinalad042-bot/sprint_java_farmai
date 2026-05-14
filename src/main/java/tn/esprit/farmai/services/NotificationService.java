package tn.esprit.farmai.services;

import tn.esprit.farmai.interfaces.CRUD;
import tn.esprit.farmai.models.Ferme;
import tn.esprit.farmai.models.Notification;
import tn.esprit.farmai.utils.MyDBConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for Notification CRUD operations.
 * Handles creating, reading, and managing notifications for users.
 */
public class NotificationService implements CRUD<Notification> {

    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());
    private final Connection cnx;

    public NotificationService() {
        this.cnx = MyDBConnexion.getInstance().getCnx();
    }

    @Override
    public void insertOne(Notification notification) throws SQLException {
        String query = "INSERT INTO notification (id_user, titre, message, type, id_reference, is_read, date_creation) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, notification.getIdUser());
            ps.setString(2, notification.getTitre());
            ps.setString(3, notification.getMessage());
            ps.setString(4, notification.getType());
            ps.setObject(5, notification.getIdReference(), Types.INTEGER);
            ps.setBoolean(6, notification.isRead());
            ps.setTimestamp(7, Timestamp.valueOf(notification.getDateCreation()));

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                notification.setIdNotification(rs.getInt(1));
            }
            
            LOGGER.log(Level.INFO, "Notification created for user {0}: {1}", 
                    new Object[]{notification.getIdUser(), notification.getTitre()});
        }
    }

    @Override
    public void updateOne(Notification notification) throws SQLException {
        String query = "UPDATE notification SET id_user = ?, titre = ?, message = ?, " +
                      "type = ?, id_reference = ?, is_read = ?, date_creation = ? " +
                      "WHERE id_notification = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, notification.getIdUser());
            ps.setString(2, notification.getTitre());
            ps.setString(3, notification.getMessage());
            ps.setString(4, notification.getType());
            ps.setObject(5, notification.getIdReference(), Types.INTEGER);
            ps.setBoolean(6, notification.isRead());
            ps.setTimestamp(7, Timestamp.valueOf(notification.getDateCreation()));
            ps.setInt(8, notification.getIdNotification());

            ps.executeUpdate();
        }
    }

    @Override
    public void deleteOne(Notification notification) throws SQLException {
        String query = "DELETE FROM notification WHERE id_notification = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, notification.getIdNotification());
            ps.executeUpdate();
        }
    }

    @Override
    public List<Notification> selectALL() throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT * FROM notification ORDER BY date_creation DESC";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
        }
        return notifications;
    }

    /**
     * Find all notifications for a specific user, ordered by date (newest first)
     */
    public List<Notification> findByUser(int userId) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT * FROM notification WHERE id_user = ? ORDER BY date_creation DESC";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
        }
        return notifications;
    }

    /**
     * Find unread notifications for a specific user
     */
    public List<Notification> findUnreadByUser(int userId) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT * FROM notification WHERE id_user = ? AND is_read = FALSE ORDER BY date_creation DESC";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
        }
        return notifications;
    }

    /**
     * Count unread notifications for a user
     */
    public int countUnreadByUser(int userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM notification WHERE id_user = ? AND is_read = FALSE";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Mark a single notification as read
     */
    public void markAsRead(int notificationId) throws SQLException {
        String query = "UPDATE notification SET is_read = TRUE WHERE id_notification = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, notificationId);
            ps.executeUpdate();
        }
    }

    /**
     * Mark all notifications for a user as read
     */
    public void markAllAsRead(int userId) throws SQLException {
        String query = "UPDATE notification SET is_read = TRUE WHERE id_user = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Create a notification for the fermier who owns a specific farm.
     * This is the main method to use when an expert adds an analysis or conseil.
     *
     * @param fermeId The farm ID
     * @param titre The notification title
     * @param message The notification message
     * @param type The notification type (ANALYSE, CONSEIL, SYSTEM)
     * @param idReference The ID of the related entity (analysis ID, conseil ID)
     * @return true if notification was created, false if farm has no fermier or error occurred
     */
    public boolean createForFermier(int fermeId, String titre, String message, String type, int idReference) {
        try {
            // Get the fermier ID for this farm
            FermeService fermeService = new FermeService();
            Ferme ferme = fermeService.findById(fermeId);

            if (ferme == null) {
                LOGGER.log(Level.WARNING, "Cannot create notification: Farm {0} not found", fermeId);
                return false;
            }

            int fermierId = ferme.getIdFermier();
            if (fermierId <= 0) {
                LOGGER.log(Level.WARNING, "Cannot create notification: Farm {0} has no associated fermier", fermeId);
                return false;
            }

            // Create and save the notification
            Notification notification = new Notification(
                    fermierId,
                    titre,
                    message,
                    type,
                    idReference
            );

            insertOne(notification);
            
            LOGGER.log(Level.INFO, "Notification created for fermier {0} regarding farm {1}: {2}", 
                    new Object[]{fermierId, fermeId, titre});
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create notification for farm " + fermeId, e);
            return false;
        }
    }

    /**
     * Create a simple notification without a reference ID
     */
    public boolean createForFermier(int fermeId, String titre, String message, String type) {
        return createForFermier(fermeId, titre, message, type, 0);
    }

    /**
     * Mark notifications of a specific type and reference as read
     * Useful when a user views an analysis - mark the corresponding notification as read
     */
    public void markAsReadByReference(int userId, String type, int idReference) throws SQLException {
        String query = "UPDATE notification SET is_read = TRUE WHERE id_user = ? AND type = ? AND id_reference = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setString(2, type);
            ps.setInt(3, idReference);
            ps.executeUpdate();
        }
    }

    /**
     * Delete old read notifications (cleanup utility)
     */
    public int deleteOldReadNotifications(int daysOld) throws SQLException {
        String query = "DELETE FROM notification WHERE is_read = TRUE AND date_creation < DATE_SUB(NOW(), INTERVAL ? DAY)";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, daysOld);
            return ps.executeUpdate();
        }
    }

    /**
     * Map ResultSet to Notification object
     */
    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setIdNotification(rs.getInt("id_notification"));
        notification.setIdUser(rs.getInt("id_user"));
        notification.setTitre(rs.getString("titre"));
        notification.setMessage(rs.getString("message"));
        notification.setType(rs.getString("type"));
        
        int idRef = rs.getInt("id_reference");
        notification.setIdReference(rs.wasNull() ? null : idRef);
        
        notification.setRead(rs.getBoolean("is_read"));
        notification.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
        
        return notification;
    }
}