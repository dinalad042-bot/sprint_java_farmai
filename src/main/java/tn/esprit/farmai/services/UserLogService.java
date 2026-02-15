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
 */
public class UserLogService implements CRUD<UserLog> {

    private final Connection cnx;

    public UserLogService() {
        this.cnx = MyDBConnexion.getInstance().getCnx();
        createTableIfNotExists();
    }

    /**
     * Creates the user_log table if it doesn't exist.
     */
    private void createTableIfNotExists() {
        String query = "CREATE TABLE IF NOT EXISTS user_log (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "action_type VARCHAR(20) NOT NULL, " +
                "performed_by VARCHAR(255) NOT NULL, " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "description TEXT, " +
                "FOREIGN KEY (user_id) REFERENCES user(id_user) ON DELETE CASCADE" +
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
            ps.setInt(1, log.getUserId());
            ps.setString(2, log.getActionType().name());
            ps.setString(3, log.getPerformedBy());
            ps.setTimestamp(4, Timestamp.valueOf(log.getTimestamp()));
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
            ps.setInt(1, log.getUserId());
            ps.setString(2, log.getActionType().name());
            ps.setString(3, log.getPerformedBy());
            ps.setString(4, log.getDescription());
            ps.setLong(5, log.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteOne(int id) throws SQLException {
        String query = "DELETE FROM user_log WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<UserLog> selectAll() throws SQLException {
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

    private UserLog mapResultSetToUserLog(ResultSet rs) throws SQLException {
        UserLog log = new UserLog();
        log.setId(rs.getLong("id"));
        log.setUserId(rs.getInt("user_id"));
        log.setActionType(UserLogAction.valueOf(rs.getString("action_type")));
        log.setPerformedBy(rs.getString("performed_by"));
        log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        log.setDescription(rs.getString("description"));
        return log;
    }
}
