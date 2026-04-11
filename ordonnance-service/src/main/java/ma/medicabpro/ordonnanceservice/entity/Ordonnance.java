package ma.medicabpro.ordonnanceservice.entity;

import jakarta.persistence.*;
import lombok.*;
import ma.medicabpro.ordonnanceservice.entity.enums.StatutOrdonnance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordonnances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ordonnance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consultation_id", nullable = false)
    private Long consultationId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "medecin_id", nullable = false)
    private Long medecinId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "date_emission")
    private LocalDate dateEmission;

    @Column(name = "date_validite")
    private LocalDate dateValidite;

    @Column(length = 2000)
    private String instructions;

    private boolean renouvellement = false;

    @Enumerated(EnumType.STRING)
    private StatutOrdonnance statut;

    @OneToMany(mappedBy = "ordonnance",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER)
    @Builder.Default
    private List<LigneOrdonnance> lignes = new ArrayList<>();

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
        if (this.dateEmission == null) {
            this.dateEmission = LocalDate.now();
        }
        if (this.dateValidite == null) {
            this.dateValidite = LocalDate.now().plusMonths(3);
        }
        if (this.statut == null) {
            this.statut = StatutOrdonnance.ACTIVE;
        }
    }

    public boolean isValide() {
        return statut == StatutOrdonnance.ACTIVE
                && LocalDate.now().isBefore(dateValidite);
    }
}
