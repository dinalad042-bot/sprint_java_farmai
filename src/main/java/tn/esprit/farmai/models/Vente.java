package tn.esprit.farmai.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Sale order — selling finished products (Produits).
 * Maps to erp_vente table.
 */
public class Vente {

    private int idVente;
    private LocalDate dateVente;
    private double total;
    private List<LigneVente> lignes = new ArrayList<>();

    public Vente() {
        this.dateVente = LocalDate.now();
        this.total = 0.0;
    }

    public void recalculateTotal() {
        this.total = lignes.stream().mapToDouble(LigneVente::getSousTotal).sum();
    }

    // Getters & Setters
    public int getIdVente() { return idVente; }
    public void setIdVente(int idVente) { this.idVente = idVente; }

    public LocalDate getDateVente() { return dateVente; }
    public void setDateVente(LocalDate dateVente) { this.dateVente = dateVente; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public List<LigneVente> getLignes() { return lignes; }
    public void setLignes(List<LigneVente> lignes) { this.lignes = lignes; }

    public void addLigne(LigneVente ligne) {
        ligne.setIdVente(this.idVente);
        this.lignes.add(ligne);
    }
}
