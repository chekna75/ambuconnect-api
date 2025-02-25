package fr.ambuconnect.rh.entity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.rh.enums.StatutCongeEnum;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "demandes_conge")
@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class DemandeCongeEntity extends PanacheEntityBase{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    public UUID id;

    @ManyToOne
    @JoinColumn(name = "chauffeur_id", nullable = false)
    public ChauffeurEntity chauffeur;

    @Column(name = "dateDebut",nullable = false)
    public LocalDate dateDebut;

    @Column(name = "dateFin", nullable = false)
    public LocalDate dateFin;

    @Column(name = "statut", nullable = false)
    @Enumerated(EnumType.STRING)
    public StatutCongeEnum statut;

    @Column(name = "commentaire")
    public String commentaire;

    public static List<DemandeCongeEntity> findByChauffeurAndStatut(ChauffeurEntity chauffeur, StatutCongeEnum statut) {
        return find("chauffeur = ?1 and statut = ?2", chauffeur, statut).list();
    }
}
