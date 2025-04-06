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

    @ConfigProperty(name = "app.frontendchauffeur.url")
    String frontendChauffeurUrl;

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
} 
