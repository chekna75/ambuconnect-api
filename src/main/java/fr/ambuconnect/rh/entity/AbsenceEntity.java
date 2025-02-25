package fr.ambuconnect.rh.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.rh.enums.TypeAbsence;
import fr.ambuconnect.rh.enums.StatutDemande;

@Entity
@Table(name = "absences")
public class AbsenceEntity extends PanacheEntityBase {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "chauffeur_id")
    private ChauffeurEntity chauffeur;
    
    @Column(nullable = false)
    private LocalDate dateDebut;
    
    @Column(nullable = false)
    private LocalDate dateFin;
    
    @Column(nullable = false)
    private LocalDate dateDemande;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeAbsence type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDemande statut;
    
    private String motif;
    private String commentaireValidation;
    private LocalDate dateValidation;
    
    public double getNombreJours() {
        return dateDebut.until(dateFin.plusDays(1)).getDays();
    }

    // Getters et Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public ChauffeurEntity getChauffeur() { return chauffeur; }
    public void setChauffeur(ChauffeurEntity chauffeur) { this.chauffeur = chauffeur; }
    
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    
    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    
    public LocalDate getDateDemande() { return dateDemande; }
    public void setDateDemande(LocalDate dateDemande) { this.dateDemande = dateDemande; }
    
    public TypeAbsence getType() { return type; }
    public void setType(TypeAbsence type) { this.type = type; }
    
    public StatutDemande getStatut() { return statut; }
    public void setStatut(StatutDemande statut) { this.statut = statut; }
    
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    
    public String getCommentaireValidation() { return commentaireValidation; }
    public void setCommentaireValidation(String commentaireValidation) { this.commentaireValidation = commentaireValidation; }
    
    public LocalDate getDateValidation() { return dateValidation; }
    public void setDateValidation(LocalDate dateValidation) { this.dateValidation = dateValidation; }
} 