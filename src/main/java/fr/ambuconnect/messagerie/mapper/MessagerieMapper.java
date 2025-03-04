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
        
        // On assigne directement l'enum sans passer par .name()
        dto.setSenderType(message.getSenderType());
        dto.setReceiverType(message.getReceiverType());
        
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
    
        if (dto.getSenderType() == null) {
            throw new IllegalArgumentException("Sender type is null");
        }
        entity.setSenderType(dto.getSenderType());
        
        if (dto.getReceiverType() == null) {
            throw new IllegalArgumentException("Receiver type is null");
        }
        entity.setReceiverType(dto.getReceiverType());
        
        return entity;
    }
    
}