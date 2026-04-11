package ma.medicabpro.notificationservice.dto;


import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequestDTO {

    @NotNull(message = "Tenant obligatoire")
    private Long tenantId;

    private Long destinataireId;

    @NotBlank(message = "Contact obligatoire")
    private String destinataireContact;

    @NotBlank(message = "Type obligatoire")
    private String type;

    @NotBlank(message = "Canal obligatoire")
    private String canal;

    @NotBlank(message = "Sujet obligatoire")
    private String sujet;

    @NotBlank(message = "Contenu obligatoire")
    private String contenu;

    private LocalDateTime dateProgrammee;
    private String referenceObjet;
}