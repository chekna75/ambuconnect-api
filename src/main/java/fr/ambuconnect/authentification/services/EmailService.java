package fr.ambuconnect.authentification.services;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

import fr.ambuconnect.administrateur.dto.InscriptionEntrepriseDto;

@ApplicationScoped
public class EmailService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);

    @Inject
    Mailer mailer;

    @ConfigProperty(name = "quarkus.mailer.from")
    String from;

    @ConfigProperty(name = "app.frontend.url")
    String frontendUrl;

    @ConfigProperty(name = "app.frontendchauffeur.url")
    String frontendChauffeurUrl;

    @ConfigProperty(name = "quarkus.mailer.email-from")
    String emailFrom;

    public void sendPasswordResetEmail(String to, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token + "&email=" + to;
        String subject = "Réinitialisation de votre mot de passe AmbuConnect";
        String body = String.format("""
            Bonjour,
            
            Vous avez demandé la réinitialisation de votre mot de passe AmbuConnect.
            
            Pour réinitialiser votre mot de passe, veuillez cliquer sur le lien suivant :
            %s
            
            Ce lien est valable pendant 1 heure.
            
            Si vous n'avez pas demandé cette réinitialisation, vous pouvez ignorer cet email.
            
            Cordialement,
            L'équipe AmbuConnect
            """, resetLink);

        mailer.send(Mail.withText(to, subject, body));
    }

    public void sendPasswordResetEmailChauffeur(String to, String token) {
        String resetLink = frontendChauffeurUrl + "/reset-password?token=" + token + "&email=" + to;
        String subject = "Réinitialisation de votre mot de passe AmbuConnect";
        String body = String.format("""
            Bonjour,
            
            Vous avez demandé la réinitialisation de votre mot de passe AmbuConnect.
            
            Pour réinitialiser votre mot de passe, veuillez cliquer sur le lien suivant :
            %s
            
            Ce lien est valable pendant 1 heure.
            
            Si vous n'avez pas demandé cette réinitialisation, vous pouvez ignorer cet email.
            
            Cordialement,
            L'équipe AmbuConnect
            """, resetLink);

        mailer.send(Mail.withText(to, subject, body));
    }

    public void sendNewAccountCredentials(String to, String nom, String prenom, String role, String motDePasse) {
        String loginLink = frontendChauffeurUrl + "/login";
        String subject = "Bienvenue sur AmbuConnect - Vos identifiants de connexion";
        String body = String.format("""
            Bonjour %s %s,
            
            Votre compte AmbuConnect a été créé avec succès en tant que %s.
            
            Voici vos identifiants de connexion :
            Email : %s
            Mot de passe : %s
            
            Pour vous connecter, rendez-vous sur :
            %s
            
            Pour des raisons de sécurité, nous vous recommandons de changer votre mot de passe lors de votre première connexion.
            En cliquant sur mot de passe, vous pourrez modifier votre mot de passe.

            Si vous rencontrez des difficultés, n'hésitez pas à nous contacter via le support.
            Cordialement,
            L'équipe AmbuConnect
            """, prenom, nom, role, to, motDePasse, loginLink);

        mailer.send(Mail.withText(to, subject, body));
    }

    public void sendNewAccountCredentialsAdmin(String to, String nom, String prenom, String role, String motDePasse) {
        String loginLink = frontendUrl + "/login";
        String subject = "Bienvenue sur AmbuConnect - Vos identifiants de connexion";
        String body = String.format("""
            Bonjour %s %s,
            
            Votre compte AmbuConnect a été créé avec succès en tant que %s.
            
            Voici vos identifiants de connexion :
            Email : %s
            Mot de passe : %s
            
            Pour vous connecter, rendez-vous sur :
            %s
            
            Pour des raisons de sécurité, nous vous recommandons de changer votre mot de passe lors de votre première connexion.
            En cliquant sur mot de passe oublié, vous pourrez modifier votre mot de passe.

            Si vous rencontrez des difficultés, n'hésitez pas à nous contacter via le support.
            Cordialement,
            L'équipe AmbuConnect
            """, prenom, nom, role, to, motDePasse, loginLink);

        mailer.send(Mail.withText(to, subject, body));
    }

    /**
     * Envoie un email générique
     * 
     * @param destinataire L'adresse email du destinataire
     * @param sujet Le sujet de l'email
     * @param contenu Le contenu de l'email
     * @throws Exception En cas d'erreur lors de l'envoi
     */
    public void sendEmail(String destinataire, String sujet, String contenu) throws Exception {
        LOG.info("Envoi d'un email à {} avec le sujet: {}", destinataire, sujet);
        
        Mail email = new Mail();
        email.setFrom(emailFrom);
        email.setTo(List.of(destinataire));
        email.setSubject(sujet);
        email.setText(contenu);
        
        mailer.send(email);
        
        LOG.info("Email envoyé avec succès à {}", destinataire);
    }

    /**
     * Envoie un email avec les détails d'inscription d'une entreprise
     */
    public void sendNewCompanyRegistrationDetails(InscriptionEntrepriseDto inscriptionDto, String adminEmail) {
        try {
            LOG.info("Envoi des détails d'inscription pour l'entreprise: {}", inscriptionDto.getEntreprise().getNom());
            
            String subject = "Nouvelle inscription entreprise - " + inscriptionDto.getEntreprise().getNom();
            
            StringBuilder body = new StringBuilder();
            body.append("Détails de la nouvelle inscription:\n\n");
            
            // Informations de l'entreprise
            body.append("ENTREPRISE:\n");
            body.append("Nom: ").append(inscriptionDto.getEntreprise().getNom()).append("\n");
            body.append("SIRET: ").append(inscriptionDto.getEntreprise().getSiret()).append("\n");
            body.append("Email: ").append(inscriptionDto.getEntreprise().getEmail()).append("\n");
            body.append("Téléphone: ").append(inscriptionDto.getEntreprise().getTelephone()).append("\n");
            body.append("Adresse: ").append(inscriptionDto.getEntreprise().getAdresse()).append("\n");
            body.append("Code Postal: ").append(inscriptionDto.getEntreprise().getCodePostal()).append("\n\n");
            
            // Informations de l'administrateur
            body.append("ADMINISTRATEUR:\n");
            if (inscriptionDto.getAdministrateur() != null) {
                body.append("Nom: ").append(inscriptionDto.getAdministrateur().getNom()).append("\n");
                body.append("Prénom: ").append(inscriptionDto.getAdministrateur().getPrenom()).append("\n");
                body.append("Email: ").append(inscriptionDto.getAdministrateur().getEmail()).append("\n");
                body.append("Téléphone: ").append(inscriptionDto.getAdministrateur().getTelephone()).append("\n");
            } else {
                body.append("Nom: ").append(inscriptionDto.getNom()).append("\n");
                body.append("Prénom: ").append(inscriptionDto.getPrenom()).append("\n");
                body.append("Email: ").append(inscriptionDto.getEmail()).append("\n");
                body.append("Téléphone: ").append(inscriptionDto.getTelephone()).append("\n");
            }
            body.append("\n");
            
            // Informations de l'abonnement
            body.append("ABONNEMENT:\n");
            body.append("Type: ").append(inscriptionDto.getCodeAbonnement() != null ? 
                inscriptionDto.getCodeAbonnement() : "START").append("\n");
            if (inscriptionDto.getStripeSubscriptionId() != null) {
                body.append("ID Stripe: ").append(inscriptionDto.getStripeSubscriptionId()).append("\n");
            }
            
            // Envoi de l'email
            mailer.send(Mail.withText(
                adminEmail,
                subject,
                body.toString()
            ));
            
            LOG.info("Email avec les détails d'inscription envoyé avec succès à {}", adminEmail);
            
        } catch (Exception e) {
            LOG.error("Erreur lors de l'envoi de l'email des détails d'inscription", e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email des détails d'inscription", e);
        }
    }
} 
