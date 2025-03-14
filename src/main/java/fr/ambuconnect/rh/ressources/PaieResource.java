package fr.ambuconnect.rh.ressources;

import java.util.UUID;
import java.util.List;

import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.rh.dto.FichePaieDTO;
import fr.ambuconnect.rh.service.BulletinPaieService;
import fr.ambuconnect.rh.service.CalculPaieService;
import fr.ambuconnect.rh.dto.CreationFichePaieDTO;
import fr.ambuconnect.rh.entity.FichePaieEntity;
import fr.ambuconnect.rh.mapper.FichePaieMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.NotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api/paie")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaieResource {
    
    private static final Logger LOG = LoggerFactory.getLogger(PaieResource.class);
    
    @Inject
    CalculPaieService calculPaieService;
    
    @Inject
    BulletinPaieService bulletinPaieService;
    
    @Inject
    FichePaieMapper fichePaieMapper;
    
    @POST
    @Path("/calculer")
    public Response calculerPaie(@Valid CreationFichePaieDTO dto) {
        ChauffeurEntity chauffeur = ChauffeurEntity.findById(dto.getChauffeurId());
        if (chauffeur == null) {
            throw new NotFoundException("Chauffeur non trouvé");
        }
        
        FichePaieDTO fichePaie = calculPaieService.calculerFichePaie(
            chauffeur,
            dto.getPeriodeDebut(),
            dto.getPeriodeFin(),
            dto.getHeuresTravaillees(),
            dto.getTauxHoraire(),
            dto.isForfaitJour(),
            dto.getForfaitJournalier()
        );
        
        return Response.ok(fichePaie).build();
    }
    
    @GET
    @Path("/{id}/pdf")
    @Produces("application/pdf")
    public Response generatePDF(@PathParam("id") UUID id) {
        try {
            LOG.info("Début de la génération PDF pour la fiche : " + id);
            
            // Récupérer la fiche de paie
            FichePaieEntity fichePaie = FichePaieEntity.<FichePaieEntity>findById(id);
            LOG.info("Recherche fiche de paie : " + (fichePaie != null ? "trouvée" : "non trouvée"));
            
            if (fichePaie == null) {
                LOG.warn("Fiche de paie non trouvée : " + id);
                return Response.status(Response.Status.NOT_FOUND)
                              .entity("Fiche de paie non trouvée")
                              .build();
            }
            
            LOG.info("Conversion en DTO pour la fiche : " + fichePaie.getId());
            FichePaieDTO fichePaieDTO = fichePaieMapper.toDTO(fichePaie);
            LOG.info("DTO créé avec succès : " + fichePaieDTO.getId());
            
            LOG.info("Génération du PDF en cours...");
            byte[] pdf = bulletinPaieService.genererBulletinPDF(fichePaieDTO);
            LOG.info("PDF généré avec succès, taille : " + pdf.length + " bytes");
            
            String fileName = String.format("bulletin-%s-%s.pdf", 
                fichePaie.getChauffeur().getNom(),
                fichePaie.getPeriodeDebut().toString());
            
            LOG.info("Envoi du PDF : " + fileName);
            return Response.ok(pdf)
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .build();
                
        } catch (Exception e) {
            LOG.error("Erreur lors de la génération du PDF", e);
            return Response.serverError()
                          .entity("Erreur lors de la génération du PDF: " + e.getMessage())
                          .build();
        }
    }
    
    @GET
    @Path("/chauffeur/{chauffeurId}")
    public Response getBulletinsByChauffeur(@PathParam("chauffeurId") UUID chauffeurId) {
        try {
            List<FichePaieDTO> bulletins = bulletinPaieService.getBulletinsByChauffeur(chauffeurId);
            return Response.ok(bulletins).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                          .entity(e.getMessage())
                          .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la récupération des bulletins de paie", e);
            return Response.serverError()
                          .entity("Erreur lors de la récupération des bulletins: " + e.getMessage())
                          .build();
        }
    }
    
    @GET
    @Path("/chauffeur/{chauffeurId}/actuel")
    public Response getActualBulletin(@PathParam("chauffeurId") UUID chauffeurId) {
        try {
            FichePaieDTO bulletin = bulletinPaieService.getActualBulletin(chauffeurId);
            return Response.ok(bulletin).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                          .entity(e.getMessage())
                          .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la récupération du bulletin de paie actuel", e);
            return Response.serverError()
                          .entity("Erreur lors de la récupération du bulletin: " + e.getMessage())
                          .build();
        }
    }
    
    @GET
    @Path("/entreprise/{entrepriseId}")
    public Response getBulletinsByEntreprise(
            @PathParam("entrepriseId") UUID entrepriseId,
            @QueryParam("mois") Integer mois,
            @QueryParam("annee") Integer annee) {
        try {
            List<FichePaieDTO> bulletins;
            
            if (mois != null && annee != null) {
                bulletins = bulletinPaieService.getBulletinsByEntrepriseAndPeriode(entrepriseId, mois, annee);
            } else {
                bulletins = bulletinPaieService.getBulletinsByEntreprise(entrepriseId);
            }
            
            return Response.ok(bulletins).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                          .entity(e.getMessage())
                          .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la récupération des bulletins de paie de l'entreprise", e);
            return Response.serverError()
                          .entity("Erreur lors de la récupération des bulletins: " + e.getMessage())
                          .build();
        }
    }
}
