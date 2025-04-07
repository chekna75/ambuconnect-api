package fr.ambuconnect.chauffeur.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ambuconnect.administrateur.entity.AdministrateurEntity;
import fr.ambuconnect.authentification.services.EmailService;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import fr.ambuconnect.paiement.entity.AbonnementEntity;
import fr.ambuconnect.paiement.entity.PlanTarifaireEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;

@ApplicationScoped
public class ChauffeurConnexionService {

    private static final Logger LOG = LoggerFactory.getLogger(ChauffeurConnexionService.class);

    // Map pour suivre les chauffeurs connectés par entreprise: EntrepriseId -> Map de (ChauffeurId -> HorodatageConnexion)
    private Map<UUID, Map<UUID, LocalDateTime>> chauffeurConnectes = new ConcurrentHashMap<>();
    
    // Map pour suivre les alertes déjà envoyées aux administrateurs: EntrepriseId -> Map de (AlerteType -> DateDernierEnvoi)
    private Map<UUID, Map<String, LocalDateTime>> alertesEnvoyees = new ConcurrentHashMap<>();
    
    private final EmailService emailService;
    
    @Inject
    public ChauffeurConnexionService(EmailService emailService) {
        this.emailService = emailService;
    }
    
    /**
     * Vérifie si un chauffeur peut se connecter en fonction des limites du pack d'abonnement
     * 
     * @param chauffeur Le chauffeur qui tente de se connecter
     * @return true si le chauffeur peut se connecter, false sinon
     */
    public boolean verifierPossibiliteConnexion(ChauffeurEntity chauffeur) throws ForbiddenException {
        LOG.debug("Vérification de la possibilité de connexion pour le chauffeur ID: {}", chauffeur.getId());
        
        EntrepriseEntity entreprise = chauffeur.getEntreprise();
        if (entreprise == null) {
            LOG.error("Le chauffeur n'est pas associé à une entreprise: {}", chauffeur.getId());
            throw new ForbiddenException("Vous n'êtes pas associé à une entreprise valide.");
        }
        
        // 1. Vérifier l'abonnement actif de l'entreprise
        AbonnementEntity abonnement = AbonnementEntity.find("entreprise.id = ?1 and actif = true", entreprise.getId())
                .firstResult();
        
        if (abonnement == null) {
            LOG.error("Aucun abonnement actif trouvé pour l'entreprise: {}", entreprise.getId());
            notifierAdministrateurProblemeAbonnement(entreprise);
            throw new ForbiddenException("Votre entreprise n'a pas d'abonnement actif. Contactez votre administrateur.");
        }
        
        // 2. Récupérer le plan tarifaire associé à l'abonnement
        PlanTarifaireEntity planTarifaire;
        try {
            planTarifaire = PlanTarifaireEntity.findById(UUID.fromString(abonnement.getPlanId()));
        } catch (IllegalArgumentException e) {
            LOG.error("Format d'ID de plan invalide: {}", abonnement.getPlanId());
            throw new ForbiddenException("Une erreur est survenue avec votre abonnement. Contactez votre administrateur.");
        }
        
        if (planTarifaire == null) {
            LOG.error("Plan tarifaire non trouvé pour l'abonnement: {}", abonnement.getId());
            throw new ForbiddenException("Une erreur est survenue avec votre abonnement. Contactez votre administrateur.");
        }
        
        // 3. Vérifier le nombre total de chauffeurs dans l'entreprise
        long nombreTotalChauffeurs = ChauffeurEntity.count("entreprise.id", entreprise.getId());
        if (planTarifaire.getNbMaxChauffeurs() != null && nombreTotalChauffeurs > planTarifaire.getNbMaxChauffeurs()) {
            LOG.error("Nombre maximum de chauffeurs atteint pour l'entreprise: {} (max: {}, actuel: {})", 
                      entreprise.getId(), planTarifaire.getNbMaxChauffeurs(), nombreTotalChauffeurs);
            
            notifierAdministrateurLimiteMaxChauffeurs(entreprise, planTarifaire, (int) nombreTotalChauffeurs);
            throw new ForbiddenException("Le nombre maximum de chauffeurs autorisés pour votre entreprise est atteint. Contactez votre administrateur.");
        }
        
        // 4. Vérifier si le seuil d'alerte de chauffeurs est atteint (mais pas encore la limite)
        if (planTarifaire.getNbMaxChauffeurs() != null) {
            double pourcentageUtilisation = ((double) nombreTotalChauffeurs / planTarifaire.getNbMaxChauffeurs()) * 100;
            if (pourcentageUtilisation >= 80 && 
                nombreTotalChauffeurs < planTarifaire.getNbMaxChauffeurs()) {
                
                notifierAdministrateurSeuilAlerteChauffeurs(entreprise, planTarifaire, (int) nombreTotalChauffeurs);
            }
        }
        
        // 5. Vérifier le nombre de connexions simultanées
        Map<UUID, LocalDateTime> chauffeurEntreprise = chauffeurConnectes.computeIfAbsent(
            entreprise.getId(), k -> new ConcurrentHashMap<>()
        );
        
        // Nettoyage des sessions inactives (plus de 1 heure)
        chauffeurEntreprise.entrySet().removeIf(entry -> 
            entry.getValue().plusHours(1).isBefore(LocalDateTime.now())
        );
        
        int connexionsSimultanees = chauffeurEntreprise.size();
        
        // Si le chauffeur est déjà connecté, on ne le compte pas à nouveau
        if (chauffeurEntreprise.containsKey(chauffeur.getId())) {
            connexionsSimultanees--;
        }
        
        if (planTarifaire.getNbMaxConnexionsSimultanees() != null && 
            connexionsSimultanees >= planTarifaire.getNbMaxConnexionsSimultanees()) {
            LOG.error("Nombre maximum de connexions simultanées atteint pour l'entreprise: {} (max: {}, actuel: {})", 
                      entreprise.getId(), planTarifaire.getNbMaxConnexionsSimultanees(), connexionsSimultanees);
            
            notifierAdministrateurLimiteConnexions(entreprise, planTarifaire, connexionsSimultanees);
            throw new ForbiddenException("Le nombre maximum de connexions simultanées est atteint pour votre entreprise. Veuillez réessayer plus tard ou contactez votre administrateur.");
        }
        
        // Tout est OK, on enregistre la connexion
        chauffeurEntreprise.put(chauffeur.getId(), LocalDateTime.now());
        LOG.info("Connexion autorisée pour le chauffeur ID: {}", chauffeur.getId());
        return true;
    }
    
