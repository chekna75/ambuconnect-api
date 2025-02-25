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
    public MaintenanceDTO addMaintenance(UUID vehicleId, CreateMaintenanceDTO maintenanceDTO) {
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
    public EquipmentDTO addEquipment(UUID vehicleId, CreateEquipmentDTO equipmentDTO) {
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
        ambulance.setVehicle(vehicle);
        
        return fleetMapper.toDto(vehicle);
    }

    @Transactional
    public VehicleDTO getVehiculeByAmbulance(UUID ambulanceId) {
        AmbulanceEntity ambulance = AmbulanceEntity.findById(ambulanceId);
        if (ambulance == null) {
            throw new NotFoundException("Ambulance non trouvée");
        }
        
        VehicleEntity vehicle = ambulance.getVehicle();
        if (vehicle == null) {
            throw new NotFoundException("Aucun véhicule associé à cette ambulance");
        }
        
        return fleetMapper.toDto(vehicle);
    }
}
