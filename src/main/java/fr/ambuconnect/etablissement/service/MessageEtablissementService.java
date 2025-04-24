package fr.ambuconnect.etablissement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.ambuconnect.common.exceptions.BadRequestException;
import fr.ambuconnect.common.exceptions.NotFoundException;
import fr.ambuconnect.etablissement.dto.MessageEtablissementDto;
import fr.ambuconnect.etablissement.entity.DemandeTransport;
import fr.ambuconnect.etablissement.entity.EtablissementSante;
import fr.ambuconnect.etablissement.entity.MessageEtablissement;
import fr.ambuconnect.etablissement.entity.UtilisateurEtablissement;
import fr.ambuconnect.etablissement.mapper.EtablissementMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class MessageEtablissementService {

    @Inject
    EntityManager entityManager;

    @Inject
    EtablissementMapper mapper;

    @Transactional
    public MessageEtablissementDto creerMessage(UUID etablissementId, UUID utilisateurId, MessageEtablissementDto dto) {
        log.debug("Création d'un message pour l'établissement : {}", etablissementId);

        try {
            // Vérifier si l'établissement existe
            EtablissementSante etablissement = entityManager.find(EtablissementSante.class, etablissementId);
            if (etablissement == null) {
                throw new NotFoundException("L'établissement n'existe pas");
            }

            // Vérifier si l'utilisateur existe et appartient à l'établissement
            UtilisateurEtablissement utilisateur = entityManager.find(UtilisateurEtablissement.class, utilisateurId);
            if (utilisateur == null || !utilisateur.getEtablissement().getId().equals(etablissementId)) {
                throw new NotFoundException("L'utilisateur n'existe pas dans cet établissement");
            }

            // Vérifier si la demande de transport existe si spécifiée
            if (dto.getDemandeTransportId() != null) {
                DemandeTransport demande = entityManager.find(DemandeTransport.class, dto.getDemandeTransportId());
                if (demande == null || !demande.getEtablissement().getId().equals(etablissementId)) {
                    throw new NotFoundException("La demande de transport n'existe pas dans cet établissement");
                }
            }

            // Convertir DTO en entité
            MessageEtablissement message = mapper.toEntity(dto);
            message.setEtablissement(etablissement);
            message.setAuteur(utilisateur);
            message.setDateEnvoi(LocalDateTime.now());

            // Persister l'entité
            entityManager.persist(message);
            entityManager.flush();

            log.info("Message créé avec succès : {}", message.getId());

            return mapper.toDto(message);

        } catch (PersistenceException e) {
            log.error("Erreur lors de la création du message", e);
            throw new BadRequestException("Erreur lors de la création du message : " + e.getMessage());
        }
    }

    public List<MessageEtablissementDto> getMessagesGlobaux(UUID etablissementId) {
        log.debug("Récupération des messages globaux pour l'établissement : {}", etablissementId);

        List<MessageEtablissement> messages = MessageEtablissement.list(
            "etablissement.id = ?1 AND demandeTransport IS NULL ORDER BY dateEnvoi DESC",
            etablissementId
        );

        return messages.stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }

    public List<MessageEtablissementDto> getMessagesDemande(UUID etablissementId, UUID demandeId) {
        log.debug("Récupération des messages pour la demande {} de l'établissement {}", demandeId, etablissementId);

        List<MessageEtablissement> messages = MessageEtablissement.list(
            "etablissement.id = ?1 AND demandeTransport.id = ?2 ORDER BY dateEnvoi DESC",
            etablissementId, demandeId
        );

        return messages.stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }

    public void diffuserMessage(MessageEtablissementDto message, List<Session> sessions) {
        log.debug("Diffusion du message {} aux {} sessions connectées", message.getId(), sessions.size());

        String messageJson = message.toString(); // À remplacer par une vraie sérialisation JSON

        for (Session session : sessions) {
            try {
                session.getAsyncRemote().sendText(messageJson);
            } catch (Exception e) {
                log.error("Erreur lors de l'envoi du message à la session {}", session.getId(), e);
            }
        }
    }
} 