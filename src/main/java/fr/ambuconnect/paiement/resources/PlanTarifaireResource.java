package fr.ambuconnect.paiement.resources;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import fr.ambuconnect.paiement.dto.PlanTarifaireDto;
import fr.ambuconnect.paiement.entity.PlanTarifaireEntity;
import fr.ambuconnect.paiement.services.PlanTarifaireService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/plans")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PlanTarifaireResource {

    private final PlanTarifaireService planTarifaireService;

    @Inject
    public PlanTarifaireResource(PlanTarifaireService planTarifaireService) {
        this.planTarifaireService = planTarifaireService;
    }

    /**
     * Récupère tous les plans tarifaires
     */
    @GET
    public Response obtenirTousLesPlansTarifaires() {
        List<PlanTarifaireEntity> plans = planTarifaireService.obtenirTousLesPlansTarifaires();
        
        // Convertir les entités en DTOs pour ne pas exposer toutes les informations
        List<PlanTarifaireDto> plansDto = plans.stream()
                .map(plan -> {
                    PlanTarifaireDto dto = new PlanTarifaireDto();
                    dto.setId(plan.getId());
                    dto.setNom(plan.getNom());
                    dto.setCode(plan.getCode());
                    dto.setDescription(plan.getDescription());
                    dto.setMontantMensuel(plan.getMontantMensuel());
                    dto.setDevise(plan.getDevise());
                    dto.setActif(plan.getActif());
                    return dto;
                })
                .collect(Collectors.toList());
        
        return Response.ok(plansDto).build();
    }

    /**
     * Récupère un plan tarifaire par son ID
     */
    @GET
    @Path("/{id}")
    public Response obtenirPlanTarifaireParId(@PathParam("id") UUID id) {
        PlanTarifaireEntity plan = planTarifaireService.obtenirPlanTarifaireParId(id);
        
        PlanTarifaireDto dto = new PlanTarifaireDto();
        dto.setId(plan.getId());
        dto.setNom(plan.getNom());
        dto.setCode(plan.getCode());
        dto.setDescription(plan.getDescription());
        dto.setMontantMensuel(plan.getMontantMensuel());
        dto.setDevise(plan.getDevise());
        dto.setDateCreation(plan.getDateCreation());
        dto.setActif(plan.getActif());
        
        return Response.ok(dto).build();
    }

    /**
     * Récupère un plan tarifaire par son code
     */
    @GET
    @Path("/code/{code}")
    public Response obtenirPlanTarifaireParCode(@PathParam("code") String code) {
        PlanTarifaireEntity plan = planTarifaireService.obtenirPlanTarifaireParCode(code);
        
        PlanTarifaireDto dto = new PlanTarifaireDto();
        dto.setId(plan.getId());
        dto.setNom(plan.getNom());
        dto.setCode(plan.getCode());
        dto.setDescription(plan.getDescription());
        dto.setMontantMensuel(plan.getMontantMensuel());
        dto.setDevise(plan.getDevise());
        dto.setDateCreation(plan.getDateCreation());
        dto.setActif(plan.getActif());
        
        return Response.ok(dto).build();
    }

    /**
     * Crée un nouveau plan tarifaire
     * Réservé aux administrateurs
     */
    @POST
    @RolesAllowed({"ADMIN", "SUPERADMIN"})
    public Response creerPlanTarifaire(@RequestBody @Valid PlanTarifaireDto planDto) {
        PlanTarifaireEntity nouveauPlan = planTarifaireService.creerPlanTarifaire(planDto);
        
        PlanTarifaireDto dto = new PlanTarifaireDto();
        dto.setId(nouveauPlan.getId());
        dto.setNom(nouveauPlan.getNom());
        dto.setCode(nouveauPlan.getCode());
        dto.setDescription(nouveauPlan.getDescription());
        dto.setMontantMensuel(nouveauPlan.getMontantMensuel());
        dto.setDevise(nouveauPlan.getDevise());
        dto.setStripePriceId(nouveauPlan.getStripePriceId());
        dto.setDateCreation(nouveauPlan.getDateCreation());
        dto.setActif(nouveauPlan.getActif());
        
        return Response.status(Response.Status.CREATED).entity(dto).build();
    }

    /**
     * Met à jour un plan tarifaire existant
     * Réservé aux administrateurs
     */
    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "SUPERADMIN"})
    public Response mettreAJourPlanTarifaire(
            @PathParam("id") UUID id,
            @RequestBody @Valid PlanTarifaireDto planDto) {
        PlanTarifaireEntity planMisAJour = planTarifaireService.mettreAJourPlanTarifaire(id, planDto);
        
        PlanTarifaireDto dto = new PlanTarifaireDto();
        dto.setId(planMisAJour.getId());
        dto.setNom(planMisAJour.getNom());
        dto.setCode(planMisAJour.getCode());
        dto.setDescription(planMisAJour.getDescription());
        dto.setMontantMensuel(planMisAJour.getMontantMensuel());
        dto.setDevise(planMisAJour.getDevise());
        dto.setStripePriceId(planMisAJour.getStripePriceId());
        dto.setDateCreation(planMisAJour.getDateCreation());
        dto.setActif(planMisAJour.getActif());
        
        return Response.ok(dto).build();
    }

    /**
     * Retourne l'ID de prix Stripe associé à un code de plan
     * Utilisé pour créer des abonnements
     */
    @GET
    @Path("/stripe-price-id/{code}")
    public Response obtenirIdPlanStripeParCode(@PathParam("code") String code) {
        String stripePriceId = planTarifaireService.obtenirIdPlanStripeParCode(code);
        return Response.ok(stripePriceId).build();
    }
} 