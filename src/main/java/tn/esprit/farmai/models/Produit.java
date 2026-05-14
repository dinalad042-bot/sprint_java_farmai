package tn.esprit.farmai.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Finished product — produced from raw materials or sold directly.
 * Maps to erp_produit table.
 */
public class Produit {

    private int idProduit;
    private String nom;
    private String description;
    private double prixVente;
    private double quantiteProduite;
    private double stock;
    private boolean isSimple;

    /** Recipe ingredients (loaded on demand) */
    private List<RecetteIngredient> recette = new ArrayList<>();

    public Produit() {
        this.prixVente = 0.0;
        this.quantiteProduite = 1.0;
        this.stock = 0.0;
        this.isSimple = false;
    }

    public Produit(String nom, String description, double prixVente, double quantiteProduite, double stock, boolean isSimple) {
        this.nom = nom;
        this.description = description;
        this.prixVente = prixVente;
        this.quantiteProduite = quantiteProduite;
        this.stock = stock;
        this.isSimple = isSimple;
    }

    // Getters & Setters
    public int getIdProduit() { return idProduit; }
    public void setIdProduit(int idProduit) { this.idProduit = idProduit; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrixVente() { return prixVente; }
    public void setPrixVente(double prixVente) { this.prixVente = prixVente; }

    public double getQuantiteProduite() { return quantiteProduite; }
    public void setQuantiteProduite(double quantiteProduite) { this.quantiteProduite = quantiteProduite; }

    public double getStock() { return stock; }
    public void setStock(double stock) { this.stock = stock; }

    public boolean isSimple() { return isSimple; }
    public void setSimple(boolean simple) { isSimple = simple; }

    public List<RecetteIngredient> getRecette() { return recette; }
    public void setRecette(List<RecetteIngredient> recette) { this.recette = recette; }

    @Override
    public String toString() {
        return nom + " (stock: " + stock + ")";
    }
}
