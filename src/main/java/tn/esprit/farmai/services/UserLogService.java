package tn.esprit.farmai.services;

import tn.esprit.farmai.interfaces.CRUD;
import tn.esprit.farmai.models.UserLog;
import tn.esprit.farmai.models.UserLogAction;
import tn.esprit.farmai.utils.MyDBConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for UserLog CRUD operations.
 * Schema matches Symfony Entity (src/Entity/UserLog.php):
 * - id (bigint)
 * - user_id (bigint)
 * - action_type (string)
 * - performed_by (bigint)
 * - timestamp (datetime)
 * - description (text)
 */
public class UserLogService implements CRUD<UserLog> {

    private final Connection cnx;

    public UserLogService() {
        this.cnx = MyDBConnexion.getInstance().getCnx();
        createTableIfNotExists();
    }

    /**
     * Creates the user_log table if it doesn't exist.
     * Matches Symfony schema exactly.
     */
    private void createTableIfNotExists() {
        String query = "CREATE TABLE IF NOT EXISTS user_log (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id BIGINT, " +
                "action_type VARCHAR(20), " +
                "performed_by BIGINT, " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "description TEXT" +
                ")";
        try (Statement st = cnx.createStatement()) {
            st.execute(query);
        } catch (SQLException e) {
            System.err.println("Error creating user_log table: " + e.getMessage());
        }
    }

    @Override
    public void insertOne(UserLog log) throws SQLException {
        String query = "INSERT INTO user_log (user_id, action_type, performed_by, timestamp, description) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, log.getUserId() > 0 ? log.getUserId() : null);
            ps.setString(2, log.getActionType() != null ? log.getActionType().name() : null);
            ps.setObject(3, log.getPerformedBy() != null ? Integer.parseInt(log.getPerformedBy()) : null);
            ps.setTimestamp(4, log.getTimestamp() != null ? Timestamp.valueOf(log.getTimestamp()) : null);
            ps.setString(5, log.getDescription());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                log.setId(rs.getLong(1));
            }
        }
    }

    @Override
    public void updateOne(UserLog log) throws SQLException {
        String query = "UPDATE user_log SET user_id = ?, action_type = ?, performed_by = ?, description = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setObject(1, log.getUserId() > 0 ? log.getUserId() : null);
            ps.setString(2, log.getActionType() != null ? log.getActionType().name() : null);
            ps.setObject(3, log.getPerformedBy() != null ? Integer.parseInt(log.getPerformedBy()) : null);
            ps.setString(4, log.getDescription());
            ps.setLong(5, log.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteOne(UserLog log) throws SQLException {
        String query = "DELETE FROM user_log WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setLong(1, log.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public List<UserLog> selectALL() throws SQLException {
        List<UserLog> logs = new ArrayList<>();
        String query = "SELECT * FROM user_log ORDER BY timestamp DESC";
        try (Statement st = cnx.createStatement();
                ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                logs.add(mapResultSetToUserLog(rs));
            }
        }
        return logs;
    }

    /**
     * Find logs by user ID
     */
    public List<UserLog> findByUserId(int userId) throws SQLException {
        List<UserLog> logs = new ArrayList<>();
        String query = "SELECT * FROM user_log WHERE user_id = ? ORDER BY timestamp DESC";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                logs.add(mapResultSetToUserLog(rs));
            }
        }
        return logs;
    }

    /**
     * Maps a ResultSet to UserLog object using exact column names from Symfony schema.
     */
    private UserLog mapResultSetToUserLog(ResultSet rs) throws SQLException {
        UserLog log = new UserLog();

        // id (bigint) - from Symfony Entity
        log.setId(rs.getLong("id"));

        // user_id (bigint)
        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            log.setUserId(userId);
        }

        // action_type (string) - NOT "action"!
        String actionType = rs.getString("action_type");
        if (actionType != null) {
            try {
                // Try to match the enum directly
                log.setActionType(UserLogAction.valueOf(actionType.toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Fallback to UNKNOWN if the action type is not in our enum
                // This prevents the app from crashing if Symfony adds new action types
                log.setActionType(UserLogAction.UNKNOWN);
            }
        }

        // performed_by (bigint) - stored as user ID string in Java model
        int performedBy = rs.getInt("performed_by");
        if (!rs.wasNull()) {
            log.setPerformedBy(String.valueOf(performedBy));
        }

        // timestamp (datetime)
        Timestamp ts = rs.getTimestamp("timestamp");
        if (ts != null) {
            log.setTimestamp(ts.toLocalDateTime());
        }

        // description (text)
        log.setDescription(rs.getString("description"));

        return log;
    }
}