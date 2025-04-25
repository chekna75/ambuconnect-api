package fr.ambuconnect.authentification.services;

import java.util.Optional;
import java.util.UUID;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.time.LocalDateTime;

import org.mindrot.jbcrypt.BCrypt;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import fr.ambuconnect.administrateur.dto.AdministrateurDto;
import fr.ambuconnect.administrateur.entity.AdministrateurEntity;
import fr.ambuconnect.administrateur.entity.SuperAdminEntity;
import fr.ambuconnect.authentification.entity.PasswordResetTokenEntity;
import fr.ambuconnect.authentification.mapper.AuthentificationMapper;
import fr.ambuconnect.authentification.utils.JwtUtils;
import fr.ambuconnect.chauffeur.dto.ChauffeurDto;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.paiement.entity.AbonnementEntity;
import fr.ambuconnect.paiement.services.StripeService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.ws.rs.NotAuthorizedException;
import fr.ambuconnect.authentification.dto.LoginRequestDto;
import fr.ambuconnect.chauffeur.services.ChauffeurConnexionService;
import jakarta.ws.rs.ForbiddenException;

@ApplicationScoped
public class AuthenService {
    
    private static final Logger LOG = LoggerFactory.getLogger(AuthenService.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    EmailService emailService;

    @Inject
    StripeService stripeService;

    @ConfigProperty(name = "app.frontend.url")
    String frontendUrl;

    private final AuthentificationMapper authentificationMapper;
    private final JwtUtils jwtUtils;
    private final ChauffeurConnexionService chauffeurConnexionService;

    @Inject
    public AuthenService(AuthentificationMapper authentificationMapper, JwtUtils jwtUtils, ChauffeurConnexionService chauffeurConnexionService) {
        this.authentificationMapper = authentificationMapper;
        this.jwtUtils = jwtUtils;
        this.chauffeurConnexionService = chauffeurConnexionService;
    }

    /**
     * Vérifie si l'identifiant de l'utilisateur correspond à l'email de l'administrateur.
     * 
     * @param identity L'identifiant de l'utilisateur.
     * @param administrateurEntity L'administrateur à vérifier.
     * @return true si l'identifiant correspond à l'email de l'administrateur, false sinon.
     */
    public boolean match(SecurityIdentity identity, AdministrateurEntity administrateurEntity) {
        return administrateurEntity.getEmail().equals(identity.getPrincipal().getName());
    }

    /**
     * Vérifie si l'identifiant de l'utilisateur correspond à l'email du chauffeur.
     * 
     * @param identity L'identifiant de l'utilisateur.
     * @param chauffeurEntity Le chauffeur à vérifier.
     * @return true si l'identifiant correspond à l'email du chauffeur, false sinon.
     */
    public boolean match(SecurityIdentity identity, ChauffeurEntity chauffeurEntity) {
        return chauffeurEntity.getEmail().equals(identity.getPrincipal().getName());
    }

    /**
     * Valide l'identifiant de l'utilisateur et retourne l'administrateur correspondant.
     * 
     * @param identity L'identifiant de l'utilisateur.
     * @return L'administrateur correspondant.
     * @throws NotFoundException Si l'administrateur n'est pas trouvé.
     */
    public AdministrateurDto validateAdministrateur(SecurityIdentity identity){
        Optional<AdministrateurEntity> administrateurEntity = Optional.ofNullable(AdministrateurEntity.findByEmail(identity.getPrincipal().getName()));
        if(administrateurEntity.isPresent() && match(identity, administrateurEntity.get())){
            return authentificationMapper.administrateurEntityToDto(administrateurEntity.get());
        }
        throw new NotFoundException("Administrateur non trouvé");
    }

    /**
     * Valide l'identifiant de l'utilisateur et retourne le chauffeur correspondant.
     * 
     * @param identity L'identifiant de l'utilisateur.
     * @return Le chauffeur correspondant.
     * @throws NotFoundException Si le chauffeur n'est pas trouvé.
     */
    public ChauffeurDto validateChauffeur(SecurityIdentity identity){
        Optional<ChauffeurEntity> chauffeurEntity = Optional.ofNullable(ChauffeurEntity.findByEmail(identity.getPrincipal().getName()));
        if(chauffeurEntity.isPresent() && match(identity, chauffeurEntity.get())){
            return authentificationMapper.chauffeurEntityToDto(chauffeurEntity.get());
        }
        throw new NotFoundException("Chauffeur non trouvé");
    }

    /**
     * Sert a hashser le mot de passe lors des creation user
     * 
     * @param motdePasse
     * @retrun le mdp hashser
     */
    public String hasherMotDePasse(String motDePasse) {
        return BCrypt.hashpw(motDePasse, BCrypt.gensalt());
    }

    /**
     * Sert a verifier le hashashe des mdp
     * 
     * @param motDePasse
     * @param motDePasseHashe
     * @return
     */
    public boolean verifierMotDePasse(String motDePasse, String motDePasseHashe) {
        try {
            return BCrypt.checkpw(motDePasse, motDePasseHashe);
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur lors de la vérification du mot de passe: " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public String connexionSuperAdmin(String email, String motDePasse) {
        try {
            LOG.info("Tentative de connexion superadmin - Email: {}", email);
            
            SuperAdminEntity admin = SuperAdminEntity.findByEmail(email);
            if (admin != null && verifierMotDePasse(motDePasse, admin.getMotDePasse())) {
                String role = "SUPERADMIN";
                return jwtUtils.generateCompleteTokenSuperAdmin(
                    admin.getId(),
                    admin.getEmail(),
                    role,
                    admin.getNom(),
                    admin.getPrenom()
                );
            }
            throw new IllegalArgumentException("Identifiants invalides");
        } catch (Exception e) {
            LOG.error("Erreur inattendue pour {}: {}", email, e.getMessage());
            throw e;
        }
    }
    

    @Transactional
    public String connexionAdmin(String email, String motDePasse, Boolean isAdmin) {
        try {
            LOG.info("Tentative de connexion admin - Email: {}", email);
            
            AdministrateurEntity admin = AdministrateurEntity.findByEmail(email);
            if (admin != null && verifierMotDePasse(motDePasse, admin.getMotDePasse())) {
                // Déterminer le rôle correct
                String role = "ADMIN";
                if (email.equals("superadmin@ambuconnect.fr")) {
                    role = "SUPERADMIN";
                }
                
                // Vérifier si l'entreprise a un abonnement actif et récupérer son type
                boolean abonnementActif = false;
                String planType = "START"; // Plan par défaut si aucun abonnement trouvé
                
                if (admin.getEntreprise() != null) {
                    try {
                        // Récupérer l'abonnement actif
                        AbonnementEntity abonnement = AbonnementEntity.find(
                            "entreprise.id = ?1 and actif = true", 
                            admin.getEntreprise().getId()
                        ).firstResult();
                        
                        if (abonnement != null && abonnement.getStripeSubscriptionId() != null) {
                            // Vérifier le statut dans Stripe
                            com.stripe.model.Subscription subscription = 
                                com.stripe.model.Subscription.retrieve(abonnement.getStripeSubscriptionId());
                            
                            if (subscription != null) {
                                abonnementActif = "active".equals(subscription.getStatus());
                                String priceId = subscription.getItems().getData().get(0).getPrice().getId();
                                planType = stripeService.getPlanTypeFromPriceId(priceId);
                                LOG.info("Abonnement Stripe trouvé - Type: {}, Actif: {}", planType, abonnementActif);
                            }
                        } else {
                            LOG.warn("Aucun abonnement actif trouvé pour l'entreprise: {}", admin.getEntreprise().getId());
                        }
                    } catch (Exception e) {
                        LOG.error("Erreur lors de la vérification de l'abonnement Stripe: {}", e.getMessage());
                        // En cas d'erreur, on utilise les valeurs par défaut
                    }
                }
                
                return jwtUtils.generateCompleteToken(
                    admin.getId(),
                    admin.getEmail(),
                    role,
                    admin.getEntreprise().getId(),
                    admin.getEntreprise().getAmbulances().get(0).getId(),
                    admin.getNom(),
                    admin.getPrenom(),
                    admin.getTelephone(),
                    admin.getEntreprise().getNom(),
                    admin.getEntreprise().getSiret(),
                    admin.getEntreprise().getAdresse(),
                    abonnementActif,
                    planType
                );
            }
            throw new IllegalArgumentException("Identifiants invalides");
        } catch (Exception e) {
            LOG.error("Erreur inattendue pour {}: {}", email, e.getMessage());
            throw e;
        }
    }

    public String connexionChauffeur(String email, String motDePasse, Boolean isAdmin) {
        LOG.debug("Connexion chauffeur demandée pour: " + email);
        
        try {
            ChauffeurEntity chauffeur = ChauffeurEntity.findByEmail(email);
            
            if (chauffeur == null) {
                LOG.warn("Chauffeur non trouvé avec l'email: " + email);
                throw new NotAuthorizedException("Email ou mot de passe incorrect");
            }
            
            if (!chauffeur.isDisponible()) {
                LOG.warn("Chauffeur {} non disponible", chauffeur.getId());
                throw new NotAuthorizedException("Votre compte n'est pas actif. Contactez votre administrateur.");
            }

            // Vérifier si le chauffeur peut se connecter selon les limites du pack
            chauffeurConnexionService.verifierPossibiliteConnexion(chauffeur);
            
            // Vérification du mot de passe
            if (!verifierMotDePasse(motDePasse, chauffeur.getMotDePasse())) {
                LOG.warn("Tentative de connexion avec mot de passe invalide pour: " + email);
                throw new NotAuthorizedException("Email ou mot de passe incorrect");
            }
            
            // Création du token JWT
            return jwtUtils.generateCompleteToken(
                chauffeur.getId(),
                chauffeur.getEmail(),
                chauffeur.getRole().getNom(),
                chauffeur.getEntreprise().getId(),
                chauffeur.getEntreprise().getAmbulances().get(0).getId(),
                chauffeur.getNom(),
                chauffeur.getPrenom(),
                chauffeur.getTelephone(),
                chauffeur.getEntreprise().getNom(),
                chauffeur.getEntreprise().getSiret(),
                chauffeur.getEntreprise().getAdresse(),
                true, // abonnementActif
                "PREMIUM" // planType
            );
            
        } catch (ForbiddenException e) {
            // Simplement relancer l'exception pour les limitations du pack
            throw e;
        } catch (Exception e) {
            LOG.error("Erreur lors de la connexion du chauffeur", e);
            throw new NotAuthorizedException("Une erreur est survenue lors de la connexion");
        }
    }

    @Transactional
    public void reinitialiserMotDePasse(String chauffeurEmail, String nouveauMotDePasse) {
        try {
            System.out.println("Début réinitialisation mot de passe pour chauffeur: " + chauffeurEmail);
            
            ChauffeurEntity chauffeur = ChauffeurEntity.findByEmail(chauffeurEmail);
            if (chauffeur == null) {
                throw new NotFoundException("Chauffeur non trouvé");
            }
            
            // Hasher le nouveau mot de passe
            String motDePasseHashe = hasherMotDePasse(nouveauMotDePasse);
            
            // Mettre à jour le mot de passe
            chauffeur.setMotDePasse(motDePasseHashe);
            chauffeur.persist();
            
            System.out.println("Mot de passe réinitialisé avec succès");
            
        } catch (Exception e) {
            System.out.println("Erreur lors de la réinitialisation du mot de passe: " + e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void reinitialiserMotDePasseAdmin(String adminEmail, String nouveauMotDePasse) {
        try {
            System.out.println("Début réinitialisation mot de passe pour administrateur: " + adminEmail);
            
            AdministrateurEntity admin = AdministrateurEntity.findByEmail(adminEmail);
            if (admin == null) {
                throw new NotFoundException("Administrateur non trouvé");
            }
            
            // Hasher le nouveau mot de passe
            String motDePasseHashe = hasherMotDePasse(nouveauMotDePasse);
            
            // Mettre à jour le mot de passe
            admin.setMotDePasse(motDePasseHashe);
            admin.persist();
            
            System.out.println("Mot de passe réinitialisé avec succès");
            
        } catch (Exception e) {
            System.out.println("Erreur lors de la réinitialisation du mot de passe: " + e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void demandeReinitialisationMotDePasse(String email) {
        LOG.info("Demande de réinitialisation de mot de passe pour: " + email);
        
        // Vérifier si c'est un admin ou un chauffeur
        AdministrateurEntity admin = AdministrateurEntity.findByEmail(email);
        ChauffeurEntity chauffeur = ChauffeurEntity.findByEmail(email);
        
        if (admin == null && chauffeur == null) {
            LOG.warn("Aucun utilisateur trouvé avec l'email: " + email);
            // On renvoie quand même un succès pour ne pas exposer quels emails existent
            return;
        }
        
        // Créer un token
        String token = UUID.randomUUID().toString();
        PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity();
        resetToken.setToken(token);
        resetToken.setUserId(admin != null ? admin.getId() : chauffeur.getId());
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(false);
        
        entityManager.persist(resetToken);
        
// Envoyer l'email approprié en fonction du type d'utilisateur
    if (admin != null) {
        // Si c'est un admin, utiliser la méthode standard
        emailService.sendPasswordResetEmail(email, token);
        LOG.info("Email de réinitialisation pour ADMIN envoyé à: " + email);
    } else {
        // Si c'est un chauffeur, utiliser la méthode spécifique aux chauffeurs
        emailService.sendPasswordResetEmailChauffeur(email, token);
        LOG.info("Email de réinitialisation pour CHAUFFEUR envoyé à: " + email);
    }
        
        LOG.info("Email de réinitialisation envoyé à: " + email);
    }

    @Transactional
    public void finaliserReinitialisationMotDePasse(String token, String email, String nouveauMotDePasse) {
        PasswordResetTokenEntity resetToken = PasswordResetTokenEntity.findByToken(token);
        
        if (resetToken == null || resetToken.isExpired() || resetToken.isUsed()) {
            throw new BadRequestException("Token invalide ou expiré");
        }
        
        // Chercher l'utilisateur par email
        AdministrateurEntity admin = AdministrateurEntity.findByEmail(email);
        if (admin != null) {
            if (!admin.getId().equals(resetToken.getUserId())) {
                throw new BadRequestException("Token invalide pour cet utilisateur");
            }
            reinitialiserMotDePasseAdmin(email, nouveauMotDePasse);
        } else {
            ChauffeurEntity chauffeur = ChauffeurEntity.findByEmail(email);
            if (chauffeur != null) {
                if (!chauffeur.getId().equals(resetToken.getUserId())) {
                    throw new BadRequestException("Token invalide pour cet utilisateur");
                }
                reinitialiserMotDePasse(email, nouveauMotDePasse);
            } else {
                throw new NotFoundException("Utilisateur non trouvé");
            }
        }
        
        // Marquer le token comme utilisé
        resetToken.setUsed(true);
        
        LOG.info("Réinitialisation du mot de passe terminée avec succès pour: {}", email);
    }

    /**
     * Enregistre la déconnexion d'un chauffeur
     */
    public void enregistrerDeconnexionChauffeur(UUID chauffeurId, UUID entrepriseId) {
        LOG.info("Enregistrement de la déconnexion du chauffeur: {}", chauffeurId);
        
        try {
            chauffeurConnexionService.enregistrerDeconnexion(chauffeurId, entrepriseId);
        } catch (Exception e) {
            LOG.error("Erreur lors de l'enregistrement de la déconnexion", e);
        }
    }

}
