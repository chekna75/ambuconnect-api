package fr.ambuconnect.patient.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import fr.ambuconnect.patient.entity.enums.PatientRequestStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "patient_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientRequestEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String patientName;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private Double pickupLatitude;

    @Column(nullable = false)
    private Double pickupLongitude;

    @Column(nullable = false)
    private String pickupAddress;

    @Column(nullable = false)
    private Double destinationLatitude;

    @Column(nullable = false)
    private Double destinationLongitude;

    @Column(nullable = false)
    private String destinationAddress;

    @Column(nullable = false)
    private LocalDateTime requestedTime;

    @Column(nullable = false)
    private LocalDateTime scheduledTime;

    @ManyToOne
    @JoinColumn(name = "assigned_entreprise_id")
    private EntrepriseEntity assignedEntreprise;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PatientRequestStatus status;

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;

    public static List<PatientRequestEntity> findPendingRequests() {
        return list("status = ?1", PatientRequestStatus.PENDING);
    }

    public static List<PatientRequestEntity> findByEntreprise(UUID entrepriseId) {
        return list("assignedEntreprise.id = ?1", entrepriseId);
    }
} 