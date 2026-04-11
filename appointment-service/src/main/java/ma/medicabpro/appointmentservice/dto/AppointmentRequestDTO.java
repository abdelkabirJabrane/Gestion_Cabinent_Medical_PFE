package ma.medicabpro.appointmentservice.dto;


import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentRequestDTO {

    @NotNull(message = "Tenant obligatoire")
    private Long tenantId;

    @NotNull(message = "Patient obligatoire")
    private Long patientId;

    @NotNull(message = "Médecin obligatoire")
    private Long medecinId;

    @NotNull(message = "Date/Heure obligatoire")
    @Future(message = "La date doit être dans le futur")
    private LocalDateTime dateHeureDebut;

    private LocalDateTime dateHeureFin;

    @NotBlank(message = "Motif obligatoire")
    @Size(min = 3, max = 500,
            message = "Motif entre 3 et 500 caractères")
    private String motif;

    private String typeConsultation;
    private String notesInternes;
}