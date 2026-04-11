package ma.medicabpro.billingservice.entity;



import jakarta.persistence.*;
import lombok.*;
import ma.medicabpro.billingservice.entity.enums.ModePaiement;
import ma.medicabpro.billingservice.entity.enums.StatutPaiement;

import java.time.LocalDateTime;

@Entity
@Table(name = "paiements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paiement {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_id")
    private Facture facture;

    @Column(nullable = false)
    private Double montant;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_paiement",
            nullable = false)
    private ModePaiement modePaiement;

    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;

    private String reference;

    @Column(name = "numero_cheque")
    private String numeroCheque;

    @Enumerated(EnumType.STRING)
    private StatutPaiement statut;

    @Column(name = "date_creation",
            updatable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
        if (this.datePaiement == null) {
            this.datePaiement =
                    LocalDateTime.now();
        }
        if (this.statut == null) {
            this.statut = StatutPaiement.VALIDE;
        }
    }
}