package ma.medicabpro.billingservice.dto;



import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaiementRequestDTO {

    @NotNull(message = "Facture obligatoire")
    private Long factureId;

    @NotNull(message = "Montant obligatoire")
    @Min(value = 1,
            message = "Montant doit être > 0")
    private Double montant;

    @NotBlank(message = "Mode paiement obligatoire")
    private String modePaiement;

    private String reference;
    private String numeroCheque;
}
