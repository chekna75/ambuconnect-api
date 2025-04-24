package fr.ambuconnect.etablissement.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TarifNegocie {

    @Column(name = "societe_id", nullable = false)
    private UUID societeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_transport", nullable = false)
    private TypeTransport typeTransport;

    @Column(name = "tarif_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifBase;

    @Column(name = "reduction_pourcentage", nullable = false)
    private Integer reductionPourcentage;
} 