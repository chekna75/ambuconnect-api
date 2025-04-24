package fr.ambuconnect.etablissement.dto;

import lombok.Data;

@Data
public class ContactMessageDto {
    private String nom;
    private String email;
    private String message;
    private String sujet;
} 