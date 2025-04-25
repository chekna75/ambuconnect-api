package fr.ambuconnect.etablissement.service;

import fr.ambuconnect.etablissement.dto.ContactMessageDto;
import fr.ambuconnect.notification.service.EmailServiceEtablissement;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class ContactService {

    @Inject
    EmailServiceEtablissement emailService;


    @ConfigProperty(name = "app.contact.email")
    String contactEmail;

    public void envoyerMessageContact(ContactMessageDto message) {
        log.info("Réception d'un nouveau message de contact de : {}", message.getEmail());
        
        String sujet = "Nouveau message de contact - " + message.getSujet();
        String contenu = String.format("""
            Nouveau message de contact reçu :
            
            De : %s
            Email : %s
            Sujet : %s
            
            Message :
            %s
            """, 
            message.getNom(),
            message.getEmail(),
            message.getSujet(),
            message.getMessage()
        );

        try {
            // Envoyer une copie à l'adresse de contact
            emailService.sendEmail(contactEmail, sujet, contenu);
            
            // Envoyer une confirmation à l'expéditeur
            String confirmationContent = String.format("""
                Bonjour %s,
                
                Nous avons bien reçu votre message. Notre équipe vous répondra dans les plus brefs délais.
                
                Votre message :
                %s
                
                Cordialement,
                L'équipe AmbuConnect
                """,
                message.getNom(),
                message.getMessage()
            );
            
            emailService.sendEmail(message.getEmail(), "Confirmation de réception de votre message", confirmationContent);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du message de contact", e);
            throw new RuntimeException("Erreur lors de l'envoi du message de contact", e);
        }
    }
} 