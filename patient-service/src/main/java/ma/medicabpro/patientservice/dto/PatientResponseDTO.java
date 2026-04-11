package ma.medicabpro.patientservice.dto;


import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientResponseDTO {

    private Long id;
    private Long tenantId;

    // ── Identité ───────────────────────────
    private String nom;
    private String prenom;
    private String nomComplet;
    private String cin;
    private String email;
    private String telephone;
    private LocalDate dateNaissance;
    private Integer age;
    private String sexe;

    // ── Médical ────────────────────────────
    private String groupeSanguin;
    private String typeAssurance;
    private String noAffiliation;
    private Double poids;
    private Double taille;
    private Double imc;
    private String categorieIMC;

    // ── Adresse ────────────────────────────
    private String ville;
    private String adresse;

    // ── Timestamps ─────────────────────────
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private boolean actif;
}
