package ma.medicabpro.notificationservice.entity;



import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id",
            nullable = false)
    private Long tenantId;

    @Column(name = "destinataire_id")
    private Long destinataireId;

    @Column(name = "destinataire_contact",
            nullable = false)
    private String destinataireContact;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeNotification type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanalEnvoi canal;

    @Column(nullable = false)
    private String sujet;

    @Column(nullable = false,
            length = 2000)
    private String contenu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutNotification statut;

    @Column(name = "date_programmee")
    private LocalDateTime dateProgrammee;

    @Column(name = "date_envoi")
    private LocalDateTime dateEnvoi;

    @Column(name = "tentatives")
    private int tentatives = 0;

    @Column(name = "erreur_message",
            length = 1000)
    private String erreurMessage;

    @Column(name = "reference_objet")
    private String referenceObjet;

    @Column(name = "date_creation",
            updatable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
        if (this.statut == null) {
            this.statut =
                    StatutNotification.EN_ATTENTE;
        }
    }

    public boolean peutReessayer() {
        return tentatives < 3
                && statut == StatutNotification.ECHEC;
    }
}
