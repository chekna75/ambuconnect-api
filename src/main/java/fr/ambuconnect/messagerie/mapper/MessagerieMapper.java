package fr.ambuconnect.messagerie.mapper;

import jakarta.enterprise.context.ApplicationScoped;

import org.mapstruct.Mapper;

import fr.ambuconnect.messagerie.dto.MessageDTO;
import fr.ambuconnect.messagerie.entity.MessagerieEntity;
import fr.ambuconnect.messagerie.enums.UserType;


@Mapper(componentModel = "cdi")
public class MessagerieMapper {

    public MessageDTO toDTO(MessagerieEntity message) {
        if (message == null) {
            return null;
        }
        
        MessageDTO dto = new MessageDTO();
        dto.setSenderId(message.getSenderId());
        dto.setReceiverId(message.getReceiverId());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());
        dto.setIsRead(message.getIsRead());
        
        return dto;
    }

    public MessagerieEntity toEntity(MessageDTO dto) {
        if (dto == null) {
            return null;
        }
        
        MessagerieEntity entity = new MessagerieEntity();
        entity.setSenderId(dto.getSenderId());
        entity.setReceiverId(dto.getReceiverId());
        entity.setContent(dto.getContent());
        entity.setTimestamp(dto.getTimestamp());
        entity.setIsRead(dto.getIsRead());
     
        return entity;
    }
    
}