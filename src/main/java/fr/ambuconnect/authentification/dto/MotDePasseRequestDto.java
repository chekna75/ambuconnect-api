package fr.ambuconnect.authentification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MotDePasseRequestDto {
    @NotBlank(message = "Le nouveau mot de passe est requis")
    private String nouveauMotDePasse;
    
    // Getters et setters
}
