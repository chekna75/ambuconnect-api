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
        dto.setSenderType(message.getSenderType().name());
        dto.setReceiverType(message.getReceiverType().name());
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
        
        // Conversion pour le type de l'expéditeur
        String senderTypeStr = dto.getSenderType();
        if (senderTypeStr == null || senderTypeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Sender type is null or empty");
        }
        try {
            entity.setSenderType(UserType.valueOf(senderTypeStr.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid sender type: " + senderTypeStr, e);
        }
        
        // Conversion pour le type du destinataire
        String receiverTypeStr = dto.getReceiverType();
        if (receiverTypeStr == null || receiverTypeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Receiver type is null or empty");
        }
        try {
            entity.setReceiverType(UserType.valueOf(receiverTypeStr.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid receiver type: " + receiverTypeStr, e);
        }
        
        return entity;
    }
    
}