package fr.ambuconnect.chauffeur.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "performances_chauffeurs")
public class PerformanceChauffeurEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chauffeur_id")
    private ChauffeurEntity chauffeur;

    @Column(name = "date_debut")
    private LocalDateTime dateDebut;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    @Column(name = "heures_travaillees")
    private Double heuresTravaillees;

    @Column(name = "nombre_courses")
    private Integer nombreCourses;

    @Column(name = "nombre_retards")
    private Integer nombreRetards;

    @Column(name = "note_moyenne_feedback")
    private Double noteMoyenneFeedback;

    @Column(name = "commentaires")
    private String commentaires;
} 