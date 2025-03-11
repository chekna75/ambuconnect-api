package fr.ambuconnect.notification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private UUID id;
    private String message;
    private String type;  // COURSE_ACCEPTEE, COURSE_TERMINEE, NOUVELLE_COURSE, etc.
    private UUID destinataireId;  // ID du chauffeur ou de l'administrateur
    private UUID courseId;  // ID de la course concernée (si applicable)
    private LocalDateTime dateCreation;
    private boolean lue;
} 