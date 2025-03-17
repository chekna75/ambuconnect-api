package fr.ambuconnect.chauffeur.services;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import fr.ambuconnect.chauffeur.dto.PerformanceChauffeurDto;
import fr.ambuconnect.chauffeur.entity.PerformanceChauffeurEntity;
import fr.ambuconnect.chauffeur.mapper.PerformanceChauffeurMapper;
import fr.ambuconnect.chauffeur.dto.RapportMensuelDto;
import fr.ambuconnect.chauffeur.services.ChauffeurService;
import fr.ambuconnect.notification.services.NotificationService;
import fr.ambuconnect.chauffeur.dto.ChauffeurDto;

@ApplicationScoped
public class PerformanceChauffeurService {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    PerformanceChauffeurMapper mapper;

    @Inject
    NotificationService notificationService;

    @Inject
    ChauffeurService chauffeurService;

    @Transactional
    public PerformanceChauffeurDto enregistrerPerformance(PerformanceChauffeurDto dto) {
        PerformanceChauffeurEntity entity = mapper.toEntity(dto);
        entityManager.persist(entity);
        return mapper.toDto(entity);
    }

    public List<PerformanceChauffeurDto> getPerformancesChauffeur(UUID chauffeurId, LocalDateTime debut, LocalDateTime fin) {
        List<PerformanceChauffeurEntity> performances = PerformanceChauffeurEntity
            .find("chauffeur.id = ?1 AND dateDebut >= ?2 AND dateFin <= ?3", 
                  chauffeurId, debut, fin)
            .list();
        return performances.stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }

    public RapportMensuelDto genererRapportMensuel(UUID chauffeurId, LocalDateTime mois) {
        // Définir la période du mois
        LocalDateTime debutMois = mois.with(TemporalAdjusters.firstDayOfMonth());
        LocalDateTime finMois = mois.with(TemporalAdjusters.lastDayOfMonth());

        // Récupérer toutes les performances du mois
        List<PerformanceChauffeurEntity> performances = PerformanceChauffeurEntity
            .find("chauffeur.id = ?1 AND dateDebut >= ?2 AND dateFin <= ?3",
                  chauffeurId, debutMois, finMois)
            .list();

        RapportMensuelDto rapport = new RapportMensuelDto();
        rapport.setChauffeurId(chauffeurId);
        
        ChauffeurDto chauffeur = chauffeurService.findById(chauffeurId);
        rapport.setNomChauffeur(chauffeur.getNom() + " " + chauffeur.getPrenom());
        rapport.setMois(mois);

        // Calculer les statistiques
        rapport.setTotalHeuresTravaillees(
            performances.stream()
                .mapToDouble(PerformanceChauffeurEntity::getHeuresTravaillees)
                .sum()
        );

        rapport.setTotalCourses(
            performances.stream()
                .mapToInt(PerformanceChauffeurEntity::getNombreCourses)
                .sum()
        );

        rapport.setTauxRetard(
            performances.stream()
                .mapToDouble(p -> (double) p.getNombreRetards() / p.getNombreCourses())
                .average()
                .orElse(0.0)
        );

        rapport.setMoyenneFeedback(
            performances.stream()
                .mapToDouble(PerformanceChauffeurEntity::getNoteMoyenneFeedback)
                .average()
                .orElse(0.0)
        );

        // Évaluation globale
        rapport.setEvaluation(evaluerPerformance(rapport));
        rapport.setRecommandations(genererRecommandations(rapport));

        // Envoyer une notification
        notificationService.envoyerRapportMensuel(rapport);

        return rapport;
    }

    private String evaluerPerformance(RapportMensuelDto rapport) {
        if (rapport.getMoyenneFeedback() >= 4.5 && rapport.getTauxRetard() < 0.05) {
            return "Excellent";
        } else if (rapport.getMoyenneFeedback() >= 4.0 && rapport.getTauxRetard() < 0.10) {
            return "Très bien";
        } else if (rapport.getMoyenneFeedback() >= 3.5 && rapport.getTauxRetard() < 0.15) {
            return "Bien";
        } else {
            return "Nécessite des améliorations";
        }
    }

    private String genererRecommandations(RapportMensuelDto rapport) {
        StringBuilder recommandations = new StringBuilder();

        if (rapport.getTauxRetard() > 0.10) {
            recommandations.append("- Améliorer la ponctualité\n");
        }
        if (rapport.getMoyenneFeedback() < 4.0) {
            recommandations.append("- Travailler sur la qualité du service client\n");
        }
        if (rapport.getTotalHeuresTravaillees() < 140) { // exemple pour un mois standard
            recommandations.append("- Augmenter la disponibilité\n");
        }

        return recommandations.length() > 0 ? recommandations.toString() : "Continuer sur cette voie";
    }

    public List<RapportMensuelDto> getAllRapportsMensuels(LocalDateTime mois, UUID entrepriseId) {
        // Récupérer tous les chauffeurs actifs de l'entreprise
        List<ChauffeurDto> chauffeurs = chauffeurService.getChauffeursByEntreprise(entrepriseId).stream()
            .filter(c -> c.isIndicActif())
            .collect(Collectors.toList());

        // Générer les rapports pour chaque chauffeur
        return chauffeurs.stream()
            .map(chauffeur -> genererRapportMensuel(chauffeur.getId(), mois))
            .collect(Collectors.toList());
    }
} 