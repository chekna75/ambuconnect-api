package fr.ambuconnect.administrateur.ressoucres;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.jboss.resteasy.reactive.RestResponse;

import fr.ambuconnect.administrateur.dto.AdministrateurDto;
import fr.ambuconnect.administrateur.entity.AdministrateurEntity;
import fr.ambuconnect.administrateur.services.AdministrateurService;
import fr.ambuconnect.administrateur.services.InscriptionService;
import fr.ambuconnect.chauffeur.dto.ChauffeurDto;
import io.quarkus.security.Authenticated;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
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

import fr.ambuconnect.paiement.services.PaiementService;
import fr.ambuconnect.administrateur.dto.InscriptionEntrepriseDto;
import fr.ambuconnect.paiement.services.AbonnementService;
import io.quarkus.arc.Arc;
import jakarta.annotation.security.PermitAll;

@Path("/administrateur")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Valid
public class AdministrateurResourse {

    private final AdministrateurService administrateurService;
    private final SecurityIdentity securityIdentity;
    private final PaiementService paiementService;
    private final InscriptionService inscriptionService;

    @Inject
    public AdministrateurResourse(SecurityIdentity securityIdentity, 
                                  AdministrateurService administrateurService,
                                  PaiementService paiementService,
                                  InscriptionService inscriptionService) {
        this.securityIdentity = securityIdentity;
        this.administrateurService = administrateurService;
        this.paiementService = paiementService;
        this.inscriptionService = inscriptionService;
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
    @RolesAllowed({"admin", "ADMIN", "chauffeur", "CHAUFFEUR"})
    public Response getAllChauffeur(@PathParam("id") UUID id){
        List<ChauffeurDto> chauffeurDtos = administrateurService.findAll(id);
        return Response.ok(chauffeurDtos).build();
    }

    @POST
    @Path("/createchauffeur")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response craeteChauffeur(@RequestBody ChauffeurDto chauffeurDto) throws Exception{
        try {
            ChauffeurDto dto = administrateurService.createChauffeur(chauffeurDto);
            return Response.ok(dto).build();
        } catch (Exception e) {
            throw new BadRequestException("Erreur lors de la cration", e);
        }
    }

    @POST
    @Path("/createAdmin")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createAdmin(@RequestBody AdministrateurDto administrateurDto) throws Exception {
        try {
            AdministrateurDto dto = administrateurService.creationAdmin(administrateurDto);
            return Response.ok(dto).build();
        } catch (Exception e) {
            throw new BadRequestException("Erreur lors de la cration", e);
        }
    }

    /**
     * Création d'un régulateur
     * Cet endpoint permet de créer un administrateur avec le rôle Régulateur
     */
    @POST
    @Path("/createRegulateur")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"admin", "ADMIN", "regulateur", "REGULATEUR"})
    public Response createRegulateur(@RequestBody AdministrateurDto administrateurDto) throws Exception {
        try {
            AdministrateurDto dto = administrateurService.createRegulateur(administrateurDto);
            return Response.ok(dto).build();
        } catch (Exception e) {
            throw new BadRequestException("Erreur lors de la cration", e);
        }
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


    @GET
@Path("/entreprise/{identreprise}")
@Produces(MediaType.APPLICATION_JSON)
public List<AdministrateurDto> getAdminsByEntreprise(@PathParam("identreprise") UUID identreprise) {
        return administrateurService.findByEntreprise(identreprise);
    }

    @GET
    @Path("/email/{email}/allchauffeur")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"admin", "ADMIN", "chauffeur", "CHAUFFEUR"})
    public Response getAllChauffeurByEmail(@PathParam("email") String email){
        try {
            AdministrateurEntity admin = AdministrateurEntity.findByEmail(email);
            if (admin == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Administrateur non trouvé avec l'email: " + email)
                    .build();
            }
            
            List<ChauffeurDto> chauffeurDtos = administrateurService.findAll(admin.getId());
            return Response.ok(chauffeurDtos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Erreur lors de la récupération des chauffeurs: " + e.getMessage())
                .build();
        }
    }

    /**
     * Création d'un superadmin
     * Cet endpoint permet de créer un compte superadmin
     */
    @POST
    @Path("/createSuperAdmin")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"admin", "ADMIN", "superadmin", "SUPERADMIN"})
    public RestResponse<AdministrateurDto> createSuperAdmin(@RequestBody @Valid AdministrateurDto administrateurDto) throws Exception {
        AdministrateurDto dto = administrateurService.createSuperAdmin(administrateurDto);
        return RestResponse.ok(dto);
    }

    @PUT
    @Path("/{id}/update")
    @RolesAllowed({"ADMIN", "SUPERADMIN"})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public RestResponse<AdministrateurDto> updateAdministrateur(@PathParam("id") UUID id, @RequestBody @Valid AdministrateurDto administrateurDto) {
        AdministrateurDto dto = administrateurService.updateAdministrateur(id, administrateurDto);
        return RestResponse.ok(dto);
    }

    @DELETE
    @Path("/{id}/delete")
    @RolesAllowed({"ADMIN", "SUPERADMIN"})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void deleteAdministrateur(@PathParam("id") UUID id) {
        administrateurService.deleteAdministrateur(id);
    }

    /**
     * Création d'un administrateur après paiement sur le site vitrine
     * Cet endpoint est public et permet l'inscription d'un nouvel administrateur après validation du paiement
     */
    @POST
    @Path("/inscription-entreprise")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response inscriptionEntreprise(@RequestBody AdministrateurDto administrateurDto, 
                                         @QueryParam("paymentIntentId") String paymentIntentId,
                                         @QueryParam("abonnementId") String abonnementId) {
        try {
            // Vérification des paramètres obligatoires
            if (abonnementId == null || abonnementId.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("L'ID d'abonnement Stripe est obligatoire")
                    .build();
            }
            
            if (paymentIntentId == null || paymentIntentId.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("L'ID de paiement Stripe est obligatoire")
                    .build();
            }
            
            // Vérification de l'abonnement (obligatoire)
            boolean abonnementValide = paiementService.verifierAbonnement(abonnementId);
            if (!abonnementValide) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("L'abonnement n'est pas actif. Veuillez contacter le support.")
                    .build();
            }
            
            // Vérification du paiement avec Stripe
            boolean paiementValide = paiementService.verifierPaiement(paymentIntentId, abonnementId);
            if (!paiementValide) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Le paiement n'a pas pu être validé. Veuillez contacter le support.")
                    .build();
            }
            
            // Création de l'administrateur avec son entreprise
            AdministrateurDto nouvelAdmin = administrateurService.inscriptionEntrepriseAdmin(administrateurDto, abonnementId);
            
            return Response.status(Response.Status.CREATED)
                .entity(nouvelAdmin)
                .build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Erreur lors de l'inscription: " + e.getMessage())
                .build();
        }
    }

    /**
     * Inscription complète d'une entreprise avec son administrateur
     * Utilise les informations de l'entreprise et de l'administrateur depuis le même DTO
     */
    @POST
    @Path("/inscription-complete")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    @PermitAll
    public Response inscriptionComplete(@RequestBody @Valid InscriptionEntrepriseDto inscriptionDto) {
        try {
            // Vérification des données obligatoires
            if (inscriptionDto.getStripeSubscriptionId() == null || inscriptionDto.getStripeSubscriptionId().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("L'ID d'abonnement Stripe est obligatoire")
                    .build();
            }
            
            // Vérification de l'abonnement (obligatoire)
            boolean abonnementValide = paiementService.verifierAbonnement(inscriptionDto.getStripeSubscriptionId());
            if (!abonnementValide) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("L'abonnement n'est pas actif. Veuillez contacter le support.")
                    .build();
            }
            
            // Vérification du paiement si fourni
            if (inscriptionDto.getStripePaymentIntentId() != null && !inscriptionDto.getStripePaymentIntentId().isEmpty()) {
                boolean paiementValide = paiementService.verifierPaiement(
                    inscriptionDto.getStripePaymentIntentId(), 
                    inscriptionDto.getStripeSubscriptionId()
                );
                
                if (!paiementValide) {
                    return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Le paiement n'a pas pu être validé. Veuillez contacter le support.")
                        .build();
                }
            }
            
            // Préparer les informations d'administrateur
            AdministrateurDto administrateurDto;
            
            if (inscriptionDto.getAdministrateur() != null) {
                // Utiliser l'administrateur fourni
                administrateurDto = inscriptionDto.getAdministrateur();
            } else {
                // Créer un objet administrateur à partir des informations de base
                if (inscriptionDto.getEmail() == null || inscriptionDto.getEmail().isEmpty() ||
                    inscriptionDto.getMotDePasse() == null || inscriptionDto.getMotDePasse().isEmpty()) {
                    return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Email et mot de passe sont obligatoires si les informations d'administrateur ne sont pas fournies")
                        .build();
                }
                
                administrateurDto = new AdministrateurDto();
                administrateurDto.setEmail(inscriptionDto.getEmail());
                administrateurDto.setMotDePasse(inscriptionDto.getMotDePasse());
                administrateurDto.setNom(inscriptionDto.getNom());
                administrateurDto.setPrenom(inscriptionDto.getPrenom());
                administrateurDto.setTelephone(inscriptionDto.getTelephone());
                administrateurDto.setActif(true);
            }
            
            // Définir le nom de l'entreprise dans l'administrateur pour la création
            administrateurDto.setEntrepriseNom(inscriptionDto.getEntreprise().getNom());
            
            // Création de l'administrateur et de l'entreprise
            AdministrateurDto nouvelAdmin = administrateurService.inscriptionEntrepriseAdmin(
                administrateurDto, 
                inscriptionDto.getStripeSubscriptionId()
            );
            
            // Enregistrement de l'abonnement dans la base de données
            AbonnementService abonnementService = Arc.container().instance(AbonnementService.class).get();
            
            // Utiliser le type d'abonnement effectif (provient de typeAbonnement ou subscriptionType)
            String typeAbonnement = inscriptionDto.getTypeAbonnementEffectif();
            
            
            return Response.status(Response.Status.CREATED)
                .entity(nouvelAdmin)
                .build();
                
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Erreur lors de l'inscription: " + e.getMessage())
                .build();
        }
    }

    /**
     * Nouveau workflow d'inscription: création de l'entreprise puis de l'administrateur
     * Le processus suit explicitement l'ordre suivant:
     * 1. Création de l'entreprise
     * 2. Récupération de l'ID de l'entreprise
     * 3. Création de l'administrateur associé à cette entreprise
     * 
     * L'abonnement peut être spécifié de deux façons:
     * - Soit par un code d'abonnement (START, PRO, ENTREPRISE) défini dans notre système
     * - Soit par un ID d'abonnement Stripe déjà créé
     */
    @POST
    @Path("/inscription-workflow")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response inscriptionWorkflow(@RequestBody @Valid InscriptionEntrepriseDto inscriptionDto) {
        try {
            // Si un code d'abonnement est fourni, pas besoin de vérifier l'abonnement Stripe
            // puisque nous allons le créer nous-mêmes
            if (inscriptionDto.getCodeAbonnement() == null || inscriptionDto.getCodeAbonnement().isEmpty()) {
                // Validation des données d'abonnement si aucun code n'est fourni
                if (inscriptionDto.getStripeSubscriptionId() != null && !inscriptionDto.getStripeSubscriptionId().isEmpty()) {
                    boolean abonnementValide = paiementService.verifierAbonnement(inscriptionDto.getStripeSubscriptionId());
                    if (!abonnementValide) {
                        return Response.status(Response.Status.BAD_REQUEST)
                            .entity("L'abonnement n'est pas actif. Veuillez contacter le support.")
                            .build();
                    }
                }
            }
            
            // Validation du paiement si un ID est fourni
            if (inscriptionDto.getStripePaymentIntentId() != null && !inscriptionDto.getStripePaymentIntentId().isEmpty()) {
                boolean paiementValide = paiementService.verifierPaiement(
                    inscriptionDto.getStripePaymentIntentId(),
                    inscriptionDto.getStripeSubscriptionId()
                );
                
                if (!paiementValide) {
                    return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Le paiement n'a pas pu être validé. Veuillez contacter le support.")
                        .build();
                }
            }
            
            // Appel du service qui gère le workflow complet
            AdministrateurDto nouvelAdmin = inscriptionService.inscrireEntreprise(inscriptionDto);
            
            return Response.status(Response.Status.CREATED)
                .entity(nouvelAdmin)
                .build();
                
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Erreur lors de l'inscription: " + e.getMessage())
                .build();
        }
    }

}
