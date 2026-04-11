package ma.medicabpro.medicalrecordservice.repository;

import ma.medicabpro.medicalrecordservice.entity.MedicalDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedicalDocumentRepository extends JpaRepository<MedicalDocument, Long> {
    List<MedicalDocument> findByDossierId(Long dossierId);
}
