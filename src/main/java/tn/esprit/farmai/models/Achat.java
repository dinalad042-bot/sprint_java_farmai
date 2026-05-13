package tn.esprit.farmai.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Purchase order — buying raw materials (Matieres).
 * Maps to erp_achat table.
 */
public class Achat {

    private int idAchat;
    private LocalDate dateAchat;
    private double total;
    private boolean paid;
    private List<LigneAchat> lignes = new ArrayList<>();

    public Achat() {
        this.dateAchat = LocalDate.now();
        this.total = 0.0;
        this.paid = false;
    }

    public void recalculateTotal() {
        this.total = lignes.stream().mapToDouble(LigneAchat::getSousTotal).sum();
    }

    // Getters & Setters
    public int getIdAchat() { return idAchat; }
    public void setIdAchat(int idAchat) { this.idAchat = idAchat; }

    public LocalDate getDateAchat() { return dateAchat; }
    public void setDateAchat(LocalDate dateAchat) { this.dateAchat = dateAchat; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public List<LigneAchat> getLignes() { return lignes; }
    public void setLignes(List<LigneAchat> lignes) { this.lignes = lignes; }

    public void addLigne(LigneAchat ligne) {
        ligne.setIdAchat(this.idAchat);
        this.lignes.add(ligne);
    }
}
