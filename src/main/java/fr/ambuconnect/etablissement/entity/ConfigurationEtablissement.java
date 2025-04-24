package fr.ambuconnect.etablissement.entity;

import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import lombok.NoArgsConstructor;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "configurations_etablissement")
public class ConfigurationEtablissement extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etablissement_id", nullable = false)
    private EtablissementSante etablissement;

    // Heures d'ouverture
    @Column(name = "lundi_debut")
    private LocalTime lundiDebut;

    @Column(name = "lundi_fin")
    private LocalTime lundiFin;

    @Column(name = "mardi_debut")
    private LocalTime mardiDebut;

    @Column(name = "mardi_fin")
    private LocalTime mardiFin;

    @Column(name = "mercredi_debut")
    private LocalTime mercrediDebut;

    @Column(name = "mercredi_fin")
    private LocalTime mercrediFin;

    @Column(name = "jeudi_debut")
    private LocalTime jeudiDebut;

    @Column(name = "jeudi_fin")
    private LocalTime jeudiFin;

    @Column(name = "vendredi_debut")
    private LocalTime vendrediDebut;

    @Column(name = "vendredi_fin")
    private LocalTime vendrediFin;

    @Column(name = "samedi_debut")
    private LocalTime samediDebut;

    @Column(name = "samedi_fin")
    private LocalTime samediFin;

    @Column(name = "dimanche_debut")
    private LocalTime dimancheDebut;

    @Column(name = "dimanche_fin")
    private LocalTime dimancheFin;

    // Sociétés préférées
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "etablissement_societes_preferees",
        joinColumns = @JoinColumn(name = "configuration_id")
    )
    @Column(name = "societe_id")
    private Set<UUID> societesPreferees;

    // Tarifs négociés
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "etablissement_tarifs_negocies",
        joinColumns = @JoinColumn(name = "configuration_id")
    )
    private Set<TarifNegocie> tarifsNegocies;
} 