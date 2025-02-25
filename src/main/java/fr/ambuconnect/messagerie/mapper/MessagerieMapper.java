package fr.ambuconnect.messagerie.mapper;

import java.util.UUID;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import fr.ambuconnect.administrateur.entity.AdministrateurEntity;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.courses.entity.CoursesEntity;
import fr.ambuconnect.messagerie.dto.MessagerieDto;
import fr.ambuconnect.messagerie.entity.MessagerieEntity;
import fr.ambuconnect.messagerie.enums.UserType;


@Mapper(componentModel = "cdi", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MessagerieMapper {
    MessagerieMapper INSTANCE = Mappers.getMapper(MessagerieMapper.class);

    @Mapping(target = "expediteurId", expression = "java(getExpediteurId(entity))")
    @Mapping(target = "expediteurType", source = "expediteurType")
    @Mapping(target = "destinataireId", expression = "java(getDestinataireid(entity))")
    @Mapping(target = "destinataireType", source = "destinataireType")
    @Mapping(target = "courseId", source = "course.id")
    MessagerieDto toDTO(MessagerieEntity entity);
    
    @InheritInverseConfiguration
    @Mapping(target = "expediteurAdmin", ignore = true)
    @Mapping(target = "expediteurChauffeur", ignore = true)
    @Mapping(target = "destinataire", source = "destinataireId")
    @Mapping(target = "course", ignore = true)
    MessagerieEntity toEntity(MessagerieDto dto);

    List<MessagerieDto> toDTOList(List<MessagerieEntity> entities);
    List<MessagerieEntity> toEntityList(List<MessagerieDto> dtos);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(MessagerieDto dto, @MappingTarget MessagerieEntity entity);

    // Méthodes utilitaires pour gérer les relations
    default void setRelations(MessagerieEntity entity, UUID expediteurId, UUID destinataireId, UUID courseId, 
                            UserType expediteurType, UserType destinataireType) {
        if (expediteurId != null) {
            if (expediteurType == UserType.administrateur) {
                AdministrateurEntity expediteur = AdministrateurEntity.findById(expediteurId);
                entity.setExpediteur(expediteur);
            } else {
                ChauffeurEntity expediteur = ChauffeurEntity.findById(expediteurId);
                entity.setExpediteur(expediteur);
            }
        }

        if (destinataireId != null) {
            if (destinataireType == UserType.administrateur) {
                AdministrateurEntity destinataire = AdministrateurEntity.findById(destinataireId);
                entity.setDestinataire(destinataire);
            } else {
                ChauffeurEntity destinataire = ChauffeurEntity.findById(destinataireId);
                entity.setDestinataire(destinataire);
            }
        }

        if (courseId != null) {
            CoursesEntity course = CoursesEntity.findById(courseId);
            entity.setCourse(course);
        }
    }
    default UUID getExpediteurId(MessagerieEntity entity) {
        if (entity.getExpediteurType() == UserType.administrateur) {
            return entity.getExpediteurAdmin() != null ? entity.getExpediteurAdmin().getId() : null;
        } else {
            return entity.getExpediteurChauffeur() != null ? entity.getExpediteurChauffeur().getId() : null;
        }
    }
    
    default UUID getDestinataireid(MessagerieEntity entity) {
        if (entity.getDestinataireType() == UserType.administrateur) {
            return entity.getDestinataire() instanceof AdministrateurEntity ? ((AdministrateurEntity) entity.getDestinataire()).getId() : null;
        } else {
            return entity.getDestinataire() instanceof ChauffeurEntity ? ((ChauffeurEntity) entity.getDestinataire()).getId() : null;
        }
    }

    // Méthodes de conversion d'UUID
    default UUID mapEntityToId(AdministrateurEntity entity) {
        return entity != null ? entity.getId() : null;
    }

    default UUID mapEntityToId(ChauffeurEntity entity) {
        return entity != null ? entity.getId() : null;
    }

    default UUID mapEntityToId(CoursesEntity entity) {
        return entity != null ? entity.getId() : null;
    }
}