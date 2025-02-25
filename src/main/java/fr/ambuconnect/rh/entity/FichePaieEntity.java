package fr.ambuconnect.rh.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import fr.ambuconnect.rh.enums.StatutFichePaie;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Data;

@Entity
@Table(name = "fiches_paie")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FichePaieEntity extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "chauffeur_id", nullable = false)
    private ChauffeurEntity chauffeur;

    @ManyToOne
    @JoinColumn(name = "entreprise_id", nullable = false)
    private EntrepriseEntity entreprise;

    @Column(name = "periode_debut", nullable = false)
    private LocalDate periodeDebut;

    @Column(name = "periode_fin", nullable = false)
    private LocalDate periodeFin;

    // Mode de calcul
    @Column(name = "is_forfait_jour")
    private boolean forfaitJour;

    @Column(name = "forfait_journalier")
    private BigDecimal forfaitJournalier;

    // Rémunération
    @Column(name = "salaire_base", nullable = false)
    private BigDecimal salaireBase;

    @Column(name = "heures_travaillees")
    private Double heuresTravaillees;

    @Column(name = "heures_supplementaires")
    private Double heuresSupplementaires;

    @Column(name = "taux_horaire")
    private BigDecimal tauxHoraire;

    // Primes et indemnités
    @Column(name = "prime_anciennete")
    private BigDecimal primeAnciennete;

    @Column(name = "indemnite_transport")
    private BigDecimal indemniteTransport;

    @Column(name = "indemnite_repas")
    private BigDecimal indemniteRepas;

    // Cotisations sociales
    @Column(name = "cotisation_maladie")
    private BigDecimal cotisationMaladie;

    @Column(name = "cotisation_securite_sociale")
    private BigDecimal cotisationSecuriteSociale;

    @Column(name = "cotisation_retraite")
    private BigDecimal cotisationRetraite;

    @Column(name = "cotisation_chomage")
    private BigDecimal cotisationChomage;

    @Column(name = "cotisation_agff")
    private BigDecimal cotisationAGFF;

    @Column(name = "contribution_csg")
    private BigDecimal contributionCSG;

    @Column(name = "contribution_crds")
    private BigDecimal contributionCRDS;

    // Totaux
    @Column(name = "total_brut", nullable = false)
    private BigDecimal totalBrut;

    @Column(name = "total_cotisations", nullable = false)
    private BigDecimal totalCotisations;

    @Column(name = "net_imposable", nullable = false)
    private BigDecimal netImposable;

    @Column(name = "net_a_payer", nullable = false)
    private BigDecimal netAPayer;

    // Métadonnées
    @Column(name = "date_creation", nullable = false)
    private LocalDate dateCreation;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutFichePaie statut;

    // Cumuls annuels
    @Column(name = "cumul_brut_annuel")
    private BigDecimal cumulBrutAnnuel;

    @Column(name = "cumul_net_imposable")
    private BigDecimal cumulNetImposable;

    @Column(name = "cumul_net_a_payer")
    private BigDecimal cumulNetAPayer;

    @Column(name = "archive")
    private boolean archive;

    @Column(name = "date_archivage")
    private LocalDate dateArchivage;
}
