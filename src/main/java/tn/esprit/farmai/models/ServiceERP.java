package tn.esprit.farmai.models;

/**
 * Service ERP model — offered by fournisseur.
 * Maps to erp_service table.
 */
public class ServiceERP {

    private int    idService;
    private String nom;
    private String description;
    private double prix;
    private int    stock;
    private int    seuilCritique;

    public ServiceERP() {}

    public ServiceERP(String nom, String description, double prix, int stock, int seuilCritique) {
        this.nom           = nom;
        this.description   = description;
        this.prix          = prix;
        this.stock         = stock;
        this.seuilCritique = seuilCritique;
    }

    /** True when seuil_critique > 0 and stock is at or below threshold */
    public boolean isStockCritique() {
        return seuilCritique > 0 && stock <= seuilCritique;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int getIdService()                    { return idService; }
    public void setIdService(int idService)      { this.idService = idService; }

    public String getNom()                       { return nom; }
    public void setNom(String nom)               { this.nom = nom; }

    public String getDescription()               { return description; }
    public void setDescription(String desc)      { this.description = desc; }

    public double getPrix()                      { return prix; }
    public void setPrix(double prix)             { this.prix = prix; }

    public int getStock()                        { return stock; }
    public void setStock(int stock)              { this.stock = stock; }

    public int getSeuilCritique()                { return seuilCritique; }
    public void setSeuilCritique(int seuil)      { this.seuilCritique = seuil; }

    @Override
    public String toString() {
        return nom != null ? nom : "ServiceERP#" + idService;
    }
}
