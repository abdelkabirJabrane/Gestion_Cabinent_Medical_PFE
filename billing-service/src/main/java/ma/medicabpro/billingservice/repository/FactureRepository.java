package ma.medicabpro.billingservice.repository;


import ma.medicabpro.billingservice.entity.Facture;
import ma.medicabpro.billingservice.entity.enums.StatutFacture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FactureRepository
        extends JpaRepository<Facture, Long> {

    List<Facture> findByPatientIdOrderByDateCreationDesc(
            Long patientId);

    List<Facture> findByTenantIdOrderByDateCreationDesc(
            Long tenantId);

    List<Facture> findByTenantIdAndStatut(
            Long tenantId, StatutFacture statut);

    long countByTenantId(Long tenantId);

    // ── Total encaissé ─────────────────────
    @Query("""
        SELECT SUM(f.montantTTC)
        FROM Facture f
        WHERE f.tenantId = :tenantId
        AND f.statut = 'PAYEE'
    """)
    Double getTotalEncaisse(
            @Param("tenantId") Long tenantId);

    // ✅ CORRIGÉ — utiliser montantTTC - montantPaye
    @Query("""
        SELECT SUM(f.montantTTC - f.montantPaye)
        FROM Facture f
        WHERE f.tenantId = :tenantId
        AND f.statut IN ('EMISE',
            'PARTIELLEMENT_PAYEE')
    """)
    Double getTotalImpaye(
            @Param("tenantId") Long tenantId);
}