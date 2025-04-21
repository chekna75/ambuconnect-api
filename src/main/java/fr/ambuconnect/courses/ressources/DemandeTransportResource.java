package fr.ambuconnect.courses.ressources;

import fr.ambuconnect.ambulances.entity.AmbulanceEntity;
import fr.ambuconnect.courses.entity.CoursesEntity;
import fr.ambuconnect.courses.entity.DemandePriseEnChargeEntity;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import fr.ambuconnect.finance.entity.DevisEntity;
import fr.ambuconnect.finance.service.TarificationService;
import fr.ambuconnect.geolocalisation.service.RechercheAmbulanceService;
import fr.ambuconnect.notification.service.NotificationService;
import fr.ambuconnect.finance.service.GenerationPDFService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.Map;

@Path("/api/transport")
@Tag(name = "Transport", description = "API de gestion des demandes de transport")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DemandeTransportResource {

    @Inject
    RechercheAmbulanceService rechercheAmbulanceService;

    @Inject
    TarificationService tarificationService;

    @Inject
    NotificationService notificationService;

    @Inject
    GenerationPDFService generationPDFService;

    @GET
    @Path("/ambulances/nearby")
    @Operation(summary = "Rechercher les ambulances disponibles à proximité")
    public Response rechercherAmbulances(
            @QueryParam("latitude") double latitude,
            @QueryParam("longitude") double longitude,
            @QueryParam("equipements") List<String> equipements
    ) {
        List<AmbulanceEntity> ambulances;
        if (equipements != null && !equipements.isEmpty()) {
            ambulances = rechercheAmbulanceService.rechercherAmbulancesDisponiblesAvecEquipements(
                latitude, longitude, equipements
            );
        } else {
            ambulances = rechercheAmbulanceService.rechercherAmbulancesDisponibles(
                latitude, longitude
            );
        }
        return Response.ok(ambulances).build();
    }

    @POST
    @Path("/requests")
    @Transactional
    @Operation(summary = "Créer une nouvelle demande de transport")
    public Response creerDemande(DemandePriseEnChargeEntity demande) {
        // Calcul de la distance
        BigDecimal distance = rechercheAmbulanceService.calculerDistance(
            demande.getLatitudeDepart(),
            demande.getLongitudeDepart(),
            demande.getLatitudeArrivee(),
            demande.getLongitudeArrivee()
        );

        // Calcul du tarif
        BigDecimal tarif = tarificationService.calculerTarif(demande, distance);

        // Création du devis
        DevisEntity devis = new DevisEntity();
        devis.setDemande(demande);
        devis.setPatient(demande.getPatient());
        devis.setMontantBase(tarif);
        devis.setDistanceEstimee(distance);
        devis.persist();

        // Sauvegarde de la demande
        demande.setStatut(DemandePriseEnChargeEntity.StatutDemande.EN_ATTENTE);
        demande.persist();

        // Notification des entreprises à proximité
        notificationService.notifierCreationDemande(demande);

        return Response.status(Response.Status.CREATED)
                .entity(demande)
                .build();
    }

    @PUT
    @Path("/requests/{id}/accept")
    @Transactional
    @Operation(summary = "Accepter une demande de transport")
    public Response accepterDemande(
            @PathParam("id") UUID demandeId,
            @QueryParam("entrepriseId") UUID entrepriseId
    ) {
        DemandePriseEnChargeEntity demande = DemandePriseEnChargeEntity.findById(demandeId);
        if (demande == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        demande.setStatut(DemandePriseEnChargeEntity.StatutDemande.VALIDEE);
        demande.setEntrepriseAssignee(EntrepriseEntity.findById(entrepriseId));
        demande.persist();

        // Notification du patient
        notificationService.notifierAcceptationDemande(demande);

        return Response.ok(demande).build();
    }

    @GET
    @Path("/requests/{id}/pdf")
    @Operation(summary = "Générer le PDF de télétransmission")
    @Produces("application/pdf")
    public Response genererPDF(@PathParam("id") UUID demandeId) {
        DemandePriseEnChargeEntity demande = DemandePriseEnChargeEntity.findById(demandeId);
        if (demande == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        CoursesEntity course = CoursesEntity.find("demande.id", demandeId).firstResult();
        DevisEntity devis = DevisEntity.find("demande.id", demandeId).firstResult();

        byte[] pdf = generationPDFService.genererPDFTeletransmission(
            course,
            devis,
            demande.getInformationsMedicales()
        );

        return Response.ok(pdf)
                .header("Content-Disposition", "attachment; filename=transport_" + demandeId + ".pdf")
                .build();
    }

    @GET
    @Path("/requests/{id}/status")
    @Operation(summary = "Obtenir le statut d'une demande")
    public Response getStatus(@PathParam("id") UUID demandeId) {
        DemandePriseEnChargeEntity demande = DemandePriseEnChargeEntity.findById(demandeId);
        if (demande == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        CoursesEntity course = CoursesEntity.find("demande.id", demandeId).firstResult();
        if (course != null) {
            // Calcul du temps estimé restant
            int tempsRestant = rechercheAmbulanceService.estimerTempsTrajet(
                course.getDistance().doubleValue(),
                course.getDateHeureDepart()
            );

            return Response.ok(Map.of(
                "statut", demande.getStatut(),
                "tempsRestantMinutes", tempsRestant,
                "ambulance", course.getAmbulance(),
                "chauffeur", course.getChauffeur()
            )).build();
        }

        return Response.ok(Map.of("statut", demande.getStatut())).build();
    }
} 