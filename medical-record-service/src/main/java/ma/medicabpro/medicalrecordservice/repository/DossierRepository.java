package ma.medicabpro.medicalrecordservice.repository;



import ma.medicabpro.medicalrecordservice.entity.DossierPatient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DossierRepository
        extends JpaRepository<DossierPatient, Long> {

    Optional<DossierPatient> findByPatientId(Long patientId);
    List<DossierPatient> findByTenantId(Long tenantId);
    boolean existsByPatientId(Long patientId);
}
