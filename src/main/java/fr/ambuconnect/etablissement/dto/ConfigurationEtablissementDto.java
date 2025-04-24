package fr.ambuconnect.etablissement.dto;

import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationEtablissementDto {

    private UUID id;

    @NotNull(message = "L'établissement est obligatoire")
    private UUID etablissementId;

    // Heures d'ouverture
    private LocalTime lundiDebut;
    private LocalTime lundiFin;
    private LocalTime mardiDebut;
    private LocalTime mardiFin;
    private LocalTime mercrediDebut;
    private LocalTime mercrediFin;
    private LocalTime jeudiDebut;
    private LocalTime jeudiFin;
    private LocalTime vendrediDebut;
    private LocalTime vendrediFin;
    private LocalTime samediDebut;
    private LocalTime samediFin;
    private LocalTime dimancheDebut;
    private LocalTime dimancheFin;

    // Sociétés préférées
    private Set<UUID> societesPreferees;

    // Tarifs négociés
    @Valid
    private Set<TarifNegocieDto> tarifsNegocies;
} 