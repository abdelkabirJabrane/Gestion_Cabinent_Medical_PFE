package ma.medicabpro.appointmentservice.repository;

import ma.medicabpro.appointmentservice.entity.ClosedDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClosedDayRepository extends JpaRepository<ClosedDay, Long> {
    List<ClosedDay> findByMedecinIdAndTenantId(Long medecinId, Long tenantId);
    List<ClosedDay> findByMedecinId(Long medecinId);  // Pour la vue publique (sans filtre tenant)
    Optional<ClosedDay> findByMedecinIdAndDateAndTenantId(Long medecinId, LocalDate date, Long tenantId);
}
