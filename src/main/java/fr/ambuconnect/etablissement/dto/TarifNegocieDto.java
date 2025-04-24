package fr.ambuconnect.etablissement.dto;

import java.math.BigDecimal;
import java.util.UUID;

import fr.ambuconnect.etablissement.entity.TypeTransport;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarifNegocieDto {

    @NotNull(message = "La société est obligatoire")
    private UUID societeId;

    @NotNull(message = "Le type de transport est obligatoire")
    private TypeTransport typeTransport;

    @NotNull(message = "Le tarif de base est obligatoire")
    @Positive(message = "Le tarif de base doit être positif")
    private BigDecimal tarifBase;

    @NotNull(message = "Le pourcentage de réduction est obligatoire")
    @Min(value = 0, message = "Le pourcentage de réduction doit être entre 0 et 100")
    @Max(value = 100, message = "Le pourcentage de réduction doit être entre 0 et 100")
    private Integer reductionPourcentage;
} 