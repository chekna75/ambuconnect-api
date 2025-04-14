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
@Table(name = "promo_codes")
public class PromoCodeEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "pourcentage_reduction", nullable = false)
    private Integer pourcentageReduction;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "nombre_utilisations_max")
    private Integer nombreUtilisationsMax;

    @Column(name = "nombre_utilisations_actuel", nullable = false)
    private Integer nombreUtilisationsActuel = 0;

    @Column(name = "actif", nullable = false)
    private Boolean actif = true;

    @Column(name = "description")
    private String description;

    // Méthodes utilitaires
    public static PromoCodeEntity findByCode(String code) {
        return find("code", code).firstResult();
    }

    public boolean isValide() {
        LocalDate now = LocalDate.now();
        return actif &&
               dateDebut.compareTo(now) <= 0 &&
               (dateFin == null || dateFin.compareTo(now) >= 0) &&
               (nombreUtilisationsMax == null || nombreUtilisationsActuel < nombreUtilisationsMax);
    }

    public void incrementerUtilisations() {
        this.nombreUtilisationsActuel++;
    }
} 