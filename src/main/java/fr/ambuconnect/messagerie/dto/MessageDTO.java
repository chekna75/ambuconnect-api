package fr.ambuconnect.messagerie.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import fr.ambuconnect.messagerie.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private UUID senderId;
    private UUID receiverId;
    private String content;
    private LocalDateTime timestamp;
    private Boolean isRead;
    private String senderType;  // admin ou chauffeur
    private String receiverType; // admin ou chauffeur

    // Getters et setters
}

