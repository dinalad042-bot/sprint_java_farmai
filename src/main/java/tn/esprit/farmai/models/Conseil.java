package tn.esprit.farmai.models;

import java.util.Objects;

/**
 * Conseil entity representing advice linked to an analysis in the FarmAI application.
 */
public class Conseil {

    private int idConseil;
    private String descriptionConseil;
    private Priorite priorite;
    private int idAnalyse; // FK to Analyse

    // Default constructor
    public Conseil() {}

    // Constructor without ID (for new conseils)
    public Conseil(String descriptionConseil, Priorite priorite, int idAnalyse) {
        this.descriptionConseil = descriptionConseil;
        this.priorite = priorite;
        this.idAnalyse = idAnalyse;
    }

    // Full constructor
    public Conseil(int idConseil, String descriptionConseil, Priorite priorite, int idAnalyse) {
        this.idConseil = idConseil;
        this.descriptionConseil = descriptionConseil;
        this.priorite = priorite;
        this.idAnalyse = idAnalyse;
    }

    // Getters and Setters
    public int getIdConseil() {
        return idConseil;
    }

    public void setIdConseil(int idConseil) {
        this.idConseil = idConseil;
    }

    public String getDescriptionConseil() {
        return descriptionConseil;
    }

    public void setDescriptionConseil(String descriptionConseil) {
        this.descriptionConseil = descriptionConseil;
    }

    public Priorite getPriorite() {
        return priorite;
    }

    public void setPriorite(Priorite priorite) {
        this.priorite = priorite;
    }

    public int getIdAnalyse() {
        return idAnalyse;
    }

    public void setIdAnalyse(int idAnalyse) {
        this.idAnalyse = idAnalyse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conseil conseil = (Conseil) o;
        return idConseil == conseil.idConseil;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idConseil);
    }

    @Override
    public String toString() {
        return "Conseil{" +
                "idConseil=" + idConseil +
                ", descriptionConseil='" + descriptionConseil + '\'' +
                ", priorite=" + priorite +
                ", idAnalyse=" + idAnalyse +
                '}';
    }
}
