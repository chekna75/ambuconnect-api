package fr.ambuconnect.administrateur.ressoucres;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.jboss.resteasy.reactive.RestResponse;

import fr.ambuconnect.administrateur.dto.AdministrateurDto;
import fr.ambuconnect.administrateur.services.AdministrateurService;
import fr.ambuconnect.authentification.services.AuthenService;
import fr.ambuconnect.chauffeur.dto.ChauffeurDto;
import io.quarkus.security.Authenticated;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/administrateur")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Valid
public class AdministrateurResourse {

    private final AdministrateurService administrateurService;
    private final AuthenService authenService;
    private final SecurityIdentity securityIdentity;

    @Inject
    public AdministrateurResourse(AuthenService authenService, SecurityIdentity securityIdentity, AdministrateurService administrateurService) {
        this.authenService = authenService;
        this.securityIdentity = securityIdentity;
        this.administrateurService = administrateurService;
    }

    @GET
    @Authenticated
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello, " + securityIdentity.getPrincipal().getName();
    }

    @GET
    @Path("/chauffeur/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getChauffeurById(@PathParam("id") UUID id){
        ChauffeurDto chauffeurDto = administrateurService.findById(id);
        return Response.ok(chauffeurDto).build();
    }

    @GET
    @Path("/{id}/allchauffeur")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getAllChauffeur(@PathParam("id") UUID id){
        List<ChauffeurDto> chauffeurDtos = administrateurService.findAll(id);
        return Response.ok(chauffeurDtos).build();
    }

    @POST
    @Path("/createchauffeur")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public RestResponse<ChauffeurDto> craeteChauffeur(@RequestBody @Valid ChauffeurDto chauffeurDto) throws Exception{
        //this.checkAutorization();
        try {
            ChauffeurDto dto = administrateurService.createChauffeur(chauffeurDto);
            return RestResponse.ok(dto);
        } catch (Exception e) {
            throw new ForbiddenException("Erreur lors de la cration", e);
        }
    }

    @POST
    @Path("/createAdmin")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public RestResponse<AdministrateurDto> createAdmin(@RequestBody @Valid AdministrateurDto administrateurDto) throws Exception {
        AdministrateurDto dto = administrateurService.creationAdmin(administrateurDto);
        return RestResponse.ok(dto);
    }

    @GET
    @Path("/{id}/recherche")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response rechercherChauffeurs(
            @PathParam("id") UUID administrateurId,
            @QueryParam("search") String searchTerm) {
        try {
            List<ChauffeurDto> chauffeurDtos = administrateurService.rechercherChauffeurs(administrateurId, searchTerm);
            return Response.ok(chauffeurDtos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Erreur lors de la recherche : " + e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/{id}/updatechauffeur")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response updateChauffeur(
            @PathParam("id") UUID id,
            ChauffeurDto chauffeurDto) {
        try {
            ChauffeurDto updatedChauffeur = administrateurService.update(id, chauffeurDto);
            return Response.ok(updatedChauffeur).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Erreur lors de la mise à jour : " + e.getMessage())
                    .build();
        }
    }

    private void checkAutorization(){
        AdministrateurDto dto = authenService.validateAdministrateur(securityIdentity);
        if(!"ADMINISTRATEUR".equals(dto.getRole())){
            throw new ForbiddenException("Votre Compte n'est pas habilité");
        }
    }

    @GET
@Path("/entreprise/{identreprise}")
@Produces(MediaType.APPLICATION_JSON)
public List<AdministrateurDto> getAdminsByEntreprise(@PathParam("identreprise") UUID identreprise) {
        return administrateurService.findByEntreprise(identreprise);
    }

}