    /**
     * Enregistre la déconnexion d'un chauffeur
     */
    public void enregistrerDeconnexion(UUID chauffeurId, UUID entrepriseId) {
        LOG.debug("Déconnexion du chauffeur ID: {}", chauffeurId);
        
        if (entrepriseId != null && chauffeurConnectes.containsKey(entrepriseId)) {
            chauffeurConnectes.get(entrepriseId).remove(chauffeurId);
        }
    }
    
    /**
     * Récupère la liste des chauffeurs actuellement connectés pour une entreprise
     */
    public Map<UUID, LocalDateTime> getChauffeurConnectes(UUID entrepriseId) {
        return chauffeurConnectes.getOrDefault(entrepriseId, new ConcurrentHashMap<>());
    }
    
    /**
     * Récupère le nombre de chauffeurs actuellement connectés pour une entreprise
     */
    public int getNombreChauffeurConnectes(UUID entrepriseId) {
        Map<UUID, LocalDateTime> chauffeurEntreprise = chauffeurConnectes.get(entrepriseId);
        if (chauffeurEntreprise == null) {
            return 0;
        }
        
        // Nettoyage des sessions inactives (plus de 1 heure)
        chauffeurEntreprise.entrySet().removeIf(entry -> 
            entry.getValue().plusHours(1).isBefore(LocalDateTime.now())
        );
        
        return chauffeurEntreprise.size();
    }
    
    // Méthodes de notification
    
