package fr.ambuconnect.rh.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import fr.ambuconnect.rh.enums.StatutDemande;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "conges")
@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class CongeEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "chauffeur_id")
    private ChauffeurEntity chauffeur;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "motif", nullable = false)
    private String motif;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutDemande statut;
    
    @Column(name = "commentaire", nullable = false)
    private String commentaire;

    @Column(name = "date_creation", nullable = false)
    private LocalDate dateCreation;
} 