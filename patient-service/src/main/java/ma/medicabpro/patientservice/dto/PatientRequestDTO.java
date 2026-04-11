package ma.medicabpro.patientservice.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientRequestDTO {

    private Long tenantId;
    private String nom;
    private String prenom;
    private String cin;
    private String email;
    private String telephone;
    private LocalDate dateNaissance;
    private String sexe;
    private String groupeSanguin;
    private String typeAssurance;
    private String noAffiliation;
    private Double poids;
    private Double taille;
    private String ville;
    private String adresse;
}