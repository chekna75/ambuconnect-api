package fr.ambuconnect.chauffeur.ressources;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.jboss.resteasy.reactive.RestResponse;

import fr.ambuconnect.administrateur.dto.AdministrateurDto;
import fr.ambuconnect.administrateur.services.AdministrateurService;
import fr.ambuconnect.authentification.services.AuthenService;
import fr.ambuconnect.chauffeur.dto.ChauffeurDto;
import fr.ambuconnect.chauffeur.dto.PerformanceChauffeurDto;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.chauffeur.services.ChauffeurService;
import fr.ambuconnect.chauffeur.services.PerformanceChauffeurService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/chauffeurs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@Valid
public class ChauffeurRessource {

    @Inject
    PerformanceChauffeurService performanceService;
    
    @Inject
    ChauffeurService chauffeurService;
    
    @Inject
    AdministrateurService administrateurService;

    @POST
    @Path("/{id}/performances")
    public Response enregistrerPerformance(
            @PathParam("id") UUID chauffeurId, 
            @RequestBody PerformanceChauffeurDto performance) {
        performance.setChauffeurId(chauffeurId);
        return Response.ok(performanceService.enregistrerPerformance(performance)).build();
    }

    @GET
    @Path("/{id}/performances")
    public Response getPerformances(
            @PathParam("id") UUID chauffeurId,
            @QueryParam("debut") LocalDateTime debut,
            @QueryParam("fin") LocalDateTime fin) {
        return Response.ok(performanceService.getPerformancesChauffeur(chauffeurId, debut, fin)).build();
    }

    @GET
    @Path("/{id}/performances/rapport-mensuel")
    public Response getRapportMensuel(
            @PathParam("id") UUID chauffeurId,
            @QueryParam("mois") LocalDateTime mois) {
        return Response.ok(performanceService.genererRapportMensuel(chauffeurId, mois)).build();
    }
    
    /**
     * Endpoint permettant aux chauffeurs de récupérer la liste des administrateurs de leur entreprise
     * pour pouvoir choisir à qui envoyer un message via WebSocket
     */
    @GET
    @Path("/admins")
    @PermitAll
    public Response getAdministrateurs(@QueryParam("chauffeurId") UUID chauffeurId) {
        try {
            // Récupérer le chauffeur pour connaître son entreprise
            ChauffeurDto chauffeur = chauffeurService.findById(chauffeurId);
            if (chauffeur == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Chauffeur non trouvé")
                    .build();
            }
            
            // Récupérer l'ID de l'entreprise du chauffeur
            UUID entrepriseId = chauffeur.getEntrepriseId();
            
            // Récupérer tous les administrateurs de cette entreprise
            List<AdministrateurDto> administrateurs = administrateurService.findByEntreprise(entrepriseId);
            
            // Supprimer les informations sensibles comme les mots de passe avant de renvoyer
            administrateurs.forEach(admin -> admin.setMotDePasse(null));
            
            return Response.ok(administrateurs).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Erreur lors de la récupération des administrateurs: " + e.getMessage())
                .build();
        }
    }
}
