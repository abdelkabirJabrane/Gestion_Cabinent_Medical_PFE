package ma.medicabpro.notificationservice.service;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.medicabpro.notificationservice.dto.NotificationRequestDTO;
import ma.medicabpro.notificationservice.dto.NotificationResponseDTO;
import ma.medicabpro.notificationservice.entity.CanalEnvoi;
import ma.medicabpro.notificationservice.entity.Notification;
import ma.medicabpro.notificationservice.entity.StatutNotification;
import ma.medicabpro.notificationservice.entity.TypeNotification;
import ma.medicabpro.notificationservice.exception.NotificationNotFoundException;
import ma.medicabpro.notificationservice.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation
        .Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl
        implements NotificationService {

    private final NotificationRepository repo;

    // ── Envoyer immédiatement ──────────────
    @Override
    public NotificationResponseDTO envoyer(
            NotificationRequestDTO dto) {

        log.info("Envoi notification {} via {}",
                dto.getType(), dto.getCanal());

        Notification notif = buildNotification(dto);
        notif.setStatut(
                StatutNotification.EN_COURS);

        // Simulation envoi
        boolean succes = simulerEnvoi(
                dto.getCanal(),
                dto.getDestinataireContact(),
                dto.getContenu());

        if (succes) {
            notif.setStatut(
                    StatutNotification.ENVOYEE);
            notif.setDateEnvoi(
                    LocalDateTime.now());
            log.info("✅ Notification envoyée à {}",
                    dto.getDestinataireContact());
        } else {
            notif.setStatut(
                    StatutNotification.ECHEC);
            notif.setErreurMessage(
                    "Échec envoi simulé");
            log.warn("❌ Échec notification à {}",
                    dto.getDestinataireContact());
        }

        notif.setTentatives(1);
        return toDTO(repo.save(notif));
    }

    // ── Programmer pour plus tard ──────────
    @Override
    public NotificationResponseDTO programmer(
            NotificationRequestDTO dto) {

        log.info("Programmation notification {}",
                dto.getType());

        Notification notif = buildNotification(dto);
        notif.setStatut(
                StatutNotification.PROGRAMMEE);
        notif.setDateProgrammee(
                dto.getDateProgrammee());

        return toDTO(repo.save(notif));
    }

    // ── Get par ID ─────────────────────────
    @Override
    @Transactional(readOnly = true)
    public NotificationResponseDTO getById(
            Long id) {
        return toDTO(repo.findById(id)
                .orElseThrow(() ->
                        new NotificationNotFoundException(
                                "Notification introuvable: "
                                        + id)));
    }

    // ── Get par tenant ─────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO>
    getByTenant(Long tenantId) {
        return repo
                .findByTenantIdOrderByDateCreationDesc(
                        tenantId)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Get par statut ─────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO>
    getByStatut(
            Long tenantId, String statut) {
        return repo.findByTenantIdAndStatut(
                        tenantId,
                        StatutNotification.valueOf(statut))
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Annuler ────────────────────────────
    @Override
    public NotificationResponseDTO annuler(
            Long id) {
        Notification notif = repo.findById(id)
                .orElseThrow(() ->
                        new NotificationNotFoundException(
                                "Notification introuvable: "
                                        + id));

        if (notif.getStatut()
                == StatutNotification.ENVOYEE) {
            throw new RuntimeException(
                    "Impossible d'annuler une notification déjà envoyée !");
        }

        notif.setStatut(
                StatutNotification.ANNULEE);
        return toDTO(repo.save(notif));
    }

    // ── Compter ────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public long countNotifications(
            Long tenantId) {
        return repo.countByTenantId(tenantId);
    }

    // ── Rappel RDV ─────────────────────────
    @Override
    public NotificationResponseDTO
    envoyerRappelRDV(
            Long tenantId,
            Long patientId,
            String contact,
            String canal,
            String dateRDV,
            String medecinNom) {

        String contenu = canal.equals("SMS")
                ? "Rappel: Votre RDV avec Dr "
                + medecinNom + " le " + dateRDV
                + ". Cabinet MediCab Pro."
                : "Rappel de votre rendez-vous médical\n\n"
                + "Médecin : Dr " + medecinNom + "\n"
                + "Date    : " + dateRDV + "\n\n"
                + "MediCab Pro";

        NotificationRequestDTO dto =
                NotificationRequestDTO.builder()
                        .tenantId(tenantId)
                        .destinataireId(patientId)
                        .destinataireContact(contact)
                        .type("RAPPEL_RDV_"
                                + canal.toUpperCase())
                        .canal(canal)
                        .sujet("Rappel RDV - "
                                + dateRDV)
                        .contenu(contenu)
                        .build();

        return envoyer(dto);
    }

    // ── Confirmation RDV ───────────────────
    @Override
    public NotificationResponseDTO
    envoyerConfirmationRDV(
            Long tenantId,
            Long patientId,
            String contact,
            String canal,
            String dateRDV) {

        String contenu = "Votre RDV du "
                + dateRDV
                + " est confirmé. MediCab Pro.";

        NotificationRequestDTO dto =
                NotificationRequestDTO.builder()
                        .tenantId(tenantId)
                        .destinataireId(patientId)
                        .destinataireContact(contact)
                        .type("CONFIRMATION_RDV")
                        .canal(canal)
                        .sujet("Confirmation RDV")
                        .contenu(contenu)
                        .build();

        return envoyer(dto);
    }

    // ── Facture disponible ─────────────────
    @Override
    public NotificationResponseDTO
    envoyerFactureDisponible(
            Long tenantId,
            Long patientId,
            String contact,
            String numeroFacture,
            double montant) {

        String contenu = "Votre facture "
                + numeroFacture
                + " d'un montant de "
                + montant + " MAD est disponible. "
                + "MediCab Pro.";

        NotificationRequestDTO dto =
                NotificationRequestDTO.builder()
                        .tenantId(tenantId)
                        .destinataireId(patientId)
                        .destinataireContact(contact)
                        .type("FACTURE_DISPONIBLE")
                        .canal("EMAIL")
                        .sujet("Facture disponible - "
                                + numeroFacture)
                        .contenu(contenu)
                        .build();

        return envoyer(dto);
    }

    // ── Helper : simuler envoi ─────────────
    private boolean simulerEnvoi(
            String canal,
            String contact,
            String contenu) {
        log.info("📤 Simulation envoi {} → {}",
                canal, contact);
        log.info("📝 Contenu: {}", contenu);
        // Simulation : 95% de succès
        return Math.random() > 0.05;
    }

    // ── Helper : builder ───────────────────
    private Notification buildNotification(
            NotificationRequestDTO dto) {
        return Notification.builder()
                .tenantId(dto.getTenantId())
                .destinataireId(
                        dto.getDestinataireId())
                .destinataireContact(
                        dto.getDestinataireContact())
                .type(TypeNotification.valueOf(
                        dto.getType()))
                .canal(CanalEnvoi.valueOf(
                        dto.getCanal()))
                .sujet(dto.getSujet())
                .contenu(dto.getContenu())
                .referenceObjet(
                        dto.getReferenceObjet())
                .build();
    }

    // ── Mapper ─────────────────────────────
    private NotificationResponseDTO toDTO(
            Notification n) {
        return NotificationResponseDTO.builder()
                .id(n.getId())
                .tenantId(n.getTenantId())
                .destinataireId(
                        n.getDestinataireId())
                .destinataireContact(
                        n.getDestinataireContact())
                .type(n.getType().name())
                .canal(n.getCanal().name())
                .sujet(n.getSujet())
                .contenu(n.getContenu())
                .statut(n.getStatut().name())
                .dateProgrammee(
                        n.getDateProgrammee())
                .dateEnvoi(n.getDateEnvoi())
                .tentatives(n.getTentatives())
                .erreurMessage(n.getErreurMessage())
                .referenceObjet(n.getReferenceObjet())
                .dateCreation(n.getDateCreation())
                .build();
    }
}