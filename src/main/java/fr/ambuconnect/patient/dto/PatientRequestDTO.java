package fr.ambuconnect.patient.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import fr.ambuconnect.patient.entity.enums.PatientRequestStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientRequestDTO {
    private UUID id;

    @NotBlank(message = "Le nom du patient est obligatoire")
    private String patientName;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    private String phoneNumber;

    @NotNull(message = "La latitude du point de départ est obligatoire")
    private Double pickupLatitude;

    @NotNull(message = "La longitude du point de départ est obligatoire")
    private Double pickupLongitude;

    @NotBlank(message = "L'adresse de départ est obligatoire")
    private String pickupAddress;

    @NotNull(message = "La latitude de destination est obligatoire")
    private Double destinationLatitude;

    @NotNull(message = "La longitude de destination est obligatoire")
    private Double destinationLongitude;

    @NotBlank(message = "L'adresse de destination est obligatoire")
    private String destinationAddress;

    private LocalDateTime requestedTime;
    
    @NotNull(message = "L'heure prévue est obligatoire")
    private LocalDateTime scheduledTime;

    private UUID assignedEntrepriseId;
    private PatientRequestStatus status;
    private String additionalNotes;
} 