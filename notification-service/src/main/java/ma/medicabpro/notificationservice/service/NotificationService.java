package ma.medicabpro.notificationservice.service;


import ma.medicabpro.notificationservice.dto.NotificationRequestDTO;
import ma.medicabpro.notificationservice.dto.NotificationResponseDTO;

import java.util.List;

public interface NotificationService {

    NotificationResponseDTO envoyer(
            NotificationRequestDTO dto);

    NotificationResponseDTO programmer(
            NotificationRequestDTO dto);

    NotificationResponseDTO getById(Long id);

    List<NotificationResponseDTO> getByTenant(
            Long tenantId);

    List<NotificationResponseDTO> getByStatut(
            Long tenantId, String statut);

    NotificationResponseDTO annuler(Long id);

    long countNotifications(Long tenantId);

    // ── Méthodes utilitaires ───────────────
    NotificationResponseDTO envoyerRappelRDV(
            Long tenantId,
            Long patientId,
            String contact,
            String canal,
            String dateRDV,
            String medecinNom);

    NotificationResponseDTO envoyerConfirmationRDV(
            Long tenantId,
            Long patientId,
            String contact,
            String canal,
            String dateRDV);

    NotificationResponseDTO envoyerFactureDisponible(
            Long tenantId,
            Long patientId,
            String contact,
            String numeroFacture,
            double montant);
}