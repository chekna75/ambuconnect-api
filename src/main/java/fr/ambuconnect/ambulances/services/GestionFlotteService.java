package fr.ambuconnect.ambulances.services;

import fr.ambuconnect.ambulances.entity.*;
import fr.ambuconnect.ambulances.mapper.FleetMapper;
import fr.ambuconnect.ambulances.dto.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class GestionFlotteService {

    @Inject
    FleetMapper fleetMapper;

    @Transactional
    public VehicleDTO addVehicle(VehicleDTO vehicleDTO) {
        // Vérifier si l'ambulance existe
        if (vehicleDTO.getAmbulanceId() != null) {
            AmbulanceEntity ambulance = AmbulanceEntity.findById(vehicleDTO.getAmbulanceId());
            if (ambulance == null) {
                throw new IllegalArgumentException("L'ambulance spécifiée n'existe pas");
            }
        }

        VehicleEntity vehicle = fleetMapper.toEntity(vehicleDTO);
        vehicle.persist();
        return fleetMapper.toDto(vehicle);
    }

    @Transactional
    public VehicleDTO getVehicleById(UUID id) {
        VehicleEntity vehicle = VehicleEntity.findById(id);
        if (vehicle == null) {
            throw new NotFoundException("Véhicule non trouvé");
        }
        return fleetMapper.toDto(vehicle);
    }

    @Transactional
    public List<VehicleDTO> getAllVehicles() {
        return VehicleEntity.<VehicleEntity>listAll().stream()
                .map(fleetMapper::toDto)
                .toList();
    }

    @Transactional
    public MaintenanceDTO addMaintenance(UUID vehicleId, MaintenanceDTO maintenanceDTO) {
        VehicleEntity vehicle = VehicleEntity.findById(vehicleId);
        if (vehicle == null) {
            throw new NotFoundException("Véhicule non trouvé");
        }

        MaintenanceEntity maintenance = fleetMapper.toEntity(maintenanceDTO);
        maintenance.setVehicle(vehicle);
        maintenance.persist();
        
        return fleetMapper.toDto(maintenance);
    }

    @Transactional
    public EquipmentDTO addEquipment(UUID vehicleId, EquipmentDTO equipmentDTO) {
        VehicleEntity vehicle = VehicleEntity.findById(vehicleId);
        if (vehicle == null) {
            throw new NotFoundException("Véhicule non trouvé");
        }

        AmbulanceEntity ambulance = vehicle.getAmbulance();
        if (ambulance == null) {
            throw new NotFoundException("Aucune ambulance associée à ce véhicule");
        }

        EquipmentEntity equipment = fleetMapper.toEntity(equipmentDTO);
        equipment.setAmbulance(ambulance);
        equipment.persist();
        
        return fleetMapper.toDto(equipment);
    }

    @Transactional
    public FuelConsumptionDTO addFuelConsumption(UUID vehicleId, CreateFuelConsumptionDTO fuelDTO) {
        VehicleEntity vehicle = VehicleEntity.findById(vehicleId);
        if (vehicle == null) {
            throw new NotFoundException("Véhicule non trouvé");
        }

        FuelConsumptionEntity fuel = fleetMapper.toEntity(fuelDTO);
        fuel.setVehicle(vehicle);
        fuel.persist();
        
        return fleetMapper.toDto(fuel);
    }

    @Transactional
    public List<MaintenanceDTO> getVehicleMaintenances(UUID vehicleId) {
        VehicleEntity vehicle = VehicleEntity.findById(vehicleId);
        if (vehicle == null) {
            throw new NotFoundException("Véhicule non trouvé");
        }
        return fleetMapper.toMaintenanceDtoList(vehicle.getMaintenances());
    }

    @Transactional
    public List<EquipmentDTO> getVehicleEquipments(UUID vehicleId) {
        VehicleEntity vehicle = VehicleEntity.findById(vehicleId);
        if (vehicle == null) {
            throw new NotFoundException("Véhicule non trouvé");
        }

        AmbulanceEntity ambulance = vehicle.getAmbulance();
        if (ambulance == null) {
            throw new NotFoundException("Aucune ambulance associée à ce véhicule");
        }

        return fleetMapper.toEquipmentDtoList(ambulance.getEquipements());
    }

    @Transactional
    public List<FuelConsumptionDTO> getVehicleFuelConsumptions(UUID vehicleId) {
        VehicleEntity vehicle = VehicleEntity.findById(vehicleId);
        if (vehicle == null) {
            throw new NotFoundException("Véhicule non trouvé");
        }
        return fleetMapper.toFuelConsumptionDtoList(vehicle.getFuelConsumptions());
    }

    @Transactional
    public VehicleDTO associerVehiculeAmbulance(UUID vehiculeId, UUID ambulanceId) {
        VehicleEntity vehicle = VehicleEntity.findById(vehiculeId);
        if (vehicle == null) {
            throw new NotFoundException("Véhicule non trouvé");
        }

        AmbulanceEntity ambulance = AmbulanceEntity.findById(ambulanceId);
        if (ambulance == null) {
            throw new NotFoundException("Ambulance non trouvée");
        }

        vehicle.setAmbulance(ambulance);
        ambulance.getVehicules().add(vehicle);
        
        return fleetMapper.toDto(vehicle);
    }

    @Transactional
    public List<VehicleDTO> getVehiculesByAmbulance(UUID ambulanceId) {
        AmbulanceEntity ambulance = AmbulanceEntity.findById(ambulanceId);
        if (ambulance == null) {
            throw new NotFoundException("Ambulance non trouvée");
        }
        
        return ambulance.getVehicules().stream()
            .map(fleetMapper::toDto)
            .toList();
    }

    @Transactional
    public VehicleDTO updateVehicle(UUID id, VehicleDTO vehicleDTO) {
        VehicleEntity vehicle = VehicleEntity.findById(id);
        if (vehicle == null) {
            throw new NotFoundException("Véhicule non trouvé");
        }

        vehicle.setImmatriculation(vehicleDTO.getImmatriculation());
        vehicle.setMarque(vehicleDTO.getMarque());
        vehicle.setStatut(vehicleDTO.getStatut());
        vehicle.setDateMiseEnService(vehicleDTO.getDateMiseEnService());
        vehicle.setModel(vehicleDTO.getModel());
        
        vehicle.persist();
        return fleetMapper.toDto(vehicle);
    }

    @Transactional
    public EquipmentDTO updateEquipment(UUID id, EquipmentDTO equipmentDTO) {
        EquipmentEntity equipment = EquipmentEntity.findById(id);
        if (equipment == null) {
            throw new NotFoundException("Équipement non trouvé");
        }

        equipment.setNom(equipmentDTO.getNom());
        equipment.setType(equipmentDTO.getType());
        equipment.setDateCreation(equipmentDTO.getDateCreation());
        equipment.setDateModification(equipmentDTO.getDateModification());
        equipment.setModifiePar(equipmentDTO.getModifiePar());
        
        equipment.persist();
        return fleetMapper.toDto(equipment);
    }

    @Transactional
    public void deleteEquipment(UUID id) {
        EquipmentEntity equipment = EquipmentEntity.findById(id);
        if (equipment == null) {
            throw new NotFoundException("Équipement non trouvé");
        }   
        equipment.delete();
    }

    @Transactional
    public VehicleDTO updateVehicleState(UUID id, VehicleDTO stateDTO) {
        VehicleEntity vehicle = VehicleEntity.findById(id);
        if (vehicle == null) {
            throw new NotFoundException("Véhicule non trouvé");
        }

        vehicle.setKilometrage(stateDTO.getKilometrage());
        vehicle.setNiveauCarburant(stateDTO.getNiveauCarburant());
        vehicle.setConditionExterieureNote(stateDTO.getConditionExterieureNote());
        vehicle.setConditionExterieureDetails(stateDTO.getConditionExterieureDetails());
        vehicle.setConditionInterieureNote(stateDTO.getConditionInterieureNote());
        vehicle.setConditionInterieureDetails(stateDTO.getConditionInterieureDetails());
        vehicle.setInventaire(stateDTO.getInventaire());
        
        vehicle.persist();
        return fleetMapper.toDto(vehicle);
    }

    @Transactional
    public List<VehicleDTO> getVehiclesInMaintenanceByAmbulance(UUID ambulanceId) {
        AmbulanceEntity ambulance = AmbulanceEntity.findById(ambulanceId);
        if (ambulance == null) {
            throw new NotFoundException("Ambulance non trouvée");
        }
        
        return ambulance.getVehicules().stream()
            .filter(vehicle -> !vehicle.getMaintenances().isEmpty())
            .map(fleetMapper::toDto)
            .toList();
    }
}

