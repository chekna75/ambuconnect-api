package fr.ambuconnect.courses.entity;

import fr.ambuconnect.patient.entity.InformationsMedicalesEntity;
import fr.ambuconnect.patient.entity.PatientEntity;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Entity
@Table(name = "demande_prise_en_charge")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class DemandePriseEnChargeEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @OneToOne
    @JoinColumn(name = "informations_medicales_id")
    private InformationsMedicalesEntity informationsMedicales;

    @ManyToOne
    @JoinColumn(name = "entreprise_id")
    private EntrepriseEntity entrepriseAssignee;

    @Column(name = "date_demande", nullable = false)
    private LocalDateTime dateDemande;

    @Column(name = "date_prise_en_charge_souhaitee", nullable = false)
    private LocalDateTime datePriseEnChargeSouhaitee;

    @Column(name = "adresse_depart", nullable = false)
    private String adresseDepart;

    @Column(name = "adresse_destination", nullable = false)
    private String adresseArrivee;

    @Column(name = "latitude_depart")
    private Double latitudeDepart;

    @Column(name = "longitude_depart")
    private Double longitudeDepart;

    @Column(name = "latitude_arrivee")
    private Double latitudeArrivee;

    @Column(name = "longitude_arrivee")
    private Double longitudeArrivee;

    @Column(name = "statut")
    @Enumerated(EnumType.STRING)
    private StatutDemande statut;

    @Column(name = "urgence")
    private Boolean urgence;

    @Column(name = "commentaires")
    private String commentaires;

    @Column(name = "documents_validation")
    private Boolean documentsValidation;

    public enum StatutDemande {
        EN_ATTENTE,
        VALIDEE,
        ASSIGNEE,
        REFUSEE,
        ANNULEE,
        TERMINEE
    }

    // Méthodes utilitaires
    public static List<DemandePriseEnChargeEntity> findDemandesEnAttente() {
        return list("statut = ?1", StatutDemande.EN_ATTENTE);
    }

    public static List<DemandePriseEnChargeEntity> findByPatient(UUID patientId) {
        return list("patient.id = ?1", patientId);
    }

    public static List<DemandePriseEnChargeEntity> findByEntreprise(UUID entrepriseId) {
        return list("entrepriseAssignee.id = ?1", entrepriseId);
    }
} 