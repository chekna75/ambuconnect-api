package fr.ambuconnect.finance.service;

import fr.ambuconnect.courses.entity.CoursesEntity;
import fr.ambuconnect.finance.entity.DevisEntity;
import fr.ambuconnect.patient.entity.InformationsMedicalesEntity;
import fr.ambuconnect.security.service.ChiffrementService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class GenerationPDFService {

    @Inject
    ChiffrementService chiffrementService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] genererPDFTeletransmission(CoursesEntity course, DevisEntity devis, InformationsMedicalesEntity infosMedicales) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // En-tête
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("FEUILLE DE TRANSPORT SANITAIRE");
                contentStream.endText();

                // Informations Patient
                ajouterSection(contentStream, "INFORMATIONS PATIENT", 700);
                String numSecu = chiffrementService.dechiffrer(infosMedicales.getNumeroSecuriteSocialeCrypte());
                ajouterLigne(contentStream, "Nom: " + course.getPatient().getNom(), 680);
                ajouterLigne(contentStream, "Prénom: " + course.getPatient().getPrenom(), 660);
                ajouterLigne(contentStream, "N° Sécurité Sociale: " + numSecu, 640);
                ajouterLigne(contentStream, "Adresse: " + course.getPatient().getAdresse(), 620);

                // Informations Transport
                ajouterSection(contentStream, "INFORMATIONS TRANSPORT", 580);
                ajouterLigne(contentStream, "Date: " + course.getDateHeureDepart().format(DATE_FORMATTER), 560);
                ajouterLigne(contentStream, "Type: " + devis.getTypeTransport(), 540);
                ajouterLigne(contentStream, "Distance: " + devis.getDistanceEstimee() + " km", 520);
                ajouterLigne(contentStream, "Départ: " + course.getAdresseDepart(), 500);
                ajouterLigne(contentStream, "Arrivée: " + course.getAdresseArrivee(), 480);

                // Informations Prescripteur
                ajouterSection(contentStream, "PRESCRIPTEUR", 440);
                ajouterLigne(contentStream, "Médecin: " + infosMedicales.getMedecinPrescripteurNom(), 420);
                ajouterLigne(contentStream, "N° RPPS: " + infosMedicales.getMedecinPrescripteurRPPS(), 400);
                ajouterLigne(contentStream, "Établissement: " + infosMedicales.getEtablissementPrescripteur(), 380);

                // Informations Entreprise
                ajouterSection(contentStream, "ENTREPRISE AMBULANCE", 340);
                ajouterLigne(contentStream, "Nom: " + course.getEntreprise().getNom(), 320);
                ajouterLigne(contentStream, "SIRET: " + course.getEntreprise().getSiret(), 300);

                // Informations Véhicule
                ajouterSection(contentStream, "VÉHICULE", 240);
                ajouterLigne(contentStream, "Immatriculation: " + course.getAmbulance().getImmatriculation(), 200);
                ajouterLigne(contentStream, "Chauffeur: " + course.getChauffeur().getNom(), 180);

                // Signatures
                ajouterSection(contentStream, "SIGNATURES", 140);
                ajouterLigne(contentStream, "Patient ou représentant:", 120);
                ajouterLigne(contentStream, "Ambulancier:", 100);
                ajouterLigne(contentStream, "Date: " + LocalDateTime.now().format(DATE_FORMATTER), 80);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    private void ajouterSection(PDPageContentStream contentStream, String titre, float y) throws IOException {
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(50, y);
        contentStream.showText(titre);
        contentStream.endText();
    }

    private void ajouterLigne(PDPageContentStream contentStream, String texte, float y) throws IOException {
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.newLineAtOffset(50, y);
        contentStream.showText(texte);
        contentStream.endText();
    }
} 