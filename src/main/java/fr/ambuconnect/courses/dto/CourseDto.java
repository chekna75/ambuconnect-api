package fr.ambuconnect.courses.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import fr.ambuconnect.planning.enums.StatutEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {

    private UUID id;
    private LocalDateTime dateHeureDepart;
    private String adresseDepart;
    private String adresseArrivee;
    private BigDecimal distance;
    private UUID chauffeurId;
    private UUID ambulanceId;
    @Enumerated(EnumType.STRING)
    private StatutEnum statut;
    private UUID planningId;
    private LocalDateTime dateHeureArrive;
    private UUID patientId;
    private String informationsSupplementaires;
    private LocalDateTime dateHeureArrivee;
    private UUID entrepriseId;
    private String informationPatient;
    private String informationCourses;
    private Double latitude;
    private Double longitude;
    private Integer tempsTrajetEstime;
    private Integer tempsTrajetReel;
    private BigDecimal distanceEstimee;
    private Double latitudeDepart;
    private Double longitudeDepart;
    private Double latitudeArrivee;
    private Double longitudeArrivee;
}
