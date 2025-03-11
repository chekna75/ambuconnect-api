package fr.ambuconnect.chauffeur.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceChauffeurDto {
    private UUID id;
    private UUID chauffeurId;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private Double heuresTravaillees;
    private Integer nombreCourses;
    private Integer nombreRetards;
    private Double noteMoyenneFeedback;
    private String commentaires;
} 