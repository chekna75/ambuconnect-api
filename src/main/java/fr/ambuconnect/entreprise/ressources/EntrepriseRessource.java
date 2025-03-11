package fr.ambuconnect.entreprise.ressources;

import fr.ambuconnect.entreprise.dto.EntrepriseDto;
import fr.ambuconnect.entreprise.services.EntrepriseService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/entreprises")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EntrepriseRessource {

    @Inject
    EntrepriseService entrepriseService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response creerEntreprise(EntrepriseDto entrepriseDto) {
        EntrepriseDto nouvelleEntreprise = entrepriseService.creerEntreprise(entrepriseDto);
        return Response.ok(nouvelleEntreprise).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response obtenirEntreprise(@PathParam("id") UUID id) {
        EntrepriseDto entreprise = entrepriseService.obtenirEntreprise(id);
        return Response.ok(entreprise).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response obtenirToutesLesEntreprises() {
        List<EntrepriseDto> entreprises = entrepriseService.obtenirToutesLesEntreprises();
        return Response.ok(entreprises).build();
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response mettreAJourEntreprise(@PathParam("id") UUID id, EntrepriseDto entrepriseDto) {
        EntrepriseDto entrepriseMiseAJour = entrepriseService.mettreAJourEntreprise(id, entrepriseDto);
        return Response.ok(entrepriseMiseAJour).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response supprimerEntreprise(@PathParam("id") UUID id) {
        entrepriseService.supprimerEntreprise(id);
        return Response.noContent().build();
    }
}
