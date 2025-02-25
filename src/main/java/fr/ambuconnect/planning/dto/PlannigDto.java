package fr.ambuconnect.planning.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import fr.ambuconnect.courses.dto.CourseDto;
import fr.ambuconnect.planning.enums.StatutEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class PlannigDto {
    private UUID id;
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private UUID chauffeurId;
    private List<CourseDto> courses;
    @Enumerated(EnumType.STRING)
    private StatutEnum statut;

   
}