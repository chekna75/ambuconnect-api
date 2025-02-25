package fr.ambuconnect.messagerie.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {

    private UUID otherUserId;
    private List<MessagerieDto> messages;
    private String lastMessage;
    private LocalDateTime lastMessageDate;
    private boolean unreadMessages;

    // Constructeurs, getters et setters
} 