package ma.medicabpro.medicalrecordservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "medical_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long dossierId; // Use simple ID to avoid modifying DossierPatient entity for now
    private String nom;
    private String type; 
    private String url;
    private String mimeType;
    private LocalDateTime dateAjout;

    @PrePersist
    public void prePersist() {
        this.dateAjout = LocalDateTime.now();
    }
}
