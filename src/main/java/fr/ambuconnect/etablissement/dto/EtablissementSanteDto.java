package fr.ambuconnect.etablissement.dto;

import java.util.UUID;

import fr.ambuconnect.etablissement.entity.TypeEtablissement;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtablissementSanteDto {

    private UUID id;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotNull(message = "Le type d'établissement est obligatoire")
    private TypeEtablissement typeEtablissement;

    @NotBlank(message = "L'adresse est obligatoire")
    private String adresse;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String emailContact;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(regexp = "^[0-9]{10}$", message = "Le numéro de téléphone doit contenir 10 chiffres")
    private String telephoneContact;

    @NotNull(message = "Le responsable référent est obligatoire")
    private UUID responsableReferentId;

    private boolean active;
} 