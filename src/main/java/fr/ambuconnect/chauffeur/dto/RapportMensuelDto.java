package fr.ambuconnect.chauffeur.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class RapportMensuelDto {
    private UUID chauffeurId;
    private String nomChauffeur;
    private LocalDateTime mois;
    private Double totalHeuresTravaillees;
    private Integer totalCourses;
    private Double tauxRetard;
    private Double moyenneFeedback;
    private String evaluation;
    private String recommandations;
    private String emailSociete;
} 