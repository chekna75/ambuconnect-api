package fr.ambuconnect.chauffeur.jobs;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import fr.ambuconnect.chauffeur.services.ChauffeurService;
import fr.ambuconnect.chauffeur.services.PerformanceChauffeurService;
import java.time.LocalDateTime;

@ApplicationScoped
public class RapportMensuelJob {

    @Inject
    ChauffeurService chauffeurService;

    @Inject
    PerformanceChauffeurService performanceService;

    @Scheduled(cron = "0 0 1 1 * ?") // Exécution le 1er de chaque mois à 1h du matin
    void genererRapportsMensuels() {
        LocalDateTime moisPrecedent = LocalDateTime.now().minusMonths(1);
        
        chauffeurService.findAll().forEach(chauffeur -> {
            try {
                performanceService.genererRapportMensuel(chauffeur.getId(), moisPrecedent);
            } catch (Exception e) {
                // Gérer les erreurs et les logger
                e.printStackTrace();
            }
        });
    }
} 