    private void notifierAdministrateurProblemeAbonnement(EntrepriseEntity entreprise) {
        if (devraitEnvoyerAlerte(entreprise.getId(), "probleme_abonnement")) {
            // Trouver les administrateurs de l'entreprise
            List<AdministrateurEntity> admins = AdministrateurEntity.list("entreprise.id", entreprise.getId())
                .stream()
                .map(admin -> (AdministrateurEntity) admin)
                .collect(Collectors.toList());
                
            for (AdministrateurEntity admin : admins) {
                if ("ADMIN".equalsIgnoreCase(admin.getRole().getNom())) {
                    try {
                        emailService.sendEmail(
                            admin.getEmail(),
                            "Problème d'abonnement AmbuConnect",
                            "Bonjour " + admin.getPrenom() + ",\n\n" +
                            "Nous avons détecté que votre entreprise " + entreprise.getNom() + " n'a pas d'abonnement actif. " +
                            "Vos chauffeurs ne peuvent pas se connecter à l'application AmbuConnect.\n\n" +
                            "Veuillez vérifier votre abonnement dans votre espace administrateur ou contacter notre support.\n\n" +
                            "Cordialement,\n" +
                            "L'équipe AmbuConnect"
                        );
                    } catch (Exception e) {
                        LOG.error("Erreur lors de l'envoi de l'email de notification d'abonnement", e);
                    }
                }
            }
            enregistrerEnvoiAlerte(entreprise.getId(), "probleme_abonnement");
        }
    }
    
    private void notifierAdministrateurLimiteMaxChauffeurs(EntrepriseEntity entreprise, PlanTarifaireEntity plan, int nombreActuel) {
        if (devraitEnvoyerAlerte(entreprise.getId(), "limite_max_chauffeurs")) {
            // Trouver les administrateurs de l'entreprise
            List<AdministrateurEntity> admins = AdministrateurEntity.list("entreprise.id", entreprise.getId())
                .stream()
                .map(admin -> (AdministrateurEntity) admin)
                .collect(Collectors.toList());
                
            for (AdministrateurEntity admin : admins) {
                if ("ADMIN".equalsIgnoreCase(admin.getRole().getNom())) {
                    try {
                        emailService.sendEmail(
                            admin.getEmail(),
                            "Limite de chauffeurs atteinte - AmbuConnect",
                            "Bonjour " + admin.getPrenom() + ",\n\n" +
                            "Le nombre maximum de chauffeurs autorisés pour votre entreprise " + entreprise.getNom() + " a été atteint.\n\n" +
                            "Votre abonnement " + plan.getNom() + " vous permet d'avoir " + plan.getNbMaxChauffeurs() + " chauffeurs, " +
                            "et vous avez actuellement " + nombreActuel + " chauffeurs.\n\n" +
                            "Pour permettre l'ajout de nouveaux chauffeurs, veuillez passer à un abonnement supérieur dans votre espace administrateur.\n\n" +
                            "Cordialement,\n" +
                            "L'équipe AmbuConnect"
                        );
                    } catch (Exception e) {
                        LOG.error("Erreur lors de l'envoi de l'email de notification de limite de chauffeurs", e);
                    }
                }
            }
            enregistrerEnvoiAlerte(entreprise.getId(), "limite_max_chauffeurs");
        }
    }
    
    private void notifierAdministrateurSeuilAlerteChauffeurs(EntrepriseEntity entreprise, PlanTarifaireEntity plan, int nombreActuel) {
        if (devraitEnvoyerAlerte(entreprise.getId(), "seuil_alerte_chauffeurs")) {
            // Trouver les administrateurs de l'entreprise
            List<AdministrateurEntity> admins = AdministrateurEntity.list("entreprise.id", entreprise.getId())
                .stream()
                .map(admin -> (AdministrateurEntity) admin)
                .collect(Collectors.toList());
                
            for (AdministrateurEntity admin : admins) {
                if ("ADMIN".equalsIgnoreCase(admin.getRole().getNom())) {
                    try {
                        double pourcentage = ((double) nombreActuel / plan.getNbMaxChauffeurs()) * 100;
                        emailService.sendEmail(
                            admin.getEmail(),
                            "Approche de la limite de chauffeurs - AmbuConnect",
                            "Bonjour " + admin.getPrenom() + ",\n\n" +
                            "Votre entreprise " + entreprise.getNom() + " approche de la limite de chauffeurs autorisés.\n\n" +
                            "Vous utilisez actuellement " + String.format("%.1f", pourcentage) + "% de votre capacité maximum " +
                            "(" + nombreActuel + " chauffeurs sur " + plan.getNbMaxChauffeurs() + " autorisés avec votre abonnement " + plan.getNom() + ").\n\n" +
                            "Si vous prévoyez d'ajouter d'autres chauffeurs, envisagez de passer à un abonnement supérieur.\n\n" +
                            "Cordialement,\n" +
                            "L'équipe AmbuConnect"
                        );
                    } catch (Exception e) {
                        LOG.error("Erreur lors de l'envoi de l'email d'alerte de seuil de chauffeurs", e);
                    }
                }
            }
            enregistrerEnvoiAlerte(entreprise.getId(), "seuil_alerte_chauffeurs");
        }
    }
    
