package ma.medicabpro.medicalrecordservice.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.medicabpro.medicalrecordservice.dto.DossierRequestDTO;
import ma.medicabpro.medicalrecordservice.dto.DossierResponseDTO;
import ma.medicabpro.medicalrecordservice.entity.DossierPatient;
import ma.medicabpro.medicalrecordservice.exception.DossierNotFoundException;
import ma.medicabpro.medicalrecordservice.repository.DossierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DossierServiceImpl
        implements DossierService {

    private final DossierRepository dossierRepository;

    @Override
    public DossierResponseDTO creerDossier(DossierRequestDTO dto) {
        log.info("Création dossier patient: {}", dto.getPatientId());

        if (dossierRepository.existsByPatientId(dto.getPatientId())) {
            throw new RuntimeException(
                    "Dossier déjà existant pour patient: "
                            + dto.getPatientId());
        }

        DossierPatient dossier = DossierPatient.builder()
                .tenantId(dto.getTenantId())
                .patientId(dto.getPatientId())
                .antecedentsFamiliaux(dto.getAntecedentsFamiliaux())
                .antecedentsPersonnels(dto.getAntecedentsPersonnels())
                .allergies(dto.getAllergies())
                .medicamentsEnCours(dto.getMedicamentsEnCours())
                .notesGenerales(dto.getNotesGenerales())
                .poids(dto.getPoids())
                .taille(dto.getTaille())
                .groupeSanguin(dto.getGroupeSanguin())
                .build();

        return toDTO(dossierRepository.save(dossier));
    }

    @Override
    @Transactional(readOnly = true)
    public DossierResponseDTO getDossierById(Long id) {
        return toDTO(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public DossierResponseDTO getDossierByPatient(Long patientId) {
        return toDTO(dossierRepository
                .findByPatientId(patientId)
                .orElseThrow(() -> new DossierNotFoundException(
                        "Dossier introuvable patient: " + patientId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DossierResponseDTO> getAllDossiers(Long tenantId) {
        return dossierRepository.findByTenantId(tenantId)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DossierResponseDTO modifierDossier(Long id, DossierRequestDTO dto) {
        DossierPatient dossier = findById(id);
        dossier.setAntecedentsFamiliaux(dto.getAntecedentsFamiliaux());
        dossier.setAntecedentsPersonnels(dto.getAntecedentsPersonnels());
        dossier.setAllergies(dto.getAllergies());
        dossier.setMedicamentsEnCours(dto.getMedicamentsEnCours());
        dossier.setNotesGenerales(dto.getNotesGenerales());
        dossier.setPoids(dto.getPoids());
        dossier.setTaille(dto.getTaille());
        dossier.setGroupeSanguin(dto.getGroupeSanguin());
        return toDTO(dossierRepository.save(dossier));
    }

    private DossierPatient findById(Long id) {
        return dossierRepository.findById(id)
                .orElseThrow(() -> new DossierNotFoundException(
                        "Dossier introuvable: " + id));
    }

    private DossierResponseDTO toDTO(DossierPatient d) {
        return DossierResponseDTO.builder()
                .id(d.getId())
                .tenantId(d.getTenantId())
                .patientId(d.getPatientId())
                .numeroDossier(d.getNumeroDossier())
                .antecedentsFamiliaux(d.getAntecedentsFamiliaux())
                .antecedentsPersonnels(d.getAntecedentsPersonnels())
                .allergies(d.getAllergies())
                .medicamentsEnCours(d.getMedicamentsEnCours())
                .notesGenerales(d.getNotesGenerales())
                .poids(d.getPoids())
                .taille(d.getTaille())
                .imc(d.getIMC())
                .groupeSanguin(d.getGroupeSanguin())
                .nombreConsultations(
                        d.getConsultations().size())
                .dateCreation(d.getDateCreation())
                .dateModification(d.getDateModification())
                .build();
    }
}
