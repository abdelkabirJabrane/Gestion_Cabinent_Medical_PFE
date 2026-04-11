package ma.medicabpro.ordonnanceservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdonnanceRequestDTO {

    @NotNull(message = "Tenant obligatoire")
    private Long tenantId;

    @NotNull(message = "Consultation obligatoire")
    private Long consultationId;

    @NotNull(message = "Patient obligatoire")
    private Long patientId;

    @NotNull(message = "Médecin obligatoire")
    private Long medecinId;

    private LocalDate dateEmission;
    private LocalDate dateValidite;
    private String instructions;
    private boolean renouvellement = false;
    private String statut;

    @Valid
    @Builder.Default
    private List<LigneOrdonnanceRequestDTO> lignes = new ArrayList<>();
}
