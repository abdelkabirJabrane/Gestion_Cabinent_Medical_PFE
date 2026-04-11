package ma.medicabpro.patientservice.controller;



import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.medicabpro.patientservice.dto.PatientRequestDTO;
import ma.medicabpro.patientservice.dto.PatientResponseDTO;
import ma.medicabpro.patientservice.service.PatientService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Slf4j
public class PatientController {

    private final PatientService patientService;

    // POST /api/patients
    @PostMapping
    public ResponseEntity<PatientResponseDTO> creer(
            @Valid @RequestBody PatientRequestDTO dto) {
        log.info("POST /api/patients");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(patientService.creerPatient(dto));
    }

    // GET /api/patients/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                patientService.getPatientById(id));
    }

    // GET /api/patients/cin/{cin}
    @GetMapping("/cin/{cin}")
    public ResponseEntity<PatientResponseDTO> getByCin(
            @PathVariable String cin) {
        return ResponseEntity.ok(
                patientService.getPatientByCin(cin));
    }

    // GET /api/patients?tenantId=1
    @GetMapping
    public ResponseEntity<List<PatientResponseDTO>>
    getAll(@RequestParam Long tenantId) {
        return ResponseEntity.ok(
                patientService.getAllPatients(tenantId));
    }

    // GET /api/patients/search?tenantId=1&q=ahmed
    @GetMapping("/search")
    public ResponseEntity<List<PatientResponseDTO>>
    rechercher(
            @RequestParam Long tenantId,
            @RequestParam String q) {
        return ResponseEntity.ok(
                patientService.rechercherPatients(
                        tenantId, q));
    }

    // PUT /api/patients/{id}
    @PutMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> modifier(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequestDTO dto) {
        return ResponseEntity.ok(
                patientService.modifierPatient(id, dto));
    }

    // DELETE /api/patients/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactiver(
            @PathVariable Long id) {
        patientService.desactiverPatient(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/patients/count?tenantId=1
    @GetMapping("/count")
    public ResponseEntity<Long> count(
            @RequestParam Long tenantId) {
        return ResponseEntity.ok(
                patientService.countPatients(tenantId));
    }
}