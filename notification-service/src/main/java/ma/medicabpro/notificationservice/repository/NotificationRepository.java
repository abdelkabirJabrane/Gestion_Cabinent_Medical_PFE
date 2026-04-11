package ma.medicabpro.notificationservice.repository;



import ma.medicabpro.notificationservice.entity.Notification;
import ma.medicabpro.notificationservice.entity.StatutNotification;
import ma.medicabpro.notificationservice.entity.TypeNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByTenantIdOrderByDateCreationDesc(
            Long tenantId);

    // ✅ CORRECT — I majuscule obligatoire
    List<Notification> findByDestinataireIdOrderByDateCreationDesc(
            Long destinataireId);

    List<Notification> findByTenantIdAndStatut(
            Long tenantId,
            StatutNotification statut);

    List<Notification> findByTenantIdAndType(
            Long tenantId,
            TypeNotification type);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndStatut(
            Long tenantId,
            StatutNotification statut);
}