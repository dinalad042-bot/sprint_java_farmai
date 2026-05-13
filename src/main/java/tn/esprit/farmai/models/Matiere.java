package tn.esprit.farmai.models;

/**
 * Raw material — purchased via Achat, consumed when producing a Produit.
 * Maps to erp_matiere table.
 */
public class Matiere {

    private int idMatiere;
    private String nom;
    private String description;
    private String unite;
    private double stock;
    private double prixUnitaire;
    private double seuilCritique;

    public Matiere() {
        this.unite = "unité";
        this.stock = 0.0;
        this.prixUnitaire = 0.0;
        this.seuilCritique = 0.0;
    }

    public Matiere(String nom, String description, String unite, double stock, double prixUnitaire, double seuilCritique) {
        this.nom = nom;
        this.description = description;
        this.unite = unite;
        this.stock = stock;
        this.prixUnitaire = prixUnitaire;
        this.seuilCritique = seuilCritique;
    }

    public boolean isStockCritique() {
        return seuilCritique > 0 && stock <= seuilCritique;
    }

    // Getters & Setters
    public int getIdMatiere() { return idMatiere; }
    public void setIdMatiere(int idMatiere) { this.idMatiere = idMatiere; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUnite() { return unite; }
    public void setUnite(String unite) { this.unite = unite; }

    public double getStock() { return stock; }
    public void setStock(double stock) { this.stock = stock; }

    public double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(double prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public double getSeuilCritique() { return seuilCritique; }
    public void setSeuilCritique(double seuilCritique) { this.seuilCritique = seuilCritique; }

    @Override
    public String toString() {
        return nom + " (" + unite + ")";
    }
}
