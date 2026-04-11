package ma.medicabpro.patientservice.repository;


import ma.medicabpro.patientservice.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository
        extends JpaRepository<Patient, Long> {

    // ── Recherches simples ─────────────────
    Optional<Patient> findByCin(String cin);
    Optional<Patient> findByEmail(String email);
    boolean existsByCin(String cin);
    boolean existsByEmail(String email);

    // ── Par cabinet (tenant) ───────────────
    List<Patient> findByTenantIdAndActifTrue(
            Long tenantId);

    // ── Recherche globale ──────────────────
    @Query("""
        SELECT p FROM Patient p
        WHERE p.tenantId = :tenantId
        AND p.actif = true
        AND (
            LOWER(p.nom) LIKE LOWER(CONCAT('%',:q,'%'))
            OR LOWER(p.prenom) LIKE LOWER(CONCAT('%',:q,'%'))
            OR LOWER(p.cin) LIKE LOWER(CONCAT('%',:q,'%'))
            OR LOWER(p.telephone) LIKE LOWER(CONCAT('%',:q,'%'))
        )
        ORDER BY p.nom ASC
    """)
    List<Patient> rechercherPatients(
            @Param("tenantId") Long tenantId,
            @Param("q") String query
    );

    // ── Statistiques ───────────────────────
    long countByTenantIdAndActifTrue(Long tenantId);

    @Query("""
        SELECT COUNT(p) FROM Patient p
        WHERE p.tenantId = :tenantId
        AND p.actif = true
        AND p.typeAssurance = 'CNSS'
    """)
    long countPatientsCNSS(
            @Param("tenantId") Long tenantId);
}