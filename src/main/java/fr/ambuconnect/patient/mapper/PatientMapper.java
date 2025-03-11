package fr.ambuconnect.patient.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import fr.ambuconnect.patient.dto.PatientDto;
import fr.ambuconnect.patient.entity.PatientEntity;

@Mapper(componentModel = "cdi")
public interface PatientMapper {

    // Conversion de PatientEntity vers PatientDto
    @Mapping(target = "entrepriseId", source = "entreprise.id")
    PatientDto toDto(PatientEntity patientEntity);

    // Conversion de PatientDto vers PatientEntity
    @Mapping(target = "entreprise", source = "entrepriseId", qualifiedByName = "idToEntreprise")
    PatientEntity toEntity(PatientDto patientDto);

    @Named("idToEntreprise")
    default EntrepriseEntity idToEntreprise(UUID id) {
        if (id == null) {
            return null;
        }
        EntrepriseEntity entreprise = new EntrepriseEntity();
        entreprise.setId(id);
        return entreprise;
    }

}
