package fr.ambuconnect.utils;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class NotificationMessage {
    private String type;
    private UUID courseId;
    private LocalDateTime timestamp;
}
