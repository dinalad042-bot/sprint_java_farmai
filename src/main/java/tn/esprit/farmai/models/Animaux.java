package tn.esprit.farmai.models;

import java.sql.Date;
import java.util.Objects;

public class Animaux {
    private int id_animal;
    private String espece;
    private String etat_sante;
    private Date date_naissance;
    private int id_ferme;

    public Animaux() {}

    public Animaux(int id_animal, String espece, String etat_sante, Date date_naissance, int id_ferme) {
        this.id_animal = id_animal;
        this.espece = espece;
        this.etat_sante = etat_sante;
        this.date_naissance = date_naissance;
        this.id_ferme = id_ferme;
    }

    public int getId_animal() { return id_animal; }
    public void setId_animal(int id_animal) { this.id_animal = id_animal; }
    public String getEspece() { return espece; }
    public void setEspece(String espece) { this.espece = espece; }
    public String getEtat_sante() { return etat_sante; }
    public void setEtat_sante(String etat_sante) { this.etat_sante = etat_sante; }
    public Date getDate_naissance() { return date_naissance; }
    public void setDate_naissance(Date date_naissance) { this.date_naissance = date_naissance; }
    public int getId_ferme() { return id_ferme; }
    public void setId_ferme(int id_ferme) { this.id_ferme = id_ferme; }

    @Override
    public String toString() {
        return "Animaux{" + "id_animal=" + id_animal + ", espece='" + espece + '\'' + ", etat_sante='" + etat_sante + '\'' + ", date_naissance=" + date_naissance + ", id_ferme=" + id_ferme + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Animaux animaux)) return false;
        return id_animal == animaux.id_animal && id_ferme == animaux.id_ferme && Objects.equals(espece, animaux.espece) && Objects.equals(etat_sante, animaux.etat_sante) && Objects.equals(date_naissance, animaux.date_naissance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_animal, espece, etat_sante, date_naissance, id_ferme);
    }
}