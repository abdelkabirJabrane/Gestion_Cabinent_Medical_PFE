package ma.medicabpro.medicalrecordservice.entity;



import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lignes_ordonnance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneOrdonnance {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordonnance_id")
    private Ordonnance ordonnance;

    @Column(nullable = false)
    private String medicament;

    private String dci;
    private String dosage;
    private String forme;
    private String posologie;

    @Column(name = "duree_traitement")
    private Integer dureeTraitement;

    private String unite;
    private String instructions;
    private boolean substituable = true;
}