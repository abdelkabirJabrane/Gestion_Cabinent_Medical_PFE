package com.shop.user_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    @NotBlank(message = "L'identifiant du plan est obligatoire")
    private String planId; // Ex: Starter, Pro, Enterprise

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Le nom du plan est obligatoire")
    private String label; // Ex: Professionnel

    @Column
    private Integer prix; // null = Sur devis

    @Column(length = 20)
    @Builder.Default
    private String color = "#10b981";

    // Features stockées comme texte séparé par \n
    @Column(columnDefinition = "TEXT")
    private String features;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
