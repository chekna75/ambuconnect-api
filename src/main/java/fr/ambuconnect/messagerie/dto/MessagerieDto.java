package fr.ambuconnect.messagerie.dto;

import java.util.UUID;

import fr.ambuconnect.messagerie.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessagerieDto {

    private UUID id;
    private String contenu;
    private String dateHeure;
    private UUID expediteurId;
    private UserType expediteurType;
    private UUID destinataireId;
    private UserType destinataireType;
    private UUID courseId;
}
