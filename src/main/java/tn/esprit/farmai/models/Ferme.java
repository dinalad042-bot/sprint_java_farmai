package tn.esprit.farmai.models;

import java.util.Objects;

public class Ferme {
    private int id_ferme;
    private String nom_ferme;
    private String lieu;
    private float surface;

    public Ferme() {}

    public Ferme(int id_ferme, String nom_ferme, String lieu, float surface) {
        this.id_ferme = id_ferme;
        this.nom_ferme = nom_ferme;
        this.lieu = lieu;
        this.surface = surface;
    }

    public int getId_ferme() { return id_ferme; }
    public void setId_ferme(int id_ferme) { this.id_ferme = id_ferme; }
    public String getNom_ferme() { return nom_ferme; }
    public void setNom_ferme(String nom_ferme) { this.nom_ferme = nom_ferme; }
    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    public float getSurface() { return surface; }
    public void setSurface(float surface) { this.surface = surface; }

    @Override
    public String toString() {
        return "Ferme{" + "id_ferme=" + id_ferme + ", nom_ferme='" + nom_ferme + '\'' + ", lieu='" + lieu + '\'' + ", surface=" + surface + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ferme ferme)) return false;
        return id_ferme == ferme.id_ferme && Float.compare(ferme.surface, surface) == 0 && Objects.equals(nom_ferme, ferme.nom_ferme) && Objects.equals(lieu, ferme.lieu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_ferme, nom_ferme, lieu, surface);
    }
}