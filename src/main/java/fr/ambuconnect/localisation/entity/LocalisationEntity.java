package fr.ambuconnect.localisation.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "localisation")
public class LocalisationEntity extends PanacheEntityBase{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "date_heure")
    private LocalDateTime dateHeure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chauffeur_id")
    private ChauffeurEntity chauffeur;

    public static List<LocalisationEntity> findByChauffeurId(UUID id) {
        return list("chauffeur.id", id);
    }

    public static List<LocalisationEntity> findByEntrepriseId(UUID entrepriseId) {
        return find("chauffeur.entreprise.id", entrepriseId).list();
    }

}
