package fr.ambuconnect.courses.entity;


import java.time.LocalDateTime;
import java.util.UUID;
import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import fr.ambuconnect.ambulances.entity.AmbulanceEntity;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import fr.ambuconnect.patient.entity.PatientEntity;
import fr.ambuconnect.planning.entity.PlannnigEntity;
import fr.ambuconnect.planning.enums.StatutEnum;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(name = "courses")
@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class CoursesEntity  extends PanacheEntityBase{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "date_heure_depart", nullable = true)
    private LocalDateTime dateHeureDepart;

    @Column(name = "adresse_depart", nullable = true)
    private String adresseDepart;

    @Column(name = "adresse_arrivee", nullable = false)
    private String adresseArrivee;

    @Column(name = "distance")
    private BigDecimal distance;

    @Column(name = "date_heure_arrivee")
    private LocalDateTime dateHeureArrivee;

    @Column(name = "informations_supplementaires")
    private String informationsSupplementaires;

    @Column(name = "information_patient")
    private String informationPatient;

    @Column(name = "information_courses")
    private String informationCourses;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "temps_trajet_estime")
    private Integer tempsTrajetEstime; // en minutes

    @Column(name = "temps_trajet_reel")
    private Integer tempsTrajetReel; // en minutes

    @Column(name = "distance_estimee")
    private BigDecimal distanceEstimee; // en kilomètres

    @Column(name = "latitude_depart")
    private Double latitudeDepart;

    @Column(name = "longitude_depart")
    private Double longitudeDepart;

    @Column(name = "latitude_arrivee")
    private Double latitudeArrivee;

    @Column(name = "longitude_arrivee")
    private Double longitudeArrivee;

    @ManyToOne
    @JoinColumn(name = "chauffeur_id")
    private ChauffeurEntity chauffeur;

    @ManyToOne
    @JoinColumn(name = "ambulance_id")
    private AmbulanceEntity ambulance;

    @Column(name = "statut")
    @Enumerated(EnumType.STRING)
    private StatutEnum statut;

    @ManyToOne
    @JoinColumn(name = "planning_id")
    private PlannnigEntity planning; // Ajout de la relation

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private PatientEntity patient; // Ajout de la relation

    @ManyToOne
    @JoinColumn(name = "entreprise_id")
    private EntrepriseEntity entreprise;
}
