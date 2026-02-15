package tn.esprit.farmai.utils;

import tn.esprit.farmai.models.User;

/**
 * Singleton class to manage the current user session.
 * Stores the logged-in user and provides session utilities.
 */
public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {
        // Private constructor for singleton
    }

    /**
     * Get the singleton instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Set the current logged-in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Get the current logged-in user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if a user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Clear the session (logout)
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Check if current user has a specific role
     */
    public boolean hasRole(String roleName) {
        if (currentUser == null || currentUser.getRole() == null) {
            return false;
        }
        return currentUser.getRole().name().equalsIgnoreCase(roleName);
    }

    /**
     * Check if current user is an admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if current user is an expert
     */
    public boolean isExpert() {
        return hasRole("EXPERT");
    }

    /**
     * Check if current user is agricole
     */
    public boolean isAgricole() {
        return hasRole("AGRICOLE");
    }

    /**
     * Check if current user is fournisseur
     */
    public boolean isFournisseur() {
        return hasRole("FOURNISSEUR");
    }
}
