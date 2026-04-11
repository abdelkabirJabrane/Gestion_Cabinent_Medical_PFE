package ma.medicabpro.ordonnanceservice.repository;

import ma.medicabpro.ordonnanceservice.entity.Ordonnance;
import ma.medicabpro.ordonnanceservice.entity.enums.StatutOrdonnance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdonnanceRepository extends JpaRepository<Ordonnance, Long> {

    List<Ordonnance> findByPatientIdOrderByDateCreationDesc(Long patientId);

    List<Ordonnance> findByMedecinIdOrderByDateCreationDesc(Long medecinId);

    List<Ordonnance> findByTenantIdOrderByDateCreationDesc(Long tenantId);

    Optional<Ordonnance> findByConsultationId(Long consultationId);

    List<Ordonnance> findByTenantIdAndStatutOrderByDateCreationDesc(Long tenantId, StatutOrdonnance statut);

    long countByTenantId(Long tenantId);
}
