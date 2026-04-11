package ma.medicabpro.medicalrecordservice.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.medicabpro.medicalrecordservice.dto.ConsultationRequestDTO;
import ma.medicabpro.medicalrecordservice.dto.ConsultationResponseDTO;
import ma.medicabpro.medicalrecordservice.entity.Consultation;
import ma.medicabpro.medicalrecordservice.entity.DossierPatient;
import ma.medicabpro.medicalrecordservice.entity.enums.TypeConsultation;
import ma.medicabpro.medicalrecordservice.exception.DossierNotFoundException;
import ma.medicabpro.medicalrecordservice.repository.ConsultationRepository;
import ma.medicabpro.medicalrecordservice.repository.DossierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConsultationServiceImpl
        implements ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final DossierRepository dossierRepository;

    @Override
    public ConsultationResponseDTO creerConsultation(ConsultationRequestDTO dto) {
        log.info("Création consultation dossier: {}", dto.getDossierId());

        DossierPatient dossier = dossierRepository
                .findById(dto.getDossierId())
                .orElseThrow(() -> new DossierNotFoundException(
                        "Dossier introuvable: " + dto.getDossierId()));

        Consultation consultation = Consultation.builder()
                .dossier(dossier)
                .tenantId(dto.getTenantId())
                .medecinId(dto.getMedecinId())
                .rdvId(dto.getRdvId())
                .dateHeure(dto.getDateHeure())
                .motif(dto.getMotif())
                .anamnese(dto.getAnamnese())
                .examenClinique(dto.getExamenClinique())
                .diagnostic(dto.getDiagnostic())
                .conclusion(dto.getConclusion())
                .recommandations(dto.getRecommandations())
                .typeConsultation(dto.getTypeConsultation() != null
                        ? TypeConsultation.valueOf(dto.getTypeConsultation())
                        : TypeConsultation.PRESENTIELLE)
                .tensionSystolique(dto.getTensionSystolique())
                .tensionDiastolique(dto.getTensionDiastolique())
                .frequenceCardiaque(dto.getFrequenceCardiaque())
                .temperature(dto.getTemperature())
                .saturationO2(dto.getSaturationO2())
                .poids(dto.getPoids())
                .taille(dto.getTaille())
                .montantTotal(dto.getMontantTotal())
                .build();

        return toDTO(consultationRepository.save(consultation));
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationResponseDTO getConsultationById(Long id) {
        return toDTO(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationResponseDTO> getByDossier(Long dossierId) {
        return consultationRepository
                .findByDossierIdOrderByDateHeureDesc(dossierId)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationResponseDTO> getByMedecin(Long medecinId) {
        return consultationRepository
                .findByMedecinIdOrderByDateHeureDesc(medecinId)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationResponseDTO> getByTenant(Long tenantId) {
        return consultationRepository
                .findByTenantIdOrderByDateHeureDesc(tenantId)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ConsultationResponseDTO modifierConsultation(Long id, ConsultationRequestDTO dto) {
        Consultation c = findById(id);
        c.setMotif(dto.getMotif());
        c.setAnamnese(dto.getAnamnese());
        c.setExamenClinique(dto.getExamenClinique());
        c.setDiagnostic(dto.getDiagnostic());
        c.setConclusion(dto.getConclusion());
        c.setRecommandations(dto.getRecommandations());
        c.setTensionSystolique(dto.getTensionSystolique());
        c.setTensionDiastolique(dto.getTensionDiastolique());
        c.setFrequenceCardiaque(dto.getFrequenceCardiaque());
        c.setTemperature(dto.getTemperature());
        c.setSaturationO2(dto.getSaturationO2());
        c.setPoids(dto.getPoids());
        c.setTaille(dto.getTaille());
        c.setMontantTotal(dto.getMontantTotal());
        return toDTO(consultationRepository.save(c));
    }

    @Override
    @Transactional(readOnly = true)
    public long countConsultations(Long tenantId) {
        return consultationRepository.countByTenantId(tenantId);
    }

    private Consultation findById(Long id) {
        return consultationRepository.findById(id)
                .orElseThrow(() -> new DossierNotFoundException(
                        "Consultation introuvable: " + id));
    }

    private ConsultationResponseDTO toDTO(Consultation c) {
        return ConsultationResponseDTO.builder()
                .id(c.getId())
                .tenantId(c.getTenantId())
                .dossierId(c.getDossier() != null
                        ? c.getDossier().getId() : null)
                .medecinId(c.getMedecinId())
                .rdvId(c.getRdvId())
                .dateHeure(c.getDateHeure())
                .motif(c.getMotif())
                .anamnese(c.getAnamnese())
                .examenClinique(c.getExamenClinique())
                .diagnostic(c.getDiagnostic())
                .conclusion(c.getConclusion())
                .recommandations(c.getRecommandations())
                .typeConsultation(c.getTypeConsultation() != null
                        ? c.getTypeConsultation().name() : null)
                .tensionSystolique(c.getTensionSystolique())
                .tensionDiastolique(c.getTensionDiastolique())
                .frequenceCardiaque(c.getFrequenceCardiaque())
                .temperature(c.getTemperature())
                .saturationO2(c.getSaturationO2())
                .poids(c.getPoids())
                .taille(c.getTaille())
                .imc(c.getIMC())
                .montantTotal(c.getMontantTotal())
                .analyseAIEffectuee(c.isAnalyseAIEffectuee())
                .hasOrdonnance(c.getOrdonnance() != null)
                .dateCreation(c.getDateCreation())
                .build();
    }
}