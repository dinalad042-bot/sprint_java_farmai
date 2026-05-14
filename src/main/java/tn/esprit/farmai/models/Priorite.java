package tn.esprit.farmai.models;

/**
 * Enum representing the priority level of a Conseil.
 * Values: BASSE, MOYENNE, HAUTE
 */
public enum Priorite {
    BASSE("Basse"),
    MOYENNE("Moyenne"),
    HAUTE("Haute");

    private final String label;

    Priorite(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
