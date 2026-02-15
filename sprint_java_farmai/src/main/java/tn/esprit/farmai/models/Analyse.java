package tn.esprit.farmai.models;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Analyse entity representing a technical analysis in the FarmAI application.
 */
public class Analyse {

    private int idAnalyse;
    private LocalDateTime dateAnalyse;
    private String resultatTechnique;
    private int idTechnicien; // FK to User
    private int idFerme; // FK to Ferme
    private String imageUrl; // URL for visual documentation (no BLOB)

    // Default constructor
    public Analyse() {}

    // Constructor without ID (for new analyses)
    public Analyse(LocalDateTime dateAnalyse, String resultatTechnique,
                   int idTechnicien, int idFerme, String imageUrl) {
        this.dateAnalyse = dateAnalyse;
        this.resultatTechnique = resultatTechnique;
        this.idTechnicien = idTechnicien;
        this.idFerme = idFerme;
        this.imageUrl = imageUrl;
    }

    // Full constructor
    public Analyse(int idAnalyse, LocalDateTime dateAnalyse, String resultatTechnique,
                   int idTechnicien, int idFerme, String imageUrl) {
        this.idAnalyse = idAnalyse;
        this.dateAnalyse = dateAnalyse;
        this.resultatTechnique = resultatTechnique;
        this.idTechnicien = idTechnicien;
        this.idFerme = idFerme;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public int getIdAnalyse() {
        return idAnalyse;
    }

    public void setIdAnalyse(int idAnalyse) {
        this.idAnalyse = idAnalyse;
    }

    public LocalDateTime getDateAnalyse() {
        return dateAnalyse;
    }

    public void setDateAnalyse(LocalDateTime dateAnalyse) {
        this.dateAnalyse = dateAnalyse;
    }

    public String getResultatTechnique() {
        return resultatTechnique;
    }

    public void setResultatTechnique(String resultatTechnique) {
        this.resultatTechnique = resultatTechnique;
    }

    public int getIdTechnicien() {
        return idTechnicien;
    }

    public void setIdTechnicien(int idTechnicien) {
        this.idTechnicien = idTechnicien;
    }

    public int getIdFerme() {
        return idFerme;
    }

    public void setIdFerme(int idFerme) {
        this.idFerme = idFerme;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Analyse analyse = (Analyse) o;
        return idAnalyse == analyse.idAnalyse;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAnalyse);
    }

    @Override
    public String toString() {
        return "Analyse{" +
                "idAnalyse=" + idAnalyse +
                ", dateAnalyse=" + dateAnalyse +
                ", resultatTechnique='" + resultatTechnique + '\'' +
                ", idTechnicien=" + idTechnicien +
                ", idFerme=" + idFerme +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
