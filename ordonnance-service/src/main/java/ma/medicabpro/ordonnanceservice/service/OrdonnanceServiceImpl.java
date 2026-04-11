package ma.medicabpro.ordonnanceservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.medicabpro.ordonnanceservice.dto.LigneOrdonnanceRequestDTO;
import ma.medicabpro.ordonnanceservice.dto.LigneOrdonnanceResponseDTO;
import ma.medicabpro.ordonnanceservice.dto.OrdonnanceRequestDTO;
import ma.medicabpro.ordonnanceservice.dto.OrdonnanceResponseDTO;
import ma.medicabpro.ordonnanceservice.entity.LigneOrdonnance;
import ma.medicabpro.ordonnanceservice.entity.Ordonnance;
import ma.medicabpro.ordonnanceservice.entity.enums.StatutOrdonnance;
import ma.medicabpro.ordonnanceservice.exception.OrdonnanceNotFoundException;
import ma.medicabpro.ordonnanceservice.repository.OrdonnanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrdonnanceServiceImpl implements OrdonnanceService {

    private final OrdonnanceRepository ordonnanceRepository;

    @Override
    public OrdonnanceResponseDTO creerOrdonnance(OrdonnanceRequestDTO dto) {
        log.info("Création ordonnance pour consultation: {}", dto.getConsultationId());

        Ordonnance ordonnance = Ordonnance.builder()
                .consultationId(dto.getConsultationId())
                .patientId(dto.getPatientId())
                .medecinId(dto.getMedecinId())
                .tenantId(dto.getTenantId())
                .dateEmission(dto.getDateEmission())
                .dateValidite(dto.getDateValidite())
                .instructions(dto.getInstructions())
                .renouvellement(dto.isRenouvellement())
                .statut(dto.getStatut() != null
                        ? StatutOrdonnance.valueOf(dto.getStatut())
                        : StatutOrdonnance.ACTIVE)
                .build();

        // Ajouter les lignes
        if (dto.getLignes() != null) {
            for (LigneOrdonnanceRequestDTO ligneDto : dto.getLignes()) {
                LigneOrdonnance ligne = toLigneEntity(ligneDto, ordonnance);
                ordonnance.getLignes().add(ligne);
            }
        }

        return toDTO(ordonnanceRepository.save(ordonnance));
    }

    @Override
    @Transactional(readOnly = true)
    public OrdonnanceResponseDTO getOrdonnanceById(Long id) {
        return toDTO(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public OrdonnanceResponseDTO getByConsultationId(Long consultationId) {
        return ordonnanceRepository.findByConsultationId(consultationId)
                .map(this::toDTO)
                .orElseThrow(() -> new OrdonnanceNotFoundException(
                        "Ordonnance introuvable pour la consultation: " + consultationId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdonnanceResponseDTO> getByPatient(Long patientId) {
        return ordonnanceRepository
                .findByPatientIdOrderByDateCreationDesc(patientId)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdonnanceResponseDTO> getByMedecin(Long medecinId) {
        return ordonnanceRepository
                .findByMedecinIdOrderByDateCreationDesc(medecinId)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdonnanceResponseDTO> getByTenant(Long tenantId) {
        return ordonnanceRepository
                .findByTenantIdOrderByDateCreationDesc(tenantId)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdonnanceResponseDTO> getByTenantAndStatut(Long tenantId, String statut) {
        StatutOrdonnance statutEnum = StatutOrdonnance.valueOf(statut.toUpperCase());
        return ordonnanceRepository
                .findByTenantIdAndStatutOrderByDateCreationDesc(tenantId, statutEnum)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrdonnanceResponseDTO modifierOrdonnance(Long id, OrdonnanceRequestDTO dto) {
        log.info("Modification ordonnance id: {}", id);
        Ordonnance ordonnance = findById(id);

        ordonnance.setDateEmission(dto.getDateEmission());
        ordonnance.setDateValidite(dto.getDateValidite());
        ordonnance.setInstructions(dto.getInstructions());
        ordonnance.setRenouvellement(dto.isRenouvellement());

        if (dto.getStatut() != null) {
            ordonnance.setStatut(StatutOrdonnance.valueOf(dto.getStatut()));
        }

        // Remplacer les lignes
        ordonnance.getLignes().clear();
        if (dto.getLignes() != null) {
            for (LigneOrdonnanceRequestDTO ligneDto : dto.getLignes()) {
                ordonnance.getLignes().add(toLigneEntity(ligneDto, ordonnance));
            }
        }

        return toDTO(ordonnanceRepository.save(ordonnance));
    }

    @Override
    public OrdonnanceResponseDTO changerStatut(Long id, String statut) {
        log.info("Changement statut ordonnance id: {} -> {}", id, statut);
        Ordonnance ordonnance = findById(id);
        ordonnance.setStatut(StatutOrdonnance.valueOf(statut.toUpperCase()));
        return toDTO(ordonnanceRepository.save(ordonnance));
    }

    @Override
    public void supprimerOrdonnance(Long id) {
        log.info("Suppression ordonnance id: {}", id);
        if (!ordonnanceRepository.existsById(id)) {
            throw new OrdonnanceNotFoundException("Ordonnance introuvable: " + id);
        }
        ordonnanceRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countOrdonnances(Long tenantId) {
        return ordonnanceRepository.countByTenantId(tenantId);
    }

    // ── Helper methods ────────────────────────────────────────────────────────

    private Ordonnance findById(Long id) {
        return ordonnanceRepository.findById(id)
                .orElseThrow(() -> new OrdonnanceNotFoundException(
                        "Ordonnance introuvable: " + id));
    }

    private LigneOrdonnance toLigneEntity(LigneOrdonnanceRequestDTO dto, Ordonnance ordonnance) {
        return LigneOrdonnance.builder()
                .ordonnance(ordonnance)
                .medicament(dto.getMedicament())
                .dci(dto.getDci())
                .dosage(dto.getDosage())
                .forme(dto.getForme())
                .posologie(dto.getPosologie())
                .dureeTraitement(dto.getDureeTraitement())
                .unite(dto.getUnite())
                .instructions(dto.getInstructions())
                .substituable(dto.isSubstituable())
                .build();
    }

    private OrdonnanceResponseDTO toDTO(Ordonnance o) {
        List<LigneOrdonnanceResponseDTO> lignesDTO = o.getLignes() == null ? List.of() :
                o.getLignes().stream().map(this::toLigneDTO).collect(Collectors.toList());

        return OrdonnanceResponseDTO.builder()
                .id(o.getId())
                .tenantId(o.getTenantId())
                .consultationId(o.getConsultationId())
                .patientId(o.getPatientId())
                .medecinId(o.getMedecinId())
                .dateEmission(o.getDateEmission())
                .dateValidite(o.getDateValidite())
                .instructions(o.getInstructions())
                .renouvellement(o.isRenouvellement())
                .statut(o.getStatut() != null ? o.getStatut().name() : null)
                .valide(o.isValide())
                .lignes(lignesDTO)
                .dateCreation(o.getDateCreation())
                .build();
    }

    private LigneOrdonnanceResponseDTO toLigneDTO(LigneOrdonnance l) {
        return LigneOrdonnanceResponseDTO.builder()
                .id(l.getId())
                .medicament(l.getMedicament())
                .dci(l.getDci())
                .dosage(l.getDosage())
                .forme(l.getForme())
                .posologie(l.getPosologie())
                .dureeTraitement(l.getDureeTraitement())
                .unite(l.getUnite())
                .instructions(l.getInstructions())
                .substituable(l.isSubstituable())
                .build();
    }
}
