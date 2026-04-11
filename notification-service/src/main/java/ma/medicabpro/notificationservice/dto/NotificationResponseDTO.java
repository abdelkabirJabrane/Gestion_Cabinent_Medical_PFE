package ma.medicabpro.notificationservice.dto;



import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDTO {

    private Long id;
    private Long tenantId;
    private Long destinataireId;
    private String destinataireContact;
    private String type;
    private String canal;
    private String sujet;
    private String contenu;
    private String statut;
    private LocalDateTime dateProgrammee;
    private LocalDateTime dateEnvoi;
    private int tentatives;
    private String erreurMessage;
    private String referenceObjet;
    private LocalDateTime dateCreation;
}
