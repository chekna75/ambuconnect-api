package fr.ambuconnect.patient.entity.enums;

public enum PatientRequestStatus {
    PENDING,        // En attente d'attribution
    ASSIGNED,       // Attribuée à une entreprise
    ACCEPTED,       // Acceptée par l'entreprise
    IN_PROGRESS,    // En cours de traitement
    COMPLETED,      // Terminée
    CANCELLED       // Annulée
} 