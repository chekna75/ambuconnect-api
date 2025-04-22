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
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(nullable = false, name = "patient_name")
    private String patientName;

    @Column(nullable = false, name = "phone_number")
    private String phoneNumber;

    @Column(nullable = false, name = "pickup_latitude")
    private Double pickupLatitude;

    @Column(nullable = false, name = "pickup_longitude")
    private Double pickupLongitude;

    @Column(nullable = false, name = "pickup_address")
    private String pickupAddress;

    @Column(nullable = false, name = "destination_latitude")
    private Double destinationLatitude;

    @Column(nullable = false, name = "destination_longitude")
    private Double destinationLongitude;

    @Column(nullable = false, name = "destination_address")
    private String destinationAddress;

    @Column(nullable = false, name = "requested_time")
    private LocalDateTime requestedTime;

    @Column(nullable = false, name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @ManyToOne
    @JoinColumn(name = "assigned_entreprise_id")
    private EntrepriseEntity assignedEntreprise;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
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