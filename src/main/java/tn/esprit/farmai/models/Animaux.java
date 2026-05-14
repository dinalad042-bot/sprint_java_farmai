package tn.esprit.farmai.models;

import java.sql.Date;
import java.util.Objects;

/**
 * Animaux entity representing an animal in a farm.
 * Fixed: Renamed fields from snake_case to camelCase for Java conventions.
 */
public class Animaux {
    private int idAnimal;        // Fixed: was id_animal
    private String espece;
    private String etatSante;    // Fixed: was etat_sante
    private Date dateNaissance;  // Fixed: was date_naissance
    private int idFerme;         // Fixed: was id_ferme

    public Animaux() {}

    public Animaux(int idAnimal, String espece, String etatSante, Date dateNaissance, int idFerme) {
        this.idAnimal = idAnimal;
        this.espece = espece;
        this.etatSante = etatSante;
        this.dateNaissance = dateNaissance;
        this.idFerme = idFerme;
    }

    // Getters and Setters with camelCase
    public int getIdAnimal() { return idAnimal; }
    public void setIdAnimal(int idAnimal) { this.idAnimal = idAnimal; }

    public String getEspece() { return espece; }
    public void setEspece(String espece) { this.espece = espece; }

    public String getEtatSante() { return etatSante; }
    public void setEtatSante(String etatSante) { this.etatSante = etatSante; }

    public Date getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(Date dateNaissance) { this.dateNaissance = dateNaissance; }

    public int getIdFerme() { return idFerme; }
    public void setIdFerme(int idFerme) { this.idFerme = idFerme; }

    @Override
    public String toString() {
        return espece + " (#" + idAnimal + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Animaux animaux)) return false;
        return idAnimal == animaux.idAnimal &&
               idFerme == animaux.idFerme &&
               Objects.equals(espece, animaux.espece) &&
               Objects.equals(etatSante, animaux.etatSante) &&
               Objects.equals(dateNaissance, animaux.dateNaissance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAnimal, espece, etatSante, dateNaissance, idFerme);
    }
}
