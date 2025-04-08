package fr.ambuconnect.bug.services;

import fr.ambuconnect.authentification.services.EmailService;
import fr.ambuconnect.bug.dto.BugReportDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class BugReportService {
    private static final Logger LOG = LoggerFactory.getLogger(BugReportService.class);

    @Inject
    EmailService emailService;

    public void traiterRapportBug(BugReportDto report) throws RuntimeException {
        LOG.info("Traitement d'un nouveau rapport de bug : {}", report.getTitle());

        try {
            // Construction du corps de l'email
            StringBuilder emailBody = new StringBuilder();
            emailBody.append("Nouveau rapport de bug\n\n");
            emailBody.append("Titre : ").append(report.getTitle()).append("\n");
            emailBody.append("Sévérité : ").append(report.getSeverity()).append("\n\n");
            emailBody.append("Description :\n").append(report.getDescription()).append("\n\n");
            
            emailBody.append("Informations utilisateur :\n");
            emailBody.append("Nom : ").append(report.getUserInfo().getName()).append("\n");
            emailBody.append("Rôle : ").append(report.getUserInfo().getRole()).append("\n");
            emailBody.append("Navigateur : ").append(report.getUserInfo().getBrowser()).append("\n");
            emailBody.append("Date/Heure : ").append(report.getUserInfo().getTimestamp()).append("\n");
            emailBody.append("Email : ").append(report.getEmail());

            // Envoi de l'email à l'équipe technique
            emailService.sendEmail(
                "ambuconnect@ambuconnect-app.com",
                "[BUG] " + report.getTitle(),
                emailBody.toString()
            );

            // Envoi d'un email de confirmation à l'utilisateur
            String confirmationMessage = String.format(
                "Bonjour %s,\n\n" +
                "Nous avons bien reçu votre rapport de bug concernant : %s\n\n" +
                "Notre équipe technique va analyser ce problème dans les plus brefs délais.\n\n" +
                "Cordialement,\n" +
                "L'équipe AmbuConnect",
                report.getUserInfo().getName(),
                report.getTitle()
            );

            emailService.sendEmail(
                report.getEmail(),
                "Confirmation de votre rapport de bug - AmbuConnect",
                confirmationMessage
            );

        } catch (Exception e) {
            LOG.error("Erreur lors du traitement du rapport de bug", e);
            throw new RuntimeException("Erreur lors du traitement du rapport de bug", e);
        }
    }
} 