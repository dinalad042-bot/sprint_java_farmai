package org.example.entity;

/**
 * Entité ListeAchat - Ligne d'un achat (table associative N:M).
 */
public class ListeAchat {
    private int id;
    private int idAchat;
    private int idService;
    private int quantite;
    private double prixUnitaire;

    // Références optionnelles pour affichage
    private String nomService;

    public ListeAchat() {
    }

    public ListeAchat(int idAchat, int idService, int quantite, double prixUnitaire) {
        this.idAchat = idAchat;
        this.idService = idService;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdAchat() {
        return idAchat;
    }

    public void setIdAchat(int idAchat) {
        this.idAchat = idAchat;
    }

    public int getIdService() {
        return idService;
    }

    public void setIdService(int idService) {
        this.idService = idService;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public double getSousTotal() {
        return quantite * prixUnitaire;
    }

    public String getNomService() {
        return nomService;
    }

    public void setNomService(String nomService) {
        this.nomService = nomService;
    }
}
