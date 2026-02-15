package tn.esprit.farmai.models;

/**
 * Enum representing user roles in the FarmAI application.
 * Each role has a display name for UI purposes.
 */
public enum Role {
    ADMIN("Administrateur"),
    EXPERT("Expert"),
    AGRICOLE("Agricole"),
    FOURNISSEUR("Fournisseur");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get Role from string value (case-insensitive)
     */
    public static Role fromString(String value) {
        if (value == null) return null;
        for (Role role : Role.values()) {
            if (role.name().equalsIgnoreCase(value) || 
                role.displayName.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}
