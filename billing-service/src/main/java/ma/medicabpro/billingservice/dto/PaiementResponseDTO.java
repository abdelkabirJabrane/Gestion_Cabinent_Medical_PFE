package ma.medicabpro.billingservice.dto;



import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaiementResponseDTO {

    private Long id;
    private Long factureId;
    private Double montant;
    private String modePaiement;
    private LocalDateTime datePaiement;
    private String reference;
    private String statut;
    private LocalDateTime dateCreation;
}
