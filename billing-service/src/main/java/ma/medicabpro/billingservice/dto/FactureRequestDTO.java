package ma.medicabpro.billingservice.dto;




import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactureRequestDTO {

    @NotNull(message = "Tenant obligatoire")
    private Long tenantId;

    @NotNull(message = "Patient obligatoire")
    private Long patientId;

    private Long medecinId;
    private Long consultationId;

    @NotNull(message = "Montant obligatoire")
    @Min(value = 0,
            message = "Montant doit être positif")
    private Double montantTTC;

    private Double tva;
    private String notesFacture;
}