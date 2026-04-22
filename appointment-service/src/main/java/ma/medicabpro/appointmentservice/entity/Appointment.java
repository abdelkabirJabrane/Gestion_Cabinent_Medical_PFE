package ma.medicabpro.appointmentservice.entity;



import jakarta.persistence.*;
import lombok.*;
import ma.medicabpro.appointmentservice.entity.enums.StatutRDV;
import ma.medicabpro.appointmentservice.entity.enums.TypeConsultation;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Multi-tenant ───────────────────────
    @Column(name = "tenant_id",
            nullable = false)
    private Long tenantId;

    // ── Références ─────────────────────────
    @Column(name = "patient_id",
            nullable = false)
    private Long patientId;

    @Column(name = "medecin_id",
            nullable = false)
    private Long medecinId;

    // ── Date & Heure ───────────────────────
    @Column(name = "date_heure_debut",
            nullable = false)
    private LocalDateTime dateHeureDebut;

    @Column(name = "date_heure_fin")
    private LocalDateTime dateHeureFin;

    // ── Statut & Type ──────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutRDV statut;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_consultation")
    private TypeConsultation typeConsultation;

    // ── Infos médicales ────────────────────
    @Column(nullable = false)
    private String motif;

    @Column(name = "notes_internes",
            length = 1000)
    private String notesInternes;

    @Column(name = "motif_annulation")
    private String motifAnnulation;

    // ── Rappels ────────────────────────────
    @Builder.Default
    @Column(name = "rappel_sms_envoye")
    private boolean rappelSmsEnvoye = false;

    @Builder.Default
    @Column(name = "rappel_email_envoye")
    private boolean rappelEmailEnvoye = false;

    // ── Timestamps ─────────────────────────
    @Column(name = "date_creation",
            updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    // ── Hooks JPA ──────────────────────────
    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
        if (this.statut == null) {
            this.statut = StatutRDV.EN_ATTENTE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.dateModification = LocalDateTime.now();
    }

    // ── Méthodes métier ────────────────────
    public void confirmer() {
        this.statut = StatutRDV.CONFIRME;
    }

    public void annuler(String motif) {
        this.statut = StatutRDV.ANNULE;
        this.motifAnnulation = motif;
    }

    public void terminer() {
        this.statut = StatutRDV.TERMINE;
    }

    public void enCours() {
        this.statut = StatutRDV.EN_COURS;
    }

    public void marquerAbsent() {
        this.statut = StatutRDV.ABSENT;
    }
}