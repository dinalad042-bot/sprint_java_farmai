package tn.esprit.farmai.models;

/**
 * One line of a purchase order.
 * Maps to erp_ligne_achat table.
 */
public class LigneAchat {

    private int id;
    private int idAchat;
    private int idMatiere;
    private double quantite;
    private double prixUnitaire;

    /** Loaded for display */
    private String nomMatiere;
    private String uniteMatiere;

    public LigneAchat() {
        this.quantite = 1.0;
        this.prixUnitaire = 0.0;
    }

    public LigneAchat(int idMatiere, double quantite, double prixUnitaire) {
        this.idMatiere = idMatiere;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
    }

    public double getSousTotal() {
        return quantite * prixUnitaire;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdAchat() { return idAchat; }
    public void setIdAchat(int idAchat) { this.idAchat = idAchat; }

    public int getIdMatiere() { return idMatiere; }
    public void setIdMatiere(int idMatiere) { this.idMatiere = idMatiere; }

    public double getQuantite() { return quantite; }
    public void setQuantite(double quantite) { this.quantite = quantite; }

    public double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(double prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public String getNomMatiere() { return nomMatiere; }
    public void setNomMatiere(String nomMatiere) { this.nomMatiere = nomMatiere; }

    public String getUniteMatiere() { return uniteMatiere; }
    public void setUniteMatiere(String uniteMatiere) { this.uniteMatiere = uniteMatiere; }
}
