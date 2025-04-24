package fr.ambuconnect.etablissement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.ambuconnect.common.exceptions.BadRequestException;
import fr.ambuconnect.common.exceptions.NotFoundException;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import fr.ambuconnect.etablissement.dto.DemandeTransportDto;
import fr.ambuconnect.etablissement.entity.DemandeTransport;
import fr.ambuconnect.etablissement.entity.EtablissementSante;
import fr.ambuconnect.etablissement.entity.StatusDemande;
import fr.ambuconnect.etablissement.entity.UtilisateurEtablissement;
import fr.ambuconnect.etablissement.mapper.EtablissementMapper;
import fr.ambuconnect.notification.service.EmailService;
import fr.ambuconnect.patient.entity.PatientEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class DemandeTransportService {

    @Inject
    EntityManager entityManager;

    @Inject
    EtablissementMapper mapper;

    @Inject
    EmailService emailService;

    @Transactional
    public DemandeTransportDto creerDemande(UUID etablissementId, UUID utilisateurId, DemandeTransportDto dto) {
        log.debug("Création d'une demande de transport pour l'établissement : {}", etablissementId);

        try {
            // Vérifier si l'établissement existe et est actif
            EtablissementSante etablissement = entityManager.find(EtablissementSante.class, etablissementId);
            if (etablissement == null) {
                throw new NotFoundException("L'établissement n'existe pas");
            }
            if (!etablissement.isActive()) {
                throw new BadRequestException("L'établissement n'est pas actif");
            }

            // Vérifier si l'utilisateur existe et appartient à l'établissement
            UtilisateurEtablissement utilisateur = entityManager.find(UtilisateurEtablissement.class, utilisateurId);
            if (utilisateur == null || !utilisateur.getEtablissement().getId().equals(etablissementId)) {
                throw new NotFoundException("L'utilisateur n'existe pas dans cet établissement");
            }

            // Vérifier si le patient existe
            PatientEntity patient = entityManager.find(PatientEntity.class, dto.getPatientId());
            if (patient == null) {
                throw new NotFoundException("Le patient n'existe pas");
            }

            // Convertir DTO en entité
            DemandeTransport demande = mapper.toEntity(dto);
            demande.setEtablissement(etablissement);
            demande.setCreatedBy(utilisateur);
            demande.setPatient(patient);
            demande.setCreatedAt(LocalDateTime.now());

            // Persister l'entité
            entityManager.persist(demande);
            entityManager.flush();

            log.info("Demande de transport créée avec succès : {}", demande.getId());

            return mapper.toDto(demande);

        } catch (PersistenceException e) {
            log.error("Erreur lors de la création de la demande de transport", e);
            throw new BadRequestException("Erreur lors de la création de la demande de transport : " + e.getMessage());
        }
    }

    @Transactional
    public DemandeTransportDto mettreAJourStatus(UUID demandeId, StatusDemande nouveauStatus) {
        log.debug("Mise à jour du status de la demande {} : {}", demandeId, nouveauStatus);

        DemandeTransport demande = entityManager.find(DemandeTransport.class, demandeId);
        if (demande == null) {
            throw new NotFoundException("La demande de transport n'existe pas");
        }

        try {
            demande.setStatus(nouveauStatus);
            demande.setUpdatedAt(LocalDateTime.now());
            entityManager.merge(demande);
            entityManager.flush();

            log.info("Status de la demande mis à jour avec succès : {}", demandeId);

            return mapper.toDto(demande);

        } catch (PersistenceException e) {
            log.error("Erreur lors de la mise à jour du status de la demande", e);
            throw new BadRequestException("Erreur lors de la mise à jour du status : " + e.getMessage());
        }
    }

    @Transactional
    public DemandeTransportDto affecterSociete(UUID demandeId, UUID societeId) {
        log.debug("Affectation de la société {} à la demande {}", societeId, demandeId);

        DemandeTransport demande = entityManager.find(DemandeTransport.class, demandeId);
        if (demande == null) {
            throw new NotFoundException("La demande de transport n'existe pas");
        }

        EntrepriseEntity societe = entityManager.find(EntrepriseEntity.class, societeId);
        if (societe == null) {
            throw new NotFoundException("La société n'existe pas");
        }

        try {
            demande.setSocieteAffectee(societe);
            demande.setStatus(StatusDemande.ACCEPTEE);
            demande.setUpdatedAt(LocalDateTime.now());
            entityManager.merge(demande);
            entityManager.flush();

            log.info("Société affectée avec succès à la demande : {}", demandeId);

            return mapper.toDto(demande);

        } catch (PersistenceException e) {
            log.error("Erreur lors de l'affectation de la société", e);
            throw new BadRequestException("Erreur lors de l'affectation de la société : " + e.getMessage());
        }
    }

    public List<DemandeTransportDto> getDemandes(UUID etablissementId, StatusDemande status) {
        log.debug("Récupération des demandes pour l'établissement : {}", etablissementId);

        List<DemandeTransport> demandes;
        if (status != null) {
            demandes = DemandeTransport.list("etablissement.id = ?1 AND status = ?2", etablissementId, status);
        } else {
            demandes = DemandeTransport.list("etablissement.id", etablissementId);
        }

        return demandes.stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }

    public DemandeTransportDto getDemande(UUID demandeId) {
        log.debug("Récupération de la demande : {}", demandeId);

        DemandeTransport demande = entityManager.find(DemandeTransport.class, demandeId);
        if (demande == null) {
            throw new NotFoundException("La demande de transport n'existe pas");
        }

        return mapper.toDto(demande);
    }

    public List<DemandeTransportDto> getDemandesParPeriode(UUID etablissementId, LocalDateTime debut, LocalDateTime fin) {
        log.debug("Récupération des demandes pour l'établissement {} entre {} et {}", etablissementId, debut, fin);

        List<DemandeTransport> demandes = DemandeTransport.list(
            "etablissement.id = ?1 AND horaireSouhaite BETWEEN ?2 AND ?3",
            etablissementId, debut, fin
        );

        return demandes.stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }
} 