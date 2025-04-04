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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.localisation.dto.LocalisationDto;
import fr.ambuconnect.localisation.entity.LocalisationEntity;
import fr.ambuconnect.localisation.mapper.LocalisationMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.websocket.Session;
import jakarta.ws.rs.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class LocalisationService {

    private static final Logger LOG = LoggerFactory.getLogger(LocalisationService.class);

    private final LocalisationMapper localisationMapper;
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, LocalisationDto> dernieresLocalisations = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<UUID, List<Session>> entrepriseSessions = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, List<Session>>> adminChauffeurSessions = new ConcurrentHashMap<>();
    
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
        
        UUID chauffeurId = entity.getChauffeur().getId();
        UUID entrepriseId = entity.getChauffeur().getEntreprise().getId();
        LocalisationDto dto = localisationMapper.toDTO(entity);
        
        // Notifier tous les observateurs de l'entreprise
        notifyEntrepriseObservers(entrepriseId, dto);
        
        // Notifier les observateurs spécifiques à ce chauffeur
        notifyAdminChauffeurObservers(entrepriseId, chauffeurId, dto);
        
        // Mettre à jour le cache des dernières localisations
        dernieresLocalisations.put(chauffeurId, dto);
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

    public void sendLocalisationUpdate(UUID chauffeurId, LocalisationDto localisation) {
        // Mettre à jour le cache des dernières localisations
        dernieresLocalisations.put(chauffeurId, localisation);
        
        // Récupérer l'entreprise du chauffeur (nous devons connaître l'entrepriseId)
        ChauffeurEntity chauffeur = ChauffeurEntity.findById(chauffeurId);
        if (chauffeur == null) {
            return;
        }
        
        UUID entrepriseId = chauffeur.getEntreprise().getId();
        
        // Notifier les observateurs spécifiques à ce chauffeur
        notifyAdminChauffeurObservers(entrepriseId, chauffeurId, localisation);
        
        // Notifier tous les observateurs de l'entreprise
        notifyEntrepriseObservers(entrepriseId, localisation);
    }

    @Transactional
    public void updateChauffeurPosition(UUID chauffeurId, LocalisationDto localisation) {
        // Récupérer le chauffeur d'abord
        ChauffeurEntity chauffeur = ChauffeurEntity.findById(chauffeurId);
        if (chauffeur == null) {
            LOG.warn("Tentative de mise à jour de position pour un chauffeur inexistant: " + chauffeurId);
            return;
        }
        
        // Supprimer les anciennes positions du chauffeur
        deleteOldPositions(chauffeurId);
        
        // Créer la nouvelle entité de localisation
        LocalisationEntity entity = new LocalisationEntity();
        entity.setLatitude(localisation.getLatitude());
        entity.setLongitude(localisation.getLongitude());
        entity.setDateHeure(LocalDateTime.now());
        entity.setChauffeur(chauffeur); // Définir la relation une seule fois
        
        // Persister l'entité
        entity.persist();
        
        // Mettre à jour le cache des dernières localisations
        localisation.setId(entity.getId());
        localisation.setChauffeurId(chauffeurId); // S'assurer que l'ID du chauffeur est correctement défini dans le DTO
        dernieresLocalisations.put(chauffeurId, localisation);
        
        LOG.info("Position mise à jour pour le chauffeur: " + chauffeurId + 
                " (lat: " + localisation.getLatitude() + ", lng: " + localisation.getLongitude() + ")");
    }
    
    /**
     * Supprime toutes les anciennes positions d'un chauffeur
     * @param chauffeurId identifiant du chauffeur
     */
    private void deleteOldPositions(UUID chauffeurId) {
        long deleted = LocalisationEntity.delete("chauffeur.id", chauffeurId);
        if (deleted > 0) {
            LOG.info("Suppression de {} anciennes positions pour le chauffeur: {}", deleted, chauffeurId);
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

    public void addEntrepriseSession(UUID entrepriseId, String role, Session session) {
        entrepriseSessions.computeIfAbsent(entrepriseId, k -> new CopyOnWriteArrayList<>()).add(session);
        
        session.getUserProperties().put("role", role);
    }

    public void removeEntrepriseSession(UUID entrepriseId, Session session) {
        if (entrepriseSessions.containsKey(entrepriseId)) {
            entrepriseSessions.get(entrepriseId).remove(session);
            if (entrepriseSessions.get(entrepriseId).isEmpty()) {
                entrepriseSessions.remove(entrepriseId);
            }
        }
    }

    public void sendAllChauffeursLocalisations(UUID entrepriseId, Session session) {
        try {
            List<LocalisationDto> localisations = getLocalisationAllChauffeur(entrepriseId);
            
            String message = new ObjectMapper().writeValueAsString(localisations);
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void notifyEntrepriseObservers(UUID entrepriseId, LocalisationDto localisation) {
        if (entrepriseSessions.containsKey(entrepriseId)) {
            try {
                String message = new ObjectMapper().writeValueAsString(localisation);
                for (Session session : entrepriseSessions.get(entrepriseId)) {
                    if (session.isOpen()) {
                        session.getAsyncRemote().sendText(message);
                    }
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    public void addAdminChauffeurSession(UUID entrepriseId, UUID chauffeurId, String role, Session session) {
        // Structure : entrepriseId -> chauffeurId -> liste de sessions
        adminChauffeurSessions
            .computeIfAbsent(entrepriseId, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(chauffeurId, k -> new CopyOnWriteArrayList<>())
            .add(session);
        
        // Stocker le rôle dans les propriétés de la session
        session.getUserProperties().put("role", role);
    }

    public void removeAdminChauffeurSession(UUID entrepriseId, UUID chauffeurId, Session session) {
        if (adminChauffeurSessions.containsKey(entrepriseId)) {
            Map<UUID, List<Session>> chauffeurMap = adminChauffeurSessions.get(entrepriseId);
            if (chauffeurMap.containsKey(chauffeurId)) {
                chauffeurMap.get(chauffeurId).remove(session);
                
                // Nettoyer les collections vides
                if (chauffeurMap.get(chauffeurId).isEmpty()) {
                    chauffeurMap.remove(chauffeurId);
                }
                
                if (chauffeurMap.isEmpty()) {
                    adminChauffeurSessions.remove(entrepriseId);
                }
            }
        }
    }

    public void sendChauffeurLocalisation(UUID chauffeurId, Session session) {
        try {
            // Récupérer la dernière localisation connue du chauffeur
            LocalisationDto localisation = getDerniereLocalisation(chauffeurId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("type", "POSITION_UPDATE");
            response.put("timestamp", LocalDateTime.now().toString());
            
            if (localisation != null) {
                response.put("status", "SUCCESS");
                response.put("data", localisation);
            } else {
                response.put("status", "NO_DATA");
                response.put("message", "Pas de données de localisation disponibles pour ce chauffeur");
            }
            
            String message = new ObjectMapper().writeValueAsString(response);
            session.getBasicRemote().sendText(message);
            
        } catch (IOException e) {
            try {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("type", "POSITION_ACK");
                errorResponse.put("status", "ERROR");
                errorResponse.put("message", "Erreur lors de l'envoi des données de localisation");
                errorResponse.put("timestamp", LocalDateTime.now().toString());
                
                String errorMessage = new ObjectMapper().writeValueAsString(errorResponse);
                session.getBasicRemote().sendText(errorMessage);
            } catch (IOException ex) {
                e.printStackTrace();
            }
        }
    }

    private void notifyAdminChauffeurObservers(UUID entrepriseId, UUID chauffeurId, LocalisationDto localisation) {
        if (adminChauffeurSessions.containsKey(entrepriseId) && 
            adminChauffeurSessions.get(entrepriseId).containsKey(chauffeurId)) {
            
            try {
                Map<String, Object> response = new HashMap<>();
                response.put("type", "POSITION_UPDATE");
                response.put("status", "SUCCESS");
                response.put("data", localisation);
                response.put("timestamp", LocalDateTime.now().toString());
                
                String message = new ObjectMapper().writeValueAsString(response);
                
                for (Session session : adminChauffeurSessions.get(entrepriseId).get(chauffeurId)) {
                    if (session.isOpen()) {
                        session.getAsyncRemote().sendText(message);
                    }
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }
}
