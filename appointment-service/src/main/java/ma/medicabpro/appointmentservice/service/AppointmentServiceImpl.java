package ma.medicabpro.appointmentservice.service;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.medicabpro.appointmentservice.dto.AppointmentRequestDTO;
import ma.medicabpro.appointmentservice.dto.AppointmentResponseDTO;
import ma.medicabpro.appointmentservice.entity.Appointment;
import ma.medicabpro.appointmentservice.entity.enums.StatutRDV;
import ma.medicabpro.appointmentservice.entity.enums.TypeConsultation;
import ma.medicabpro.appointmentservice.exception.AppointmentNotFoundException;
import ma.medicabpro.appointmentservice.feign.PatientClient;
import ma.medicabpro.appointmentservice.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation
        .Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentServiceImpl
        implements AppointmentService {

    private final AppointmentRepository repo;
    private final PatientClient patientClient;

    // ── Créer RDV ──────────────────────────
    @Override
    public AppointmentResponseDTO creerRDV(
            AppointmentRequestDTO dto) {

        log.info("Création RDV patient: {} médecin: {}",
                dto.getPatientId(), dto.getMedecinId());

        // Calculer fin si non fournie (30 min)
        LocalDateTime fin = dto.getDateHeureFin();
        if (fin == null) {
            fin = dto.getDateHeureDebut()
                    .plusMinutes(30);
        }

        // Vérifier conflit horaire
        long conflits = repo.countConflits(
                dto.getMedecinId(),
                dto.getDateHeureDebut(),
                fin
        );
        if (conflits > 0) {
            throw new RuntimeException(
                    "Créneau non disponible !"
                            + " Le médecin a déjà un RDV.");
        }

        // Récupérer nom patient via Feign
        String patientNom = "Patient";
        try {
            Map<String, Object> patient =
                    patientClient.getPatientById(
                            dto.getPatientId());
            patientNom = patient.get(
                    "nomComplet").toString();
        } catch (Exception e) {
            log.warn("Patient service indisponible");
        }

        Appointment rdv = Appointment.builder()
                .tenantId(dto.getTenantId())
                .patientId(dto.getPatientId())
                .medecinId(dto.getMedecinId())
                .dateHeureDebut(dto.getDateHeureDebut())
                .dateHeureFin(fin)
                .motif(dto.getMotif())
                .statut(StatutRDV.EN_ATTENTE)
                .typeConsultation(
                        dto.getTypeConsultation() != null
                                ? TypeConsultation.valueOf(
                                dto.getTypeConsultation())
                                : TypeConsultation.PRESENTIELLE)
                .notesInternes(dto.getNotesInternes())
                .build();

        Appointment saved = repo.save(rdv);
        log.info("RDV créé ID: {}", saved.getId());

        AppointmentResponseDTO response =
                toDTO(saved);
        response.setPatientNom(patientNom);
        return response;
    }

    // ── Récupérer par ID ───────────────────
    @Override
    @Transactional(readOnly = true)
    public AppointmentResponseDTO getRDVById(
            Long id) {
        return toDTO(findById(id));
    }

    // ── RDV par patient ────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO>
    getRDVByPatient(Long patientId) {
        return repo
                .findByPatientIdOrderByDateHeureDebutDesc(
                        patientId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── RDV par médecin ────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO>
    getRDVByMedecin(Long medecinId) {
        return repo
                .findByMedecinIdOrderByDateHeureDebutAsc(
                        medecinId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── RDV du jour ────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO>
    getRDVDuJour(
            Long medecinId, LocalDate date) {
        LocalDateTime debut = date.atStartOfDay();
        LocalDateTime fin = date.atTime(
                LocalTime.MAX);
        return repo.findRDVDuJour(
                        medecinId, debut, fin)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── RDV par tenant ─────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO>
    getRDVByTenant(Long tenantId) {
        return repo
                .findByTenantIdOrderByDateHeureDebutDesc(
                        tenantId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Confirmer RDV ──────────────────────
    @Override
    public AppointmentResponseDTO confirmerRDV(
            Long id) {
        Appointment rdv = findById(id);
        rdv.confirmer();
        return toDTO(repo.save(rdv));
    }

    // ── Annuler RDV ────────────────────────
    @Override
    public AppointmentResponseDTO annulerRDV(
            Long id, String motif) {
        Appointment rdv = findById(id);
        rdv.annuler(motif);
        return toDTO(repo.save(rdv));
    }

    // ── Terminer RDV ───────────────────────
    @Override
    public AppointmentResponseDTO terminerRDV(
            Long id) {
        Appointment rdv = findById(id);
        rdv.terminer();
        return toDTO(repo.save(rdv));
    }

    // ── Marquer absent ─────────────────────
    @Override
    public AppointmentResponseDTO marquerAbsent(
            Long id) {
        Appointment rdv = findById(id);
        rdv.marquerAbsent();
        return toDTO(repo.save(rdv));
    }

    // ── Supprimer RDV ──────────────────────
    @Override
    public void supprimerRDV(Long id) {
        if (!repo.existsById(id)) {
            throw new AppointmentNotFoundException(
                    "RDV introuvable : " + id);
        }
        repo.deleteById(id);
    }

    // ── Compter RDV ────────────────────────
    @Override
    @Transactional(readOnly = true)
    public long countRDV(Long tenantId) {
        return repo.countByTenantId(tenantId);
    }

    // ── Helper ─────────────────────────────
    private Appointment findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() ->
                        new AppointmentNotFoundException(
                                "RDV introuvable : " + id));
    }

    private AppointmentResponseDTO toDTO(
            Appointment a) {
        return AppointmentResponseDTO.builder()
                .id(a.getId())
                .tenantId(a.getTenantId())
                .patientId(a.getPatientId())
                .medecinId(a.getMedecinId())
                .dateHeureDebut(a.getDateHeureDebut())
                .dateHeureFin(a.getDateHeureFin())
                .statut(a.getStatut().name())
                .typeConsultation(
                        a.getTypeConsultation() != null
                                ? a.getTypeConsultation().name()
                                : null)
                .motif(a.getMotif())
                .notesInternes(a.getNotesInternes())
                .motifAnnulation(
                        a.getMotifAnnulation())
                .rappelSmsEnvoye(
                        a.isRappelSmsEnvoye())
                .rappelEmailEnvoye(
                        a.isRappelEmailEnvoye())
                .dateCreation(a.getDateCreation())
                .dateModification(
                        a.getDateModification())
                .build();
    }
}
