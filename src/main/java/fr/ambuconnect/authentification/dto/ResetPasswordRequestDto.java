package fr.ambuconnect.authentification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ResetPasswordRequestDto {
    @NotBlank
    private String token;
    
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    private String nouveauMotDePasse;
} 