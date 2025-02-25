package fr.ambuconnect.planning.entity;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;


import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.courses.entity.CoursesEntity;
import fr.ambuconnect.planning.enums.StatutEnum;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "planning")
@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PlannnigEntity extends PanacheEntityBase{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;

    @Column(name = "heure_fin", nullable = false)
    private LocalTime heureFin;

    @ManyToOne
    @JoinColumn(name = "chauffeur_id")
    private ChauffeurEntity chauffeur;

    @OneToMany(mappedBy = "planning")
    private List<CoursesEntity> course;

    @Column(name = "statut")
    @Enumerated(EnumType.STRING)
    private StatutEnum statut;
}

