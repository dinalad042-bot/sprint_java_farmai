package tn.esprit.farmai.models;

/**
 * One ingredient in a product recipe.
 * Maps to erp_recette_ingredient table.
 */
public class RecetteIngredient {

    private int id;
    private int idProduit;
    private int idMatiere;
    private double quantite;

    /** Loaded for display purposes */
    private String nomMatiere;
    private String uniteMatiere;

    public RecetteIngredient() {
        this.quantite = 1.0;
    }

    public RecetteIngredient(int idProduit, int idMatiere, double quantite) {
        this.idProduit = idProduit;
        this.idMatiere = idMatiere;
        this.quantite = quantite;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdProduit() { return idProduit; }
    public void setIdProduit(int idProduit) { this.idProduit = idProduit; }

    public int getIdMatiere() { return idMatiere; }
    public void setIdMatiere(int idMatiere) { this.idMatiere = idMatiere; }

    public double getQuantite() { return quantite; }
    public void setQuantite(double quantite) { this.quantite = quantite; }

    public String getNomMatiere() { return nomMatiere; }
    public void setNomMatiere(String nomMatiere) { this.nomMatiere = nomMatiere; }

    public String getUniteMatiere() { return uniteMatiere; }
    public void setUniteMatiere(String uniteMatiere) { this.uniteMatiere = uniteMatiere; }
}
