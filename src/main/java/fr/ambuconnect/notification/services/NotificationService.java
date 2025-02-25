package fr.ambuconnect.notification.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import fr.ambuconnect.chauffeur.dto.RapportMensuelDto;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class NotificationService {

    @Inject
    Mailer mailer;

    public void envoyerRapportMensuel(RapportMensuelDto rapport) {
        String sujet = String.format("Rapport mensuel de performance - %s - %s",
            rapport.getNomChauffeur(),
            rapport.getMois().format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        String corps = genererCorpsEmail(rapport);

        mailer.send(Mail.withText(
            rapport.getEmailSociete(),
            sujet,
            corps
        ));
    }

    private String genererCorpsEmail(RapportMensuelDto rapport) {
        return String.format("""
            Rapport mensuel de performance
            
            Chauffeur: %s
            Période: %s
            
            Statistiques:
            - Heures travaillées: %.2f
            - Nombre total de courses: %d
            - Taux de retard: %.2f%%
            - Note moyenne des clients: %.2f/5
            
            Évaluation globale: %s
            
            Recommandations:
            %s
            
            Ce rapport a été généré automatiquement.
            """,
            rapport.getNomChauffeur(),
            rapport.getMois().format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            rapport.getTotalHeuresTravaillees(),
            rapport.getTotalCourses(),
            rapport.getTauxRetard() * 100,
            rapport.getMoyenneFeedback(),
            rapport.getEvaluation(),
            rapport.getRecommandations()
        );
    }
} 