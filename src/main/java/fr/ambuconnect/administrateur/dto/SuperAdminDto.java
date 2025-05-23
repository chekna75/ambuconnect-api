package fr.ambuconnect.administrateur.dto;

import java.util.UUID;

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
public class SuperAdminDto {

    private UUID id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    
}
