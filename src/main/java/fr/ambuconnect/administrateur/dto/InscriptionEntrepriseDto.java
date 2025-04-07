package fr.ambuconnect.administrateur.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import fr.ambuconnect.entreprise.dto.EntrepriseDto;

/**
 * DTO pour l'inscription d'une entreprise avec son administrateur
 * Utilisé lors de l'inscription depuis le site vitrine après paiement
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InscriptionEntrepriseDto {

    /**
     * Informations de l'administrateur
     * Si null, on créera un administrateur avec les informations d'entreprise
     */
    @Valid
    private AdministrateurDto administrateur;

    /**
     * Informations de l'entreprise
     */
    @NotNull(message = "Les informations de l'entreprise sont obligatoires")
    @Valid
    private EntrepriseDto entreprise;

    /**
     * Email de l'administrateur qui sera créé
     * Utilisé si administrateur est null
     */
    @Email(message = "L'email doit être valide")
    private String email;

    /**
     * Mot de passe de l'administrateur qui sera créé
     * Utilisé si administrateur est null
     */
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String motDePasse;

    /**
     * Nom de l'administrateur qui sera créé
     * Utilisé si administrateur est null
     */
    private String nom;

    /**
     * Prénom de l'administrateur qui sera créé
     * Utilisé si administrateur est null
     */
    private String prenom;

    /**
     * Téléphone de l'administrateur qui sera créé
     * Utilisé si administrateur est null
     */
    @Pattern(regexp = "^(\\+33|0)[1-9](\\d{8})$", message = "Le numéro de téléphone doit être valide")
    private String telephone;

    /**
     * Type d'abonnement souscrit (basic, standard, premium, etc.)
     * @deprecated Remplacé par subscriptionType, gardé pour rétrocompatibilité
     */
    private String typeAbonnement;

    /**
     * Type d'abonnement souscrit (START, PRO, ENTREPRISE)
     */
    private String subscriptionType;

    /**
     * Méthode getter personnalisée qui retourne typeAbonnement ou subscriptionType
     * Permet de maintenir la compatibilité tout en supportant le nouveau format
     * Cette méthode est utilisée par la validation
     */
    @NotBlank(message = "Le type d'abonnement est obligatoire")
    public String getTypeAbonnementEffectif() {
        if (typeAbonnement != null && !typeAbonnement.isEmpty()) {
            return typeAbonnement;
        }
        return subscriptionType;
    }

    /**
     * Informations concernant le paiement et l'abonnement Stripe
     */
    private String stripeCustomerId;
    
    /**
     * ID du paiement Stripe (PaymentIntent ID)
     */
    private String stripePaymentIntentId;
    
    /**
     * ID de l'abonnement Stripe (Subscription ID)
     * Ce champ est obligatoire mais non annoté avec @NotBlank pour permettre une
     * validation personnalisée dans le controller
     */
    private String stripeSubscriptionId;

    /**
     * Code du plan tarifaire (START, PRO, ENTREPRISE)
     */
    private String codeAbonnement;
} 