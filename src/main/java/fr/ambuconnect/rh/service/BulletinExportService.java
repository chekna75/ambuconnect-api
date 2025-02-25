package fr.ambuconnect.rh.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.rh.dto.FichePaieDTO;
import org.jboss.logging.Logger;

@ApplicationScoped
public class BulletinExportService {
    
    private static final Logger LOG = Logger.getLogger(BulletinExportService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy");
    
    @Inject
    BulletinPaieService bulletinPaieService;
    
    @ConfigProperty(name = "mail.smtp.host", defaultValue = "localhost")
    String smtpHost;
    
    @ConfigProperty(name = "mail.smtp.port", defaultValue = "25")
    String smtpPort;
    
    @ConfigProperty(name = "mail.from", defaultValue = "noreply@ambuconnect.fr")
    String mailFrom;
    
    public void envoyerBulletinParEmail(UUID bulletinId, String emailDestinataire) {
        try {
            FichePaieDTO fichePaie = bulletinPaieService.findById(bulletinId);
            ChauffeurEntity chauffeur = ChauffeurEntity.findById(fichePaie.getChauffeurId());
            
            // Génération du PDF
            byte[] pdfContent = bulletinPaieService.genererBulletinPDF(fichePaie);
            
            // Configuration email
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort);
            
            Session session = Session.getInstance(props, null);
            Message message = new MimeMessage(session);
            
            // En-têtes du mail
            message.setFrom(new InternetAddress(mailFrom));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(emailDestinataire));
            message.setSubject("Bulletin de paie - " + 
                fichePaie.getPeriodeDebut().format(DATE_FORMAT));
            
            // Corps du mail
            String corps = String.format(
                "Bonjour %s %s,\n\n" +
                "Veuillez trouver ci-joint votre bulletin de paie pour la période %s.\n\n" +
                "Cordialement,\n" +
                "Le service RH",
                chauffeur.getPrenom(),
                chauffeur.getNom(),
                fichePaie.getPeriodeDebut().format(DATE_FORMAT)
            );
            
            // Pièce jointe
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(corps);
            
            MimeBodyPart pdfPart = new MimeBodyPart();
            pdfPart.setContent(pdfContent, "application/pdf");
            pdfPart.setFileName("bulletin_" + 
                fichePaie.getPeriodeDebut().format(DateTimeFormatter.ofPattern("yyyy_MM")) + 
                ".pdf");
            
            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(pdfPart);
            
            message.setContent(multipart);
            
            // Envoi
            Transport.send(message);
            
            LOG.info("Bulletin envoyé par email à " + emailDestinataire);
            
        } catch (MessagingException e) {
            LOG.error("Erreur lors de l'envoi du bulletin par email", e);
            throw new RuntimeException("Erreur lors de l'envoi du bulletin par email", e);
        }
    }
    
    public void envoyerBulletinsMasse(int mois, int annee) {
        // Récupérer tous les bulletins du mois
        List<FichePaieDTO> bulletins = bulletinPaieService.getBulletinsPeriode(mois, annee);
        
        for (FichePaieDTO bulletin : bulletins) {
            ChauffeurEntity chauffeur = ChauffeurEntity.findById(bulletin.getChauffeurId());
            if (chauffeur.getEmail() != null && !chauffeur.getEmail().isEmpty()) {
                try {
                    envoyerBulletinParEmail(bulletin.getId(), chauffeur.getEmail());
                } catch (Exception e) {
                    LOG.error("Erreur lors de l'envoi du bulletin à " + chauffeur.getEmail(), e);
                }
            }
        }
    }
    
    public String genererNomFichier(FichePaieDTO fichePaie) {
        ChauffeurEntity chauffeur = ChauffeurEntity.findById(fichePaie.getChauffeurId());
        return String.format("bulletin_%s_%s_%s.pdf",
            chauffeur.getNom().toLowerCase(),
            fichePaie.getPeriodeDebut().format(DateTimeFormatter.ofPattern("yyyy_MM")),
            fichePaie.getId().toString().substring(0, 8)
        );
    }
} 