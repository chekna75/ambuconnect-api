package fr.ambuconnect.etablissement.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import fr.ambuconnect.etablissement.entity.CanalMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageEtablissementDto {

    private UUID id;

    @NotNull(message = "L'établissement est obligatoire")
    private UUID etablissementId;

    private UUID auteurId;

    @NotBlank(message = "Le message est obligatoire")
    private String message;

    private LocalDateTime dateEnvoi;

    @NotNull(message = "Le canal est obligatoire")
    private CanalMessage canal;

    private UUID demandeTransportId;

    // Informations complémentaires pour l'affichage
    private String nomAuteur;
    private String prenomAuteur;
} 