package ma.medicabpro.appointmentservice.dto;


import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponseDTO {

    private Long id;
    private Long tenantId;
    private Long patientId;
    private String patientNom;
    private Long medecinId;
    private LocalDateTime dateHeureDebut;
    private LocalDateTime dateHeureFin;
    private String statut;
    private String typeConsultation;
    private String motif;
    private String notesInternes;
    private String motifAnnulation;
    private boolean rappelSmsEnvoye;
    private boolean rappelEmailEnvoye;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}