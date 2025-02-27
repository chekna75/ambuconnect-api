package fr.ambuconnect.messagerie.entity;

import java.util.UUID;
import fr.ambuconnect.administrateur.entity.AdministrateurEntity;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.courses.entity.CoursesEntity;
import fr.ambuconnect.messagerie.enums.UserType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "messages")
public class MessagerieEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "contenu", nullable = false)
    private String contenu;

    @Column(name = "date_heure", nullable = false)
    private String dateHeure;

    @Column(name = "expediteur_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserType expediteurType;

    @Column(name = "destinataire_type", nullable = true)
    @Enumerated(EnumType.STRING)
    private UserType destinataireType;

    @Column(name = "expediteur_id")
    private UUID expediteurId;

    @Column(name = "destinataire_id")
    private UUID destinataireId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediteur_id", referencedColumnName = "id", insertable = false, updatable = false)
    private AdministrateurEntity expediteurAdmin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediteur_id", referencedColumnName = "id", insertable = false, updatable = false)
    private ChauffeurEntity expediteurChauffeur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire_id", referencedColumnName = "id", insertable = false, updatable = false)
    private AdministrateurEntity destinataireAdmin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire_id", referencedColumnName = "id", insertable = false, updatable = false)
    private ChauffeurEntity destinataireChauffeur;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private CoursesEntity course;

    // Méthodes utilitaires
    public Object getExpediteur() {
        return expediteurType == UserType.administrateur ? expediteurAdmin : expediteurChauffeur;
    }

    public void setExpediteur(Object expediteur) {
        if (expediteur instanceof AdministrateurEntity) {
            this.expediteurAdmin = (AdministrateurEntity) expediteur;
            this.expediteurChauffeur = null;
            this.expediteurType = UserType.administrateur;
            this.expediteurId = expediteurAdmin.getId();
        } else if (expediteur instanceof ChauffeurEntity) {
            this.expediteurChauffeur = (ChauffeurEntity) expediteur;
            this.expediteurAdmin = null;
            this.expediteurType = UserType.chauffeur;
            this.expediteurId = expediteurChauffeur.getId();
        }
    }

    public Object getDestinataire() {
        return destinataireType == UserType.administrateur ? destinataireAdmin : destinataireChauffeur;
    }

    public void setDestinataire(Object destinataire) {
        if (destinataire instanceof AdministrateurEntity) {
            this.destinataireAdmin = (AdministrateurEntity) destinataire;
            this.destinataireChauffeur = null;
            this.destinataireType = UserType.administrateur;
            this.destinataireId = destinataireAdmin.getId();
        } else if (destinataire instanceof ChauffeurEntity) {
            this.destinataireChauffeur = (ChauffeurEntity) destinataire;
            this.destinataireAdmin = null;
            this.destinataireType = UserType.chauffeur;
            this.destinataireId = destinataireChauffeur.getId();
        }
    }
}