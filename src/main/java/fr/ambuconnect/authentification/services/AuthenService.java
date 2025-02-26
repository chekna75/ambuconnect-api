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

    @ConfigProperty(name = "jwt.secret")
    String jwtSecret;

    @ConfigProperty(name = "jwt.expiration")
    private long jwtExpiration;

    @ConfigProperty(name = "jwt.issuer")
    String jwtIssuer;

    @Inject
    public AuthenService(AuthentificationMapper authentificationMapper) {
        this.authentificationMapper = authentificationMapper;
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
            
            UUID userId = verifierIdentifiantsAdmin(email, motDePasse);
            System.out.println("Résultat vérification - UserId: " + userId);
            
            if (userId != null) {
                String token = generateJWT(userId, isAdmin);
                System.out.println("Token généré avec succès");
                return token;
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
            System.out.println("Email reçu: " + email);
            System.out.println("Mot de passe reçu (longueur): " + (motDePasse != null ? motDePasse.length() : "null"));
            
            UUID userId = verifierIdentifiantsChauffeur(email, motDePasse);
            System.out.println("Résultat vérification - UserId: " + userId);
            
            if (userId != null) {
                System.out.println("Génération du token pour userId: " + userId);
                String token = generateJWT(userId, isAdmin);
                System.out.println("Token généré avec succès (longueur): " + token.length());
                return token;
            }
            System.out.println("!!! Échec de la vérification des identifiants !!!");
            throw new IllegalArgumentException("Identifiants invalides");
        } catch (Exception e) {
            System.out.println("!!! Erreur lors de la connexion !!!");
            System.out.println("Type d'erreur: " + e.getClass().getName());
            System.out.println("Message d'erreur: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private String generateJWT(UUID userId, Boolean isAdmin) {
        try {
            System.out.println("Génération du JWT pour l'utilisateur: " + userId);
            
            if (isAdmin) {
                AdministrateurEntity admin = AdministrateurEntity.findById(userId);
                if (admin == null) {
                    throw new NotFoundException("Administrateur non trouvé");
                }
                return Jwt.claims()
                    .subject(userId.toString())
                    .issuer(jwtIssuer)
                    .issuedAt(System.currentTimeMillis())
                    .expiresIn(Duration.ofHours(1))
                    .groups(new HashSet<>(Arrays.asList("admin")))
                    .claim("id", admin.getId().toString())
                    .claim("nom", admin.getNom())
                    .claim("prenom", admin.getPrenom())
                    .claim("email", admin.getEmail())
                    .claim("telephone", admin.getTelephone())
                    .claim("entrepriseId", admin.getEntreprise().getId().toString())
                    .claim("entrepriseNom", admin.getEntreprise().getNom())
                    .sign(jwtSecret);
            } else {
                ChauffeurEntity chauffeur = ChauffeurEntity.findById(userId);
                if (chauffeur == null) {
                    throw new NotFoundException("Chauffeur non trouvé");
                }
                return Jwt.claims()
                    .subject(userId.toString())
                    .issuer(jwtIssuer)
                    .issuedAt(System.currentTimeMillis())
                    .expiresIn(Duration.ofHours(1))
                    .groups(new HashSet<>(Arrays.asList("chauffeur")))
                    .claim("id", chauffeur.getId().toString())
                    .claim("nom", chauffeur.getNom())
                    .claim("prenom", chauffeur.getPrenom())
                    .claim("email", chauffeur.getEmail())
                    .claim("telephone", chauffeur.getTelephone())
                    .claim("entrepriseId", chauffeur.getEntreprise().getId().toString())
                    .claim("entrepriseNom", chauffeur.getEntreprise().getNom())
                    .sign(jwtSecret);
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la génération du JWT: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la génération du token: " + e.getMessage());
        }
    }

    private UUID verifierIdentifiantsAdmin(String email, String motDePasse) {
        System.out.println("Vérification des identifiants admin");
        AdministrateurEntity administrateurEntity = AdministrateurEntity.findByEmail(email);
        
        if (administrateurEntity != null) {
            System.out.println("Administrateur trouvé avec l'ID: " + administrateurEntity.getId());
            boolean motDePasseValide = verifierMotDePasse(motDePasse, administrateurEntity.getMotDePasse());
            System.out.println("Mot de passe valide: " + motDePasseValide);
            
            if (motDePasseValide) {
                return administrateurEntity.getId();
            }
        } else {
            System.out.println("Aucun administrateur trouvé pour l'email: " + email);
        }
        return null;
    }

    private UUID verifierIdentifiantsChauffeur(String email, String motDePasse) {
        System.out.println("Vérification des identifiants chauffeur - Email: " + email);
        
        ChauffeurEntity chauffeurEntity = ChauffeurEntity.findByEmail(email);
        
        if (chauffeurEntity != null) {
            System.out.println("Chauffeur trouvé avec l'ID: " + chauffeurEntity.getId());
            boolean motDePasseValide = verifierMotDePasse(motDePasse, chauffeurEntity.getMotDePasse());
            System.out.println("Mot de passe valide: " + motDePasseValide);
            
            if (motDePasseValide) {
                return chauffeurEntity.getId();
            }
        } else {
            System.out.println("Aucun chauffeur trouvé pour l'email: " + email);
        }
        return null;
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

}
