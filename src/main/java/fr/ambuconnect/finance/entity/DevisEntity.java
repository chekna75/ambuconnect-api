package fr.ambuconnect.finance.entity;

import fr.ambuconnect.courses.entity.DemandePriseEnChargeEntity;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import fr.ambuconnect.patient.entity.PatientEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Entity
@Table(name = "devis")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class DevisEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "demande_id", nullable = false)
    private DemandePriseEnChargeEntity demande;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @ManyToOne
    @JoinColumn(name = "entreprise_id", nullable = false)
    private EntrepriseEntity entreprise;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_validite")
    private LocalDateTime dateValidite;

    @Column(name = "montant_base", nullable = false)
    private BigDecimal montantBase;

    @Column(name = "montant_majoration")
    private BigDecimal montantMajoration;

    @Column(name = "montant_total", nullable = false)
    private BigDecimal montantTotal;

    @Column(name = "distance_estimee")
    private BigDecimal distanceEstimee;

    @Column(name = "type_transport")
    @Enumerated(EnumType.STRING)
    private TypeTransport typeTransport;

    @Column(name = "statut")
    @Enumerated(EnumType.STRING)
    private StatutDevis statut;

    @Column(name = "taux_remboursement")
    private Integer tauxRemboursement; // en pourcentage

    @Column(name = "montant_remboursement")
    private BigDecimal montantRemboursement;

    @Column(name = "reste_a_charge")
    private BigDecimal resteACharge;

    public enum TypeTransport {
        AMBULANCE,
        VSL,
        TAXI_CONVENTIONNÉ
    }

    public enum StatutDevis {
        EN_ATTENTE,
        ACCEPTE,
        REFUSE,
        EXPIRE
    }

    // Méthodes utilitaires
    public static List<DevisEntity> findByPatient(UUID patientId) {
        return list("patient.id = ?1", patientId);
    }

    public static List<DevisEntity> findByEntreprise(UUID entrepriseId) {
        return list("entreprise.id = ?1", entrepriseId);
    }

    public static List<DevisEntity> findEnAttente() {
        return list("statut = ?1", StatutDevis.EN_ATTENTE);
    }
} 