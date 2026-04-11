package ma.medicabpro.medicalrecordservice.dto;



import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationRequestDTO {

    @NotNull(message = "Tenant obligatoire")
    private Long tenantId;

    @NotNull(message = "Dossier obligatoire")
    private Long dossierId;

    @NotNull(message = "Médecin obligatoire")
    private Long medecinId;

    private Long rdvId;
    private LocalDateTime dateHeure;

    @NotBlank(message = "Motif obligatoire")
    private String motif;

    private String anamnese;
    private String examenClinique;
    private String diagnostic;
    private String conclusion;
    private String recommandations;
    private String typeConsultation;

    // Constantes vitales
    private Double tensionSystolique;
    private Double tensionDiastolique;
    private Double frequenceCardiaque;
    private Double temperature;
    private Double saturationO2;
    private Double poids;
    private Double taille;
    private Double montantTotal;
}
