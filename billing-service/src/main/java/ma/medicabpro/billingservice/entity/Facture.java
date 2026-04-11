package ma.medicabpro.billingservice.entity;



import jakarta.persistence.*;
import lombok.*;
import ma.medicabpro.billingservice.entity.enums.StatutFacture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "factures")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Facture {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id",
            nullable = false)
    private Long tenantId;

    @Column(name = "numero_facture",
            unique = true)
    private String numeroFacture;

    @Column(name = "patient_id",
            nullable = false)
    private Long patientId;

    @Column(name = "medecin_id")
    private Long medecinId;

    @Column(name = "consultation_id")
    private Long consultationId;

    // ── Montants ───────────────────────────
    @Column(name = "montant_ht")
    private Double montantHT;

    @Column(name = "tva")
    private Double tva = 0.0;

    @Column(name = "montant_ttc",
            nullable = false)
    private Double montantTTC;

    @Column(name = "montant_paye")
    private Double montantPaye = 0.0;

    // ── Statut ─────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutFacture statut;

    // ── Dates ──────────────────────────────
    @Column(name = "date_emission")
    private LocalDate dateEmission;

    @Column(name = "date_echeance")
    private LocalDate dateEcheance;

    @Column(name = "notes_facture",
            length = 1000)
    private String notesFacture;

    // ── Paiements ──────────────────────────
    @OneToMany(mappedBy = "facture",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<Paiement> paiements =
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
        if (this.statut == null) {
            this.statut = StatutFacture.EMISE;
        }
        if (this.dateEmission == null) {
            this.dateEmission = LocalDate.now();
        }
        if (this.dateEcheance == null) {
            this.dateEcheance =
                    LocalDate.now().plusDays(30);
        }
        if (this.numeroFacture == null) {
            this.numeroFacture = "FACT-"
                    + System.currentTimeMillis();
        }
        if (this.montantPaye == null) {
            this.montantPaye = 0.0;
        }
        if (this.montantHT == null) {
            this.montantHT = this.montantTTC;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.dateModification =
                LocalDateTime.now();
    }

    // ── Méthodes métier ────────────────────
    public double getMontantRestant() {
        return montantTTC - montantPaye;
    }

    public boolean isPayeeCompletement() {
        return montantPaye >= montantTTC;
    }

    public void ajouterPaiement(
            double montant) {
        this.montantPaye += montant;
        if (isPayeeCompletement()) {
            this.statut = StatutFacture.PAYEE;
        } else {
            this.statut =
                    StatutFacture.PARTIELLEMENT_PAYEE;
        }
    }
}