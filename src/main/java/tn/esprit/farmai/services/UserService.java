package tn.esprit.farmai.services;

import tn.esprit.farmai.interfaces.CRUD;
import tn.esprit.farmai.models.Role;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.models.UserLog;
import tn.esprit.farmai.models.UserLogAction;
import tn.esprit.farmai.utils.MyDBConnexion;
import tn.esprit.farmai.utils.PasswordUtil;
import tn.esprit.farmai.utils.SessionManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for User CRUD operations and authentication.
 */
public class UserService implements CRUD<User> {

    private final Connection cnx;
    private final UserLogService userLogService;

    public UserService() {
        this.cnx = MyDBConnexion.getInstance().getCnx();
        this.userLogService = new UserLogService();
    }

    /**
     * Helper method to log user actions.
     */
    private void logAction(int userId, UserLogAction action, String description) {
        try {
            User current = SessionManager.getInstance().getCurrentUser();
            String performedBy = (current != null) ? current.getEmail() : "System/Guest";
            userLogService.insertOne(new UserLog(userId, action, performedBy, description));
        } catch (SQLException e) {
            System.err.println("Failed to save user log: " + e.getMessage());
        }
    }

    @Override
    public void insertOne(User user) throws SQLException {
        String query = "INSERT INTO user (nom, prenom, email, password, cin, adresse, telephone, image_url, role) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, PasswordUtil.hashPassword(user.getPassword()));
            ps.setString(5, user.getCin());
            ps.setString(6, user.getAdresse());
            ps.setString(7, user.getTelephone());
            ps.setString(8, user.getImageUrl());
            ps.setString(9, user.getRole() != null ? user.getRole().name() : Role.AGRICOLE.name());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                user.setIdUser(rs.getInt(1));
            }

            logAction(user.getIdUser(), UserLogAction.CREATE, "User registered: " + user.getEmail());
        }
    }

    @Override
    public void updateOne(User user) throws SQLException {
        String query = "UPDATE user SET nom = ?, prenom = ?, email = ?, cin = ?, " +
                "adresse = ?, telephone = ?, image_url = ?, role = ? WHERE id_user = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getCin());
            ps.setString(5, user.getAdresse());
            ps.setString(6, user.getTelephone());
            ps.setString(7, user.getImageUrl());
            ps.setString(8, user.getRole() != null ? user.getRole().name() : Role.AGRICOLE.name());
            ps.setInt(9, user.getIdUser());

            ps.executeUpdate();
            logAction(user.getIdUser(), UserLogAction.UPDATE, "User updated: " + user.getEmail());
        }
    }

    /**
     * Update user password
     */
    public void updatePassword(int userId, String newPassword) throws SQLException {
        String query = "UPDATE user SET password = ? WHERE id_user = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, PasswordUtil.hashPassword(newPassword));
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteOne(User user) throws SQLException {
        String query = "DELETE FROM user WHERE id_user = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, user.getIdUser());
            ps.executeUpdate();
            // logAction cannot be called here because the user deletion cascades to logs
        }
    }

    /**
     * Delete user by ID
     */
    public void deleteById(int userId) throws SQLException {
        String query = "DELETE FROM user WHERE id_user = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
            // logAction cannot be called here because the user deletion cascades to logs
        }
    }

    @Override
    public List<User> selectALL() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user ORDER BY id_user DESC";

        try (Statement st = cnx.createStatement();
                ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    /**
     * Find user by ID
     */
    public Optional<User> findById(int id) throws SQLException {
        String query = "SELECT * FROM user WHERE id_user = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) throws SQLException {
        String query = "SELECT * FROM user WHERE email = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Find user by CIN
     */
    public Optional<User> findByCin(String cin) throws SQLException {
        String query = "SELECT * FROM user WHERE cin = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, cin);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Check if email exists
     */
    public boolean emailExists(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM user WHERE email = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Check if CIN exists
     */
    public boolean cinExists(String cin) throws SQLException {
        String query = "SELECT COUNT(*) FROM user WHERE cin = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, cin);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Authenticate user with email and password
     */
    public Optional<User> authenticate(String email, String password) throws SQLException {
        String query = "SELECT * FROM user WHERE email = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (PasswordUtil.verifyPassword(password, storedPassword)) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Get all users by role
     */
    public List<User> findByRole(Role role) throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user WHERE role = ? ORDER BY id_user DESC";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, role.name());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    /**
     * Search users by name or email
     */
    public List<User> search(String keyword) throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user WHERE nom LIKE ? OR prenom LIKE ? OR email LIKE ? ORDER BY id_user DESC";
        String searchPattern = "%" + keyword + "%";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    /**
     * Count total users
     */
    public int countAll() throws SQLException {
        String query = "SELECT COUNT(*) FROM user";

        try (Statement st = cnx.createStatement();
                ResultSet rs = st.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Count users by role
     */
    public int countByRole(Role role) throws SQLException {
        String query = "SELECT COUNT(*) FROM user WHERE role = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, role.name());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Map ResultSet to User object
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setIdUser(rs.getInt("id_user"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setCin(rs.getString("cin"));
        user.setAdresse(rs.getString("adresse"));
        user.setTelephone(rs.getString("telephone"));
        user.setImageUrl(rs.getString("image_url"));

        String roleStr = rs.getString("role");
        if (roleStr != null && !roleStr.isEmpty()) {
            try {
                user.setRole(Role.valueOf(roleStr));
            } catch (IllegalArgumentException e) {
                user.setRole(Role.AGRICOLE); // Default role
            }
        } else {
            user.setRole(Role.AGRICOLE);
        }

        return user;
    }
}
