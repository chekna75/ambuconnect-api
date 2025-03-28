package fr.ambuconnect.authentification.services;

import java.util.Optional;
import java.util.UUID;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;

import org.mindrot.jbcrypt.BCrypt;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import fr.ambuconnect.administrateur.dto.AdministrateurDto;
import fr.ambuconnect.administrateur.entity.AdministrateurEntity;
import fr.ambuconnect.authentification.mapper.AuthentificationMapper;
import fr.ambuconnect.authentification.utils.JwtUtils;
import fr.ambuconnect.chauffeur.dto.ChauffeurDto;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class AuthenService {
    
    private final AuthentificationMapper authentificationMapper;
    private final JwtUtils jwtUtils;




    @Inject
    public AuthenService(AuthentificationMapper authentificationMapper, JwtUtils jwtUtils) {
        this.authentificationMapper = authentificationMapper;
        this.jwtUtils = jwtUtils;
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
    public String connexionAdmin(String email, String motDePasse, Boolean isAdmin) {
        try {
            System.out.println("Tentative de connexion admin - Email: " + email);
            
            AdministrateurEntity admin = AdministrateurEntity.findByEmail(email);
            if (admin != null && verifierMotDePasse(motDePasse, admin.getMotDePasse())) {
                // Déterminer le rôle correct
                String role = "ADMIN";
                if (email.equals("superadmin@ambuconnect.fr")) {
                    role = "SUPERADMIN";
                }
                
                // Vérifier si l'entreprise a un abonnement actif
                boolean abonnementActif = true; // Par défaut, tous les utilisateurs ont un abonnement actif
                String planType = "PREMIUM"; // Par défaut, tous les utilisateurs ont un plan PREMIUM
                
                return jwtUtils.generateCompleteToken(
                    admin.getId(),
                    admin.getEmail(),
                    role,
                    admin.getEntreprise().getId(),
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
            System.out.println("Erreur lors de la connexion: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public String connexionChauffeur(String email, String motDePasse, Boolean isAdmin) {
        try {
            System.out.println("=== Début connexion chauffeur ===");
            
            ChauffeurEntity chauffeur = ChauffeurEntity.findByEmail(email);
            if (chauffeur != null && verifierMotDePasse(motDePasse, chauffeur.getMotDePasse())) {
                // Déterminer le rôle correct
                String role = "CHAUFFEUR";
                
                // Vérifier si l'entreprise a un abonnement actif
                boolean abonnementActif = true; // Par défaut, tous les utilisateurs ont un abonnement actif
                String planType = "PREMIUM"; // Par défaut, tous les utilisateurs ont un plan PREMIUM
                
                return jwtUtils.generateCompleteToken(
                    chauffeur.getId(),
                    chauffeur.getEmail(),
                    role,
                    chauffeur.getEntreprise().getId(),
                    chauffeur.getNom(),
                    chauffeur.getPrenom(),
                    chauffeur.getTelephone(),
                    chauffeur.getEntreprise().getNom(),
                    chauffeur.getEntreprise().getSiret(),
                    chauffeur.getEntreprise().getAdresse(),
                    abonnementActif,
                    planType
                );
            }
            throw new IllegalArgumentException("Identifiants invalides");
        } catch (Exception e) {
            System.out.println("Erreur lors de la connexion: " + e.getMessage());
            e.printStackTrace();
            throw e;
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

}
