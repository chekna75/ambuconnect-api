package fr.ambuconnect.etablissement.service;

import java.util.UUID;

import fr.ambuconnect.common.exceptions.BadRequestException;
import fr.ambuconnect.common.exceptions.NotFoundException;
import fr.ambuconnect.etablissement.dto.ConfigurationEtablissementDto;
import fr.ambuconnect.etablissement.entity.ConfigurationEtablissement;
import fr.ambuconnect.etablissement.entity.EtablissementSante;
import fr.ambuconnect.etablissement.mapper.EtablissementMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class ConfigurationEtablissementService {

    @Inject
    EntityManager entityManager;

    @Inject
    EtablissementMapper mapper;

    @Transactional
    public ConfigurationEtablissementDto creerConfiguration(UUID etablissementId, ConfigurationEtablissementDto dto) {
        log.debug("Création de la configuration pour l'établissement : {}", etablissementId);

        try {
            // Vérifier si l'établissement existe
            EtablissementSante etablissement = entityManager.find(EtablissementSante.class, etablissementId);
            if (etablissement == null) {
                throw new NotFoundException("L'établissement n'existe pas");
            }

            // Vérifier si une configuration existe déjà
            if (ConfigurationEtablissement.count("etablissement.id", etablissementId) > 0) {
                throw new BadRequestException("Une configuration existe déjà pour cet établissement");
            }

            // Convertir DTO en entité
            ConfigurationEtablissement configuration = mapper.toEntity(dto);
            configuration.setEtablissement(etablissement);

            // Persister l'entité
            entityManager.persist(configuration);
            entityManager.flush();

            log.info("Configuration créée avec succès pour l'établissement : {}", etablissementId);

            return mapper.toDto(configuration);

        } catch (PersistenceException e) {
            log.error("Erreur lors de la création de la configuration", e);
            throw new BadRequestException("Erreur lors de la création de la configuration : " + e.getMessage());
        }
    }

    @Transactional
    public ConfigurationEtablissementDto mettreAJourConfiguration(UUID etablissementId, ConfigurationEtablissementDto dto) {
        log.debug("Mise à jour de la configuration pour l'établissement : {}", etablissementId);

        try {
            // Vérifier si l'établissement existe
            EtablissementSante etablissement = entityManager.find(EtablissementSante.class, etablissementId);
            if (etablissement == null) {
                throw new NotFoundException("L'établissement n'existe pas");
            }

            // Récupérer la configuration existante
            ConfigurationEtablissement configuration = ConfigurationEtablissement.find("etablissement.id", etablissementId)
                .firstResult();
            if (configuration == null) {
                throw new NotFoundException("La configuration n'existe pas");
            }

            // Mettre à jour l'entité
            mapper.updateEntity(configuration, dto);
            entityManager.merge(configuration);
            entityManager.flush();

            log.info("Configuration mise à jour avec succès pour l'établissement : {}", etablissementId);

            return mapper.toDto(configuration);

        } catch (PersistenceException e) {
            log.error("Erreur lors de la mise à jour de la configuration", e);
            throw new BadRequestException("Erreur lors de la mise à jour de la configuration : " + e.getMessage());
        }
    }

    public ConfigurationEtablissementDto getConfiguration(UUID etablissementId) {
        log.debug("Récupération de la configuration pour l'établissement : {}", etablissementId);

        ConfigurationEtablissement configuration = ConfigurationEtablissement.find("etablissement.id", etablissementId)
            .firstResult();
        if (configuration == null) {
            throw new NotFoundException("La configuration n'existe pas");
        }

        return mapper.toDto(configuration);
    }
} 