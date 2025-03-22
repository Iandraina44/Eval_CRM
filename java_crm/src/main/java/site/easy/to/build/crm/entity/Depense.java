package site.easy.to.build.crm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "depense")
public class Depense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer depenseId;

    @Column(name = "valeur_depense", nullable = false, precision = 10, scale = 2)
    private double valeurDepense;

    @Column(name = "date_depense", nullable = false)
    private LocalDateTime dateDepense;

    @Column(name = "etat", nullable = false)
    private int etat;

    @ManyToOne
    @JoinColumn(name = "lead_id")
    private int lead;

    @ManyToOne
    @JoinColumn(name = "ticket_id")
    private int ticket;

    public Integer getDepenseId() {
        return depenseId;
    }

    public void setDepenseId(Integer depenseId) {
        this.depenseId = depenseId;
    }

    public double getValeurDepense() {
        return valeurDepense;
    }

    public void setValeurDepense(double valeurDepense) {
        this.valeurDepense = valeurDepense;
    }

    public LocalDateTime getDateDepense() {
        return dateDepense;
    }

    public void setDateDepense(LocalDateTime dateDepense) {
        this.dateDepense = dateDepense;
    }

    public int getEtat() {
        return etat;
    }

    public void setEtat(int etat) {
        this.etat = etat;
    }

    public int getLead() {
        return lead;
    }

    public void setLead(int lead) {
        this.lead = lead;
    }

    public int getTicket() {
        return ticket;
    }

    public void setTicket(int ticket) {
        this.ticket = ticket;
    }

    // Getters and Setters
}