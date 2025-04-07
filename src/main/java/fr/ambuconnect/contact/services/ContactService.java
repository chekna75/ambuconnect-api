package fr.ambuconnect.contact.services;

import fr.ambuconnect.authentification.services.EmailService;
import fr.ambuconnect.contact.dto.ContactRequestDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class ContactService {
    private static final Logger LOG = LoggerFactory.getLogger(ContactService.class);

    @Inject
    EmailService emailService;

    public void traiterDemandeContact(ContactRequestDto request) throws RuntimeException {
        LOG.info("Traitement d'une nouvelle demande de contact de : {} {}", request.getFirstName(), request.getLastName());

        try {
            // Construction du corps de l'email
            StringBuilder emailBody = new StringBuilder();
            emailBody.append("Nouvelle demande de contact\n\n");
            emailBody.append("Type de demande : ").append(request.getType()).append("\n");
            emailBody.append("Nom : ").append(request.getLastName()).append("\n");
            emailBody.append("Prénom : ").append(request.getFirstName()).append("\n");
            emailBody.append("Email : ").append(request.getEmail()).append("\n");
            emailBody.append("Téléphone : ").append(request.getPhone()).append("\n");
            emailBody.append("Entreprise : ").append(request.getCompany()).append("\n");
            
            if (request.getDate() != null) {
                emailBody.append("Date souhaitée : ").append(request.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
            }
            
            if (request.getTimeSlot() != null && !request.getTimeSlot().isEmpty()) {
                emailBody.append("Créneau horaire : ").append(request.getTimeSlot()).append("\n");
            }
            
            emailBody.append("\nMessage :\n").append(request.getMessage());

            // Envoi de l'email à l'équipe AmbuConnect
            emailService.sendEmail(
                "ambuconnect@ambuconnect-app.com", // Remplacer par l'email de destination souhaité
                "Nouvelle demande de contact - " + request.getType(),
                emailBody.toString()
            );

            // Envoi d'un email de confirmation au client
            String confirmationMessage = String.format(
                "Bonjour %s,\n\n" +
                "Nous avons bien reçu votre demande de contact concernant : %s\n\n" +
                "Notre équipe vous répondra dans les plus brefs délais.\n\n" +
                "Cordialement,\n" +
                "L'équipe AmbuConnect",
                request.getFirstName(),
                request.getType()
            );

            emailService.sendEmail(
                request.getEmail(),
                "Confirmation de votre demande de contact - AmbuConnect",
                confirmationMessage
            );

        } catch (Exception e) {
            LOG.error("Erreur lors du traitement de la demande de contact", e);
            throw new RuntimeException("Erreur lors du traitement de la demande de contact", e);
        }
    }
} 