package ma.medicabpro.medicalrecordservice.dto;



import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationResponseDTO {

    private Long id;
    private Long tenantId;
    private Long dossierId;
    private Long medecinId;
    private Long rdvId;
    private LocalDateTime dateHeure;
    private String motif;
    private String anamnese;
    private String examenClinique;
    private String diagnostic;
    private String conclusion;
    private String recommandations;
    private String typeConsultation;
    private Double tensionSystolique;
    private Double tensionDiastolique;
    private Double frequenceCardiaque;
    private Double temperature;
    private Double saturationO2;
    private Double poids;
    private Double taille;
    private Double imc;
    private Double montantTotal;
    private boolean analyseAIEffectuee;
    private boolean hasOrdonnance;
    private LocalDateTime dateCreation;
}
