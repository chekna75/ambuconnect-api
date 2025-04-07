package fr.ambuconnect.paiement.entity;

import java.time.LocalDate;
import java.util.UUID;

import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "abonnements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AbonnementEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "entreprise_id")
    private EntrepriseEntity entreprise;

    @Column(name = "stripe_subscription_id", nullable = false, unique = true)
    private String stripeSubscriptionId;

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(name = "plan_id")
    private UUID planId;

    @Column(name = "statut")
    private String statut;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "date_prochain_paiement")
    private LocalDate dateProchainPaiement;

    @Column(name = "montant_mensuel")
    private Double montantMensuel;

    @Column(name = "prix_mensuel", nullable = false)
    private Double prixMensuel;

    @Column(name = "devise")
    private String devise;

    @Column(name = "actif")
    private boolean actif;

    @Column(name = "type", nullable = false)
    private String type;

    // Champs pour les webhooks
    @Column(name = "date_dernier_paiement")
    private LocalDate dateDernierPaiement;

    @Column(name = "statut_dernier_paiement")
    private String statutDernierPaiement;

    /**
     * Trouve un abonnement par son ID Stripe
     * 
     * @param stripeSubscriptionId L'ID d'abonnement Stripe
     * @return L'abonnement correspondant ou null
     */
    public static AbonnementEntity findByStripeSubscriptionId(String stripeSubscriptionId) {
        return find("stripeSubscriptionId", stripeSubscriptionId).firstResult();
    }

    /**
     * Trouve tous les abonnements d'une entreprise
     * 
     * @param entrepriseId L'ID de l'entreprise
     * @return La liste des abonnements
     */
    public static java.util.List<AbonnementEntity> findByEntrepriseId(UUID entrepriseId) {
        return find("entreprise.id", entrepriseId).list();
    }

    /**
     * Trouve l'abonnement actif d'une entreprise
     * 
     * @param entrepriseId L'ID de l'entreprise
     * @return L'abonnement actif ou null
     */
    public static AbonnementEntity findActiveByEntrepriseId(UUID entrepriseId) {
        return find("entreprise.id = ?1 and actif = true", entrepriseId).firstResult();
    }
} 