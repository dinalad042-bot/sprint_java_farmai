package tn.esprit.farmai.models;

import java.util.Objects;

/**
 * Ferme entity representing a farm in the FarmAI application.
 * Linked to a User (fermier) via id_user.
 */
public class Ferme {

    private int idFerme;
    private String nomFerme;
    private String lieu;
    private double surface; // en hectares
    private int idFermier; // FK to User (id_user column in DB)

    // Default constructor
    public Ferme() {}

    // Constructor without ID (for new fermes)
    public Ferme(String nomFerme, String lieu, double surface, int idFermier) {
        this.nomFerme = nomFerme;
        this.lieu = lieu;
        this.surface = surface;
        this.idFermier = idFermier;
    }

    // Full constructor
    public Ferme(int idFerme, String nomFerme, String lieu, double surface, int idFermier) {
        this.idFerme = idFerme;
        this.nomFerme = nomFerme;
        this.lieu = lieu;
        this.surface = surface;
        this.idFermier = idFermier;
    }

    // Getters and Setters
    public int getIdFerme() {
        return idFerme;
    }

    public void setIdFerme(int idFerme) {
        this.idFerme = idFerme;
    }

    public String getNomFerme() {
        return nomFerme;
    }

    public void setNomFerme(String nomFerme) {
        this.nomFerme = nomFerme;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public double getSurface() {
        return surface;
    }

    public void setSurface(double surface) {
        this.surface = surface;
    }

    public int getIdFermier() {
        return idFermier;
    }

    public void setIdFermier(int idFermier) {
        this.idFermier = idFermier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ferme ferme = (Ferme) o;
        return idFerme == ferme.idFerme;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFerme);
    }

    /**
     * Display name for ComboBox: "ID - NomFerme (Lieu)"
     */
    @Override
    public String toString() {
        return idFerme + " - " + nomFerme + " (" + lieu + ")";
    }
}
