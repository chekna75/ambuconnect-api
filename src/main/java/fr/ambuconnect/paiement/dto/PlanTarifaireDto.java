package fr.ambuconnect.paiement.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PlanTarifaireDto {

    private UUID id;
    private String nom;
    private String code;
    private String description;
    private Double montantMensuel;
    private String devise;
    private String stripeProductId;
    private String stripePriceId;
    private LocalDate dateCreation;
    private Boolean actif;
    private Integer nbMaxChauffeurs;
    private Integer nbMaxConnexionsSimultanees;
    private Integer seuilAlerteChauffeurs;
} 