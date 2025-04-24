package fr.ambuconnect.etablissement.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.ambuconnect.common.exceptions.NotFoundException;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import fr.ambuconnect.etablissement.entity.DemandeTransport;
import fr.ambuconnect.etablissement.entity.EtablissementSante;
import fr.ambuconnect.etablissement.entity.StatusDemande;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class StatsEtablissementService {

    @Inject
    EntityManager entityManager;

    public Map<String, Object> getStats(UUID etablissementId, LocalDateTime debut, LocalDateTime fin) {
        log.debug("Calcul des statistiques pour l'établissement {} entre {} et {}", etablissementId, debut, fin);

        // Vérifier si l'établissement existe
        EtablissementSante etablissement = entityManager.find(EtablissementSante.class, etablissementId);
        if (etablissement == null) {
            throw new NotFoundException("L'établissement n'existe pas");
        }

        Map<String, Object> stats = new HashMap<>();

        // Nombre total de transports
        Long nombreTotal = DemandeTransport.count(
            "etablissement.id = ?1 AND createdAt BETWEEN ?2 AND ?3",
            etablissementId, debut, fin
        );
        stats.put("nombreTotal", nombreTotal);

        // Nombre de transports par status
        Map<StatusDemande, Long> parStatus = new HashMap<>();
        for (StatusDemande status : StatusDemande.values()) {
            Long nombre = DemandeTransport.count(
                "etablissement.id = ?1 AND status = ?2 AND createdAt BETWEEN ?3 AND ?4",
                etablissementId, status, debut, fin
            );
            parStatus.put(status, nombre);
        }
        stats.put("parStatus", parStatus);

        // Taux d'annulation
        double tauxAnnulation = nombreTotal > 0 
            ? (double) parStatus.get(StatusDemande.ANNULEE) / nombreTotal * 100 
            : 0;
        stats.put("tauxAnnulation", tauxAnnulation);

        // Délai moyen de prise en charge
        TypedQuery<Double> delaiQuery = entityManager.createQuery(
            "SELECT AVG(EXTRACT(EPOCH FROM (d.updatedAt - d.createdAt))) " +
            "FROM DemandeTransport d " +
            "WHERE d.etablissement.id = :etablissementId " +
            "AND d.status = :status " +
            "AND d.createdAt BETWEEN :debut AND :fin",
            Double.class
        );
        delaiQuery.setParameter("etablissementId", etablissementId);
        delaiQuery.setParameter("status", StatusDemande.ACCEPTEE);
        delaiQuery.setParameter("debut", debut);
        delaiQuery.setParameter("fin", fin);
        Double delaiMoyenSecondes = delaiQuery.getSingleResult();
        Duration delaiMoyen = delaiMoyenSecondes != null 
            ? Duration.ofSeconds(delaiMoyenSecondes.longValue()) 
            : Duration.ZERO;
        stats.put("delaiMoyenPriseEnCharge", delaiMoyen.toMinutes());

        // Classement des sociétés
        TypedQuery<Object[]> societesQuery = entityManager.createQuery(
            "SELECT d.societeAffectee.id, d.societeAffectee.nom, COUNT(d) " +
            "FROM DemandeTransport d " +
            "WHERE d.etablissement.id = :etablissementId " +
            "AND d.createdAt BETWEEN :debut AND :fin " +
            "AND d.societeAffectee IS NOT NULL " +
            "GROUP BY d.societeAffectee.id, d.societeAffectee.nom " +
            "ORDER BY COUNT(d) DESC",
            Object[].class
        );
        societesQuery.setParameter("etablissementId", etablissementId);
        societesQuery.setParameter("debut", debut);
        societesQuery.setParameter("fin", fin);
        List<Map<String, Object>> classementSocietes = societesQuery.getResultList().stream()
            .map(result -> {
                Map<String, Object> societe = new HashMap<>();
                societe.put("id", result[0]);
                societe.put("nom", result[1]);
                societe.put("nombreTransports", result[2]);
                return societe;
            })
            .collect(Collectors.toList());
        stats.put("classementSocietes", classementSocietes);

        // Taux de retard
        TypedQuery<Long> retardsQuery = entityManager.createQuery(
            "SELECT COUNT(d) FROM DemandeTransport d " +
            "WHERE d.etablissement.id = :etablissementId " +
            "AND d.createdAt BETWEEN :debut AND :fin " +
            "AND d.status = :status " +
            "AND d.updatedAt > d.horaireSouhaite",
            Long.class
        );
        retardsQuery.setParameter("etablissementId", etablissementId);
        retardsQuery.setParameter("status", StatusDemande.TERMINEE);
        retardsQuery.setParameter("debut", debut);
        retardsQuery.setParameter("fin", fin);
        Long nombreRetards = retardsQuery.getSingleResult();
        Long nombreTerminees = parStatus.get(StatusDemande.TERMINEE);
        double tauxRetard = nombreTerminees > 0 
            ? (double) nombreRetards / nombreTerminees * 100 
            : 0;
        stats.put("tauxRetard", tauxRetard);

        return stats;
    }
} 