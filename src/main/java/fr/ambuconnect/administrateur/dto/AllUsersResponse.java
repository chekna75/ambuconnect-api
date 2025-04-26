package fr.ambuconnect.administrateur.dto;

import java.util.List;

import fr.ambuconnect.chauffeur.dto.ChauffeurDto;
import fr.ambuconnect.etablissement.dto.UtilisateurEtablissementDto;
import fr.ambuconnect.patient.dto.PatientDto;

public class AllUsersResponse {
    public List<ChauffeurDto> chauffeurs;
    public List<PatientDto> patients;
    public List<AdministrateurDto> administrateurs;
    public List<AdministrateurDto> regulateurs;
    public List<UtilisateurEtablissementDto> utilisateursEtablissement;

}
