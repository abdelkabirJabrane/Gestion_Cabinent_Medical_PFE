package ma.medicabpro.medicalrecordservice.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.medicabpro.medicalrecordservice.dto.DossierRequestDTO;
import ma.medicabpro.medicalrecordservice.dto.DossierResponseDTO;
import ma.medicabpro.medicalrecordservice.service.DossierService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import ma.medicabpro.medicalrecordservice.entity.MedicalDocument;
import ma.medicabpro.medicalrecordservice.repository.MedicalDocumentRepository;
import java.io.IOException;
import java.nio.file.*;

@RestController
@RequestMapping("/api/dossiers")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:30010"}, allowedHeaders = "*")
public class DossierController {

    private final DossierService dossierService;
    private final MedicalDocumentRepository medicalDocumentRepository;

    @PostMapping
    public ResponseEntity<DossierResponseDTO> creer(
            @Valid @RequestBody DossierRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dossierService.creerDossier(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DossierResponseDTO> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                dossierService.getDossierById(id));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<DossierResponseDTO> getByPatient(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(
                dossierService.getDossierByPatient(patientId));
    }

    @GetMapping
    public ResponseEntity<List<DossierResponseDTO>> getAll(
            @RequestParam Long tenantId) {
        return ResponseEntity.ok(
                dossierService.getAllDossiers(tenantId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DossierResponseDTO> modifier(
            @PathVariable Long id,
            @Valid @RequestBody DossierRequestDTO dto) {
        return ResponseEntity.ok(
                dossierService.modifierDossier(id, dto));
    }

    // ── Documents ──────────────────────────

    @GetMapping("/{id}/documents")
    public ResponseEntity<List<MedicalDocument>> getDocuments(@PathVariable Long id) {
        return ResponseEntity.ok(medicalDocumentRepository.findByDossierId(id));
    }

    @PostMapping("/{id}/documents")
    public ResponseEntity<?> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            @RequestParam("nom") String nom) {
        
        try {
            // Simple storage logic (saving to a local folder 'uploads')
            String uploadDir = "uploads/";
            Files.createDirectories(Paths.get(uploadDir));
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir + fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            MedicalDocument doc = MedicalDocument.builder()
                    .dossierId(id)
                    .nom(nom)
                    .type(type)
                    .mimeType(file.getContentType())
                    .url("/uploads/" + fileName)
                    .build();

            return ResponseEntity.ok(medicalDocumentRepository.save(doc));
            
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur upload: " + e.getMessage());
        }
    }

    @DeleteMapping("/{dossierId}/documents/{docId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long dossierId, @PathVariable Long docId) {
        medicalDocumentRepository.deleteById(docId);
        return ResponseEntity.ok().build();
    }
}
