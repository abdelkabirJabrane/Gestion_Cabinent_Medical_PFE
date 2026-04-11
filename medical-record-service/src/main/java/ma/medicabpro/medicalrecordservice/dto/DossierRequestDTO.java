package ma.medicabpro.medicalrecordservice.dto;



import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierRequestDTO {

    @NotNull(message = "Tenant obligatoire")
    private Long tenantId;

    @NotNull(message = "Patient obligatoire")
    private Long patientId;

    private String antecedentsFamiliaux;
    private String antecedentsPersonnels;
    private String allergies;
    private String medicamentsEnCours;
    private String notesGenerales;
    private Double poids;
    private Double taille;
    private String groupeSanguin;
}
