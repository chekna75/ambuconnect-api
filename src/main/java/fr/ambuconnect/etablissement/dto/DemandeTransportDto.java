package fr.ambuconnect.etablissement.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import fr.ambuconnect.etablissement.entity.StatusDemande;
import fr.ambuconnect.etablissement.entity.TypeTransport;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeTransportDto {

    private UUID id;

    @NotNull(message = "L'établissement est obligatoire")
    private UUID etablissementId;

    private UUID createdById;

    @NotNull(message = "Le patient est obligatoire")
    private UUID patientId;

    @NotBlank(message = "L'adresse de départ est obligatoire")
    private String adresseDepart;

    @NotBlank(message = "L'adresse d'arrivée est obligatoire")
    private String adresseArrivee;

    @NotNull(message = "L'horaire souhaité est obligatoire")
    @Future(message = "L'horaire souhaité doit être dans le futur")
    private LocalDateTime horaireSouhaite;

    @NotNull(message = "Le type de transport est obligatoire")
    private TypeTransport typeTransport;

    private StatusDemande status = StatusDemande.EN_ATTENTE;

    private UUID societeAffecteeId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Informations complémentaires pour l'affichage
    private String nomPatient;
    private String prenomPatient;
    private String nomSociete;
    private String nomCreateur;
    private String prenomCreateur;
} 