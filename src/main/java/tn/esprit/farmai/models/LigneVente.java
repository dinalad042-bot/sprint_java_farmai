package tn.esprit.farmai.models;

/**
 * One line of a sale order.
 * Maps to erp_ligne_vente table.
 */
public class LigneVente {

    private int id;
    private int idVente;
    private int idProduit;
    private int quantite;
    private double prixUnitaire;

    /** Loaded for display */
    private String nomProduit;

    public LigneVente() {
        this.quantite = 1;
        this.prixUnitaire = 0.0;
    }

    public LigneVente(int idProduit, int quantite, double prixUnitaire) {
        this.idProduit = idProduit;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
    }

    public double getSousTotal() {
        return quantite * prixUnitaire;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdVente() { return idVente; }
    public void setIdVente(int idVente) { this.idVente = idVente; }

    public int getIdProduit() { return idProduit; }
    public void setIdProduit(int idProduit) { this.idProduit = idProduit; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(double prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public String getNomProduit() { return nomProduit; }
    public void setNomProduit(String nomProduit) { this.nomProduit = nomProduit; }
}
