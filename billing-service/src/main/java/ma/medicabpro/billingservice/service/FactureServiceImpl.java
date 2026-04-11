package ma.medicabpro.billingservice.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.medicabpro.billingservice.dto.FactureRequestDTO;
import ma.medicabpro.billingservice.dto.FactureResponseDTO;
import ma.medicabpro.billingservice.dto.PaiementRequestDTO;
import ma.medicabpro.billingservice.dto.PaiementResponseDTO;
import ma.medicabpro.billingservice.entity.Facture;
import ma.medicabpro.billingservice.entity.Paiement;
import ma.medicabpro.billingservice.entity.enums.ModePaiement;
import ma.medicabpro.billingservice.entity.enums.StatutFacture;
import ma.medicabpro.billingservice.exception.FactureNotFoundException;
import ma.medicabpro.billingservice.repository.FactureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation
        .Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FactureServiceImpl implements FactureService {

    private final FactureRepository factureRepository;

    // ── Créer facture ──────────────────────
    @Override
    public FactureResponseDTO creerFacture(
            FactureRequestDTO dto) {

        log.info("Création facture patient: {}",
                dto.getPatientId());

        Facture facture = Facture.builder()
                .tenantId(dto.getTenantId())
                .patientId(dto.getPatientId())
                .medecinId(dto.getMedecinId())
                .consultationId(
                        dto.getConsultationId())
                .montantTTC(dto.getMontantTTC())
                .tva(dto.getTva() != null
                        ? dto.getTva() : 0.0)
                .notesFacture(dto.getNotesFacture())
                .build();

        return toDTO(
                factureRepository.save(facture));
    }

    // ── Get par ID ─────────────────────────
    @Override
    @Transactional(readOnly = true)
    public FactureResponseDTO getFactureById(
            Long id) {
        return toDTO(findById(id));
    }

    // ── Get par patient ────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<FactureResponseDTO> getByPatient(
            Long patientId) {
        return factureRepository
                .findByPatientIdOrderByDateCreationDesc(
                        patientId)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Get par tenant ─────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<FactureResponseDTO> getByTenant(
            Long tenantId) {
        return factureRepository
                .findByTenantIdOrderByDateCreationDesc(
                        tenantId)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Get par statut ─────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<FactureResponseDTO> getByStatut(
            Long tenantId, String statut) {
        return factureRepository
                .findByTenantIdAndStatut(
                        tenantId,
                        StatutFacture.valueOf(statut))
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Payer facture ──────────────────────
    @Override
    public FactureResponseDTO payerFacture(
            PaiementRequestDTO dto) {

        log.info("Paiement facture: {}",
                dto.getFactureId());

        Facture facture = findById(
                dto.getFactureId());

        if (facture.getStatut()
                == StatutFacture.ANNULEE) {
            throw new RuntimeException(
                    "Facture annulée !");
        }
        if (facture.getStatut()
                == StatutFacture.PAYEE) {
            throw new RuntimeException(
                    "Facture déjà payée !");
        }
        if (dto.getMontant() > facture
                .getMontantRestant()) {
            throw new RuntimeException(
                    "Montant > reste dû : "
                            + facture.getMontantRestant());
        }

        Paiement paiement = Paiement.builder()
                .facture(facture)
                .montant(dto.getMontant())
                .modePaiement(ModePaiement.valueOf(
                        dto.getModePaiement()))
                .reference(dto.getReference())
                .numeroCheque(dto.getNumeroCheque())
                .build();

        facture.getPaiements().add(paiement);
        facture.ajouterPaiement(dto.getMontant());

        return toDTO(
                factureRepository.save(facture));
    }

    // ── Annuler facture ────────────────────
    @Override
    public FactureResponseDTO annulerFacture(
            Long id) {
        Facture facture = findById(id);
        if (facture.getStatut()
                == StatutFacture.PAYEE) {
            throw new RuntimeException(
                    "Impossible d'annuler une facture payée !");
        }
        facture.setStatut(StatutFacture.ANNULEE);
        return toDTO(
                factureRepository.save(facture));
    }

    // ── Stats ──────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public long countFactures(Long tenantId) {
        return factureRepository
                .countByTenantId(tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getTotalEncaisse(
            Long tenantId) {
        Double total = factureRepository
                .getTotalEncaisse(tenantId);
        return total != null ? total : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Double getTotalImpaye(
            Long tenantId) {
        Double total = factureRepository
                .getTotalImpaye(tenantId);
        return total != null ? total : 0.0;
    }

    // ── Helper ─────────────────────────────
    private Facture findById(Long id) {
        return factureRepository.findById(id)
                .orElseThrow(() ->
                        new FactureNotFoundException(
                                "Facture introuvable: " + id));
    }

    private FactureResponseDTO toDTO(
            Facture f) {
        List<PaiementResponseDTO> paiements =
                f.getPaiements().stream()
                        .map(p -> PaiementResponseDTO
                                .builder()
                                .id(p.getId())
                                .factureId(f.getId())
                                .montant(p.getMontant())
                                .modePaiement(
                                        p.getModePaiement()
                                                .name())
                                .datePaiement(
                                        p.getDatePaiement())
                                .reference(p.getReference())
                                .statut(p.getStatut().name())
                                .dateCreation(
                                        p.getDateCreation())
                                .build())
                        .collect(Collectors.toList());

        return FactureResponseDTO.builder()
                .id(f.getId())
                .tenantId(f.getTenantId())
                .numeroFacture(f.getNumeroFacture())
                .patientId(f.getPatientId())
                .medecinId(f.getMedecinId())
                .consultationId(f.getConsultationId())
                .montantHT(f.getMontantHT())
                .tva(f.getTva())
                .montantTTC(f.getMontantTTC())
                .montantPaye(f.getMontantPaye())
                .montantRestant(
                        f.getMontantRestant())
                .statut(f.getStatut().name())
                .dateEmission(f.getDateEmission())
                .dateEcheance(f.getDateEcheance())
                .notesFacture(f.getNotesFacture())
                .paiements(paiements)
                .dateCreation(f.getDateCreation())
                .dateModification(
                        f.getDateModification())
                .build();
    }
}
