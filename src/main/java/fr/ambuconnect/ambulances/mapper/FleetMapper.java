package fr.ambuconnect.ambulances.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import fr.ambuconnect.ambulances.dto.CreateEquipmentDTO;
import fr.ambuconnect.ambulances.dto.CreateFuelConsumptionDTO;
import fr.ambuconnect.ambulances.dto.CreateMaintenanceDTO;
import fr.ambuconnect.ambulances.dto.EquipmentDTO;
import fr.ambuconnect.ambulances.dto.FuelConsumptionDTO;
import fr.ambuconnect.ambulances.dto.MaintenanceDTO;
import fr.ambuconnect.ambulances.dto.VehicleDTO;
import fr.ambuconnect.ambulances.entity.EquipmentEntity;
import fr.ambuconnect.ambulances.entity.FuelConsumptionEntity;
import fr.ambuconnect.ambulances.entity.MaintenanceEntity;
import fr.ambuconnect.ambulances.entity.VehicleEntity;

@Mapper(componentModel = "cdi")
public interface FleetMapper {
    VehicleDTO toDto(VehicleEntity entity);
    MaintenanceDTO toDto(MaintenanceEntity entity);
    EquipmentDTO toDto(EquipmentEntity entity);
    FuelConsumptionDTO toDto(FuelConsumptionEntity entity);

    @Mapping(target = "id", ignore = true)
    MaintenanceEntity toEntity(MaintenanceDTO dto);

    @Mapping(target = "id", ignore = true)
    EquipmentEntity toEntity(EquipmentDTO dto);

    @Mapping(target = "id", ignore = true)
    FuelConsumptionEntity toEntity(CreateFuelConsumptionDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ambulance.id", source = "ambulanceId")
    VehicleEntity toEntity(VehicleDTO dto);

    List<MaintenanceDTO> toMaintenanceDtoList(List<MaintenanceEntity> entities);
    List<EquipmentDTO> toEquipmentDtoList(List<EquipmentEntity> entities);
    List<FuelConsumptionDTO> toFuelConsumptionDtoList(List<FuelConsumptionEntity> entities);
}