    private void notifierAdministrateurLimiteConnexions(EntrepriseEntity entreprise, PlanTarifaireEntity plan, int nombreActuel) {
        if (devraitEnvoyerAlerte(entreprise.getId(), "limite_connexions")) {
            // Trouver les administrateurs de l'entreprise
            List<AdministrateurEntity> admins = AdministrateurEntity.list("entreprise.id", entreprise.getId())
                .stream()
                .map(admin -> (AdministrateurEntity) admin)
                .collect(Collectors.toList());
                
            for (AdministrateurEntity admin : admins) {
                if ("ADMIN".equalsIgnoreCase(admin.getRole().getNom())) {
                    try {
                        emailService.sendEmail(
                            admin.getEmail(),
                            "Limite de connexions simultanées atteinte - AmbuConnect",
                            "Bonjour " + admin.getPrenom() + ",\n\n" +
                            "Le nombre maximum de connexions simultanées pour votre entreprise " + entreprise.getNom() + " a été atteint.\n\n" +
                            "Votre abonnement " + plan.getNom() + " vous permet d'avoir " + plan.getNbMaxConnexionsSimultanees() + " chauffeurs connectés simultanément, " +
                            "et cette limite a été atteinte.\n\n" +
                            "Pour permettre plus de connexions simultanées, veuillez passer à un abonnement supérieur dans votre espace administrateur.\n\n" +
                            "Voici la liste des chauffeurs actuellement connectés:\n" +
                            getChauffeurConnectesString(entreprise.getId()) + "\n\n" +
                            "Cordialement,\n" +
                            "L'équipe AmbuConnect"
                        );
                    } catch (Exception e) {
                        LOG.error("Erreur lors de l'envoi de l'email de notification de limite de connexions", e);
                    }
                }
            }
            enregistrerEnvoiAlerte(entreprise.getId(), "limite_connexions");
        }
    }
    
    private String getChauffeurConnectesString(UUID entrepriseId) {
        Map<UUID, LocalDateTime> chauffeurEntreprise = chauffeurConnectes.get(entrepriseId);
        if (chauffeurEntreprise == null || chauffeurEntreprise.isEmpty()) {
            return "Aucun chauffeur connecté";
        }
        
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<UUID, LocalDateTime> entry : chauffeurEntreprise.entrySet()) {
            ChauffeurEntity chauffeur = ChauffeurEntity.findById(entry.getKey());
            if (chauffeur != null) {
                sb.append("- ").append(chauffeur.getPrenom()).append(" ").append(chauffeur.getNom());
                sb.append(" (").append(entry.getValue()).append(")\n");
            }
        }
        return sb.toString();
    }
    
    private boolean devraitEnvoyerAlerte(UUID entrepriseId, String typeAlerte) {
        Map<String, LocalDateTime> alertesEntreprise = alertesEnvoyees.computeIfAbsent(
            entrepriseId, k -> new ConcurrentHashMap<>()
        );
        
        LocalDateTime dernierEnvoi = alertesEntreprise.get(typeAlerte);
        
        // N'envoyer une alerte que si la dernière date d'il y a plus de 24 heures ou jamais envoyée
        return dernierEnvoi == null || dernierEnvoi.plusHours(24).isBefore(LocalDateTime.now());
    }
    
    private void enregistrerEnvoiAlerte(UUID entrepriseId, String typeAlerte) {
        Map<String, LocalDateTime> alertesEntreprise = alertesEnvoyees.computeIfAbsent(
            entrepriseId, k -> new ConcurrentHashMap<>()
        );
        
        alertesEntreprise.put(typeAlerte, LocalDateTime.now());
    }
} 