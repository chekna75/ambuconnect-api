package fr.ambuconnect.authentification.services;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class EmailService {

    @Inject
    Mailer mailer;

    @ConfigProperty(name = "quarkus.mailer.from")
    String from;

    @ConfigProperty(name = "app.frontend.url")
    String frontendUrl;

    public void sendPasswordResetEmail(String to, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;
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
} 