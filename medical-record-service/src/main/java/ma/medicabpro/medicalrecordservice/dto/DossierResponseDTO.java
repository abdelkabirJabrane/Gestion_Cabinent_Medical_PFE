package ma.medicabpro.medicalrecordservice.dto;



import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierResponseDTO {

    private Long id;
    private Long tenantId;
    private Long patientId;
    private String numeroDossier;
    private String antecedentsFamiliaux;
    private String antecedentsPersonnels;
    private String allergies;
    private String medicamentsEnCours;
    private String notesGenerales;
    private Double poids;
    private Double taille;
    private Double imc;
    private String groupeSanguin;
    private int nombreConsultations;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}