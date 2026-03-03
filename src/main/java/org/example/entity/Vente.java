package org.example.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité Achat - Commande / bon d'achat.
 */
public class Vente {
    private int idVente;
    private LocalDate dateVente;
    private double total;
    private List<ListeVente> lignes = new ArrayList<>();

    public Vente() {
    }

    public Vente(LocalDate dateVente, double total) {
        this.dateVente = dateVente;
        this.total = total;
    }

    public int getIdVente() {
        return idVente;
    }

    public void setIdVente(int idVente) {
        this.idVente = idVente;
    }

    public LocalDate getDateVente() {
        return dateVente;
    }

    public void setDateVente(LocalDate dateVente) {
        this.dateVente = dateVente;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public List<ListeVente> getLignes() {
        return lignes;
    }

    public void setLignes(List<ListeVente> lignes) {
        this.lignes = lignes;
    }

    @Override
    public String toString() {
        return "Vente #" + idVente + " - " + dateVente + " - " + total + " €";
    }
}
