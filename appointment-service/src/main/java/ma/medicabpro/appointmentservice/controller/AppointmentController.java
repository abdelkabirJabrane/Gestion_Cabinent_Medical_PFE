package ma.medicabpro.appointmentservice.controller;




import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.medicabpro.appointmentservice.dto.AppointmentRequestDTO;
import ma.medicabpro.appointmentservice.dto.AppointmentResponseDTO;
import ma.medicabpro.appointmentservice.service.AppointmentService;
import ma.medicabpro.appointmentservice.dto.ToggleClosedDayRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentService service;

    // ══════════════════════════════════════════════════════
    // ── ENDPOINTS PUBLICS (consultation calendrier patient) ──
    // ══════════════════════════════════════════════════════

    /** Retourne les créneaux occupés (non annulés) d'un médecin – accessible sans authentification */
    @GetMapping("/public/medecin/{medecinId}")
    public ResponseEntity<List<AppointmentResponseDTO>> getPublicByMedecin(
            @PathVariable Long medecinId) {
        List<AppointmentResponseDTO> rdvs = service.getRDVByMedecin(medecinId)
                .stream()
                .filter(r -> !"ANNULE".equals(r.getStatut()))
                .toList();
        return ResponseEntity.ok(rdvs);
    }

    /** Retourne les jours fermés d'un médecin – accessible sans authentification (pas de filtre tenantId) */
    @GetMapping("/public/closed-days")
    public ResponseEntity<List<String>> getPublicClosedDays(
            @RequestParam Long medecinId) {
        return ResponseEntity.ok(service.getPublicClosedDays(medecinId));
    }

    // ── POST /api/appointments ─────────────
    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> creer(
            @Valid @RequestBody AppointmentRequestDTO dto) {
        log.info("POST /api/appointments");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.creerRDV(dto));
    }

    // ── GET /api/appointments/{id} ─────────
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(service.getRDVById(id));
    }

    // ── GET /api/appointments/patient/{id} ─
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponseDTO>> getByPatient(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(
                service.getRDVByPatient(patientId));
    }

    // ── GET /api/appointments/medecin/{id} ─
    @GetMapping("/medecin/{medecinId}")
    public ResponseEntity<List<AppointmentResponseDTO>> getByMedecin(
            @PathVariable Long medecinId) {
        return ResponseEntity.ok(
                service.getRDVByMedecin(medecinId));
    }

    // ── GET /api/appointments/jour ─────────
    @GetMapping("/jour")
    public ResponseEntity<List<AppointmentResponseDTO>> getRDVDuJour(
            @RequestParam Long medecinId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(
                service.getRDVDuJour(medecinId, date));
    }

    // ── GET /api/appointments?tenantId=1 ───
    @GetMapping
    public ResponseEntity<List<AppointmentResponseDTO>> getByTenant(
            @RequestParam Long tenantId) {
        return ResponseEntity.ok(
                service.getRDVByTenant(tenantId));
    }

    // ── PUT /api/appointments/{id}/confirmer
    @PutMapping("/{id}/confirmer")
    public ResponseEntity<AppointmentResponseDTO> confirmer(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                service.confirmerRDV(id));
    }

    // ── PUT /api/appointments/{id}/annuler ─
    @PutMapping("/{id}/annuler")
    public ResponseEntity<AppointmentResponseDTO> annuler(
            @PathVariable Long id,
            @RequestParam String motif) {
        return ResponseEntity.ok(
                service.annulerRDV(id, motif));
    }

    // ── PUT /api/appointments/{id}/terminer ─
    @PutMapping("/{id}/terminer")
    public ResponseEntity<AppointmentResponseDTO> terminer(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                service.terminerRDV(id));
    }

    // ── PUT /api/appointments/{id}/en-cours ─
    @PutMapping("/{id}/en-cours")
    public ResponseEntity<AppointmentResponseDTO> enCours(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                service.enCoursRDV(id));
    }

    // ── PUT /api/appointments/{id}/absent ──
    @PutMapping("/{id}/absent")
    public ResponseEntity<AppointmentResponseDTO> absent(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                service.marquerAbsent(id));
    }

    // ── DELETE /api/appointments/{id} ──────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(
            @PathVariable Long id) {
        service.supprimerRDV(id);
        return ResponseEntity.noContent().build();
    }

    // ── GET /api/appointments/count ────────
    @GetMapping("/count")
    public ResponseEntity<Long> count(
            @RequestParam Long tenantId) {
        return ResponseEntity.ok(
                service.countRDV(tenantId));
    }

    // ── GET /api/appointments/closed-days ──
    @GetMapping("/closed-days")
    public ResponseEntity<List<String>> getClosedDays(
            @RequestParam Long medecinId,
            @RequestParam Long tenantId) {
        return ResponseEntity.ok(
                service.getClosedDays(medecinId, tenantId));
    }

    // ── POST /api/appointments/closed-days/toggle ──
    @PostMapping("/closed-days/toggle")
    public ResponseEntity<Void> toggleClosedDay(
            @Valid @RequestBody ToggleClosedDayRequest request) {
        service.toggleClosedDay(request);
        return ResponseEntity.ok().build();
    }
}