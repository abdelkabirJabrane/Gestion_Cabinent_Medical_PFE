package com.shop.user_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cabinets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cabinet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    @NotBlank(message = "Le nom du cabinet est obligatoire")
    private String nom;

    @Column(length = 150)
    private String responsable; // Nom du médecin responsable

    @Column(nullable = false, unique = true, length = 100)
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @Column(length = 20)
    private String telephone;

    @Column(length = 100)
    private String ville;

    @Column(name = "nb_medecins")
    @Builder.Default
    private Integer nbMedecins = 0;

    @Column(length = 50)
    @Builder.Default
    private String plan = "Starter"; // Starter, Pro, Business, Enterprise

    @Column(name = "date_expiration")
    private LocalDate dateExpiration;

    @Column(length = 20)
    @Builder.Default
    private String statut = "essai"; // actif, essai, suspendu

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper process pour le frontend
    public Long getTenantId() {
        return this.id;
    }
}
