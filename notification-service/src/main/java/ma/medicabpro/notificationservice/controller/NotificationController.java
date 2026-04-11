package ma.medicabpro.notificationservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.medicabpro.notificationservice.dto.NotificationRequestDTO;
import ma.medicabpro.notificationservice.dto.NotificationResponseDTO;
import ma.medicabpro.notificationservice.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService service;

    // POST /api/notifications/envoyer
    @PostMapping("/envoyer")
    public ResponseEntity<NotificationResponseDTO> envoyer(
            @Valid @RequestBody NotificationRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.envoyer(dto));
    }

    // POST /api/notifications/programmer
    @PostMapping("/programmer")
    public ResponseEntity<NotificationResponseDTO> programmer(
            @Valid @RequestBody NotificationRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.programmer(dto));
    }

    // GET /api/notifications/{id}
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponseDTO> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // ✅ CORRIGÉ — ajout du < manquant
    // GET /api/notifications?tenantId=1
    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getByTenant(
            @RequestParam Long tenantId) {
        return ResponseEntity.ok(
                service.getByTenant(tenantId));
    }

    // ✅ CORRIGÉ — ajout du < manquant
    // GET /api/notifications/statut
    @GetMapping("/statut")
    public ResponseEntity<List<NotificationResponseDTO>> getByStatut(
            @RequestParam Long tenantId,
            @RequestParam String statut) {
        return ResponseEntity.ok(
                service.getByStatut(tenantId, statut));
    }

    // PUT /api/notifications/{id}/annuler
    @PutMapping("/{id}/annuler")
    public ResponseEntity<NotificationResponseDTO> annuler(
            @PathVariable Long id) {
        return ResponseEntity.ok(service.annuler(id));
    }

    // GET /api/notifications/count
    @GetMapping("/count")
    public ResponseEntity<Long> count(
            @RequestParam Long tenantId) {
        return ResponseEntity.ok(
                service.countNotifications(tenantId));
    }

    // POST /api/notifications/rappel-rdv
    @PostMapping("/rappel-rdv")
    public ResponseEntity<NotificationResponseDTO> rappelRDV(
            @RequestParam Long tenantId,
            @RequestParam Long patientId,
            @RequestParam String contact,
            @RequestParam String canal,
            @RequestParam String dateRDV,
            @RequestParam String medecinNom) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.envoyerRappelRDV(
                        tenantId, patientId,
                        contact, canal,
                        dateRDV, medecinNom));
    }

    // POST /api/notifications/confirmation-rdv
    @PostMapping("/confirmation-rdv")
    public ResponseEntity<NotificationResponseDTO> confirmationRDV(
            @RequestParam Long tenantId,
            @RequestParam Long patientId,
            @RequestParam String contact,
            @RequestParam String canal,
            @RequestParam String dateRDV) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.envoyerConfirmationRDV(
                        tenantId, patientId,
                        contact, canal, dateRDV));
    }

    // POST /api/notifications/facture
    @PostMapping("/facture")
    public ResponseEntity<NotificationResponseDTO> factureDisponible(
            @RequestParam Long tenantId,
            @RequestParam Long patientId,
            @RequestParam String contact,
            @RequestParam String numeroFacture,
            @RequestParam double montant) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.envoyerFactureDisponible(
                        tenantId, patientId,
                        contact, numeroFacture,
                        montant));
    }
}