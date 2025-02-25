package fr.ambuconnect.rh.ressources;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.YearMonth;
import java.util.UUID;

import fr.ambuconnect.rh.service.BulletinPaieService;
import fr.ambuconnect.rh.service.HistoriqueBulletinsService;
import fr.ambuconnect.rh.service.BulletinExportService;
import fr.ambuconnect.rh.dto.FichePaieDTO;

@Path("/bulletins")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BulletinPaieRessource {

    @Inject
    BulletinPaieService bulletinPaieService;
    
    @Inject
    HistoriqueBulletinsService historiqueService;
    
    @Inject
    BulletinExportService exportService;

    @GET
    @Path("/chauffeur/{chauffeurId}/historique")
    public Response getHistoriqueBulletins(
            @PathParam("chauffeurId") UUID chauffeurId,
            @QueryParam("debut") String debut,
            @QueryParam("fin") String fin) {
        YearMonth yearMonthDebut = YearMonth.parse(debut);
        YearMonth yearMonthFin = YearMonth.parse(fin);
        return Response.ok(historiqueService.getBulletinsChauffeur(chauffeurId, yearMonthDebut, yearMonthFin)).build();
    }

    @GET
    @Path("/chauffeur/{chauffeurId}/annee/{annee}")
    public Response getBulletinsAnnee(
            @PathParam("chauffeurId") UUID chauffeurId,
            @PathParam("annee") int annee) {
        return Response.ok(historiqueService.getBulletinsAnnee(chauffeurId, annee)).build();
    }

    @GET
    @Path("/chauffeur/{chauffeurId}/dernier")
    public Response getDernierBulletin(@PathParam("chauffeurId") UUID chauffeurId) {
        return Response.ok(historiqueService.getDernierBulletin(chauffeurId)).build();
    }

    @GET
    @Path("/{bulletinId}/pdf")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response telechargerBulletin(@PathParam("bulletinId") UUID bulletinId) {
        FichePaieDTO fichePaie = bulletinPaieService.findById(bulletinId);
        byte[] pdf = bulletinPaieService.genererBulletinPDF(fichePaie);
        String nomFichier = exportService.genererNomFichier(fichePaie);
        
        return Response.ok(pdf)
            .header("Content-Disposition", "attachment; filename=\"" + nomFichier + "\"")
            .build();
    }

    @POST
    @Path("/{bulletinId}/email")
    public Response envoyerParEmail(
            @PathParam("bulletinId") UUID bulletinId,
            @QueryParam("email") String email) {
        exportService.envoyerBulletinParEmail(bulletinId, email);
        return Response.ok().build();
    }

    @POST
    @Path("/envoi-masse")
    public Response envoyerBulletinsMasse(
            @QueryParam("mois") int mois,
            @QueryParam("annee") int annee) {
        exportService.envoyerBulletinsMasse(mois, annee);
        return Response.ok().build();
    }

    @POST
    @Path("/{bulletinId}/archiver")
    public Response archiverBulletin(@PathParam("bulletinId") UUID bulletinId) {
        historiqueService.archiver(bulletinId);
        return Response.ok().build();
    }
} 