package ma.medicabpro.appointmentservice.repository;


import ma.medicabpro.appointmentservice.entity.Appointment;
import ma.medicabpro.appointmentservice.entity.enums.StatutRDV;
import org.springframework.data.jpa.repository
        .JpaRepository;
import org.springframework.data.jpa.repository
        .Query;
import org.springframework.data.repository.query
        .Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    // Par patient
    List<Appointment> findByPatientIdOrderByDateHeureDebutDesc(
            Long patientId);

    // Par médecin
    List<Appointment> findByMedecinIdOrderByDateHeureDebutAsc(
            Long medecinId);

    // Par tenant
    List<Appointment> findByTenantIdOrderByDateHeureDebutDesc(
            Long tenantId);

    // Par statut
    List<Appointment> findByTenantIdAndStatut(
            Long tenantId, StatutRDV statut);

    // RDV du jour
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.medecinId = :medecinId
        AND a.dateHeureDebut >= :debut
        AND a.dateHeureDebut < :fin
        ORDER BY a.dateHeureDebut ASC
    """)
    List<Appointment> findRDVDuJour(
            @Param("medecinId") Long medecinId,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin
    );

    // Vérifier conflit horaire
    @Query("""
        SELECT COUNT(a) FROM Appointment a
        WHERE a.medecinId = :medecinId
        AND a.statut NOT IN ('ANNULE', 'ABSENT')
        AND a.dateHeureDebut < :fin
        AND a.dateHeureFin > :debut
    """)
    long countConflits(
            @Param("medecinId") Long medecinId,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin
    );

    // Compter par tenant
    long countByTenantId(Long tenantId);
}