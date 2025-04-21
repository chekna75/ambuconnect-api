package fr.ambuconnect.patient.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import fr.ambuconnect.patient.dto.PatientRequestDTO;
import fr.ambuconnect.patient.entity.PatientRequestEntity;
import fr.ambuconnect.patient.entity.enums.PatientRequestStatus;
import fr.ambuconnect.patient.mapper.PatientRequestMapper;
import fr.ambuconnect.patient.websocket.PatientRequestWebSocket;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class PatientRequestService {

    @Inject
    PatientRequestMapper mapper;

    @Inject
    PatientRequestWebSocket webSocket;

    @Transactional
    public PatientRequestDTO createRequest(PatientRequestDTO requestDTO) {
        PatientRequestEntity entity = mapper.toEntity(requestDTO);
        entity.setRequestedTime(LocalDateTime.now());
        entity.setStatus(PatientRequestStatus.PENDING);
        entity.persist();

        PatientRequestDTO createdDTO = mapper.toDTO(entity);
        // Notifier toutes les entreprises connectées
        webSocket.broadcast(createdDTO);
        
        return createdDTO;
    }

    @Transactional
    public PatientRequestDTO assignToEntreprise(UUID requestId, UUID entrepriseId) {
        PatientRequestEntity request = PatientRequestEntity.findById(requestId);
        if (request == null) {
            throw new NotFoundException("Demande non trouvée");
        }

        EntrepriseEntity entreprise = EntrepriseEntity.findById(entrepriseId);
        if (entreprise == null) {
            throw new NotFoundException("Entreprise non trouvée");
        }

        request.setAssignedEntreprise(entreprise);
        request.setStatus(PatientRequestStatus.ASSIGNED);

        PatientRequestDTO updatedDTO = mapper.toDTO(request);
        // Notifier l'entreprise assignée
        webSocket.notifyEntreprise(entrepriseId, updatedDTO);
        
        return updatedDTO;
    }

    public List<PatientRequestDTO> getPendingRequests() {
        return PatientRequestEntity.findPendingRequests()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    public List<PatientRequestDTO> getEntrepriseRequests(UUID entrepriseId) {
        return PatientRequestEntity.findByEntreprise(entrepriseId)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    public PatientRequestDTO updateRequestStatus(UUID requestId, PatientRequestStatus newStatus) {
        PatientRequestEntity request = PatientRequestEntity.findById(requestId);
        if (request == null) {
            throw new NotFoundException("Demande non trouvée");
        }

        request.setStatus(newStatus);
        PatientRequestDTO updatedDTO = mapper.toDTO(request);
        
        // Notifier les parties concernées du changement de statut
        if (request.getAssignedEntreprise() != null) {
            webSocket.notifyEntreprise(request.getAssignedEntreprise().getId(), updatedDTO);
        }
        
        return updatedDTO;
    }
} 