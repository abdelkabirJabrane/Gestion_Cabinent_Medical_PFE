package ma.medicabpro.ordonnanceservice.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdonnanceResponseDTO {

    private Long id;
    private Long tenantId;
    private Long consultationId;
    private Long patientId;
    private Long medecinId;
    private LocalDate dateEmission;
    private LocalDate dateValidite;
    private String instructions;
    private boolean renouvellement;
    private String statut;
    private boolean valide;
    private List<LigneOrdonnanceResponseDTO> lignes;
    private LocalDateTime dateCreation;
}
