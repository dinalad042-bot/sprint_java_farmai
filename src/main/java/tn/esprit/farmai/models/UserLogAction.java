package tn.esprit.farmai.models;

/**
 * Enum defining the types of actions recorded in user logs.
 * Matches action_type values from Symfony (src/Entity/UserLog.php).
 */
public enum UserLogAction {
    // Basic CRUD operations
    CREATE,
    UPDATE,
    DELETE,

    // Authentication actions
    LOGIN,
    LOGOUT,
    SIGNUP,
    SIGNUP_WEB,

    // Face recognition actions
    LOGIN_FACE,
    FACE_DELETE,

    // Fallback for unknown action types from database
    UNKNOWN
}