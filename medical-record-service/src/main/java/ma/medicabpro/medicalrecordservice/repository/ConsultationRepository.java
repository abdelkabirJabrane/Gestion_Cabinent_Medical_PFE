package ma.medicabpro.medicalrecordservice.repository;


import ma.medicabpro.medicalrecordservice.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConsultationRepository
        extends JpaRepository<Consultation, Long> {

    List<Consultation> findByDossierIdOrderByDateHeureDesc(Long dossierId);
    List<Consultation> findByMedecinIdOrderByDateHeureDesc(Long medecinId);
    List<Consultation> findByTenantIdOrderByDateHeureDesc(Long tenantId);
    long countByTenantId(Long tenantId);
}
