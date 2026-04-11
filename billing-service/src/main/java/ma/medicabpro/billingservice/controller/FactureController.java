package ma.medicabpro.billingservice.controller;



import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.medicabpro.billingservice.dto.FactureRequestDTO;
import ma.medicabpro.billingservice.dto.FactureResponseDTO;
import ma.medicabpro.billingservice.dto.PaiementRequestDTO;
import ma.medicabpro.billingservice.service.FactureService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/factures")
@RequiredArgsConstructor
@Slf4j
public class FactureController {

    private final FactureService factureService;

    @PostMapping
    public ResponseEntity<FactureResponseDTO> creer(
            @Valid @RequestBody FactureRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(factureService.creerFacture(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FactureResponseDTO> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                factureService.getFactureById(id));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<FactureResponseDTO>> getByPatient(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(
                factureService.getByPatient(patientId));
    }

    @GetMapping
    public ResponseEntity<List<FactureResponseDTO>> getByTenant(
            @RequestParam Long tenantId) {
        return ResponseEntity.ok(
                factureService.getByTenant(tenantId));
    }

    @GetMapping("/statut")
    public ResponseEntity<List<FactureResponseDTO>> getByStatut(
            @RequestParam Long tenantId,
            @RequestParam String statut) {
        return ResponseEntity.ok(
                factureService.getByStatut(tenantId, statut));
    }

    @PostMapping("/payer")
    public ResponseEntity<FactureResponseDTO> payer(
            @Valid @RequestBody PaiementRequestDTO dto) {
        return ResponseEntity.ok(
                factureService.payerFacture(dto));
    }

    @PutMapping("/{id}/annuler")
    public ResponseEntity<FactureResponseDTO> annuler(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                factureService.annulerFacture(id));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> count(
            @RequestParam Long tenantId) {
        return ResponseEntity.ok(
                factureService.countFactures(tenantId));
    }

    @GetMapping("/stats/encaisse")
    public ResponseEntity<Double> totalEncaisse(
            @RequestParam Long tenantId) {
        return ResponseEntity.ok(
                factureService.getTotalEncaisse(tenantId));
    }

    @GetMapping("/stats/impaye")
    public ResponseEntity<Double> totalImpaye(
            @RequestParam Long tenantId) {
        return ResponseEntity.ok(
                factureService.getTotalImpaye(tenantId));
    }
}
