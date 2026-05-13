package tn.esprit.farmai.models;

/**
 * Service offered by a fournisseur.
 * Maps to erp_service table.
 */
public class ServiceERP {

    private int idService;
    private String nom;
    private String description;
    private double prix;
    private int stock;
    private int seuilCritique;

    public ServiceERP() {
        this.prix = 0.0;
        this.stock = 0;
        this.seuilCritique = 0;
    }

    public ServiceERP(String nom, String description, double prix, int stock, int seuilCritique) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.stock = stock;
        this.seuilCritique = seuilCritique;
    }

    public boolean isStockCritique() {
        return seuilCritique > 0 && stock <= seuilCritique;
    }

    // Getters & Setters
    public int getIdService() { return idService; }
    public void setIdService(int idService) { this.idService = idService; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getSeuilCritique() { return seuilCritique; }
    public void setSeuilCritique(int seuilCritique) { this.seuilCritique = seuilCritique; }

    @Override
    public String toString() {
        return nom + " (" + prix + " TND)";
    }
}
