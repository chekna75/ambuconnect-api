package fr.ambuconnect.notification.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import fr.ambuconnect.notification.dto.NotificationDto;
import fr.ambuconnect.notification.entity.NotificationEntity;

@Mapper(componentModel = "cdi")
public interface NotificationMapper {

    @Mapping(source = "course.id", target = "courseId")
    NotificationDto toDto(NotificationEntity entity);

    @Mapping(target = "course", ignore = true)
    NotificationEntity toEntity(NotificationDto dto);
} 