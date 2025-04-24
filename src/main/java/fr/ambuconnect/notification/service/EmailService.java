package fr.ambuconnect.notification.service;



import lombok.extern.slf4j.Slf4j;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Slf4j
@ApplicationScoped
public class EmailService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);

    @Inject Mailer mailer;

    @ConfigProperty(name = "quarkus.mailer.from")
    String from;

    @ConfigProperty(name = "app.frontendhps.url")
    String frontendUrl;


    @ConfigProperty(name = "quarkus.mailer.email-from")
    String emailFrom;

    public void sendNewEtablissementConfirmation(String email, String nomEtablissement, String nomResponsable, String prenomResponsable) {
        log.info("Envoi email de confirmation de création d'établissement à {}", email);
        String resetLink = frontendUrl + "/confirmation-etablissement?email=" + email;
        String subject = "Confirmation de création d'établissement AmbuConnect";
        String body = String.format("""
            Bonjour,
            
            Vous avez créé un établissement AmbuConnect.    
            
            Pour confirmer votre inscription, veuillez cliquer sur le lien suivant :
            %s
            
            Cordialement,
            L'équipe AmbuConnect
            """, resetLink);
        mailer.send(Mail.withText(email, subject, body));
        }

    public void sendNewUserCredentials(String email, String nom, String prenom, String role, String motDePasse, String nomEtablissement) {
        log.info("Envoi email des identifiants à {}", email);
        String resetLink = frontendUrl + "/login";
        String subject = "Identifiants AmbuConnect";
        String body = String.format("""
            Bonjour %s %s,
            
            Vous avez créé un compte AmbuConnect.
            
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
            """, prenom, nom, email, motDePasse, resetLink);
        mailer.send(Mail.withText(email, subject, body));
        
    }

    public void sendEtablissementActivationConfirmation(String email, String nomEtablissement) {
        log.info("Envoi email de confirmation d'activation à {}", email);
        // TODO: Implémenter l'envoi d'email
    }

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
} 