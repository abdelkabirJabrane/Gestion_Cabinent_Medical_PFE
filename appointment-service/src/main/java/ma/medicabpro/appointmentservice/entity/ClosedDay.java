package ma.medicabpro.appointmentservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "closed_days")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClosedDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long medecinId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Long tenantId;
}
