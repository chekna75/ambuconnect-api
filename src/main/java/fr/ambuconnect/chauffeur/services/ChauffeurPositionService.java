package fr.ambuconnect.chauffeur.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import fr.ambuconnect.chauffeur.dto.ChauffeurPositionDTO;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.chauffeur.entity.ChauffeurPositionEntity;
import fr.ambuconnect.chauffeur.mapper.ChauffeurPositionMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class ChauffeurPositionService {

    @Inject
    ChauffeurPositionMapper mapper;

    @Transactional
    public ChauffeurPositionDTO enregistrerPosition(ChauffeurPositionDTO positionDTO) {
        ChauffeurEntity chauffeur = ChauffeurEntity.findById(positionDTO.getChauffeurId());
        if (chauffeur == null) {
            throw new NotFoundException("Chauffeur non trouvé");
        }

        ChauffeurPositionEntity position = mapper.toEntity(positionDTO);
        position.setChauffeur(chauffeur);
        position.setTimestamp(LocalDateTime.now());
        position.persist();

        return mapper.toDTO(position);
    }

    public ChauffeurPositionDTO getDernierePosition(UUID chauffeurId) {
        ChauffeurPositionEntity position = ChauffeurPositionEntity.findLatestByChauffeurId(chauffeurId);
        if (position == null) {
            throw new NotFoundException("Aucune position trouvée pour ce chauffeur");
        }
        return mapper.toDTO(position);
    }

    public List<ChauffeurPositionDTO> getHistoriquePositions(UUID chauffeurId, LocalDateTime debut, LocalDateTime fin) {
        List<ChauffeurPositionEntity> positions = ChauffeurPositionEntity.findByChauffeurIdAndTimeRange(chauffeurId, debut, fin);
        return positions.stream()
                .map(mapper::toDTO)
                .toList();
    }

    public List<ChauffeurPositionDTO> getChauffeurProches(Double latitude, Double longitude, Double rayonKm) {
        LocalDateTime depuisQuand = LocalDateTime.now().minusMinutes(5); // On ne considère que les positions récentes
        List<ChauffeurPositionEntity> positions = ChauffeurPositionEntity.findNearbyDrivers(latitude, longitude, rayonKm, depuisQuand);
        return positions.stream()
                .map(mapper::toDTO)
                .toList();
    }
} 