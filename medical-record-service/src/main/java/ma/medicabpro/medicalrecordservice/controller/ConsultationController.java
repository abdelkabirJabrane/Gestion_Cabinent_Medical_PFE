package ma.medicabpro.medicalrecordservice.controller;



import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.medicabpro.medicalrecordservice.dto.ConsultationRequestDTO;
import ma.medicabpro.medicalrecordservice.dto.ConsultationResponseDTO;
import ma.medicabpro.medicalrecordservice.service.ConsultationService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;

    @PostMapping
    public ResponseEntity<ConsultationResponseDTO> creer(
            @Valid @RequestBody ConsultationRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(consultationService.creerConsultation(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultationResponseDTO> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                consultationService.getConsultationById(id));
    }

    @GetMapping("/dossier/{dossierId}")
    public ResponseEntity<List<ConsultationResponseDTO>> getByDossier(
            @PathVariable Long dossierId) {
        return ResponseEntity.ok(
                consultationService.getByDossier(dossierId));
    }

    @GetMapping("/medecin/{medecinId}")
    public ResponseEntity<List<ConsultationResponseDTO>> getByMedecin(
            @PathVariable Long medecinId) {
        return ResponseEntity.ok(
                consultationService.getByMedecin(medecinId));
    }

    @GetMapping
    public ResponseEntity<List<ConsultationResponseDTO>> getByTenant(
            @RequestParam Long tenantId) {
        return ResponseEntity.ok(
                consultationService.getByTenant(tenantId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsultationResponseDTO> modifier(
            @PathVariable Long id,
            @Valid @RequestBody ConsultationRequestDTO dto) {
        return ResponseEntity.ok(
                consultationService.modifierConsultation(id, dto));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> count(
            @RequestParam Long tenantId) {
        return ResponseEntity.ok(
                consultationService.countConsultations(tenantId));
    }
}