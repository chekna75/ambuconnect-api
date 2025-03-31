package fr.ambuconnect.ambulances.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.MappingTarget;

import fr.ambuconnect.ambulances.dto.AmbulanceDTO;
import fr.ambuconnect.ambulances.entity.AmbulanceEntity;
import fr.ambuconnect.ambulances.entity.VehicleEntity;
import fr.ambuconnect.ambulances.dto.VehicleDTO;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;

import java.util.UUID;
import java.util.List;

@Mapper(componentModel = "cdi")
public interface AmbulancesMapper {

    @Mapping(target = "entrepriseId", source = "entreprise.id")
    @Mapping(target = "kilometrage", source = "vehicules", qualifiedByName = "getKilometrage")
    @Mapping(target = "niveauCarburant", source = "vehicules", qualifiedByName = "getNiveauCarburant")
    @Mapping(target = "conditionExterieureNote", source = "vehicules", qualifiedByName = "getConditionExterieureNote")
    @Mapping(target = "conditionExterieureDetails", source = "vehicules", qualifiedByName = "getConditionExterieureDetails")
    @Mapping(target = "conditionInterieureNote", source = "vehicules", qualifiedByName = "getConditionInterieureNote")
    @Mapping(target = "conditionInterieureDetails", source = "vehicules", qualifiedByName = "getConditionInterieureDetails")
    @Mapping(target = "inventaire", source = "vehicules", qualifiedByName = "getInventaire")
    AmbulanceDTO toDto(AmbulanceEntity entity);

    @Mapping(target = "entreprise", source = "entrepriseId", qualifiedByName = "idToEntreprise")
    AmbulanceEntity toEntity(AmbulanceDTO dto);

    @Named("idToEntreprise")
    default EntrepriseEntity idToEntreprise(UUID id) {
        if (id == null) {
            return null;
        }
        EntrepriseEntity entreprise = new EntrepriseEntity();
        entreprise.setId(id);
        return entreprise;
    }

    @Named("getKilometrage")
    default Integer getKilometrage(List<VehicleEntity> vehicules) {
        return vehicules != null && !vehicules.isEmpty() ? vehicules.get(0).getKilometrage() : null;
    }

    @Named("getNiveauCarburant")
    default Integer getNiveauCarburant(List<VehicleEntity> vehicules) {
        return vehicules != null && !vehicules.isEmpty() ? vehicules.get(0).getNiveauCarburant() : null;
    }

    @Named("getConditionExterieureNote")
    default Integer getConditionExterieureNote(List<VehicleEntity> vehicules) {
        return vehicules != null && !vehicules.isEmpty() ? vehicules.get(0).getConditionExterieureNote() : null;
    }

    @Named("getConditionExterieureDetails")
    default String getConditionExterieureDetails(List<VehicleEntity> vehicules) {
        return vehicules != null && !vehicules.isEmpty() ? vehicules.get(0).getConditionExterieureDetails() : null;
    }

    @Named("getConditionInterieureNote")
    default Integer getConditionInterieureNote(List<VehicleEntity> vehicules) {
        return vehicules != null && !vehicules.isEmpty() ? vehicules.get(0).getConditionInterieureNote() : null;
    }

    @Named("getConditionInterieureDetails")
    default String getConditionInterieureDetails(List<VehicleEntity> vehicules) {
        return vehicules != null && !vehicules.isEmpty() ? vehicules.get(0).getConditionInterieureDetails() : null;
    }

    @Named("getInventaire")
    default String getInventaire(List<VehicleEntity> vehicules) {
        return vehicules != null && !vehicules.isEmpty() ? vehicules.get(0).getInventaire() : null;
    }

    @Mapping(target = "kilometrage", source = "kilometrage")
    @Mapping(target = "niveauCarburant", source = "niveauCarburant")
    @Mapping(target = "conditionExterieureNote", source = "conditionExterieureNote")
    @Mapping(target = "conditionExterieureDetails", source = "conditionExterieureDetails")
    @Mapping(target = "conditionInterieureNote", source = "conditionInterieureNote")
    @Mapping(target = "conditionInterieureDetails", source = "conditionInterieureDetails")
    @Mapping(target = "inventaire", source = "inventaire")
    void updateFromVehicle(AmbulanceDTO ambulanceDTO, @MappingTarget VehicleEntity vehicle);
}
