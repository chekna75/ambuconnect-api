package fr.ambuconnect.messagerie.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.ambuconnect.messagerie.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    @JsonProperty("senderId")
    private UUID senderId;

    @JsonProperty("receiverId")
    private UUID receiverId;

    @JsonProperty("content")
    private String content;

    @JsonProperty("senderType")
    private UserType senderType;

    @JsonProperty("receiverType")
    private UserType receiverType;

    @JsonProperty("isRead")
    private Boolean isRead;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
}

