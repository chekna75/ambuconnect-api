package fr.ambuconnect.messagerie.mapper;

import java.util.UUID;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;

import fr.ambuconnect.messagerie.dto.MessageDTO;
import fr.ambuconnect.messagerie.entity.MessagerieEntity;
import fr.ambuconnect.messagerie.enums.UserType;


@ApplicationScoped
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
        dto.setSenderType(message.getSenderType().name());
        dto.setReceiverType(message.getReceiverType().name());
        return dto;
    }

    public MessagerieEntity toEntity(MessageDTO dto) {
        if (dto == null) {
            return null;
        }
        
        MessagerieEntity message = new MessagerieEntity();
        message.setSenderId(dto.getSenderId());
        message.setReceiverId(dto.getReceiverId());
        message.setContent(dto.getContent());
        message.setTimestamp(dto.getTimestamp());
        message.setIsRead(dto.getIsRead());
        message.setSenderType(UserType.valueOf(dto.getSenderType()));
        message.setReceiverType(UserType.valueOf(dto.getReceiverType()));
        return message;
    }
}