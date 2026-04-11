package ma.medicabpro.medicalrecordservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dossiers_patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierPatient {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id",
            nullable = false)
    private Long tenantId;

    @Column(name = "patient_id",
            nullable = false, unique = true)
    private Long patientId;

    @Column(name = "numero_dossier",
            unique = true)
    private String numeroDossier;

    // ── Antécédents ────────────────────────
    @Column(name = "antecedents_familiaux",
            length = 2000)
    private String antecedentsFamiliaux;

    @Column(name = "antecedents_personnels",
            length = 2000)
    private String antecedentsPersonnels;

    @Column(length = 1000)
    private String allergies;

    @Column(name = "medicaments_en_cours",
            length = 1000)
    private String medicamentsEnCours;

    @Column(name = "notes_generales",
            length = 2000)
    private String notesGenerales;

    // ── Constantes ─────────────────────────
    private Double poids;
    private Double taille;

    @Column(name = "groupe_sanguin",
            length = 5)
    private String groupeSanguin;

    // ── Consultations ──────────────────────
    @OneToMany(mappedBy = "dossier",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<Consultation> consultations =
            new ArrayList<>();

    // ── Timestamps ─────────────────────────
    @Column(name = "date_creation",
            updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
        this.dateModification =
                LocalDateTime.now();
        if (this.numeroDossier == null) {
            this.numeroDossier = "DOS-"
                    + System.currentTimeMillis();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.dateModification =
                LocalDateTime.now();
    }

    // ── Méthodes métier ────────────────────
    public double getIMC() {
        if (poids == null || taille == null
                || taille == 0) return 0;
        return Math.round(
                (poids / (taille * taille))
                        * 100.0) / 100.0;
    }

    public boolean aAllergie(
            String medicament) {
        if (allergies == null) return false;
        return allergies.toLowerCase()
                .contains(
                        medicament.toLowerCase());
    }
}