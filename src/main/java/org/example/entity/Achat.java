package org.example.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité Achat - Commande / bon d'achat.
 */
public class Achat {
    private int idAchat;
    private LocalDate dateAchat;
    private double total;
    private List<ListeAchat> lignes = new ArrayList<>();

    public Achat() {
    }

    public Achat(LocalDate dateAchat, double total) {
        this.dateAchat = dateAchat;
        this.total = total;
    }

    public int getIdAchat() {
        return idAchat;
    }

    public void setIdAchat(int idAchat) {
        this.idAchat = idAchat;
    }

    public LocalDate getDateAchat() {
        return dateAchat;
    }

    public void setDateAchat(LocalDate dateAchat) {
        this.dateAchat = dateAchat;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public List<ListeAchat> getLignes() {
        return lignes;
    }

    public void setLignes(List<ListeAchat> lignes) {
        this.lignes = lignes;
    }

    @Override
    public String toString() {
        return "Achat #" + idAchat + " - " + dateAchat + " - " + total + " €";
    }
}
