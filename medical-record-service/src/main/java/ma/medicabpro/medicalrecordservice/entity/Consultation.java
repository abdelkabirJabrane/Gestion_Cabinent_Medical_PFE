package ma.medicabpro.medicalrecordservice.entity;


import jakarta.persistence.*;
import lombok.*;
import ma.medicabpro.medicalrecordservice.entity.enums.TypeConsultation;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consultation {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id")
    private DossierPatient dossier;

    @Column(name = "tenant_id",
            nullable = false)
    private Long tenantId;

    @Column(name = "medecin_id",
            nullable = false)
    private Long medecinId;

    @Column(name = "rdv_id")
    private Long rdvId;

    @Column(name = "date_heure",
            nullable = false)
    private LocalDateTime dateHeure;

    // ── Infos médicales ────────────────────
    @Column(nullable = false)
    private String motif;

    @Column(length = 3000)
    private String anamnese;

    @Column(name = "examen_clinique",
            length = 3000)
    private String examenClinique;

    @Column(length = 3000)
    private String diagnostic;

    @Column(length = 3000)
    private String conclusion;

    @Column(length = 3000)
    private String recommandations;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_consultation")
    private TypeConsultation typeConsultation;

    // ── Constantes vitales ─────────────────
    @Column(name = "tension_systolique")
    private Double tensionSystolique;

    @Column(name = "tension_diastolique")
    private Double tensionDiastolique;

    @Column(name = "frequence_cardiaque")
    private Double frequenceCardiaque;

    private Double temperature;

    @Column(name = "saturation_o2")
    private Double saturationO2;

    private Double poids;
    private Double taille;

    // ── Financier ──────────────────────────
    @Column(name = "montant_total")
    private Double montantTotal;

    @Column(name = "analyse_ai_effectuee")
    private boolean analyseAIEffectuee = false;

    // ── Ordonnance ─────────────────────────
    @OneToOne(mappedBy = "consultation",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private Ordonnance ordonnance;

    // ── Timestamps ─────────────────────────
    @Column(name = "date_creation",
            updatable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
        if (this.dateHeure == null) {
            this.dateHeure = LocalDateTime.now();
        }
    }

    // ── Méthodes métier ────────────────────
    public double getIMC() {
        if (poids == null || taille == null
                || taille == 0) return 0;
        return Math.round(
                (poids / (taille * taille))
                        * 100.0) / 100.0;
    }
}
