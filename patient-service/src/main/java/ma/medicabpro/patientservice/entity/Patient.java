package ma.medicabpro.patientservice.entity;


import jakarta.persistence.*;
import lombok.*;
import ma.medicabpro.patientservice.entity.enums.Sexe;
import ma.medicabpro.patientservice.entity.enums.TypeAssurance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Multi-tenant ───────────────────────
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    // ── Identité ───────────────────────────
    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(unique = true, nullable = false, length = 10)
    private String cin;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false, length = 15)
    private String telephone;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Enumerated(EnumType.STRING)
    private Sexe sexe;

    // ── Médical ────────────────────────────
    @Column(name = "groupe_sanguin", length = 5)
    private String groupeSanguin;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_assurance")
    private TypeAssurance typeAssurance;

    @Column(name = "no_affiliation")
    private String noAffiliation;

    private Double poids;
    private Double taille;

    // ── Adresse ────────────────────────────
    private String ville;
    private String adresse;

    // ── Timestamps ─────────────────────────
    @Column(name = "date_creation",
            updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @Column(name = "actif")
    private boolean actif = true;

    // ── Hooks JPA ──────────────────────────
    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
        this.actif = true;
    }

    @PreUpdate
    public void preUpdate() {
        this.dateModification = LocalDateTime.now();
    }

    // ── Méthodes métier ────────────────────
    public int getAge() {
        if (this.dateNaissance == null) return 0;
        return Period.between(
                this.dateNaissance,
                LocalDate.now()
        ).getYears();
    }

    public double getIMC() {
        if (this.poids == null
                || this.taille == null
                || this.taille == 0) return 0;
        return Math.round(
                (this.poids / (this.taille * this.taille))
                        * 100.0) / 100.0;
    }

    public String getNomComplet() {
        return this.prenom + " " + this.nom;
    }
}
