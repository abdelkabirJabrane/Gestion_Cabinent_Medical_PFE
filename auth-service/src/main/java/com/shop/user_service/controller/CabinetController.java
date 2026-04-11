package com.shop.user_service.controller;

import com.shop.user_service.entity.Cabinet;
import com.shop.user_service.service.CabinetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cabinets")
@RequiredArgsConstructor
@Slf4j
public class CabinetController {

    private final CabinetService cabinetService;

    // Récupérer tous les cabinets (Admin)
    @GetMapping
    public ResponseEntity<List<Cabinet>> getAllCabinets() {
        log.info("Fetching all cabinets");
        return ResponseEntity.ok(cabinetService.getAllCabinets());
    }

    // Récupérer un cabinet
    @GetMapping("/{id}")
    public ResponseEntity<Cabinet> getCabinetById(@PathVariable Long id) {
        log.info("Fetching cabinet with id {}", id);
        return ResponseEntity.ok(cabinetService.getCabinetById(id));
    }

    // Créer un cabinet
    @PostMapping
    public ResponseEntity<Cabinet> createCabinet(@Valid @RequestBody Cabinet cabinet) {
        log.info("Creating new cabinet");
        Cabinet created = cabinetService.createCabinet(cabinet);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // Modifier un cabinet existant
    @PutMapping("/{id}")
    public ResponseEntity<Cabinet> updateCabinet(@PathVariable Long id, @Valid @RequestBody Cabinet cabinet) {
        log.info("Updating cabinet {}", id);
        return ResponseEntity.ok(cabinetService.updateCabinet(id, cabinet));
    }

    // Changer le statut (activer, suspendre) - C'est ce qu'attend le front-end {"status": "actif"}
    @PutMapping("/{id}/status")
    public ResponseEntity<Cabinet> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        log.info("Updating status of cabinet {} to {}", id, status);
        return ResponseEntity.ok(cabinetService.updateStatus(id, status));
    }

    // Changer le plan d'abonnement d'un cabinet
    @PutMapping("/{id}/plan")
    public ResponseEntity<Cabinet> updatePlan(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String plan = body.get("plan");
        String dateExpiration = body.get("dateExpiration"); // format ISO: "2027-04-01"
        log.info("Updating plan of cabinet {} to {}", id, plan);
        return ResponseEntity.ok(cabinetService.updatePlan(id, plan, dateExpiration));
    }

    // Supprimer un cabinet
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCabinet(@PathVariable Long id) {
        log.info("Deleting cabinet {}", id);
        cabinetService.deleteCabinet(id);
        return ResponseEntity.noContent().build();
    }
}
