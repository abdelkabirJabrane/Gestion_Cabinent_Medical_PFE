package com.shop.user_service.service;

import com.shop.user_service.entity.Cabinet;
import com.shop.user_service.repository.CabinetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CabinetServiceImpl implements CabinetService {

    private final CabinetRepository cabinetRepository;

    @Override
    public Cabinet createCabinet(Cabinet cabinet) {
        log.info("Creating new cabinet: {}", cabinet.getNom());
        
        if (cabinetRepository.existsByEmail(cabinet.getEmail())) {
            throw new IllegalArgumentException("Un cabinet avec cet email existe déjà !");
        }
        
        // Initialiser avec des valeurs par défaut au besoin
        cabinet.setDateExpiration(LocalDate.now().plusDays(14)); // 14 jours d'essai
        cabinet.setStatut("essai"); 
        if (cabinet.getNbMedecins() == null) {
            cabinet.setNbMedecins(0);
        }

        return cabinetRepository.save(cabinet);
    }

    @Override
    public Cabinet updateCabinet(Long id, Cabinet updatedCabinet) {
        Cabinet existing = getCabinetById(id);
        
        existing.setNom(updatedCabinet.getNom());
        existing.setResponsable(updatedCabinet.getResponsable());
        if (!existing.getEmail().equals(updatedCabinet.getEmail()) && cabinetRepository.existsByEmail(updatedCabinet.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà pris par un autre cabinet");
        }
        existing.setEmail(updatedCabinet.getEmail());
        existing.setTelephone(updatedCabinet.getTelephone());
        existing.setVille(updatedCabinet.getVille());
        existing.setNbMedecins(updatedCabinet.getNbMedecins());
        existing.setPlan(updatedCabinet.getPlan());
        
        return cabinetRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public Cabinet getCabinetById(Long id) {
        return cabinetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cabinet introuvable avec l'ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cabinet> getAllCabinets() {
        return cabinetRepository.findAll();
    }

    @Override
    public void deleteCabinet(Long id) {
        Cabinet cabinet = getCabinetById(id);
        cabinetRepository.delete(cabinet);
        log.info("Cabinet {} deleted", id);
    }

    @Override
    public Cabinet updateStatus(Long id, String status) {
        Cabinet cabinet = getCabinetById(id);
        cabinet.setStatut(status);
        return cabinetRepository.save(cabinet);
    }

    @Override
    public Cabinet updatePlan(Long id, String plan, String dateExpiration) {
        Cabinet cabinet = getCabinetById(id);
        if (plan != null && !plan.isBlank()) {
            cabinet.setPlan(plan);
        }
        if (dateExpiration != null && !dateExpiration.isBlank()) {
            cabinet.setDateExpiration(LocalDate.parse(dateExpiration));
        } else {
            // Par défaut: renouvellement d'1 an à partir d'aujourd'hui
            cabinet.setDateExpiration(LocalDate.now().plusYears(1));
        }
        cabinet.setStatut("actif");
        log.info("Plan updated for cabinet {}: plan={}, expiration={}", id, plan, cabinet.getDateExpiration());
        return cabinetRepository.save(cabinet);
    }
}
