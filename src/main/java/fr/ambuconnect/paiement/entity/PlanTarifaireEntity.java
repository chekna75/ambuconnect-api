package fr.ambuconnect.paiement.entity;

import java.time.LocalDate;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "plans_tarifaires")
public class PlanTarifaireEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "nom", nullable = false)
    private String nom;
    
    @Column(name = "code", nullable = false, unique = true)
    private String code;
    
    @Column(name = "description")
    private String description;

    @Column(name = "montant_mensuel", nullable = false)
    private Double montantMensuel;
    
    @Column(name = "devise", nullable = false)
    private String devise;
    
    @Column(name = "stripe_product_id")
    private String stripeProductId;
    
    @Column(name = "stripe_price_id")
    private String stripePriceId;
    
    @Column(name = "date_creation")
    private LocalDate dateCreation;
    
    @Column(name = "actif")
    private Boolean actif;
    
    @Column(name = "nb_max_chauffeurs")
    private Integer nbMaxChauffeurs;
    
    @Column(name = "nb_max_connexions_simultanees")
    private Integer nbMaxConnexionsSimultanees;
    
    @Column(name = "seuil_alerte_chauffeurs")
    private Integer seuilAlerteChauffeurs; // Pourcentage à partir duquel on alerte (ex: 90%)
    
    // Méthodes de recherche
    
    public static PlanTarifaireEntity findByCode(String code) {
        return find("code", code).firstResult();
    }
    
    public static PlanTarifaireEntity findByStripePriceId(String stripePriceId) {
        return find("stripePriceId", stripePriceId).firstResult();
    }
} 