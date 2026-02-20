package tn.esprit.farmai.models;

import java.util.Objects;

public class Plantes {
    private int id_plante;
    private String nom_espece;
    private String cycle_vie;
    private int id_ferme;
    private double quantite;

    public Plantes() {}

    public Plantes(int id_plante, String nom_espece, String cycle_vie, int id_ferme, double quantite) {
        this.id_plante = id_plante;
        this.nom_espece = nom_espece;
        this.cycle_vie = cycle_vie;
        this.id_ferme = id_ferme;
        this.quantite = quantite;
    }

    public int getId_plante() { return id_plante; }
    public void setId_plante(int id_plante) { this.id_plante = id_plante; }
    public String getNom_espece() { return nom_espece; }
    public void setNom_espece(String nom_espece) { this.nom_espece = nom_espece; }
    public String getCycle_vie() { return cycle_vie; }
    public void setCycle_vie(String cycle_vie) { this.cycle_vie = cycle_vie; }
    public int getId_ferme() { return id_ferme; }
    public void setId_ferme(int id_ferme) { this.id_ferme = id_ferme; }
    public double getQuantite() { return quantite; }
    public void setQuantite(double quantite) { this.quantite = quantite; }

    @Override
    public String toString() {
        return "Plantes{" + "id_plante=" + id_plante + ", nom_espece='" + nom_espece + '\'' + ", cycle_vie='" + cycle_vie + '\'' + ", id_ferme=" + id_ferme + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Plantes plantes)) return false;
        return id_plante == plantes.id_plante && id_ferme == plantes.id_ferme && Objects.equals(nom_espece, plantes.nom_espece) && Objects.equals(cycle_vie, plantes.cycle_vie);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_plante, nom_espece, cycle_vie, id_ferme);
    }
}