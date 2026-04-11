package ma.medicabpro.billingservice.dto;



import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactureResponseDTO {

    private Long id;
    private Long tenantId;
    private String numeroFacture;
    private Long patientId;
    private Long medecinId;
    private Long consultationId;
    private Double montantHT;
    private Double tva;
    private Double montantTTC;
    private Double montantPaye;
    private Double montantRestant;
    private String statut;
    private LocalDate dateEmission;
    private LocalDate dateEcheance;
    private String notesFacture;
    private List<PaiementResponseDTO> paiements;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
