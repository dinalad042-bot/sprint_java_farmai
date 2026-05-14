package tn.esprit.farmai.models;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Analyse entity representing a technical analysis in the FarmAI application.
 * Supports farmer requests and expert handling workflow.
 */
public class Analyse {

    private int idAnalyse;
    private LocalDateTime dateAnalyse;
    private String resultatTechnique;
    private int idTechnicien; // FK to User (expert who handles the analysis)
    private int idFerme; // FK to Ferme

    // Farmer request fields
    private String statut; // en_attente, en_cours, terminee, annulee
    private int idDemandeur; // FK to User (farmer who made the request)
    private String descriptionDemande; // Farmer's request description
    private String imageUrl; // URL for visual documentation
    private int idAnimalCible; // FK to Animal (optional)
    private int idPlanteCible; // FK to Plante (optional)

    // AI Diagnosis fields
    private String aiDiagnosisResult;
    private LocalDateTime aiDiagnosisDate;
    private String aiConfidenceScore;
    private String diagnosisMode; // text or vision

    // Default constructor
    public Analyse() {
        this.dateAnalyse = LocalDateTime.now();
        this.statut = "en_attente";
    }

    // Backward compatible constructor (for old tests)
    public Analyse(LocalDateTime dateAnalyse, String resultatTechnique,
                   int idTechnicien, int idFerme, String imageUrl) {
        this.dateAnalyse = dateAnalyse;
        this.resultatTechnique = resultatTechnique;
        this.idTechnicien = idTechnicien;
        this.idFerme = idFerme;
        this.imageUrl = imageUrl;
        this.statut = "en_attente";
    }

    // Backward compatible full constructor (for old tests)
    public Analyse(int idAnalyse, LocalDateTime dateAnalyse, String resultatTechnique,
                   int idTechnicien, int idFerme, String imageUrl) {
        this.idAnalyse = idAnalyse;
        this.dateAnalyse = dateAnalyse;
        this.resultatTechnique = resultatTechnique;
        this.idTechnicien = idTechnicien;
        this.idFerme = idFerme;
        this.imageUrl = imageUrl;
        this.statut = "en_attente";
    }

    // Constructor for farmer request
    public Analyse(LocalDateTime dateAnalyse, int idDemandeur, int idFerme,
                   String descriptionDemande, String imageUrl, int idAnimalCible, int idPlanteCible) {
        this.dateAnalyse = dateAnalyse;
        this.idDemandeur = idDemandeur;
        this.idFerme = idFerme;
        this.descriptionDemande = descriptionDemande;
        this.imageUrl = imageUrl;
        this.idAnimalCible = idAnimalCible;
        this.idPlanteCible = idPlanteCible;
        this.statut = "en_attente";
    }

    // Full constructor
    public Analyse(int idAnalyse, LocalDateTime dateAnalyse, String resultatTechnique,
                   int idTechnicien, int idFerme, String statut, int idDemandeur,
                   String descriptionDemande, String imageUrl, int idAnimalCible,
                   int idPlanteCible, String aiDiagnosisResult, LocalDateTime aiDiagnosisDate,
                   String aiConfidenceScore, String diagnosisMode) {
        this.idAnalyse = idAnalyse;
        this.dateAnalyse = dateAnalyse;
        this.resultatTechnique = resultatTechnique;
        this.idTechnicien = idTechnicien;
        this.idFerme = idFerme;
        this.statut = statut;
        this.idDemandeur = idDemandeur;
        this.descriptionDemande = descriptionDemande;
        this.imageUrl = imageUrl;
        this.idAnimalCible = idAnimalCible;
        this.idPlanteCible = idPlanteCible;
        this.aiDiagnosisResult = aiDiagnosisResult;
        this.aiDiagnosisDate = aiDiagnosisDate;
        this.aiConfidenceScore = aiConfidenceScore;
        this.diagnosisMode = diagnosisMode;
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

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getIdDemandeur() {
        return idDemandeur;
    }

    public void setIdDemandeur(int idDemandeur) {
        this.idDemandeur = idDemandeur;
    }

    public String getDescriptionDemande() {
        return descriptionDemande;
    }

    public void setDescriptionDemande(String descriptionDemande) {
        this.descriptionDemande = descriptionDemande;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getIdAnimalCible() {
        return idAnimalCible;
    }

    public void setIdAnimalCible(int idAnimalCible) {
        this.idAnimalCible = idAnimalCible;
    }

    public int getIdPlanteCible() {
        return idPlanteCible;
    }

    public void setIdPlanteCible(int idPlanteCible) {
        this.idPlanteCible = idPlanteCible;
    }

    public String getAiDiagnosisResult() {
        return aiDiagnosisResult;
    }

    public void setAiDiagnosisResult(String aiDiagnosisResult) {
        this.aiDiagnosisResult = aiDiagnosisResult;
    }

    public LocalDateTime getAiDiagnosisDate() {
        return aiDiagnosisDate;
    }

    public void setAiDiagnosisDate(LocalDateTime aiDiagnosisDate) {
        this.aiDiagnosisDate = aiDiagnosisDate;
    }

    public String getAiConfidenceScore() {
        return aiConfidenceScore;
    }

    public void setAiConfidenceScore(String aiConfidenceScore) {
        this.aiConfidenceScore = aiConfidenceScore;
    }

    public String getDiagnosisMode() {
        return diagnosisMode;
    }

    public void setDiagnosisMode(String diagnosisMode) {
        this.diagnosisMode = diagnosisMode;
    }

    public boolean isPending() {
        return "en_attente".equals(statut);
    }

    public boolean isInProgress() {
        return "en_cours".equals(statut);
    }

    public boolean isCompleted() {
        return "terminee".equals(statut);
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
                ", statut='" + statut + '\'' +
                ", idDemandeur=" + idDemandeur +
                ", idFerme=" + idFerme +
                ", idTechnicien=" + idTechnicien +
                '}';
    }
}
