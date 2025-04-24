package fr.ambuconnect.etablissement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.ambuconnect.administrateur.entity.AdministrateurEntity;
import fr.ambuconnect.authentification.services.AuthenService;
import fr.ambuconnect.common.exceptions.BadRequestException;
import fr.ambuconnect.common.exceptions.NotFoundException;
import fr.ambuconnect.etablissement.dto.EtablissementSanteDto;
import fr.ambuconnect.etablissement.dto.UtilisateurEtablissementDto;
import fr.ambuconnect.etablissement.entity.EtablissementSante;
import fr.ambuconnect.etablissement.entity.UtilisateurEtablissement;
import fr.ambuconnect.etablissement.mapper.EtablissementMapper;
import fr.ambuconnect.notification.service.EmailService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class EtablissementService {

    @Inject
    EntityManager entityManager;

    @Inject
    EtablissementMapper mapper;

    @Inject
    AuthenService authenService;

    @Inject
    EmailService emailService;

    @Transactional
    public EtablissementSanteDto creerEtablissement(EtablissementSanteDto dto) {
        log.debug("Création d'un établissement de santé : {}", dto.getEmailContact());

        // Vérifier si l'email existe déjà
        if (EtablissementSante.findByEmail(dto.getEmailContact()) != null) {
            throw new BadRequestException("Un établissement avec cet email existe déjà");
        }

        try {
            // Vérifier si le responsable référent existe
            AdministrateurEntity responsable = entityManager.find(AdministrateurEntity.class, dto.getResponsableReferentId());
            if (responsable == null) {
                throw new NotFoundException("Le responsable référent n'existe pas");
            }

            // Convertir DTO en entité
            EtablissementSante etablissement = mapper.toEntity(dto);
            etablissement.setResponsableReferent(responsable);

            // Persister l'entité
            entityManager.persist(etablissement);
            entityManager.flush();

            // Envoyer l'email de confirmation
            emailService.sendNewEtablissementConfirmation(
                dto.getEmailContact(),
                dto.getNom(),
                responsable.getNom(),
                responsable.getPrenom()
            );

            log.info("Établissement créé avec succès : {}", dto.getEmailContact());

            return mapper.toDto(etablissement);

        } catch (PersistenceException e) {
            log.error("Erreur lors de la création de l'établissement", e);
            throw new BadRequestException("Erreur lors de la création de l'établissement : " + e.getMessage());
        }
    }

    @Transactional
    public UtilisateurEtablissementDto creerUtilisateur(UUID etablissementId, UtilisateurEtablissementDto dto) {
        log.debug("Création d'un utilisateur pour l'établissement {} : {}", etablissementId, dto.getEmail());

        // Vérifier si l'email existe déjà
        if (UtilisateurEtablissement.findByEmail(dto.getEmail()) != null) {
            throw new BadRequestException("Un utilisateur avec cet email existe déjà");
        }

        try {
            // Vérifier si l'établissement existe
            EtablissementSante etablissement = entityManager.find(EtablissementSante.class, etablissementId);
            if (etablissement == null) {
                throw new NotFoundException("L'établissement n'existe pas");
            }

            // Sauvegarder le mot de passe en clair pour l'email
            String motDePasseClair = dto.getMotDePasse();

            // Hasher le mot de passe
            String hashedPassword = authenService.hasherMotDePasse(motDePasseClair);
            dto.setMotDePasse(hashedPassword);

            // Convertir DTO en entité
            UtilisateurEtablissement utilisateur = mapper.toEntity(dto);
            utilisateur.setEtablissement(etablissement);

            // Persister l'entité
            entityManager.persist(utilisateur);
            entityManager.flush();

            // Envoyer l'email avec les identifiants
            emailService.sendNewUserCredentials(
                dto.getEmail(),
                dto.getNom(),
                dto.getPrenom(),
                dto.getRole().toString(),
                motDePasseClair,
                etablissement.getNom()
            );

            log.info("Utilisateur créé avec succès : {}", dto.getEmail());

            return mapper.toDto(utilisateur);

        } catch (PersistenceException e) {
            log.error("Erreur lors de la création de l'utilisateur", e);
            throw new BadRequestException("Erreur lors de la création de l'utilisateur : " + e.getMessage());
        }
    }

    @Transactional
    public void activerEtablissement(UUID id) {
        log.debug("Activation de l'établissement : {}", id);

        EtablissementSante etablissement = entityManager.find(EtablissementSante.class, id);
        if (etablissement == null) {
            throw new NotFoundException("L'établissement n'existe pas");
        }

        etablissement.setActive(true);
        entityManager.merge(etablissement);

        // Envoyer l'email de confirmation d'activation
        emailService.sendEtablissementActivationConfirmation(
            etablissement.getEmailContact(),
            etablissement.getNom()
        );

        log.info("Établissement activé avec succès : {}", id);
    }

    @Transactional
    public void desactiverEtablissement(UUID id) {
        log.debug("Désactivation de l'établissement : {}", id);

        EtablissementSante etablissement = entityManager.find(EtablissementSante.class, id);
        if (etablissement == null) {
            throw new NotFoundException("L'établissement n'existe pas");
        }

        etablissement.setActive(false);
        entityManager.merge(etablissement);

        log.info("Établissement désactivé avec succès : {}", id);
    }

    @Transactional
    public EtablissementSanteDto mettreAJourEtablissement(UUID id, EtablissementSanteDto dto) {
        log.debug("Mise à jour de l'établissement : {}", id);

        EtablissementSante etablissement = entityManager.find(EtablissementSante.class, id);
        if (etablissement == null) {
            throw new NotFoundException("L'établissement n'existe pas");
        }

        // Vérifier si le nouvel email est déjà utilisé par un autre établissement
        if (!etablissement.getEmailContact().equals(dto.getEmailContact())) {
            EtablissementSante existant = EtablissementSante.findByEmail(dto.getEmailContact());
            if (existant != null && !existant.getId().equals(id)) {
                throw new BadRequestException("Un établissement avec cet email existe déjà");
            }
        }

        try {
            // Mettre à jour l'entité
            mapper.updateEntity(etablissement, dto);
            entityManager.merge(etablissement);
            entityManager.flush();

            log.info("Établissement mis à jour avec succès : {}", id);

            return mapper.toDto(etablissement);

        } catch (PersistenceException e) {
            log.error("Erreur lors de la mise à jour de l'établissement", e);
            throw new BadRequestException("Erreur lors de la mise à jour de l'établissement : " + e.getMessage());
        }
    }

    public List<EtablissementSanteDto> rechercherEtablissements(String query) {
        log.debug("Recherche d'établissements avec le critère : {}", query);

        String searchQuery = "%" + query.toLowerCase() + "%";
        List<EtablissementSante> etablissements = EtablissementSante.list(
            "LOWER(nom) LIKE ?1 OR LOWER(emailContact) LIKE ?1 OR LOWER(telephoneContact) LIKE ?1",
            searchQuery
        );

        return etablissements.stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }

    public EtablissementSanteDto getEtablissement(UUID id) {
        log.debug("Récupération de l'établissement : {}", id);

        EtablissementSante etablissement = entityManager.find(EtablissementSante.class, id);
        if (etablissement == null) {
            throw new NotFoundException("L'établissement n'existe pas");
        }

        return mapper.toDto(etablissement);
    }

    public List<UtilisateurEtablissementDto> getUtilisateurs(UUID etablissementId) {
        log.debug("Récupération des utilisateurs de l'établissement : {}", etablissementId);

        List<UtilisateurEtablissement> utilisateurs = UtilisateurEtablissement.list(
            "etablissement.id", etablissementId
        );

        return utilisateurs.stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public void supprimerUtilisateur(UUID etablissementId, UUID utilisateurId) {
        log.debug("Suppression de l'utilisateur {} de l'établissement {}", utilisateurId, etablissementId);

        UtilisateurEtablissement utilisateur = entityManager.find(UtilisateurEtablissement.class, utilisateurId);
        if (utilisateur == null || !utilisateur.getEtablissement().getId().equals(etablissementId)) {
            throw new NotFoundException("L'utilisateur n'existe pas dans cet établissement");
        }

        entityManager.remove(utilisateur);
        log.info("Utilisateur supprimé avec succès : {}", utilisateurId);
    }
} 