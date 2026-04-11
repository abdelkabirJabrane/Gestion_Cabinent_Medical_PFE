package ma.medicabpro.ordonnanceservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.medicabpro.ordonnanceservice.dto.OrdonnanceRequestDTO;
import ma.medicabpro.ordonnanceservice.dto.OrdonnanceResponseDTO;
import ma.medicabpro.ordonnanceservice.service.OrdonnanceService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordonnances")
@RequiredArgsConstructor
public class OrdonnanceController {

    private final OrdonnanceService ordonnanceService;

    // ── Création ──────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<OrdonnanceResponseDTO> creer(
            @Valid @RequestBody OrdonnanceRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ordonnanceService.creerOrdonnance(dto));
    }

    // ── Lecture ───────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<OrdonnanceResponseDTO> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ordonnanceService.getOrdonnanceById(id));
    }

    @GetMapping("/consultation/{consultationId}")
    public ResponseEntity<OrdonnanceResponseDTO> getByConsultation(
            @PathVariable Long consultationId) {
        return ResponseEntity.ok(
                ordonnanceService.getByConsultationId(consultationId));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<OrdonnanceResponseDTO>> getByPatient(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(ordonnanceService.getByPatient(patientId));
    }

    @GetMapping("/medecin/{medecinId}")
    public ResponseEntity<List<OrdonnanceResponseDTO>> getByMedecin(
            @PathVariable Long medecinId) {
        return ResponseEntity.ok(ordonnanceService.getByMedecin(medecinId));
    }

    @GetMapping
    public ResponseEntity<List<OrdonnanceResponseDTO>> getByTenant(
            @RequestParam Long tenantId) {
        return ResponseEntity.ok(ordonnanceService.getByTenant(tenantId));
    }

    @GetMapping("/statut")
    public ResponseEntity<List<OrdonnanceResponseDTO>> getByStatut(
            @RequestParam Long tenantId,
            @RequestParam String statut) {
        return ResponseEntity.ok(
                ordonnanceService.getByTenantAndStatut(tenantId, statut));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> count(@RequestParam Long tenantId) {
        return ResponseEntity.ok(ordonnanceService.countOrdonnances(tenantId));
    }

    // ── Modification ──────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<OrdonnanceResponseDTO> modifier(
            @PathVariable Long id,
            @Valid @RequestBody OrdonnanceRequestDTO dto) {
        return ResponseEntity.ok(ordonnanceService.modifierOrdonnance(id, dto));
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<OrdonnanceResponseDTO> changerStatut(
            @PathVariable Long id,
            @RequestParam String statut) {
        return ResponseEntity.ok(ordonnanceService.changerStatut(id, statut));
    }

    // ── Suppression ───────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        ordonnanceService.supprimerOrdonnance(id);
        return ResponseEntity.noContent().build();
    }
}
