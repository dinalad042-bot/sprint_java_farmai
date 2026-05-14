package tn.esprit.farmai.models;

import java.util.Objects;

/**
 * Plantes entity representing a plant in a farm.
 * Fixed: Renamed fields from snake_case to camelCase for Java conventions.
 */
public class Plantes {
    private int idPlante;        // Fixed: was id_plante
    private String nomEspece;    // Fixed: was nom_espece
    private String cycleVie;     // Fixed: was cycle_vie
    private int idFerme;         // Fixed: was id_ferme
    private double quantite;

    public Plantes() {}

    public Plantes(int idPlante, String nomEspece, String cycleVie, int idFerme, double quantite) {
        this.idPlante = idPlante;
        this.nomEspece = nomEspece;
        this.cycleVie = cycleVie;
        this.idFerme = idFerme;
        this.quantite = quantite;
    }

    // Getters and Setters with camelCase
    public int getIdPlante() { return idPlante; }
    public void setIdPlante(int idPlante) { this.idPlante = idPlante; }

    public String getNomEspece() { return nomEspece; }
    public void setNomEspece(String nomEspece) { this.nomEspece = nomEspece; }

    public String getCycleVie() { return cycleVie; }
    public void setCycleVie(String cycleVie) { this.cycleVie = cycleVie; }

    public int getIdFerme() { return idFerme; }
    public void setIdFerme(int idFerme) { this.idFerme = idFerme; }

    public double getQuantite() { return quantite; }
    public void setQuantite(double quantite) { this.quantite = quantite; }

    @Override
    public String toString() {
        return "Plantes{" +
                "idPlante=" + idPlante +
                ", nomEspece='" + nomEspece + '\'' +
                ", cycleVie='" + cycleVie + '\'' +
                ", idFerme=" + idFerme +
                ", quantite=" + quantite +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Plantes plantes)) return false;
        return idPlante == plantes.idPlante &&
               idFerme == plantes.idFerme &&
               Double.compare(plantes.quantite, quantite) == 0 &&
               Objects.equals(nomEspece, plantes.nomEspece) &&
               Objects.equals(cycleVie, plantes.cycleVie);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPlante, nomEspece, cycleVie, idFerme, quantite);
    }
}
