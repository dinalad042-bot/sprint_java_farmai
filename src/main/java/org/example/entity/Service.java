package org.example.entity;

/**
 * Entité Service - Produit ou service géré en stock.
 */
public class Service {
    private int idService;
    private String nom;
    private String description;
    private double prix;
    private int stock;
    private int seuilCritique;

    public Service() {
    }

    public Service(String nom, String description, double prix, int stock, int seuilCritique) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.stock = stock;
        this.seuilCritique = seuilCritique;
    }

    public int getIdService() {
        return idService;
    }

    public void setIdService(int idService) {
        this.idService = idService;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getSeuilCritique() {
        return seuilCritique;
    }

    public void setSeuilCritique(int seuilCritique) {
        this.seuilCritique = seuilCritique;
    }

    /** Retourne true si le stock est en dessous du seuil critique. */
    public boolean isStockCritique() {
        return stock < seuilCritique;
    }

    @Override
    public String toString() {
        return nom + " (stock: " + stock + ")";
    }
}
