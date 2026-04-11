package ma.medicabpro.ordonnanceservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneOrdonnanceRequestDTO {

    @NotBlank(message = "Médicament obligatoire")
    private String medicament;

    private String dci;
    private String dosage;
    private String forme;
    private String posologie;
    private Integer dureeTraitement;
    private String unite;
    private String instructions;
    private boolean substituable = true;
}
