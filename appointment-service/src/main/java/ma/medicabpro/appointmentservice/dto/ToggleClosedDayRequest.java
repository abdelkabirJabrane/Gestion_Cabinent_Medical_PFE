package ma.medicabpro.appointmentservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ToggleClosedDayRequest {
    @NotNull
    private Long medecinId;

    @NotNull
    private LocalDate date;

    @NotNull
    private Long tenantId;
}
