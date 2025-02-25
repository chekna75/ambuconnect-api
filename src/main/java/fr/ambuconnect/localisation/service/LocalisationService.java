package fr.ambuconnect.localisation.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import fr.ambuconnect.localisation.dto.LocalisationDto;
import fr.ambuconnect.localisation.entity.LocalisationEntity;
import fr.ambuconnect.localisation.mapper.LocalisationMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.websocket.Session;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class LocalisationService {

    private final LocalisationMapper localisationMapper;
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, LocalisationDto> dernieresLocalisations = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    // Fréquence de mise à jour en secondes (par défaut : 10 secondes)
    private static final int FREQUENCE_MAJ = 10;

    @Inject
    public LocalisationService(LocalisationMapper localisationMapper){
        this.localisationMapper = localisationMapper;
    }

    @Transactional
    public LocalisationDto createLocalisation(LocalisationDto dto) {
        LocalisationEntity entity = localisationMapper.toEntity(dto);
        entity.persist();;
        return localisationMapper.toDTO(entity);
    }

    public LocalisationDto getLocalisationById(UUID id) {
        LocalisationEntity entity = LocalisationEntity.findById(id);
        return localisationMapper.toDTO(entity);
    }

    public List<LocalisationDto> getLocalisationsByChauffeurId(UUID chauffeurId) {
        List<LocalisationEntity> entities = LocalisationEntity.findByChauffeurId(chauffeurId);
        return entities.stream()
                .map(localisationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateLocalisation(LocalisationDto localisation) {
        LocalisationEntity entity = LocalisationEntity.findById(localisation.getId());
        if (entity == null) {
            throw new NotFoundException("Localisation with id " + localisation.getId() + " not found");
        }
        entity.setLatitude(localisation.getLatitude());
        entity.setLongitude(localisation.getLongitude());
        entity.setDateHeure(LocalDateTime.now());
        entity.persist();
    }
    

    public void addSession(UUID chauffeurId, Session session) {
        sessions.put(chauffeurId, session);
        // Démarrer la mise à jour périodique pour ce chauffeur
        demarrerMiseAJourPeriodique(chauffeurId);
    }

    public void removeSession(UUID chauffeurId) {
        sessions.remove(chauffeurId);
        dernieresLocalisations.remove(chauffeurId);
    }

    public void sendLocalisationUpdate(UUID chauffeurId, LocalisationDto dto) {
        Session session = sessions.get(chauffeurId);
        if (session != null && session.isOpen()) {
            try {
                String message = localisationMapper.toEntity(dto).toString();
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                // Gérer l'exception ici
                e.printStackTrace(); // Ajout d'un traitement d'exception simple
            }
        }
    }

    @Transactional
    public List<LocalisationDto> getLocalisationAllChauffeur(UUID entrepriseId) {
        List<LocalisationEntity> entities = LocalisationEntity.findByEntrepriseId(entrepriseId);
        return entities.stream()
                .map(localisationMapper::toDTO)
                .collect(Collectors.toList());
    }

    private void demarrerMiseAJourPeriodique(UUID chauffeurId) {
        scheduler.scheduleAtFixedRate(() -> {
            LocalisationDto derniereLoc = dernieresLocalisations.get(chauffeurId);
            if (derniereLoc != null) {
                sendLocalisationUpdate(chauffeurId, derniereLoc);
            }
        }, 0, FREQUENCE_MAJ, TimeUnit.SECONDS);
    }

    // Méthode pour changer la fréquence de mise à jour pour un chauffeur spécifique
    public void setFrequenceMiseAJour(UUID chauffeurId, int frequenceEnSecondes) {
        // Arrêter l'ancienne tâche
        scheduler.shutdown();
        
        // Créer une nouvelle tâche avec la nouvelle fréquence
        scheduler.scheduleAtFixedRate(() -> {
            LocalisationDto derniereLoc = dernieresLocalisations.get(chauffeurId);
            if (derniereLoc != null) {
                sendLocalisationUpdate(chauffeurId, derniereLoc);
            }
        }, 0, frequenceEnSecondes, TimeUnit.SECONDS);
    }

    // Méthode pour obtenir la dernière localisation connue d'un chauffeur
    public LocalisationDto getDerniereLocalisation(UUID chauffeurId) {
        return dernieresLocalisations.get(chauffeurId);
    }
}
